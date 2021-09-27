package com.linkare.assinare.sign.dss;

import com.linkare.assinare.commons.AssinareError;
import com.linkare.assinare.commons.AssinareException;
import com.linkare.assinare.sign.SigningKey;

import eu.europa.esig.dss.enumerations.DigestAlgorithm;
import eu.europa.esig.dss.enumerations.EncryptionAlgorithm;
import eu.europa.esig.dss.enumerations.SignatureAlgorithm;
import eu.europa.esig.dss.model.SignatureValue;
import eu.europa.esig.dss.model.ToBeSigned;

/**
 *
 * @author bnazare
 */
public class SigningUtils {

    private SigningUtils() {
    }

    public static SignatureValue sign(SigningKey signingKey, ToBeSigned dataToSign, DigestAlgorithm digestAlgorithm) throws AssinareError, AssinareException {
        byte[] signedData = signingKey.sign(dataToSign.getBytes(), digestAlgorithm.getName());

        EncryptionAlgorithm encryptionAlgorithm = EncryptionAlgorithm.forName(signingKey.getEncryptionAlgorithm());
        SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.getAlgorithm(encryptionAlgorithm, digestAlgorithm);
        SignatureValue signatureValue = new SignatureValue(signatureAlgorithm, signedData);

        return signatureValue;
    }

}
