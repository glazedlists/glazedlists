/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.swing;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.TransformedList;
import ca.odell.glazedlists.TreeList;
import ca.odell.glazedlists.event.ListEvent;
import ca.odell.glazedlists.event.ListEventListener;

import javax.swing.JTree;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Adapt a {@link TreeList} for use in a {@link JTree}.
 *
 * <p><strong>Ongoing problem:</strong> TreeList cannot cache all tree data on
 * the swing thread proxy due to the extra data in a tree.
 *
 * <p><strong>Developer Preview</strong> this class is still under heavy development
 * and subject to API changes. It's also really slow at the moment and won't scale
 * to lists of size larger than a hundred or so efficiently.
 *
 * @author jessewilson
 */
public class EventTreeModel<E> implements TreeModel, ListEventListener<E> {

    /** the proxy moves events to the Swing Event Dispatch thread */
    protected TransformedList swingThreadSource;

    /** <tt>true</tt> indicates that disposing this TreeModel should dispose of the swingThreadSource as well */
    private final boolean disposeSwingThreadSource;

    private TreeList<E> treeList;

    /** swing trees all have only a single root */
    private final Object treeRoot = new Object();

    /** Listeners. */
    protected List<TreeModelListener> listenerList = new ArrayList<>();

    /**
     * Creates a new tree model that extracts the tree data from the given
     * <code>source</code>.
     *
     * @param source a {@link TreeList} that provides the tree data
     */
    public EventTreeModel(TreeList<E> source) {
        // lock the source list for reading since we want to prevent writes
        // from occurring until we fully initialize this EventTableModel
        source.getReadWriteLock().readLock().lock();
        try {
            disposeSwingThreadSource = !GlazedListsSwing.isSwingThreadProxyList(source);
            swingThreadSource = disposeSwingThreadSource ? GlazedListsSwing.swingThreadProxyList(source) : (TransformedList) source;

            // prepare listeners
            swingThreadSource.addListEventListener(this);
        } finally {
            source.getReadWriteLock().readLock().unlock();
        }

        this.treeList = source;
    }

    /** {@inheritDoc} */
    @Override
    public Object getRoot() {
        return treeRoot;
    }

    /** {@inheritDoc} */
    @Override
    public Object getChild(Object parent, int index) {
        if(parent == treeRoot) {
            return treeList.getRoots().get(index);
        } else {
            TreeList.Node<E> node = (TreeList.Node<E>)parent;
            return node.getChildren().get(index);
        }
    }

    /** {@inheritDoc} */
    @Override
    public int getChildCount(Object parent) {
        if(parent == treeRoot) {
            return treeList.getRoots().size();
        } else {
            TreeList.Node<E> node = (TreeList.Node<E>)parent;
            return node.getChildren().size();
        }
    }

    /** {@inheritDoc} */
    @Override
    public boolean isLeaf(Object node) {
        if(node == treeRoot) {
            return treeList.isEmpty();
        } else {
            TreeList.Node<E> treeNode = (TreeList.Node<E>)node;
            return treeNode.isLeaf();
        }
    }

    /** {@inheritDoc} */
    @Override
    public void valueForPathChanged(TreePath path, Object newValue) {
        // todo
    }

    /** {@inheritDoc} */
    @Override
    public int getIndexOfChild(Object parent, Object child) {
        if(parent == treeRoot) {
            return treeList.getRoots().indexOf(child);
        } else {
            TreeList.Node<E> treeNode = (TreeList.Node<E>)parent;
            return treeNode.getChildren().indexOf(child);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void addTreeModelListener(TreeModelListener listener) {
        listenerList.add(listener);
    }

    /** {@inheritDoc} */
    @Override
    public void removeTreeModelListener(TreeModelListener listener) {
        listenerList.remove(listener);
    }

    /** {@inheritDoc} */
    @Override
    public void listChanged(ListEvent<E> listChanges) {
        // todo: fire a more general event
        for(Iterator<TreeModelListener> i = listenerList.iterator(); i.hasNext(); ) {
            TreeModelListener listener = i.next();
            listener.treeStructureChanged(new TreeModelEvent(this, new Object[] { treeRoot }));
        }
    }

    /**
     * Releases the resources consumed by this {@link EventTreeModel} so that it
     * may eventually be garbage collected.
     *
     * <p>An {@link EventTreeModel} will be garbage collected without a call to
     * {@link #dispose()}, but not before its source {@link EventList} is garbage
     * collected. By calling {@link #dispose()}, you allow the {@link EventTreeModel}
     * to be garbage collected before its source {@link EventList}. This is
     * necessary for situations where an {@link EventTreeModel} is short-lived but
     * its source {@link EventList} is long-lived.
     *
     * <p><strong><font color="#FF0000">Warning:</font></strong> It is an error
     * to call any method on an {@link EventTreeModel} after it has been disposed.
     */
    public void dispose() {
        swingThreadSource.removeListEventListener(this);

        // if we created the swingThreadSource then we must also dispose it
        if(disposeSwingThreadSource) {
            swingThreadSource.dispose();
        }

        // this encourages exceptions to be thrown if this model is incorrectly accessed again
        swingThreadSource = null;
    }
}
