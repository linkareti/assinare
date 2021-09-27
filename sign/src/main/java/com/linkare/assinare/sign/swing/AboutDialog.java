package com.linkare.assinare.sign.swing;

import java.awt.Color;
import java.awt.Window;
import java.awt.image.BufferedImage;

import org.pushingpixels.substance.api.ComponentState;
import org.pushingpixels.substance.api.DecorationAreaType;
import org.pushingpixels.substance.api.SubstanceColorScheme;
import org.pushingpixels.substance.api.SubstanceColorSchemeBundle;
import org.pushingpixels.substance.api.SubstanceConstants;
import org.pushingpixels.substance.api.SubstanceLookAndFeel;
import org.pushingpixels.substance.api.colorscheme.BaseDarkColorScheme;
import org.pushingpixels.substance.api.painter.border.GlassBorderPainter;
import org.pushingpixels.substance.api.painter.decoration.ArcDecorationPainter;
import org.pushingpixels.substance.api.painter.decoration.FlatDecorationPainter;
import org.pushingpixels.substance.api.painter.fill.GlassFillPainter;
import org.pushingpixels.substance.api.painter.highlight.ClassicHighlightPainter;
import org.pushingpixels.substance.api.shaper.ClassicButtonShaper;
import org.pushingpixels.substance.api.skin.ChallengerDeepSkin;
import org.pushingpixels.substance.api.watermark.SubstanceImageWatermark;

/**
 *
 * @author rvaz
 */
public class AboutDialog extends javax.swing.JDialog {

    private static final long serialVersionUID = -1348529344149729009L;

    private static final String ASSINARE_HOMEPAGE = "http://www.assinare.eu/";
    private static final String LINKARE_HOMEPAGE = "http://www.linkare.com/";

    /**
     * Creates new form AboutDialog
     *
     * @param parent
     * @param title
     */
    public AboutDialog(Window parent, String title) {
        super(parent, title);
        initSubstanceProperties();

        initComponents();
    }

    private void initSubstanceProperties() {
        getRootPane().putClientProperty(SubstanceLookAndFeel.SKIN_PROPERTY, new AboutDialogSkin());
        getRootPane().putClientProperty(SubstanceLookAndFeel.WATERMARK_VISIBLE, Boolean.TRUE);
    }

    public static void showModal(Window parent, String title) {
        AboutDialog aboutDialog = new AboutDialog(parent, title);
        aboutDialog.setModal(true);
        aboutDialog.setLocationRelativeTo(parent);

        aboutDialog.setVisible(true);
    }

    private BufferedImage getEmptyImage() {
        return new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);

    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setIconImage(getEmptyImage());
        setMinimumSize(new java.awt.Dimension(480, 208));
        setResizable(false);

        jLabel4.setFont(jLabel4.getFont().deriveFont(jLabel4.getFont().getStyle() | java.awt.Font.BOLD, jLabel4.getFont().getSize()-2));
        jLabel4.setText("www.assinare.eu");
        jLabel4.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jLabel4.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLabel4MouseClicked(evt);
            }
        });

        jLabel5.setFont(jLabel5.getFont().deriveFont(jLabel5.getFont().getSize()-1f));
        jLabel5.setText("Copyright © 2014");

        jLabel2.setFont(jLabel2.getFont().deriveFont(jLabel2.getFont().getStyle() | java.awt.Font.BOLD, jLabel2.getFont().getSize()-1));
        jLabel2.setText("Linkare.");
        jLabel2.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jLabel2.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLabel2MouseClicked(evt);
            }
        });

        jLabel3.setFont(jLabel3.getFont().deriveFont(jLabel3.getFont().getSize()-1f));
        jLabel3.setText("All rights reserved.");

        jLabel6.setFont(jLabel6.getFont().deriveFont(jLabel6.getFont().getSize()-1f));
        jLabel6.setText("Licenciado para");

        jLabel7.setFont(jLabel7.getFont().deriveFont(jLabel7.getFont().getStyle() | java.awt.Font.BOLD, jLabel7.getFont().getSize()-1));
        jLabel7.setText("efeitos de teste");
        jLabel7.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jLabel7.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLabel7MouseClicked(evt);
            }
        });

        jLabel1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/about1.png"))); // NOI18N

        jLabel8.setBackground(java.awt.Color.white);
        jLabel8.setOpaque(true);
        jLabel8.putClientProperty(SubstanceLookAndFeel.COLORIZATION_FACTOR, 0.8d);

        jLabel9.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/about3.png"))); // NOI18N

        jLabel10.setFont(jLabel10.getFont().deriveFont(jLabel10.getFont().getSize()-2f));
        jLabel10.setText("Version 1.1.0");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jLabel8, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel5)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel3)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel6)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, 178, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(jLabel9))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel4)
                                .addGap(0, 345, Short.MAX_VALUE)))
                        .addContainerGap())))
            .addGroup(layout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel1)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(jLabel10)
                        .addGap(8, 8, 8)))
                .addGap(0, 0, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel10)
                .addGap(18, 18, 18)
                .addComponent(jLabel4)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel8, javax.swing.GroupLayout.PREFERRED_SIZE, 1, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel5)
                            .addComponent(jLabel2)
                            .addComponent(jLabel3))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel6)
                            .addComponent(jLabel7)))
                    .addComponent(jLabel9, javax.swing.GroupLayout.Alignment.TRAILING))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jLabel4MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel4MouseClicked
        SwingHelper.openUrlInBrowser(ASSINARE_HOMEPAGE);
    }//GEN-LAST:event_jLabel4MouseClicked

    private void jLabel2MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel2MouseClicked
        SwingHelper.openUrlInBrowser(LINKARE_HOMEPAGE);
    }//GEN-LAST:event_jLabel2MouseClicked

    private void jLabel7MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel7MouseClicked
        SwingHelper.openUrlInBrowser(LINKARE_HOMEPAGE);
    }//GEN-LAST:event_jLabel7MouseClicked

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    // End of variables declaration//GEN-END:variables
}

