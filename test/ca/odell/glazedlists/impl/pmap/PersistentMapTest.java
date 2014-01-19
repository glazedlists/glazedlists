/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.impl.pmap;

// for being a JUnit test case
import ca.odell.glazedlists.impl.io.Bufferlo;
import ca.odell.glazedlists.io.GlazedListsIO;

import java.awt.Color;
import java.io.File;
import java.io.IOException;

import org.junit.Test;

import static org.junit.Assert.*;


/**
 * This test verifies that the PersistentMap works.
 *
 * @author <a href="mailto:jesse@swank.ca">Jesse Wilson</a>
 */
public class PersistentMapTest {

    /**
     * Creates a map file, reads a key and writes a key. The written value is double
     * the value read. If no value is found (which is the case when the file is new),
     * then the written value is one. The key's value will therefore follow the
     * sequence null, 1, 2, 4, 8.
     */
    @Test
    public void testCreate() throws IOException {
        File mapFile = File.createTempFile("counting", "j81");
        mapFile.deleteOnExit();

        Integer value = null;
        Integer expectedValue = null;
        String key = "lucky#";

        for(int i = 0; i < 5; i++) {
            PersistentMap map = new PersistentMap(mapFile);

            // read the current value
            Chunk chunk = (Chunk)map.get(key);
            if(chunk != null) {
                Bufferlo valueBufferlo = chunk.getValue();
                value = Integer.valueOf(valueBufferlo.toString());
            } else {
                value = null;
            }
            assertEquals(expectedValue, value);

            // write the next value
            expectedValue = (expectedValue == null) ? new Integer(1) : new Integer(expectedValue.intValue() * 2);
            Bufferlo expectedValueBufferlo = new Bufferlo();
            expectedValueBufferlo.write(expectedValue.toString());
            map.put(key, new Chunk(expectedValueBufferlo));

            map.close();
        }
    }

    /**
     * Get a Chunk from a value.
     */
    public static Chunk chunkify(Object value) throws IOException {
        Bufferlo data = new Bufferlo();
        GlazedListsIO.serializableByteCoder().encode(value, data.getOutputStream());
        System.err.println("CHUNKIFIED " + data.length() + " BYTES");
        return new Chunk(data);
    }

    /**
     * Get a value from a Chunk.
     */
    public static Object deChunkify(Chunk chunk) throws IOException {
        Bufferlo data = chunk.getValue();
        return GlazedListsIO.serializableByteCoder().decode(data.getInputStream());
    }

