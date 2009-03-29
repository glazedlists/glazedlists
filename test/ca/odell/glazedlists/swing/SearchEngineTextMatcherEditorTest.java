/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.swing;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.FilterList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.TextFilterator;
import ca.odell.glazedlists.impl.testing.GlazedListsTests;
import ca.odell.glazedlists.matchers.SearchEngineTextMatcherEditor;
import ca.odell.glazedlists.matchers.SearchEngineTextMatcherEditor.Field;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JTextField;

import junit.framework.TestCase;

public class SearchEngineTextMatcherEditorTest extends TestCase {

    private FilterList<String> filterList;
    private JTextField textField = new JTextField();

    @Override
    protected void setUp() throws Exception {
        filterList = new FilterList<String>(new BasicEventList<String>());
        filterList.addAll(GlazedListsTests.delimitedStringToList("James Jesse Jodie Jimney Jocelyn"));

        textField = new JTextField();
        filterList.setMatcherEditor(new SearchEngineTextFieldMatcherEditor<String>(textField, GlazedLists.toStringTextFilterator()));
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

    public void testFields() {
        final Customer jesse = new Customer("Jesse", "Wilson");
        final Customer james = new Customer("James", "Lemieux");
        final Customer holger = new Customer("Holger", "Brands");
        final Customer kevin = new Customer("Kevin", "Maltby");

        final FilterList<Customer> filteredCustomers = new FilterList<Customer>(new BasicEventList<Customer>());
        filteredCustomers.add(jesse);
        filteredCustomers.add(james);
        filteredCustomers.add(holger);
        filteredCustomers.add(kevin);

        final JTextField customerFilterField = new JTextField();
        final TextFilterator<Customer> customerFilterator = GlazedLists.textFilterator(Customer.class, "firstName", "lastName");
        Set<SearchEngineTextMatcherEditor.Field<Customer>> fields = new HashSet<SearchEngineTextMatcherEditor.Field<Customer>>(2);
        fields.add(new SearchEngineTextMatcherEditor.Field<Customer>("first", GlazedLists.textFilterator(Customer.class, "firstName")));
        fields.add(new SearchEngineTextMatcherEditor.Field<Customer>("last", GlazedLists.textFilterator(Customer.class, "lastName")));

        SearchEngineTextFieldMatcherEditor<Customer> matcherEditor = new SearchEngineTextFieldMatcherEditor<Customer>(customerFilterField, customerFilterator);
        matcherEditor.setFields(fields);
        filteredCustomers.setMatcherEditor(matcherEditor);

        assertEquals(Arrays.asList(jesse, james, holger, kevin), filteredCustomers);

        customerFilterField.setText("J");
        customerFilterField.postActionEvent();
        assertEquals(Arrays.asList(jesse, james), filteredCustomers);

        customerFilterField.setText("first:J");
        customerFilterField.postActionEvent();
        assertEquals(Arrays.asList(jesse, james), filteredCustomers);

        customerFilterField.setText("last:J");
        customerFilterField.postActionEvent();
        assertTrue(filteredCustomers.isEmpty());

        customerFilterField.setText("last:B");
        customerFilterField.postActionEvent();
        assertEquals(Arrays.asList(holger, kevin), filteredCustomers);

        customerFilterField.setText("first:e");
        customerFilterField.postActionEvent();
        assertEquals(Arrays.asList(jesse, james, holger, kevin), filteredCustomers);

        customerFilterField.setText("first:e last:B");
        customerFilterField.postActionEvent();
        assertEquals(Arrays.asList(holger, kevin), filteredCustomers);

        customerFilterField.setText("first:e last:-B");
        customerFilterField.postActionEvent();
        assertEquals(Arrays.asList(jesse, james), filteredCustomers);

        customerFilterField.setText("first:e last:-B last:+W");
        customerFilterField.postActionEvent();
        assertEquals(Arrays.asList(jesse), filteredCustomers);

        customerFilterField.setText("first:e last:-B +\"James\"");
        customerFilterField.postActionEvent();
        assertEquals(Arrays.asList(james), filteredCustomers);

        customerFilterField.setText("first:e last:-B -\"James\"");
        customerFilterField.postActionEvent();
        assertEquals(Arrays.asList(jesse), filteredCustomers);

        customerFilterField.setText("first:-e last:B");
        customerFilterField.postActionEvent();
        assertTrue(filteredCustomers.isEmpty());

        customerFilterField.setText("a");
        customerFilterField.postActionEvent();
        assertEquals(Arrays.asList(james, holger, kevin), filteredCustomers);

        customerFilterField.setText("a -B");
        customerFilterField.postActionEvent();
        assertEquals(Arrays.asList(james), filteredCustomers);

        customerFilterField.setText("-B");
        customerFilterField.postActionEvent();
        assertEquals(Arrays.asList(jesse, james), filteredCustomers);

        customerFilterField.setText("-B +esse");
        customerFilterField.postActionEvent();
        assertEquals(Arrays.asList(jesse), filteredCustomers);

        matcherEditor.setFields(Collections.<Field<Customer>>emptySet());
        customerFilterField.setText("first:e");
        customerFilterField.postActionEvent();
        assertTrue(filteredCustomers.isEmpty());
    }

    private void setNewFilter(String text) {
        textField.setText(text);
        textField.postActionEvent();
    }

    public static final class Customer {
        private final String firstName;
        private final String lastName;

        public Customer(String firstName, String lastName) {
            this.firstName = firstName;
            this.lastName = lastName;
        }

        public String getFirstName() { return firstName; }
        public String getLastName() { return lastName; }
    }
}