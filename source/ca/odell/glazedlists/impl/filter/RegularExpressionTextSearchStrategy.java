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

    private Pattern pattern;

    public void setSubtext(String regex) {
        pattern = Pattern.compile(regex);
    }

    public int indexOf(String text) {
        int result = -1;
        Matcher matcher = pattern.matcher(text);
        if (matcher.matches())
            result = matcher.start();
        return result;
    }
}