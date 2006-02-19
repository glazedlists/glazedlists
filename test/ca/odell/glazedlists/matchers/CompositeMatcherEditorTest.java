/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.matchers;

// for being a JUnit test case
import junit.framework.*;
// the Glazed Lists' change objects
import ca.odell.glazedlists.*;
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
        textMatcherEditor = new TextMatcherEditor(GlazedLists.toStringTextFilterator());
        anotherTextMatcherEditor = new TextMatcherEditor(GlazedLists.toStringTextFilterator());
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
     * matchers match in AND mode.
     */
    public void testCompositeMatcherEditorMatchesAnd() {
        assertEquals(CompositeMatcherEditor.AND, compositeMatcherEditor.getMode());
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
     * Test that the {@ link CompositeMatcherEditor} matches only if either
     * matchers match in OR mode.
     */
    public void testCompositeMatcherEditorMatchesOr() {
        compositeMatcherEditor.setMode(CompositeMatcherEditor.OR);
        
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
        assertEquals(true, compositeMatcherEditor.getMatcher().matches("Ford Focus Concept"));
        
        anotherTextMatcherEditor.setFilterText(new String[] { "GT" });
        assertEquals(true, compositeMatcherEditor.getMatcher().matches("2005 Ford Mustang GT"));
        assertEquals(true, compositeMatcherEditor.getMatcher().matches("Pontiac Sunfire GT"));
        assertEquals(false, compositeMatcherEditor.getMatcher().matches("Pontiac Firebird Trans-Am"));
        
        compositeMatcherEditor.getMatcherEditors().remove(0);
        assertEquals(true, compositeMatcherEditor.getMatcher().matches("Pontiac Sunfire GT"));
        assertEquals(false, compositeMatcherEditor.getMatcher().matches("Ford Mustang"));
        
        anotherTextMatcherEditor.setFilterText(new String[] { "SS" });
        assertEquals(true, compositeMatcherEditor.getMatcher().matches("Chevy Camaro SS"));
        assertEquals(false, compositeMatcherEditor.getMatcher().matches("Mustang Convertable"));
        
        compositeMatcherEditor.getMatcherEditors().remove(0);
        assertEquals(true, compositeMatcherEditor.getMatcher().matches("Pontiac Sunfire GT"));
        assertEquals(true, compositeMatcherEditor.getMatcher().matches("Ford Mustang"));
    }
    
    /**
     * Test that the {@ link CompositeMatcherEditor} fires the right events in AND mode.
     */
    public void testCompositeMatcherEditorAndEvents() {
        SimpleMatcherEditorListener listener = new SimpleMatcherEditorListener();
        compositeMatcherEditor.addMatcherEditorListener(listener);
        assertEquals(CompositeMatcherEditor.AND, compositeMatcherEditor.getMode());
        
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
        anotherTextMatcherEditor.setFilterText(new String[] { "EarthQuake" });
        listener.assertConstrained(8);
        anotherTextMatcherEditor.setFilterText(new String[] { "Earth" , "Quake" });
        anotherTextMatcherEditor.setFilterText(new String[] { "Quake" });
        listener.assertRelaxed(10);
        
        anotherTextMatcherEditor.setFilterText(new String[0]);
        listener.assertRelaxed(11);
        
        compositeMatcherEditor.getMatcherEditors().remove(1);
        listener.assertRelaxed(12);
        compositeMatcherEditor.getMatcherEditors().remove(0);
        listener.assertMatchAll(13);
    }
    
    /**
     * Test that the {@ link CompositeMatcherEditor} fires the right events in OR mode.
     */
    public void testCompositeMatcherEditorOrEvents() {
        SimpleMatcherEditorListener listener = new SimpleMatcherEditorListener();
        compositeMatcherEditor.addMatcherEditorListener(listener);
        compositeMatcherEditor.setMode(CompositeMatcherEditor.OR);
        
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
        listener.assertRelaxed(7);
        anotherTextMatcherEditor.setFilterText(new String[] { "EarthQuake" });
        listener.assertConstrained(8);
        anotherTextMatcherEditor.setFilterText(new String[] { "Earth" , "Quake" });
        anotherTextMatcherEditor.setFilterText(new String[] { "Quake" });
        listener.assertRelaxed(10);
        
        anotherTextMatcherEditor.setFilterText(new String[0]);
        listener.assertRelaxed(11);
        
        compositeMatcherEditor.getMatcherEditors().remove(1);
        listener.assertConstrained(12);
        compositeMatcherEditor.getMatcherEditors().remove(0);
        listener.assertMatchAll(13);
    }
}
