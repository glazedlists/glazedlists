/**
 * Glazed Lists
 * http://opensource.odellengineeringltd.com/glazedlists/
 *
 * COPYRIGHT 2003 O'DELL ENGINEERING LTD.
 */
package com.odellengineeringltd.glazedlists.demo;

// the core Glazed Lists
import com.odellengineeringltd.glazedlists.*;
// Glazed Lists displayed in a JTable
import com.odellengineeringltd.glazedlists.jtable.*;
// manipulation of objects using Java collections
import java.util.*;
// Swing toolkit stuff for displaying widgets
import javax.swing.*;
import java.awt.*;
import com.odellengineeringltd.glazedlists.util.ExitOnCloseHandler;


/**
 * This class adds a checkbox that is always checked for properties that
 * must be included in the Java 1.4.2 JVM, as documented on the
 * <a href="http://java.sun.com/j2se/1.4.2/docs/api/java/lang/System.html#getProperties()">Java 1.4.2 API</a>.
 *
 * The table is rendered differently depending on whether or not the
 * cell is selected. This allows the user to "pop-open" a table cell for more
 * information.
 *
 * @author <a href="mailto:jesse@odel.on.ca">Jesse Wilson</a>
 */
public class Demo implements SelectionListener {
    
    /** the frame that hosts the demo */
    private JFrame frame = null;
    
    /** the base list */
    private EventList properties;
    /** a sorted view of the base list */
    private SortedList sortedProperties;
    /** a filtered view of the sorted list */
    private CaseInsensitiveFilterList filteredProperties;
    /** a GUI view of the filtered list in a table */
    private ListTable listTable;
    /** a tool to select the sorting criteria from the table */
    private TableComparatorSelector sorter;

    /**
     * Creates a new demo of the Glazed Lists.
     */
    public Demo() {
        // build the list models
        createLists();
        
        // build the display
        createFrame();

        // start listening for events
        listTable.addSelectionListener(this);
    }
    
    /**
     * Creates a series of layered Glazed Lists. The base list is an EventList
     * of Property objects. That list is covered by a Sorted List which provides
     * a sorted view. That list is covered by a filtered list. The filtered
     * list is displayed in a Java table by a ListTable.
     */
    private void createLists() {
        // set up the model
        properties = new BasicEventList();

        // load the properties
        for(Enumeration e = System.getProperties().propertyNames(); e.hasMoreElements(); ) {
            String key = (String)e.nextElement();
            String value = System.getProperty(key);
            Property property = new Property(key, value);
            properties.add(property);
        }

        // set up the sorted table display
        sortedProperties = new SortedList(properties);
	
        // filter the table, this requires my Property class to implement Filterable
        filteredProperties = new CaseInsensitiveFilterList(sortedProperties);
	
        // display the list in the table
        listTable = new ListTable(filteredProperties, new PropertiesTableFormat());

        // for specifying sorting criteria
        sorter = new TableComparatorSelector(listTable, sortedProperties);
        sorter.addComparator(0, "System Properties (by key)", new PropertyKeyComparator());
        sorter.addComparator(0, "System Properties (by value)", new PropertyValueComparator());
    }
    
    /**
     * Creates and displays a frame to host the demo.
     */
    private void createFrame() {

        // display the properties in a frame
        frame = new JFrame("Property Browser");
        ExitOnCloseHandler.addToFrame(frame);
        frame.setSize(640, 480);
        
        // layout the panel using messy gridbag
        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());
        GridBagConstraints cell = new GridBagConstraints();
        cell.gridx = 0;
        cell.gridwidth = 1;
        cell.gridheight = 1;
        cell.anchor = GridBagConstraints.WEST;
        cell.fill = GridBagConstraints.BOTH;
        cell.weightx = 0.0;
        cell.weighty = 0.0;
        panel.add(new JLabel("Filter: "));
        cell.weightx = 1.0;
        cell.gridx = 1;
        panel.add(filteredProperties.getFilterEdit(), cell);
        cell.gridx = 0;
        cell.gridwidth = 2;
        cell.weightx = 1.0;
        cell.weighty = 1.0;
        panel.add(listTable.getTableScrollPane(), cell);

        // add the panel to the frame and display
        frame.getContentPane().add(panel);
        frame.show();
    }

    /**
     * Create a list of all system properties and display those properties in
     * a table inside of a JFrame.
     */
    public static void main(String[] args) {
        new Demo();
    }
    
    /**
     * When the selection changes, change the Window title.
     */
    public void setSelection(Object selected) {
        Property property = (Property)selected;
        frame.setTitle(property.getKey() + " - Property Browser");
    }
    /**
     * When the selection is cleared, reset the window title.
     */
    public void clearSelection() {
        frame.setTitle("Property Browser");
    }
    /**
     * When a property is double-clicked, ignore that event.
     */
    public void setDoubleClicked(Object doubleClicked) {
        // do nothing
    }
}
/**
 * An object to model a property. It has a key and a value.
 */
class Property implements Comparable, Filterable {
    
