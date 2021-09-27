package com.linkare.assinare.test;

import java.security.KeyStore.PrivateKeyEntry;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import eu.europa.esig.dss.model.DSSException;
import eu.europa.esig.dss.token.AbstractSignatureTokenConnection;
import eu.europa.esig.dss.token.DSSPrivateKeyEntry;
import eu.europa.esig.dss.token.KSPrivateKeyEntry;

/**
 *
 * @author bnazare
 */
public class MockSignatureToken extends AbstractSignatureTokenConnection {

    private static final String DUMMY_ALIAS = "foo";

    final List<DSSPrivateKeyEntry> keys;

    public MockSignatureToken(PrivateKeyEntry keyEntry) {
        Objects.requireNonNull(keyEntry);
        KSPrivateKeyEntry dssEntry = new KSPrivateKeyEntry(DUMMY_ALIAS, keyEntry);
        this.keys = Collections.singletonList(dssEntry);
    }

    @Override
    public List<DSSPrivateKeyEntry> getKeys() throws DSSException {
        return keys;
    }

    @Override
    public void close() {
        // NOOP
    }

}
