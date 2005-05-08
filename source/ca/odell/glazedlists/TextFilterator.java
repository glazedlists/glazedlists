/* Glazed Lists                                                 (c) 2003-2005 */
/* http://publicobject.com/glazedlists/                      publicboject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists;

import java.util.List;

/**
 * An utility class that can get a list of Strings for a given object
 * for testing whether a filter matches.
 *
 * @see <a href="http://publicobject.com/glazedlists/tutorial-0.9.1/">Glazed
 * Lists Tutorial</a>
 * @see GlazedLists#textFilterator(String[])
 *
 * @author <a href="mailto:jesse@odel.on.ca">Jesse Wilson</a>
 */
public interface TextFilterator {

    /**
     * Gets the specified object as a list of Strings. These Strings
     * should contain all object information so that it can be compared
     * to the filter set.
     *
     * @param baseList a list that the implementor shall add their filter
     *      strings to via <code>baseList.add()</code>. This may be a non-empty
     *      List and it is an error to call any method other than add().
     * @param element the object to extract the filter strings from.
     */
    public void getFilterStrings(List baseList, Object element);
}