/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists;

import ca.odell.glazedlists.matchers.AbstractMatcherEditor;
import ca.odell.glazedlists.matchers.Matcher;

/**
 * A matcher editor for filtering integer arrays, which are particularly well
 * suited for sorting and filtering tests.
 *
 * @author <a href="mailto:kevin@swank.ca">Kevin Maltby</a>
 */
public class IntegerArrayMatcherEditor extends AbstractMatcherEditor<int[]> {

    public IntegerArrayMatcherEditor(int index, int threshhold) {
        setFilter(index, threshhold);
    }
    public void setFilter(int index, int threshhold) {
        fireChanged(new IntegerArrayMatcher(index, threshhold));
    }
    private static class IntegerArrayMatcher implements Matcher<int[]> {
        private int index;
        private int threshhold;
        public IntegerArrayMatcher(int index, int threshhold) {
            this.index = index;
            this.threshhold = threshhold;
        }
        public boolean matches(int[] array) {
            return (array[index] >= threshhold);
        }
    }
}