/*             Glazed Lists  http://publicobject.com/glazedlists/             */                        
/*        Copyright 2003-2005 publicobject.com, O'Dell Engineering Ltd.       */
package ca.odell.glazedlists.impl.filter;

/**
 * This is the old implementation of
 * <code>TextFilterList.caseInsensitiveIndexOf</code> which I made to implement
 * the {@link TextSearchStrategy} interface so it could easily be compared with the new
 * {@link TextSearchStrategy} implementations.
 *
 * @author James Lemieux
 */
public class OldCaseInsensitiveTextSearchStrategy implements TextSearchStrategy {

    private String filter;

    /** {@inheritDoc} */
    public void setSubtext(String subtext) {
        this.filter = subtext.toUpperCase();
    }

    /** {@inheritDoc} */
    public int indexOf(String host) {
        // Tests if one String contains the other. Originally this task was performed
        // by Java's regular expressions library, but this is faster and less complex.
        // @see <a href="https://glazedlists.dev.java.net/issues/show_bug.cgi?id=89">Bug 89</a>
        // @param host the string to search within

        int lastFirst = host.length() - filter.length();
        sourceCharacter:
        for(int c = 0; c <= lastFirst; c++) {
            for(int f = 0; f < filter.length(); f++) {
                if(Character.toUpperCase(host.charAt(c+f)) != filter.charAt(f)) continue sourceCharacter;
            }
            return c;
        }
        return -1;
    }
}