package com.linkare.assinare.sign.swing;

import java.awt.Desktop;
import java.awt.Font;
import java.awt.FontFormatException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.UIDefaults;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.plaf.FontUIResource;

import org.apache.commons.io.IOUtils;
import org.pushingpixels.substance.api.SubstanceLookAndFeel;
import org.pushingpixels.substance.api.fonts.FontSet;
import org.pushingpixels.substance.api.skin.SubstanceGraphiteLookAndFeel;

/**
 *
 * @author bnazare
 */
public class SwingHelper {
    
    private static final Logger LOG = Logger.getLogger(SwingHelper.class.getName());

    private SwingHelper() {
    }
    
    public static void openUrlInBrowser(String url) {
        Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
        if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
            try {
                desktop.browse(new URI(url));
            } catch (URISyntaxException | IOException e) {
                LOG.log(Level.SEVERE, null, e);
            }
        }
    }

    public static void setupLookAndFeel() {
        try {
            JFrame.setDefaultLookAndFeelDecorated(true);
            JDialog.setDefaultLookAndFeelDecorated(true);
            UIManager.setLookAndFeel(new SubstanceGraphiteLookAndFeel());
            final Font myFont = Font.createFont(Font.TRUETYPE_FONT, SwingHelper.class.getClassLoader().getResourceAsStream("fonts/DejaVuSans.ttf"));
            SubstanceLookAndFeel.setFontPolicy((String string, UIDefaults uid) -> new AssinareFontSet(myFont));
        } catch (UnsupportedLookAndFeelException | FontFormatException | IOException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
    }

    public static void openPdfWithSOApplication(InputStream pdfSrc) throws IOException {
        File tmpFile = File.createTempFile("doc", ".pdf");
        
        try (FileOutputStream fos = new FileOutputStream(tmpFile)) {
            IOUtils.copy(pdfSrc, fos);
            fos.flush();
        }
        
        Desktop.getDesktop().open(tmpFile);
    }

    private static class AssinareFontSet implements FontSet {
        
        private static final float REGULAR_FONT_SIZE = 14.0F;
        private static final float SMALL_FONT_SIZE = 13.0F;
        private static final float VERY_LARGE_FONT_SIZE = 18.0F;

        private final Font baseFont;

        public AssinareFontSet(Font baseFont) {
            this.baseFont = baseFont;
        }

        @Override
        public FontUIResource getControlFont() {
            return new FontUIResource(baseFont.deriveFont(REGULAR_FONT_SIZE));
        }

        @Override
        public FontUIResource getMenuFont() {
            return new FontUIResource(baseFont.deriveFont(SMALL_FONT_SIZE));
        }

        @Override
        public FontUIResource getTitleFont() {
            return new FontUIResource(baseFont.deriveFont(REGULAR_FONT_SIZE));
        }

        @Override
        public FontUIResource getWindowTitleFont() {
            return new FontUIResource(baseFont.deriveFont(Font.BOLD, VERY_LARGE_FONT_SIZE));
        }

        @Override
        public FontUIResource getSmallFont() {
            return new FontUIResource(baseFont.deriveFont(SMALL_FONT_SIZE));
        }

        @Override
        public FontUIResource getMessageFont() {
            return new FontUIResource(baseFont.deriveFont(REGULAR_FONT_SIZE));
        }
    }
}
