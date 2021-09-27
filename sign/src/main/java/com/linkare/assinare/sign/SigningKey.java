package com.linkare.assinare.sign;

import java.security.cert.X509Certificate;

import com.linkare.assinare.commons.AssinareError;
import com.linkare.assinare.commons.AssinareException;

/**
 *
 * @author bnazare
 */
public interface SigningKey {

    void lazyInit() throws AssinareException;

    X509Certificate getCertificate();

    X509Certificate[] getCertificateChain();

    String getEncryptionAlgorithm();

    byte[] sign(byte[] data, String hashAlgorithm) throws AssinareException, AssinareError;

}
