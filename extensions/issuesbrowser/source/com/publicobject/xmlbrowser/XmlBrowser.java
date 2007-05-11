/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package com.publicobject.xmlbrowser;

import java.util.List;
import java.util.Comparator;

import ca.odell.glazedlists.*;
import ca.odell.glazedlists.gui.TableFormat;
import ca.odell.glazedlists.swing.*;

import javax.swing.*;
import javax.swing.tree.DefaultTreeCellRenderer;
import java.awt.*;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;

import com.publicobject.misc.swing.MacCornerScrollPaneLayoutManager;
import com.publicobject.misc.swing.LookAndFeelTweaks;

/**
 * Display an XML file in a table, to show off our tree code.
 *
 * @author <a href="mailto:jesse@swank.ca">Jesse Wilson</a>
 */
public class XmlBrowser {

    public static void main(String[] args) throws InvocationTargetException, InterruptedException {
        if(args.length != 1) {
            args = new String[] { "pom.xml" };
        }

        // create an EventList to share data between main and the Swing EDT
        EventList<Tag> eventList = new BasicEventList<Tag>();

        // start the UI on the EDT
        SwingUtilities.invokeAndWait(new StartUIRunnable(eventList));

        // parse the XML file on the main thread
        InputStream xmlIn = XmlBrowser.class.getClassLoader().getResourceAsStream(args[0]);
        new EventListXmlContentHandler(eventList).parse(xmlIn);
    }

    private static class StartUIRunnable implements Runnable {
        private final EventList<Tag> eventList;

        public StartUIRunnable(EventList<Tag> eventList) {
            this.eventList = GlazedListsSwing.swingThreadProxyList(eventList);
        }

        public void run() {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                // do nothing - fall back to default look and feel
            }

            // prepare the table filters
            JTextField filterEdit = new JTextField(15);
            TextFilterator<Tag> filterator = GlazedLists.textFilterator(new String[]{"qName", "text"});
            TextComponentMatcherEditor<Tag> matcherEditor = new TextComponentMatcherEditor<Tag>(filterEdit, filterator);
            JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            filterPanel.add(new JLabel("Filter:"));
            filterPanel.add(filterEdit);

            // convert the XML into an EventList, then a TreeList
            SortedList<Tag> sortedList = new SortedList<Tag>(eventList, null);
            FilterList<Tag> filteredList = new FilterList<Tag>(sortedList, matcherEditor);

            DefaultExternalExpansionModel<Tag> expansionProvider = new DefaultExternalExpansionModel<Tag>(TreeList.NODES_START_COLLAPSED);
            TreeList<Tag> treeList = new TreeList<Tag>(filteredList, new TagTreeFormat(), expansionProvider);

            // display the XML in a tree table
            String[] columnFields = new String[] { "qName", "text" };
            String[] columnNames = new String[] { "Element", "Content" };
            TableFormat<Tag> tableFormat = GlazedLists.tableFormat(Tag.class, columnFields, columnNames);
            EventTableModel<Tag> tableModel = new EventTableModel<Tag>(treeList, tableFormat);
            JTable table = new JTable(tableModel);
            TreeTableSupport treeSupport = TreeTableSupport.install(table, treeList, 0);
            treeSupport.setArrowKeyExpansionEnabled(true);
            treeSupport.setShowExpanderForEmptyParent(false);
            TableComparatorChooser.install(table, sortedList, TableComparatorChooser.MULTIPLE_COLUMN_KEYBOARD);
            LookAndFeelTweaks.tweakTable(table);
            JScrollPane tableScrollPane = new JScrollPane(table, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
            tableScrollPane.getViewport().setBackground(UIManager.getColor("EditorPane.background"));

            // display the XML in a tree
            EventTreeModel<Tag> treeModel = new EventTreeModel<Tag>(treeList);
            JTree tree = new JTree(treeModel);
            tree.setRootVisible(false);
            tree.setCellRenderer(new TreeListNodeRenderer());

            // build tha application
            JScrollPane treeScrollPane = new JScrollPane(tree, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
            Color borderColor = new Color(153, 153, 204);
            treeScrollPane.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 1, borderColor));
            tableScrollPane.setBorder(BorderFactory.createMatteBorder(1, 1, 0, 0, borderColor));
            MacCornerScrollPaneLayoutManager.install(tableScrollPane);
            JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, treeScrollPane, tableScrollPane);
            splitPane.setBorder(BorderFactory.createEmptyBorder());
            splitPane.setDividerLocation(200); 
            JPanel panel = new JPanel(new BorderLayout());
            panel.add(filterPanel, BorderLayout.NORTH);
            panel.add(splitPane, BorderLayout.CENTER);
            JFrame frame = new JFrame("XML Browser");
            frame.getContentPane().add(panel);
            frame.setSize(640, 480);
            frame.setLocationRelativeTo(null);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setVisible(true);
        }
    }

    public static class TagElementTableFormat implements TableFormat<TreeList.Node<Tag>> {

        public int getColumnCount() {
            return 2;
        }

        public String getColumnName(int column) {
            switch(column) {
                case 0 : return "Element";
                case 1 : return "Content";
                case 2 : return "Has Children";
                case 3 : return "Supports Children";
            }
            throw new IllegalStateException();
        }

        public Object getColumnValue(TreeList.Node<Tag> baseObject, int column) {
            switch(column) {
                case 0 : return baseObject.getElement().getQName();
                case 1 : return baseObject.getElement().getText();
                case 2 : return Boolean.valueOf(!baseObject.isLeaf());
                case 3 : return Boolean.TRUE;
            }
            throw new IllegalStateException();
        }
    }

    /**
     * Adapt {@link Tag}s for use in a tree.
     */
    private static class TagTreeFormat implements TreeList.Format<Tag> {
        public void getPath(List<Tag> path, Tag tag) {
            if(tag == null) return;
            getPath(path, tag.getParent());
            path.add(tag);
        }

        public boolean allowsChildren(Tag element) {
            return true;
        }

        public Comparator<Tag> getComparator(int depth) {
            return null;
        }
    }

    /**
     * Render a TreeList node in a JTree.
     *
     * TODO: make this available as an API via a factory method?
     */
    private static class TreeListNodeRenderer extends DefaultTreeCellRenderer {
        public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
            Object renderValue;
            if(value instanceof TreeList.Node) {
                TreeList.Node<Tag> node = (TreeList.Node<Tag>)value;
                renderValue = node.getElement();
            } else {
                // sometimes JTree inexplicably wants to render the root
                renderValue = null;
            }
            return super.getTreeCellRendererComponent(tree, renderValue, selected, expanded, leaf, row, hasFocus);
        }

    }
}
