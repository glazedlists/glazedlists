/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package com.publicobject.xmlbrowser;

import java.util.List;
import ca.odell.glazedlists.*;
import ca.odell.glazedlists.gui.WritableTableFormat;
import ca.odell.glazedlists.swing.*;

import javax.swing.*;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.awt.*;
import java.io.InputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.lang.reflect.InvocationTargetException;

import org.xml.sax.*;
import org.xml.sax.helpers.DefaultHandler;
import com.publicobject.misc.xml.SaxParserSidekick;

/**
 * Display an XML file in a table, to show off our tree code.
 *
 * @author <a href="mailto:jesse@swank.ca">Jesse Wilson</a>
 */
public class XmlBrowser {

    /**
     * Model an XML element.
     */
    public static class Element implements Comparable<Element> {
        private Element parent = null;
        private final String qName;
        private String text = "";

        public int compareTo(Element o) {
            return qName.compareTo(o.qName);
        }

        public Element(String qName) {
            this.parent = null;
            this.qName = qName;
            this.text = "";
        }

        public String getQName() {
            return qName;
        }

        public Element getParent() {
            return parent;
        }

        public String getText() {
            return text.trim();
        }

        public Element createChild(String qName) {
            Element child = new Element(qName);
            child.parent = this;
            return child;
        }

        public void append(String text) {
            this.text += text;
        }

        public String toString() {
            return qName;
        }
    }

    /**
     * Convert an XML input stream into an EventList of Elements.
     */
    private static class EventListXmlContentHandler extends DefaultHandler {

        private final EventList<Element> target;
        private final List<Element> stack = new ArrayList<Element>();

        public EventListXmlContentHandler(EventList<Element> target) {
            this.target = GlazedLists.threadSafeList(target);
        }

        public EventList<Element> parse(InputStream inputStream) {
            try {
                XMLReader xmlReader = SAXParserFactory.newInstance().newSAXParser().getXMLReader();
                SaxParserSidekick.install(xmlReader);

                EventListXmlContentHandler handler = new EventListXmlContentHandler(target);
                xmlReader.setContentHandler(handler);
                xmlReader.parse(new InputSource(inputStream));

                return handler.target;
            } catch(IOException e) {
                throw new RuntimeException(e);
            } catch(SAXException e) {
                throw new RuntimeException(e);
            } catch(ParserConfigurationException e) {
                throw new RuntimeException(e);
            }
        }

        public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
            Element child;
            if(!stack.isEmpty()) {
                Element last = stack.get(stack.size() - 1);
                child = last.createChild(qName);

            } else {
                child = new Element(qName);
            }

            target.add(child);
            stack.add(child);
        }

        public void endElement(String uri, String localName, String qName) throws SAXException {
            stack.remove(stack.size() - 1);
        }

        public void characters(char[] ch, int start, int length) throws SAXException {
            Element last = stack.get(stack.size() - 1);
            last.append(new String(ch, start, length));
        }
    }


    public static void main(String[] args) throws InvocationTargetException, InterruptedException {
        if(args.length != 1) {
            args = new String[] { "pom.xml" };
        }

        EventList<Element> eventList = new BasicEventList<Element>();
        SwingUtilities.invokeAndWait(new StartUIRunnable(eventList));

        // get a handle to an XML file for input
        InputStream xmlIn = XmlBrowser.class.getClassLoader().getResourceAsStream(args[0]);
        new EventListXmlContentHandler(eventList).parse(xmlIn);
    }

    private static class StartUIRunnable implements Runnable {
        private final EventList<Element> eventList;

        public StartUIRunnable(EventList<Element> eventList) {
            this.eventList = eventList;
        }

        public void run() {
            // prepare the table filters
            JTextField filterEdit = new JTextField(15);
            TextFilterator<Element> filterator = GlazedLists.textFilterator(new String[]{"qName", "text"});
            TextComponentMatcherEditor<Element> matcherEditor = new TextComponentMatcherEditor<Element>(filterEdit, filterator);
            JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            filterPanel.add(new JLabel("Filter:"));
            filterPanel.add(filterEdit);

            // convert the XML into an EventList, then a TreeList
            SortedList<Element> sortedList = new SortedList<Element>(eventList, null);
            FilterList<Element> filteredList = new FilterList<Element>(sortedList, matcherEditor);
            TreeList<Element> treeList = new TreeList<Element>(filteredList, new ElementTreeFormat());

            // display the XML in a tree table
            EventTableModel<Element> tableModel = new EventTableModel<Element>(treeList, new ElementTableFormat());
            JTable table = new JTable(tableModel);
            TreeTableSupport.install(table, treeList, 0);
            TableComparatorChooser.install(table, sortedList, TableComparatorChooser.MULTIPLE_COLUMN_KEYBOARD);

            // display the XML in a tree
            EventTreeModel<Element> treeModel = new EventTreeModel<Element>(treeList);
            JTree tree = new JTree(treeModel);
            tree.setRootVisible(false);
            tree.setCellRenderer(new TreeListNodeRenderer());

            // build tha application
            JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, new JScrollPane(tree), new JScrollPane(table));
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

    /**
     * Render a TreeList node in a JTree.
     *
     * TODO: make this available as an API via a factory method?
     */
    private static class TreeListNodeRenderer extends DefaultTreeCellRenderer {
        public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
            Object renderValue;
            if(value instanceof TreeList.Node) {
                TreeList.Node<Element> node = (TreeList.Node<Element>)value;
                renderValue = node.getElement();
            } else {
                // sometimes JTree inexplicably wants to render the root
                renderValue = null;
            }
            return super.getTreeCellRendererComponent(tree, renderValue, selected, expanded, leaf, row, hasFocus);
        }
    }

    /**
     * Adapt {@link TreeList.Node}s for use in a table.
     */
    private static class ElementTableFormat implements WritableTableFormat<Element> {
        public int getColumnCount() {
            return 2;
        }

        public String getColumnName(int column) {
            switch(column) {
                case 0: return "Element";
                case 1: return "Content";
            }
            throw new IndexOutOfBoundsException();
        }

        public Object getColumnValue(Element baseObject, int column) {
            switch(column) {
                case 0: return baseObject.getQName();
                case 1: return baseObject.getText();
            }
            throw new IndexOutOfBoundsException();
        }

        public boolean isEditable(Element baseObject, int column) {
            return true;
        }

        public Element setColumnValue(Element baseObject, Object editedValue, int column) {
            throw new UnsupportedOperationException();
        }
    }

    /**
     * Adapt {@link Element}s for use in a tree.
     */
    private static class ElementTreeFormat implements TreeList.Format<Element> {
        public void getPath(List<Element> path, Element element) {
            if(element == null) return;
            getPath(path, element.parent);
            path.add(element);
        }

        public boolean allowsChildren(Element element) {
            return true;
        }
    }
}
