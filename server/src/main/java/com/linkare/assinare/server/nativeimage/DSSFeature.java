package com.linkare.assinare.server.nativeimage;

import org.graalvm.nativeimage.hosted.RuntimeClassInitialization;
import org.graalvm.nativeimage.hosted.RuntimeReflection;

import com.oracle.svm.core.annotate.AutomaticFeature;
import com.oracle.svm.util.ReflectionUtil;

/**
 *
 * @author bnazare
 */
@AutomaticFeature
public class DSSFeature extends BaseFeature {

    @Override
    public void duringSetup(DuringSetupAccess access) {
        Class[] lazyInitializedClasses = {
            /**
             * PDType1Font initializes a few instances of itself (for default
             * fonts) during static initialization. Some (or all) of those
             * instances keep a reference to a TrueTypeFont which keeps a
             * reference to a RAFDataStream which keeps a reference to a
             * RandomAccessFile. RandomAccessFile is not supported in build time
             * initialization and so we have to delay it.
             */
            access.findClassByName("org.apache.pdfbox.pdmodel.font.PDType1Font"),
            /**
             * Creates an instance of SecureRandom during static initialization
             * which is forbidden at native build time.
             */
            access.findClassByName("org.apache.http.impl.auth.NTLMEngineImpl")
        };
        RuntimeClassInitialization.initializeAtRunTime(lazyInitializedClasses);
    }

    @Override
    public void beforeAnalysis(BeforeAnalysisAccess access) {
        access.registerReachabilityHandler(this::registerIUtilsImplementation,
                access.findClassByName("eu.europa.esig.dss.utils.IUtils")
        );

        /*
         * all accesses to these fonts are now being made from static code
         * so there's no need to register them for dynamic loading anymore
         */
//        access.registerReachabilityHandler(this::registerDefaultDssFont,
//                ReflectionUtil.lookupField(access.findClassByName("eu.europa.esig.dss.pades.DSSFileFont"), "DEFAULT_FONT")
//        );
//
//        access.registerReachabilityHandler(this::registerLiberationSansFont,
//                ReflectionUtil.lookupConstructor(access.findClassByName("org.apache.pdfbox.pdmodel.font.FontMapperImpl")),
//                ReflectionUtil.lookupField(access.findClassByName("com.linkare.assinare.sign.pdf.dss.DssPdfSigner"), "DEFAULT_SIG_FONT_DATA")
//        );
//
        access.registerReachabilityHandler(this::registerLogFactoryImpl,
                ReflectionUtil.lookupMethod(access.findClassByName("org.apache.commons.logging.LogFactory"), "getFactory")
        );

        access.registerReachabilityHandler(this::registerLogImpl,
                ReflectionUtil.lookupMethod(access.findClassByName("org.apache.commons.logging.impl.LogFactoryImpl"), "discoverLogImplementation", String.class)
        );
    }

    private void registerIUtilsImplementation(DuringAnalysisAccess access) {
        registerService(access, "eu.europa.esig.dss.utils.IUtils", "eu.europa.esig.dss.utils.apache.impl.ApacheCommonsUtils");
    }

//    private void registerDefaultDssFont(DuringAnalysisAccess access) {
//        registerResources(access,
//                "fonts/PTSerifRegular.ttf"
//        );
//    }
//
//    private void registerLiberationSansFont(DuringAnalysisAccess access) {
//        registerResources(access,
//                "org/apache/pdfbox/resources/ttf/LiberationSans-Regular.ttf"
//        );
//    }
//
    private void registerLogFactoryImpl(DuringAnalysisAccess access) {
        Class<?> logFactoryImpl = access.findClassByName("org.apache.commons.logging.impl.LogFactoryImpl");
        RuntimeReflection.register(logFactoryImpl);
        RuntimeReflection.registerForReflectiveInstantiation(logFactoryImpl);

        access.requireAnalysisIteration();
    }

    private void registerLogImpl(DuringAnalysisAccess access) {
        Class<?> logFactoryImpl = access.findClassByName("org.apache.commons.logging.impl.Jdk14Logger");
        RuntimeReflection.register(logFactoryImpl);
        RuntimeReflection.register(ReflectionUtil.lookupConstructor(logFactoryImpl, String.class));

        access.requireAnalysisIteration();
    }

}