    /**
     * Creates a map and adds ten entries. Then removes four. Then verifies that
     * the contents are consistent both in memory and on disk.
     */
    @Test
    public void testRemove() throws IOException {
        File colorsFile = File.createTempFile("colors", "j81");
        colorsFile.deleteOnExit();
        PersistentMap colors = new PersistentMap(colorsFile);

        // create up some colors
        colors.put("red",    chunkify(Color.red));
        colors.put("orange", chunkify(Color.orange));
        colors.put("sunny",  chunkify(Color.yellow));
        colors.put("vert",   chunkify(Color.green));
        colors.put("blue",   chunkify(Color.blue));
        colors.put("grape",  chunkify(Color.magenta.darker()));
        colors.put("white",  chunkify(Color.white));
        colors.put("medium", chunkify(Color.gray));
        colors.put("dark",   chunkify(Color.black));

        // kill some colors
        colors.remove("red");
        colors.remove("white");
        colors.remove("blue");

        // persist and restore
        colors.close();
        colors = new PersistentMap(colorsFile);

        // meet expectations
        assertNull(colors.get("red"));
        assertEquals(deChunkify((Chunk)colors.get("orange")), Color.orange);
        assertEquals(deChunkify((Chunk)colors.get("sunny")),  Color.yellow);
        assertEquals(deChunkify((Chunk)colors.get("vert")),   Color.green);
        assertNull(colors.get("blue"));
        assertEquals(deChunkify((Chunk)colors.get("grape")),  Color.magenta.darker());
        assertNull(colors.get("white"));
        assertEquals(deChunkify((Chunk)colors.get("medium")), Color.gray);
        assertEquals(deChunkify((Chunk)colors.get("dark")),   Color.black);
        assertNull(colors.get("turquoise"));
        assertNull(colors.get("creme"));
        assertNull(colors.get("lavender"));

        // delete stuff created earlier
        colors.remove("sunny");
        colors.remove("dark");

        // meet expectations
        assertNull(colors.get("red"));
        assertEquals(deChunkify((Chunk)colors.get("orange")), Color.orange);
        assertNull(colors.get("sunny"));
        assertEquals(deChunkify((Chunk)colors.get("vert")),   Color.green);
        assertNull(colors.get("blue"));
        assertEquals(deChunkify((Chunk)colors.get("grape")),  Color.magenta.darker());
        assertNull(colors.get("white"));
        assertEquals(deChunkify((Chunk)colors.get("medium")), Color.gray);
        assertNull(colors.get("dark"));
        assertNull(colors.get("turquoise"));
        assertNull(colors.get("creme"));
        assertNull(colors.get("lavender"));

        // persist and restore
        colors.close();
        colors = new PersistentMap(colorsFile);

        // meet expectations
        assertNull(colors.get("red"));
        assertEquals(deChunkify((Chunk)colors.get("orange")), Color.orange);
        assertNull(colors.get("sunny"));
        assertEquals(deChunkify((Chunk)colors.get("vert")),   Color.green);
        assertNull(colors.get("blue"));
        assertEquals(deChunkify((Chunk)colors.get("grape")),  Color.magenta.darker());
        assertNull(colors.get("white"));
        assertEquals(deChunkify((Chunk)colors.get("medium")), Color.gray);
        assertNull(colors.get("dark"));
        assertNull(colors.get("turquoise"));
        assertNull(colors.get("creme"));
        assertNull(colors.get("lavender"));
    }

    /**
     * Tests that the flush() method of PersistentMap works.
     *
     * <p>This test initially queues up a lot of work to keep the PersistentMap
     * very busy. All of the calls are carefully chosen to be long-running non-blockin
     * operations. After all the busy work completes, a token value is written.
     * Finally the PersistenMap is flushed. This should block until the token value
     * has been written out.
     *
     * <p>Another PersistentMap then loads the same file and checks if the token
     * value exists. If it does exist, then the flush worked. Otherwise that value
     * is still to be written.
     */
    @Test
    public void testFlush() throws IOException {
        File sharedFile = File.createTempFile("shared", "j81");
        sharedFile.deleteOnExit();
        PersistentMap writer = new PersistentMap(sharedFile);

        // create a big long queue to keep the writer thread busy
        NullValueCallback callback = new NullValueCallback();
        Bufferlo data = new Bufferlo();
        GlazedListsIO.serializableByteCoder().encode(new long[4000], data.getOutputStream()); // 4000 x 8 bytes ~ 32000 bytes
        for(int i = 0; i < 50; i++) {
            Chunk chunk = new Chunk(data);
            writer.put("big data", chunk);
            chunk.fetchValue(callback);;
        }

        // write a token
        String tokenKey = "Saskatchewan";
        String tokenValue = "Roughriders";
        writer.put(tokenKey, chunkify(tokenValue));
        writer.flush();

        // whatever you do, don't do this:
        PersistentMap reader = new PersistentMap(sharedFile);
        Chunk chunk = (Chunk)reader.get(tokenKey);
        Object tokenValuePersisted = (chunk == null) ? null : deChunkify(chunk);
        assertEquals(tokenValue, tokenValuePersisted);
        reader.close();

        // and clean up
        writer.close();
    }

    /**
     * Ignores a value callback.
     */
    static class NullValueCallback implements ValueCallback {
        @Override
        public void valueLoaded(Chunk member, Bufferlo value) {
            // do nothing
        }
    }
}
