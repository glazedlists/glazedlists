/* Glazed Lists                                                 (c) 2003-2005 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package com.publicobject.misc.swing;

import javax.swing.border.Border;
import javax.swing.*;
import java.awt.*;

/**
 * A border with cute rounded edges.
 * 
 * @author <a href="mailto:jesse@swank.ca">Jesse Wilson</a>
 */
public class RoundedBorder implements Border {

    private Color background;
    private Color outline;
    private Color foreground;
    private int radius;
    private int stroke;

    public RoundedBorder(Color background, Color outline, Color foreground, int radius, int stroke) {
        this.background = background;
        this.outline = outline;
        this.foreground = foreground;
        this.radius = radius;
        this.stroke = stroke;
    }

    public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
        Graphics2D g2d = (Graphics2D)g.create();
        g2d.addRenderingHints(Icons.RENDERING_HINTS);
        int diameter = this.radius * 2;

        // background in corners
        g2d.setColor(background);
        g2d.fillRect(0,              0,                    radius, this.radius);
        g2d.fillRect(width - radius, 0,                    radius, radius);
        g2d.fillRect(0,              height - this.radius, radius, radius);
        g2d.fillRect(width - radius, height - radius,      radius, radius);

        // fill corners
        int arcLeft = 0;
        int arcTop = 0;
        int arcRight = width - diameter - 1;
        int arcBottom = height - diameter - 1;
        int arcDiameter = diameter;
        int sideWidth = width - diameter; // + 1;
        int sideHeight = height - diameter; // + 1;
        g2d.setColor(foreground);
        g2d.fillArc(arcLeft,  arcTop,    arcDiameter, arcDiameter, 90, 90);
        g2d.fillArc(arcRight, arcTop,    arcDiameter, arcDiameter, 0, 90);
        g2d.fillArc(arcLeft,  arcBottom, arcDiameter, arcDiameter, 180, 90);
        g2d.fillArc(arcRight, arcBottom, arcDiameter, arcDiameter, 270, 90);
        // fill sides
        g2d.fillRect(radius,         0,               sideWidth, radius);
        g2d.fillRect(radius,         height - radius, sideWidth, radius);
        g2d.fillRect(0,              radius,          radius,    sideHeight);
        g2d.fillRect(width - radius, radius,          radius,    sideHeight);

        // prepare the arc lines
        if(stroke > 0) {
            g2d.setColor(outline);
            g2d.setStroke(new BasicStroke(stroke));
            int halfStroke = (stroke) / 2;

            // stroke corners
            int strokeDiameter = diameter - stroke;
            int leftStroke = halfStroke;
            int rightStroke = width - diameter + halfStroke;
            int topStroke = halfStroke;
            int bottomStroke = height - diameter + halfStroke;
            g2d.drawArc(leftStroke,  topStroke,    strokeDiameter, strokeDiameter, 90, 90);
            g2d.drawArc(rightStroke, topStroke,    strokeDiameter, strokeDiameter, 0,  90);
            g2d.drawArc(leftStroke,  bottomStroke, strokeDiameter, strokeDiameter, 180, 90);
            g2d.drawArc(rightStroke, bottomStroke, strokeDiameter, strokeDiameter, 270, 90);

            // stroke sides
            int sideBottom = height - stroke;
            int sideTop = 0;
            int sideLeft = 0;
            int sideRight = width - stroke;
            g2d.fillRect(radius,    sideTop,    sideWidth, stroke);
            g2d.fillRect(radius,    sideBottom, sideWidth, stroke);
            g2d.fillRect(sideLeft,  radius,     stroke,    sideHeight);
            g2d.fillRect(sideRight, radius,     stroke,    sideHeight);
        }
    }

    public Insets getBorderInsets(Component c) {
        return new Insets(radius, radius, radius, radius);
    }

    public boolean isBorderOpaque() {
        return false;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new TestRunnable());
    }

    private static class TestRunnable implements Runnable { 
        public void run() {
            JPanel panel = new JPanel(new FlowLayout());
            final Color background = new Color(204, 204, 255);
            final Color border = Color.BLACK;
            final Color foreground = Color.WHITE;
            panel.setBackground(background);

            for(int p = 2; p < 20; p++) {
                for(int s = 0; s <= p; s++) {
                    JPanel cell = new JPanel();
                    cell.add(new JLabel("<html><i>radius</i>: " + p + "<br><i>stroke</i>: " + s));
                    cell.setBorder(new RoundedBorder(background, border, foreground, p, s));
                    cell.setBackground(Color.WHITE);
                    panel.add(cell);
                }
            }

            JFrame frame = new JFrame();
            frame.getContentPane().add(panel);
            frame.setSize(640, 480);
            frame.setLocationRelativeTo(null);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setVisible(true);
        }
    }
}