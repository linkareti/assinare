package com.linkare.assinare.server.nativeimage;

// Checkstyle: allow reflection
import java.io.Externalizable;
import java.io.ObjectStreamClass;
import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.graalvm.nativeimage.ImageSingletons;
import org.graalvm.nativeimage.hosted.Feature;
import org.graalvm.nativeimage.hosted.RuntimeReflection;

import com.oracle.svm.core.annotate.AutomaticFeature;
import com.oracle.svm.core.configure.ConfigurationFiles;
import com.oracle.svm.core.configure.SerializationConfigurationParser;
import com.oracle.svm.core.configure.SerializationConfigurationParser.SerializationParserFunction;
import com.oracle.svm.core.jdk.Package_jdk_internal_reflect;
import com.oracle.svm.core.jdk.RecordSupport;
import com.oracle.svm.core.jdk.serialize.SerializationRegistry;
import com.oracle.svm.core.util.UserError;
import com.oracle.svm.core.util.VMError;
import com.oracle.svm.core.util.json.JSONParserException;
import com.oracle.svm.hosted.FallbackFeature;
import com.oracle.svm.hosted.FeatureImpl;
import com.oracle.svm.hosted.ImageClassLoader;
import com.oracle.svm.hosted.NativeImageOptions;
import com.oracle.svm.hosted.config.ConfigurationParserUtils;
import com.oracle.svm.reflect.hosted.ReflectionFeature;
import com.oracle.svm.reflect.serialize.SerializationSupport;
import com.oracle.svm.util.ReflectionUtil;

/**
 * Temporary workaround for the issue fixed
 * <a href="https://github.com/oracle/graal/pull/3502">here</a>. This code is
 * EVIL!<br>
 * DO. NOT. TOUCH.
 *
 * @author bnazare
 */
@AutomaticFeature
public class SerializationFeature implements Feature {

    private int loadedConfigurations;

    @Override
    public List<Class<? extends Feature>> getRequiredFeatures() {
        return Collections.singletonList(ReflectionFeature.class);
    }

    @Override
    public void duringSetup(Feature.DuringSetupAccess a) {
        FeatureImpl.DuringSetupAccessImpl access = (FeatureImpl.DuringSetupAccessImpl) a;
        SerializationBuilder serializationBuilder = new SerializationBuilder(access);

        Map<Class<?>, Boolean> deniedClasses = new HashMap<>();
        SerializationConfigurationParser denyCollectorParser = new SerializationConfigurationParser((strTargetSerializationClass, strCustomTargetConstructorClass) -> {
            Class<?> serializationTargetClass = resolveClass(strTargetSerializationClass, access);
            if (serializationTargetClass != null) {
                deniedClasses.put(serializationTargetClass, true);
            }
        });
        ImageClassLoader imageClassLoader = access.getImageClassLoader();
        ConfigurationParserUtils.parseAndRegisterConfigurations(denyCollectorParser, imageClassLoader, "serialization",
                ConfigurationFiles.Options.SerializationDenyConfigurationFiles, ConfigurationFiles.Options.SerializationDenyConfigurationResources,
                ConfigurationFiles.SERIALIZATION_DENY_NAME);

        SerializationParserFunction serializationAdapter = (strTargetSerializationClass, strCustomTargetConstructorClass) -> {
            Class<?> serializationTargetClass = resolveClass(strTargetSerializationClass, access);
            UserError.guarantee(serializationTargetClass != null, "Cannot find serialization target class %s. The missing of this class can't be ignored even if -H:+AllowIncompleteClasspath is set."
                    + " Please make sure it is in the classpath", strTargetSerializationClass);
            if (Serializable.class.isAssignableFrom(serializationTargetClass)) {
                if (deniedClasses.containsKey(serializationTargetClass)) {
                    if (deniedClasses.get(serializationTargetClass)) {
                        deniedClasses.put(serializationTargetClass, false);
                        /* Warn only once */
                        println("Warning: Serialization deny list contains " + serializationTargetClass.getName() + ". Image will not support serialization/deserialization of this class.");
                    }
                } else {
                    Class<?> customTargetConstructorClass = null;
                    if (strCustomTargetConstructorClass != null) {
                        customTargetConstructorClass = resolveClass(strCustomTargetConstructorClass, access);
                        UserError.guarantee(customTargetConstructorClass != null, "Cannot find " + SerializationConfigurationParser.CUSTOM_TARGET_CONSTRUCTOR_CLASS_KEY + " %s that was specified in"
                                + " the serialization configuration. The missing of this class can't be ignored even if -H:+AllowIncompleteClasspath is set. Please make sure it is in the classpath",
                                strCustomTargetConstructorClass);
                        UserError.guarantee(customTargetConstructorClass.isAssignableFrom(serializationTargetClass),
                                "The given " + SerializationConfigurationParser.CUSTOM_TARGET_CONSTRUCTOR_CLASS_KEY
                                + " %s that was specified in the serialization configuration is not a subclass of the serialization target class %s.",
                                strCustomTargetConstructorClass, strTargetSerializationClass);
                    }
                    Class<?> targetConstructor = serializationBuilder.addConstructorAccessor(serializationTargetClass, customTargetConstructorClass);
                    addReflections(serializationTargetClass, targetConstructor);
                }
            }
        };

        SerializationConfigurationParser parser = new SerializationConfigurationParser(serializationAdapter);
        loadedConfigurations = ConfigurationParserUtils.parseAndRegisterConfigurations(parser, imageClassLoader, "serialization",
                ConfigurationFiles.Options.SerializationConfigurationFiles, ConfigurationFiles.Options.SerializationConfigurationResources,
                ConfigurationFiles.SERIALIZATION_NAME);
    }

