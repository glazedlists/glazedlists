/*             Glazed Lists  http://publicobject.com/glazedlists/             */                        
/*        Copyright 2003-2005 publicobject.com, O'Dell Engineering Ltd.       */
package ca.odell.glazedlists.impl.filter;

import java.util.Arrays;

/**
 * This implementation of {@link TextSearchStrategy} implements a
 * simple version of the Boyer-Moore text searching algorithm, generally
 * considered to be the fastest known text searching algorithm.
 *
 * @author James Lemieux
 */
public class BoyerMooreCaseInsensitiveTextSearchStrategy implements TextSearchStrategy {

    /** The length of the subtext to locate. */
    private int subtextLength;

    /** The last index in the subtext */
    private int lastSubtextIndex;

    /** The array of characters comprising the subtext. */
    private char[] subtextChars;

    /** The Boyer-Moore shift table reduced to only 256 elements rather than all 65,536 Unicode characters. */
    private int[] shiftTable = new int[256];

    /**
     * This method builds a shortened version of the Boyer-Moore shift table.
     * The shift table normally contains an entry for each letter in the
     * text search alphabet. Since this search strategy covers all valid
     * Unicode characters, the shift table would normally contain 65,536
     * entries. US-ASCII comprises 99% of the alphabet used for most searched
     * messages, so we exploit this fact by reducing our shift table to just
     * 256 entries to save on initialization time. We convert each character to
     * its shift table index by modding it by 256. This creates the possibility
     * of shift table collisions since more than one Unicode character will map
     * to the same integer in the range [0..255]. Note however, that this only
     * causes the Boyer-Moore algorithm to run suboptimally, not incorrectly.
     * Since the number of collisions encountered is low in practice (based on
     * our assumption that 99% of the alphabet used for most search messages is
     * US-ASCII), it should have little effect on the execution time of
     * {@link #indexOf(String)}.
     *
     * @param subtext the String to locate in {@link #indexOf(String)}
     */
    public void setSubtext(String subtext) {
        // record the length of the subtext
        this.subtextLength = subtext.length();

        // record the last index of the subtext
        this.lastSubtextIndex = this.subtextLength-1;

        // extract the upper case version of the subtext into an easily accessible char[]
        this.subtextChars = subtext.toUpperCase().toCharArray();

        // initialize the shift table with the maximum shift -> the length of the subtext
        Arrays.fill(this.shiftTable, 0, this.shiftTable.length, this.subtextLength);

        // for each character in the subtext, calculate its maximum safe shift distance
        for(int i = 0; i < this.lastSubtextIndex; i++) {
            this.shiftTable[this.subtextChars[i] % 256] = this.lastSubtextIndex - i;
        }
    }

    /** {@inheritDoc} */
    public int indexOf(String text) {
        // ensure we are in a state to search the text
        if(this.subtextChars == null) {
            throw new IllegalStateException("setSubtext must be called with a valid value before this method can operate");
        }

        // initialize some variables modified within the text search loop
        int textPosition = this.lastSubtextIndex;
        char textChar = ' ';
        int subtextPosition;
        int textLength = text.length();

        // search through text until the textPosition exceeds the textLength
        while(textPosition < textLength) {
            // reset the comparison position within the subtext to the END of the subtext
            subtextPosition = this.lastSubtextIndex;

            if(subtextPosition >= 0) {
                // locate the character in the text to be compared against
                textChar = Character.toUpperCase(text.charAt(textPosition));

                // check for matching character from the end to the beginning of the subtext
                while(subtextPosition >= 0 && this.subtextChars[subtextPosition] == textChar) {
                    // the text char and subtext char matched, so shift both positions left and recompare
                    subtextPosition--;
                    textPosition--;

                    // calculate the next character of the text to compare
                    if(textPosition != -1) {
                        textChar = Character.toUpperCase(text.charAt(textPosition));
                    }
                }
            }

            // subtextPosition == -1 indicates we have successfully matched the
            // entire subtext from last char to first char so return the
            // matching index
            if(subtextPosition == -1) {
                return textPosition + 1;
            }

            // otherwise we had a mismatch, so calculate the maximum safe shift
            // and move ahead to the next search position in the text
            textPosition += Math.max(this.shiftTable[textChar % 256], this.subtextLength-subtextPosition);
        }

        // if we fall out of the search loop then we couldn't find the subtext
        return -1;
    }
}