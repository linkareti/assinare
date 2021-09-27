package com.linkare.assinare.sign.keysupplier.dss;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import com.linkare.assinare.sign.KeySupplier;
import com.linkare.assinare.sign.SigningKey;

import at.gv.egiz.smcc.pin.gui.PINGUI;

/**
 *
 * @author bnazare
 */
public class LazyMOCCASigningKeySupplier implements KeySupplier {

    private final PINGUI pinCallback;
    private final List<LazyMOCCASigningKey> suppliedKeys;

    public LazyMOCCASigningKeySupplier(PINGUI pinCallback) {
        this.pinCallback = pinCallback;
        this.suppliedKeys = new LinkedList<>();
    }

    @Override
    public List<SigningKey> getKeys() {
        LazyMOCCASigningKey key = new LazyMOCCASigningKey(pinCallback);
        suppliedKeys.add(key);
        return Collections.singletonList(key);
    }

    @Override
    public void close() {
        suppliedKeys.forEach((suppliedKey) -> {
            suppliedKey.closeToken();
        });
        suppliedKeys.clear();
    }

}
