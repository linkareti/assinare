package com.linkare.assinare.sign.pdf;

import static java.util.stream.Collectors.toList;

import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.RunnableFuture;
import java.util.stream.Stream;

import javax.swing.SwingUtilities;

import com.linkare.assinare.commons.AssinareError;
import com.linkare.assinare.commons.AssinareException;
import com.linkare.assinare.commons.ui.AssinarePINGUI;
import com.linkare.assinare.sign.AbstractSignatureProcess;
import com.linkare.assinare.sign.KeySupplier;
import com.linkare.assinare.sign.MinimalSignatureViewer;
import com.linkare.assinare.sign.SignatureViewer;
import com.linkare.assinare.sign.Signer;
import com.linkare.assinare.sign.SimpleSignatureViewer;
import com.linkare.assinare.sign.fileprovider.FileService;
import com.linkare.assinare.sign.keysupplier.dss.LazyMOCCASigningKeySupplier;
import com.linkare.assinare.sign.keysupplier.dss.MultiSigningKeySupplier;
import com.linkare.assinare.sign.model.AssinareDocument;
import com.linkare.assinare.sign.pdf.dss.DssPdfSigner;

/**
 *
 * @author bnazare
 */
public class PdfBatchSignatureProcess extends AbstractSignatureProcess<AssinareDocument, PDFSignatureFields> {

    public PdfBatchSignatureProcess(final FileService fileService) {
        super(fileService);
    }

    @Override
    protected List<List<String>> splitDocList(final String[] docList) {
        return Stream.of(docList).map(
                docName -> Collections.singletonList(docName)
        ).collect(toList());
    }

    @Override
    protected AssinareDocument prepareData(final List<AssinareDocument> docs) {
        if (docs.size() == 1) {
            return docs.get(0);
        } else {
            // This should never happen.
            // Hitting this condition means a programming error,
            // most likely in method splitDocList().
            // Thus, there's no need to throw a checked exception.
            throw new IllegalArgumentException("Too many documents received");
        }
    }

    @Override
    protected Signer<AssinareDocument, PDFSignatureFields> getSigner(final PDFSignatureFields finalSigFields) throws AssinareError, AssinareException {
        return new DssPdfSigner();
    }

    @Override
    protected String getDestDocName(final List<String> docNames, final PDFSignatureFields finalSigFields) {
        return docNames.get(0);
    }

    @Override
    protected KeySupplier getKeySupplier(final PDFSignatureFields sigFields, final AssinarePINGUI pinCallback) {
        if (sigFields.getUiMode() == UiMode.SIMPLE) {
            return new MultiSigningKeySupplier(pinCallback);
        } else {
            return new LazyMOCCASigningKeySupplier(pinCallback);
        }
    }

    @Override
    protected SignatureViewer<AssinareDocument, PDFSignatureFields> getSignatureViewer(final PDFSignatureFields sigFields, final KeySupplier keySupplier) throws InterruptedException, InvocationTargetException, ExecutionException {
        UiMode uiMode = sigFields.getUiMode();
        if (uiMode == UiMode.MINIMAL) {
            return new MinimalSignatureViewer<>(sigFields, keySupplier);
        } else if (uiMode == UiMode.SIMPLE) {
            return showSimplePdfSignatureViewer(sigFields, keySupplier);
        } else {
            return showPdfSignatureViewer(sigFields, keySupplier);
        }
    }

    private SignatureViewer<AssinareDocument, PDFSignatureFields> showPdfSignatureViewer(final PDFSignatureFields sigFields, final KeySupplier signingKeysSupplier) throws InterruptedException, InvocationTargetException, ExecutionException {
        RunnableFuture<PdfSignatureViewer> task = new FutureTask(() -> {
            PdfSignatureViewer pdfSigViewer;
            pdfSigViewer = new PdfSignatureViewer(false, sigFields, signingKeysSupplier);
            pdfSigViewer.setLocationRelativeTo(null);
            pdfSigViewer.setVisible(true); // non-blocking

            return pdfSigViewer;
        });

        SwingUtilities.invokeAndWait(task);

        return task.get();
    }

    private SignatureViewer<AssinareDocument, PDFSignatureFields> showSimplePdfSignatureViewer(final PDFSignatureFields sigFields, final KeySupplier signingKeysSupplier) throws InterruptedException, InvocationTargetException, ExecutionException {
        RunnableFuture<SimpleSignatureViewer> task = new FutureTask(() -> {
            SimpleSignatureViewer pdfSigViewer;
            pdfSigViewer = new SimpleSignatureViewer(false, sigFields, signingKeysSupplier);
            pdfSigViewer.setLocationRelativeTo(null);
            pdfSigViewer.setVisible(true); // non-blocking

            return pdfSigViewer;
        });

        SwingUtilities.invokeAndWait(task);

        return task.get();
    }

}
