/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package com.publicobject.misc.swing;

import javax.swing.*;
import java.awt.*;

/**
 * A customized panel which paints a color gradient for its background
 * rather than a single color. The start and end colors of the gradient
 * are specified via the constructor.
 */
public class GradientPanel extends JPanel {
    private Color gradientStartColor;
    private Color gradientEndColor;
    private boolean vertical;

    public GradientPanel(Color gradientStartColor, Color gradientEndColor, boolean vertical) {
        this.gradientStartColor = gradientStartColor;
        this.gradientEndColor = gradientEndColor;
        this.vertical = vertical;
    }

    public void paintComponent(Graphics g) {
        if (this.isOpaque())
            paintGradient((Graphics2D) g, this.gradientStartColor, this.gradientEndColor, vertical ? this.getHeight() : this.getWidth(), vertical);
    }

    /**
     * A convenience method to paint a gradient between <code>gradientStartColor</code>
     * and <code>gradientEndColor</code> over <code>length</code> pixels.
     */
    private static void paintGradient(Graphics2D g2d, Color gradientStartColor, Color gradientEndColor, int length, boolean vertical) {
        final Paint oldPainter = g2d.getPaint();
        try {
            if(vertical) g2d.setPaint(new GradientPaint(0, 0, gradientStartColor, 0, length, gradientEndColor));
            else g2d.setPaint(new GradientPaint(0, 0, gradientStartColor, length, 0, gradientEndColor));
            g2d.fill(g2d.getClip());
        } finally {
            g2d.setPaint(oldPainter);
        }
    }
}