package com.linkare.assinare.sign.dss;

import java.security.cert.X509Certificate;
import java.util.Arrays;

import eu.europa.esig.dss.model.x509.CertificateToken;

/**
 *
 * @author bnazare
 */
public class CertificateUtils {

    private CertificateUtils() {
    }

    public static X509Certificate[] convertCertificates(CertificateToken[] certTokens) {
        return Arrays.stream(certTokens)
                .map(CertificateToken::getCertificate)
                .toArray(X509Certificate[]::new);
    }

    public static CertificateToken[] convertCertificates(X509Certificate[] certs) {
        return Arrays.stream(certs)
                .map(CertificateToken::new)
                .toArray(CertificateToken[]::new);
    }

}
