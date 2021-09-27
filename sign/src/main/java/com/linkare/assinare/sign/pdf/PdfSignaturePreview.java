package com.linkare.assinare.sign.pdf;

import static com.linkare.assinare.sign.AssinareConstants.PDF_DEFAULT_PAGE;
import static com.linkare.assinare.sign.AssinareConstants.SIGNATURE_DEFAULT_HEIGHT;
import static com.linkare.assinare.sign.AssinareConstants.SIGNATURE_DEFAULT_WIDTH;
import static com.linkare.assinare.sign.AssinareConstants.SIGNATURE_DEFAULT_X_PCT;
import static com.linkare.assinare.sign.AssinareConstants.SIGNATURE_DEFAULT_Y_PCT;

import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.Point;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JLabel;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.pushingpixels.substance.api.SubstanceLookAndFeel;

import com.linkare.assinare.sign.AssinareConstants;
import com.linkare.assinare.sign.helpers.RectangleHelper;
import com.linkare.assinare.sign.model.AssinareDocument;
import com.linkare.assinare.sign.swing.ScalingLayoutManager;
import com.linkare.assinare.sign.swing.SwingHelper;

/**
 * Show a preview of a given page of a given PDF document. Shows an fallback
 * screen when a document is not provided.<br>
 * TODO: split into two panels, one for PDF preview and another for the
 * fallback.
 *
 * @author bnazare
 */
public class PdfSignaturePreview extends javax.swing.JPanel implements Closeable {

    private static final long serialVersionUID = -8332735049014292347L;

    private static final Logger LOG = Logger.getLogger(PdfSignaturePreview.class.getName());

    private static final Dimension A4_SIZE = new Dimension(595, 841);

    private static final PDFSignatureFields DEFAULT_SIG_OPTS = new PDFSignatureFields(
            SIGNATURE_DEFAULT_X_PCT, SIGNATURE_DEFAULT_Y_PCT,
            PDF_DEFAULT_PAGE,
            SIGNATURE_DEFAULT_WIDTH, SIGNATURE_DEFAULT_HEIGHT);

    private double percentX;
    private double percentY;
    private int signatureWidth;
    private int signatureHeight;
    private double percentWidth;
    private double percentHeight;
    private Point originalPosition = null;
    private PDDocument pdfFile;
    private PDPage page;
    private int curPage;
    private final List<SignatureInfo> signaturesInfo = new ArrayList<>();
    private final Map<Integer, JLabel> signatureLabels;
    private AssinareDocument doc;

    /**
     * Creates new form PdfSignaturePreview
     */
    public PdfSignaturePreview() {
        signatureLabels = new HashMap<>();
        putClientProperty(SubstanceLookAndFeel.COLORIZATION_FACTOR, 1d);
        initComponents();
    }

    public void init(AssinareDocument doc, final PDFSignatureFields sigPosition) {
        this.doc = doc;

        ((ScalingLayoutManager) getLayout()).setConstraints(scaledPanel, A4_SIZE);
        ((CardLayout) scaledPanel.getLayout()).last(scaledPanel);
    }

    public void init(AssinareDocument doc, final PDFSignatureFields sigPosition, ParsedPdfInfo parsedPdfInfo) {
        PDFSignatureFields mergedSigPosition = DEFAULT_SIG_OPTS.merge(sigPosition);
        percentX = mergedSigPosition.getPercentX();
        percentY = mergedSigPosition.getPercentY();
        signatureWidth = mergedSigPosition.getWidth();
        signatureHeight = mergedSigPosition.getHeight();

        try (InputStream docStream = doc.openInputStream()) {
            pdfFile = PDDocument.load(docStream);
            pdfPreview.setDoc(pdfFile);
            changeToPage(mergedSigPosition.getPageNumber());
        } catch (IOException ex) {
            Logger.getLogger(PdfSignatureViewer.class.getName()).log(Level.SEVERE, null, ex);
        }

        addPreviousSignatures(parsedPdfInfo);
        resizeSignatures();
    }

