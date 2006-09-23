/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.impl.filter;

/**
 * This implementation of {@link TextSearchStrategy} matches a given text
 * against the prefix of a given string. If the given string starts with the
 * prefix, the index 0 is returned; otherwise -1 is returned.
 *
 * @author James Lemieux
 */
public class StartsWithCaseInsensitiveTextSearchStrategy extends AbstractTextSearchStrategy {

    /** One of two strategies for the indexOf method; one is optimized for single character matching */
    private IndexOfStrategy indexOfStrategy;

    /**
     * This method selects one of two strategies used by {@link #indexOf(String)}
     * when testing the prefix of a given string. One strategy is tuned for
     * performance in the special case of matching a single character prefix.
     *
     * @param subtext the String check for as the prefix in {@link #indexOf(String)}
     */
    public void setSubtext(String subtext) {
        if (subtext.length() == 1)
            this.indexOfStrategy = new SingleCharacterIndexOfStrategy(subtext.charAt(0));
        else
            this.indexOfStrategy = new MultiCharacterIndexOfStrategy(subtext);
    }

    /**
     * The nature of this TextSearchStrategy is to check only the first
     * characters of the given <code>text</code> against a known prefix.
     * Consequently, this method will only return <code>-1</code> in the case
     * of no match or <code>0</code> when the prefix does match.
     *
     * @param text the prefix to match
     * @return <code>0</code> if the prefix was matched; <code>-1</code> if it
     *      was not
     */
    public int indexOf(String text) {
        // ensure we are in a state to search the text
        if (this.indexOfStrategy == null)
            throw new IllegalStateException("setSubtext must be called with a valid value before this method can operate");

        return this.indexOfStrategy.indexOf(text);
    }

    /**
     * Implementations of this interface are used to provide the return value
     * for {@link StartsWithCaseInsensitiveTextSearchStrategy#indexOf}.
     */
    private interface IndexOfStrategy {
        public int indexOf(String text);
    }

    /**
     * This implementation of IndexOfStrategy is optimized for the case when
     * the prefix is precisely one character long.
     */
    private class SingleCharacterIndexOfStrategy implements IndexOfStrategy {
        /** The upper and lower case versions of the prefix to match. */
        private final char upperCase;
        private final char lowerCase;

        public SingleCharacterIndexOfStrategy(char c) {
            this.upperCase = Character.toUpperCase(c);
            this.lowerCase = Character.toLowerCase(c);
        }

        public int indexOf(String text) {
            // if the text is not long enough to match the subtext, bail early
            if (text.length() < 1)
                return -1;

            char c = map(text.charAt(0));

            return (c == this.upperCase || c == this.lowerCase) ? 0 : -1;
        }
    }

    /**
     * This implementation of IndexOfStrategy executes the normal case of
     * prefixes with length > 1.
     */
    private class MultiCharacterIndexOfStrategy implements IndexOfStrategy {
        /** The length of the subtext to locate. */
        private final int subtextLength;

        /** The array of characters comprising the subtext. */
        private char[] subtextCharsUpper;
        private char[] subtextCharsLower;

        public MultiCharacterIndexOfStrategy(String prefix) {
            // record the length of the prefix
            this.subtextLength = prefix.length();

            // extract the upper case version of the prefix into an easily accessible char[]
            this.subtextCharsUpper = prefix.toUpperCase().toCharArray();
            this.subtextCharsLower = prefix.toLowerCase().toCharArray();
        }

        public int indexOf(String text) {
            // if the text is not long enough to match the subtext, bail early
            if (text.length() < this.subtextLength)
                return -1;

            for (int i = 0; i < subtextLength; i++) {
                char c = map(text.charAt(i));

                if (this.subtextCharsLower[i] != c && this.subtextCharsUpper[i] != c)
                    return -1;
            }

            return 0;
        }
    }
}