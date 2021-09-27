//package com.linkare.assinare.server.nativeimage.substitution;
//
//import org.apache.wss4j.common.ext.WSSecurityException;
//import org.apache.wss4j.common.saml.SAMLCallback;
//import org.apache.wss4j.common.saml.SamlAssertionWrapper;
//import org.apache.wss4j.common.saml.builder.SAML1ComponentBuilder;
//import org.apache.wss4j.common.saml.builder.SAML2ComponentBuilder;
//
//import com.oracle.svm.core.annotate.Delete;
//import com.oracle.svm.core.annotate.Substitute;
//import com.oracle.svm.core.annotate.TargetClass;
//
///**
// * Substitutions in this file hide code that causes two very bizarre errors
// * during native image compilation. Currently, it is still unknown what the
// * impact of these changes at runtime is. The original errors appear as follow:
// * <ul>
// * <li>com.oracle.graal.pointsto.util.AnalysisError$FieldNotPresentError: Field
// * java.net.URLClassLoader.acc is not present on type java.lang.ClassLoader.
// * Error encountered while analysing
// * java.net.URLClassLoader.access$200(java.net.URLClassLoader)</li>
// * <li>com.oracle.graal.pointsto.util.AnalysisError$FieldNotPresentError: Field
// * java.net.URLClassLoader.ucp is not present on type java.lang.ClassLoader.
// * Error encountered while analysing
// * java.net.URLClassLoader.access$000(java.net.URLClassLoader)</li>
// * </ul>
// *
// * @author bnazare
// */
//interface CXFCompilationFailSubstitutions {
//
//    static final String NOT_SUPPORTED_IN_NATIVE_BUILD = "Not supported in native build";
//
//}
//
//@TargetClass(SamlAssertionWrapper.class)
//final class Target_org_apache_wss4j_common_saml_SamlAssertionWrapper {
//
//    /**
//     * Substitutes {@link SamlAssertionWrapper#parseCallback}. The original
//     * method makes several calls to static methods in
//     * {@link SAML1ComponentBuilder} and {@link SAML2ComponentBuilder} which
//     * somehow cause the compilation errors. Substituting those classes did not
//     * seem to make a difference so we have to remove all their usages which
//     * occur only in this method.
//     * <br><br>
//     * Since we don't use SAML, this method is currently never called at runtime
//     * and so it's safe to substitute the original method with just throwing an
//     * exception.
//     *
//     * @param samlCallback
//     * @throws WSSecurityException
//     */
//    @Substitute
//    private void parseCallback(SAMLCallback samlCallback) throws WSSecurityException {
//        throw new UnsupportedOperationException(CXFCompilationFailSubstitutions.NOT_SUPPORTED_IN_NATIVE_BUILD);
//    }
//
//}
//
///**
// * Removes {@link SAML1ComponentBuilder}. Invoking the static methods in the
// * original class seems to cause the compilation errors even if we substitute it
// * with a no-op implementation. And so, the only way to fix it is to prevent any
// * call the original class at all.
// *
// * @author bnazare
// */
//@TargetClass(SAML1ComponentBuilder.class)
//@Delete
//final class Target_org_apache_wss4j_common_saml_builder_SAML1ComponentBuilder {
//}
//
///**
// * Removes {@link SAML2ComponentBuilder}. Invoking the static methods in the
// * original class seems to cause the compilation errors even if we substitute it
// * with a no-op implementation. And so, the only way to fix it is to prevent any
// * call the original class at all.
// *
// * @author bnazare
// */
//@TargetClass(SAML2ComponentBuilder.class)
//@Delete
//final class Target_org_apache_wss4j_common_saml_builder_SAML2ComponentBuilder {
//}
