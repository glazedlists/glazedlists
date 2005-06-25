package ca.odell.glazedlists.matchers;

import junit.framework.Assert;

/**
 * A MatcherEditorListener that simply remembers how the filter has been changed.
 *
 * @author <a href="mailto:jesse@swank.ca">Jesse Wilson</a>
 */
public class SimpleMatcherEditorListener implements MatcherEditorListener {
    private boolean matchAll = false;
    private boolean matchNone = false;
    private boolean changed = false;
    private boolean constrained = false;
    private boolean relaxed = false;
    private int changes = 0;
    public void changedMatcher(MatcherEvent matcherEvent) {
        switch (matcherEvent.getType()) {
            case MatcherEvent.CONSTRAINED: changes++; constrained = true; break;
            case MatcherEvent.RELAXED: changes++; relaxed = true; break;
            case MatcherEvent.CHANGED: changes++; changed = true; break;
            case MatcherEvent.MATCH_ALL: changes++; matchAll = true; break;
            case MatcherEvent.MATCH_NONE: changes++; matchNone = true; break;
        }
    }
    public void assertMatchAll(int expectedChanges) {
        Assert.assertEquals(expectedChanges, changes);
        Assert.assertTrue(matchAll & !matchNone & !changed & !constrained & !relaxed);
        // reset on success
        matchAll = false;
    }
    public void assertMatchNone(int expectedChanges) {
        Assert.assertEquals(expectedChanges, changes);
        Assert.assertTrue(!matchAll & matchNone & !changed & !constrained & !relaxed);
        // reset on success
        matchNone = false;
    }
    public void assertChanged(int expectedChanges) {
        Assert.assertEquals(expectedChanges, changes);
        Assert.assertTrue(!matchAll & !matchNone & changed & !constrained & !relaxed);
        // reset on success
        changed = false;
    }
    public void assertConstrained(int expectedChanges) {
        Assert.assertEquals(expectedChanges, changes);
        Assert.assertTrue(!matchAll & !matchNone & !changed & constrained & !relaxed);
        // reset on success
        constrained = false;
    }
    public void assertRelaxed(int expectedChanges) {
        Assert.assertEquals(expectedChanges, changes);
        Assert.assertTrue(!matchAll & !matchNone & !changed & !constrained & relaxed);
        // reset on success
        relaxed = false;
    }
    public void assertNoEvents(int expectedChanges) {
        Assert.assertEquals(expectedChanges, changes);
        Assert.assertTrue(!matchAll & !matchNone & !changed & !constrained & !relaxed);
    }
}
