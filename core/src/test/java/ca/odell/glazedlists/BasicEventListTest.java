/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists;

import ca.odell.glazedlists.event.ListEventAssembler;
import ca.odell.glazedlists.event.ListEventPublisher;
import ca.odell.glazedlists.impl.testing.GlazedListsTests;
import ca.odell.glazedlists.impl.testing.GlazedListsTests.SerializableListener;
import ca.odell.glazedlists.impl.testing.GlazedListsTests.UnserializableListener;
import ca.odell.glazedlists.util.concurrent.LockFactory;
import ca.odell.glazedlists.util.concurrent.ReadWriteLock;

import java.io.IOException;
import java.io.ObjectStreamClass;
import java.io.Serializable;
import java.util.*;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Makes sure that {@link BasicEventList} works above and beyond its duties as
 * an {@link EventList}. This includes support for {@link Serializable}.
 *
 * @author <a href="mailto:jesse@swank.ca">Jesse Wilson</a>
 */
public class BasicEventListTest {

	@Test
	public void testConstructors() {
		BasicEventList<String> list = new BasicEventList<String>();
		assertNotNull(list.getPublisher());
		assertNotNull(list.getReadWriteLock());

		list = new BasicEventList<String>(15);
		assertNotNull(list.getPublisher());
		assertNotNull(list.getReadWriteLock());

		list = new BasicEventList<String>((ReadWriteLock) null);
		assertNotNull(list.getPublisher());
		assertNotNull(list.getReadWriteLock());

		final ReadWriteLock lock = LockFactory.DEFAULT.createReadWriteLock();
		list = new BasicEventList<String>(lock);
		assertNotNull(list.getPublisher());
		assertEquals(lock, list.getReadWriteLock());

		list = new BasicEventList<String>(null, null);
		assertNotNull(list.getPublisher());
		assertNotNull(list.getReadWriteLock());
		final ListEventPublisher publisher = ListEventAssembler.createListEventPublisher();
		list = new BasicEventList<String>(publisher, lock);
		assertEquals(lock, list.getReadWriteLock());
		assertEquals(publisher, list.getPublisher());

		list = new BasicEventList<String>(15, null, null);
		assertNotNull(list.getPublisher());
		assertNotNull(list.getReadWriteLock());

		list = new BasicEventList<String>(15, publisher, lock);
		assertEquals(lock, list.getReadWriteLock());
		assertEquals(publisher, list.getPublisher());
	}


    @Test
    public void testSimpleSerialization() throws IOException, ClassNotFoundException {
        EventList<String> serializableList = new BasicEventList<String>();
        serializableList.addAll(GlazedListsTests.stringToList("Saskatchewan Roughriders"));
        EventList<String> serializedCopy = GlazedListsTests.serialize(serializableList);
        assertEquals(serializableList, serializedCopy);

        serializedCopy.addAll(GlazedListsTests.stringToList("Hamilton Tiger-Cats"));
        assertFalse(serializableList.equals(serializedCopy));
    }

    @Test
    public void testSerializeEmpty() throws IOException, ClassNotFoundException {
        EventList serializableList = new BasicEventList();
        EventList serializedCopy = GlazedListsTests.serialize(serializableList);
        assertEquals(serializableList, serializedCopy);
    }

    @Test
    public void testSerializableListeners() throws IOException, ClassNotFoundException {
        EventList<String> serializableList = new BasicEventList<String>();
        SerializableListener listener = new SerializableListener();
        serializableList.addListEventListener(listener);

        serializableList.addAll(GlazedListsTests.stringToList("Szarka"));
        assertEquals(serializableList, SerializableListener.getLastSource());

        EventList<String> serializedCopy = GlazedListsTests.serialize(serializableList);
        assertEquals(serializableList, serializedCopy);

        assertEquals(serializableList, SerializableListener.getLastSource());
        serializedCopy.addAll(GlazedListsTests.stringToList("McCalla"));
        assertEquals(serializedCopy, SerializableListener.getLastSource());
    }

