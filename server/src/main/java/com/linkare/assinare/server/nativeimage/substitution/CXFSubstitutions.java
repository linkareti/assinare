package com.linkare.assinare.server.nativeimage.substitution;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.function.BooleanSupplier;

import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.apache.cxf.interceptor.FIStaxInInterceptor;
import org.apache.cxf.interceptor.FIStaxOutInterceptor;

import com.oracle.svm.core.annotate.Substitute;
import com.oracle.svm.core.annotate.TargetClass;

/**
 *
 * @author bnazare
 */
public interface CXFSubstitutions {

    static final String NOT_SUPPORTED_IN_NATIVE_BUILD = "Not supported in native build";

}

/**
 * Verifies if the FastInfoset classes are missing from the classpath. Since
 * testing all classes would be infeasible, we test the few classes used by
 * {@link FIStaxInInterceptor} and {@link FIStaxOutInterceptor}, which are
 * themselves the main users of FastInfoset.
 *
 * @author bnazare
 */
class FastInfosetMissing implements BooleanSupplier {

    /**
     * Used by {@link FIStaxInInterceptor}.
     */
    private static final String PARSER_CLASSNAME = "com.sun.xml.fastinfoset.stax.StAXDocumentParser";

    /**
     * Used by {@link FIStaxOutInterceptor}.
     */
    private static final String SERIALIZER_CLASSNAME = "com.sun.xml.fastinfoset.stax.StAXDocumentSerializer";

    @Override
    public boolean getAsBoolean() {
        try {
            Class.forName(PARSER_CLASSNAME);
        } catch (ClassNotFoundException ex) {
            return true;
        }

        try {
            Class.forName(SERIALIZER_CLASSNAME);
        } catch (ClassNotFoundException ex) {
            return true;
        }

        return false;
    }

}

/**
 * Substitutes {@link FIStaxInInterceptor} when FastInfoset classes are not
 * found in the classpath.
 *
 * @author bnazare
 */
@TargetClass(className = "org.apache.cxf.interceptor.FIStaxInInterceptor", onlyWith = FastInfosetMissing.class)
final class Target_org_apache_cxf_interceptor_FIStaxInInterceptor {

    @Substitute
    private XMLStreamReader getParser(InputStream in) {
        throw new UnsupportedOperationException("FastInfoset support was requested but its classes are not present in the classpath.");
    }

}

/**
 * Substitutes {@link FIStaxOutInterceptor} when FastInfoset classes are not
 * found in the classpath.
 *
 * @author bnazare
 */
@TargetClass(className = "org.apache.cxf.interceptor.FIStaxOutInterceptor", onlyWith = FastInfosetMissing.class)
final class Target_org_apache_cxf_interceptor_FIStaxOutInterceptor {

    @Substitute
    private XMLStreamWriter getOutput(OutputStream out) {
        throw new UnsupportedOperationException("FastInfoset support was requested but its classes are not present in the classpath.");
    }

}