    private void updatePercentages() {
        Point currentLocation = lblSignature.getLocation();
        Logger.getLogger(PdfSignatureViewer.class.getName()).log(Level.FINEST, "Current Location:{0}", currentLocation);
        Dimension lblDimension = lblSignature.getSize();
        Logger.getLogger(PdfSignatureViewer.class.getName()).log(Level.FINEST, "Signature dimension:{0}", lblDimension);
        Dimension enclosingPanelDimension = pdfPreview.getSize();
        Logger.getLogger(PdfSignatureViewer.class.getName()).log(Level.FINEST, "PDF dimension:{0}", enclosingPanelDimension);
        //What matters is the center of lblDimension relative to the enclosing Panel, discounted of the lblDimension size
        enclosingPanelDimension = new Dimension(enclosingPanelDimension.width - lblDimension.width, enclosingPanelDimension.height - lblDimension.height);
        Logger.getLogger(PdfSignatureViewer.class.getName()).log(Level.FINEST, "PDF dimension (without label):{0}", enclosingPanelDimension);
        percentX = currentLocation.getX() / enclosingPanelDimension.getWidth();
        percentY = currentLocation.getY() / enclosingPanelDimension.getHeight();
        Logger.getLogger(PdfSignatureViewer.class.getName()).log(Level.FINER, "Percent x,y:{0};{1}", new Object[]{percentX, percentY});
    }

    private void showPopupPosition() {
        popupPosition.show(lblSignature, lblSignature.getWidth() / 2, lblSignature.getHeight() / 2);
    }

    private void redrawPdfPreview() {
        final PDRectangle cropBox = RectangleHelper.getRotatedCropBox(page);

        ((ScalingLayoutManager) getLayout()).setConstraints(scaledPanel, new Dimension((int) cropBox.getWidth(), (int) cropBox.getHeight()));
        pdfPreview.setPageNum(curPage);
        revalidate();
        repaint();
    }

    private void resizeSignatures() {
        resizeLblSignature();

        if (signaturesInfo != null) {
            signaturesInfo.forEach((si) -> {
                resizeSignature(si, signatureLabels.get(si.getRevision()));
            });
        }
    }

    //refactor resizeLblSignature() and resizeSignature(SignatureInfo si) to be only one method
    private void resizeLblSignature() {
        if (page != null) {
            int newWidth = (int) (pdfPreview.getWidth() * percentWidth);
            int newHeight = (int) (pdfPreview.getHeight() * percentHeight);
            int newX = (int) ((pdfPreview.getWidth() - newWidth) * percentX);
            int newY = (int) ((pdfPreview.getHeight() - newHeight) * percentY);
            lblSignature.setBounds(newX, newY, newWidth, newHeight);
            lblSignature.setFont(lblSignature.getFont().deriveFont(newHeight / (2 * 1.618f)));
        }
    }

    private void resizeSignature(SignatureInfo si, JLabel label) {
        int newWidth = (int) (pdfPreview.getWidth() * si.getPercentWidth());
        int newHeight = (int) (pdfPreview.getHeight() * si.getPercentHeight());
        int newX = (int) (pdfPreview.getWidth() * si.getPercentX());
        int newY = (int) (pdfPreview.getHeight() * si.getPercentY());
        if (label != null) {
            label.setBounds(newX, newY, newWidth, newHeight);
        }
    }

    public void changeToPage(int pageNr) {
        curPage = pageNr - 1;
        page = pdfFile.getPage(curPage);

        final PDRectangle cropBox = RectangleHelper.getRotatedCropBox(page);

        percentWidth = signatureWidth / cropBox.getWidth();
        percentHeight = signatureHeight / cropBox.getHeight();

        redrawPdfPreview();

        toggleExistingSignatures();
    }

    public int getPageCount() {
        return pdfFile.getNumberOfPages();
    }

    public int getCurrentPage() {
        return curPage + 1;
    }

    public double getPercentX() {
        return percentX;
    }

    public double getPercentY() {
        return percentY;
    }

    public int getSignatureWidth() {
        return signatureWidth;
    }

    public int getSignatureHeight() {
        return signatureHeight;
    }

    public boolean isShowSigTarget() {
        return lblSignature.isVisible();
    }

