/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package com.publicobject.xmlbrowser;

import java.util.List;
import ca.odell.glazedlists.*;
import ca.odell.glazedlists.impl.testing.ListConsistencyListener;
import ca.odell.glazedlists.matchers.TextMatcherEditor;
import ca.odell.glazedlists.gui.WritableTableFormat;
import ca.odell.glazedlists.swing.*;

import javax.swing.*;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.awt.*;
import java.io.InputStream;
import java.io.IOException;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;

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

        private final EventList<Element> target = GlazedLists.threadSafeList(new BasicEventList<Element>());
        private final List<Element> stack = new ArrayList<Element>();

        public static EventList<Element> create(InputStream inputStream) {
            try {
                XMLReader xmlReader = SAXParserFactory.newInstance().newSAXParser().getXMLReader();
                SaxParserSidekick.install(xmlReader);

                EventListXmlContentHandler handler = new EventListXmlContentHandler();
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


    public static void main(String[] args) {
        if(args.length != 1) {
            System.out.println("Usage: XmlBrowser <xmlfile>");
            return;
        }

        SwingUtilities.invokeLater(new StartUIRunnable(args[0]));
    }

    private static class StartUIRunnable implements Runnable {
        private String xmlFile;

        public StartUIRunnable(String xmlFile) {
            this.xmlFile = xmlFile;
        }

        public void run() {
            // get a handle to an XML file for input
            InputStream xmlIn = null;
            try {
                xmlIn = new FileInputStream(xmlFile);
            } catch(FileNotFoundException e) {
                throw new RuntimeException(e);
            }

            // prepare the table filters
            JTextField filterEdit = new JTextField(15);
            TextFilterator<Element> filterator = GlazedLists.textFilterator(new String[]{"qName", "text"});
            TextComponentMatcherEditor<Element> matcherEditor = new TextComponentMatcherEditor<Element>(filterEdit, filterator);
            JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            filterPanel.add(new JLabel("Filter:"));
            filterPanel.add(filterEdit);

            // convert the XML into an EventList, then a TreeList
            EventList<Element> eventList = EventListXmlContentHandler.create(xmlIn);
            SortedList<Element> sortedList = new SortedList<Element>(eventList, null);
            FilterList<Element> filteredList = new FilterList<Element>(sortedList, matcherEditor);
            TreeList<Element> treeList = new TreeList<Element>(filteredList, new ElementTreeFormat());
            ListConsistencyListener consistencyListener = ListConsistencyListener.install(treeList);
            consistencyListener.setPreviousElementTracked(false);

            // display the XML in a tree table
            EventTableModel<Element> tableModel = new EventTableModel<Element>(treeList, new ElementTableFormat());
            JTable table = new JTable(tableModel);
            TreeTableSupport.install(table, treeList, 0);
            TableComparatorChooser.install(table, sortedList, TableComparatorChooser.MULTIPLE_COLUMN_KEYBOARD);

            // display the XML in a tree
            EventTreeModel<Element> treeModel = new EventTreeModel<Element>(treeList);
            JTree tree = new JTree(treeModel);

            // build tha application
            JPanel panel = new JPanel(new BorderLayout());
            panel.add(filterPanel, BorderLayout.NORTH);
            panel.add(new JScrollPane(table), BorderLayout.CENTER);
            panel.add(new JScrollPane(tree), BorderLayout.WEST);
            JFrame frame = new JFrame("XML Browser");
            frame.getContentPane().add(panel);
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setVisible(true);
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
