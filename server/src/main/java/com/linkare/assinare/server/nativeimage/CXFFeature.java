package com.linkare.assinare.server.nativeimage;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.jws.WebService;
import javax.xml.namespace.QName;

import org.apache.commons.lang3.StringUtils;
import org.apache.cxf.common.injection.NoJSR250Annotations;
import org.graalvm.nativeimage.hosted.RuntimeClassInitialization;
import org.graalvm.nativeimage.hosted.RuntimeReflection;

import com.oracle.svm.core.annotate.AutomaticFeature;
import com.oracle.svm.core.jdk.Resources;
import com.oracle.svm.core.util.UserError;
import com.oracle.svm.hosted.FeatureImpl.BeforeAnalysisAccessImpl;
import com.oracle.svm.hosted.FeatureImpl.DuringAnalysisAccessImpl;
import com.oracle.svm.hosted.ImageClassLoader;
import com.oracle.svm.util.ReflectionUtil;

/**
 * Registers required classes from CXF or its dependencies for reflection or
 * delayed initialization during native image generation.
 *
 * @author bnazare
 */
@AutomaticFeature
public class CXFFeature extends BaseFeature {

    /**
     * Copied from
     * {@link org.apache.cxf.bus.extension.ExtensionManagerImpl#BUS_EXTENSION_RESOURCE ExtensionManagerImpl.BUS_EXTENSION_RESOURCE}
     */
    private static final String BUS_EXTENSION_RESOURCE = "META-INF/cxf/bus-extensions.txt";

    /**
     * Map of extension interfaces and respective extension implementations that
     * are compatible with native mode. These implementations are meant to
     * replace the default ones that generate bytecode at runtime and are thus
     * incompatible with native mode. Most of the list was taken from
     * <a href="https://cwiki.apache.org/confluence/display/CXF20DOC/GraalVM+Support">this
     * page</a>.
     * <br/><br/>
     * <b>NOTE:</b> The implementations supplied here assume that the required
     * bytecode was generated at some point before runtime. In this context that
     * means that it should be generated during the native build. We are not
     * doing that however because it is not mandatory for using a SOAP client.
     * However, if we ever implement a SOAP service we might have to implement
     * such generation.
     */
    private static final Map<String, String> REPLACED_EXTENSIONS = Map.of(
            "org.apache.cxf.common.spi.ClassLoaderService", "org.apache.cxf.common.spi.ClassLoaderProxyService$LoadFirst",
            "org.apache.cxf.jaxb.WrapperHelperCreator", "org.apache.cxf.jaxb.WrapperHelperClassLoader",
            "org.apache.cxf.wsdl.ExtensionClassCreator", "org.apache.cxf.wsdl.ExtensionClassLoader",
            "org.apache.cxf.endpoint.dynamic.ExceptionClassCreator", "org.apache.cxf.endpoint.dynamic.ExceptionClassLoader",
            "org.apache.cxf.jaxws.spi.WrapperClassCreator", "org.apache.cxf.jaxws.spi.WrapperClassLoader",
            "org.apache.cxf.jaxb.FactoryClassCreator", "org.apache.cxf.jaxb.FactoryClassLoader",
            "org.apache.cxf.common.spi.NamespaceClassCreator", "org.apache.cxf.common.spi.GeneratedNamespaceClassLoader"
    );

    @Override
    public void duringSetup(DuringSetupAccess access) {
        Class<?>[] lazyInitializedClasses = {
            //            /**
            //             * Obtains an instance of Inet4Address during static initialization
            //             * which is not allowed.
            //             */
            //            access.findClassByName("net.sf.ehcache.Cache"),
            /**
             * References class org.jvnet.fastinfoset.VocabularyApplicationData
             * which is not available on the classpath and causes native image
             * compilation errors.
             */
            access.findClassByName("com.sun.xml.bind.v2.runtime.output.FastInfosetStreamWriterOutput"),
            /**
             * Creates an instance of Random during static initialization which
             * is forbidden at native build time.
             */
            access.findClassByName("org.apache.cxf.attachment.AttachmentUtil")
        };
        RuntimeClassInitialization.initializeAtRunTime(lazyInitializedClasses);
    }

