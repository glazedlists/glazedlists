/*
 * Glazed Lists
 * http://glazedlists.dev.java.net/
 *
 * COPYRIGHT 2003 O'DELL ENGINEERING LTD.
 */
package ca.odell.glazedlists.gui;

/**
 * Specifies how to format labels for Objects that will be displayed in
 * List or ComboBox widgets.
 *
 * @author <a href="mailto:kevin@swank.ca">Kevin Maltby</a>
 */
public interface LabelFormat {

    /**
     * Gets the formatted text value that represents a particular element.
     */
    public String getText(Object element);

}