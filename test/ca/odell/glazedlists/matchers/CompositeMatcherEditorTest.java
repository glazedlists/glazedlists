/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.matchers;

import ca.odell.glazedlists.GlazedLists;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Test the {@link CompositeMatcherEditor}.
 *
 * @author <a href="mailto:jesse@swank.ca">Jesse Wilson</a>
 */
public class CompositeMatcherEditorTest {

    /** combine multiple matcher editors */
    private CompositeMatcherEditor<String> compositeMatcherEditor;

    /** some matcher editors to demo */
    private TextMatcherEditor<String> textMatcherEditor;
    private TextMatcherEditor<String> anotherTextMatcherEditor;

    /**
     * Prepare for the test.
     */
    @Before
    public void setUp() {
        compositeMatcherEditor = new CompositeMatcherEditor<String>();
        textMatcherEditor = new TextMatcherEditor<String>(GlazedLists.toStringTextFilterator());
        anotherTextMatcherEditor = new TextMatcherEditor<String>(GlazedLists.toStringTextFilterator());
    }

    /**
     * Clean up after the test.
     */
    @After
    public void tearDown() {
        compositeMatcherEditor = null;
        textMatcherEditor = null;
        anotherTextMatcherEditor = null;
    }

    /**
     * Test that the {@link CompositeMatcherEditor} matches only if both
     * matchers match in AND mode.
     */
    @Test
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
     * Test that the {@link CompositeMatcherEditor} matches only if either
     * matchers match in OR mode.
     */
    @Test
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
     * Test that the {@link CompositeMatcherEditor} fires the right events in AND mode.
     */
    @Test
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
     * Test that the {@link CompositeMatcherEditor} fires the right events in OR mode.
     */
    @Test
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