    @Override
    public void beforeAnalysis(BeforeAnalysisAccess access) {
        access.registerReachabilityHandler(
                acs -> registerService(acs, "javax.xml.ws.spi.Provider", "org.apache.cxf.jaxws.spi.ProviderImpl"),
                access.findClassByName("javax.xml.ws.spi.Provider")
        );

        access.registerReachabilityHandler(
                acs -> registerService(acs, "javax.xml.soap.SAAJMetaFactory", "com.sun.xml.messaging.saaj.soap.SAAJMetaFactoryImpl"),
                access.findClassByName("javax.xml.soap.SAAJMetaFactory")
        );

        registerHardcodedClassesHandlers(access);

        Class<?> extensionManagerImplClass = access.findClassByName("org.apache.cxf.bus.extension.ExtensionManagerImpl");
        access.registerReachabilityHandler(this::registerCXFExtensions,
                // the cast is just here to solve an ambiguity caused by the varargs param
                (Object[]) extensionManagerImplClass.getConstructors()
        );

        Class<?> extensionRegistryClass = access.findClassByName("javax.wsdl.extensions.ExtensionRegistry");
        access.registerReachabilityHandler(this::registerWsdl4jExtensions,
                ReflectionUtil.lookupMethod(extensionRegistryClass, "createExtension", Class.class, QName.class)
        );

        Class<?> logUtilsClass = access.findClassByName("org.apache.cxf.common.logging.LogUtils");
        access.registerReachabilityHandler(
                acs -> {
                    Constructor<?> slf4jLoggerCtr = ReflectionUtil.lookupConstructor(
                            access.findClassByName("org.apache.cxf.common.logging.Slf4jLogger"),
                            String.class, String.class);
                    RuntimeReflection.register(slf4jLoggerCtr);

                    acs.requireAnalysisIteration();
                },
                ReflectionUtil.lookupMethod(logUtilsClass, "createLogger", Class.class, String.class, String.class)
        );

        registerWebServicesHandlers(access);

//        List<Class<? extends Node>> nodeSubtypes = ((BeforeAnalysisAccessImpl) access).findSubclasses(Node.class);
//        nodeSubtypes.stream()
//                .filter(subtype -> subtype.getPackageName().startsWith("com.sun.xml.messaging.saaj.soap."))
//                .forEach(subtype -> {
//                    /**
//                     * Some SAAJ classes are instantiated reflectively via this
//                     * constructor from within other SAAJ classes, e.g.
//                     * SOAPDocumentImpl.
//                     */
//                    registerConstructorSafely(subtype,
//                            access.findClassByName("com.sun.xml.messaging.saaj.soap.SOAPDocumentImpl"),
//                            access.findClassByName("org.w3c.dom.Element"));
//
//                    /**
//                     * The following methods and field are used reflectively by
//                     * org.apache.cxf.helpers.DOMUtils and
//                     * org.apache.wss4j.dom.util.WSSecurityUtil.
//                     *
//                     * Currently, the offending code has been substituted due to
//                     * an unrelated GraalVM issue however this might be
//                     * necessary again once that issue is fixed and the
//                     * substitution removed.
//                     */
////                    try {
////                        Method method = subtype.getMethod("getDomElement");
////                        RuntimeReflection.register(method);
////                    } catch (NoSuchMethodException | SecurityException ex) {
////                        // dont'care
////                    }
////
////                    try {
////                        Method method = subtype.getMethod("getEnvelope");
////                        RuntimeReflection.register(method);
////                    } catch (NoSuchMethodException | SecurityException ex) {
////                        // dont'care
////                    }
////
////                    try {
////                        Field field = subtype.getDeclaredField("documentFragment");
////                        RuntimeReflection.register(field);
////                    } catch (NoSuchFieldException | SecurityException ex) {
////                        // dont'care
////                    }
//                });
//
        access.registerReachabilityHandler(this::registerNeethiConverters,
                access.findClassByName("org.apache.neethi.builders.converters.ConverterRegistry")
        );

        /**
         * In theory we should also register all JAXB enabled classes. However
         * this is already taken care of by the JAXB extension of Quarkus as
         * long as the relevant JARs are registered in application.properties
         * via properties "quarkus.index-dependency.*".
         */
    }

