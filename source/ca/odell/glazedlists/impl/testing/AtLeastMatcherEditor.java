/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.impl.testing;

import ca.odell.glazedlists.matchers.AbstractMatcherEditor;

/**
 * A MatcherEditor for minimum Number values.
 *
 * @author <a href="mailto:jesse@swank.ca">Jesse Wilson</a>
 */
public class AtLeastMatcherEditor extends AbstractMatcherEditor<Number> {
    private int minimum;
    public AtLeastMatcherEditor() {
        this(0);
    }
    public AtLeastMatcherEditor(int minimum) {
        this.minimum = minimum;
        currentMatcher = GlazedListsTests.matchAtLeast(minimum);
    }
    public void setMinimum(int value) {
        if(value < minimum) {
            this.minimum = value;
            fireRelaxed(GlazedListsTests.matchAtLeast(minimum));
        } else if(value == minimum) {
            // do nothing
        } else {
            this.minimum = value;
            fireConstrained(GlazedListsTests.matchAtLeast(minimum));
        }
    }
}