    /** default properties and their descriptions, from the API */
    private static final Properties mandatoryProperties = new Properties();
    static {
        mandatoryProperties.setProperty("java.version", "Java Runtime Environment version");
        mandatoryProperties.setProperty("java.vendor", "Java Runtime Environment vendor");
        mandatoryProperties.setProperty("java.vendor.url", "Java vendor URL");
        mandatoryProperties.setProperty("java.home", "Java installation directory");
        mandatoryProperties.setProperty("java.vm.specification.version", "Java Virtual Machine specification version");
        mandatoryProperties.setProperty("java.vm.specification.vendor", "Java Virtual Machine specification vendor");
        mandatoryProperties.setProperty("java.vm.specification.name", "Java Virtual Machine specification name");
        mandatoryProperties.setProperty("java.vm.version", "Java Virtual Machine implementation version");
        mandatoryProperties.setProperty("java.vm.vendor", "Java Virtual Machine implementation vendor");
        mandatoryProperties.setProperty("java.vm.name", "Java Virtual Machine implementation name");
        mandatoryProperties.setProperty("java.specification.version", "Java Runtime Environment specification version");
        mandatoryProperties.setProperty("java.specification.vendor", "Java Runtime Environment specification vendor");
        mandatoryProperties.setProperty("java.specification.name", "Java Runtime Environment specification name");
        mandatoryProperties.setProperty("java.class.version", "Java class format version number");
        mandatoryProperties.setProperty("java.class.path", "Java class path");
        mandatoryProperties.setProperty("java.library.path", "List of paths to search when loading libraries");
        mandatoryProperties.setProperty("java.io.tmpdir", "Default temp file path");
        mandatoryProperties.setProperty("java.compiler", "Name of JIT compiler to use");
        mandatoryProperties.setProperty("java.ext.dirs", "Path of extension directory or directories");
        mandatoryProperties.setProperty("os.name", "Operating system name");
        mandatoryProperties.setProperty("os.arch", "Operating system architecture");
        mandatoryProperties.setProperty("os.version", "Operating system version");
        mandatoryProperties.setProperty("file.separator", "File separator (\"/\" on UNIX)");
        mandatoryProperties.setProperty("path.separator", "Path separator (\":\" on UNIX)");
        mandatoryProperties.setProperty("line.separator", "Line separator (\"\\n\" on UNIX)");
        mandatoryProperties.setProperty("user.name", "User's account name");
        mandatoryProperties.setProperty("user.home", "User's home directory");
        mandatoryProperties.setProperty("user.dir", "User's current working directory");
    }
    /** the value of this property */
    private String key;
    private String value;
    /** the description of this property, or null if it is not mandatory */
    private String description;
    public Property(String key, String value) {
        this.key = key;
        this.value = value;
        // set the description from the mandatory properties
        description = mandatoryProperties.getProperty(key);
    }
    public String getKey() {
        return key;
    }
    public String getValue() { 
        return value;
    }
    public String getDescription() { 
        return description;
    }
    public int compareTo(Object other) {
        Property otherProperty = (Property)other;
        return getKey().compareToIgnoreCase(otherProperty.getKey());
    }
    /**
     * For implementing the Filterable interface. This gets the entire text
     * of this object, so that it can be checked against the current filter
     * text. To conserve String concatenations, a String array is used.
     */
    public String[] getFilterStrings() {
        return new String[] { key, value };
    }
}
    
/**
 * Formats the JTable for holding properties. By using a more sophisticated
 * table renderer, we have collapsed both the key and value fields into one
 * cell, each in a different font.
 */
class PropertiesTableFormat implements TableFormat {
    public int getFieldCount() {
        return 1;
    }
    public String getFieldName(int column) {
        return "System Property";
    }
    public Object getFieldValue(Object baseObject, int column) {
        return baseObject;
    }
    public void configureTable(JTable table) {
        table.getColumnModel().getColumn(0).setCellRenderer(new PropertyRenderer());
    }
}
    
/**
 * Formats the cell display of the property. This writes the property name
 * and value in rich formatted text.
 */
class PropertyRenderer extends StyledDocumentRenderer {

    public PropertyRenderer() {
        super(true);
    }
    public void writeObject(JTable table, Object value, 
        boolean isSelected, boolean hasFocus, int row, int column) {
            
        Property property = (Property)value;

        // checkbox for mandatory fields, which have non-null descriptions
        String description = property.getDescription();
        
        // append an "M" for mandatory properties
        if(description != null) append("[M] ", small);
        
        // add the key and value
        append(property.getKey(), strong);
        append(": ", strong);
        append(property.getValue(), plainItalic);
        
        // when this property is selected, write its description on a second line
        if(isSelected) {
            if(description != null) {
                append("\n", plain);
                append(description, plain);
            }
        }
        rendered.repaint();
    }
}
    
/**
 * Sorts properties by key.
 */
class PropertyKeyComparator implements Comparator {
    public int compare(Object alpha, Object beta) {
        Property alphaProperty = (Property)alpha;
        Property betaProperty = (Property)beta;
        return alphaProperty.getKey().compareToIgnoreCase(betaProperty.getKey());
    }
}

/**
 * Sorts properties by value.
 */
class PropertyValueComparator implements Comparator {
    public int compare(Object alpha, Object beta) {
        Property alphaProperty = (Property)alpha;
        Property betaProperty = (Property)beta;
        return alphaProperty.getValue().compareToIgnoreCase(betaProperty.getValue());
    }
}
