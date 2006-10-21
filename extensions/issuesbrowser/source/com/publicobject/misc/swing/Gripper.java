/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package com.publicobject.misc.swing;

import ca.odell.glazedlists.impl.GlazedListsImpl;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.IndexColorModel;

/**
 * Implements a bumpy gripper like the one found in the Metal Look and Feel.
 *
 * @author James Lemieux
 */
public class Gripper implements Icon {

    static final Color ALPHA = new Color(0, 0, 0, 0);

    protected int xBumps;
    protected int yBumps;
    protected Color topColor;
    protected Color shadowColor;
    protected Color backColor;

    protected GripperBuffer buffer;

    /**
     * Creates a Gripper of the specified size with the specified colors.
     * If <code>newBackColor</code> is null, the background will be transparent.
     */
    public Gripper(int width, int height, Color newTopColor, Color newShadowColor, Color newBackColor) {
        xBumps = width / 2;
	    yBumps = height / 2;

        topColor = newTopColor;
	    shadowColor = newShadowColor;
        backColor = newBackColor == null ? ALPHA : newBackColor;
    }

    private GripperBuffer getBuffer(GraphicsConfiguration gc, Color aTopColor, Color aShadowColor, Color aBackColor) {
        if (buffer != null && buffer.hasSameConfiguration(gc, aTopColor, aShadowColor, aBackColor))
            return buffer;

        return new GripperBuffer(gc, topColor, shadowColor, backColor);
    }

    public void paintIcon(Component c, Graphics g, int x, int y) {
        GraphicsConfiguration gc = g instanceof Graphics2D ? (GraphicsConfiguration) ((Graphics2D) g).getDeviceConfiguration() : null;

        buffer = getBuffer(gc, topColor, shadowColor, backColor);

        int bufferWidth = buffer.getImageSize().width;
        int bufferHeight = buffer.getImageSize().height;
        int iconWidth = getIconWidth();
        int iconHeight = getIconHeight();
        int x2 = x + iconWidth;
        int y2 = y + iconHeight;

        for (; y < y2; y += bufferHeight) {
            int h = Math.min(y2 - y, bufferHeight);
            for (; x < x2; x += bufferWidth) {
                int w = Math.min(x2 - x, bufferWidth);
                g.drawImage(buffer.getImage(), x, y, x+w, y+h, 0, 0, w, h, null);
            }
        }
    }

    public int getIconWidth() {
        return xBumps * 2;
    }

    public int getIconHeight() {
        return yBumps * 2;
    }
}

class GripperBuffer {

    static final int IMAGE_SIZE = 64;
    static Dimension imageSize = new Dimension( IMAGE_SIZE, IMAGE_SIZE );

    transient Image image;
    Color topColor;
    Color shadowColor;
    Color backColor;
    private GraphicsConfiguration gc;

    public GripperBuffer(GraphicsConfiguration gc, Color aTopColor, Color aShadowColor, Color aBackColor) {
        this.gc = gc;
        topColor = aTopColor;
        shadowColor = aShadowColor;
        backColor = aBackColor;
        createImage();
        fillBumpBuffer();
    }

    public boolean hasSameConfiguration(GraphicsConfiguration gc, Color top, Color shadow, Color back) {
        if (!GlazedListsImpl.equal(this.gc, gc)) return false;

	    return topColor.equals(top) && shadowColor.equals(shadow) && backColor.equals(back);
    }

    /**
     * Returns the Image containing the bumps appropriate for the passed in
     * <code>GraphicsConfiguration</code>.
     */
    public Image getImage() {
        return image;
    }

    public Dimension getImageSize() {
        return imageSize;
    }

    /**
     * Paints the bumps into the current image.
     */
    private void fillBumpBuffer() {
        Graphics g = image.getGraphics();

        g.setColor(backColor);
        g.fillRect(0, 0, IMAGE_SIZE, IMAGE_SIZE);

        g.setColor(topColor);
        for (int x = 0; x < IMAGE_SIZE; x+=4) {
            for (int y = 0; y < IMAGE_SIZE; y+=4) {
                g.drawLine(x, y, x, y);
                g.drawLine(x+2, y+2, x+2, y+2);
            }
        }

        g.setColor(shadowColor);
        for (int x = 0; x < IMAGE_SIZE; x+=4) {
            for (int y = 0; y < IMAGE_SIZE; y+=4) {
                g.drawLine( x+1, y+1, x+1, y+1 );
                g.drawLine( x+3, y+3, x+3, y+3);
            }
        }

        g.dispose();
    }

    /**
     * Creates the image appropriate for the passed in
     * <code>GraphicsConfiguration</code>, which may be null.
     */
    private void createImage() {
        if (gc != null) {
            final int transparency = backColor != Gripper.ALPHA ? Transparency.OPAQUE : Transparency.BITMASK;
            image = gc.createCompatibleImage(IMAGE_SIZE, IMAGE_SIZE, transparency);

        } else {
            int cmap[] = {backColor.getRGB(), topColor.getRGB(), shadowColor.getRGB()};
            IndexColorModel icm = new IndexColorModel(8, 3, cmap, 0, false, backColor == Gripper.ALPHA ? 0 : -1, DataBuffer.TYPE_BYTE);
            image = new BufferedImage(IMAGE_SIZE, IMAGE_SIZE, BufferedImage.TYPE_BYTE_INDEXED, icm);
        }
    }
}