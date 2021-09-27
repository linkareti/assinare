package com.linkare.assinare.sign;

import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;

import javax.security.auth.x500.X500Principal;

import com.linkare.assinare.commons.AssinareError;

/**
 *
 * @deprecated Chain patching should not be needed. Use TSL-based validation
 * instead.
 * @author bnazare
 */
@Deprecated
public class CertChainPatcher {

    private static final String CC_KS_FILENAME = "ccKeystore";
    private static final String SUB_EC_KS_FILENAME = "subECEstado.jks";
    private static final String KEYSTORE_PASSWORD = "changeit";

    private static KeyStore ccKS;
    private static KeyStore subECKS;

    private CertChainPatcher() {
    }

    // TSL-based implementation, unused
    private static List<Certificate> buildCertificateChainFromTSL(X509Certificate holderCert, X509Certificate caCert) throws AssinareError {
        List<Certificate> chain = new LinkedList<>();

        // commented out because the method CommonCertificateSource#get used
        // below disappearead without replacement in v5.5
//        chain.add(holderCert);
//        if (caCert != null) {
//            chain.add(caCert);
//        } else {
//            if (isCCSubCAApplicable(holderCert)) {
//                X509Certificate cert = findCertificate(getSubECKS(), holderCert.getIssuerX500Principal());
//                if (cert != null) {
//                    chain.add(cert);
//                }
//            }
//        }
//        TrustedListsCertificateSource tslCertificateSource = DssTSLUtils.buildTSLCertificateSource();
//        X509Certificate lastCert = (X509Certificate) chain.get(chain.size() - 1);
//        List<CertificateToken> nextCerts = tslCertificateSource.get(lastCert.getIssuerX500Principal());
//        while (!nextCerts.isEmpty()) {
//            X509Certificate nextCert = nextCerts.get(0).getCertificate();
//            chain.add(nextCert);
//            nextCerts = tslCertificateSource.get(nextCert.getIssuerX500Principal());
//        }
        return chain;
    }

    public static List<Certificate> buildCertificateChain(X509Certificate holderCert) throws AssinareError {
        return buildCertificateChain(holderCert, null);
    }

    public static List<Certificate> buildCertificateChain(X509Certificate holderCert, X509Certificate caCert) throws AssinareError {
        List<Certificate> chain = new LinkedList<>();
        chain.add(holderCert);
        if (caCert != null) {
            chain.add(caCert);
        } else {
            if (isCCSubCAApplicable(holderCert)) {
                X509Certificate cert = findCertificate(getSubECKS(), holderCert.getIssuerX500Principal());
                if (cert != null) {
                    chain.add(cert);
                }
            }
        }
        X509Certificate lastCert = (X509Certificate) chain.get(chain.size() - 1);
        while (lastCert != null && !lastCert.getSubjectX500Principal().equals(lastCert.getIssuerX500Principal())) {
            lastCert = findCertificate(getCcKS(), lastCert.getIssuerX500Principal());
            if (lastCert != null) {
                chain.add(lastCert);
            }
        }
        return chain;
    }

    private static boolean isCCSubCAApplicable(X509Certificate holderCert) throws AssinareError {
        X500NameParser holder = new X500NameParser(holderCert.getSubjectX500Principal());
        String holderCountry = holder.getCountry();
        String holderOrg = holder.getOrganization();

        // inverted equals because values can be null
        return "PT".equals(holderCountry) && "Cartão de Cidadão".equals(holderOrg);
    }

    private static X509Certificate findCertificate(KeyStore ks, X500Principal subject) throws AssinareError {
        try {
            Enumeration<String> aliases = ks.aliases();

            while (aliases.hasMoreElements()) {
                X509Certificate cert = (X509Certificate) ks.getCertificate(aliases.nextElement());
                if (cert.getSubjectX500Principal().equals(subject)) {
                    return cert;
                }
            }

            return null;
        } catch (KeyStoreException ksex) {
            // should never happen apart from programming errors
            throw new AssinareError(ksex);
        }
    }

    private static KeyStore getCcKS() throws AssinareError {
        if (ccKS == null) {
            ccKS = getKeyStore(CC_KS_FILENAME, KEYSTORE_PASSWORD.toCharArray());
        }

        return ccKS;
    }

    private static KeyStore getSubECKS() throws AssinareError {
        if (subECKS == null) {
            subECKS = getKeyStore(SUB_EC_KS_FILENAME, KEYSTORE_PASSWORD.toCharArray());
        }

        return subECKS;
    }

    private static KeyStore getKeyStore(String file, char[] password) throws AssinareError {
        try {
            KeyStore ks = KeyStore.getInstance("JKS");
            ks.load(CertChainPatcher.class.getClassLoader().getResourceAsStream(file), password);

            return ks;
        } catch (KeyStoreException | IOException | NoSuchAlgorithmException | CertificateException ex) {
            // these should never happen apart from programming errors
            throw new AssinareError(ex);
        }
    }

}
