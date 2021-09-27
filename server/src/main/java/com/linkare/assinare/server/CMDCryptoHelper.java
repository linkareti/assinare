package com.linkare.assinare.server;

import static java.nio.charset.StandardCharsets.US_ASCII;

import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.util.Collection;

import javax.crypto.Cipher;
import javax.enterprise.context.Dependent;

/**
 *
 * @author bnazare
 */
@Dependent
public class CMDCryptoHelper {

    private static final String CIPHER_ALGO = "RSA/ECB/PKCS1Padding";
    private static final String CMD_CERT_PATH = "certs/certnew.cer";

    /**
     * DO NOT USE OUTSIDE OF {@link CMDCryptoHelper}. Made protected to help
     * with tests only.
     */
    protected static final Certificate CMD_CERT;

    /**
     * DO NOT USE OUTSIDE OF {@link CMDCryptoHelper}. Made protected to help
     * with tests only.
     */
    protected Cipher cipher;

    static {
        try (final InputStream certStream = CMDCryptoHelper.class.getClassLoader().getResourceAsStream(CMD_CERT_PATH)) {
            final CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
            final Collection<? extends Certificate> certChain = certFactory.generateCertificates(certStream);

            CMD_CERT = certChain.iterator().next();
        } catch (IOException | CertificateException ex) {
            throw new IllegalStateException(ex);
        }
    }

    public CMDCryptoHelper() {
        try {
            cipher = Cipher.getInstance(CIPHER_ALGO);
            cipher.init(Cipher.ENCRYPT_MODE, CMD_CERT);
        } catch (GeneralSecurityException ex) {
            throw new IllegalStateException(ex);
        }
    }

    public byte[] encrypt(final String data) {
        return encrypt(data.getBytes(US_ASCII));
    }

    public byte[] encrypt(final byte[] data) {
        try {
            return cipher.doFinal(data);
        } catch (GeneralSecurityException ex) {
            throw new IllegalArgumentException(ex);
        }
    }
}
