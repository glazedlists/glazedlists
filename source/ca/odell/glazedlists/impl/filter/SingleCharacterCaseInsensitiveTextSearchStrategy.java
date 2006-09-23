/* Glazed Lists                                                 (c) 2003-2006 */
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
public class SingleCharacterCaseInsensitiveTextSearchStrategy extends AbstractTextSearchStrategy {

    /** The single character to locate. */
    private char subtextCharLower;
    private char subtextCharUpper;

    /** <tt>true</tt> if subtext has been set; <tt>false</tt> otherwise. */
    private boolean subtextInitialized = false;

    /**
     * Sets the subtext to locate found when {@link #indexOf(String)} is called.
     * This method is expected to be called with a String of length 1.
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

        final char c = subtext.charAt(0);
        this.subtextCharLower = Character.toLowerCase(c);
        this.subtextCharUpper = Character.toUpperCase(c);
        this.subtextInitialized = true;
    }

    /** {@inheritDoc} */
    public int indexOf(String text) {
        // ensure we are in a state to search the text
        if(!this.subtextInitialized) throw new IllegalStateException("setSubtext must be called with a valid value before this method can operate");

        char firstChar;
        // search for subtextChar in the given text
        for(int i = 0; i < text.length(); i++) {
            firstChar = map(text.charAt(i));

            if(firstChar == this.subtextCharLower || firstChar == this.subtextCharUpper) {
                return i;
            }
        }

        // we didn't find the subtextChar so return -1
        return -1;
    }
}