    public void setShowSigTarget(boolean showSigTarget) {
        lblSignature.setVisible(showSigTarget);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        popupPosition = new javax.swing.JPopupMenu();
        centerMenuItem = new javax.swing.JMenuItem();
        topLeftMenuItem = new javax.swing.JMenuItem();
        topRightMenuItem = new javax.swing.JMenuItem();
        bottomLeftMenuItem = new javax.swing.JMenuItem();
        bottomRightMenuItem = new javax.swing.JMenuItem();
        scaledPanel = new javax.swing.JPanel();
        pdfPreview = new com.linkare.assinare.sign.swing.PdfPreviewPanel();
        lblSignature = new javax.swing.JLabel();
        previewUnavailablePanel = new javax.swing.JPanel();
        previewUnavailableLbl = new javax.swing.JLabel();
        viewDocumentBtn = new javax.swing.JButton();

        centerMenuItem.setText("Center");
        centerMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                centerMenuItemActionPerformed(evt);
            }
        });
        popupPosition.add(centerMenuItem);

        topLeftMenuItem.setText("Top Left");
        topLeftMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                topLeftMenuItemActionPerformed(evt);
            }
        });
        popupPosition.add(topLeftMenuItem);

        topRightMenuItem.setText("Top Right");
        topRightMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                topRightMenuItemActionPerformed(evt);
            }
        });
        popupPosition.add(topRightMenuItem);

        bottomLeftMenuItem.setText("Bottom Left");
        bottomLeftMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bottomLeftMenuItemActionPerformed(evt);
            }
        });
        popupPosition.add(bottomLeftMenuItem);

        bottomRightMenuItem.setText("Bottom Right");
        bottomRightMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bottomRightMenuItemActionPerformed(evt);
            }
        });
        popupPosition.add(bottomRightMenuItem);

        setBackground(java.awt.Color.lightGray);
        setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.LOWERED));
        setLayout(new com.linkare.assinare.sign.swing.ScalingLayoutManager());

        scaledPanel.setName("scaledPanel"); // NOI18N
        scaledPanel.setLayout(new java.awt.CardLayout());

        pdfPreview.setName("pdfPreview"); // NOI18N
        pdfPreview.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                pdfPreviewMouseClicked(evt);
            }
        });
        pdfPreview.addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentResized(java.awt.event.ComponentEvent evt) {
                pdfPreviewComponentResized(evt);
            }
        });
        pdfPreview.setLayout(null);

        lblSignature.setBackground(new java.awt.Color(100, 100, 100));
        lblSignature.setFont(lblSignature.getFont().deriveFont(lblSignature.getFont().getSize()-1f));
        lblSignature.setForeground(new java.awt.Color(255, 255, 255));
        lblSignature.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("com/linkare/assinare/resourceBundle/Language"); // NOI18N
        lblSignature.setText(bundle.getString("assinare.label.signature")); // NOI18N
        lblSignature.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(153, 153, 153), 1, true));
        lblSignature.setName("lblSignature"); // NOI18N
        lblSignature.setOpaque(true);
        lblSignature.setPreferredSize(new java.awt.Dimension(75, 50));
        java.awt.Color c = lblSignature.getBackground();
        lblSignature.setBackground(new java.awt.Color(c.getRed(), c.getGreen(), c.getBlue(), 200));
        lblSignature.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            public void mouseDragged(java.awt.event.MouseEvent evt) {
                lblSignatureMouseDragged(evt);
            }
        });
        lblSignature.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                lblSignatureMousePressed(evt);
            }
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                lblSignatureMouseReleased(evt);
            }
        });
        pdfPreview.add(lblSignature);
        lblSignature.setBounds(75, 360, 130, 50);

        scaledPanel.add(pdfPreview, "card2");

        previewUnavailablePanel.setName("previewUnavailablePanel"); // NOI18N
        previewUnavailablePanel.setLayout(new javax.swing.BoxLayout(previewUnavailablePanel, javax.swing.BoxLayout.Y_AXIS));

        previewUnavailableLbl.setFont(new java.awt.Font("DejaVu Sans", 0, 30)); // NOI18N
        previewUnavailableLbl.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        previewUnavailableLbl.setText(bundle.getString("assinare.label.noPreviewAvailable")); // NOI18N
        previewUnavailableLbl.setVerticalAlignment(javax.swing.SwingConstants.TOP);
        previewUnavailableLbl.setAlignmentX(0.5F);
        previewUnavailablePanel.add(previewUnavailableLbl);

        viewDocumentBtn.setBackground(new java.awt.Color(34, 255, 0));
        viewDocumentBtn.setFont(viewDocumentBtn.getFont().deriveFont(viewDocumentBtn.getFont().getStyle() | java.awt.Font.BOLD));
        viewDocumentBtn.setText(bundle.getString("assinare.label.open")); // NOI18N
        viewDocumentBtn.setAlignmentX(0.5F);
        viewDocumentBtn.setMaximumSize(new java.awt.Dimension(130, 52));
        viewDocumentBtn.putClientProperty(SubstanceLookAndFeel.COLORIZATION_FACTOR, 0.75d);
        viewDocumentBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                viewDocumentBtnActionPerformed(evt);
            }
        });
        previewUnavailablePanel.add(viewDocumentBtn);

        scaledPanel.add(previewUnavailablePanel, "card3");

        add(scaledPanel);
    }// </editor-fold>//GEN-END:initComponents

    private void lblSignatureMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lblSignatureMousePressed
        if (evt.isPopupTrigger()) {
            showPopupPosition();
        } else {
            originalPosition = evt.getPoint();
        }
    }//GEN-LAST:event_lblSignatureMousePressed

    private void lblSignatureMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lblSignatureMouseReleased
        if (evt.isPopupTrigger()) {
            showPopupPosition();
        } else {
            updatePercentages();
            originalPosition = null;
        }
    }//GEN-LAST:event_lblSignatureMouseReleased

    private void lblSignatureMouseDragged(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lblSignatureMouseDragged
        if (!evt.isPopupTrigger()) {
            Point currentPosition = evt.getPoint();
            Logger.getLogger(PdfSignatureViewer.class.getName()).log(Level.FINEST, "Current Position={0}", currentPosition);
            double deltaX = currentPosition.getX() - originalPosition.getX();
            Logger.getLogger(PdfSignatureViewer.class.getName()).log(Level.FINEST, "DeltaX={0}", deltaX);
            double deltaY = currentPosition.getY() - originalPosition.getY();
            Logger.getLogger(PdfSignatureViewer.class.getName()).log(Level.FINEST, "DeltaY={0}", deltaY);
            Point currentPositionInPanel = lblSignature.getLocation();
            Logger.getLogger(PdfSignatureViewer.class.getName()).log(Level.FINEST, "Current PositionInPanel={0}", currentPositionInPanel);

            moveLblSignature((int) (currentPositionInPanel.getX() + deltaX), (int) (currentPositionInPanel.getY() + deltaY));
            Logger.getLogger(PdfSignatureViewer.class.getName()).log(Level.FINEST, "lblSignature location after move:{0}", lblSignature.getLocation());
        }
    }//GEN-LAST:event_lblSignatureMouseDragged

    private void centerMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_centerMenuItemActionPerformed
        popupPosition.setVisible(false);
        moveLblSignature(pdfPreview.getWidth() / 2 - lblSignature.getWidth() / 2, pdfPreview.getHeight() / 2 - lblSignature.getHeight() / 2);
    }//GEN-LAST:event_centerMenuItemActionPerformed

    private void topLeftMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_topLeftMenuItemActionPerformed
        popupPosition.setVisible(false);
        moveLblSignature(0, 0);
    }//GEN-LAST:event_topLeftMenuItemActionPerformed

    private void topRightMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_topRightMenuItemActionPerformed
        popupPosition.setVisible(false);
        moveLblSignature(pdfPreview.getWidth() - lblSignature.getWidth(), 0);
    }//GEN-LAST:event_topRightMenuItemActionPerformed

    private void bottomLeftMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bottomLeftMenuItemActionPerformed
        popupPosition.setVisible(false);
        moveLblSignature(0, pdfPreview.getHeight() - lblSignature.getHeight());
    }//GEN-LAST:event_bottomLeftMenuItemActionPerformed

    private void bottomRightMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bottomRightMenuItemActionPerformed
        popupPosition.setVisible(false);
        moveLblSignature(pdfPreview.getWidth() - lblSignature.getWidth(), pdfPreview.getHeight() - lblSignature.getHeight());
    }//GEN-LAST:event_bottomRightMenuItemActionPerformed

    private void pdfPreviewMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_pdfPreviewMouseClicked
        if (SwingUtilities.isLeftMouseButton(evt)) {
            moveLblSignature(evt.getX() - lblSignature.getWidth() / 2, evt.getY() - lblSignature.getHeight() / 2);
        }
    }//GEN-LAST:event_pdfPreviewMouseClicked

    private void viewDocumentBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_viewDocumentBtnActionPerformed
        try (InputStream docStream = doc.openInputStream()) {
            SwingHelper.openPdfWithSOApplication(docStream);
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_viewDocumentBtnActionPerformed

    private void pdfPreviewComponentResized(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_pdfPreviewComponentResized
        previewUnavailableLbl.setBounds(0, 0, pdfPreview.getBounds().width, pdfPreview.getBounds().height);
        resizeSignatures();
    }//GEN-LAST:event_pdfPreviewComponentResized

    private void moveLblSignature(int x, int y) {
        int normX, normY;
        if (x < 0) {
            normX = 0;
        } else if (x > pdfPreview.getWidth() - lblSignature.getWidth()) {
            normX = pdfPreview.getWidth() - lblSignature.getWidth();
        } else {
            normX = x;
        }

        if (y < 0) {
            normY = 0;
        } else if (y > pdfPreview.getHeight() - lblSignature.getHeight()) {
            normY = pdfPreview.getHeight() - lblSignature.getHeight();
        } else {
            normY = y;
        }

        lblSignature.setLocation(normX, normY);
        updatePercentages();
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JMenuItem bottomLeftMenuItem;
    private javax.swing.JMenuItem bottomRightMenuItem;
    private javax.swing.JMenuItem centerMenuItem;
    private javax.swing.JLabel lblSignature;
    private com.linkare.assinare.sign.swing.PdfPreviewPanel pdfPreview;
    private javax.swing.JPopupMenu popupPosition;
    private javax.swing.JLabel previewUnavailableLbl;
    private javax.swing.JPanel previewUnavailablePanel;
    private javax.swing.JPanel scaledPanel;
    private javax.swing.JMenuItem topLeftMenuItem;
    private javax.swing.JMenuItem topRightMenuItem;
    private javax.swing.JButton viewDocumentBtn;
    // End of variables declaration//GEN-END:variables

    private void addPreviousSignatures(ParsedPdfInfo parsedPdfInfo) {
        signaturesInfo.addAll(parsedPdfInfo.getExistingSignatures());

        signaturesInfo.forEach((si) -> {
            JLabel l = createJLabel(si);
            /*
            The indexes in the start are as follows:
            0 -> lblSignature
            1 -> previewImage
            The magical '1' below puts the new labels over the pdf render
            but under the draggable signature mark.
             */
            pdfPreview.add(l, 1);
            signatureLabels.put(si.getRevision(), l);
        });
    }

    private void toggleExistingSignatures() {
        signaturesInfo.forEach((si) -> {
            signatureLabels.get(si.getRevision()).setVisible(getCurrentPage() == si.getPage());
        });
    }

    private JLabel createJLabel(SignatureInfo sigInfo) {
        JLabel signatureLbl = new javax.swing.JLabel();

        if (sigInfo.isValid()) {
            signatureLbl.setBackground(AssinareConstants.VALID_EXISTING_SIGNATURES_COLOR);
        } else {
            signatureLbl.setBackground(AssinareConstants.INVALID_EXISTING_SIGNATURES_COLOR);
        }

        signatureLbl.setForeground(new java.awt.Color(255, 255, 255));
        signatureLbl.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        signatureLbl.setFont(signatureLbl.getFont());
        //signatureLbl.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(153, 153, 153), 1, true));
        signatureLbl.setOpaque(true);

        resizeSignature(sigInfo, signatureLbl);
        signatureLbl.setText(String.valueOf(sigInfo.getRevision()));
        signatureLbl.setVisible(page.getStructParents() == sigInfo.getPage());

        ToolTipManager ttm = ToolTipManager.sharedInstance();
        ttm.setInitialDelay(100);
        ttm.setDismissDelay(2 * 60 * 1000);

        Date sigInfoDate = sigInfo.getDate();
        String signDate;
        if (sigInfoDate != null) {
            signDate = AssinareConstants.DEFAULT_DATE_FORMATTER.format(sigInfoDate);
        } else {
            signDate = AssinareConstants.NOT_AVAILABLE;
        }

        // String.valueOf used to prevent NPEs
        String toolTipText = String.format(
                "<html>%s<br/>  %s, %s<br/>  Razão: %s<br/>  Contacto: %s</html>",
                sigInfo.getSignerName(), String.valueOf(sigInfo.getLocation()),
                signDate, String.valueOf(sigInfo.getReason()),
                String.valueOf(sigInfo.getContactDetails()));

        signatureLbl.setToolTipText(toolTipText);

        return signatureLbl;
    }

    @Override
    public void close() {
        pdfPreview = null;
        if (pdfFile != null) {
            try {
                pdfFile.close();
            } catch (IOException ex) {
                LOG.log(Level.WARNING, null, ex);
            }

            pdfFile = null;
        }
    }

}
