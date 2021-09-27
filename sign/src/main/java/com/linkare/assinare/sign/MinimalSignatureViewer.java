package com.linkare.assinare.sign;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

import com.linkare.assinare.commons.ui.SignatureStage;

/**
 *
 * @author bnazare
 * @param <D> the type of the data displayed
 * @param <K> the type of signature fields
 */
public class MinimalSignatureViewer<D, K extends SignatureFields> implements SignatureViewer<D, K> {

    private final CompletableFuture<K> sigFieldsFuture;
    private final CompletableFuture<SigningKey> sigKeyFuture;

    public MinimalSignatureViewer(K sigOptions, KeySupplier signingKeysSupplier) {
        this.sigFieldsFuture = CompletableFuture.completedFuture(sigOptions);
        this.sigKeyFuture = CompletableFuture.completedFuture(signingKeysSupplier.getKeys().get(0));
    }

    @Override
    public Future<K> getSignatureFields() {
        return sigFieldsFuture;
    }

    @Override
    public Future<SigningKey> getSigningKey() {
        return sigKeyFuture;
    }

    @Override
    public void dataReady(D data) {
        // NOOP
    }

    @Override
    public void signatureDone() {
        // NOOP
    }

    @Override
    public void dispose() {
        // NOOP
    }

    @Override
    public void publicPublish(SignatureStage... chunks) {
        // NOOP
    }

}
