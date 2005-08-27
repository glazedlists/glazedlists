/* Glazed Lists                                                 (c) 2003-2005 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.impl.filter;

/**
 * This implementation of <code>TextSearchStrategy</code> searches the
 * source text for a single character - the first character of the given
 * subtext. This implementation is optimized for use when the subtext is
 * precisely 1 character.
 *
 * @author James Lemieux
 */
public class SingleCharacterCaseInsensitiveTextSearchStrategy implements TextSearchStrategy {

    /** The single character to locate. */
    private char subtextChar;

    /** <tt>true</tt> if subtext has been set; <tt>false</tt> otherwise. */
    private boolean subtextInitialized = false;

    /** caching upper case characters has shown a 5% performance boost over Character.toUpperCase() */
    private static final char[] UPPER_CASE_CACHE = new char[256];
    static {
        for(char c = 0; c < UPPER_CASE_CACHE.length; c++) {
            UPPER_CASE_CACHE[c] = Character.toUpperCase(c);
        }
    }

    /**
     * Sets the subtext to locate found when {@link #indexOf(String)} is called.
     * This
     *
     * @param subtext the String containing the single character to locate in
     *      {@link #indexOf(String)}
     * @throws IllegalArgumentException if <code>subtext</code> is
     *      <code>null</code> or does not contain precisely <code>1</code>
     *      character
     */
    public void setSubtext(String subtext) {
        if (subtext == null) throw new IllegalArgumentException("subtext may not be null");
        if (subtext.length() != 1) throw new IllegalArgumentException("subtext (" + subtext + ") must contain a single character");

        this.subtextChar = toUpperCase(subtext.charAt(0));
        this.subtextInitialized = true;
    }

    /** {@inheritDoc} */
    public int indexOf(String text) {
        // ensure we are in a state to search the text
        if(!this.subtextInitialized) throw new IllegalStateException("setSubtext must be called with a valid value before this method can operate");

        // search for subtextChar in the given text
        for(int c = 0; c < text.length(); c++) {
            if(toUpperCase(text.charAt(c)) == this.subtextChar) {
                return c;
            }
        }

        // we didn't find the subtextChar so return -1
        return -1;
    }

    /**
     * Our superfast toUpperCase method only works when a character is in the base
     * 256 characters. Those values are precalculated because Character.toUpperCase()
     * has proven to be a bottleneck.
     *
     * @see <a href="https://glazedlists.dev.java.net/issues/show_bug.cgi?id=136">Issue 136</a>
     */
    private static char toUpperCase(char anyCase) {
        return anyCase < UPPER_CASE_CACHE.length ? UPPER_CASE_CACHE[anyCase] : Character.toUpperCase(anyCase);
    }
}