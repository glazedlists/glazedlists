/**
 * Glazed Lists
 * http://glazedlists.dev.java.net/
 *
 * COPYRIGHT 2003-2005 O'DELL ENGINEERING LTD.
 */
package ca.odell.glazedlists;

import java.util.*;
// the core Glazed Lists package
import ca.odell.glazedlists.event.*;
// volatile implementation support
import ca.odell.glazedlists.impl.adt.*;


/**
 * A list that acts like a tree in that it contains child elements to nodes contained in
 * another list. An example usage would be to wrap a parent list containing record albums
 * and use the CollectionList to display the songs on the album.
 * 
 * <p>The actual mapping from the parent list to the child list (record to songs in the
 * above example) is done by a {@link CollectionListModel} that is provided to the
 * constructor.
 *
 * <p><table border="1" width="100%" cellpadding="3" cellspacing="0">
 * <tr class="tableheadingcolor"><td colspan=2><font size="+2"><b>EventList Overview</b></font></td></tr>
 * <tr><td class="tablesubheadingcolor"><b>Writable:</b></td><td>yes</td></tr>
 * <tr><td class="tablesubheadingcolor"><b>Concurrency:</b></td><td>thread ready, not thread safe</td></tr>
 * <tr><td class="tablesubheadingcolor"><b>Performance:</b></td><td>reads: O(log N), writes O(log N)</td></tr>
 * <tr><td class="tablesubheadingcolor"><b>Memory:</b></td><td>O(N)</td></tr>
 * <tr><td class="tablesubheadingcolor"><b>Unit Tests:</b></td><td>N/A</td></tr>
 * <tr><td class="tablesubheadingcolor"><b>Issues:</b></td><td>N/A</td></tr>
 * </table>
 *
 * @author <a href="mailto:rob@starlight-systems.com">Rob Eden</a>
 * @see CollectionListModel
 */
public class CollectionList extends TransformedList implements ListEventListener {
    
    /** used to extract children */
    private final CollectionListModel collectionListModel;

    /**
     * Barcode containing the node mappings. There is a black node for each parent
     * followed by a white node for each of its children.
     */
    private final Barcode barcode = new Barcode();
    
    /** the Lists and EventLists that this is composed of */
    private final IndexedTree childLists = new IndexedTree();
    
    /**
     * Create a {@link CollectionList} that's contents will be the children of the
     * elements in the specified source {@link EventList}.
     */
    public CollectionList(EventList source, CollectionListModel collectionListModel) {
        super(source);
        if(collectionListModel == null) throw new IllegalArgumentException("Collection map cannot be null");

        this.collectionListModel = collectionListModel;

        // Sync the current size and indexes
        for(int i = 0; i < source.size(); i++) {
            List children = collectionListModel.getChildren(source.get(i));

            // prepare the children to the barcode
            IndexedTreeNode node = childLists.addByNode(i, children);
            barcode.addBlack(barcode.size(), 1);
            if(!children.isEmpty()) barcode.addWhite(barcode.size(), children.size());

            // if the child list fires events, handle them
            if(children instanceof EventList) {
                ChildListListener listener = new ChildListListener((EventList)children, node);
                node.setValue(listener);
            }
        }

        // Listen for events
        source.addListEventListener(this);
    }

    /**
     * {@inheritDoc}
     */
    public int size() {
        // Size of the child nodes only
        return barcode.whiteSize();
    }

    /**
     * {@inheritDoc}
     */
    public Object get(int index) {
        if(index < 0) throw new IndexOutOfBoundsException("Invalid index: " + index);
        if(index >= size()) throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size());

        // get the parent
        int parentIndex = barcode.getBlackBeforeWhite(index);
        List children = getChildren(parentIndex);

