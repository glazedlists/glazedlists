/**
 * Glazed Lists
 * http://glazedlists.dev.java.net/
 *
 * COPYRIGHT 2003 O'DELL ENGINEERING LTD.
 */
package com.odellengineeringltd.glazedlists.jtable;

// the core Glazed Lists packages
import com.odellengineeringltd.glazedlists.*;
import com.odellengineeringltd.glazedlists.event.*;
// the Glazed Lists util package includes default comparators
import com.odellengineeringltd.glazedlists.util.*;
// Swing toolkit stuff for displaying widgets
import javax.swing.*;
import javax.swing.table.*;
// for responding to user actions
import java.awt.event.*;
// this class uses tables for displaying contact lists
import java.awt.event.MouseAdapter;
// for keeping lists of comparators
import java.util.*;
// for looking up icon files in jars
import java.net.URL;

/**
 * A TableComparatorChooser is a tool that allows the user to sort a ListTable by clicking
 * on the table's headers. It requires that the ListTable has a SortedList as
 * a source as the sorting on that list is used.
 *
 * <p>The TableComparatorChooser includes custom arrow icons that indicate the sort
 * order. The icons used are chosen based on the current Swing look and feel.
 * Icons are available for the following look and feels: Mac OS X, Metal, Windows.
 *
 * <p>The TableComparatorChooser supports multiple sort strategies for each
 * column, specified by having muliple comparators for each column. This may
 * be useful when you want to sort a single column in either of two ways. For
 * example, when sorting movie names, "The Phantom Menace" may be sorted under
 * "T" for "The", or "P" for "Phantom".
 *
 * <p>The TableComparatorChooser supports sorting multiple columns simultaneously.
 * In this mode, the user clicks a first column to sort by, and then the user
 * clicks subsequent columns. The list is sorted by the first column and ties
 * are broken by the second column.
 * 
 * @see <a href="https://glazedlists.dev.java.net/issues/show_bug.cgi?id=4">Issue #4</a>
 * @see <a href="https://glazedlists.dev.java.net/issues/show_bug.cgi?id=31">Issue #31</a>
 * 
 * @author <a href="mailto:jesse@odel.on.ca">Jesse Wilson</a>
 */
public class TableComparatorChooser extends MouseAdapter {
    
    /** the table being sorted */
    private ListTable listTable;
    private JTable table;
    
    /** the sorted list to choose the comparators for */
    private SortedList sortedList;
    
    /** the first comparator in the comparator chain */
    private int primaryColumn = -1;
    
    /** the columns and their click counts */
    private ColumnClickTracker[] columnClickTrackers;
    
    /** an array that contains all columns with non-zero click counts */
    private ArrayList recentlyClickedColumns = new ArrayList();
    
    /** the sorting style on a column is used for icon choosing */
    private int COLUMN_UNSORTED = 0;
    private int COLUMN_PRIMARY_SORTED = 1;
    private int COLUMN_PRIMARY_SORTED_REVERSE = 2;
    private int COLUMN_PRIMARY_SORTED_ALTERNATE = 3;
    private int COLUMN_PRIMARY_SORTED_ALTERNATE_REVERSE = 4;
    private int COLUMN_SECONDARY_SORTED = 5;
    private int COLUMN_SECONDARY_SORTED_REVERSE = 6;
    private int COLUMN_SECONDARY_SORTED_ALTERNATE = 7;
    private int COLUMN_SECONDARY_SORTED_ALTERNATE_REVERSE = 8;

    /** a map of look and feels to resource paths for icons */
    private static String resourceRoot = "resources";
    private static String defaultResourcePath = "aqua";
    private static Map lookAndFeelResourcePathMap = new HashMap();
    static {
        lookAndFeelResourcePathMap.put("Mac OS X Aqua", "aqua");
        lookAndFeelResourcePathMap.put("Metal", "metal");
        lookAndFeelResourcePathMap.put("Windows", "windows");
    }

