/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.matchers;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.FilterList;
import ca.odell.glazedlists.GlazedLists;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;

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
        compositeMatcherEditor = new CompositeMatcherEditor<>();
        textMatcherEditor = new TextMatcherEditor<>(GlazedLists.toStringTextFilterator());
        anotherTextMatcherEditor = new TextMatcherEditor<>(GlazedLists.toStringTextFilterator());
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
     * matcher matches in OR mode.
     */
    @Test
    public void testCompositeMatcherEditorMatchesOr() {
        compositeMatcherEditor.setMode(CompositeMatcherEditor.OR);

        assertEquals(false, compositeMatcherEditor.getMatcher().matches(null));
        assertEquals(false, compositeMatcherEditor.getMatcher().matches("Football"));
        assertEquals(false, compositeMatcherEditor.getMatcher().matches("Inked"));

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

        listener.assertMatchNone(1);
        compositeMatcherEditor.getMatcherEditors().add(textMatcherEditor);
        textMatcherEditor.setFilterText(new String[] { "and" });
        textMatcherEditor.setFilterText(new String[] { "Band" });
        textMatcherEditor.setFilterText(new String[] { "Bandaid" });
        listener.assertConstrained(5);
        textMatcherEditor.setFilterText(new String[] { "aid" });
        textMatcherEditor.setFilterText(new String[] { "id" });
        listener.assertRelaxed(7);

        compositeMatcherEditor.getMatcherEditors().add(anotherTextMatcherEditor);
        listener.assertRelaxed(8);
        anotherTextMatcherEditor.setFilterText(new String[] { "EarthQuake" });
        listener.assertConstrained(9);
        anotherTextMatcherEditor.setFilterText(new String[] { "Earth" , "Quake" });
        anotherTextMatcherEditor.setFilterText(new String[] { "Quake" });
        listener.assertRelaxed(11);

        anotherTextMatcherEditor.setFilterText(new String[0]);
        listener.assertRelaxed(12);

        compositeMatcherEditor.getMatcherEditors().remove(1);
        listener.assertConstrained(13);
        compositeMatcherEditor.getMatcherEditors().remove(0);
        listener.assertMatchAll(14);
    }

    /**
     * Test that the {@link CompositeMatcherEditor} fires the right event when switching modes.
     */
    @Test
    public void testCompositeMatcherEditorChangeModeWithNoMatcherEditors() {
        SimpleMatcherEditorListener listener = new SimpleMatcherEditorListener();
        compositeMatcherEditor.addMatcherEditorListener(listener);

        final EventList<String> strings = GlazedLists.eventListOf("horse", "cow", "pig", "sheep", "chicken", "duck");
        final FilterList<String> filtered = new FilterList<>(strings, compositeMatcherEditor);

        // AND is the default mode
        assertEquals(CompositeMatcherEditor.AND, compositeMatcherEditor.getMode());
        // no MatcherEditors have been added to the composite yet
        assertEquals(0, compositeMatcherEditor.getMatcherEditors().size());
        // no items have been filtered out
        assertEquals(strings.size(), filtered.size());
        // no events have been fired from the composite
        listener.assertNoEvents(0);

        // change the mode to OR
        compositeMatcherEditor.setMode(CompositeMatcherEditor.OR);

        // OR is now the mode
        assertEquals(CompositeMatcherEditor.OR, compositeMatcherEditor.getMode());
        // no MatcherEditors have been added to the composite yet
        assertEquals(0, compositeMatcherEditor.getMatcherEditors().size());
        // all items have been filtered out
        assertEquals(0, filtered.size());
        // one event has been fired from the composite
        listener.assertMatchNone(1);

        // change the mode back to AND
        compositeMatcherEditor.setMode(CompositeMatcherEditor.AND);

        // AND is now the mode
        assertEquals(CompositeMatcherEditor.AND, compositeMatcherEditor.getMode());
        // no MatcherEditors have been added to the composite yet
        assertEquals(0, compositeMatcherEditor.getMatcherEditors().size());
        // no items have been filtered out
        assertEquals(strings.size(), filtered.size());
        // one event has been fired from the composite
        listener.assertMatchAll(2);
    }

    /**
     * Test that the {@link CompositeMatcherEditor} fires the right event when switching modes.
     */
    @Test
    public void testCompositeMatcherEditorChangeModeWithOneMatcherEditor() {
        SimpleMatcherEditorListener listener = new SimpleMatcherEditorListener();
        compositeMatcherEditor.addMatcherEditorListener(listener);

        final EventList<String> strings = GlazedLists.eventListOf("horse", "cow", "pig", "sheep", "chicken", "duck");
        final FilterList<String> filtered = new FilterList<>(strings, compositeMatcherEditor);
        textMatcherEditor.setMode(TextMatcherEditor.CONTAINS);
        textMatcherEditor.setFilterText(new String[] {"e"});
        compositeMatcherEditor.getMatcherEditors().add(textMatcherEditor);

        // AND is the default mode
        assertEquals(CompositeMatcherEditor.AND, compositeMatcherEditor.getMode());
        // one MatcherEditor has been added to the composite
        assertEquals(1, compositeMatcherEditor.getMatcherEditors().size());
        // three items have been filtered out
        assertEquals(Arrays.asList("horse", "sheep", "chicken") , filtered);
        // one constrain event has been fired from the composite
        listener.assertConstrained(1);

        // change the mode to OR
        compositeMatcherEditor.setMode(CompositeMatcherEditor.OR);

        // OR is now the mode
        assertEquals(CompositeMatcherEditor.OR, compositeMatcherEditor.getMode());
        // one MatcherEditor has been added to the composite
        assertEquals(1, compositeMatcherEditor.getMatcherEditors().size());
        // same three items are filtered out
        assertEquals(Arrays.asList("horse", "sheep", "chicken") , filtered);
        // no further events have been fired from the composite
        listener.assertNoEvents(1);

        // change the mode back to AND
        compositeMatcherEditor.setMode(CompositeMatcherEditor.AND);

        // AND is now the mode
        assertEquals(CompositeMatcherEditor.AND, compositeMatcherEditor.getMode());
        // one MatcherEditor has been added to the composite
        assertEquals(1, compositeMatcherEditor.getMatcherEditors().size());
        // same three items are filtered out
        assertEquals(Arrays.asList("horse", "sheep", "chicken") , filtered);
        // no further events have been fired from the composite
        listener.assertNoEvents(1);
    }

    /**
     * Test that the {@link CompositeMatcherEditor} fires the right event when switching modes.
     */
    @Test
    public void testCompositeMatcherEditorChangeModeWithMoreThanOneMatcherEditor() {
        SimpleMatcherEditorListener listener = new SimpleMatcherEditorListener();
        compositeMatcherEditor.addMatcherEditorListener(listener);

        final EventList<String> strings = GlazedLists.eventListOf("horse", "cow", "pig", "sheep", "chicken", "duck");
        final FilterList<String> filtered = new FilterList<>(strings, compositeMatcherEditor);
        textMatcherEditor.setMode(TextMatcherEditor.CONTAINS);
        textMatcherEditor.setFilterText(new String[] {"e"});
        anotherTextMatcherEditor.setMode(TextMatcherEditor.CONTAINS);
        anotherTextMatcherEditor.setFilterText(new String[] {"s"});
        compositeMatcherEditor.getMatcherEditors().add(textMatcherEditor);
        compositeMatcherEditor.getMatcherEditors().add(anotherTextMatcherEditor);

        // AND is the default mode
        assertEquals(CompositeMatcherEditor.AND, compositeMatcherEditor.getMode());
        // two MatcherEditors have been added to the composite
        assertEquals(2, compositeMatcherEditor.getMatcherEditors().size());
        // four items have been filtered out
        assertEquals(Arrays.asList("horse", "sheep") , filtered);
        // two constrain events have been fired from the composite
        listener.assertConstrained(2);

        // change the mode to OR
        compositeMatcherEditor.setMode(CompositeMatcherEditor.OR);

        // OR is now the mode
        assertEquals(CompositeMatcherEditor.OR, compositeMatcherEditor.getMode());
        // two MatcherEditors have been added to the composite
        assertEquals(2, compositeMatcherEditor.getMatcherEditors().size());
        // three items are filtered out
        assertEquals(Arrays.asList("horse", "sheep", "chicken") , filtered);
        // one relax event has been fired from the composite
        listener.assertRelaxed(3);

        // change the mode back to AND
        compositeMatcherEditor.setMode(CompositeMatcherEditor.AND);

        // AND is now the mode
        assertEquals(CompositeMatcherEditor.AND, compositeMatcherEditor.getMode());
        // two MatcherEditors have been added to the composite
        assertEquals(2, compositeMatcherEditor.getMatcherEditors().size());
        // back to four items filtered out
        assertEquals(Arrays.asList("horse", "sheep") , filtered);
        // a constrain event has been fired from the composite
        listener.assertConstrained(4);
    }
}
