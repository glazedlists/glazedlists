/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.impl.filter;

import ca.odell.glazedlists.util.text.CharacterNormalizer;

/**
 * An abstract base class to ease the burden of implementing the
 * {@link TextSearchStrategy} interface.
 *
 * @author James Lemieux
 */
abstract class AbstractTextSearchStrategy implements TextSearchStrategy {

    /**
     * The strategy for normalizing each character immediately before
     * it is compared with a target character; <code>null</code> if no
     * normalization is required.
     */
    CharacterNormalizer characterNormalizer;

    /**
     * Sets the strategy used to normalize characters immediately before they
     * are compared. For example, if the <code>characterNormalizer</code>
     * converts 'é' to 'e', then it allows "resume" to match "résumé". By
     * plugging in arbitrary <code>CharacterNormalizer</code> strategies, one
     * can control the "fuzziness" of their text matches.
     */
    public void setCharacterNormalizer(CharacterNormalizer characterNormalizer) {
        this.characterNormalizer = characterNormalizer;
    }
}