    @Override
    public void duringAnalysis(DuringAnalysisAccess access) {
        log(access, "analysis iteration occurred");
    }

    private void registerHardcodedClassesHandlers(BeforeAnalysisAccess access) throws UserError.UserException, SecurityException {
        /**
         * The names of these classes are straight up hardcoded in some classes
         * which make use of them via reflection so we need to manually register
         * them for reflective look up and instantiation.
         */

        access.registerReachabilityHandler(
                acs -> registerHardcodedClass(acs, access.findClassByName("org.apache.cxf.bus.CXFBusFactory")),
                ReflectionUtil.lookupMethod(access.findClassByName("org.apache.cxf.BusFactory"), "newInstance", String.class)
        );

        access.registerReachabilityHandler(
                acs -> registerHardcodedClass(acs, access.findClassByName("com.ibm.wsdl.factory.WSDLFactoryImpl")),
                ReflectionUtil.lookupMethod(access.findClassByName("javax.wsdl.factory.WSDLFactory"), "newInstance")
        );

        access.registerReachabilityHandler(
                acs -> {
                    registerHardcodedClass(acs, access.findClassByName("org.apache.cxf.jaxb.JAXBDataBinding"));
                    /**
                     * JAXBDataBinding may be loaded via a non-default
                     * constructor so we need to register that too.
                     */
                    Constructor<?> constructor = ReflectionUtil.lookupConstructor(
                            access.findClassByName("org.apache.cxf.jaxb.JAXBDataBinding"),
                            Boolean.TYPE, Map.class);
                    RuntimeReflection.register(constructor);
                },
                ReflectionUtil.lookupMethod(access.findClassByName("org.apache.cxf.wsdl.service.factory.ReflectionServiceFactoryBean"), "getJAXBClass")
        );
    }

    private void registerHardcodedClass(DuringAnalysisAccess access, Class<?>... hardcodedClasses) {
        RuntimeReflection.register(hardcodedClasses);
        RuntimeReflection.registerForReflectiveInstantiation(hardcodedClasses);

        log(access, "hardcoded: " + Arrays.toString(hardcodedClasses));
        access.requireAnalysisIteration();
    }

