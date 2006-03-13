/* Glazed Lists                                                 (c) 2003-2005 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.impl.adt.barcode2;

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Try to make conversions and color operations as efficient as possible
 * by using bytes as values rather than full-size objects. This exploits
 * a limitation that there's at most 8 possible values in the list of colors.
 *
 * @author <a href="mailto:jesse@swank.ca">Jesse Wilson</a>
 */
public class ListToByteCoder<C> {
    private final List<C> allColors;
    private final int colorCount;

    public ListToByteCoder(List<C> allColors) {
        if(allColors.size() > 8) throw new IllegalArgumentException("Max 8 colors!");
        this.allColors = Collections.unmodifiableList(new ArrayList<C>(allColors));
        this.colorCount = this.allColors.size();
    }

    /**
     * List the colors encoded by this coder.
     */
    public List<C> getColors() {
        return allColors;
    }

    /**
     * Encode the specified list of colors into a byte.
     */
    public byte colorsToByte(List<C> colors) {
        int result = 0;
        for(int i = 0; i < colors.size(); i++) {
            C color = colors.get(i);
            int index = allColors.indexOf(color);
            result = result | (1 << index);
        }
        return (byte)result;
    }

    /**
     * Encode the specified color into a byte.
     */
    public byte colorToByte(C color) {
        int index = allColors.indexOf(color);
        int result = (1 << index);
        return (byte)result;
    }

    /**
     * Decode the specified byte into a color.
     */
    public C byteToColor(byte encoded) {
        for(int i = 0; i < colorCount; i++) {
            if(((1 << i) & encoded) > 0) return allColors.get(i);
        }
        throw new IllegalStateException();
    }

    /**
     * Decode the specified bytes into colors.
     */
    public List<C> byteToColors(byte encoded) {
        List<C> result = new ArrayList<C>(colorCount);
        for(int i = 0; i < colorCount; i++) {
            if(((1 << i) & encoded) > 0) result.add(allColors.get(i));
        }
        return result;
    }
}