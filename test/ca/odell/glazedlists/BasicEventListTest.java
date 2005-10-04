/* Glazed Lists                                                 (c) 2003-2005 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists;

import junit.framework.TestCase;

import java.io.*;
import java.text.NumberFormat;
import java.util.List;
import java.util.ArrayList;

import ca.odell.glazedlists.event.ListEventListener;
import ca.odell.glazedlists.event.ListEvent;

/**
 * Makes sure that {@link BasicEventList} works above and beyond its duties as
 * an {@link EventList}. This includes support for {@link Serializable}.
 *
 * @author <a href="mailto:jesse@swank.ca">Jesse Wilson</a>
 */
public class BasicEventListTest extends TestCase {


    public void testSimpleSerialization() throws IOException, ClassNotFoundException {
        EventList serializableList = new BasicEventList();
        serializableList.addAll(GlazedListsTests.stringToList("Saskatchewan Roughriders"));
        EventList serializedCopy = serialize(serializableList);
        assertEquals(serializableList, serializedCopy);

        serializedCopy.addAll(GlazedListsTests.stringToList("Hamilton Tiger-Cats"));
        assertFalse(serializableList.equals(serializedCopy));
    }

    public void testSerializeEmpty() throws IOException, ClassNotFoundException {
        EventList serializableList = new BasicEventList();
        EventList serializedCopy = serialize(serializableList);
        assertEquals(serializableList, serializedCopy);
    }

    public void testSerializableListeners() throws IOException, ClassNotFoundException {
        EventList serializableList = new BasicEventList();
        SerializableListener listener = new SerializableListener();
        serializableList.addListEventListener(listener);

        serializableList.addAll(GlazedListsTests.stringToList("Szarka"));
        assertEquals(serializableList, SerializableListener.getLastSource());

        EventList serializedCopy = serialize(serializableList);
        assertEquals(serializableList, serializedCopy);

        assertEquals(serializableList, SerializableListener.getLastSource());
        serializedCopy.addAll(GlazedListsTests.stringToList("McCalla"));
        assertEquals(serializedCopy, SerializableListener.getLastSource());
    }

    public void testUnserializableListeners() throws IOException, ClassNotFoundException {
        EventList serializableList = new BasicEventList();
        UnserializableListener listener = new UnserializableListener();
        serializableList.addListEventListener(listener);

        serializableList.addAll(GlazedListsTests.stringToList("Keith"));
        assertEquals(serializableList, UnserializableListener.getLastSource());

        EventList serializedCopy = serialize(serializableList);
        assertEquals(serializableList, serializedCopy);

        assertEquals(serializableList, UnserializableListener.getLastSource());
        serializedCopy.addAll(GlazedListsTests.stringToList("Holmes"));
        assertEquals(serializableList, UnserializableListener.getLastSource());
    }

    public void testSerialVersionUID() {
        assertEquals(4883958173323072345L, ObjectStreamClass.lookup(BasicEventList.class).getSerialVersionUID());
    }

    /**
     * Ensures the serialization format as of October 3, 2005 is still valid today.
     * We created a {@link BasicEventList} containing a sequence of one character
     * Strings, and dumped that to bytes.
     */
    public void testVersion20051003() throws IOException, ClassNotFoundException {
        byte[] serializedBytes = new byte[] {
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

        List expected = new ArrayList();
        expected.addAll(GlazedListsTests.stringToList("October 10, 2005"));

        Object deserialized = fromBytes(serializedBytes);
        assertEquals(expected, deserialized);
        assertEquals("[O, c, t, o, b, e, r,  , 1, 0, ,,  , 2, 0, 0, 5]", deserialized.toString());
    }


    /**
     * Serialize the specified object to bytes, then deserialize it back.
     */
    private static <T> T serialize(T object) throws IOException, ClassNotFoundException {
        return (T)fromBytes(toBytes(object));
    }

    private static byte[] toBytes(Object object) throws IOException {
        ByteArrayOutputStream bytesOut = new ByteArrayOutputStream();
        ObjectOutputStream objectsOut = new ObjectOutputStream(bytesOut);
        objectsOut.writeObject(object);
        return bytesOut.toByteArray();
    }

    private static Object fromBytes(byte[] bytes) throws IOException, ClassNotFoundException {
        ByteArrayInputStream bytesIn = new ByteArrayInputStream(bytes);
        ObjectInputStream objectsIn = new ObjectInputStream(bytesIn);
        return objectsIn.readObject();
    }

    private static String toString(byte[] bytes) {
        StringBuffer result = new StringBuffer();
        for(int b = 0; b < bytes.length; b++) {
            result.append(bytes[b] < 0 ? "-" : " ");
            String hexString = Integer.toString(Math.abs(bytes[b]), 16);
            while(hexString.length() < 2) hexString = "0" + hexString;
            result.append("0x").append(hexString).append(", ");
            if(b % 16 == 15) result.append("\n");
        }
        return result.toString();
    }

    /**
     * This listener invokes a static method each time it's target {@link EventList} is changed.
     */
    private static class SerializableListener implements ListEventListener, Serializable {
        private static EventList lastSource = null;

        public void listChanged(ListEvent listChanges) {
            lastSource = listChanges.getSourceList();
        }
        public static EventList getLastSource() {
            return lastSource;
        }
    }

    /**
     * This listener is not serializable, but it shouldn't prevent serialization on an observing
     * {@link EventList}.
     */
    private static class UnserializableListener implements ListEventListener {
        private static EventList lastSource = null;

        public void listChanged(ListEvent listChanges) {
            lastSource = listChanges.getSourceList();
        }
        public static EventList getLastSource() {
            return lastSource;
        }
    }


}