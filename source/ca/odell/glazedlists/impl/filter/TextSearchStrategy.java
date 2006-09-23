/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.impl.filter;

/**
 * This interface defines a Strategy for locating a particular subtext within
 * another (typically larger) text. Implementations may make assumptions about
 * the texts in order to gain performance benefits. Users of this interface
 * <strong>must</strong> call {@link #setSubtext(String)} before
 * {@link #indexOf(String)} or indexOf will throw an
 * {@link IllegalStateException}.
 *
 * @author James Lemieux
 */
public interface TextSearchStrategy {

    /**
     * Sets the strategy, if any, to map the characters being compared
     * during a text search. A <code>null</code> normalizer indicates the raw
     * characters should be used during the text search.
     *
     * @param charMap the strategy to use when normalizing characters
     *    immediately before comparing them for equality
     */
    public void setCharacterMap(char[] charMap);

    /**
     * Sets the subtext to locate when {@link #indexOf(String)} is called.
     * Implementations should build any special data structures they may need
     * concerning the subtext in this method.
     *
     * @param subtext the String to locate in {@link #indexOf(String)}
     */
    public void setSubtext(String subtext);

    /**
     * Returns the index of the first occurrence of <code>subtext</code> within
     * <code>text</code>; or <code>-1</code> if <code>subtext</code> does not
     * occur within <code>text</code>. Implementations must throw an
     * {@link IllegalStateException} if {@link #setSubtext(String)} has
     * not yet been called.
     *
     * @param text String in which to locate <code>subtext</code>
     * @return the index of the first occurrence of <code>subtext</code> within
     *      <code>text</code>; or <code>-1</code>
     * @throws IllegalStateException if no subtext has been set
     */
    public int indexOf(String text);
}