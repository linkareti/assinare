package com.linkare.assinare.id;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import com.linkare.assinare.commons.AssinareError;
import com.linkare.assinare.commons.AssinareException;
import com.linkare.assinare.commons.ui.AssinarePINGUI;
import com.linkare.assinare.commons.utils.MOCCAUtils;
import com.linkare.assinare.commons.utils.ManifestUtils;

import at.gv.egiz.smcc.CardDataSet;
import at.gv.egiz.smcc.SignatureCard;
import at.gv.egiz.smcc.SignatureCardException;
import iaik.me.utils.Base64;

/**
 *
 * @author Ricardo Vaz - Linkare TI
 */
public final class AssinareId {

    private static final Logger LOG = Logger.getLogger(AssinareId.class.getName());

    private static final String CARD_CONNECTION_ERROR_MSG = "Ocorreu um erro de comunicação com o cartão";

    public AssinareId() {
        init();
    }

    public void init() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
            LOG.log(Level.WARNING, "Não foi possível activar o LaF de sistema.", ex);
        }

        try {
            MOCCAUtils.loadPCSCLibrary();
        } catch (AssinareError ex) {
            throw new RuntimeException(ex);
        }

        LOG.info("AssinareIdMain initialized.");
    }

    public Map<String, ?> getCitizenData() throws AssinareException, AssinareError {
        validatePreConditions();
        try {
            SignatureCard signatureCard = CardUtils.getDataProvidingCard();

            return signatureCard
                    .getCardData(null, new AssinarePINGUI(0), CardDataSet.HOLDER_DATA)
                    .get(CardDataSet.HOLDER_DATA);
        } catch (SignatureCardException | InterruptedException ex) {
            throw new AssinareException(CARD_CONNECTION_ERROR_MSG, ex);
        }
    }

    public Map<String, ?> getCitizenAddress() throws AssinareException, AssinareError {
        validatePreConditions();
        try {
            SignatureCard signatureCard = CardUtils.getDataProvidingCard();

            return signatureCard
                    .getCardData(null, new AssinarePINGUI(0), CardDataSet.HOLDER_ADDRESS)
                    .get(CardDataSet.HOLDER_ADDRESS);
        } catch (SignatureCardException | InterruptedException ex) {
            throw new AssinareException(CARD_CONNECTION_ERROR_MSG, ex);
        }
    }

    public Map<String, ?> getCitizenPicture() throws AssinareException, AssinareError {
        validatePreConditions();
        try {
            SignatureCard signatureCard = CardUtils.getDataProvidingCard();
            Map<String, ?> data = signatureCard
                    .getCardData(null, new AssinarePINGUI(0), CardDataSet.HOLDER_PICTURE)
                    .get(CardDataSet.HOLDER_PICTURE);

            byte[] picData = (byte[]) data.get("picture");
            String output = Base64.encode(picData).replace("\n", "").replace("\r", "");

            Map<String, Object> retVal = new HashMap<>();
            retVal.put("picture", output);

            return retVal;
        } catch (SignatureCardException | InterruptedException ex) {
            throw new AssinareException(CARD_CONNECTION_ERROR_MSG, ex);
        }
    }

    private static boolean isTestDeploy() {
        return ManifestUtils.isCodebaseWildcard();
    }

    private void validatePreConditions() {
        if (isTestDeploy()) {
            try {
                SwingUtilities.invokeAndWait(() -> {
                    JOptionPane optionPane = new JOptionPane(
                            "<html>Obrigado por utilizar a versão de testes/avalição da AssinareID applet.<br/>"
                            + "Tenha em atenção que esta versão não oferece as mesmas garantias que uma versão de produção.<br/>"
                            + "Para saber mais visite <a href=\"http://www.assinare.eu\">http://www.assinare.eu</a> (by Linkare).</html>",
                            JOptionPane.WARNING_MESSAGE
                    );

                    JDialog dlg = optionPane.createDialog("AssinareID");
                    dlg.setIconImage(new ImageIcon(AssinareId.class.getResource("/icons/assinareIconHeader.png")).getImage());
                    dlg.setVisible(true);
                    dlg.dispose();
                });
            } catch (InterruptedException | InvocationTargetException ex) {
                // This is not a terribly important functionality so just log the exception
                LOG.log(Level.SEVERE, null, ex);
            }
        }
    }

}