    @Test
    public void testUnserializableListeners() throws IOException, ClassNotFoundException {
        EventList<String> serializableList = new BasicEventList<String>();
        UnserializableListener listener = new UnserializableListener();
        serializableList.addListEventListener(listener);

        serializableList.addAll(GlazedListsTests.stringToList("Keith"));
        assertEquals(serializableList, UnserializableListener.getLastSource());

        EventList<String> serializedCopy = GlazedListsTests.serialize(serializableList);
        assertEquals(serializableList, serializedCopy);

        assertEquals(serializableList, UnserializableListener.getLastSource());
        serializedCopy.addAll(GlazedListsTests.stringToList("Holmes"));
        assertEquals(serializableList, UnserializableListener.getLastSource());
    }

    @Test
    public void testSerialVersionUID() {
        assertEquals(4883958173323072345L, ObjectStreamClass.lookup(BasicEventList.class).getSerialVersionUID());
    }

    /**
     * Ensures the serialization format as of October 3, 2005 is still valid today.
     * We created a {@link BasicEventList} containing a sequence of one character
     * Strings, and dumped that to bytes.
     */
    @Test
    public void testVersion20051003() throws IOException, ClassNotFoundException {
        byte[] serializedBytes = {
            -0x54, -0x13,  0x00,  0x05,  0x73,  0x72,  0x00,  0x23,  0x63,  0x61,  0x2e,  0x6f,  0x64,  0x65,  0x6c,  0x6c,
             0x2e,  0x67,  0x6c,  0x61,  0x7a,  0x65,  0x64,  0x6c,  0x69,  0x73,  0x74,  0x73,  0x2e,  0x42,  0x61,  0x73,
             0x69,  0x63,  0x45,  0x76,  0x65,  0x6e,  0x74,  0x4c,  0x69,  0x73,  0x74,  0x43, -0x39,  0x4e,  0x15,  0x12,
            -0x37, -0x6d,  0x59,  0x03,  0x00,  0x01,  0x4c,  0x00,  0x04,  0x64,  0x61,  0x74,  0x61,  0x74,  0x00,  0x10,
             0x4c,  0x6a,  0x61,  0x76,  0x61,  0x2f,  0x75,  0x74,  0x69,  0x6c,  0x2f,  0x4c,  0x69,  0x73,  0x74,  0x3b,
             0x78,  0x70,  0x75,  0x72,  0x00,  0x13,  0x5b,  0x4c,  0x6a,  0x61,  0x76,  0x61,  0x2e,  0x6c,  0x61,  0x6e,
             0x67,  0x2e,  0x4f,  0x62,  0x6a,  0x65,  0x63,  0x74,  0x3b, -0x70, -0x32,  0x58, -0x61,  0x10,  0x73,  0x29,
             0x6c,  0x02,  0x00,  0x00,  0x78,  0x70,  0x00,  0x00,  0x00,  0x10,  0x74,  0x00,  0x01,  0x4f,  0x74,  0x00,
             0x01,  0x63,  0x74,  0x00,  0x01,  0x74,  0x74,  0x00,  0x01,  0x6f,  0x74,  0x00,  0x01,  0x62,  0x74,  0x00,
             0x01,  0x65,  0x74,  0x00,  0x01,  0x72,  0x74,  0x00,  0x01,  0x20,  0x74,  0x00,  0x01,  0x31,  0x74,  0x00,
             0x01,  0x30,  0x74,  0x00,  0x01,  0x2c,  0x74,  0x00,  0x01,  0x20,  0x74,  0x00,  0x01,  0x32,  0x74,  0x00,
             0x01,  0x30,  0x74,  0x00,  0x01,  0x30,  0x74,  0x00,  0x01,  0x35,  0x75,  0x72,  0x00,  0x2f,  0x5b,  0x4c,
             0x63,  0x61,  0x2e,  0x6f,  0x64,  0x65,  0x6c,  0x6c,  0x2e,  0x67,  0x6c,  0x61,  0x7a,  0x65,  0x64,  0x6c,
             0x69,  0x73,  0x74,  0x73,  0x2e,  0x65,  0x76,  0x65,  0x6e,  0x74,  0x2e,  0x4c,  0x69,  0x73,  0x74,  0x45,
             0x76,  0x65,  0x6e,  0x74,  0x4c,  0x69,  0x73,  0x74,  0x65,  0x6e,  0x65,  0x72,  0x3b,  0x32,  0x43,  0x6c,
             0x62, -0x52,  0x4f,  0x32,  0x0d,  0x02,  0x00,  0x00,  0x78,  0x70,  0x00,  0x00,  0x00,  0x00,  0x78,
        };

        List<String> expected = new ArrayList<String>();
        expected.addAll(GlazedListsTests.stringToList("October 10, 2005"));

        Object deserialized = GlazedListsTests.fromBytes(serializedBytes);
        assertEquals(expected, deserialized);
        assertEquals("[O, c, t, o, b, e, r,  , 1, 0, ,,  , 2, 0, 0, 5]", deserialized.toString());
    }

