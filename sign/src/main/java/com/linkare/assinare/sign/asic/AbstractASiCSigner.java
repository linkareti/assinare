package com.linkare.assinare.sign.asic;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import com.linkare.assinare.commons.AssinareError;
import com.linkare.assinare.commons.AssinareException;
import com.linkare.assinare.commons.ui.AssinarePINGUI;
import com.linkare.assinare.commons.ui.SignatureStage;
import com.linkare.assinare.commons.ui.SignatureStageListener;
import com.linkare.assinare.sign.SigningKey;
import com.linkare.assinare.sign.fileprovider.FileAccessException;
import com.linkare.assinare.sign.model.AssinareDocument;
import com.linkare.assinare.sign.model.FileDocument;

/**
 *
 * @author bnazare
 */
public abstract class AbstractASiCSigner implements ASiCSigner {

    private final boolean reSign;

    public AbstractASiCSigner() {
        this(false);
    }

    public AbstractASiCSigner(boolean reSign) {
        this.reSign = reSign;
    }

    @Override
    public AssinareDocument sign(SigningKey key, List<AssinareDocument> filesData, AssinarePINGUI pinCallback,
            ASiCSignatureFields sigFields, SignatureStageListener listener) throws AssinareError, AssinareException, FileAccessException {
        try {
            FileDocument tmpFile = FileDocument.createTemporary();

            signDocumentsInContainer(key, filesData, tmpFile, sigFields, listener, pinCallback);

            return tmpFile;
        } catch (IOException ioex) {
            // this wrapping is meant only for temp file related errors
            throw new AssinareError(ioex);
        }
    }

    private void signDocumentsInContainer(SigningKey key, List<AssinareDocument> targets, AssinareDocument dest,
            ASiCSignatureFields sigFields, SignatureStageListener listener, AssinarePINGUI pinCallback) throws AssinareError, AssinareException {

        listener.publicPublish(SignatureStage.SEARCHING_FOR_CARD);

        pinCallback.setContextWorker(listener);

        try {
            key.lazyInit();

            listener.publicPublish(SignatureStage.CARD_FOUND);

            byte[] signedContainer;
            if (reSign) {
                signedContainer = reSignContainer(key, targets.get(0), sigFields);
            } else {
                signedContainer = signDocuments(key, targets, sigFields);
            }

            try (OutputStream destStream = dest.openOutputStream()) {
                destStream.write(signedContainer);
            }
        } catch (IOException ex) {
            throw new AssinareError(ex);
        } finally {
            pinCallback.documentDone();
        }
    }

    protected abstract byte[] reSignContainer(SigningKey signingKey, AssinareDocument get, ASiCSignatureFields sigFields) throws IOException, AssinareException, AssinareError;

    protected abstract byte[] signDocuments(SigningKey signingKey, List<AssinareDocument> targets, ASiCSignatureFields sigFields) throws IOException, AssinareException, AssinareError;
}
