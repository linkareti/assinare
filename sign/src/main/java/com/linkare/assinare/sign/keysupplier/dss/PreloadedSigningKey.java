package com.linkare.assinare.sign.keysupplier.dss;

import eu.europa.esig.dss.token.DSSPrivateKeyEntry;
import eu.europa.esig.dss.token.SignatureTokenConnection;

/**
 *
 * @author bnazare
 */
class PreloadedSigningKey extends AbstractDSSSigningKey {

    PreloadedSigningKey(SignatureTokenConnection signingToken, DSSPrivateKeyEntry privateKey) {
        super(signingToken, privateKey);
    }

    @Override
    public void lazyInit() {
        // NOOP
    }

    @Override
    public String toString() {
        return "PreloadedSigningKey{" + "signingToken=" + signingToken.getClass().getSimpleName() + ", privateKey=" + privateKey.getClass().getSimpleName() + ":" + privateKey.getCertificate().getSubject().getPrincipal() + '}';
    }

}
