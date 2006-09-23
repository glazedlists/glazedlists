/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.impl.filter;

/**
 * An abstract base class to ease the burden of implementing the
 * {@link TextSearchStrategy} interface. This base class provides the
 * {@link #map(char)} method which consults an internal character map to map
 * characters as necessary.
 *
 * <p>Some implementations may support mapping characters to other characters
 * in order to support fuzzy matching. For example, diacritical marks could be
 * stripped during the text search by mapping characters like 'é' to 'e'.
 *
 * @author James Lemieux
 */
abstract class AbstractTextSearchStrategy implements TextSearchStrategy {

    /**
     * The strategy for mapping each character immediately before it is
     * compared with a target character; <code>null</code> if no mapping
     * should be performed.
     */
    char[] characterMap;

    /**
     * Sets the strategy used to map characters immediately before they
     * are compared. For example, if the <code>characterMap</code>
     * converts 'é' to 'e', then it allows "resume" to match "résumé". By
     * plugging in arbitrary <code>characterMap</code> strategies, the
     * "fuzziness" of text matches can be controlled.
     */
    public void setCharacterMap(char[] characterMap) {
        this.characterMap = characterMap;
    }

    /**
     * A convenience method to map the given character if a character map has
     * been specified. If either a character map does not exist, or the
     * character map does not define a mapping for <code>c</code>, then
     * <code>c</code> is returned unchanged.
     *
     * @param c the character to be mapped
     * @return the character <code>c</code> is mapped to, or <code>c</code> if
     *      no mapping exists
     */
    char map(char c) {
        return characterMap != null && c < characterMap.length ? characterMap[c] : c;
    }
}