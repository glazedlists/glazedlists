package com.publicobject.misc.swing;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.geom.Rectangle2D;
import java.util.Map;
import java.util.HashMap;

/**
 * Draw a clock as a progress bar.
 *
 * @author <a href="mailto:jesse@swank.ca">Jesse Wilson</a>
 */
public class ProgressClock extends JComponent {

    private final Image before;
    private final Image after;
    private final Paint foregroundPaint;

    private final int width;
    private final int height;

    private float progress = 0.0f;

    public ProgressClock(Image before, Image after) {
        this.before = before;
        this.after = after;

        this.width = before.getWidth(null);
        this.height = before.getHeight(null);

        Rectangle bounds = new Rectangle(width, height);

        BufferedImage afterBuffered = new BufferedImage(width, height, BufferedImage.TYPE_4BYTE_ABGR);
        afterBuffered.getGraphics().drawImage(after, 0, 0, null);
        foregroundPaint = new TexturePaint(afterBuffered, bounds);
    }

    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D)g.create();

        g2d.drawImage(before, 0, 0, null);
        g2d.setClip(0, 0, width, height);
        if(progress > 0f) {
            int arcAngle = (int)(360 * progress);
            g2d.setPaint(foregroundPaint);
            g2d.fillArc(-width, -height, width * 3, height * 3, 90 - arcAngle, arcAngle);
        }
    }

    private void setProgress(float progress) {
        this.progress = progress;
        repaint();
    }

    public Dimension getPreferredSize() {
        return new Dimension(width, height);
    }

    public Dimension getMaximumSize() {
        return getPreferredSize();
    }

    public Dimension getMinimumSize() {
        return getPreferredSize();
    }

    public static void main(String[] args) throws Exception {

        final ProgressClock[] progressClock = new ProgressClock[1];

        SwingUtilities.invokeAndWait(new Runnable() { public void run() {
            JPanel panel = new JPanel(new BorderLayout());
            Image before = new ImageIcon(getClass().getClassLoader().getResource("resources/before.png")).getImage();
            Image after = new ImageIcon(getClass().getClassLoader().getResource("resources/after.png")).getImage();

            progressClock[0] = new ProgressClock(before, after);
            progressClock[0].setProgress(0.75f);
            panel.add(progressClock[0]);

            JFrame frame = new JFrame();
            frame.getContentPane().add(panel);
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setVisible(true);
        }});

        Object wait = new Object();
        synchronized(wait) {
            while(true) {
                for(int i = 0; i <= 100; i++) {
                    progressClock[0].setProgress(i / 100f);
                    wait.wait(100);
                }
                wait.wait(1000);
                progressClock[0].setProgress(0f);
                wait.wait(1000);
            }
        }
    }
}
