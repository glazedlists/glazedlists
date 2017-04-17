/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package com.publicobject.misc.swing;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Factory method for a handful of icons.
 *
 * @author <a href="mailto:jesse@swank.ca">Jesse Wilson</a>
 */
public class Icons {

    /**
     * An 'x' icon.
     */
    public static Icon x(int size, int fraction, Color foreground) {
        return new X(size, fraction, foreground);
    }

    /**
     * A '+' icon.
     */
    public static Icon plus(int size, int width, Color foreground) {
        return new Plus(size, width, foreground);
    }

    /**
     * A triangle icon.
     */
    public static Icon triangle(int size, int direction, Color foreground) {
        return new Triangle(size, direction, foreground);
    }

    /** all icons are drawn in high quality */
    public static final Map<RenderingHints.Key,Object> RENDERING_HINTS = new HashMap<RenderingHints.Key,Object>();
    static {
        RENDERING_HINTS.put(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    }

    /**
     * Implement the 'X' icon.
     */
    private static final class X implements Icon {
        private final int size;
        private final int fraction;
        private final Color foreground;

        public X(int size, int fraction, Color foreground) {
            this.size = size;
            this.fraction = fraction;
            this.foreground = foreground;
        }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2d = (Graphics2D)g.create();
            g2d.addRenderingHints(RENDERING_HINTS);
            g2d.setColor(foreground);

            int oneSixth = size / fraction;
            int fiveSixths = size - oneSixth;
            g2d.fillPolygon(
                new int[] { x + oneSixth, x + size, x + fiveSixths, x },
                new int[] { y, y + fiveSixths, y + size, y + oneSixth },
                4
            );
            g2d.fillPolygon(
                new int[] { x, x + fiveSixths, x + size, x + oneSixth },
                new int[] { y + fiveSixths, y, y + oneSixth, y + size },
                4
            );
        }

        @Override
        public int getIconWidth() { return size; }
        @Override
        public int getIconHeight() { return size; }
    }

    /**
     * Implement the '+' icon.
     */
    private static final class Plus implements Icon {
        private final int size;
        private final int width;
        private final Color foreground;

        public Plus(int size, int width, Color foreground) {
            this.size = size;
            this.width = width;
            this.foreground = foreground;
        }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2d = (Graphics2D)g.create();
            g2d.addRenderingHints(RENDERING_HINTS);
            g2d.setColor(foreground);
            int left = (size - width) / 2;
            g2d.fillRect(x + left, y, width, width + 2 * left);
            g2d.fillRect(x, y + left, width + 2 * left, width);
        }

        @Override
        public int getIconWidth() { return size; }
        @Override
        public int getIconHeight() { return size; }
    }

    /**
     * Implement the triangle icon.
     */
    private static final class Triangle implements Icon {
        private final int size;
        private final int direction;
        private final Color foreground;

        public Triangle(int size, int direction, Color foreground) {
            this.size = size;
            this.direction = direction;
            this.foreground = foreground;
        }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2d = (Graphics2D)g.create();
            g2d.addRenderingHints(RENDERING_HINTS);
            g2d.setColor(foreground);
            int center = size / 2;
            int[] xPoints;
            int[] yPoints;
            if(direction == SwingConstants.NORTH) {
                xPoints = new int[] { x, x + center, x + center * 2 };
                yPoints = new int[] { y + center * 2, y, y + center * 2 };
            } else if(direction == SwingConstants.SOUTH) {
                xPoints = new int[] { x, x + center, x + center * 2 };
                yPoints = new int[] { y, y + center * 2, y };
            } else if(direction == SwingConstants.WEST) {
                xPoints = new int[] { x + center * 2, x, x + center * 2 };
                yPoints = new int[] { y, y + center, y + center * 2 };
            } else if(direction == SwingConstants.EAST) {
                xPoints = new int[] { x, x + center * 2, x, x };
                yPoints = new int[] { y, y + center, y + center * 2 };
            } else {
                throw new IllegalStateException();
            }

            g2d.fillPolygon(xPoints, yPoints, 3);
        }

        @Override
        public int getIconWidth() { return size; }
        @Override
        public int getIconHeight() { return size; }
    }


    /** test the icons */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new TestRunnable());
    }

    private static class TestRunnable implements Runnable {
        @Override
        public void run() {
            JPanel panel = new JPanel(new FlowLayout());
            for(int size = 10; size <= 15; size += 1) {
                for(int fraction = 10; fraction >= 5; fraction--) {
                    JLabel label = new JLabel("" + size, x(size, fraction, Color.BLACK), JLabel.CENTER);
                    label.setHorizontalTextPosition(JLabel.CENTER);
                    label.setVerticalTextPosition(JLabel.BOTTOM);
                    panel.add(label);
                }
            }

            for(int size = 10; size <= 15; size += 1) {
                for(int width = 1; width <= 5; width++) {
                    JLabel label = new JLabel("" + size, plus(size, width, Color.BLACK), JLabel.CENTER);
                    label.setHorizontalTextPosition(JLabel.CENTER);
                    label.setVerticalTextPosition(JLabel.BOTTOM);
                    panel.add(label);
                }
            }

            int[] directions = new int[] { SwingConstants.NORTH, SwingConstants.EAST, SwingConstants.SOUTH, SwingConstants.WEST };
            for(int size = 10; size <= 15; size += 1) {
                for(int d = 0; d < directions.length; d++) {
                    JLabel label = new JLabel("" + size, triangle(size, directions[d], Color.BLACK), JLabel.CENTER);
                    label.setHorizontalTextPosition(JLabel.CENTER);
                    label.setVerticalTextPosition(JLabel.BOTTOM);
                    panel.add(label);
                }
            }

            JFrame frame = new JFrame();
            frame.getContentPane().add(panel);
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setVisible(true);
        }
    }
}