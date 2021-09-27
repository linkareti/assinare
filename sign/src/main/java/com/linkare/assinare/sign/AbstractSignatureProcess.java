package com.linkare.assinare.sign;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.lang3.StringUtils;

import com.linkare.assinare.commons.AssinareError;
import com.linkare.assinare.commons.AssinareException;
import com.linkare.assinare.commons.ui.AssinarePINGUI;
import com.linkare.assinare.sign.fileprovider.FileAccessException;
import com.linkare.assinare.sign.fileprovider.FileService;
import com.linkare.assinare.sign.model.AssinareDocument;

/**
 *
 * @author bnazare
 * @param <D> the type of the data to sign
 * @param <K> the type of the signature fields
 */
public abstract class AbstractSignatureProcess<D, K extends SignatureFields> implements SignatureProcess<D, K> {

    private static final Logger LOG = Logger.getLogger(AbstractSignatureProcess.class.getName());

    private final FileService fileService;

    public AbstractSignatureProcess(FileService fileService) {
        this.fileService = fileService;
    }

    @Override
    public String doSignature(final String[] docList, final K sigFields) throws AssinareError, AssinareException {
        validateSignatureParameters(docList, sigFields);

        List<List<String>> docChunks = splitDocList(docList);

        AssinarePINGUI pinCallback = new AssinarePINGUI(docChunks.size());
        for (List<String> docChunk : docChunks) {
            signData(docChunk, sigFields, pinCallback);
        }

        return "Successo";
    }

    protected void validateSignatureParameters(final String[] docList, final K sigFields) throws AssinareException {
        if (docList.length < 1) {
            throw new AssinareException("Nenhum ficheiro foi selecionado!");
        }

        boolean emptyDoc = Arrays.stream(docList).anyMatch(StringUtils::isBlank);
        if (emptyDoc) {
            throw new AssinareException("ID de documento vazio!");
        }
    }

    protected void signData(final List<String> docNames, final K sigFields, final AssinarePINGUI pinCallback) throws AssinareError, AssinareException {
        SignatureViewer<D, K> sigViewer = null;
        KeySupplier keySupplier = null;
        try {
            keySupplier = getKeySupplier(sigFields, pinCallback);
            sigViewer = getSignatureViewer(sigFields, keySupplier);

            Future<K> futureSigFields = sigViewer.getSignatureFields();
            Future<SigningKey> futureSigningKey = sigViewer.getSigningKey();

            List<AssinareDocument> docs = getFiles(docNames);
            D srcData = prepareData(docs);
            sigViewer.dataReady(srcData);

            K finalSigFields = futureSigFields.get();
            SigningKey signingKey = futureSigningKey.get();

            Signer<D, K> signer = getSigner(finalSigFields);
            AssinareDocument signedFile = signer.sign(signingKey, srcData, pinCallback, finalSigFields, sigViewer);

            fileService.putFile(getDestDocName(docNames, finalSigFields), signedFile);
        } catch (CancellationException ex) {
            throw new AssinareException("Processo de Assinatura Cancelado.");
        } catch (InterruptedException ex) {
            throw new AssinareError(ex);
        } catch (ExecutionException | InvocationTargetException ex) {
            Throwable cause = ex.getCause();

            if (cause == null) {
                throw new AssinareError(ex);
            } else if (cause instanceof AssinareException) {
                throw (AssinareException) cause;
            } else if (cause instanceof AssinareError) {
                throw (AssinareError) cause;
            } else {
                throw new AssinareError(cause);
            }
        } finally {
            if (sigViewer != null) {
                sigViewer.signatureDone();
                sigViewer.dispose();
            }

            if (keySupplier != null) {
                try {
                    keySupplier.close();
                } catch (Exception ex) {
                    LOG.log(Level.WARNING, null, ex);
                }
            }
        }
    }

    protected List<AssinareDocument> getFiles(final List<String> docNames) throws FileAccessException {
        List<AssinareDocument> docs = new ArrayList<>(docNames.size());
        for (String docName : docNames) {
            AssinareDocument doc = fileService.getFile(docName);
            docs.add(doc);
        }

        return docs;
    }

    protected abstract List<List<String>> splitDocList(final String[] docList);

    protected abstract D prepareData(final List<AssinareDocument> docs) throws AssinareException;

    protected abstract Signer<D, K> getSigner(final K finalSigFields) throws AssinareError, AssinareException;

    protected abstract String getDestDocName(final List<String> docNames, final K finalSigFields);

    protected abstract KeySupplier getKeySupplier(final K sigFields, final AssinarePINGUI pinCallback);

    protected abstract SignatureViewer<D, K> getSignatureViewer(final K sigFields, final KeySupplier keySupplier) throws InterruptedException, InvocationTargetException, ExecutionException;

}
