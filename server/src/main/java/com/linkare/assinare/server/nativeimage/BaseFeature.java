package com.linkare.assinare.server.nativeimage;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.util.ServiceLoader;

import org.graalvm.compiler.debug.DebugContext;
import org.graalvm.compiler.debug.DebugContext.Scope;
import org.graalvm.nativeimage.ImageSingletons;
import org.graalvm.nativeimage.hosted.Feature;
import org.graalvm.nativeimage.hosted.RuntimeReflection;
import org.graalvm.nativeimage.impl.RuntimeClassInitializationSupport;

import com.oracle.svm.core.jdk.Resources;
import com.oracle.svm.core.util.UserError;
import com.oracle.svm.core.util.UserError.UserException;
import com.oracle.svm.hosted.FeatureImpl.FeatureAccessImpl;
import com.oracle.svm.hosted.ImageClassLoader;

/**
 *
 * @author bnazare
 */
public abstract class BaseFeature implements Feature {

    protected static final String SERVICE_LOADER_PREFIX = "META-INF/services/";

    protected String logFormat = getClass().getSimpleName() + ": %s";

    /**
     * Attempts to register a class's constructor for reflection, does not fail
     * when the constructor does not exist.
     *
     * @param clazz the {@link Class} to get the constructor from
     * @param constructorParams the parameters of the constructor
     */
    protected void registerConstructorSafely(Class<?> clazz, Class<?>... constructorParams) {
        try {
            Constructor constructor = clazz.getConstructor(constructorParams);
            RuntimeReflection.register(constructor);
        } catch (NoSuchMethodException ex) {
            // don't care
        }
    }

    /**
     * Finds and registers the resources with the given names. Does not
     * currently support multiple resources with the same name.
     *
     * @param access
     * @param resourceNames
     * @throws UserException
     */
    protected void registerResources(FeatureAccess access, String... resourceNames) throws UserException {
        ImageClassLoader imgCl = ((FeatureAccessImpl) access).getImageClassLoader();

        for (String resourceName : resourceNames) {
            try (InputStream resourceStream = imgCl.findResourceAsStreamByName(resourceName)) {
                Resources.registerResource(resourceName, resourceStream);

                log(access, "resource: " + resourceName);
            } catch (IOException ex) {
                throw UserError.abort(ex, "Error adding resource");
            }
        }

    }

    /**
     * Registers the provided classes for reinitialization at runtime.
     *
     * @param reason
     * @param classes
     */
    protected void rerunInitialization(String reason, Class<?>... classes) {
        RuntimeClassInitializationSupport runtimeClassInitialization = ImageSingletons.lookup(RuntimeClassInitializationSupport.class);
        for (Class<?> aClass : classes) {
            runtimeClassInitialization.rerunInitialization(aClass, reason);
        }
    }

    /**
     * Log a message in the build context.
     *
     * @param access
     * @param message
     */
    protected void log(FeatureAccess access, String message) {
        DebugContext debugContext = ((FeatureAccessImpl) access).getDebugContext();

        try (Scope scope = debugContext.scope("assinareRegistration")) {
            debugContext.log(logFormat, message);
        }
    }

    /**
     * Checks if the given class is a concrete class. In actuality, it checks
     * that the class is not an interface and also not an abstract class.
     *
     * @param clazz the class to check
     * @return true, if the given class is concrete
     */
    protected static boolean isConcreteClass(Class<?> clazz) {
        return !clazz.isInterface() && !Modifier.isAbstract(clazz.getModifiers());
    }

    /**
     * Register the given implementation class for reflection and add the
     * respective {@link ServiceLoader} configutation file to the native image
     * classpath.
     *
     * @param access
     * @param serviceClassName
     * @param implementationClassName
     */
    protected void registerService(DuringAnalysisAccess access, String serviceClassName, String implementationClassName) {
        Class<?> implementationClass = access.findClassByName(implementationClassName);
        RuntimeReflection.register(implementationClass);
        RuntimeReflection.registerForReflectiveInstantiation(implementationClass);

        Resources.registerResource(SERVICE_LOADER_PREFIX + serviceClassName,
                new ByteArrayInputStream((implementationClassName + "\n").getBytes(StandardCharsets.UTF_8))
        );

        access.requireAnalysisIteration();
        log(access, "serviceProvider: " + serviceClassName + ":" + implementationClassName);
    }

}
