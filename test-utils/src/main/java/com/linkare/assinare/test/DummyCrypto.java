package com.linkare.assinare.test;

import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.KeyStore.PasswordProtection;
import java.security.KeyStore.PrivateKeyEntry;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.UnrecoverableEntryException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Objects;

import eu.europa.esig.dss.token.SignatureTokenConnection;

/**
 *
 * @author bnazare
 */
public final class DummyCrypto {

    private static final char[] EMPTY_PASS = new char[0];

    private static final KeyStore DUMMIES_KS;

    private static final X509Certificate USER_A_CERT;
    private static final PrivateKey USER_A_PRIV_KEY;
    private static final PublicKey USER_A_PUB_KEY;

    private static final X509Certificate CMD_CERT;
    private static final PrivateKey CMD_PRIV_KEY;

    private static final X509Certificate TSA_CERT;
    private static final PrivateKey TSA_PRIV_KEY;

    private static final String DUMMIES_KS_LOCATION = "dummies.p12";
    private static final String USERA_ALIAS = "usera";
    private static final String CMD_ALIAS = "cmd";
    private static final String TSA_ALIAS = "tsa";

    static {
        try {
            DUMMIES_KS = loadP12KS(DUMMIES_KS_LOCATION, EMPTY_PASS);

            USER_A_CERT = loadCertificate(DUMMIES_KS, USERA_ALIAS);
            USER_A_PRIV_KEY = loadPrivateKey(DUMMIES_KS, USERA_ALIAS);
            USER_A_PUB_KEY = loadPublicKey(DUMMIES_KS, USERA_ALIAS);

            CMD_CERT = loadCertificate(DUMMIES_KS, CMD_ALIAS);
            CMD_PRIV_KEY = loadPrivateKey(DUMMIES_KS, CMD_ALIAS);

            TSA_CERT = loadCertificate(DUMMIES_KS, TSA_ALIAS);
            TSA_PRIV_KEY = loadPrivateKey(DUMMIES_KS, TSA_ALIAS);
        } catch (IOException | GeneralSecurityException ex) {
            throw new IllegalArgumentException(ex);
        }
    }

    private DummyCrypto() {
    }

    private static KeyStore loadP12KS(final String ksPath, final char[] password) throws CertificateException, NoSuchAlgorithmException, IOException, KeyStoreException {
        InputStream ksStream = DummyCrypto.class.getResourceAsStream(ksPath);
        KeyStore ks = KeyStore.getInstance("PKCS12");
        ks.load(ksStream, password);

        return ks;
    }

    private static X509Certificate loadCertificate(final KeyStore ks, final String alias) throws KeyStoreException {
        return (X509Certificate) ks.getCertificate(alias);
    }

    private static PrivateKey loadPrivateKey(final KeyStore ks, final String alias) throws NoSuchAlgorithmException, KeyStoreException, UnrecoverableKeyException {
        return Objects.requireNonNull((PrivateKey) ks.getKey(alias, null));
    }

    private static PublicKey loadPublicKey(final KeyStore ks, final String alias) throws KeyStoreException {
        X509Certificate cert = (X509Certificate) ks.getCertificate(alias);
        return cert.getPublicKey();
    }

    public static X509Certificate getUserACert() {
        return USER_A_CERT;
    }

    public static PrivateKey getUserAPrivKey() {
        return USER_A_PRIV_KEY;
    }

    public static PublicKey getUserAPubKey() {
        return USER_A_PUB_KEY;
    }

    public static X509Certificate getCmdCert() {
        return CMD_CERT;
    }

    public static PrivateKey getCmdPrivKey() {
        return CMD_PRIV_KEY;
    }

    public static X509Certificate getTsaCert() {
        return TSA_CERT;
    }

    public static PrivateKey getTsaPrivKey() {
        return TSA_PRIV_KEY;
    }

    public static SignatureTokenConnection getUserAToken() throws IOException, GeneralSecurityException {
        return getMockSignatureToken(USERA_ALIAS);
    }

    private static MockSignatureToken getMockSignatureToken(final String alias) throws NoSuchAlgorithmException, KeyStoreException, CertificateException, IOException, UnrecoverableEntryException {
        PrivateKeyEntry entry = (PrivateKeyEntry) DUMMIES_KS.getEntry(alias, new PasswordProtection(EMPTY_PASS));
        return new MockSignatureToken(entry);
    }

}
