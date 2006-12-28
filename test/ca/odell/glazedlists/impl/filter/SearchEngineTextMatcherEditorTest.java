/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.impl.filter;

import junit.framework.TestCase;
import ca.odell.glazedlists.FilterList;
import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.swing.SearchEngineTextMatcherEditor;
import ca.odell.glazedlists.impl.testing.GlazedListsTests;

import javax.swing.*;

public class SearchEngineTextMatcherEditorTest extends TestCase {

    private FilterList<String> filterList;
    private JTextField textField = new JTextField();

    protected void setUp() throws Exception {
        filterList = new FilterList<String>(new BasicEventList<String>());
        filterList.addAll(GlazedListsTests.delimitedStringToList("James Jesse Jodie Jimney Jocelyn"));

        textField = new JTextField();
        filterList.setMatcherEditor(new SearchEngineTextMatcherEditor<String>(textField, GlazedLists.toStringTextFilterator()));
    }

    public void testBasicFilter() {
        setNewFilter("Jo");
        assertEquals(GlazedListsTests.delimitedStringToList("Jodie Jocelyn"), filterList);

        setNewFilter("Ja");
        assertEquals(GlazedListsTests.delimitedStringToList("James"), filterList);

        setNewFilter("Jarek");
        assertTrue(filterList.isEmpty());

        setNewFilter("");
        assertEquals(GlazedListsTests.delimitedStringToList("James Jesse Jodie Jimney Jocelyn"), filterList);
    }

    public void testNegationFilter() {
        setNewFilter("Jo");
        assertEquals(GlazedListsTests.delimitedStringToList("Jodie Jocelyn"), filterList);

        setNewFilter("-Jo");
        assertEquals(GlazedListsTests.delimitedStringToList("James Jesse Jimney"), filterList);

        setNewFilter("-JK");
        assertEquals(GlazedListsTests.delimitedStringToList("James Jesse Jodie Jimney Jocelyn"), filterList);

        setNewFilter("JK");
        assertTrue(filterList.isEmpty());

        setNewFilter("");
        assertEquals(GlazedListsTests.delimitedStringToList("James Jesse Jodie Jimney Jocelyn"), filterList);
    }

    public void testQuotedFilter() {
        setNewFilter("Jo die");
        assertEquals(GlazedListsTests.delimitedStringToList("Jodie"), filterList);

        setNewFilter("\"Jo die\"");
        assertTrue(filterList.isEmpty());

        setNewFilter("\"Jo die");
        assertTrue(filterList.isEmpty());

        setNewFilter("Jo die\"");
        assertEquals(GlazedListsTests.delimitedStringToList("Jodie"), filterList);
    }

    private void setNewFilter(String text) {
        textField.setText(text);
        textField.postActionEvent();
    }
}