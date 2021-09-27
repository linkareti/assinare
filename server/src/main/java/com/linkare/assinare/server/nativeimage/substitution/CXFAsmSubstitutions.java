//package com.linkare.assinare.server.nativeimage.substitution;
//
//import java.lang.reflect.Constructor;
//import java.lang.reflect.Method;
////import java.net.URL;
//import java.util.Map;
//
//import org.apache.cxf.common.jaxb.JAXBUtils;
//import org.apache.cxf.common.util.ASMHelper;
////import org.apache.cxf.common.util.ASMHelper.TypeHelperClassLoader;
//import org.apache.cxf.databinding.WrapperHelper;
//import org.apache.cxf.jaxws.WrapperClassGenerator;
//import org.apache.cxf.service.model.MessageInfo;
//import org.apache.cxf.service.model.MessagePartInfo;
//import org.apache.cxf.service.model.OperationInfo;
//
//import com.oracle.svm.core.annotate.Delete;
//import com.oracle.svm.core.annotate.Substitute;
//import com.oracle.svm.core.annotate.TargetClass;
//
///**
// * This file provides subsitutions for classes that directly or indirectly
// * attempt to inject bytecode into the JVM.
// *
// * @author bnazare
// */
//interface CXFAsmSubstitutions {
//
//    static final String NOT_SUPPORTED_IN_NATIVE_BUILD = "Not supported in native build";
//
//}
//
//@TargetClass(ASMHelper.class)
//final class Target_org_apache_cxf_common_util_ASMHelper {
//
//    /**
//     * Removes {@link ASMHelper#loadClass(String, Class, byte[]) }. Removed
//     * because its only purpose is to call
//     * {@link TypeHelperClassLoader#defineClass} which was itself removed.
//     *
//     * @param className
//     * @param clz
//     * @param bytes
//     * @return
//     */
//    @Delete
//    public native Class<?> loadClass(String className, Class<?> clz, byte[] bytes);
//
//    /**
//     * Removes {@link ASMHelper#loadClass(String, ClassLoader, byte[]) }.
//     * Removed because its only purpose is to call
//     * {@link TypeHelperClassLoader#defineClass} which was itself removed.
//     *
//     * @param className
//     * @param l
//     * @param bytes
//     * @return
//     */
//    @Delete
//    public native Class<?> loadClass(String className, ClassLoader l, byte[] bytes);
//
////    @TargetClass(TypeHelperClassLoader.class)
////    public final static class Target_TypeHelperClassLoader {
////
////        /**
////         * Removes {@link TypeHelperClassLoader#defineClass}. The original
////         * version of this method calls {@link ClassLoader#getPackage(String)},
////         * {@link ClassLoader#definePackage(String, String, String, String, String, String, String, URL)}
////         * and {@link ClassLoader#defineClass(String, byte[], int, int)}, all of
////         * which are unsupported by Graal native images.
////         *
////         * @param name
////         * @param bytes
////         * @return
////         */
////        @Delete
////        public native Class<?> defineClass(String name, byte[] bytes);
////
////    }
//
//}
//
//@TargetClass(WrapperClassGenerator.class)
//final class Target_org_apache_cxf_jaxws_WrapperClassGenerator {
//
//    /**
//     * Substitutes {@link WrapperClassGenerator#createWrapperClass} with a
//     * version that does not attempt to dynamically load bytecode using
//     * {@link ASMHelper}.
//     *
//     * @param wrapperPart
//     * @param messageInfo
//     * @param op
//     * @param method
//     * @param isRequest
//     */
//    @Substitute
//    private void createWrapperClass(MessagePartInfo wrapperPart,
//            MessageInfo messageInfo,
//            OperationInfo op,
//            Method method,
//            boolean isRequest) {
//        throw new UnsupportedOperationException(CXFAsmSubstitutions.NOT_SUPPORTED_IN_NATIVE_BUILD);
//    }
//
//}
//
//@TargetClass(className = "org.apache.cxf.jaxb.WrapperHelperCompiler")
//final class Target_org_apache_cxf_jaxb_WrapperHelperCompiler {
//
//    /**
//     * Substitutes
//     * {@link org.apache.cxf.jaxb.WrapperHelperCompiler#compile WrapperHelperCompiler.compile}
//     * with a version that does not attempt to dynamically load bytecode using
//     * {@link ASMHelper}.
//     *
//     * @return
//     */
//    @Substitute
//    public WrapperHelper compile() {
//        throw new UnsupportedOperationException(CXFAsmSubstitutions.NOT_SUPPORTED_IN_NATIVE_BUILD);
//    }
//
//}
//
//@TargetClass(className = "org.apache.cxf.jaxb.JAXBContextInitializer")
//final class Target_org_apache_cxf_jaxb_JAXBContextInitializer {
//
//    /**
//     * Substitutes
//     * {@link org.apache.cxf.jaxb.JAXBContextInitializer#createFactory JAXBContextInitializer.createFactory}
//     * with a version that does not attempt to dynamically load bytecode using
//     * {@link ASMHelper}.
//     *
//     * @param cls
//     * @param contructor
//     * @return
//     */
//    @Substitute
//    private Object createFactory(Class<?> cls, Constructor<?> contructor) {
//        throw new UnsupportedOperationException(CXFAsmSubstitutions.NOT_SUPPORTED_IN_NATIVE_BUILD);
//    }
//
//}
//
//@TargetClass(JAXBUtils.class)
//final class Target_org_apache_cxf_common_jaxb_JAXBUtils {
//
//    /**
//     * Substitutes {@link JAXBUtils#createNamespaceWrapper} with a version that
//     * does not attempt to dynamically load bytecode using {@link ASMHelper}.
//     *
//     * @param mcls
//     * @param map
//     * @return
//     */
//    @Substitute
//    private static synchronized Object createNamespaceWrapper(Class<?> mcls, Map<String, String> map) {
//        throw new UnsupportedOperationException(CXFAsmSubstitutions.NOT_SUPPORTED_IN_NATIVE_BUILD);
//    }
//
//}
