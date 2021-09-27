package com.linkare.assinare.sign.swing;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JPanel;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.rendering.PDFRenderer;

import com.linkare.assinare.sign.helpers.RectangleHelper;

/**
 *
 * @author bnazare
 */
public class PdfPreviewPanel extends JPanel {

    private static final long serialVersionUID = 2243347407115558365L;

    private static final Logger LOG = Logger.getLogger(PdfPreviewPanel.class.getName());

    private PDDocument doc;
    private PDFRenderer renderer;
    private int pageNum = 0;

    private Dimension cachedSize = null;
    private Image cachedImage;

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (renderer != null) {
            if (!getSize().equals(cachedSize)) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setBackground(Color.WHITE);

                PDRectangle cropBox = RectangleHelper.getRotatedCropBox(doc.getPage(pageNum));
                float scale = getHeight() / cropBox.getHeight();

                try {
                    cachedImage = renderer.renderImage(pageNum, scale);
                    cachedSize = getSize();

                    g.drawImage(cachedImage, 0, 0, null);
                } catch (IOException ex) {
                    LOG.log(Level.SEVERE, null, ex);
                }
            } else {
                g.drawImage(cachedImage, 0, 0, null);
            }
        }
    }

    public PDDocument getDoc() {
        return doc;
    }

    public void setDoc(PDDocument doc) {
        this.doc = doc;
        this.renderer = new PDFRenderer(doc);
    }

    public int getPageNum() {
        return pageNum;
    }

    public void setPageNum(int pageNum) {
        if (pageNum != this.pageNum) {
            clearCache();
        }
        this.pageNum = pageNum;
    }

    private void clearCache() {
        cachedSize = null;
        repaint();
    }

}
