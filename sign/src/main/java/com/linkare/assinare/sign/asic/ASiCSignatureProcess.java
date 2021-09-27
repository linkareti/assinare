package com.linkare.assinare.sign.asic;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.RunnableFuture;

import javax.swing.SwingUtilities;

import com.linkare.assinare.commons.AssinareError;
import com.linkare.assinare.commons.AssinareException;
import com.linkare.assinare.commons.ui.AssinarePINGUI;
import com.linkare.assinare.sign.AbstractSignatureProcess;
import com.linkare.assinare.sign.KeySupplier;
import com.linkare.assinare.sign.SignatureViewer;
import com.linkare.assinare.sign.Signer;
import com.linkare.assinare.sign.asic.dss.DssASiCParser;
import com.linkare.assinare.sign.asic.dss.DssASiCSigner;
import com.linkare.assinare.sign.fileprovider.FileService;
import com.linkare.assinare.sign.keysupplier.dss.LazyMOCCASigningKeySupplier;
import com.linkare.assinare.sign.model.AssinareDocument;

/**
 *
 * @author bnazare
 */
public class ASiCSignatureProcess extends AbstractSignatureProcess<List<AssinareDocument>, ASiCSignatureFields> {

    private boolean reSign;

    public ASiCSignatureProcess(final FileService fileService) {
        super(fileService);
    }

    @Override
    protected List<List<String>> splitDocList(final String[] docList) {
        return Collections.singletonList(Arrays.asList(docList));
    }

    @Override
    protected List<AssinareDocument> prepareData(final List<AssinareDocument> docs) throws AssinareException {
        reSign = checkReSigning(docs);

        return docs;
    }

    @Override
    protected Signer<List<AssinareDocument>, ASiCSignatureFields> getSigner(final ASiCSignatureFields finalSigFields) throws AssinareError, AssinareException {
        return new DssASiCSigner(reSign);
    }

    @Override
    protected String getDestDocName(final List<String> docNames, final ASiCSignatureFields finalSigFields) {
        return finalSigFields.getContainerName();
    }

    @Override
    protected LazyMOCCASigningKeySupplier getKeySupplier(final ASiCSignatureFields sigFields, final AssinarePINGUI pinCallback) {
        return new LazyMOCCASigningKeySupplier(pinCallback);
    }

    @Override
    protected SignatureViewer<List<AssinareDocument>, ASiCSignatureFields> getSignatureViewer(final ASiCSignatureFields sigFields, final KeySupplier keySupplier) throws InterruptedException, InvocationTargetException, ExecutionException {
        return showContainerSignatureViewer(sigFields, keySupplier);
    }

    private boolean checkReSigning(final Collection<AssinareDocument> docs) throws AssinareException {
        int resignCount = 0;
        ASiCParser asicParser = new DssASiCParser();
        for (AssinareDocument doc : docs) {
            if (asicParser.isReSignable(doc)) {
                resignCount++;

                if (resignCount > 1) {
                    throw new AssinareException("Não é possível assinar múltiplos ficheiros ASiC simultâneamente");
                } else if (resignCount == 1 && docs.size() > 1) {
                    throw new AssinareException("Não é possível assinar ficheiros ASiC e não-ASiC simultâneamente");
                }
            }
        }

        return resignCount > 0;
    }

    private SignatureViewer<List<AssinareDocument>, ASiCSignatureFields> showContainerSignatureViewer(final ASiCSignatureFields sigFields, final KeySupplier signingKeysSupplier) throws InterruptedException, InvocationTargetException, ExecutionException {
        RunnableFuture<ContainerSignatureViewer> task = new FutureTask(() -> {
            ContainerSignatureViewer containerSigViewer;
            containerSigViewer = new ContainerSignatureViewer(false, sigFields, signingKeysSupplier);
            containerSigViewer.setLocationRelativeTo(null);
            containerSigViewer.setVisible(true); // non-blocking

            return containerSigViewer;
        });

        SwingUtilities.invokeAndWait(task);

        return task.get();
    }

}
