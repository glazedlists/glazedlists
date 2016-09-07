/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package com.publicobject.misc.xml;

/**
 * A Converter is a simple interface to convert raw String data collected by a
 * {@link Parser} into a more usable, type-safe Object appropriate for use in
 * the Objects being created from the XML Document.
 *
 * <p>Common example include converting date strings to {@link java.util.Date}
 * objects and integer strings to {@link Integer} objects.
 *
 * @author James Lemieux
 */
public interface Converter<S,T> {
    /**
     * Convert the given <code>value</code> to a more appropriate, type-safe
     * value for use in the Objects being created from the XML Document.
     */
    public T convert(S value);
}