/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists;

import ca.odell.glazedlists.event.ListEventAssembler;
import ca.odell.glazedlists.impl.testing.ListConsistencyListener;
import ca.odell.glazedlists.util.concurrent.LockFactory;

import java.util.Arrays;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class PluggableListTest {

    private EventList<String> source;
    private PluggableList<String> pl;

    @Before
    public void setUp() throws Exception {
        source = new BasicEventList<String>();
        pl = new PluggableList<String>(source);
        ListConsistencyListener.install(pl);
    }

    @After
    public void tearDown() throws Exception {
        pl.dispose();
        source = null;
        pl = null;
    }

    @Test
    public void testNormalOperation() {
        source.add("first");
        assertSame("first", pl.get(0));

        source.set(0, "second");
        assertSame("second", pl.get(0));

        source.remove(0);
        assertTrue(pl.isEmpty());
    }

    @Test
    public void testBadSourceSwap() {
        try {
            pl.setSource(null);
            fail("failed to throw exception on null source");
        } catch (IllegalArgumentException e) {
            // expected
        }

        try {
            final EventList<String> badLockList = new BasicEventList<String>(pl.getPublisher(), LockFactory.DEFAULT.createReadWriteLock());
            pl.setSource(badLockList);
            fail("failed to throw exception on source with mismatching lock");
        } catch (IllegalArgumentException e) {
            // expected
        }

        try {
            final EventList<String> badPublisherList = new BasicEventList<String>(ListEventAssembler.createListEventPublisher(), pl.getReadWriteLock());
            pl.setSource(badPublisherList);
            fail("failed to throw exception on source with mismatching publisher");
        } catch (IllegalArgumentException e) {
            // expected
        }

        final EventList<String> goodSourceList = new BasicEventList<String>(pl.getPublisher(), pl.getReadWriteLock());
        pl.setSource(goodSourceList);

        try {
            pl.dispose();
            pl.setSource(source);
            fail("failed to throw exception on disposed PluggableList");
        } catch (IllegalStateException e) {
            // expected
        }
    }

    @Test
    public void testListEventFireBySourceSwap() {
        // add some data to the existing source list
        source.addAll(Arrays.asList("james", "kevin", "sam"));
        assertEquals(Arrays.asList("james", "kevin", "sam"), pl);

        // create and build a new source list
        final EventList<String> source2 = pl.createSourceList();
        source2.addAll(Arrays.asList("kermit", "animal", "piggy"));

        // set the source list and verify the new data is present
        pl.setSource(source2);
        assertEquals(Arrays.asList("kermit", "animal", "piggy"), pl);
    }
}
