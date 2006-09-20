/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.util.text;

/**
 * When users wish to locate text in non-native languages, they often do not
 * understand the rules or symbols of the foreign language they are searching.
 * Character Normalization is the process of stripping away lesser marks and
 * symbols (called diacritics) to provide a reasonable base on which to perform
 * text searching.
 *
 * <p>For example, English users searching French text may want to type
 * "resume" and have it match the French word "résumé". In order to accomplish
 * this, a <code>CharacterNormalizer</code> is used to remove the accents from
 * the é characters.
 *
 * <p>For an introduction to unicode normalization, go
 * <a href="http://www.unicode.org/reports/tr15/">here</a>.
 *
 * @author James Lemieux
 */
public interface CharacterNormalizer {

    /**
     * Optionally normalize the given character to another character for the
     * purpose of controlling when characters match within a text search.
     *
     * @param c the character to be normalized
     * @return the normalized version of <code>c</code>, which can be any character
     */
    public char normalize(char c);
}