class AboutDialogSkin extends ChallengerDeepSkin {

    AboutDialogSkin() {
        registerColorSchemes();

        this.buttonShaper = new ClassicButtonShaper();
        this.fillPainter = new GlassFillPainter();
        this.decorationPainter = new ArcDecorationPainter();
        this.highlightPainter = new ClassicHighlightPainter();
        this.borderPainter = new GlassBorderPainter();

        this.decorationPainter = new FlatDecorationPainter();

        watermark = new SubstanceImageWatermark(Thread.currentThread().getContextClassLoader().getResourceAsStream("icons/about2.png"));
        ((SubstanceImageWatermark) watermark).setOpacity(0.2f);
        ((SubstanceImageWatermark) watermark).setKind(SubstanceConstants.ImageWatermarkKind.APP_CENTER);
    }

    private void registerColorSchemes() {
        SubstanceColorScheme activeScheme = new AboutDialogColorScheme();
        SubstanceColorScheme enabledScheme = activeScheme.tone(0.1f).named(
                "Challenger Deep Enabled");

        SubstanceColorSchemeBundle defaultSchemeBundle = new SubstanceColorSchemeBundle(
                activeScheme, enabledScheme, enabledScheme);
        defaultSchemeBundle.registerColorScheme(activeScheme, 0.0f,
                ComponentState.DISABLED_SELECTED);
        defaultSchemeBundle.registerColorScheme(enabledScheme, 0.4f,
                ComponentState.DISABLED_UNSELECTED);
        this.registerDecorationAreaSchemeBundle(defaultSchemeBundle,
                DecorationAreaType.NONE);

        this.registerAsDecorationArea(enabledScheme,
                DecorationAreaType.PRIMARY_TITLE_PANE,
                DecorationAreaType.SECONDARY_TITLE_PANE,
                DecorationAreaType.HEADER, DecorationAreaType.FOOTER,
                DecorationAreaType.GENERAL, DecorationAreaType.TOOLBAR);
    }

    @Override
    public String getDisplayName() {
        return "Challenger-mod";
    }

    private static final class AboutDialogColorScheme extends BaseDarkColorScheme {

        private static final Color THE_COLOR = new Color(2, 9, 50);

        private AboutDialogColorScheme() {
            super("AboutDialog");
        }

        @Override
        public Color getForegroundColor() {
            return Color.WHITE;
        }

        @Override
        public Color getUltraLightColor() {
            return THE_COLOR;
        }

        @Override
        public Color getExtraLightColor() {
            return THE_COLOR;
        }

        @Override
        public Color getLightColor() {
            return THE_COLOR;
        }

        @Override
        public Color getMidColor() {
            return THE_COLOR;
        }

        @Override
        public Color getDarkColor() {
            return THE_COLOR;
        }

        @Override
        public Color getUltraDarkColor() {
            return THE_COLOR;
        }
    }

}