    private void registerCXFExtensions(DuringAnalysisAccess access) throws SecurityException, UserError.UserException {
        ImageClassLoader imgCl = ((DuringAnalysisAccessImpl) access).getImageClassLoader();
        StringBuilder mergedFileContents = new StringBuilder();
        boolean registeredExtensions = false;

        List<URL> resources;
        try {
            /**
             * Find all /META-INF/cxf/bus-extensions.txt files in the classpath
             * which contain the names of all extension classes used by CXF. The
             * files themselves are merged into an unified extension file and
             * added to the native image classpath.
             */
            resources = Collections.list(imgCl.findResourcesByName(BUS_EXTENSION_RESOURCE));
        } catch (IOException ex) {
            throw UserError.abort(ex, "Error loading CXF's extension catalog files");
        }

        for (URL resourceURL : resources) {
            List<String[]> exts;
            try (InputStream is = resourceURL.openStream();
                    InputStreamReader isr = new InputStreamReader(is);
                    BufferedReader reader = new BufferedReader(isr)) {
                exts = reader.lines()
                        // discard empty lines
                        .filter(StringUtils::isNotBlank)
                        // discard commented lines
                        .filter(line -> !line.startsWith("#"))
                        // split the line, -1 param ensures trailing empty strings are kept
                        .map(line -> line.split(":", -1))
                        // ignore line if first part is empty
                        .filter(parts -> StringUtils.isNotBlank(parts[0]))
                        // replace extensions incompatible with native mode
                        .map(parts -> replaceExtension(parts, access))
                        // add line to merged contents
                        .peek(parts -> mergedFileContents.append(String.join(":", parts)).append('\n'))
                        .collect(Collectors.toList());
            } catch (IOException ex) {
                throw UserError.abort(ex, "Error loading extension catalog file from URL `" + resourceURL + "`");
            }

            for (String[] ext : exts) {
                String classname = ext[0];

                log(access, "cxfExtension: " + classname);
                /**
                 * Some CXF extensions also declare their interfaces which are
                 * then looked up via reflection and so we need to register then
                 * too.
                 */
                if (ext.length >= 2 && StringUtils.isNotBlank(ext[1])) {
                    Class<?> itf = access.findClassByName(ext[1]);
                    RuntimeReflection.register(itf);
                }

                Class<?> clazz = access.findClassByName(classname);

                /**
                 * Register the actual extension class for look up and
                 * instantiation via reflection. The CXF extensions may have any
                 * combination of four possible constructors so we attemp to
                 * register all of them.
                 */
                Class<?> busClass = access.findClassByName("org.apache.cxf.Bus");
                RuntimeReflection.register(clazz);
                registerConstructorSafely(clazz);
                registerConstructorSafely(clazz, busClass);
                registerConstructorSafely(clazz, busClass, Object[].class);
                registerConstructorSafely(clazz, Object[].class);
                registeredExtensions = true;

                /**
                 * Some extensions may have resources injected after their
                 * instantiation. For this to work, they need to have their
                 * fields and methods available for reflection. This may happen
                 * if the extensions class is not annotated with
                 * NoJSR250Annotations or if it is annotated with a
                 * NoJSR250Annotations annotation but with a non-empty
                 * "unlessNull" attribute.
                 */
                NoJSR250Annotations antn = clazz.getAnnotation(NoJSR250Annotations.class);
                if (antn == null || antn.unlessNull().length > 0) {
                    RuntimeReflection.register(clazz.getDeclaredFields());
                    RuntimeReflection.register(clazz.getDeclaredMethods());

                    log(access, "enableResourceInjection: " + classname);
                }
            }
        }

        Resources.registerResource(BUS_EXTENSION_RESOURCE,
                new ByteArrayInputStream(mergedFileContents.toString().getBytes(UTF_8)));

        if (registeredExtensions) {
            access.requireAnalysisIteration();
        }
    }

    private String[] replaceExtension(String[] parts, FeatureAccess access) {
        if (parts.length >= 2 && REPLACED_EXTENSIONS.containsKey(parts[1])) {
            parts[0] = REPLACED_EXTENSIONS.get(parts[1]);
            log(access, "replaced cxfExtension: {" + parts[1] + "} to {" + parts[0] + "}");
        }
        return parts;
    }

    private void registerWsdl4jExtensions(DuringAnalysisAccess access) {
        /**
         * CXF uses wsdl4j which itself instantiates a lot of its classes via
         * reflection. There are too many of these classes to track manually and
         * no configuration file. Instead, the configuration is done
         * programatically in an ExtensionRegistry. Luckily, all of these
         * classes extend from ExtensibilityElement which means we can use that
         * to find and register all of them. However, since using this method we
         * might get more classes than the ones we are actually looking for, we
         * have to take care to only include classes from the wsdl4j project.
         */
        Class extensibilityElementItf = access.findClassByName("javax.wsdl.extensions.ExtensibilityElement");
        List<Class> extensionElementClasses = ((DuringAnalysisAccessImpl) access).findSubclasses(extensibilityElementItf);
        // the poor man's mutable reference
        boolean[] extensionsRegistered = {false};

        extensionElementClasses.stream()
                // filter out interfaces and abstract classes
                .filter(BaseFeature::isConcreteClass)
                // include only classes from the wsdl4j project
                .filter(clazz -> clazz.getPackage().getName().startsWith("com.ibm.wsdl.extensions."))
                .forEach(clazz -> {
                    RuntimeReflection.registerForReflectiveInstantiation(clazz);
                    log(access, "wsdl4jExtension: " + clazz.getName());
                    extensionsRegistered[0] = true;
                });

        if (extensionsRegistered[0]) {
            access.requireAnalysisIteration();
        }
    }

