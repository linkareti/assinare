package com.linkare.assinare.sign.swing;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.LayoutManager2;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author bnazare
 */
public class ScalingLayoutManager implements LayoutManager2 {
    
    private final Map<Component,Dimension> comptable = new HashMap<>();
    private final Dimension defaultConstraints = new Dimension(100, 100);

    @Override
    public void addLayoutComponent(String name, Component comp) {
        // NOOP
    }

    @Override
    public void removeLayoutComponent(Component comp) {
        // NOOP
    }

    @Override
    public Dimension preferredLayoutSize(Container parent) {
        Insets insets = parent.getInsets();
        int prefWidth = 0;
        int prefHeight = 0;

        for (Component child : parent.getComponents()) {
            Dimension childPrefSize = child.getPreferredSize();
            prefWidth = (int) Math.max(prefWidth, childPrefSize.getWidth());
            prefHeight = (int) Math.max(prefHeight, childPrefSize.getHeight());
        }

        return new Dimension(prefWidth + insets.left + insets.right, prefHeight + insets.bottom + insets.top);
    }

    @Override
    public Dimension minimumLayoutSize(Container parent) {
        Insets insets = parent.getInsets();
        int minWidth = 0;
        int minHeight = 0;

        for (Component child : parent.getComponents()) {
            Dimension childMinSize = child.getMinimumSize();
            minWidth = (int) Math.max(minWidth, childMinSize.getWidth());
            minHeight = (int) Math.max(minHeight, childMinSize.getHeight());
        }

        return new Dimension(minWidth + insets.left + insets.right, minHeight + insets.bottom + insets.top);
    }

    @Override
    public void layoutContainer(Container parent) {
        Insets insets = parent.getInsets();

        for (Component child : parent.getComponents()) {
            Dimension childPrefSize = getConstraints(child);

            double heightFactor = parent.getHeight() / childPrefSize.getHeight();
            double widthFactor = parent.getWidth() / childPrefSize.getWidth();

            if (childPrefSize.getWidth() * heightFactor < parent.getWidth()) {
                double x = (parent.getWidth() - (childPrefSize.getWidth() * heightFactor)) / 2;
                child.setBounds((int) x + insets.left, insets.top, (int) (childPrefSize.getWidth() * heightFactor) - insets.left - insets.right, (int) (parent.getHeight()) - insets.bottom - insets.top);
            } else {
                double y = (parent.getHeight() - (childPrefSize.getHeight() * widthFactor)) / 2;
                child.setBounds(insets.left, (int) y + insets.top, (int) (parent.getWidth()) - insets.left - insets.right, (int) (childPrefSize.getHeight() * widthFactor) - insets.bottom - insets.top);
            }
        }
    }

    @Override
    public void addLayoutComponent(Component comp, Object constraints) {
        if (constraints instanceof Dimension) {
            setConstraints(comp, (Dimension)constraints);
        } else if (constraints != null) {
            throw new IllegalArgumentException("cannot add to layout: constraints must be a Dimension");
        }
    }

    @Override
    public Dimension maximumLayoutSize(Container target) {
        return new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE);
    }

    @Override
    public float getLayoutAlignmentX(Container target) {
        return 0.5f;
    }

    @Override
    public float getLayoutAlignmentY(Container target) {
        return 0.5f;
    }

    @Override
    public void invalidateLayout(Container target) {
        // NOOP
    }
    
    public void setConstraints(Component comp, Dimension constraints) {
        comptable.put(comp, (Dimension)constraints.clone());
    }
    
    public Dimension getConstraints(Component comp) {
        Dimension constraints = comptable.get(comp);
        if (constraints == null) {
            setConstraints(comp, defaultConstraints);
            constraints = comptable.get(comp);
        }
        return (Dimension)constraints.clone();
    }

}
