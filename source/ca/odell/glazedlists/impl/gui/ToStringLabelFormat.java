/**
 * Glazed Lists
 * http://glazedlists.dev.java.net/
 *
 * COPYRIGHT 2003 O'DELL ENGINEERING LTD.
 */
package ca.odell.glazedlists.impl.gui;

// to implement the LabelFormat interface
import ca.odell.glazedlists.gui.LabelFormat;

/**
 * Provides simple label formatting where each element's label will be
 * formatted as the value obtained by calling toString() on the source Object.
 *
 * @author <a href="mailto:kevin@swank.ca">Kevin Maltby</a>
 */
public final class ToStringLabelFormat implements LabelFormat {

    /**
     * Gets the toString() value for a particular element.
     */
    public String getText(Object element) {
        return element.toString();
    }
}