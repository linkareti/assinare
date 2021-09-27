package com.linkare.assinare.sign.pdf.itext;

import java.security.GeneralSecurityException;

import com.itextpdf.text.pdf.security.ExternalSignature;
import com.linkare.assinare.commons.AssinareError;
import com.linkare.assinare.commons.AssinareException;
import com.linkare.assinare.sign.SigningKey;

/**
 *
 * @author bnazare
 */
class SigningKeyExternalSignature implements ExternalSignature {

    private final String hashAlgorithm;
    private final SigningKey signingKey;

    public SigningKeyExternalSignature(String hashAlgorithm, SigningKey signingKey) {
        this.hashAlgorithm = hashAlgorithm;
        this.signingKey = signingKey;
    }

    @Override
    public String getHashAlgorithm() {
        return hashAlgorithm;
    }

    @Override
    public String getEncryptionAlgorithm() {
        return signingKey.getEncryptionAlgorithm();
    }

    @Override
    public byte[] sign(byte[] message) throws GeneralSecurityException {
        try {
            return signingKey.sign(message, hashAlgorithm);
        } catch (AssinareException | AssinareError ex) {
            throw new GeneralSecurityException(ex);
        }
    }

}