    /** the icons to use for indicating sort order */
    private static Icon[] icons = new Icon[] { null, null, null, null, null, null, null, null, null };
    private static String[] iconFileNames = new String[] {
        "unsorted.png", "primary_sorted.png", "primary_sorted_reverse.png",
        "primary_sorted_alternate.png", "primary_sorted_alternate_reverse.png",
        "secondary_sorted.png", "secondary_sorted_reverse.png",
        "secondary_sorted_alternate.png", "secondary_sorted_alternate_reverse.png"
    };
    /** load the icons at classloading time */
    static {
        loadIcons();
    }
    
    /** whether to support sorting on single or multiple columns */
    private boolean multipleColumnSort;
    
    /**
     * Creates a new TableComparatorChooser that responds to clicks
     * on the specified table and uses them to sort the specified list.
     *
     * @param listTable the table with headers that can be clicked on.
     * @param sortedList the sorted list to update.
     * @param singleColumnSort true to sort by only one column at a time, or
     *      false to sort by multiple columns. Although sorting by multiple
     *      columns is more powerful, the user interface is not as powerful and
     *      this strategy should only be used where necessary.
     */
    public TableComparatorChooser(ListTable listTable, SortedList sortedList, boolean multipleColumnSort) {
        this.listTable = listTable;
        table = listTable.getTable();
        this.sortedList = sortedList;
        this.multipleColumnSort = multipleColumnSort;
        
        // build the column click managers
        columnClickTrackers = new ColumnClickTracker[table.getColumnCount()];
        for(int i = 0; i < columnClickTrackers.length; i++) {
            columnClickTrackers[i] = new ColumnClickTracker(i);
        }
        
        // set the table header
        table.getTableHeader().setDefaultRenderer(new SortArrowHeaderRenderer());

        // listen for events on the specified table
        table.setColumnSelectionAllowed(false);
        table.getTableHeader().addMouseListener(this);
    }
    
    /**
     * Gets the list of comparators for the specified column. The user is
     * free to add comparators to this list or clear the list if the specified
     * column cannot be sorted.
     */
    public List getComparatorsForColumn(int column) {
        return columnClickTrackers[column].getComparators();
    }
    
    /**
     * When the mouse is clicked, this selects the next comparator in
     * sequence for the specified table. This will re-sort the table
     * by a new criterea.
     *
     * This code is based on the Java Tutorial's TableSorter
     * @see <a href="http://java.sun.com/docs/books/tutorial/uiswing/components/table.html#sorting">The Java Tutorial</a>
     */
    public void mouseClicked(MouseEvent e) {
        TableColumnModel columnModel = table.getColumnModel();
        int viewColumn = columnModel.getColumnIndexAtX(e.getX()); 
        int column = table.convertColumnIndexToModel(viewColumn); 
        int clicks = e.getClickCount();
        if(clicks >= 1 && column != -1) {
            columnClicked(column, clicks);
        }
    }
    
    /**
     * Handle a column being clicked by sorting that column.
     */
    private void columnClicked(int column, int clicks) {
        ColumnClickTracker currentTracker = columnClickTrackers[column];

        // on a double click, clear the click counts
        if(clicks == 2) {
            for(Iterator i = recentlyClickedColumns.iterator(); i.hasNext(); ) {
                ColumnClickTracker columnClickTracker = (ColumnClickTracker)i.next();
                columnClickTracker.resetClickCount();
            }
            primaryColumn = -1;
            recentlyClickedColumns.clear();
        // if we're only sorting one column at a time, clear other columns
        } else if(!multipleColumnSort) {
            for(Iterator i = recentlyClickedColumns.iterator(); i.hasNext(); ) {
                ColumnClickTracker columnClickTracker = (ColumnClickTracker)i.next();
                if(columnClickTracker != currentTracker) {
                    columnClickTracker.resetClickCount();
                }
            }
            primaryColumn = -1;
            recentlyClickedColumns.clear();
        }
        
        // add a click to the newly clicked column if it has any comparators
        if(!currentTracker.getComparators().isEmpty()) {
            currentTracker.addClick();
            if(recentlyClickedColumns.isEmpty()) {
                recentlyClickedColumns.add(currentTracker);
                primaryColumn = column;
            } else if(!recentlyClickedColumns.contains(currentTracker)) {
                recentlyClickedColumns.add(currentTracker);
            }
        }
        
        // build a new comparator
        if(recentlyClickedColumns.size() > 0) {
            List comparators = new ArrayList();
            for(Iterator i = recentlyClickedColumns.iterator(); i.hasNext(); ) {
                ColumnClickTracker columnClickTracker = (ColumnClickTracker)i.next();
                Comparator comparator = columnClickTracker.getComparator();
                comparators.add(comparator);
            }
            ComparatorChain comparatorChain = new ComparatorChain(comparators);
            
            // select the new comparator
            sortedList.setComparator(comparatorChain);
        }

        // force the table header to redraw itself
        table.getTableHeader().revalidate();
        table.getTableHeader().repaint();
    }
    
