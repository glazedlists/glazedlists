/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package com.publicobject.amazonbrowser.swing;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.URL;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.*;
import javax.swing.table.*;

import ca.odell.glazedlists.*;
import ca.odell.glazedlists.impl.testing.ListConsistencyListener;
import ca.odell.glazedlists.matchers.SearchEngineTextMatcherEditor;
import ca.odell.glazedlists.swing.*;

import com.publicobject.amazonbrowser.*;
import com.publicobject.misc.Exceptions;
import com.publicobject.misc.swing.ExceptionHandlerFactory;
import com.publicobject.misc.swing.GradientPanel;
import com.publicobject.misc.swing.MacCornerScrollPaneLayoutManager;
import com.publicobject.misc.swing.RoundedBorder;

/**
 * An AmazonBrowser is a program for searching and viewing products from amazon.com.
 *
 * @author James Lemieux
 */
public class AmazonBrowser implements Runnable {

    /** application appearance */
    public static final Color CLEAR = new Color(0, 0, 0, 0);
    public static final Color AMAZON_SEARCH_LIGHT_BLUE = new Color(171, 208, 226);
    public static final Color AMAZON_SEARCH_DARK_BLUE = new Color(54, 127, 168);
    public static final Color AMAZON_TAB_LIGHT_BEIGE = new Color(251, 252, 252);
    public static final Icon GO = loadIcon("resources/go.gif");

    /** an event list to host the items */
    private EventList<Item> itemEventList = new BasicEventList<Item>();

    /** the TreeList backing the EventTableModel that models the treetable data. */
    private TreeList<Item> treeList;

    /** the TableModel backing the treetable of items */
    private EventTableModel<Item> itemTableModel;

    /** the ListSelectionModel backing the treetable of items */
    private EventSelectionModel<Item> itemTableSelectionModel;

    /** loads items as requested */
    private ItemLoader itemLoader;

    /** the field containing the keywords to search items with */
    private JTextField searchField;

    /** the progress bar that tracks the item loading progress */
    private JProgressBar progressBar;

    /** the field containing the terms to filter the treetable of items with */
    private JTextField filterField;

    /** the application window */
    private JFrame frame;

    /**
     * Loads the AmazonBrowser as standalone application.
     */
    public void run() {
        constructStandalone();

        // create the issue loader and start loading issues
        itemLoader = new ItemLoader(itemEventList, progressBar);
        itemLoader.start();
    }

    /**
     * Load the specified icon from the pathname on the classpath.
     */
    private static ImageIcon loadIcon(String pathname) {
        ClassLoader jarLoader = AmazonBrowser.class.getClassLoader();
        URL url = jarLoader.getResource(pathname);
        if (url == null) return null;
        return new ImageIcon(url);
    }

