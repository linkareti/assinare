package com.linkare.assinare.sign;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JFileChooser;
import javax.swing.SwingUtilities;

import com.linkare.assinare.commons.AssinareError;
import com.linkare.assinare.commons.AssinareException;
import com.linkare.assinare.commons.utils.MOCCAUtils;
import com.linkare.assinare.sign.asic.ASiCSignatureFields;
import com.linkare.assinare.sign.asic.ASiCSignatureProcess;
import com.linkare.assinare.sign.fileprovider.FileService;
import com.linkare.assinare.sign.fileprovider.HTTPFileService;
import com.linkare.assinare.sign.fileprovider.HTTPFileServiceConfiguration;
import com.linkare.assinare.sign.fileprovider.LocalWrapperFileService;
import com.linkare.assinare.sign.pdf.PDFSignatureFields;
import com.linkare.assinare.sign.pdf.PdfBatchSignatureProcess;
import com.linkare.assinare.sign.swing.SwingHelper;

public final class AssinareSign {

    private static final Logger LOG = Logger.getLogger(AssinareSign.class.getName());

    private FileService wrappedFileService;

    public AssinareSign() {
        this(null);
    }

    public AssinareSign(final HTTPFileServiceConfiguration config) {
        FileService httpFileService = new HTTPFileService(config);
        this.wrappedFileService = new LocalWrapperFileService(httpFileService);

        SwingHelper.setupLookAndFeel();
        try {
            MOCCAUtils.loadPCSCLibrary();
        } catch (AssinareError e) {
            LOG.log(Level.SEVERE, "Failed to initialize Assinare. Reason: " + e.getMessage(), e);
            throw new RuntimeException(e);
        }
        LOG.info("AssinareMain initialized.");
    }

    public void reconfigure(HTTPFileServiceConfiguration config) {
        FileService httpFileService = new HTTPFileService(config);
        this.wrappedFileService = new LocalWrapperFileService(httpFileService);
    }

    public String signPdf(final String[] docList, final PDFSignatureFields sigFields) throws AssinareException, AssinareError {
        return new PdfBatchSignatureProcess(this.wrappedFileService).doSignature(docList, sigFields);
    }

    public String signContainer(final String[] docList, final ASiCSignatureFields sigFields) throws AssinareException, AssinareError {
        return new ASiCSignatureProcess(this.wrappedFileService).doSignature(docList, sigFields);
    }

    public String[] chooseLocalFiles() throws AssinareException, AssinareError {
        try {
            FutureTask<File[]> task = new FutureTask<>(() -> {
                final JFileChooser fc = new JFileChooser();
                fc.setMultiSelectionEnabled(true);
                fc.showOpenDialog(null);
                return fc.getSelectedFiles();
            });

            SwingUtilities.invokeAndWait(task);

            final File[] files = task.get();
            final String[] fileNames = new String[files.length];
            for (int i = 0; i < files.length; i++) {
                fileNames[i] = "file://" + files[i].getAbsolutePath();
            }
            return fileNames;
        } catch (InterruptedException | InvocationTargetException | ExecutionException ex) {
            throw new AssinareError(ex);
        }
    }

}