    public static void addReflections(Class<?> serializationTargetClass, Class<?> targetConstructorClass) {
        if (targetConstructorClass != null) {
            RuntimeReflection.register(ReflectionUtil.lookupConstructor(targetConstructorClass));
        }

        if (Externalizable.class.isAssignableFrom(serializationTargetClass)) {
            RuntimeReflection.register(ReflectionUtil.lookupConstructor(serializationTargetClass, (Class<?>[]) null));
        }

        RecordSupport recordSupport = RecordSupport.singleton();
        if (recordSupport.isRecord(serializationTargetClass)) {
            /* Serialization for records uses the canonical record constructor directly. */
            RuntimeReflection.register(recordSupport.getCanonicalRecordConstructor(serializationTargetClass));
            /*
             * Serialization for records invokes Class.getRecordComponents(). Registering all record
             * component accessor methods for reflection ensures that the record components are
             * available at run time.
             */
            RuntimeReflection.register(recordSupport.getRecordComponentAccessorMethods(serializationTargetClass));
        }

        RuntimeReflection.register(serializationTargetClass);
        /*
         * ObjectStreamClass.computeDefaultSUID is always called at runtime to verify serialization
         * class consistency, so need to register all constructors, methods and fields/
         */
        RuntimeReflection.register(serializationTargetClass.getDeclaredConstructors());
        registerMethods(serializationTargetClass);
        registerFields(serializationTargetClass);
    }

    private static void registerMethods(Class<?> serializationTargetClass) {
        RuntimeReflection.register(serializationTargetClass.getDeclaredMethods());
        // computeDefaultSUID will be reflectively called at runtime to verify class consistency
        Method computeDefaultSUID = ReflectionUtil.lookupMethod(ObjectStreamClass.class, "computeDefaultSUID", Class.class);
        RuntimeReflection.register(computeDefaultSUID);
    }

    private static void registerFields(Class<?> serializationTargetClass) {
        for (Field f : serializationTargetClass.getDeclaredFields()) {
            int modifiers = f.getModifiers();
            boolean allowWrite = false;
            boolean allowUnsafeAccess = false;
            int staticFinalMask = Modifier.STATIC | Modifier.FINAL;
            if ((modifiers & staticFinalMask) != staticFinalMask) {
                allowUnsafeAccess = !Modifier.isStatic(f.getModifiers());
            }
            RuntimeReflection.register(allowWrite, allowUnsafeAccess, f);
        }
    }

    private static Class<?> resolveClass(String typeName, Feature.FeatureAccess a) {
        String name = typeName;
        if (name.indexOf('[') != -1) {
            /* accept "int[][]", "java.lang.String[]" */
            throw new UnsupportedOperationException();
//            name = MetaUtil.internalNameToJava(MetaUtil.toInternalName(name), true, true);
        }
        Class<?> ret = a.findClassByName(name);
        if (ret == null) {
            handleError("Could not resolve " + name + " for serialization configuration.");
        }
        return ret;
    }

    @Override
    public void beforeCompilation(Feature.BeforeCompilationAccess access) {
        if (!ImageSingletons.contains(FallbackFeature.class)) {
            return;
        }
        FallbackFeature.FallbackImageRequest serializationFallback = ImageSingletons.lookup(FallbackFeature.class).serializationFallback;
        if (serializationFallback != null && loadedConfigurations == 0) {
            throw serializationFallback;
        }
    }

    private static void handleError(String message) {
        boolean allowIncompleteClasspath = NativeImageOptions.AllowIncompleteClasspath.getValue();
        if (allowIncompleteClasspath) {
            println("WARNING: " + message);
        } else {
            throw new JSONParserException(message + " To allow unresolvable reflection configuration, use option -H:+AllowIncompleteClasspath");
        }
    }

    static void println(String str) {
        // Checkstyle: stop
        System.out.println(str);
        // Checkstyle: resume
    }
}

final class SerializationBuilder {

    private final Object reflectionFactory;
    private final Method newConstructorForSerializationMethod1;
    private final Method newConstructorForSerializationMethod2;
    private final Method getConstructorAccessorMethod;
    private final Method getExternalizableConstructorMethod;
    private final Constructor<?> stubConstructor;