    /**
     * This test adds 4 BasicEventList<String> to a serialization container
     * (a simple ArrayList). All 4 BasicEventLists are constructed to share the
     * SAME Locks and Publisher. The test then serializes/deserializes the
     * container and ensures that the 4 BasicEventLists still share common
     * Locks and Publisher, though the identity is not expected or required
     * to be preserved after deserialization.
     */
    @Test
    public void testSerializableLocksAndPublisher() throws IOException, ClassNotFoundException {
        // 1. create the Lock and Publisher that will be shared by all BasicEventLists
        final ReadWriteLock sharedLock = LockFactory.DEFAULT.createReadWriteLock();
        final ListEventPublisher sharedPublisher = ListEventAssembler.createListEventPublisher();

        // 2. add 4 BasicEventLists to a container, each of which shares a common Publisher and ReadWriteLocks
        final List<EventList<String>> serializationContainer = new ArrayList<EventList<String>>();
        for (int i = 0; i < 4; i++) {
            final EventList<String> eventList = new BasicEventList<String>(sharedPublisher, sharedLock);
            eventList.add("Test " + i);
            serializationContainer.add(eventList);
        }

        // 3. serialize/deserialize the container
        final List<EventList<String>> serializedCopy = GlazedListsTests.serialize(serializationContainer);
        assertEquals(serializationContainer, serializedCopy);

        // 4. ensure deserialized lists still share the lock and publisher
        final ListEventPublisher publisher = serializedCopy.get(0).getPublisher();
        final ReadWriteLock lock = serializedCopy.get(0).getReadWriteLock();
        final CompositeList<String> compositeList = new CompositeList<String>(publisher, lock);
        for (int i = 0; i < 4; i++) {
            // explicitly check the identity of the publisher and lock
            final EventList<String> eventList = serializedCopy.get(i);
            assertSame(publisher, eventList.getPublisher());
            assertSame(lock, eventList.getReadWriteLock());

            // as a result, CompositeList should accept the BasicEventList for use
            compositeList.addMemberList(eventList);
        }
    }


    @Test
	public void testRemoveIf() {
    	EventList<String> list = new BasicEventList<>();
    	list.add( "One" );
    	list.add( "Two" );
    	list.add( "Three" );
    	list.removeIf( s -> s.startsWith( "T" ) );

    	assertEquals( Collections.singletonList( "One" ), list );
    }

    @Test
	public void testRemoveAll_list() {
	    EventList<String> list = new BasicEventList<>();
	    list.add( "One" );
	    list.add( "Two" );
	    list.add( "Three" );

	    // This hits a different logic path than removals based on Sets
	    list.removeAll( Arrays.asList( "Three", "Two" ) );

	    assertEquals( Collections.singletonList( "One" ), list );
    }

	@Test
	public void testRemoveAll_set() {
		EventList<String> list = new BasicEventList<>();
		list.add( "One" );
		list.add( "Two" );
		list.add( "Three" );

		// This hits a different logic path than removals based on non-Sets
		list.removeAll( new HashSet<>( Arrays.asList( "Three", "Two" ) ) );

		assertEquals( Collections.singletonList( "One" ), list );
	}

	@Test
    public void testRetainAll() {
        EventList<String> list = new BasicEventList<>();
        list.add( "One" );
        list.add( "Two" );
        list.add( "Three" );

        list.retainAll( Arrays.asList( "Four", "Three", "Two" ) );

        assertEquals( Arrays.asList( "Two", "Three" ), list );
    }
}