    private void registerWebServicesHandlers(BeforeAnalysisAccess access) {
        /**
         * Find all interfaces annotated with WebService and, if they are
         * accessible, register them and their methods for reflection so JAX-WS
         * can find them.
         */
        List<Class<?>> wsClasses = ((BeforeAnalysisAccessImpl) access).findAnnotatedClasses(WebService.class);
        wsClasses.forEach(wsClass -> {
            access.registerReachabilityHandler(
                    acs -> {
                        RuntimeReflection.register(wsClass);
                        RuntimeReflection.register(wsClass.getMethods());
                        log(acs, "webService: " + wsClass);

                        acs.requireAnalysisIteration();
                    }, wsClass);
        });
    }

    /**
     * Register the converters used by Apache Neethi and some of their methods
     * for reflective invocation. The class hierarchy for these classes is a
     * mess which results in at least two overloaded versions of each relevant
     * method appearing at runtime, one receives {@link Object} and the other
     * receives a parameterized type. For our purposes, we need the latter. To
     * find what that parameterized type is we have to crawl the list of
     * interfaces implemented by each converter class and collect the first type
     * parameter passed to
     * {@link org.apache.neethi.builders.converters.Converter Converter}.
     *
     * @param access the "access" object
     * @throws SecurityException on reflection issues
     */
    private void registerNeethiConverters(DuringAnalysisAccess access) throws SecurityException {
        Class converterItf = access.findClassByName("org.apache.neethi.builders.converters.Converter");
        List<Class> converterClassNames = ((BeforeAnalysisAccessImpl) access).findSubclasses(converterItf);
        boolean[] extensionsRegistered = {false};

        converterClassNames.stream()
                // the interface is in the list of subclasses too, go figure ...
                .filter(BaseFeature::isConcreteClass)
                .forEach(converterClass -> {
                    Class<?> srcClass;
                    try {
                        ParameterizedType converterParameterizedType = getParameterizedType(converterClass, converterItf);
                        if (converterParameterizedType == null) {
                            // should never happen but protects against programming errors
                            throw UserError.abort("Converter interface not found!!");
                        }

                        srcClass = (Class<?>) converterParameterizedType.getActualTypeArguments()[0];
                    } catch (TypeNotPresentException err) {
                        // expected for any Axiom related converter, just skip it
                        return;
                    }

                    RuntimeReflection.register(converterClass);
                    extensionsRegistered[0] = true;

                    try {
                        RuntimeReflection.register(converterClass.getMethod("getQName", srcClass));
                        RuntimeReflection.register(converterClass.getMethod("getAttributes", srcClass));
                        RuntimeReflection.register(converterClass.getMethod("getChildren", srcClass));
                    } catch (NoSuchMethodException ex) {
                        // should never happen but protects against programming errors
                        throw UserError.abort(ex, "Converter method not found");
                    }
                });

        if (extensionsRegistered[0]) {
            access.requireAnalysisIteration();
        }
    }

    private ParameterizedType getParameterizedType(Class clazz, Class itf) {
        for (Type genericInterface : clazz.getGenericInterfaces()) {
            if (genericInterface instanceof ParameterizedType
                    && ((ParameterizedType) genericInterface).getRawType() == itf) {
                return (ParameterizedType) genericInterface;
            }
        }
        return null;
    }

}