    /**
     * Gets the sorting style currently applied to the specified column.
     */
    private int getSortingStyle(int column) {
        int modelColumn = table.convertColumnIndexToModel(column);
        return columnClickTrackers[modelColumn].getSortingStyle();
    }
    
    /**
     * A ColumnClickTracker monitors the clicks on a specified column
     * and provides access to the most appropriate comparator for that
     * column.
     */
    class ColumnClickTracker {

        /** the column for this comparator */
        private int column = 0;
        /** the number of repeated clicks on this column header */
        private int clickCount = 0;
        /** the sequence of comparators for this column */
        private ArrayList comparators = new ArrayList();

        /**
         * Creates a new ColumnClickTracker for the specified column.
         */
        public ColumnClickTracker(int column) {
            this.column = column;
            // add a default comparator
            comparators.add(new TableFieldComparator(listTable.getTableFormat(), column));
        }

        /**
         * Adds a single click to this column.
         */
        public void addClick() {
            clickCount++;
        }

        /**
         * Resets the count of clicks on this column.
         */
        public void resetClickCount() {
            clickCount = 0;
        }

        /**
         * Gets the column for this ColumnComparator.
         */
        public int getColumn() {
            return column;
        }

        /**
         * Returns true if this column is sorted in reverse order.
         */
        public boolean isReverse() {
            return (clickCount % 2 == 0);
        }

        /**
         * Gets the index of the comparator to use for this column.
         */
        private int getComparatorIndex() {
            if(comparators.size() == 0 || clickCount == 0) return -1;
            return ((clickCount-1) / 2) % comparators.size();
        }
        
        /**
         * Gets the list of comparators for this column.
         */
        public List getComparators() {
            return comparators;
        }

        /**
         * Gets the current best comparator to sort this column.
         */
        public Comparator getComparator() {
            Comparator comparator = (Comparator)comparators.get(getComparatorIndex());
            if(isReverse()) comparator = new ReverseComparator(comparator);
            return comparator;
        }
        
        /**
         * Gets the sorting style for this column.
         */
        public int getSortingStyle() {
            if(clickCount == 0) return COLUMN_UNSORTED;

            if(column == primaryColumn) {
                if(isReverse()) {
                    if(getComparatorIndex() == 0) return COLUMN_PRIMARY_SORTED;
                    else return COLUMN_PRIMARY_SORTED_ALTERNATE;
                } else {
                    if(getComparatorIndex() == 0) return COLUMN_PRIMARY_SORTED_REVERSE;
                    else return COLUMN_PRIMARY_SORTED_ALTERNATE_REVERSE;
                }
            } else {
                if(isReverse()) {
                    if(getComparatorIndex() == 0) return COLUMN_SECONDARY_SORTED;
                    else return COLUMN_SECONDARY_SORTED_ALTERNATE;
                } else {
                    if(getComparatorIndex() == 0) return COLUMN_SECONDARY_SORTED_REVERSE;
                    else return COLUMN_SECONDARY_SORTED_ALTERNATE_REVERSE;
                }
            }
        }
    }

    /**
     * Loads the set of icons used to be consistent with the current UIManager.
     */
    private static void loadIcons() {
        // look up the icon resource path based on the current look and feel
        String lookAndFeelName = UIManager.getLookAndFeel().getName();
        String resourcePath = (String)lookAndFeelResourcePathMap.get(lookAndFeelName);
        if(resourcePath == null) resourcePath = defaultResourcePath;
        
        // use the classloader to look inside this jar file
        ClassLoader jarLoader = TableComparatorChooser.class.getClassLoader();
    
        // load each icon as a resource from the source .jar file
        for(int i = 0; i < icons.length; i++) {
            URL iconLocation = jarLoader.getResource(resourceRoot + "/" + resourcePath + "/" + iconFileNames[i]);
            if(iconLocation != null) icons[i] = new ImageIcon(iconLocation);
            else icons[i] = null;
        }
    }
    
