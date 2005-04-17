/* Glazed Lists                                                 (c) 2003-2005 */
/* http://publicobject.com/glazedlists/                      publicboject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
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
 * <tr><td class="tablesubheadingcolor"><b>Writable:</b></td><td>only {@link #set(int,Object)} and {@link #remove(int)}</td></tr>
 * <tr><td class="tablesubheadingcolor"><b>Concurrency:</b></td><td>thread ready, not thread safe</td></tr>
 * <tr><td class="tablesubheadingcolor"><b>Performance:</b></td><td>reads: O(log N), writes O(log N)</td></tr>
 * <tr><td class="tablesubheadingcolor"><b>Memory:</b></td><td>96 bytes per element</td></tr>
 * <tr><td class="tablesubheadingcolor"><b>Unit Tests:</b></td><td>N/A</td></tr>
 * <tr><td class="tablesubheadingcolor"><b>Issues:</b></td><td>N/A</td></tr>
 * </table>
 *
 * @see CollectionListModel
 *
 * @author <a href="mailto:rob@starlight-systems.com">Rob Eden</a>
 * @author <a href="mailto:jesse@odel.on.ca">Jesse Wilson</a>
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
    private final IndexedTree childElements = new IndexedTree();
    
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

            // update the list of child lists
            IndexedTreeNode node = childElements.addByNode(i, this);
            node.setValue(createChildElementForList(children, node));
            
            // update the barcode
            barcode.addBlack(barcode.size(), 1);
            if(!children.isEmpty()) barcode.addWhite(barcode.size(), children.size());
        }

        // Listen for events
        source.addListEventListener(this);
    }
    
    /** {@inheritDoc} */
    public int size() {
        // Size of the child nodes only
        return barcode.whiteSize();
    }

    /** {@inheritDoc} */
    public Object get(int index) {
        // get the child
        ChildElement childElement = getChildElement(index);
        int childIndexInParent = barcode.getWhiteSequenceIndex(index);
        return childElement.get(childIndexInParent);
    }


    /** {@inheritDoc} */
    public Object set(int index, Object value) {
        // set on the child
        ChildElement childElement = getChildElement(index);
        int childIndexInParent = barcode.getWhiteSequenceIndex(index);
        return childElement.set(childIndexInParent, value);
    }

    /** {@inheritDoc} */
    public Object remove(int index) {
        // remove from the child
        ChildElement childElement = getChildElement(index);
        int childIndexInParent = barcode.getWhiteSequenceIndex(index);
        return childElement.remove(childIndexInParent);
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

        // update the list of child lists
        IndexedTreeNode node = childElements.addByNode(parentIndex, this);
        node.setValue(createChildElementForList(children, node));
        
        // update the barcode
        barcode.addBlack(absoluteIndex, 1);
        if(!children.isEmpty()) barcode.addWhite(absoluteIndex + 1, children.size());
        
        // add events
        if(!children.isEmpty()) {
            int childIndex = absoluteIndex - parentIndex;
            updates.addInsert(childIndex, childIndex + children.size() - 1);
        }
    }
    
    /**
     * Helper for {@link #listChanged(ListEvent)} when deleting.
     */
    private void handleDelete(int parentIndex) {
        // Find the index of the black node with that index
        int absoluteIndex = getAbsoluteIndex(parentIndex);
        int nextNodeIndex = getAbsoluteIndex(parentIndex+1);
        
        // update the list of child lists
        ChildElement removedChildElement = (ChildElement)childElements.removeByIndex(parentIndex).getValue();
        removedChildElement.dispose();
        
        // update the barcode
        int removeRange = nextNodeIndex - absoluteIndex;
        barcode.remove(absoluteIndex, removeRange);

        // add events
        int childrenToDelete = removeRange - 2; // 1 for parent and 1 for inclusive ranges
        if(childrenToDelete > 0) {
            int childIndex = absoluteIndex - parentIndex;
            updates.addDelete(childIndex, childIndex + childrenToDelete);
        }
    }
    
    /**
     * Get the child element for the specified child index.
     */
    private ChildElement getChildElement(int childIndex) {
        if(childIndex < 0) throw new IndexOutOfBoundsException("Invalid index: " + childIndex);
        if(childIndex >= size()) throw new IndexOutOfBoundsException("Index: " + childIndex + ", Size: " + size());
        
        // get the child element
        int parentIndex = barcode.getBlackBeforeWhite(childIndex);
        return (ChildElement)childElements.getNode(parentIndex).getValue();
    }
    
    /**
     * Create a {@link ChildElement} for the specified List.
     */
    private ChildElement createChildElementForList(List children, IndexedTreeNode node) {
        if(children instanceof EventList) return new EventChildElement((EventList)children, node);
        else return new SimpleChildElement(children, node);
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
     * Models a list held by the CollectionList.
     */
    private interface ChildElement {
        public Object get(int index);
        public Object remove(int index);
        public Object set(int index, Object element);
        public void dispose();
    }
    
    /**
     * Manages a standard List that does not implement {@link EventList}.
     */
    private class SimpleChildElement implements ChildElement {
        private List children;
        private IndexedTreeNode node;
        public SimpleChildElement(List children, IndexedTreeNode node) {
            this.children = children;
            this.node = node;
        }
        public Object get(int index) {
            return children.get(index);
        }
        public Object remove(int index) {
            Object removed = children.remove(index);
            
            // update the barcode
            int parentIndex = node.getIndex();
            int absoluteIndex = getAbsoluteIndex(parentIndex);
            int firstChildIndex = absoluteIndex + 1;
            barcode.remove(firstChildIndex + index, 1);

            // forward the offset event
            int childOffset = absoluteIndex - parentIndex;
            updates.beginEvent();
            updates.addDelete(index + childOffset);
            updates.commitEvent();
            
            // all done
            return removed;
        }
        public Object set(int index, Object element) {
            Object replaced = children.set(index, element);
            
            // forward the offset event
            int parentIndex = node.getIndex();
            int absoluteIndex = getAbsoluteIndex(parentIndex);
            int childOffset = absoluteIndex - parentIndex;
            updates.beginEvent();
            updates.addUpdate(index + childOffset);
            updates.commitEvent();
            
            // all done
            return replaced;
        }
        public void dispose() {
            // do nothing
        }
    }
    
    /**
     * Monitors changes to a member EventList and forwards changes to all listeners
     * of the CollectionList.
     */
    private class EventChildElement implements ChildElement, ListEventListener {
        private EventList children;
        private IndexedTreeNode node;
        public EventChildElement(EventList children, IndexedTreeNode node) {
            this.children = children;
            this.node = node;
            children.addListEventListener(this);
        }
        public Object get(int index) {
            return children.get(index);
        }
        public Object remove(int index) {
            // events will be fired from this call
            return children.remove(index);
        }
        public Object set(int index, Object element) {
            // events will be fired from this call
            return children.set(index, element);
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
