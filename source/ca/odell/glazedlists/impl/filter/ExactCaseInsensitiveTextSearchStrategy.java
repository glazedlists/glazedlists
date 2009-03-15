/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.impl.filter;

/**
 * This implementation of {@link TextSearchStrategy} matches a given text
 * against an exact subtext. If the subtext matches the given text character
 * for character it is considered a match and the index of 0 is returned. If
 * there is no match, -1 is returned.
 *
 * @author James Lemieux
 */
public class ExactCaseInsensitiveTextSearchStrategy extends StartsWithCaseInsensitiveTextSearchStrategy {

    private int subtextLength;

    @Override
    public void setSubtext(String subtext) {
        super.setSubtext(subtext);
        this.subtextLength = subtext.length();
    }

    @Override
    public int indexOf(String text) {
        if (text.length() != subtextLength)
            return -1;

        return super.indexOf(text);
    }
}