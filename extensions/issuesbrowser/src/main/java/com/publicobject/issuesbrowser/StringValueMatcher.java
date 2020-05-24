/* Glazed Lists                                                 (c) 2003-2011 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package com.publicobject.issuesbrowser;

import ca.odell.glazedlists.matchers.Matcher;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

/**
 * If the set of given values for this matcher contains any of an
 * issue's values, that issue matches.
 *
 * @author Holger Brands
 */
public class StringValueMatcher implements Matcher<Issue> {

    private final Set<String> values;

    private final Function<Issue, List<String>> valueExtractor;

    /**
     * Create a new {@link StringValueMatcher}, creating a private copy
     * of the specified {@link Collection} to match against. A private
     * copy is made because {@link Matcher}s must be immutable.
     */
    public StringValueMatcher(Collection<String> values, Function<Issue, List<String>> valueExtractor) {
        this.values = new HashSet<>(values);
        this.valueExtractor = valueExtractor;
    }

    /**
     * @return true if this matches every {@link Issue} the other matches.
     */
    public boolean isRelaxationOf(Matcher other) {
        if(!(other instanceof StringValueMatcher)) {
            return false;
        }
        StringValueMatcher otherComponentMatcher = (StringValueMatcher)other;
        return values.containsAll(otherComponentMatcher.values);
    }

    /**
     * Test whether to include or not include the specified issue based
     * on whether or not their value is selected.
     */
    @Override
    public boolean matches(Issue issue) {
        for (Iterator<String> i = valueExtractor.apply(issue).iterator(); i.hasNext(); ) {
            if (this.values.contains(i.next())) {
                return true;
            }
        }
        return false;
    }
}