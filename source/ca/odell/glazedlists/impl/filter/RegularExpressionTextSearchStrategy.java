/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.impl.filter;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

/**
 * This implementation of {@link TextSearchStrategy} matches a given text
 * against a regular expression. If the regular expression matches, the start
 * position of the match is returned. If there is no match, -1 is returned.
 *
 * @author Wim Deblauwe
 */
public class RegularExpressionTextSearchStrategy extends AbstractTextSearchStrategy {

    private Matcher matcher;

    public void setSubtext(String regex) {
        matcher = Pattern.compile(regex).matcher("");
    }

    public int indexOf(String text) {
        return matcher.reset(text).matches() ? matcher.start() : -1;
    }
}