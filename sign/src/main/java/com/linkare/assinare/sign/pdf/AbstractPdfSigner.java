package com.linkare.assinare.sign.pdf;

import java.io.IOException;

import com.linkare.assinare.commons.AssinareError;
import com.linkare.assinare.commons.AssinareException;
import com.linkare.assinare.commons.ui.AssinarePINGUI;
import com.linkare.assinare.commons.ui.SignatureStage;
import com.linkare.assinare.commons.ui.SignatureStageListener;
import com.linkare.assinare.sign.SigningKey;
import com.linkare.assinare.sign.model.AssinareDocument;
import com.linkare.assinare.sign.model.FileDocument;

/**
 *
 * @author bnazare
 */
public abstract class AbstractPdfSigner implements PdfSigner {

    @Override
    public AssinareDocument sign(SigningKey key, AssinareDocument doc, AssinarePINGUI pinCallback,
            PDFSignatureFields sigFields, SignatureStageListener listener) throws AssinareError, AssinareException {
        try {
            FileDocument tmpDoc = FileDocument.createTemporary();

            signDocument(key, doc, tmpDoc, sigFields, listener, pinCallback);

            return tmpDoc;
        } catch (IOException ioex) {
            // this wrapping is meant only for temp file related errors
            throw new AssinareError(ioex);
        }
    }

    private void signDocument(SigningKey key, AssinareDocument target, AssinareDocument dest, PDFSignatureFields sigFields,
            SignatureStageListener listener, AssinarePINGUI pinCallback) throws AssinareError, AssinareException {

        listener.publicPublish(SignatureStage.SEARCHING_FOR_CARD);

        pinCallback.setContextWorker(listener);

        try {
            key.lazyInit();

            listener.publicPublish(SignatureStage.CARD_FOUND);

            signPdf(key, target, dest, sigFields);
        } finally {
            pinCallback.documentDone();
        }
    }

    protected abstract void signPdf(SigningKey signingKey, AssinareDocument target, AssinareDocument dest, PDFSignatureFields sigFields) throws AssinareException, AssinareError;

}