    /**
     * Constructs the browser as a standalone frame.
     */
    private void constructStandalone() {
        // we have advice for the user when we cannot connect to a host
        Exceptions.getInstance().addHandler(ExceptionHandlerFactory.unknownHostExceptionHandler(frame));
        Exceptions.getInstance().addHandler(ExceptionHandlerFactory.connectExceptionHandler(frame));
        Exceptions.getInstance().addHandler(ExceptionHandlerFactory.noRouteToHostExceptionHandler(frame));
        Exceptions.getInstance().addHandler(ExceptionHandlerFactory.accessControlExceptionHandler(frame));
        Exceptions.getInstance().addHandler(ExceptionHandlerFactory.ioExceptionCode500Handler(frame));

        frame = new JFrame("Amazon Browser");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);
        frame.setLocationRelativeTo(null);
        frame.getContentPane().setLayout(new BorderLayout());
        frame.getContentPane().add(constructView(), BorderLayout.CENTER);
        frame.setVisible(true);
    }

    /**
     * Construct a frame for search and browsing items from Amazon.
     */
    private JPanel constructView() {
        final JLabel filterFieldLabel = new JLabel("Filter");
        filterFieldLabel.setFont(new Font("Verdana", Font.BOLD, 14));
        filterFieldLabel.setForeground(Color.WHITE);

        filterField = new JTextField(10);
        final SearchEngineTextFieldMatcherEditor<Item> filterFieldMatcherEditor = new SearchEngineTextFieldMatcherEditor<Item>(filterField, new ItemTextFilterator());

        final Set<SearchEngineTextMatcherEditor.Field<Item>> filterFields = new HashSet<SearchEngineTextMatcherEditor.Field<Item>>();
        filterFields.add(new SearchEngineTextMatcherEditor.Field<Item>("title", new TitleTextFilterator()));
        filterFields.add(new SearchEngineTextMatcherEditor.Field<Item>("director", new DirectorTextFilterator()));
        filterFieldMatcherEditor.setFields(filterFields);

        // sort the original items list
        final SortedList<Item> sortedItemsList = new SortedList<Item>(itemEventList, null);
        final FilterList<Item> filteredItemsList = new FilterList<Item>(sortedItemsList, filterFieldMatcherEditor);

        final StartNewSearchActionListener startNewSearch = new StartNewSearchActionListener();

        final JLabel searchFieldLabel = new JLabel("Search");
        searchFieldLabel.setFont(new Font("Verdana", Font.BOLD, 14));
        searchFieldLabel.setForeground(Color.WHITE);

        searchField = new JTextField(10);
        searchField.addActionListener(startNewSearch);

        final JButton searchButton = new JButton(GO);
        searchButton.setBorder(BorderFactory.createEmptyBorder());
        searchButton.setContentAreaFilled(false);
        searchButton.addActionListener(startNewSearch);

        progressBar = new JProgressBar();
        progressBar.setString("");
        progressBar.setStringPainted(true);
        progressBar.setBorder(BorderFactory.createLineBorder(AMAZON_SEARCH_DARK_BLUE, 2));

        final TreeCriteriaEditor treeCriteriaEditor = new TreeCriteriaEditor(TreeCriterion.ALL_CRITERIA);
        treeCriteriaEditor.addPropertyChangeListener("activeCriteria", new ActiveCriteriaPropertyChangeListener());
        treeCriteriaEditor.setOpaque(false);
        treeCriteriaEditor.setBorder(new RoundedBorder(CLEAR, AMAZON_SEARCH_DARK_BLUE, AMAZON_SEARCH_LIGHT_BLUE, 8, 1));

        final JPanel editorPanel = new JPanel(new GridBagLayout());
        editorPanel.setBackground(AMAZON_SEARCH_DARK_BLUE);
        editorPanel.add(treeCriteriaEditor,           new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 5, 0, 5), 0, 0));
        editorPanel.add(Box.createVerticalStrut(1),   new GridBagConstraints(0, 1, 1, 1, 0.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.VERTICAL, new Insets(0, 0, 0, 0), 0, 0));

        final JPanel searchPanel = new GradientPanel(AMAZON_SEARCH_LIGHT_BLUE, AMAZON_SEARCH_DARK_BLUE, true);
        searchPanel.setLayout(new GridBagLayout());
        searchPanel.add(searchFieldLabel,             new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 10, 0, 3), 0, 0));
        searchPanel.add(searchField,                  new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 5), 0, 0));
        searchPanel.add(searchButton,                 new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 10), 0, 0));
        searchPanel.add(progressBar,                  new GridBagConstraints(3, 0, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 10), 0, 0));
        searchPanel.add(filterFieldLabel,             new GridBagConstraints(4, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 5), 0, 0));
        searchPanel.add(filterField,                  new GridBagConstraints(5, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 10), 0, 0));
        searchPanel.add(Box.createVerticalStrut(65),  new GridBagConstraints(6, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));

        final EventList<Item> swingFilteredItemsList = GlazedListsSwing.swingThreadProxyList(filteredItemsList);
        treeList = new TreeList<Item>(swingFilteredItemsList, new ItemTreeFormat(treeCriteriaEditor.getActiveCriteria()), TreeList.<Item>nodesStartExpanded());

        // create a JTable to display the items
        itemTableModel = new EventTableModel<Item>(treeList, new ItemTableFormat());
        itemTableSelectionModel = new EventSelectionModel<Item>(treeList);
        final JTable itemTable = new StripedTable(itemTableModel, null, itemTableSelectionModel);
        JScrollPane itemScrollPane = new JScrollPane(itemTable, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        itemScrollPane.setBorder(BorderFactory.createEmptyBorder());
        MacCornerScrollPaneLayoutManager.install(itemScrollPane);

        // add sorting to the table
        TableComparatorChooser.install(itemTable, sortedItemsList, TableComparatorChooser.MULTIPLE_COLUMN_KEYBOARD);

        // add a hierarchical column to the table
        ListConsistencyListener<Item> listConsistencyListener = ListConsistencyListener.install(treeList);
        listConsistencyListener.setPreviousElementTracked(false);
        final TreeTableSupport treeTableSupport = TreeTableSupport.install(itemTable, treeList, 2);
        treeTableSupport.setDelegateRenderer(new TitleRenderer());

        // build a panel for the search panel and results table
        final JPanel panel = new JPanel(new BorderLayout());
        panel.add(BorderLayout.NORTH, searchPanel);
        panel.add(BorderLayout.WEST, editorPanel);
        panel.add(BorderLayout.CENTER, itemScrollPane);
        return panel;
    }

    /**
     * When started via a main method, this creates a standalone issues browser.
     */
    public static void main(final String[] args) {
        SwingUtilities.invokeLater(new AmazonBrowserStarter());
    }

    /**
     * This Runnable contains the logic to start the IssuesBrowser application.
     * It is guaranteed to be executed on the EventDispatch Thread.
     */
    private static class AmazonBrowserStarter implements Runnable {
        public void run() {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                // do nothing - fall back to default look and feel
            }

            final AmazonBrowser browser = new AmazonBrowser();
            browser.run();
        }
    }

    /**
     * Notified when the user wishes to begin a new Search of Amazon Items.
     */
    private class StartNewSearchActionListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            final String keywords = searchField.getText();

            if (keywords.length() > 0)
                itemLoader.setKeywords(keywords);
        }
    }

    /**
     * Watch the TreeCriteriaEditor for changes to its "activeCriteria"
     * property and respond by updating the TreeFormat used by the
     * AmazonBrowser treetable to respect the new tree criteria.
     */
    private class ActiveCriteriaPropertyChangeListener implements PropertyChangeListener {
        public void propertyChange(PropertyChangeEvent evt) {
            final List<TreeCriterion> treeCriteria = (List<TreeCriterion>) evt.getNewValue();
            treeList.setTreeFormat(new ItemTreeFormat(treeCriteria));
        }
    }

    /**
     * A custom table that stripes the rows to help ensure our
     * TreeTableCellPanel handles cosmetic customizations (background,
     * foreground, font, etc).
     */
    private static class StripedTable extends JTable {
        public StripedTable(TableModel dm, TableColumnModel cm, ListSelectionModel sm) {
            super(dm, cm, sm);
            setSurrendersFocusOnKeystroke(true);
        }

        @Override
        public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
            return normalize(super.prepareRenderer(renderer, row, column), row);
        }

        @Override
        public Component prepareEditor(TableCellEditor editor, int row, int column) {
            return normalize(super.prepareEditor(editor, row, column), row);
        }

        /**
         * This method applies some common formatting to the given Component
         * which is either a renderer or editor component.
         */
        private Component normalize(Component c, int row) {
            if (!isRowSelected(row))
                c.setBackground(row % 2 == 0 ? Color.WHITE : AMAZON_SEARCH_LIGHT_BLUE);

            return c;
        }
    }

    private static final class TitleTextFilterator implements TextFilterator<Item> {
        public void getFilterStrings(List<String> baseList, Item element) {
            baseList.add(element.getItemAttributes().getTitle());
        }
    }

    private static final class DirectorTextFilterator implements TextFilterator<Item> {
        public void getFilterStrings(List<String> baseList, Item element) {
            baseList.add(element.getItemAttributes().getDirector());
        }
    }

    /**
     * A special renderer for the Title column (which renders the hierarchy)
     * which is given access to the hierarchy data for rendering. It uses this
     * hierarchy data to make all hierarchy text gray and all leaf text black.
     */
    private static final class TitleRenderer extends AbstractTreeTableNodeDataRenderer {

        /** The delegate renderer that does most of the rendering. We simply tweak the result. */
        private TableCellRenderer delegate = new DefaultTableCellRenderer();

        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            // fetch the component from the delegate renderer
            final JLabel label = (JLabel) delegate.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

            // selected rows always have white text, otherwise parent nodes are gray and leaf nodes are black
            if (isSelected)
                label.setForeground(Color.WHITE);
            else
                label.setForeground(hasChildren() ? Color.GRAY : Color.BLACK);

            return label;
        }
    }
}