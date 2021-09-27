package com.linkare.assinare.sign.run;

import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.RunnableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import com.linkare.assinare.sign.KeySupplier;
import com.linkare.assinare.sign.SigningKey;
import com.linkare.assinare.sign.asic.ASiCSignatureFields;
import com.linkare.assinare.sign.asic.ContainerSignatureViewer;

/**
 *
 * @author bnazare
 */
public class RunContainerSignatureViewer {

    private static final Logger LOG = Logger.getLogger(RunContainerSignatureViewer.class.getName());

    public static void main(String[] args) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html
         */
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the dialog */
        ASiCSignatureFields sigFields = new ASiCSignatureFields();
//        sigFields = new ASiCSignatureFields("foo", "bar", "authorship",
//                CommitmentType.PROOF_OF_RECEIPT, true);

        RunnableFuture<ContainerSignatureViewer> task = new FutureTask<>(() -> {
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
            ContainerSignatureViewer dialog = new ContainerSignatureViewer(false, sigFields, keySupplier);
            dialog.setLocationRelativeTo(null);
            dialog.setVisible(true);

            return dialog;
        });

        ContainerSignatureViewer dialog = null;
        try {
            SwingUtilities.invokeAndWait(task);

            dialog = task.get();

            dialog.dataReady(Collections.EMPTY_LIST);

            System.out.println("Result: " + dialog.getSignatureFields().get());
        } catch (RuntimeException | InterruptedException | InvocationTargetException | ExecutionException ex) {
            Logger.getLogger(RunPdfSignatureViewer.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            if (dialog != null) {
                dialog.signatureDone();
                dialog.dispose();
            }
        }
    }

}
