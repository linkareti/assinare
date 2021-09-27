package com.linkare.assinare.sign.pdf;

import static com.linkare.assinare.sign.AssinareConstants.DEFAULT_DATE_FORMATTER;

import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.WindowEvent;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.ListCellRenderer;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import org.apache.commons.io.FilenameUtils;

import com.linkare.assinare.commons.ui.SignatureStage;
import com.linkare.assinare.sign.AssinareConstants;
import com.linkare.assinare.sign.DocumentParseException;
import com.linkare.assinare.sign.KeySupplier;
import com.linkare.assinare.sign.SignatureRenderingMode;
import com.linkare.assinare.sign.SignatureViewer;
import com.linkare.assinare.sign.SigningKey;
import com.linkare.assinare.sign.model.AssinareDocument;
import com.linkare.assinare.sign.pdf.dss.DssPdfParser;
import com.linkare.assinare.sign.swing.AboutDialog;
import com.linkare.assinare.sign.swing.SwingHelper;

/**
 *
 * @author jpereira
 */
public class PdfSignatureViewer extends javax.swing.JDialog implements SignatureViewer<AssinareDocument, PDFSignatureFields> {

    private static final long serialVersionUID = -8865895373583288140L;

    private static final Logger LOG = Logger.getLogger(PdfSignatureViewer.class.getName());

    private static final SignatureFieldInfo DEFAULT_BLANK_SIG_INFO = new SignatureFieldInfo(null, -1, -1, -1, -1, -1, -1);

    final NumberFormat format = DecimalFormat.getPercentInstance();
    private URL selectedLogoFile;

    private ParsedPdfInfo parsedPdfInfo;
    private List<SignatureInfo> signaturesInfo = new ArrayList<>();
    private List<SignatureFieldInfo> blankSignaturesInfo = new ArrayList<>();
    private boolean allSignaturesValid;

    private static final ResourceBundle BUNDLE = ResourceBundle.getBundle("com/linkare/assinare/resourceBundle/Language");

    private static final PDFSignatureFields DEFAULT_SIG_OPTS = new PDFSignatureFields(
            "www.linkare.com", BUNDLE.getString("assinare.label.place.inputTextDefault"),
            BUNDLE.getString("assinare.label.reason.inputTextDefault"),
            null, null, null, null, null,
            SignatureRenderingMode.PRE_DEFINED_LOGO, null, null, false);

    private final PDFSignatureFields baseSigOpts;
    private final KeySupplier signingKeysSupplier;
    private final CompletableFuture<PDFSignatureFields> sigFieldsFuture = new CompletableFuture<>();
    private final CompletableFuture<SigningKey> sigKeyFuture = new CompletableFuture<>();

    public PdfSignatureViewer(boolean modal, PDFSignatureFields sigOptions, KeySupplier signingKeysSupplier) {
        super((Window) null, modal ? JDialog.DEFAULT_MODALITY_TYPE : ModalityType.MODELESS);
        baseSigOpts = DEFAULT_SIG_OPTS.merge(sigOptions.sanitize());
        this.signingKeysSupplier = signingKeysSupplier;
        initComponents();
        if (baseSigOpts.isUserLogo()) {
            selectedLogoFile = baseSigOpts.getLogoFileURL();

            if (selectedLogoFile != null) {
                logoFileName.setText(FilenameUtils.getName(baseSigOpts.getLogoFileURL().toString()));
            }
        }

        setGlassPane(blockerPanel);
        getGlassPane().setVisible(true);
    }

    private void setComboBoxPageNrInitialValues(int pdfNrPages) {
        ArrayList<Integer> pageNumbers = new ArrayList<>(pdfNrPages);
        for (int i = 1; i <= pdfNrPages; i++) {
            pageNumbers.add(i);
        }
        DefaultComboBoxModel model = new DefaultComboBoxModel(pageNumbers.toArray());
        jComboBoxPageNr.setModel(model);
    }

    private void setDocumentSignaturesInfo() {
        if (signaturesInfo.isEmpty()) {
            jLabelSignsState.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/emptyState.png")));
            jPopupMenuSignsState.add("O documento não tem nenhuma assinatura.");
        } else {
            if (allSignaturesValid) {
                jLabelSignsState.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/validState.png")));
            } else {
                jLabelSignsState.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/invalidState.png")));
            }
            jLabelSignsState.setText(String.valueOf(signaturesInfo.size()));

            ActionListener actionListener = new PopupActionListener();
            for (SignatureInfo signInfo : signaturesInfo) {
                JMenuItem jm = createMenuItem(signInfo);
                //jm.setForeground(new Color(125, 125, 125));
                jm.addActionListener(actionListener);
                jPopupMenuSignsState.add(jm);
            }
        }
    }

