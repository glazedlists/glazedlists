/**
 * Glazed Lists
 * http://glazedlists.dev.java.net/
 *
 * COPYRIGHT 2003-2005 O'DELL ENGINEERING LTD.
 */
package ca.odell.glazedlists;

import ca.odell.glazedlists.event.ListEvent;
import ca.odell.glazedlists.event.ListEventListener;

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
	private static final int MIN_ARRAY_SIZE = 10;

	private final CollectionListModel collection_list_model;

	/**
	 * Total size of the expanded collection
	 */
	private int total_size;
	/**
	 * Size of the parent nodes
	 */
	private int[] node_sizes;
	/**
	 * Number of {@link #node_sizes} nodes filled
	 */
	private int node_sizes_usage = 0;

	/**
	 * Indices of the nodes. The value for a node is "node[i-1] + node_sizes[i-1]"
	 * This is a performance trade-off because it costs more to keep this up to data
	 * but makes lookups (the common case) more expensive.
	 * Because the values only increase (i.e., it's sorted), it can be quickly searched
	 * using Arrays.binarySearch(int[],int).
	 */
	private int[] node_indices;


	/**
	 * Factor by which array length resizing will be done.
	 *
	 * @see #checkNodeSizeArrayLength(int)
	 */
	private final float resizing_factor;


	/**
	 * Create a new instance with the default resizing factor of 10%.
	 *
	 * @param parent_list           The list containing the parent nodes.
	 * @param collection_list_model A model which maps the parent nodes to their
	 *                              child nodes.
	 */
	public CollectionList(EventList parent_list,
		CollectionListModel collection_list_model) {

		this(parent_list, collection_list_model, 0.2f);
	}


	/**
	 * Creates a new instance where the resizing factor can be specified.
	 *
	 * @param parent_list           The list containing the parent nodes.
	 * @param collection_list_model A model which maps the parent nodes to their
	 *                              child nodes.
	 * @param resize_factor         Factor by which array length resizing will be done
	 *                              (see {@link #checkNodeSizeArrayLength(int)}).
	 */
	protected CollectionList(EventList parent_list,
		CollectionListModel collection_list_model, float resize_factor) {

		super(parent_list);

		this.resizing_factor = resize_factor;

		if (collection_list_model == null)
			throw new IllegalArgumentException("Collection map cannot be null");

		this.collection_list_model = collection_list_model;

		// Sync the current size and indexes
		int parent_size = parent_list.size();
		node_sizes = new int[ parent_size ];
		for (int i = 0; i < parent_size; i++) {
			// Get the number of children this node has and make sure it's positive
			List children = collection_list_model.getChildren(parent_list.get(i));
			int count = children == null ? 0 : children.size();

			node_sizes[ i ] = count;
			node_sizes_usage++;

			total_size += count;
		}
		assert node_sizes_usage == node_sizes.length;

		// Build the index array
		node_indices = new int[ parent_size ];
		node_indices[ 0 ] = 0;					// index 0 is always 0
		updateNodeIndices(0);

		// Listen for events
		parent_list.addListEventListener(this);
	}


	/**
	 * Return the index of the first child in the CollectionList for the given parent
	 * index. This can be very useful for things like selecting the children in a
	 * CollectionList when the parent is selected in another list.
	 *
	 * @see #childEndingIndex
	 */
	public int childStartingIndex(int parent_index) {
		return node_indices[ parent_index ];
	}

	/**
	 * Return the index of the last child in the CollectionList for the given parent
	 * index. This can be very useful for things like selecting the children in a
	 * CollectionList when the parent is selected in another list.
	 *
	 * @see #childStartingIndex
	 */
	public int childEndingIndex(int parent_index) {
		return node_indices[ parent_index ] + node_sizes[ parent_index ];
	}


	/**
	 * {@inheritDoc}
	 */
	public int size() {
		return total_size;
	}


	/**
	 * {@inheritDoc}
	 */
	public Object get(int index) {
		if (index < 0) throw new IllegalArgumentException("Invalid index: " + index);

		if (index >= total_size)
			throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + total_size);

		// Need to find the correct parent and index within the parent
		assert validateIndexCache();
		int parent_index = childToParent(index);
		assert parent_index >= 0;
//		assert parent_index < node_sizes_usage;

		// Now we need to find out it's relative index inside the parent
		int child_index_in_parent = index - node_indices[ parent_index ];

		// Lookup and return the child
//		try {
			List children = collection_list_model.getChildren(source.get(parent_index));
			return children.get(child_index_in_parent);
