package com.linkare.assinare.sign.pdf;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;

/**
 *
 * @author bnazare
 */
public class DemoImageUtils {

    private static final Logger LOG = Logger.getLogger(DemoImageUtils.class.getName());

    private static final int DEFAULT_IMAGE_WIDTH = 64;
    private static final int DEFAULT_IMAGE_HEIGHT = 64;

    private DemoImageUtils() {
    }

    public static BufferedImage makeDemoImageOverlay(URL logoFileUrl) {
        BufferedImage image;
        try {
            image = ImageIO.read(logoFileUrl);
            int imageWidth = image.getWidth();
            int imageHeight = image.getHeight();

            BufferedImage combined = new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_INT_ARGB);

            // paint logo image, preserving the alpha channels
            Graphics g = combined.createGraphics();
            g.drawImage(image, 0, 0, null);
            String demoString = "DEMO";

            int fontSize = imageWidth / demoString.length();
            Font font = new Font("Verdana", Font.CENTER_BASELINE, fontSize);

            //first rectangle for demo string
            g.setColor(Color.WHITE);
            FontMetrics fontMetrics = g.getFontMetrics(font);

            if (fontMetrics.getHeight() > imageHeight / 2) {
                font = font.deriveFont((float) imageHeight / 2);
                fontMetrics = g.getFontMetrics(font);
            }

            int xPos = imageWidth / 2 - fontMetrics.stringWidth(demoString) / 2;
            int yPos = imageHeight / 2 - fontMetrics.getHeight() / 2;

            g.fillRect(xPos, yPos, fontMetrics.stringWidth(demoString), fontMetrics.getHeight());

            //second background rectangle for assinareLink
            g.setFont(font);
            g.setColor(Color.BLACK);
            g.drawString(demoString, xPos, yPos + fontMetrics.getHeight() - fontMetrics.getHeight() / 4);

            return combined;
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }

        return new BufferedImage(DEFAULT_IMAGE_WIDTH, DEFAULT_IMAGE_HEIGHT, BufferedImage.TYPE_INT_ARGB);
    }

}
