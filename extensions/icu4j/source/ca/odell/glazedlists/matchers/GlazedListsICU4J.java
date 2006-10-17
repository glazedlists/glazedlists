/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.matchers;

import ca.odell.glazedlists.impl.filter.TextSearchStrategy;
import ca.odell.glazedlists.impl.filter.UnicodeCaseInsensitiveTextSearchStrategy;

/**
 * Bind Glazed Lists to ICU4J.
 *
 * @author <a href="mailto:jesse@swank.ca">Jesse Wilson</a>
 */
public class GlazedListsICU4J {

    public static final Object STRATEGY = new UnicodeStrategyFactory();

    private static class UnicodeStrategyFactory implements TextSearchStrategy.Factory {
        public TextSearchStrategy create(int mode, String filter) {
            return new UnicodeCaseInsensitiveTextSearchStrategy(mode);
        }
    }

}