    /**
     * A comparator chain compares objects using a list of comparators. The
     * first comparison where the objects differ is returned.
     */
    class ComparatorChain implements Comparator {

        /** the comparators to execute in sequence */
        private List comparators;
        
        /**
         * Creates a comparator chain that views the specified comparators in
         * sequence.
         *
         * @param comparators a list of classes implementing Comparator. It is
         *      an error to modify the specified list after using it as an
         *      argument to comparator chain.
         */
        public ComparatorChain(List comparators) {
            this.comparators = comparators;
        }
        
        /**
         * Compares the two objects with each comparator in sequence.
         */
        public int compare(Object alpha, Object beta) {
            for(Iterator i = comparators.iterator(); i.hasNext(); ) {
                Comparator currentComparator = (Comparator)i.next();
                int compareResult = currentComparator.compare(alpha, beta);
                if(compareResult != 0) return compareResult;
            }
            return 0;
        }
    }

    /**
     * The SortArrowHeaderRenderer simply delegates most of the rendering
     * to the previous renderer, and adds an icon to indicate sorting
     * direction. This eliminates the hassle of setting the border and
     * background colours.
     *
     * <p>This class fails to add indicator arrows on tables where the
     * renderer does not extend DefaultTableCellRenderer.
     */
    class SortArrowHeaderRenderer implements TableCellRenderer {
        
        /** the renderer to delegate */
        private TableCellRenderer delegate;
        
        /** whether we can inject icons into this renderer */
        private boolean iconInjection = false;
        
        /**
         * Creates a new SortArrowHeaderRenderer that delegates most drawing
         * to the tables current header renderer.
         */
        public SortArrowHeaderRenderer() {
            // find the delegate
            this.delegate = table.getTableHeader().getDefaultRenderer();
            
            // determine if we can inject icons into the delegate
            iconInjection = (delegate instanceof DefaultTableCellRenderer);
        }
        
        /**
         * Renders the header in the default way but with the addition of an icon.
         */
        public java.awt.Component getTableCellRendererComponent(JTable table, Object value,
        boolean isSelected, boolean hasFocus, int row, int column) {
            if(iconInjection) {
                DefaultTableCellRenderer jLabelRenderer = (DefaultTableCellRenderer)delegate;
                Icon iconToUse = icons[getSortingStyle(column)];
                jLabelRenderer.setIcon(iconToUse);
                jLabelRenderer.setHorizontalTextPosition(jLabelRenderer.LEADING);
                return delegate.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            } else {
                return delegate.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            }
        }
    }
}


/**
 * A comparator that sorts a table by the column that was clicked.
 */
class TableFieldComparator implements Comparator {

    /** the table format knows to map objects to their fields */
    private TableFormat tableFormat;

    /** the field of interest */
    private int column;
    
    /** comparison is delegated to a ComparableComparator */
    private static ComparableComparator comparableComparator = new ComparableComparator();
    
    /**
     * Creates a new TableFieldComparator that sorts objects by the specified
     * column using the specified table format.
     */
    public TableFieldComparator(TableFormat tableFormat, int column) {
        this.column = column;
        this.tableFormat = tableFormat;
    }
    
    /**
     * Compares the two objects, returning a result based on how they compare.
     */
    public int compare(Object alpha, Object beta) {
        try {
            Object alphaField = tableFormat.getFieldValue(alpha, column);
            Object betaField = tableFormat.getFieldValue(beta, column);
            return comparableComparator.compare(alphaField, betaField);
        // throw a 'nicer' exception if the class does not implement Comparable
        } catch(ClassCastException e) {
            IllegalStateException illegalStateException = new IllegalStateException("TableComparatorChooser can not sort objects that do not implement Comparable");
            illegalStateException.initCause(e);
            throw illegalStateException;
        }
    }
}
