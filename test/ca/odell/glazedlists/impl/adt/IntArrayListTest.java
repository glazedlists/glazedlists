/* Glazed Lists                                                 (c) 2012       */
/* http://glazedlists.com/                                                     */
package ca.odell.glazedlists.impl.adt;

import junit.framework.TestCase;


/**
 * @author <a href="mailto:rob@starlight-systems.com">Rob Eden</a>
 */
public class IntArrayListTest extends TestCase {
	public void testSimpleUsage() {
		IntArrayList list = new IntArrayList( 10 );
		
		// 0-9
		for( int i = 0; i < 10; i++ ) {
			list.add( i );
		}
		assertEquals( 10, list.size() );
		assertEquals( false, list.isEmpty() );
		for( int i = 0; i < 10; i++ ) {
			assertEquals( i, list.get( i ) );
		}
		for( int i = 0; i < 10; i++ ) {
			list.set( i, list.get( i ) + 1 );
		}
		for( int i = 0; i < 10; i++ ) {
			assertEquals( i + 1, list.get( i ) );
		}
		
		
		list.clear();
		assertEquals( 0, list.size() );
		assertEquals( true, list.isEmpty() );
		
		
		// 0-99
		for( int i = 0; i < 100; i++ ) {
			assertEquals( i, list.size() );
			list.add( i * 3 );
			assertEquals( i + 1, list.size() );
		}
		for( int i = 0; i < 100; i++ ) {
			assertEquals( i * 3, list.get( i ) );
		}
		
		
		// Out of bounds get
		try {
			list.get( 100 );
			fail( "Shouldn't get here" );
		}
		catch( IndexOutOfBoundsException ex ) {
			// this is good
		}

		// Out of bounds set
		try {
			list.set( 100, 0 );
			fail( "Shouldn't get here" );
		}
		catch( IndexOutOfBoundsException ex ) {
			// this is good
		}
	}
}
