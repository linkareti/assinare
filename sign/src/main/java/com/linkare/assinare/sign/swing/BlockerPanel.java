package com.linkare.assinare.sign.swing;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.event.KeyAdapter;
import java.awt.event.MouseAdapter;

import javax.swing.JComponent;
import javax.swing.UIManager;

/**
 *
 * @author bnazare
 */
public class BlockerPanel extends JComponent {

    private static final String PANEL_BACKGROUND_KEY = "Panel.background";
    
    public BlockerPanel() {
        super();
        
        setOpaque(false);
        
        Color bg = UIManager.getColor(PANEL_BACKGROUND_KEY);
        setBackground(new Color(bg.getRed(), bg.getGreen(), bg.getBlue(), 127));
        
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        
        setFocusable(true);
        blockInputEvents();
    }

    private void blockInputEvents() {
        addMouseListener(new MouseAdapter() {});
        addMouseMotionListener(new MouseAdapter() {});
        addMouseWheelListener(new MouseAdapter() {});
        
        addKeyListener(new KeyAdapter() {});
        
        setFocusTraversalKeysEnabled(false);
    }

    @Override
    protected void paintComponent(Graphics g) {
        g.setColor(getBackground());
        g.fillRect(0, 0, getSize().width, getSize().height);
    }

}
