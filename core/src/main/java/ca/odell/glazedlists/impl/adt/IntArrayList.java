/* Glazed Lists                                                 (c) 2012       */
/* http://glazedlists.com/                                                     */
package ca.odell.glazedlists.impl.adt;

/**
 * Simple implementation of an array list suitable for storage of primitive integers.
 *
 * @author <a href="mailto:rob@starlight-systems.com">Rob Eden</a>
 */
public class IntArrayList {
	private int[] data;
	private int size = 0;


	/**
	 * Create a list with an initial capacity of ten.
	 */
	public IntArrayList() {
		this( 10 );
	}

	/**
	 * Create a list with the given initial capacity.
	 *
	 * @param initial_capacity  The capacity to initially allow in the list without
	 *                          requiring additional array space to be allocated.
	 */
	public IntArrayList( int initial_capacity ) {
		data = new int[ initial_capacity ];
	}


	/**
	 * Returns the number of entries in the list.
	 */
	public int size() {
		return size;
	}


	/**
	 * Indicates whether or not the list is empty.
	 */
	public boolean isEmpty() {
		return size == 0;
	}


	/**
	 * Clear the existing data in the list.
	 */
	public void clear() {
		size = 0;
	}


	/**
	 * Return the value at the given index. If the index is outside the current bounds
	 * of the list, an exception will be thrown.
	 *
	 * @param index         The index from which to get the value.
	 */
	public int get( int index ) {
		checkAccess( index );

		return data[ index ];
	}


	/**
	 * Add the given value to the end of the list.
	 */
	public void add( int value ) {
		checkGrow( 1 );

		data[ size ] = value;
		size++;
	}


	/**
	 * Set the value of the given index. If the index is outside the existing bounds of
	 * the list, an exception will be thrown.
	 *
	 * @param index         The index at which to set the value.
	 * @param value         The new value to be set.
	 */
	public void set( int index, int value ) {
		checkAccess( index );

		data[ index ] = value;
	}


	/**
	 * Determine if there is sufficient data in the list to make access to the given index
	 * make sense. If it is outside the current bounds of the list, an exception will
	 * be thrown.
	 *
	 * @param index         The index to be accessed.
	 *
	 * @throws IndexOutOfBoundsException    If the index is outside the bounds of the list.
	 */
	private void checkAccess( int index ) {
		if ( size <= index ) {
			throw new IndexOutOfBoundsException( "Index " + index +
				" is outside list bounds (size=" + size + ")" );
		}
	}


	/**
	 * Determines if the existing array has enough space left to add the given amount of
	 * data. If it doesn't, a new array of sufficient size will be created and existing
	 * data will be copied to it.
	 *
	 * @param amount        The amount of entries we would like to add to the list.
	 */
	private void checkGrow( int amount ) {
		if ( size + amount <= data.length ) return;

		int new_length = data.length * 2;
		while( new_length < ( size + amount ) ) {
			new_length = data.length * 2;
		}

		int[] new_data = new int[ new_length ];
		System.arraycopy( data, 0, new_data, 0, size );
		data = new_data;
	}
}
