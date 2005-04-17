/* Glazed Lists                                                 (c) 2003-2005 */
/* http://publicobject.com/glazedlists/                      publicboject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.matchers;

// for being a JUnit test case
import junit.framework.*;
// the Glazed Lists' change objects
import ca.odell.glazedlists.*;
import ca.odell.glazedlists.event.*;
// Java collections are used for underlying data storage
import java.util.*;

/**
 * Test the {@link CompositeMatcherEditor}.
 *
 * @author <a href="mailto:jesse@swank.ca">Jesse Wilson</a>
 */
public class CompositeMatcherEditorTest extends TestCase {

    /** combine multiple matcher editors */
    private CompositeMatcherEditor compositeMatcherEditor;
    
    /** some matcher editors to demo */
    private TextMatcherEditor textMatcherEditor;
    private TextMatcherEditor anotherTextMatcherEditor;
    
    /**
     * Prepare for the test.
     */
    public void setUp() {
        compositeMatcherEditor = new CompositeMatcherEditor();
        textMatcherEditor = new TextMatcherEditor(new StringFilterator());
        anotherTextMatcherEditor = new TextMatcherEditor(new StringFilterator());
    }

    /**
     * Clean up after the test.
     */
    public void tearDown() {
        compositeMatcherEditor = null;
        textMatcherEditor = null;
        anotherTextMatcherEditor = null;
    }

    /**
     * Test that the {@ link CompositeMatcherEditor} matches only if both
     * matchers match.
     */
    public void testCompositeMatcherEditorMatches() {
        assertEquals(true, compositeMatcherEditor.getMatcher().matches(null));
        assertEquals(true, compositeMatcherEditor.getMatcher().matches("Football"));
        assertEquals(true, compositeMatcherEditor.getMatcher().matches("Inked"));
        
        compositeMatcherEditor.getMatcherEditors().add(textMatcherEditor);
        assertEquals(true, compositeMatcherEditor.getMatcher().matches("Kryptonite"));
        assertEquals(true, compositeMatcherEditor.getMatcher().matches("Truck"));
        
        textMatcherEditor.setFilterText(new String[] { "Ford", "Mustang" });
        assertEquals(true, compositeMatcherEditor.getMatcher().matches("1981 Ford Mustang"));
        assertEquals(false, compositeMatcherEditor.getMatcher().matches("1982 Chevy Camaro"));
        
        compositeMatcherEditor.getMatcherEditors().add(anotherTextMatcherEditor);
        assertEquals(true, compositeMatcherEditor.getMatcher().matches("Ford Mustang Concept"));
        assertEquals(false, compositeMatcherEditor.getMatcher().matches("Ford Focus Concept"));
        
        anotherTextMatcherEditor.setFilterText(new String[] { "GT" });
        assertEquals(true, compositeMatcherEditor.getMatcher().matches("2005 Ford Mustang GT"));
        assertEquals(false, compositeMatcherEditor.getMatcher().matches("Pontiac Sunfire GT"));
        
        compositeMatcherEditor.getMatcherEditors().remove(0);
        assertEquals(true, compositeMatcherEditor.getMatcher().matches("Pontiac Sunfire GT"));
        assertEquals(false, compositeMatcherEditor.getMatcher().matches("Ford Mustang"));
        
        anotherTextMatcherEditor.setFilterText(new String[] { "SS" });
        assertEquals(true, compositeMatcherEditor.getMatcher().matches("Chevy Camaro SS"));
        assertEquals(false, compositeMatcherEditor.getMatcher().matches("Mustang Convertable"));
    }
    
    /**
     * Test that the {@ link CompositeMatcherEditor} fires the right events.
     */
    public void testCompositeMatcherEditorEvents() {
        SimpleMatcherEditorListener listener = new SimpleMatcherEditorListener();
        compositeMatcherEditor.addMatcherEditorListener(listener);
        
        listener.assertNoEvents(0);
        compositeMatcherEditor.getMatcherEditors().add(textMatcherEditor);
        textMatcherEditor.setFilterText(new String[] { "and" });
        textMatcherEditor.setFilterText(new String[] { "Band" });
        textMatcherEditor.setFilterText(new String[] { "Bandaid" });
        listener.assertConstrained(4);
        textMatcherEditor.setFilterText(new String[] { "aid" });
        textMatcherEditor.setFilterText(new String[] { "id" });
        listener.assertRelaxed(6);
        
        compositeMatcherEditor.getMatcherEditors().add(anotherTextMatcherEditor);
        anotherTextMatcherEditor.setFilterText(new String[] { "Earthquake" });
        listener.assertConstrained(8);
        anotherTextMatcherEditor.setFilterText(new String[] { "Earth" , "Quake" });
        anotherTextMatcherEditor.setFilterText(new String[] { "Quake" });
        listener.assertRelaxed(10);
        
        compositeMatcherEditor.getMatcherEditors().remove(1);
        listener.assertRelaxed(11);
        compositeMatcherEditor.getMatcherEditors().remove(0);
        listener.assertMatchAll(12);
    }
    
    /**
     * A String's Strings are itself.
     */
    private class StringFilterator implements TextFilterator {
        public void getFilterStrings(List baseList, Object element) {
            baseList.add(element);
        }
    }
    
    /**
     * A MatcherEditorListener that simply remembers how the filter has been changed.
     */
    private class SimpleMatcherEditorListener implements MatcherEditorListener {
        private boolean matchAll = false;
        private boolean matchNone = false;
        private boolean changed = false;
        private boolean constrained = false;
        private boolean relaxed = false;
        private int changes = 0;
        public void matchAll(MatcherEditor source) { changes++; matchAll = true; }
        public void matchNone(MatcherEditor source) { changes++; matchNone = true; }
        public void changed(MatcherEditor source, Matcher matcher) { changes++; changed = true; }
        public void constrained(MatcherEditor source, Matcher matcher) { changes++; constrained = true; }
        public void relaxed(MatcherEditor source, Matcher matcher) { changes++; relaxed = true; }
        public void assertMatchAll(int expectedChanges) {
            assertEquals(expectedChanges, changes);
            assertTrue(matchAll & !matchNone & !changed & !constrained & !relaxed);
            // reset on success
            matchAll = false;
        }
        public void assertMatchNone(int expectedChanges) {
            assertEquals(expectedChanges, changes);
            assertTrue(!matchAll & matchNone & !changed & !constrained & !relaxed);
            // reset on success
            matchNone = false;
        }
        public void assertChanged(int expectedChanges) {
            assertEquals(expectedChanges, changes);
            assertTrue(!matchAll & !matchNone & changed & !constrained & !relaxed);
            // reset on success
            changed = false;
        }
        public void assertConstrained(int expectedChanges) {
            assertEquals(expectedChanges, changes);
            assertTrue(!matchAll & !matchNone & !changed & constrained & !relaxed);
            // reset on success
            constrained = false;
        }
        public void assertRelaxed(int expectedChanges) {
            assertEquals(expectedChanges, changes);
            assertTrue(!matchAll & !matchNone & !changed & !constrained & relaxed);
            // reset on success
            relaxed = false;
        }
        public void assertNoEvents(int expectedChanges) {
            assertEquals(expectedChanges, changes);
            assertTrue(!matchAll & !matchNone & !changed & !constrained & !relaxed);
        }
    }
}
