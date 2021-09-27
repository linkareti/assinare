package com.linkare.assinare.sign.swing;

import static org.junit.jupiter.api.Assertions.*;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.IOException;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.junit.jupiter.api.Test;

/**
 * Tests {@link PdfPreviewPanel}. This test is WIP.
 *
 * @author bnazare
 */
public class PdfPreviewPanelTest {

    @Test
    public void testPdfPreviewPanel() throws IOException {
        Graphics g = new BufferedImage(100, 100, BufferedImage.TYPE_INT_ARGB).getGraphics();
        PdfPreviewPanel pdfPreviewPanel = new PdfPreviewPanel();
        pdfPreviewPanel.setLayout(null);
        pdfPreviewPanel.setSize(100, 100);
        pdfPreviewPanel.update(g);

        assertNull(pdfPreviewPanel.getDoc());
        assertEquals(0, pdfPreviewPanel.getPageNum());
        pdfPreviewPanel.setPageNum(0); // for king and coverage!!!
        assertEquals(0, pdfPreviewPanel.getPageNum());

        try (PDDocument doc = new PDDocument()) {
            doc.addPage(new PDPage());
            doc.addPage(new PDPage());
            pdfPreviewPanel.setDoc(doc);
            pdfPreviewPanel.update(g);
            assertSame(doc, pdfPreviewPanel.getDoc());

            pdfPreviewPanel.update(g); // draw image from cache

            pdfPreviewPanel.setPageNum(1);
            pdfPreviewPanel.update(g);
            assertEquals(1, pdfPreviewPanel.getPageNum());
        }
    }

}
