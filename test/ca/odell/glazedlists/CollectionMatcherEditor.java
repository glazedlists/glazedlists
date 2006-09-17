/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists;

import ca.odell.glazedlists.matchers.Matcher;
import ca.odell.glazedlists.matchers.AbstractMatcherEditor;

import java.util.Collection;
import java.util.Collections;

/**
 * A simple matcher editor that uses set collection for matching.
 *
 * @author <a href="mailto:jesse@swank.ca">Jesse Wilson</a>
 */
public class CollectionMatcherEditor<E> extends AbstractMatcherEditor<E> {

    private Collection current = null;

    public void matchAll() {
        current = null;
        fireMatchAll();
    }

    public void matchNone() {
        current = Collections.emptyList();
        fireMatchNone();
    }

    public void setCollection(Collection<E> values) {
        boolean relaxed = current != null && values.containsAll(current);
        boolean constrained = current == null || current.containsAll(values);
        this.current = values;
        CollectionMatcher<E> matcherEditor = new CollectionMatcher<E>(values);

        if(relaxed) {
            fireRelaxed(matcherEditor);

        } else if(constrained) {
            fireConstrained(matcherEditor);

        } else {
            fireChanged(matcherEditor);

        }
    }

    private static class CollectionMatcher<E> implements Matcher<E> {
        private final Collection<E> values;
        public CollectionMatcher(Collection<E> values) {
            this.values = values;
        }
        public boolean matches(E item) {
            return values.contains(item);
        }
    }
}
