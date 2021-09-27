package com.linkare.assinare.sign.run;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.RunnableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.SwingUtilities;

import com.linkare.assinare.sign.KeySupplier;
import com.linkare.assinare.sign.SigningKey;
import com.linkare.assinare.sign.model.AssinareDocument;
import com.linkare.assinare.sign.model.InMemoryDocument;
import com.linkare.assinare.sign.pdf.PDFSignatureFields;
import com.linkare.assinare.sign.pdf.PdfSignatureViewer;
import com.linkare.assinare.sign.swing.SwingHelper;

/**
 *
 * @author bnazare
 */
public class RunPdfSignatureViewer {

    /**
     * @param args the command line arguments
     * @throws java.net.MalformedURLException
     */
    public static void main(String args[]) throws MalformedURLException {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        SwingHelper.setupLookAndFeel();
        //</editor-fold>

        /* Create and display the dialog */
        final String fileName = "file:///home/bnazare/assinare/original/doc.pdf";

        PDFSignatureFields sigFields = new PDFSignatureFields();
//        PDFSignatureFields sigFields = new PDFSignatureFields("foo", "bar", "bang", 1d, 0d, 1, 500, 100,
//                SignatureRenderingMode.LOGO_CHOOSED_BY_USER, null, true,
//                new URL("http://test.assinare.eu/assinare-web/id/img/silhouette.jpg"), UiMode.SIMPLE);

        RunnableFuture<PdfSignatureViewer> task = new FutureTask<>(() -> {
            PdfSignatureViewer dialog;
            KeySupplier keySupplier = new KeySupplier() {
                @Override
                public List<SigningKey> getKeys() {
                    return Collections.singletonList(null);
                }

                @Override
                public void close() throws Exception {
                    // NOOP
                }
            };
            dialog = new PdfSignatureViewer(false, sigFields, keySupplier);
            dialog.setLocationRelativeTo(null);
            dialog.setVisible(true);

            return dialog;
        });

        PdfSignatureViewer dialog = null;
        try {
            SwingUtilities.invokeAndWait(task);

            dialog = task.get();

            URL url = new URL(fileName);
            final AssinareDocument doc = new InMemoryDocument(fileName, url.openStream());
            dialog.dataReady(doc);

            System.out.println("Result: " + dialog.getSignatureFields().get());
        } catch (IOException | RuntimeException | InterruptedException | InvocationTargetException | ExecutionException ex) {
            Logger.getLogger(RunPdfSignatureViewer.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            if (dialog != null) {
                dialog.signatureDone();
                dialog.dispose();
            }
        }
    }

}
