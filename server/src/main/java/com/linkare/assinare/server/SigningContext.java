package com.linkare.assinare.server;

import java.security.cert.X509Certificate;
import java.time.ZonedDateTime;
import java.util.List;

import com.linkare.assinare.sign.pdf.PDFSignatureFields;

/**
 *
 * @author bnazare
 */
public class SigningContext {

    private final ZonedDateTime signingDate;
    private final List<X509Certificate> certChain;
    private final List<String> docNames;
    private final PDFSignatureFields sigFields;

    public SigningContext(ZonedDateTime signingDate, List<X509Certificate> certChain, List<String> docNames, PDFSignatureFields sigFields) {
        this.signingDate = signingDate;
        this.certChain = List.copyOf(certChain);
        this.docNames = List.copyOf(docNames);
        this.sigFields = sigFields;
    }

    public ZonedDateTime getSigningDate() {
        return signingDate;
    }

    public List<X509Certificate> getCertChain() {
        return certChain;
    }

    public List<String> getDocNames() {
        return docNames;
    }

    public PDFSignatureFields getSigFields() {
        return sigFields;
    }

}
