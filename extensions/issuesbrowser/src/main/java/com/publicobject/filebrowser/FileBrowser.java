/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package com.publicobject.filebrowser;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.SortedList;
import ca.odell.glazedlists.TreeList;
import ca.odell.glazedlists.gui.TableFormat;
import ca.odell.glazedlists.swing.GlazedListsSwing;
import ca.odell.glazedlists.swing.TableComparatorChooser;
import ca.odell.glazedlists.swing.TreeTableSupport;

import java.util.Comparator;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.TableModel;

/**
 * A simplistic file system browser for exploring the TreeList API.
 *
 * @author <a href="mailto:jesse@swank.ca">Jesse Wilson</a>
 */
public class FileBrowser implements Runnable {

    private FileBrowserModel fileBrowserModel;

    public FileBrowser(FileBrowserModel fileBrowserModel) {
        this.fileBrowserModel = fileBrowserModel;
    }

    public static void main(String[] args) {
        FileBrowserModel fileBrowserModel = new FileBrowserModel(args);
        SwingUtilities.invokeLater(new FileBrowser(fileBrowserModel));
        fileBrowserModel.run();
    }

    @Override
    public void run() {
        TableFormat<Entry> tableFormat = new EntryTableFormat();
        TreeList.Format<Entry> treeFormat = new EntryTreeFormat();

        EventList<Entry> sourceEntries = fileBrowserModel.getEntries();
        sourceEntries.getReadWriteLock().writeLock().lock();
        try {
            EventList<Entry> entries = GlazedListsSwing.swingThreadProxyList(sourceEntries);

            SortedList<Entry> sortedEntries = new SortedList<Entry>(entries, null);

            TreeList<Entry> treeList = new TreeList<Entry>(sortedEntries, treeFormat, TreeList.<Entry>nodesStartExpanded());
            TableModel model = GlazedListsSwing.eventTableModel(treeList, tableFormat);
            JTable table = new JTable(model);
            TreeTableSupport.install(table, treeList, 0);
            TableComparatorChooser.install(table, sortedEntries, TableComparatorChooser.SINGLE_COLUMN);

            JFrame frame = new JFrame(fileBrowserModel.getRoot().getName());
            frame.setSize(640, 480);
            frame.getContentPane().add(new JScrollPane(table));
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setVisible(true);
        } finally {
            sourceEntries.getReadWriteLock().writeLock().unlock();
        }

    }

    /**
     * Describe how to show an {@link com.publicobject.filebrowser.Entry} in a hierarchy.
     */
    private class EntryTreeFormat implements TreeList.Format<Entry> {
        @Override
        public void getPath(List<Entry> path, Entry element) {
            for(Entry e = element; e != null; e = e.getParent()) {
                path.add(0, e);
            }
        }
        @Override
        public boolean allowsChildren(Entry element) {
            return element.isDirectory();
        }

        @Override
        public Comparator<Entry> getComparator(int depth) {
            return GlazedLists.comparableComparator();
        }
    }

    private class EntryTableFormat implements TableFormat<Entry> {
        @Override
        public int getColumnCount() {
            return 5;
        }
        @Override
        public String getColumnName(int column) {
            switch(column) {
                case 0: return "Name";
                case 1: return "Date Modified";
                case 2: return "Date Created";
                case 3: return "Size";
                case 4: return "Kind";
            }
            throw new IllegalStateException();
        }
        @Override
        public Object getColumnValue(Entry baseObject, int column) {
            switch(column) {
                case 0: return baseObject.getName();
                case 1: return baseObject.getDateModified();
                case 2: return baseObject.getDateCreated();
                case 3: return new Long(baseObject.getSize());
                case 4: return baseObject.getKind();
            }
            throw new IllegalStateException();
        }
    }
}
