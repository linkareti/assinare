package com.linkare.assinare.sign.keysupplier.dss;

import java.io.IOException;
import java.security.GeneralSecurityException;

import com.linkare.assinare.test.DummyCrypto;

import eu.europa.esig.dss.token.SignatureTokenConnection;

/**
 *
 * @author bnazare
 */
public class MockSigningKey extends AbstractDSSSigningKey {

    public MockSigningKey() throws IOException, GeneralSecurityException {
        SignatureTokenConnection myToken = DummyCrypto.getUserAToken();
        this.signingToken = myToken;
        this.privateKey = myToken.getKeys().get(0);
    }

    @Override
    public void lazyInit() {
        // NOOP
    }

}
