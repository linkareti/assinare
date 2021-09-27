package com.linkare.assinare.commons.ui;

import java.awt.Component;
import java.awt.Font;
import java.awt.Window;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.SwingUtilities;

import at.gv.egiz.smcc.CancelledException;
import at.gv.egiz.smcc.PinInfo;
import at.gv.egiz.smcc.pin.gui.PINGUI;

/**
 *
 * @author Ricardo Vaz - Linkare TI
 */
public class AssinarePINGUI implements PINGUI {

    private SignatureStageListener contextWorker;
    private char[] pinCache;
    private int nrDocs2Sign;
    private boolean firstTryForDocument = false;

    public AssinarePINGUI(int nrDocs2Sign) {
        this.nrDocs2Sign = nrDocs2Sign;
    }

    public AssinarePINGUI(SignatureStageListener contextWorker, int nrDocs2Sign) {
        this.contextWorker = contextWorker;
        this.nrDocs2Sign = nrDocs2Sign;
    }

    @Override
    public void allKeysCleared() {
        // no-op
    }

    @Override
    public void correctionButtonPressed() {
        // no-op
    }

    @Override
    public void enterPIN(PinInfo spec, int retries) throws CancelledException,
            InterruptedException {
        if (contextWorker != null) {
            contextWorker.publicPublish(SignatureStage.WAITING_ON_PINPAD);
        }
    }

    @Override
    public void enterPINDirect(PinInfo spec, int retries)
            throws CancelledException, InterruptedException {
        if (contextWorker != null) {
            contextWorker.publicPublish(SignatureStage.WAITING_ON_PINPAD);
        }
    }

    @Override
    public void validKeyPressed() {
        // no-op
    }

    @Override
    public char[] providePIN(final PinInfo pinSpec, final int retries)
            throws CancelledException, InterruptedException {
        FutureTask<char[]> task = new FutureTask<>(
                () -> providePINinEDT(pinSpec, retries)
        );

        // only attempt to use cache on first try
        if (pinCache != null && firstTryForDocument) {
            firstTryForDocument = false;
            /*
             This will prevent any programming errors from completely exausting
             the PIN retries. It might fail if a card has a maximum number of tries
             equal to 1 or 2 and reports such on the first try.
             */
            if (retries == 2 || retries == 1) {
                throw new CancelledException("Cache do PIN utilizada indevidamente.");
            }
            return pinCache.clone();
        } else {
            firstTryForDocument = false;
            clearCache();
            try {
                SwingUtilities.invokeAndWait(task);

                return task.get();
            } catch (InvocationTargetException | ExecutionException ex) {
                unwrapException(ex);
                return null;
            }
        }
    }

    private void unwrapException(Exception ex) throws CancelledException, InterruptedException {
        Throwable cause = ex.getCause();
        if (cause instanceof CancelledException) {
            throw (CancelledException) cause;
        } else if (cause instanceof InterruptedException) {
            throw (InterruptedException) cause;
        } else if (ex instanceof RuntimeException) {
            throw (RuntimeException) ex;
        } else {
            throw new RuntimeException(ex);
        }
    }

    private char[] providePINinEDT(PinInfo pinSpec, int retries) throws CancelledException {
        JPanel pinRequestPanel = new JPanel();
        pinRequestPanel.setLayout(new BoxLayout(pinRequestPanel, BoxLayout.Y_AXIS));
        JLabel pinNameLabel = new JLabel("Introduza o seu código " + pinSpec.getLocalizedName() + ".");
        JLabel retriesLabel = new JLabel();
        JLabel badPinLabel = new JLabel("Formato do PIN inválido.");
        badPinLabel.setFont(badPinLabel.getFont().deriveFont(badPinLabel.getFont().getStyle() | Font.BOLD));
        final JPasswordField pass = new JPasswordField(pinSpec.getMaxLength());

        pinRequestPanel.add(pinNameLabel);
        pinNameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        if (retries == PinInfo.UNKNOWN_RETRIES) {
            retriesLabel.setText("Número de tentativas desconhecido.");
        } else {
            retriesLabel.setText("Restam " + retries + " tentativas.");
        }
        pinRequestPanel.add(retriesLabel);
        retriesLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        pinRequestPanel.add(pass);
        pass.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel pinSavePanel = new JPanel();
        JLabel pinSaveLabel = new JLabel();
        JCheckBox pinSaveChkBox = new JCheckBox();

        Object[] dialogContents;
        if (nrDocs2Sign > 0) {
            pinSavePanel.add(pinSaveLabel);
            if (nrDocs2Sign > 1) {
                pinSaveLabel.setText("Usar Pin para assinar estes " + nrDocs2Sign + " documentos?");
                pinSaveChkBox.setSelected(true);
                pinSavePanel.add(pinSaveChkBox);
            } else {
                pinSaveLabel.setText("para assinar este documento.");
            }

            dialogContents = new Object[]{pinRequestPanel, pinSavePanel};
        } else {
            dialogContents = new Object[]{pinRequestPanel};
        }

        boolean badPinBefore = false;
        while (true) {
            JOptionPane optionPane = new JOptionPane(dialogContents, JOptionPane.QUESTION_MESSAGE, JOptionPane.DEFAULT_OPTION) {
                @Override
                public void selectInitialValue() {
                    pass.requestFocusInWindow();
                }
            };

            JDialog dlg;
            if (contextWorker instanceof Window) {
                dlg = optionPane.createDialog((Window) contextWorker, "Confirmação");
            } else {
                dlg = optionPane.createDialog("Confirmação");
            }
            dlg.setIconImage(new ImageIcon(AssinarePINGUI.class.getResource("/icons/assinareIconHeader.png")).getImage());
            dlg.setVisible(true);
            dlg.dispose();

            Object dialogOuput = optionPane.getValue();
            if (dialogOuput != null && dialogOuput.equals(JOptionPane.OK_OPTION)) {
                char[] pin = pass.getPassword();

                if (pin != null && pin.length >= pinSpec.getMinLength() && pin.length <= pinSpec.getMaxLength()) {
                    String regexPattern = pinSpec.getRegexpPattern();
                    boolean validChars = true;
                    for (char c : pin) {
                        if (!Character.toString(c).matches(regexPattern)) {
                            validChars = false;
                            break;
                        }
                    }
                    if (validChars) {
                        // make sure the checkbox is both selected and contained in the OptionPane
                        if (pinSaveChkBox.isSelected() && pinSavePanel.isAncestorOf(pinSaveChkBox)) {
                            pinCache = pin;
                        }
                        return pin;
                    }
                }

                if (!badPinBefore) {
                    pinRequestPanel.add(badPinLabel);
                    badPinLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
                    badPinBefore = true;
                }
            } else {
                throw new CancelledException("Operação cancelada pelo utilizador.");
            }
        }
    }

    public SignatureStageListener getContextWorker() {
        return contextWorker;
    }

    public void setContextWorker(SignatureStageListener contextWorker) {
        this.contextWorker = contextWorker;
    }

    public void clearCache() {
        pinCache = null;
    }

    public void documentDone() {
        firstTryForDocument = true;
        nrDocs2Sign--;
    }

}
