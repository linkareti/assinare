package com.linkare.assinare.sign.keysupplier.dss;

import java.security.cert.X509Certificate;

import org.apache.commons.lang3.StringUtils;

import com.linkare.assinare.commons.AssinareError;
import com.linkare.assinare.commons.AssinareException;
import com.linkare.assinare.sign.SigningKey;
import com.linkare.assinare.sign.dss.CertificateUtils;

import at.gv.egiz.smcc.CancelledException;
import eu.europa.esig.dss.enumerations.DigestAlgorithm;
import eu.europa.esig.dss.model.DSSException;
import eu.europa.esig.dss.model.SignatureValue;
import eu.europa.esig.dss.model.ToBeSigned;
import eu.europa.esig.dss.token.DSSPrivateKeyEntry;
import eu.europa.esig.dss.token.SignatureTokenConnection;

/**
 *
 * @author bnazare
 */
abstract class AbstractDSSSigningKey implements SigningKey {

    protected SignatureTokenConnection signingToken;
    protected DSSPrivateKeyEntry privateKey;

    AbstractDSSSigningKey() {
    }

    AbstractDSSSigningKey(SignatureTokenConnection signingToken, DSSPrivateKeyEntry privateKey) {
        this.signingToken = signingToken;
        this.privateKey = privateKey;
    }

    @Override
    public X509Certificate getCertificate() {
        return privateKey.getCertificate().getCertificate();
    }

    @Override
    public X509Certificate[] getCertificateChain() {
        return CertificateUtils.convertCertificates(privateKey.getCertificateChain());
    }

//    @Override
//    public Certificate[] getSigningCertificateChain() throws AssinareError {
//        CertificateToken[] certificateChain = privateKey.getCertificateChain();
//        X509Certificate holderCert = certificateChain[0].getCertificate();
//        X509Certificate caCert = null;
//        if (certificateChain.length > 1) {
//            caCert = certificateChain[1].getCertificate();
//        }
//
//        return CertChainPatcher.buildCertificateChain(holderCert, caCert).toArray(new Certificate[0]);
//    }
    @Override
    public String getEncryptionAlgorithm() {
        return privateKey.getEncryptionAlgorithm().getName();
    }

    @Override
    public byte[] sign(byte[] data, String hashAlgorithm) throws AssinareException, AssinareError {
        try {
            ToBeSigned toBeSigned = new ToBeSigned(data);
            SignatureValue signValue = signingToken.sign(toBeSigned, DigestAlgorithm.forName(hashAlgorithm), privateKey);
            return signValue.getValue();
        } catch (DSSException ex) {
            Throwable cause = ex.getCause();
            if (cause instanceof CancelledException) {
                if (StringUtils.isBlank(cause.getMessage())) {
                    throw new AssinareException("PIN inserido de forma incorrecta.", ex);
                } else {
                    throw new AssinareException(cause.getMessage(), ex);
                }
            } else {
                throw new AssinareError(ex);
            }
        }
    }

}
