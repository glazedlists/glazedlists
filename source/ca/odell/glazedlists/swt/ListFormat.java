/*
 * Glazed Lists
 * http://glazedlists.dev.java.net/
 *
 * COPYRIGHT 2003 O'DELL ENGINEERING LTD.
 */
package ca.odell.glazedlists.swt;

/**
 * Specifies how a set of elements will be formatted in a List widget.
 *
 * @author <a href="mailto:kevin@swank.ca">Kevin Maltby</a>
 */
public interface ListFormat {

    /**
     * Gets the formatted display value for a particular element.
     */
    public String getDisplayValue(Object element);

}