    private static JMenuItem createMenuItem(SignatureInfo signInfo) {
        String signDate;
        if (signInfo.getDate() != null) {
            signDate = DEFAULT_DATE_FORMATTER.format(signInfo.getDate());
        } else {
            signDate = AssinareConstants.NOT_AVAILABLE;
        }

        String signReason;
        if (signInfo.getReason() == null) {
            signReason = AssinareConstants.NOT_AVAILABLE;
        } else {
            signReason = signInfo.getReason();
        }

        String itemText = String.format("<html>%d: %s<br/><small>%s - %s</small></html>",
                signInfo.getRevision(), signInfo.getSignerName(), signDate, signReason);

        return new JMenuItem(itemText);
    }

    // Define ActionListener
    class PopupActionListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent actionEvent) {
            String revision = actionEvent.getActionCommand().substring(6).split(":")[0];
            int index = Integer.parseInt(revision);
            int pageNr = signaturesInfo.get(index - 1).getPage();
            jComboBoxPageNr.setSelectedIndex(pageNr - 1);
        }
    }

    private void redrawPdfPreview(int pageNumber) {
        pdfPreview.changeToPage(pageNumber);
    }

    @Override
    public void dataReady(AssinareDocument doc) {
        boolean pdfLoaded = loadPDFInfo(doc);

        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater(() -> {
                if (pdfLoaded) {
                    pdfPreview.init(doc, baseSigOpts, parsedPdfInfo);
                    int pdfNrPages = pdfPreview.getPageCount();
                    jLabelTotalPags.setText(String.valueOf(pdfNrPages));
                    setComboBoxPageNrInitialValues(pdfNrPages);

                    if (baseSigOpts.getPageNumber() != null) {
                        changePdfPage(baseSigOpts.getPageNumber());
                    }

                    setDocumentSignaturesInfo();
                    prepareSigFieldComboBox();
                } else {
                    pdfPreview.init(doc, baseSigOpts);

                    jComboBoxPageNr.setEnabled(false);
                    jComboBoxSigField.setVisible(false);
                    jLabelTotalPags.setText("1");
                    jLabelTotalPags.setEnabled(false);
                }

                ((CardLayout) previewArea.getLayout()).next(previewArea);
            });

            getGlassPane().setVisible(false);
            reason.requestFocusInWindow();
        }
    }

    private void prepareSigFieldComboBox() {
        if (!blankSignaturesInfo.isEmpty()) {
            List<SignatureFieldInfo> sigFieldOptions = new ArrayList<>();
            sigFieldOptions.add(DEFAULT_BLANK_SIG_INFO);
            sigFieldOptions.addAll(blankSignaturesInfo);
            jComboBoxSigField.setModel(new DefaultComboBoxModel<>(sigFieldOptions.toArray(new SignatureFieldInfo[0])));
            jComboBoxSigField.setSelectedItem(blankSignaturesInfo.get(0));
        } else {
            jComboBoxSigField.setModel(new DefaultComboBoxModel<>(new SignatureFieldInfo[]{DEFAULT_BLANK_SIG_INFO}));
            jComboBoxSigField.setVisible(false);
        }
    }

    private boolean loadPDFInfo(AssinareDocument data) {
        try {
            PdfParser pdfParser = new DssPdfParser();
            parsedPdfInfo = pdfParser.parsePdf(data);

            signaturesInfo = parsedPdfInfo.getExistingSignatures();

            allSignaturesValid = true;
            for (SignatureInfo signatureInfo : signaturesInfo) {
                if (!signatureInfo.isValid()) {
                    allSignaturesValid = false;
                    break;
                }
            }

            blankSignaturesInfo = parsedPdfInfo.getSignatureFields();

            return true;
        } catch (DocumentParseException ex) {
            LOG.log(Level.INFO, null, ex);
            return false;
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        helpPopupMenu = new javax.swing.JPopupMenu();
        manualMenuItem = new javax.swing.JMenuItem();
        aboutMenuItem = new javax.swing.JMenuItem();
        jPopupMenuSignsState = new javax.swing.JPopupMenu();
        blockerPanel = new com.linkare.assinare.sign.swing.BlockerPanel();
        previewArea = new javax.swing.JPanel();
        loadingLbl = new javax.swing.JLabel();
        pdfPreview = new com.linkare.assinare.sign.pdf.PdfSignaturePreview();
        jPanel2 = new javax.swing.JPanel();
        reasonLbl = new javax.swing.JLabel();
        reason = new javax.swing.JTextField();
        locationLbl = new javax.swing.JLabel();
        location = new javax.swing.JTextField();
        contactLbl = new javax.swing.JLabel();
        contact = new javax.swing.JTextField();
        logoFileName = new javax.swing.JTextField();
        imageSelect = new javax.swing.JButton();
        jComboBoxSignatureTypeChooser = new javax.swing.JComboBox<>();
        contactLbl1 = new javax.swing.JLabel();
        jButton1 = new javax.swing.JButton();
        jCheckBoxLTV = new javax.swing.JCheckBox();
        deckCards = new com.linkare.assinare.sign.swing.DeckCardsPanel();
        jPanelBottom = new javax.swing.JPanel();
        jButtonDecPage = new javax.swing.JButton();
        jButtonIncPage = new javax.swing.JButton();
        jButtonLastPage = new javax.swing.JButton();
        jButtonFirstPage = new javax.swing.JButton();
        jComboBoxPageNr = new javax.swing.JComboBox<>();
        jLabel1 = new javax.swing.JLabel();
        jLabelTotalPags = new javax.swing.JLabel();
        jLabelSignsState = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jComboBoxSigField = new javax.swing.JComboBox<>();
        jPanelTop = new javax.swing.JPanel();

        helpPopupMenu.setName("helpPopupMenu"); // NOI18N

        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("com/linkare/assinare/resourceBundle/Language"); // NOI18N
        manualMenuItem.setText(bundle.getString("assinare.label.operation")); // NOI18N
        manualMenuItem.setName("manualMenuItem"); // NOI18N
        manualMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                manualMenuItemActionPerformed(evt);
            }
        });
        helpPopupMenu.add(manualMenuItem);

        aboutMenuItem.setText(bundle.getString("assinare.label.about")); // NOI18N
        aboutMenuItem.setName("aboutMenuItem"); // NOI18N
        aboutMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                aboutMenuItemActionPerformed(evt);
            }
        });
        helpPopupMenu.add(aboutMenuItem);

        jPopupMenuSignsState.setName("jPopupMenuSignsState"); // NOI18N

        blockerPanel.setName("blockerPanel"); // NOI18N

        setTitle("ASSINARE");
        setForeground(new java.awt.Color(255, 255, 255));
        setIconImage(new ImageIcon(getClass().getResource("/icons/assinareIconHeader.png")).getImage());
        setMinimumSize(new java.awt.Dimension(600, 550));
        addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentShown(java.awt.event.ComponentEvent evt) {
                formComponentShown(evt);
            }
        });
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });

        previewArea.setName("previewArea"); // NOI18N
        previewArea.setLayout(new java.awt.CardLayout());

        loadingLbl.setFont(loadingLbl.getFont().deriveFont((float)30));
        loadingLbl.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        loadingLbl.setText("A carregar o seu documento");
        loadingLbl.setName("loadingLbl"); // NOI18N
        previewArea.add(loadingLbl, "loading");

        pdfPreview.setName("pdfPreview"); // NOI18N
        previewArea.add(pdfPreview, "pdfPreview");

        getContentPane().add(previewArea, java.awt.BorderLayout.CENTER);

        jPanel2.setBackground(new java.awt.Color(100, 100, 100));
        jPanel2.setName("jPanel2"); // NOI18N
        jPanel2.setPreferredSize(new java.awt.Dimension(250, 567));

        reasonLbl.setFont(reasonLbl.getFont().deriveFont(reasonLbl.getFont().getSize()-1f));
        reasonLbl.setForeground(new java.awt.Color(255, 255, 255));
        reasonLbl.setText(bundle.getString("assinare.label.reason")); // NOI18N
        reasonLbl.setVerticalAlignment(javax.swing.SwingConstants.BOTTOM);
        reasonLbl.setName("reasonLbl"); // NOI18N

        reason.setText(baseSigOpts.getReason());
        reason.setName("reason"); // NOI18N

        locationLbl.setFont(locationLbl.getFont().deriveFont(locationLbl.getFont().getSize()-1f));
        locationLbl.setForeground(new java.awt.Color(255, 255, 255));
        locationLbl.setText(bundle.getString("assinare.label.signingPlace")); // NOI18N
        locationLbl.setVerticalAlignment(javax.swing.SwingConstants.BOTTOM);
        locationLbl.setName("locationLbl"); // NOI18N

        location.setText(baseSigOpts.getLocation());
        location.setName("location"); // NOI18N

        contactLbl.setFont(contactLbl.getFont().deriveFont(contactLbl.getFont().getSize()-1f));
        contactLbl.setForeground(new java.awt.Color(255, 255, 255));
        contactLbl.setText(bundle.getString("assinare.label.contactDetails")); // NOI18N
        contactLbl.setVerticalAlignment(javax.swing.SwingConstants.BOTTOM);
        contactLbl.setName("contactLbl"); // NOI18N

        contact.setText(baseSigOpts.getContact());
        contact.setName("contact"); // NOI18N

        logoFileName.setEditable(false);
        logoFileName.setFont(logoFileName.getFont().deriveFont(logoFileName.getFont().getSize()-1f));
        logoFileName.setName("logoFileName"); // NOI18N

        imageSelect.setFont(imageSelect.getFont().deriveFont(imageSelect.getFont().getSize()-1f));
        imageSelect.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/searchIcon.png"))); // NOI18N
        imageSelect.setName("imageSelect"); // NOI18N
        imageSelect.setRolloverIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/searchIconOver.png"))); // NOI18N
        imageSelect.setRolloverSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/searchIconOver.png"))); // NOI18N
        imageSelect.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                imageSelectActionPerformed(evt);
            }
        });

        jComboBoxSignatureTypeChooser.setFont(jComboBoxSignatureTypeChooser.getFont().deriveFont(jComboBoxSignatureTypeChooser.getFont().getSize()-1f));
        jComboBoxSignatureTypeChooser.setModel(new DefaultComboBoxModel(SignatureRenderingMode.values()));
        jComboBoxSignatureTypeChooser.setSelectedItem(baseSigOpts.getSigRenderingMode());
        jComboBoxSignatureTypeChooser.setName("jComboBoxSignatureTypeChooser"); // NOI18N
        jComboBoxSignatureTypeChooser.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jComboBoxSignatureTypeChooserItemStateChanged(evt);
            }
        });

        contactLbl1.setFont(contactLbl1.getFont().deriveFont(contactLbl1.getFont().getSize()-1f));
        contactLbl1.setForeground(new java.awt.Color(255, 255, 255));
        contactLbl1.setText(bundle.getString("assinare.label.signatureOptions")); // NOI18N
        contactLbl1.setVerticalAlignment(javax.swing.SwingConstants.BOTTOM);
        contactLbl1.setName("contactLbl1"); // NOI18N

        jButton1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/helpIcon.png"))); // NOI18N
        jButton1.setBorderPainted(false);
        jButton1.setContentAreaFilled(false);
        jButton1.setFocusPainted(false);
        jButton1.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton1.setName("jButton1"); // NOI18N
        jButton1.setPreferredSize(new java.awt.Dimension(21, 26));
        jButton1.setPressedIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/helpIconOver.png"))); // NOI18N
        jButton1.setRolloverIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/helpIconOver.png"))); // NOI18N
        jButton1.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/helpIconOver.png"))); // NOI18N
        jButton1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jButton1MouseClicked(evt);
            }
        });

        jCheckBoxLTV.setForeground(new java.awt.Color(255, 255, 255));
        jCheckBoxLTV.setSelected(baseSigOpts.isArchiving());
        jCheckBoxLTV.setText(bundle.getString("assinare.label.longTermValidation")); // NOI18N
        jCheckBoxLTV.setToolTipText("<html>Permite verificar a validade de uma assinatura <br/>\nmuito depois da assinatura do documento");
        jCheckBoxLTV.setName("jCheckBoxLTV"); // NOI18N
        jCheckBoxLTV.setHorizontalTextPosition(SwingConstants.LEFT);

        deckCards.setBackground(new java.awt.Color(100, 100, 100));
        deckCards.setName("deckCards"); // NOI18N
        deckCards.addCancelListener(new com.linkare.assinare.sign.swing.CancelListener() {
            public void actionCanceled(java.awt.event.ActionEvent evt) {
                signatureCanceled(evt);
            }
        });
        deckCards.addConfirmListener(new com.linkare.assinare.sign.swing.ConfirmListener() {
            public void actionConfirmed(java.awt.event.ActionEvent evt) {
                signatureConfirmed(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                        .addComponent(logoFileName)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(imageSelect))
                    .addComponent(jComboBoxSignatureTypeChooser, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jCheckBoxLTV, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(reason, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(location)
                            .addComponent(contact)
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(locationLbl)
                                    .addComponent(contactLbl)
                                    .addComponent(reasonLbl)
                                    .addComponent(contactLbl1))
                                .addGap(0, 0, Short.MAX_VALUE))
                            .addComponent(deckCards, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addContainerGap())))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(27, 27, 27)
                .addComponent(reasonLbl)
                .addGap(3, 3, 3)
                .addComponent(reason, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(30, 30, 30)
                .addComponent(locationLbl)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(location, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(30, 30, 30)
                .addComponent(contactLbl)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(contact, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(30, 30, 30)
                .addComponent(contactLbl1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jComboBoxSignatureTypeChooser, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(logoFileName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(imageSelect))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jCheckBoxLTV)
                .addGap(15, 15, 15)
                .addComponent(deckCards, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(21, 21, 21))
        );

        getContentPane().add(jPanel2, java.awt.BorderLayout.LINE_END);

        jPanelBottom.setName("jPanelBottom"); // NOI18N
        jPanelBottom.setPreferredSize(new java.awt.Dimension(816, 30));

        jButtonDecPage.setFont(new java.awt.Font("SansSerif", 1, 12)); // NOI18N
        jButtonDecPage.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/pageDec.png"))); // NOI18N
        jButtonDecPage.setBorderPainted(false);
        jButtonDecPage.setContentAreaFilled(false);
        jButtonDecPage.setName("jButtonDecPage"); // NOI18N
        jButtonDecPage.setPressedIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/pageDecOver.png"))); // NOI18N
        jButtonDecPage.setRolloverIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/pageDecOver.png"))); // NOI18N
        jButtonDecPage.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/pageDecOver.png"))); // NOI18N
        jButtonDecPage.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonDecPageActionPerformed(evt);
            }
        });

        jButtonIncPage.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/pageInc.png"))); // NOI18N
        jButtonIncPage.setBorderPainted(false);
        jButtonIncPage.setContentAreaFilled(false);
        jButtonIncPage.setName("jButtonIncPage"); // NOI18N
        jButtonIncPage.setPressedIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/pageIncOver.png"))); // NOI18N
        jButtonIncPage.setRolloverIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/pageIncOver.png"))); // NOI18N
        jButtonIncPage.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/pageIncOver.png"))); // NOI18N
        jButtonIncPage.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonIncPageActionPerformed(evt);
            }
        });

        jButtonLastPage.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/lastPage.png"))); // NOI18N
        jButtonLastPage.setBorderPainted(false);
        jButtonLastPage.setContentAreaFilled(false);
        jButtonLastPage.setName("jButtonLastPage"); // NOI18N
        jButtonLastPage.setPressedIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/lastPageOver.png"))); // NOI18N
        jButtonLastPage.setRolloverIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/lastPageOver.png"))); // NOI18N
        jButtonLastPage.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/lastPageOver.png"))); // NOI18N
        jButtonLastPage.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonLastPageActionPerformed(evt);
            }
        });

        jButtonFirstPage.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/firstPage.png"))); // NOI18N
        jButtonFirstPage.setBorderPainted(false);
        jButtonFirstPage.setContentAreaFilled(false);
        jButtonFirstPage.setName("jButtonFirstPage"); // NOI18N
        jButtonFirstPage.setPressedIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/firstPageOver.png"))); // NOI18N
        jButtonFirstPage.setRolloverIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/firstPageOver.png"))); // NOI18N
        jButtonFirstPage.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/firstPageOver.png"))); // NOI18N
        jButtonFirstPage.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonFirstPageActionPerformed(evt);
            }
        });

        jComboBoxPageNr.setEditable(true);
        jComboBoxPageNr.setFont(jComboBoxPageNr.getFont().deriveFont(jComboBoxPageNr.getFont().getSize()-1f));
        jComboBoxPageNr.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        jComboBoxPageNr.setName("jComboBoxPageNr"); // NOI18N
        jComboBoxPageNr.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jComboBoxPageNrItemStateChanged(evt);
            }
        });

        jLabel1.setText("de ");
        jLabel1.setName("jLabel1"); // NOI18N

        jLabelTotalPags.setText("1000");
        jLabelTotalPags.setName("jLabelTotalPags"); // NOI18N

        jLabelSignsState.setBackground(new java.awt.Color(255, 255, 255));
        jLabelSignsState.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabelSignsState.setName("jLabelSignsState"); // NOI18N
        jLabelSignsState.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLabelSignsStateMouseClicked(evt);
            }
        });

        jLabel2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/resizeIcon.png"))); // NOI18N
        jLabel2.setName("jLabel2"); // NOI18N

        jComboBoxSigField.setName("jComboBoxSigField"); // NOI18N
        jComboBoxSigField.setRenderer(getSignatureFieldInfoRenderer());
        jComboBoxSigField.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jComboBoxSigFieldChanged(evt);
            }
        });

        javax.swing.GroupLayout jPanelBottomLayout = new javax.swing.GroupLayout(jPanelBottom);
        jPanelBottom.setLayout(jPanelBottomLayout);
        jPanelBottomLayout.setHorizontalGroup(
            jPanelBottomLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanelBottomLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabelSignsState, javax.swing.GroupLayout.PREFERRED_SIZE, 41, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jButtonFirstPage)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButtonDecPage)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jComboBoxPageNr, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(4, 4, 4)
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabelTotalPags)
                .addGap(6, 6, 6)
                .addComponent(jButtonIncPage)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButtonLastPage)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 388, Short.MAX_VALUE)
                .addComponent(jComboBoxSigField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel2))
        );
        jPanelBottomLayout.setVerticalGroup(
            jPanelBottomLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jButtonLastPage, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jButtonIncPage, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jComboBoxPageNr)
            .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jLabelTotalPags, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jButtonDecPage, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jButtonFirstPage, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jLabelSignsState, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(jPanelBottomLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addComponent(jComboBoxSigField, javax.swing.GroupLayout.Alignment.TRAILING)
        );

        getContentPane().add(jPanelBottom, java.awt.BorderLayout.PAGE_END);

        jPanelTop.setName("jPanelTop"); // NOI18N
        jPanelTop.setPreferredSize(new java.awt.Dimension(861, 0));

        javax.swing.GroupLayout jPanelTopLayout = new javax.swing.GroupLayout(jPanelTop);
        jPanelTop.setLayout(jPanelTopLayout);
        jPanelTopLayout.setHorizontalGroup(
            jPanelTopLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 861, Short.MAX_VALUE)
        );
        jPanelTopLayout.setVerticalGroup(
            jPanelTopLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );

        getContentPane().add(jPanelTop, java.awt.BorderLayout.PAGE_START);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButtonIncPageActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonIncPageActionPerformed
        rollPdfPages(1);
    }//GEN-LAST:event_jButtonIncPageActionPerformed

    private void jButtonDecPageActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonDecPageActionPerformed
        rollPdfPages(-1);
    }//GEN-LAST:event_jButtonDecPageActionPerformed

    private void jButtonLastPageActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonLastPageActionPerformed
        changePdfPage(pdfPreview.getPageCount());
    }//GEN-LAST:event_jButtonLastPageActionPerformed

    private void jButtonFirstPageActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonFirstPageActionPerformed
        changePdfPage(1);
    }//GEN-LAST:event_jButtonFirstPageActionPerformed

    private void rollPdfPages(int numPages) {
        int currPage = pdfPreview.getCurrentPage();
        changePdfPage(currPage + numPages);
    }

    private void changePdfPage(int pageNumber) {
        int currIdx = jComboBoxPageNr.getSelectedIndex();
        int firstPage = 1;
        int lastPage = pdfPreview.getPageCount();

        if (pageNumber >= firstPage && pageNumber <= lastPage) {
            if ((pageNumber - 1) != currIdx) {
                jComboBoxPageNr.setSelectedIndex(pageNumber - 1);
            }
        }
    }

    private void jComboBoxPageNrItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jComboBoxPageNrItemStateChanged
        JComboBox comboBox = ((JComboBox) evt.getItemSelectable());
        if (evt.getStateChange() == ItemEvent.SELECTED) {
            if (evt.getItem() instanceof Integer) {
                int pageNr = (Integer) evt.getItem();
                if (pageNr < 1) {
                    changeComboBox(comboBox, 0);
                } else if (pageNr > pdfPreview.getPageCount()) {
                    changeComboBox(comboBox, comboBox.getModel().getSize() - 1);
                } else {
                    redrawPdfPreview(pageNr);
                }
            } else {
                changeComboBox(comboBox, 0);
            }
        }
    }//GEN-LAST:event_jComboBoxPageNrItemStateChanged

    private void changeComboBox(JComboBox comboBox, int index) {
        if (comboBox.getSelectedIndex() != index) {
            comboBox.setSelectedIndex(index);
            redrawPdfPreview((Integer) comboBox.getSelectedItem());
        }
    }

    private void formComponentShown(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_formComponentShown
        imageSelect.setEnabled(((SignatureRenderingMode) jComboBoxSignatureTypeChooser.getSelectedItem()).isUserLogo());

        // Show and hide helpPopupMenu so it's size gets calculated properly
        helpPopupMenu.show(jButton1, 0, 0);
        helpPopupMenu.setVisible(false);

        // Show and hide jPopupMenuSignsState so it's size gets calculated properly
        jPopupMenuSignsState.show(jLabelSignsState, 0, 0);
        jPopupMenuSignsState.setVisible(false);
    }//GEN-LAST:event_formComponentShown

    private void manualMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_manualMenuItemActionPerformed
        SwingHelper.openUrlInBrowser("http://support.assinare.eu");
    }//GEN-LAST:event_manualMenuItemActionPerformed

    private void aboutMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_aboutMenuItemActionPerformed
        AboutDialog.showModal(this, null);
    }//GEN-LAST:event_aboutMenuItemActionPerformed

    private void jLabelSignsStateMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabelSignsStateMouseClicked
        jPopupMenuSignsState.show(evt.getComponent(), 0, -jPopupMenuSignsState.getHeight() - 10);
    }//GEN-LAST:event_jLabelSignsStateMouseClicked

    private void jButton1MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButton1MouseClicked
        helpPopupMenu.show(evt.getComponent(), -helpPopupMenu.getWidth() + evt.getComponent().getWidth(), evt.getComponent().getHeight());
    }//GEN-LAST:event_jButton1MouseClicked

    private void jComboBoxSignatureTypeChooserItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jComboBoxSignatureTypeChooserItemStateChanged
        if (((SignatureRenderingMode) evt.getItem()).isUserLogo()) {
            imageSelect.setEnabled(true);
        } else {
            logoFileName.setText("");
            imageSelect.setEnabled(false);
        }
    }//GEN-LAST:event_jComboBoxSignatureTypeChooserItemStateChanged

    private void imageSelectActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_imageSelectActionPerformed
        JFileChooser fileChooser = new JFileChooser();

        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            logoFileName.setText(fileChooser.getSelectedFile().getName());
            try {
                selectedLogoFile = fileChooser.getSelectedFile().toURI().toURL();
            } catch (MalformedURLException ex) {
                LOG.log(Level.SEVERE, null, ex);
            }
        }
    }//GEN-LAST:event_imageSelectActionPerformed

    private void signatureConfirmed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_signatureConfirmed
        sigFieldsFuture.complete(collectSignatureFields());
        sigKeyFuture.complete(signingKeysSupplier.getKeys().get(0));
    }//GEN-LAST:event_signatureConfirmed

    private void signatureCanceled(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_signatureCanceled
        sigFieldsFuture.cancel(false);
        sigKeyFuture.cancel(false);
    }//GEN-LAST:event_signatureCanceled

    private void jComboBoxSigFieldChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jComboBoxSigFieldChanged
        if (evt.getStateChange() == ItemEvent.SELECTED) {
            SignatureFieldInfo blankSignatureInfo = (SignatureFieldInfo) evt.getItem();

            if (blankSignatureInfo.getFieldName() != null) {
                pdfPreview.setShowSigTarget(false);
                changePdfPage(blankSignatureInfo.getPage());
            } else {
                pdfPreview.setShowSigTarget(true);
            }
        }
    }//GEN-LAST:event_jComboBoxSigFieldChanged

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        if (!sigFieldsFuture.isDone()) {
            sigFieldsFuture.cancel(false);
        }
    }//GEN-LAST:event_formWindowClosing

    private void closeWindow() {
        dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JMenuItem aboutMenuItem;
    private com.linkare.assinare.sign.swing.BlockerPanel blockerPanel;
    private javax.swing.JTextField contact;
    private javax.swing.JLabel contactLbl;
    private javax.swing.JLabel contactLbl1;
    private com.linkare.assinare.sign.swing.DeckCardsPanel deckCards;
    private javax.swing.JPopupMenu helpPopupMenu;
    private javax.swing.JButton imageSelect;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButtonDecPage;
    private javax.swing.JButton jButtonFirstPage;
    private javax.swing.JButton jButtonIncPage;
    private javax.swing.JButton jButtonLastPage;
    private javax.swing.JCheckBox jCheckBoxLTV;
    private javax.swing.JComboBox<Integer> jComboBoxPageNr;
    private javax.swing.JComboBox<SignatureFieldInfo> jComboBoxSigField;
    private javax.swing.JComboBox<SignatureRenderingMode> jComboBoxSignatureTypeChooser;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabelSignsState;
    private javax.swing.JLabel jLabelTotalPags;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanelBottom;
    private javax.swing.JPanel jPanelTop;
    private javax.swing.JPopupMenu jPopupMenuSignsState;
    private javax.swing.JLabel loadingLbl;
    private javax.swing.JTextField location;
    private javax.swing.JLabel locationLbl;
    private javax.swing.JTextField logoFileName;
    private javax.swing.JMenuItem manualMenuItem;
    private com.linkare.assinare.sign.pdf.PdfSignaturePreview pdfPreview;
    private javax.swing.JPanel previewArea;
    private javax.swing.JTextField reason;
    private javax.swing.JLabel reasonLbl;
    // End of variables declaration//GEN-END:variables

    private PDFSignatureFields collectSignatureFields() {
        SignatureRenderingMode sigRenderingMode = (SignatureRenderingMode) jComboBoxSignatureTypeChooser.getSelectedItem();
        String sigFieldName = ((SignatureFieldInfo) jComboBoxSigField.getSelectedItem()).getFieldName();

        if (sigRenderingMode.isUserLogo()) {
            return baseSigOpts.merge(new PDFSignatureFields(contact.getText(), location.getText(), reason.getText(),
                    pdfPreview.getPercentX(), pdfPreview.getPercentY(), pdfPreview.getCurrentPage(),
                    pdfPreview.getSignatureWidth(), pdfPreview.getSignatureHeight(),
                    sigRenderingMode, sigFieldName, null, jCheckBoxLTV.isSelected(), selectedLogoFile,
                    null));
        } else {
            return baseSigOpts.merge(new PDFSignatureFields(contact.getText(), location.getText(), reason.getText(),
                    pdfPreview.getPercentX(), pdfPreview.getPercentY(), pdfPreview.getCurrentPage(),
                    pdfPreview.getSignatureWidth(), pdfPreview.getSignatureHeight(),
                    sigRenderingMode, sigFieldName, null, jCheckBoxLTV.isSelected()));
        }
    }

    @Override
    public Future<PDFSignatureFields> getSignatureFields() {
        return sigFieldsFuture;
    }

    @Override
    public Future<SigningKey> getSigningKey() {
        return sigKeyFuture;
    }

    @Override
    public void publicPublish(final SignatureStage... chunks) {
        SignatureStage latestStage = chunks[chunks.length - 1];

        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater(() -> {
                deckCards.changePanel(latestStage);
            });
        }
    }

    @Override
    public void signatureDone() {
        closeWindow();
    }

    @Override
    public void dispose() {
        pdfPreview.close();
        super.dispose();
    }

    private ListCellRenderer<Object> getSignatureFieldInfoRenderer() {
        return new SignatureFieldInfoListCellRenderer();

    }

    private static class SignatureFieldInfoListCellRenderer extends DefaultListCellRenderer {

        private static final long serialVersionUID = 2078374075658656447L;

        @Override
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            if (value instanceof SignatureFieldInfo) {
                SignatureFieldInfo sigFieldInfo = (SignatureFieldInfo) value;

                String textValue;
                if (sigFieldInfo.getFieldName() != null) {
                    textValue = sigFieldInfo.getFieldName() + " (Page " + sigFieldInfo.getPage() + ")";
                } else {
                    textValue = "-Custom-";
                }

                return super.getListCellRendererComponent(list, textValue, index, isSelected, cellHasFocus);
            } else {
                return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            }
        }

    }

}
