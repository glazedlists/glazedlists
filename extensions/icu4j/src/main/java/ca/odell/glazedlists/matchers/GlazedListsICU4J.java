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

    /** a text search strategy with full unicode locale-sensitive string searching */
    public static final Object UNICODE_TEXT_SEARCH_STRATEGY = new UnicodeStrategyFactory();
    // this would be an anonymous class if declawer supported them!
    private static class UnicodeStrategyFactory implements TextSearchStrategy.Factory {
        @Override
        public TextSearchStrategy create(int mode, String filter) {
            return new UnicodeCaseInsensitiveTextSearchStrategy(mode);
        }
    }
}