//		} catch(IndexOutOfBoundsException ex) {
//			System.out.println("Index: " + index);
//			System.out.println("Parent index: " + parent_index);
//			System.out.println("Node size usage: " + node_sizes_usage);
//			System.out.println("Node sizes:");
//			for (int i = 0; i < node_sizes_usage; i++) {
//				if (i != 0) System.out.print(", ");
//				if ((i % 50) == 0) System.out.println();
//
//				System.out.print(node_sizes[ i ]);
//			}
//			System.out.println();
//			System.out.println("Node indices:");
//			for (int i = 0; i < node_sizes_usage; i++) {
//				if (i != 0) System.out.print(", ");
//				System.out.print(node_indices[ i ]);
//			}
//			System.out.println();
//
//			ex.printStackTrace();
//			throw ex;
//		}
	}

	/**
	 * Returns the index of the parent for the given overall (child) index.
	 */
	private int childToParent(int child_index) {
		int index = binarySearch(node_indices, child_index, node_sizes_usage);

		if (index >= 0) {
			// There's a problem with Arrays.binarySearch where two indices right in a row
			// with return one element short. For example, with the following array:
			//				0, 5, 6, 16
			// Searching for "6" would indicate that it should be inserted at position 1.
			// So, we need to see if the next index is an exact match and return it if so.
			if (node_indices.length > (index + 1) &&
				node_indices[ index + 1 ] == child_index) {

				return index + 1;
			}

			return index;			// found it exactly
		} else
			return -index - 2;
	}


	/**
	 * Make sure the {@link #node_sizes} array is neither too big nor too small.
	 *
	 * @param parent_node_count
	 */
	private void checkNodeSizeArrayLength(int parent_node_count) {
		//
		// Basic logic for the following taken from ArrayList
		//
		int old_capacity = node_sizes.length;

		int[] old_node_sizes = node_sizes;
		int[] old_node_indices = node_indices;
		int new_capacity;
		if (parent_node_count > old_capacity) {
			// Grow by 10%
			new_capacity = Math.round(old_capacity * (1.0f + resizing_factor));
		}
		// Don't let it shrink below 10, make sure it's not more than twice the resizing
		// factor bigger than it needs to be.
		else if (old_capacity > MIN_ARRAY_SIZE &&
			old_capacity > (parent_node_count * (1.0f + (resizing_factor * 2)))) {

			// Shrink by 10%
			new_capacity = Math.round(old_capacity * (1.0f - resizing_factor));
		}
		else return;			// do nothing

		// Make sure the arrays don't get resized below the minimum
		new_capacity = Math.max(MIN_ARRAY_SIZE, new_capacity);
		// Make sure it doesn't go below the necessary size
		new_capacity = Math.max(parent_node_count, new_capacity);

		System.out.println("Resizing: old size: " + old_capacity + "  new size: " +
			new_capacity);

		node_sizes = new int[ new_capacity ];
		System.arraycopy(old_node_sizes, 0, node_sizes, 0, node_sizes_usage);

		node_indices = new int[ new_capacity ];
		System.arraycopy(old_node_indices, 0, node_indices, 0, node_sizes_usage);
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
		// Make sure we have enough space in the array for one more node
		checkNodeSizeArrayLength(node_sizes_usage + 1);

		// Move all node at and past the index up one slot
		System.arraycopy(node_sizes, index, node_sizes, index + 1,
			node_sizes.length - index - 1);	// don't copy last node: it's new

		// Find the size of the new node and add it to the total
		List children = collection_list_model.getChildren(source.get(index));
		int size = children == null ? 0 : children.size();
		node_sizes[ index ] = size;
		node_sizes_usage++;
		total_size += node_sizes[ index ];

		// Update the node_indices array starting at the position that changed
		updateNodeIndices(index);

		// Determine the index of the new node in this list
		int new_node_index = 0;
		for (int i = 0; i < index; i++) {
			new_node_index += node_sizes[ i ];
		}

		// Fire an event for this list
		if (size > 0) {
			updates.addInsert(new_node_index, new_node_index + size);
		}
	}

	/**
	 * Helper for {@link #listChanged(ca.odell.glazedlists.event.ListEvent)} when deleting.
	 */
	private void helper_handleDelete(int index) {
		// Remove the size of the node from the total
		int size = node_sizes[ index ];
		total_size -= size;

		// Determine the index of the old node in this list
		int old_node_index = 0;
		for (int i = 0; i < index; i++) {
			old_node_index += node_sizes[ i ];
		}

		// Move all the node aboe the index down one position
		if (index + 1 < node_sizes.length) {
			System.arraycopy(node_sizes, index + 1, node_sizes, index,
				node_sizes.length - index - 1);
		}
		node_sizes_usage--;

		// Update the node_indices array start at the position that was deleted
		updateNodeIndices(index);

		// Compress the array if needed
		checkNodeSizeArrayLength(node_sizes_usage);

		// Fire an event for this list
		if (size > 0) {
			updates.addDelete(old_node_index, old_node_index + size);
		}
	}


	/**
	 * Regenerates the {@link #node_indices} array starting at the given index.
	 *
	 * @param starting_position The index at which we need to recalculate.
	 */
	private void updateNodeIndices(int starting_position) {
		if (starting_position == 0) starting_position = 1;	// index 0 is always 0

		for (int i = starting_position; i < node_sizes_usage; i++) {
			assert node_sizes_usage <= node_indices.length;

			node_indices[ i ] = node_indices[ i - 1 ] + node_sizes[ i - 1 ];
		}
	}


	/**
	 * Basically does the same things as Arrays.binarySearch, except that it allows the
	 * portion of the array to be searched to be specified.
	 *
	 * @see java.util.Arrays#binarySearch(int[],int)
	 */
	private static int binarySearch(int[] array, int key, int length) {
		int low_index = 0;
		int high_index = Math.min(length, array.length) - 1;

		while (low_index <= high_index) {
			int mid_index = (low_index + high_index) / 2;
			int mid_value = array[ mid_index ];

			if (mid_value < key)
				low_index = mid_index + 1;		// key above mid point
			else if (mid_value > key)
				high_index = mid_index - 1;	// key below mid point
			else
				return mid_index;									// key found
		}

		return -(low_index + 1);  	// key not found: use algorithm defined by
		// Arrays.binarySearch
	}


	/**
	 * Used when assertions are enabled to validate that the index cache is correct.
	 */
	private boolean validateIndexCache() {
		if (node_indices[ 0 ] != 0) {
			System.err.println("Index cache invalid at index 0: " + node_indices[ 0 ]);
			return false;
		}

		for (int i = 1; i < node_sizes_usage; i++) {
			if (node_indices[ i ] != node_indices[ i - 1 ] + node_sizes[ i - 1 ]) {
				System.err.println("Index cache invalid at index " + i +
					": should be " + (node_indices[ i - 1 ] + node_sizes[ i - 1 ]) +
					" but is " + node_indices[ i ]);
				return false;
			}
		}

		return true;
	}
}
