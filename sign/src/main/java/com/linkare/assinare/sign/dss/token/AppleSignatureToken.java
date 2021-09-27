package com.linkare.assinare.sign.dss.token;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.KeyStore.PasswordProtection;

import eu.europa.esig.dss.model.DSSException;
import eu.europa.esig.dss.token.AbstractKeyStoreTokenConnection;

/**
 *
 * @author bnazare
 */
public class AppleSignatureToken extends AbstractKeyStoreTokenConnection {

    @Override
    protected KeyStore getKeyStore() throws DSSException {
        KeyStore keyStore;
        try {
            keyStore = KeyStore.getInstance("KeychainStore");
            keyStore.load(null, null);
        } catch (IOException | GeneralSecurityException e) {
            throw new DSSException("Unable to load Apple keystore", e);
        }
        return keyStore;
    }

    @Override
    protected PasswordProtection getKeyProtectionParameter() {
        return new PasswordProtection("dummy".toCharArray());
    }

    @Override
    public void close() {
        // NOOP
    }

}