    private final SerializationSupport serializationSupport;

    SerializationBuilder(FeatureImpl.DuringSetupAccessImpl access) {
        try {
            Class<?> reflectionFactoryClass = access.findClassByName(Package_jdk_internal_reflect.getQualifiedName() + ".ReflectionFactory");
            Method getReflectionFactoryMethod = ReflectionUtil.lookupMethod(reflectionFactoryClass, "getReflectionFactory");
            reflectionFactory = getReflectionFactoryMethod.invoke(null);
            newConstructorForSerializationMethod1 = ReflectionUtil.lookupMethod(reflectionFactoryClass, "newConstructorForSerialization", Class.class);
            newConstructorForSerializationMethod2 = ReflectionUtil.lookupMethod(reflectionFactoryClass, "newConstructorForSerialization", Class.class, Constructor.class);
            getConstructorAccessorMethod = ReflectionUtil.lookupMethod(Constructor.class, "getConstructorAccessor");
            getExternalizableConstructorMethod = ReflectionUtil.lookupMethod(ObjectStreamClass.class, "getExternalizableConstructor", Class.class);
        } catch (ReflectiveOperationException e) {
            throw VMError.shouldNotReachHere(e);
        }
        stubConstructor = newConstructorForSerialization(SerializationSupport.StubForAbstractClass.class, null);

//        serializationSupport = new SerializationSupport();
//        ImageSingletons.add(SerializationRegistry.class, serializationSupport);
        serializationSupport = (SerializationSupport) ImageSingletons.lookup(SerializationRegistry.class);
    }

    private Constructor<?> newConstructorForSerialization(Class<?> serializationTargetClass, Constructor<?> customConstructorToCall) {
        try {
            if (customConstructorToCall == null) {
                return (Constructor<?>) newConstructorForSerializationMethod1.invoke(reflectionFactory, serializationTargetClass);
            } else {
                return (Constructor<?>) newConstructorForSerializationMethod2.invoke(reflectionFactory, serializationTargetClass, customConstructorToCall);
            }
        } catch (ReflectiveOperationException e) {
            throw VMError.shouldNotReachHere(e);
        }
    }

    private Object getConstructorAccessor(Constructor<?> constructor) {
        try {
            return getConstructorAccessorMethod.invoke(constructor);
        } catch (ReflectiveOperationException e) {
            throw VMError.shouldNotReachHere(e);
        }
    }

    private Constructor<?> getExternalizableConstructor(Class<?> serializationTargetClass) {
        try {
            return (Constructor<?>) getExternalizableConstructorMethod.invoke(null, serializationTargetClass);
        } catch (ReflectiveOperationException e) {
            throw VMError.shouldNotReachHere(e);
        }
    }

    Class<?> addConstructorAccessor(Class<?> serializationTargetClass, Class<?> customTargetConstructorClass) {
        if (serializationTargetClass.isArray() || Enum.class.isAssignableFrom(serializationTargetClass)) {
            return null;
        }

        // Don't generate SerializationConstructorAccessor class for Externalizable case
        if (Externalizable.class.isAssignableFrom(serializationTargetClass)) {
            try {
                Constructor<?> externalizableConstructor = getExternalizableConstructor(serializationTargetClass);
                return externalizableConstructor.getDeclaringClass();
            } catch (Exception e) {
                throw VMError.shouldNotReachHere(e);
            }
        }

        /*
         * Using reflection to make sure code is compatible with both JDK 8 and above. Reflectively
         * call method ReflectionFactory.newConstructorForSerialization(Class) to get the
         * SerializationConstructorAccessor instance.
         */
        Constructor<?> targetConstructor;
        Class<?> targetConstructorClass;
//        if (Modifier.isAbstract(serializationTargetClass.getModifiers())) {
//            targetConstructor = stubConstructor;
//            targetConstructorClass = targetConstructor.getDeclaringClass();
//        } else {
        Constructor<?> customConstructorToCall = null;
        if (customTargetConstructorClass != null) {
            try {
                customConstructorToCall = customTargetConstructorClass.getDeclaredConstructor();
            } catch (NoSuchMethodException ex) {
                UserError.abort("The given targetConstructorClass %s does not declare a parameterless constructor.",
                        customTargetConstructorClass.getTypeName());
            }
        }
        targetConstructor = newConstructorForSerialization(serializationTargetClass, customConstructorToCall);
        targetConstructorClass = targetConstructor.getDeclaringClass();
//        }
//        Object constructorAccessor = getConstructorAccessor(targetConstructor);
        Object constructorAccessor;
        if (Modifier.isAbstract(serializationTargetClass.getModifiers())) {
            constructorAccessor = getConstructorAccessor(stubConstructor);
        } else {
            constructorAccessor = getConstructorAccessor(targetConstructor);
        }
        serializationSupport.addConstructorAccessor(serializationTargetClass, targetConstructorClass, constructorAccessor);
        return targetConstructorClass;
    }
}
