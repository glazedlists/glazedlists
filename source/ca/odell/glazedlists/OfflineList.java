/**
 * Glazed Lists
 * http://glazedlists.dev.java.net/
 *
 * COPYRIGHT 2003 O'DELL ENGINEERING LTD.
 */
package ca.odell.glazedlists;

import java.util.*;
import java.io.*;

/**
 * List implementation backed out by a temporary disk file.
 * It deviates from {@link List} contract because it accepts
 * only {@link Serializable} elements and it can throw extra
 * {@link RuntimeException}s if I/O error occures. The first
 * can be eliminated by reimplmenting {@link #toBytes} and
 * {@link #fromBytes} methods.
 *
 * <p>Use {@link Collections#synchronizedList(List)} to get thread
 * safe implementation.
 *
 * <p><strong>Perfomance Note:</strong>This implementation is not
 * optimized. The file never shrinks and there is no in-memory cache.
 * For best performance, wrap this list in a BasicEventList, then wrap
 * that list in a CachingList:<br>
 * <code>EventList myList = new CachingList(new BasicEventList(new FileList()), 100);</code>
 *
 * <p><font size="5"><strong><font color="#FF0000">Warning:</font></strong> This
 * class is a technology preview and is subject to API changes.</font>
 *
 * @author Petr Kuzel
 */
public class OfflineList extends AbstractList implements List {

    /** Backend file. */
    private RandomAccessFile file;

    /** Map list indexes to file positions. */
    private List map = new ArrayList();

    /**
     * Creates new file based list.
     * @throws IOException if cannot create backend file
     */
    public OfflineList() throws IOException {
        File tmp = File.createTempFile("NB-FileList-", null);   // NOI18N
        tmp.deleteOnExit();
        file = new RandomAccessFile(tmp, "rw");
    }

    public final Object get(int index) {
        if (index < 0 || index >= size()) throw new IndexOutOfBoundsException();

        Long filePos = (Long) map.get(index);
        long pos = filePos.longValue();

        return readFromFile(pos);
    }

    public final int size() {
        return map.size();
    }

    public final void add(int index, Object element) {
        if (index < 0 || index > size()) throw new IndexOutOfBoundsException("Index " + index + " of " + size());
        if (element != null && (element instanceof Serializable) == false) throw new ClassCastException("got " + element.getClass());

        long pos = writeToFile(element);
        map.add(index, new Long(pos));
    }

    public final Object set(int index, Object element) {
        if (index < 0 || index >= size()) throw new IndexOutOfBoundsException("Index " + index + " of " + size());
        if (element != null && (element instanceof Serializable) == false) throw new ClassCastException("got " + element.getClass());

        Object orig = get(index);
        long pos = writeToFile(element);
        map.set(index, new Long(pos));

        return orig;
    }

    public final Object remove(int index) {
        if (index < 0 || index >= size()) throw new IndexOutOfBoundsException("Index " + index + " of " + size());

        Object orig = get(index);
        map.remove(index);

        return orig;
    }

    /**
     * Convert object to bytes, by default using serialization.
     * @param object object to convert (may be null)
     * @return raw data bytes, never null
     */
    protected byte[] toBytes(Object object) throws IOException {

        if (object == null) object = new Null();

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(out);
        try {
            oos.writeObject(object);
            oos.flush();
        } finally {
            oos.close();
        }
        byte[] data = out.toByteArray();
        return data;
    }

    /**
     * Convert bytes back to object, by default using serialization.
     * @param bytes raw data created by {@link #toBytes}
     * @return object or null if data represents null
     */
    protected Object fromBytes(byte[] bytes) throws IOException, ClassNotFoundException {
        ByteArrayInputStream in = new ByteArrayInputStream(bytes);
        ObjectInputStream ois = new ObjectInputStream(in) {

            // class resolution must use system class loader to
            // be able to access classes from client modules

            protected Class resolveClass(ObjectStreamClass desc) throws IOException, ClassNotFoundException {
//                String name = desc.getName();
//                try {
//                    ClassLoader systemClassLoader = (ClassLoader) Lookup.getDefault().lookup(ClassLoader.class);
//                    return Class.forName(name, false, systemClassLoader);
//                } catch (ClassNotFoundException ex) {
                    return super.resolveClass(desc);
//                }
            }
        };
        Object obj = ois.readObject();
        ois.close();

        if (obj instanceof Null) return null;
        return obj;
    }

    /**
     * Writes object to file.
     * @return position that can recall the data by readFromFile
     */
    private long writeToFile(Object element) {

        try {
            byte[] data = toBytes(element);

            // write them down in len:int, data:byte[len] format

            long len = file.length();
            file.seek(len);
            file.writeInt(data.length);
            file.write(data);
            return len;
        } catch (IOException ex) {
            RuntimeException out = new RuntimeException(ex.getMessage());
            out.initCause(ex);
            throw out;
        }
    }

    /**
     * Reads object written by writeToFile.
     * @throws RuntimeException on I/O error or most commonly
     * unsynchronized access from multiple threads.
     */
    private Object readFromFile(long pos) {
        try {

            // read raw data from len:int, data:byte[len] format

            file.seek(pos);
            int size = file.readInt();
            byte[] data = new byte[size];
            file.readFully(data);

            // instantiate object

            return fromBytes(data);

        } catch (IOException ex) {
            RuntimeException out = new RuntimeException(ex.getMessage());
            out.initCause(ex);
            throw out;
        } catch (ClassNotFoundException ex) {
            RuntimeException out = new RuntimeException(ex.getMessage());
            out.initCause(ex);
            throw out;
        }
    }

    protected void finalize() throws Throwable {
        file.setLength(0);
        file.close();
    }

    /** Special value for null elements. */
    private static class Null implements Serializable {
    }
}

