/**
 * Glazed Lists
 * http://glazedlists.dev.java.net/
 *
 * COPYRIGHT 2003-2005 O'DELL ENGINEERING LTD.
 */
package ca.odell.glazedlists;

import ca.odell.glazedlists.event.ListEvent;
import ca.odell.glazedlists.event.ListEventListener;
import ca.odell.glazedlists.impl.adt.Barcode;
import java.util.List;


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

	public CollectionList(EventList source, CollectionListModel collectionListModel) {
		super(source);
		if(collectionListModel == null) throw new IllegalArgumentException("Collection map cannot be null");

		this.collectionListModel = collectionListModel;

		// Sync the current size and indexes
		for(int i = 0; i < source.size(); i++) {
			barcode.addBlack(barcode.size(), 1);

			// Get the number of children this node has and make sure it's positive
			List children = collectionListModel.getChildren(source.get(i));
			if(!children.isEmpty()) barcode.addWhite(barcode.size(), children.size());
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
        Object parent = source.get(parentIndex);
		List children = collectionListModel.getChildren(parent);

        // get the child
		int childIndexInParent = barcode.getWhiteSequenceIndex(index);
		return children.get(childIndexInParent);
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
	private void handleInsert(int index) {
		// Find the index of the black node with that index
		int absoluteIndex;
		if(index == barcode.blackSize()) {
			absoluteIndex = barcode.size();
		} else {
			absoluteIndex = barcode.getIndex(index, Barcode.BLACK);
		}

		// Find the size of the new node and add it to the total
		List children = collectionListModel.getChildren(source.get(index));
		int childrenSize = children.size();

		// Add the parent node
		barcode.addBlack(absoluteIndex, 1);
        
        // Add the children and fire the event
		if(childrenSize > 0) {
            barcode.addWhite(absoluteIndex + 1, childrenSize);
            int childIndex = absoluteIndex - index;
            updates.addInsert(childIndex, childIndex + childrenSize - 1);
        }
	}
    
	/**
	 * Helper for {@link #listChanged(ListEvent)} when deleting.
	 */
	private void handleDelete(int index) {
		// Find the index of the black node with that index
		int absoluteIndex = barcode.getIndex(index, Barcode.BLACK);

		// Find the index of the NEXT black node (so we know what to delete)
		int nextNodeIndex;
        if(index + 1 < barcode.blackSize()) {
            nextNodeIndex = barcode.getIndex(index + 1, Barcode.BLACK);
        // If there are no more black nodes, simulate it with the full barcode size
        } else {
            nextNodeIndex = barcode.size();
        }
        
		// Remove the nodes
		int removeRange = nextNodeIndex - absoluteIndex;
        barcode.remove(absoluteIndex, removeRange);
        
        // fire the event
        int childIndex = absoluteIndex - index;
        int childrenToDelete = removeRange - 2; // 1 for parent and 1 for inclusive ranges
        if(childrenToDelete > 0) {
            updates.addDelete(childIndex, childIndex + childrenToDelete);
        }
	}
}