        // get the child
        int childIndexInParent = barcode.getWhiteSequenceIndex(index);
        return children.get(childIndexInParent);
    }
    
    /**
     * Hack method for getting the children from the specified child index.
     * This uses some ugly instanceof code on the childLists mixed data structure.
     */
    private List getChildren(int parentIndex) {
        IndexedTreeNode node = childLists.getNode(parentIndex);
        if(node.getValue() instanceof List) return (List)node.getValue();
        else return ((ChildListListener)node.getValue()).getChildren();
    }

    /**
     * Return the index of the first child in the CollectionList for the given parent
     * index. This can be very useful for things like selecting the children in a
     * CollectionList when the parent is selected in another list.
     *
     * @see #childEndingIndex
     */
    public int childStartingIndex(int parentIndex) {
        if(parentIndex < 0) throw new IndexOutOfBoundsException("Invalid index: " + parentIndex);
        if(parentIndex >= source.size()) throw new IndexOutOfBoundsException("Invalid index: " + parentIndex);

        // Get the index of the next node
        // Find the index of the black node with that index
        int parentFullIndex = barcode.getIndex(parentIndex, Barcode.BLACK);
        int childFullIndex = parentFullIndex + 1;
        
        // If this node has no children, the next node index will be past the size or black
        if(childFullIndex >= barcode.size()) return -1;
        if(barcode.get(childFullIndex) != Barcode.WHITE) return -1;

        // return the child index
        int childIndex = childFullIndex - (parentIndex+1);
        assert(barcode.getWhiteIndex(childFullIndex) == childIndex);
        return childIndex;
    }

    /**
     * Return the index of the last child in the CollectionList for the given parent
     * index. This can be very useful for things like selecting the children in a
     * CollectionList when the parent is selected in another list.
     *
     * @see #childStartingIndex
     */
    public int childEndingIndex(int parentIndex) {
        if(parentIndex < 0) throw new IndexOutOfBoundsException("Invalid index: " + parentIndex);
        if(parentIndex >= source.size()) throw new IndexOutOfBoundsException("Invalid index: " + parentIndex);

        // Get the index of the next node
        // Find the index of the black node with that index
        int nextParentFullIndex = (parentIndex == barcode.blackSize() - 1) ? barcode.size() : barcode.getIndex(parentIndex + 1, Barcode.BLACK);
        int lastWhiteBeforeNextParent = nextParentFullIndex - 1;

        // If this node has no children, the next node index will be past the size or black
        if(barcode.get(lastWhiteBeforeNextParent) == Barcode.BLACK) return -1;

        // return the child index
        int childIndex = lastWhiteBeforeNextParent - (parentIndex+1);
        assert(barcode.getWhiteIndex(lastWhiteBeforeNextParent) == childIndex);
        return childIndex;
    }

    /**
     * Handle changes in the parent list. We'll need to update our node list sizes.
     */
    public void listChanged(ListEvent listChanges) {
        // Need to process the changes so that our size caches are up to date.
        updates.beginEvent();
        while(listChanges.next()) {
            int index = listChanges.getIndex();
            int type = listChanges.getType();

            // Insert means we'll need to insert a new node in the array
            if(type == ListEvent.INSERT) {
                handleInsert(index);

            } else if(type == ListEvent.DELETE) {
                handleDelete(index);

            // Treat like a delete and then an add:
            } else if(type == ListEvent.UPDATE) {
                handleDelete(index);
                handleInsert(index);
            }
        }
        updates.commitEvent();
    }


    /**
     * Helper for {@link #listChanged(ListEvent)} when inserting.
     */
    private void handleInsert(int parentIndex) {
        // Find the index of the black node with that index
        int absoluteIndex = getAbsoluteIndex(parentIndex);

        // Find the size of the new node and add it to the total
        Object parent = source.get(parentIndex);
        List children = collectionListModel.getChildren(parent);

        // Add the parent node
        IndexedTreeNode node = childLists.addByNode(parentIndex, children);
        barcode.addBlack(absoluteIndex, 1);
        
        // Add the children and fire the event
        if(!children.isEmpty()) {
            barcode.addWhite(absoluteIndex + 1, children.size());
            int childIndex = absoluteIndex - parentIndex;
            updates.addInsert(childIndex, childIndex + children.size() - 1);
        }

        // if the child list fires events, handle them
        if(children instanceof EventList) {
            ChildListListener listener = new ChildListListener((EventList)children, node);
            node.setValue(listener);
        }
    }
    
    /**
     * Helper for {@link #listChanged(ListEvent)} when deleting.
     */
    private void handleDelete(int parentIndex) {
        // Find the index of the black node with that index
        int absoluteIndex = getAbsoluteIndex(parentIndex);
        int nextNodeIndex = getAbsoluteIndex(parentIndex+1);
        
        // Remove the nodes
        int removeRange = nextNodeIndex - absoluteIndex;
        barcode.remove(absoluteIndex, removeRange);
        IndexedTreeNode removedNode = childLists.removeByIndex(parentIndex);
        
        // fire the event
        int childIndex = absoluteIndex - parentIndex;
        int childrenToDelete = removeRange - 2; // 1 for parent and 1 for inclusive ranges
        if(childrenToDelete > 0) {
            updates.addDelete(childIndex, childIndex + childrenToDelete);
        }
        
        // stop listening for events
        if(removedNode.getValue() instanceof ChildListListener) {
            ((ChildListListener)removedNode.getValue()).dispose();
        }
    }
    
    /**
     * Get the absolute index for the specified parent index. This may be virtual
     * if the parent index is one greater than the last element. This is useful
     * for calculating the size of a range by using the location of its follower.
     */
    private int getAbsoluteIndex(int parentIndex) {
        if(parentIndex < barcode.blackSize()) {
            return barcode.getIndex(parentIndex, Barcode.BLACK);
        } else if(parentIndex == barcode.blackSize()) {
            return barcode.size();
        } else {
            throw new IndexOutOfBoundsException();
        }
    }
    
    /**
     * Monitors changes to a member EventList and forwards changes to all listeners
     * of the CollectionList.
     */
    private class ChildListListener implements ListEventListener {
        private EventList children;
        private IndexedTreeNode node;
        public ChildListListener(EventList children, IndexedTreeNode node) {
            this.children = children;
            this.node = node;
            children.addListEventListener(this);
        }
        public EventList getChildren() {
            return children;
        }
        public void listChanged(ListEvent listChanges) {
            int parentIndex = node.getIndex();
            int absoluteIndex = getAbsoluteIndex(parentIndex);
            int nextNodeIndex = getAbsoluteIndex(parentIndex+1);
            
            // update the barcode
            int firstChildIndex = absoluteIndex + 1;
            int previousChildrenCount = nextNodeIndex - firstChildIndex;
            if(previousChildrenCount > 0) barcode.remove(firstChildIndex, previousChildrenCount);
            if(!children.isEmpty()) barcode.addWhite(firstChildIndex, children.size());
            
            // get the offset of this child list
            int childOffset = absoluteIndex - parentIndex;

            // forward the offset event
            updates.beginEvent();
            while(listChanges.next()) {
                int index = listChanges.getIndex();
                int type = listChanges.getType();
                updates.addChange(type, index + childOffset);
            }
            updates.commitEvent();
        }
        public void dispose() {
            children.removeListEventListener(this);
        }
    }
}
