package com.linkare.assinare.sign;

import java.util.concurrent.Future;

import com.linkare.assinare.commons.ui.SignatureStageListener;

/**
 *
 * @author bnazare
 * @param <D> the type of the data displayed
 * @param <K> the type of signature fields
 */
public interface SignatureViewer<D, K extends SignatureFields> extends SignatureStageListener {

    void dataReady(D data);

    Future<K> getSignatureFields();

    Future<SigningKey> getSigningKey();

    void signatureDone();

    void dispose();

}
