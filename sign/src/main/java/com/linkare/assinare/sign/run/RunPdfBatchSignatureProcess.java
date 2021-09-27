package com.linkare.assinare.sign.run;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.linkare.assinare.commons.AssinareError;
import com.linkare.assinare.commons.AssinareException;
import com.linkare.assinare.commons.utils.MOCCAUtils;
import com.linkare.assinare.sign.fileprovider.LocalFileService;
import com.linkare.assinare.sign.pdf.PDFSignatureFields;
import com.linkare.assinare.sign.pdf.PdfBatchSignatureProcess;
import com.linkare.assinare.sign.swing.SwingHelper;

/**
 *
 * @author bnazare
 */
public class RunPdfBatchSignatureProcess {

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
            * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html
         */
        SwingHelper.setupLookAndFeel();
        //</editor-fold>

        /* Create and display the dialog */
        final String fileName = "file:///home/bnazare/assinare/original/doc.pdf";

        try {
            MOCCAUtils.loadPCSCLibrary();
        } catch (AssinareError ex) {
            Logger.getLogger(RunPdfSignatureViewer.class.getName()).log(Level.SEVERE, null, ex);
        }

        PDFSignatureFields sigFields = new PDFSignatureFields();
//        try {
//            sigFields = new PDFSignatureFields("foo", "bar", "bang", 1d, 0d, 1, 500, 100,
//                    SignatureRenderingMode.LOGO_CHOOSED_BY_USER, null, null, true,
//                    new URL("http://test.assinare.eu/assinare-web/id/img/silhouette.jpg"), UiMode.SIMPLE);
//        } catch (MalformedURLException ex) {
//            Logger.getLogger(RunPdfBatchSignatureProcess.class.getName()).log(Level.SEVERE, null, ex);
//        }

        String[] docList = new String[]{fileName};
        LocalFileService fileService = new LocalFileService();
        PdfBatchSignatureProcess sigProcess = new PdfBatchSignatureProcess(fileService);

        try {
            sigProcess.doSignature(docList, sigFields);
        } catch (AssinareError | AssinareException | RuntimeException ex) {
            Logger.getLogger(RunPdfBatchSignatureProcess.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
