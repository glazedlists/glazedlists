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
 * <p/>
 * <p>The actual mapping from the parent list to the child list (record to songs in the
 * above example) is done by a {@link CollectionListModel} that is provided to the
 * constructor.
 * <p/>
 * <p><strong><font color="#FF0000">Warning:</font></strong> This class is
 * thread ready but not thread safe. See {@link EventList} for an example
 * of thread safe code.
 *
 * @author <a href="mailto:rob@starlight-systems.com">Rob Eden</a>
 * @see CollectionListModel
 */
public class CollectionList extends TransformedList implements ListEventListener {
	private final EventList parent_list;
	private final CollectionListModel collection_list_model;

	/**
	 * Barcode containing the node mappings. There is a black node for each parent
	 * followed by a white node for each of its children.
	 */
	private final Barcode node_barcode = new Barcode();

	public CollectionList(EventList parent_list, CollectionListModel collection_list_model) {
		super(parent_list);

		readWriteLock = parent_list.getReadWriteLock();

		if (parent_list == null)
			throw new IllegalArgumentException("Parent list cannot be null");
		if (collection_list_model == null)
			throw new IllegalArgumentException("Collection map cannot be null");

		this.parent_list = parent_list;
		this.collection_list_model = collection_list_model;

		// Sync the current size and indexes
		int parent_size = parent_list.size();
		int index = 0;
		for (int i = 0; i < parent_size; i++) {
			node_barcode.addBlack(index++, 1);

			// Get the number of children this node has and make sure it's positive
			List children = collection_list_model.getChildren(parent_list.get(i));
			int count = children == null ? 0 : children.size();

			node_barcode.addWhite(index, count);
			index += count;
		}

		// Listen for events
		parent_list.addListEventListener(this);
	}

	/**
	 * {@inheritDoc}
	 */
	public int size() {
		// Size of the child nodes only
		return node_barcode.whiteSize();
	}

	/**
	 * {@inheritDoc}
	 */
	public Object get(int index) {
		if (index < 0) throw new IllegalArgumentException("Invalid index: " + index);

		if (index >= size())
			throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size());

		// Need to find the correct parent and index within the parent
		int parent_index = node_barcode.getBlackBeforeWhite(index);

		int index_in_parent = node_barcode.getWhiteSequenceIndex(index);

		List children = collection_list_model.getChildren(parent_list.get(parent_index));
		return children.get(index_in_parent);
	}


	/**
	 * Return the index of the first child in the CollectionList for the given parent
	 * index. This can be very useful for things like selecting the children in a
	 * CollectionList when the parent is selected in another list.
	 *
	 * @see #childEndingIndex
	 */
	public int childStartingIndex(int parent_index) {
		// Handle bad input
		if (parent_index < 0) return -1;

		// Make sure we're not overstepping the barcode
		if (node_barcode.blackSize() <= parent_index) return -1;

		// Get the index of the next node
		// Find the index of the black node with that index
		int next_node_index = node_barcode.getIndex(parent_index, Barcode.BLACK) + 1;

		// Make sure we're not overstepping the barcode
		if (node_barcode.size() <= next_node_index) return -1;

		// Make sure it's white
		Object color = node_barcode.get(next_node_index);
		if (Barcode.BLACK.equals(color)) return -1;

		return node_barcode.getWhiteIndex(next_node_index);
	}

	/**
	 * Return the index of the last child in the CollectionList for the given parent
	 * index. This can be very useful for things like selecting the children in a
	 * CollectionList when the parent is selected in another list.
	 *
	 * @see #childStartingIndex
	 */
	public int childEndingIndex(int parent_index) {
		// Get the starting index and add the size
		int starting_index = childStartingIndex(parent_index);
		if (starting_index < 0) return -1;

		List children = collection_list_model.getChildren(parent_list.get(parent_index));
		return starting_index + children.size() - 1;
	}

	/**
	 * Handle changes in the parent list. We'll need to update our node list sizes.
	 */
	public void listChanged(ListEvent listChanges) {
		// Need to process the changes so that our size caches are up to date.
		updates.beginEvent();
		while (listChanges.next()) {
			int index = listChanges.getIndex();

			switch(listChanges.getType()) {
				// Insert means we'll need to insert a new node in the array
				case ListEvent.INSERT:
					helper_handleInsert(index);
					break;
				case ListEvent.DELETE:
					helper_handleDelete(index);
					break;
				case ListEvent.UPDATE:
					// Treat like a delete and then an add:
					helper_handleDelete(index);
					helper_handleInsert(index);

					break;
			}
		}
		updates.commitEvent();
	}


	/**
	 * Helper for {@link #listChanged(ca.odell.glazedlists.event.ListEvent)} when inserting.
	 */
	private void helper_handleInsert(int index) {
		// Find the index of the black node with that index
		int absolute_index;
		if (node_barcode.blackSize() <= index) {
			absolute_index = node_barcode.size();
		} else {
			absolute_index = node_barcode.getIndex(index, Barcode.BLACK);
		}

		// Find the size of the new node and add it to the total
		List children = collection_list_model.getChildren(parent_list.get(index));
		int size = children == null ? 0 : children.size();

		// Add the nodes
		node_barcode.addBlack(absolute_index, 1);
		if (size != 0) node_barcode.addWhite(absolute_index + 1, size);

		// Fire an event for this list
		updates.addInsert(absolute_index, absolute_index + size);
	}

	/**
	 * Helper for {@link #listChanged(ca.odell.glazedlists.event.ListEvent)} when deleting.
	 */
	private void helper_handleDelete(int index) {
		// Find the index of the black node with that index
		// Find the index of the black node with that index
		int absolute_index;
		if (node_barcode.blackSize() <= index) {
			absolute_index = node_barcode.size();
		} else {
			absolute_index = node_barcode.getIndex(index, Barcode.BLACK);
		}

		// Find the index of the NEXT black node (so we know what to delete)
		int next_node_index;
		try {
			next_node_index = node_barcode.getIndex(index + 1, Barcode.BLACK);
		} catch(NullPointerException ex) {
			// This can happen if there are no more black nodes
			next_node_index = -1;
		}
		// If there is no next node, act like it's after the last node
		if (next_node_index == -1) next_node_index = node_barcode.size();

		// Remove the nodes
		int size = next_node_index - absolute_index;
		node_barcode.remove(absolute_index, size);

		// Fire an event for this list
		updates.addDelete(absolute_index, absolute_index + size);
	}
}
