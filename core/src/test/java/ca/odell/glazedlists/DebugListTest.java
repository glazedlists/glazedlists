/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists;

import ca.odell.glazedlists.impl.testing.ListConsistencyListener;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class DebugListTest {

    private static final Pattern BAD_READER_THREAD_PATTERN = Pattern.compile("DebugList detected an unexpected Thread (.*) attempting to perform a read operation");
    private static final Pattern BAD_WRITER_THREAD_PATTERN = Pattern.compile("DebugList detected an unexpected Thread (.*) attempting to perform a write operation");

    private DebugList<String> list;

    private final List<Runnable> READ_OPERATIONS = Arrays.asList(
            () -> list.get(0),
            () -> list.size(),
            () -> list.contains("one"),
            () -> list.containsAll(Arrays.asList("one", "five")),
            () -> list.indexOf("two"),
            () -> list.lastIndexOf("three"),
            () -> list.isEmpty(),
            () -> list.toArray(),
            () -> list.toArray(new Object[5]),
            () -> list.equals(null),
            () -> list.hashCode(),
            () -> list.toString(),
            () -> list.forEach(l -> {
            })
    );

    private final List<Runnable> WRITE_OPERATIONS = Arrays.asList(
            () -> list.add("six"),
            () -> list.remove("six"),
            () -> list.addAll(Arrays.asList("six", "seven", "eight")),
            () -> list.removeAll(Arrays.asList("six", "seven", "eight")),
            () -> list.addAll(0, Arrays.asList("six", "seven", "eight")),
            () -> list.retainAll(Arrays.asList("one", "two", "three")),
            () -> list.set(0, "four"),
            () -> list.add(1, "five"),
            () -> list.remove(0),
            () -> list.removeIf(elem -> elem.length() > 3),
            () -> list.replaceAll(String::toUpperCase),
            () -> list.sort(Comparator.naturalOrder()),
            () -> list.clear()
    );

    @Before
    public void setUp() {
        list = new DebugList<>();
        list.addAll(Arrays.asList("one", "two", "three", "four", "five"));
        ListConsistencyListener.install(list);
    }

    @After
    public void tearDown() {
        list = null;
    }

    @Test
    public void testDebugListPassThrough() {
        final EventList<String> sorted = SortedList.create(list);

        assertEquals(sorted.size(), list.size());
        assertTrue(sorted.containsAll(list));
        assertTrue(sorted.contains("one"));

        list.remove("one");
        assertFalse(sorted.contains("one"));
    }

    @Test
    public void testSanctionedReaderThread() throws InterruptedException {
        // No sanctioned reader Threads implies ALL Threads can read
        for (Iterator<Runnable> i = READ_OPERATIONS.iterator(); i.hasNext();) {
            RecorderRunnable recorderRunnable = new RecorderRunnable(i.next());
            Thread t = new Thread(recorderRunnable);
            t.start();
            t.join();

            assertNull(recorderRunnable.getRuntimeException());
        }

        // register THIS Thread as a sanctioned reader Thread
        list.getSanctionedReaderThreads().add(Thread.currentThread());

        // All reads from THIS Thread should succeed
        for (Iterator<Runnable> i = READ_OPERATIONS.iterator(); i.hasNext();) {
            i.next().run();
        }

        // Now, reads from an alterate Thread should fail, since we have specified which reader Threads are valid
        for (Iterator<Runnable> i = READ_OPERATIONS.iterator(); i.hasNext();) {
            RecorderRunnable recorderRunnable = new RecorderRunnable(i.next());
            Thread t = new Thread(recorderRunnable);
            t.start();
            t.join();

            RuntimeException runtimeException = recorderRunnable.getRuntimeException();
            assertNotNull(runtimeException);
            assertTrue(BAD_READER_THREAD_PATTERN.matcher(runtimeException.getMessage()).matches());
        }
    }

    @Test
    public void testSanctionedWriterThread() throws InterruptedException {
        // No sanctioned writer Threads implies ALL Threads can write
        for (Iterator<Runnable> i = WRITE_OPERATIONS.iterator(); i.hasNext();) {
            RecorderRunnable recorderRunnable = new RecorderRunnable(i.next());
            Thread t = new Thread(recorderRunnable);
            t.start();
            t.join();

            assertNull(recorderRunnable.getRuntimeException());
        }

        // register THIS Thread as a sanctioned writer Thread
        list.getSanctionedWriterThreads().add(Thread.currentThread());

        // All writes from THIS Thread should succeed
        list.clear();
        list.addAll(Arrays.asList("one", "two", "three", "four", "five"));
        for (Iterator<Runnable> i = WRITE_OPERATIONS.iterator(); i.hasNext();) {
            i.next().run();
        }

        // Now, writes from an alterate Thread should fail, since we have specified which writer Threads are valid
        list.clear();
        list.addAll(Arrays.asList("one", "two", "three", "four", "five"));
        for (Iterator<Runnable> i = WRITE_OPERATIONS.iterator(); i.hasNext();) {
            RecorderRunnable recorderRunnable = new RecorderRunnable(i.next());
            Thread t = new Thread(recorderRunnable);
            t.start();
            t.join();

            RuntimeException runtimeException = recorderRunnable.getRuntimeException();
            assertNotNull(runtimeException);
            assertTrue(BAD_WRITER_THREAD_PATTERN.matcher(runtimeException.getMessage()).matches());
        }
    }

    @Test
    public void testReadLockOperations() {
        // DebugList has not yet been told to perform lock checking, so all reads should succeed
        for (Iterator<Runnable> i = READ_OPERATIONS.iterator(); i.hasNext();) {
            i.next().run();
        }

        list.setLockCheckingEnabled(true);

        // all read operations should now fail without locks
        for (Iterator<Runnable> i = READ_OPERATIONS.iterator(); i.hasNext();) {
            runReadLockFailure(i.next());
        }

        // holding the readLock during reads is acceptable
        list.getReadWriteLock().readLock().lock();
        for (Iterator<Runnable> i = READ_OPERATIONS.iterator(); i.hasNext();) {
            i.next().run();
        }
        list.getReadWriteLock().readLock().unlock();

        // holding the writeLock during reads is acceptable
        list.getReadWriteLock().writeLock().lock();
        for (Iterator<Runnable> i = READ_OPERATIONS.iterator(); i.hasNext();) {
            i.next().run();
        }
        list.getReadWriteLock().writeLock().unlock();
    }

    @Test
    public void testWriteLockOperations() {
        // DebugList has not yet been told to perform lock checking, so all writes should succeed
        for (Iterator<Runnable> i = WRITE_OPERATIONS.iterator(); i.hasNext();) {
            i.next().run();
        }

        list.clear();
        list.addAll(Arrays.asList("one", "two", "three", "four", "five"));
        list.setLockCheckingEnabled(true);

        // all write operations should now fail without locks
        for (Iterator<Runnable> i = WRITE_OPERATIONS.iterator(); i.hasNext();) {
            runWriteLockFailure(i.next());
        }

        // holding the readLock during writes is unacceptable
        list.getReadWriteLock().readLock().lock();
        for (Iterator<Runnable> i = WRITE_OPERATIONS.iterator(); i.hasNext();) {
            runWriteLockFailure(i.next());
        }
        list.getReadWriteLock().readLock().unlock();

        // holding the writeLock during writes is acceptable
        list.getReadWriteLock().writeLock().lock();
        for (Iterator<Runnable> i = WRITE_OPERATIONS.iterator(); i.hasNext();) {
            i.next().run();
        }
        list.getReadWriteLock().writeLock().unlock();
    }

    @Test
    public void testCreateNewDebugList() {
        DebugList<String> list1 = new DebugList<>();
        DebugList<Integer> list2 = list1.createNewDebugList();

        assertSame(list1.getPublisher(), list2.getPublisher());
        assertSame(list1.getReadWriteLock(), list2.getReadWriteLock());
        assertSame(list1.getReadWriteLock().readLock(), list2.getReadWriteLock().readLock());
        assertSame(list1.getReadWriteLock().writeLock(), list2.getReadWriteLock().writeLock());

        list1.add("James");

        assertEquals(1, list1.size());
        assertEquals(0, list2.size());
    }


	@Test
	public void testLockUpgradeAttempt() {
        DebugList<String> list = new DebugList<>();

		list.getReadWriteLock().readLock().lock();

		try {
			// Try to upgrade
			list.getReadWriteLock().writeLock().lock();
		}
		catch( IllegalStateException ex ) {
			assertEquals( "DebugList detected an attempt to acquire a writeLock from " +
				"a thread already owning a readLock (deadlock)", ex.getMessage() );
		}
	}


    private void runReadLockFailure(Runnable r) {
        try {
            r.run();
            fail("failed to receive an IllegalStateException as expected");
        } catch (IllegalStateException e) {
            assertEquals("DebugList detected a failure to acquire the readLock prior to a read operation", e.getMessage());
        }
    }

    private void runWriteLockFailure(Runnable r) {
        try {
            r.run();
            fail("failed to receive an IllegalStateException as expected");
        } catch (IllegalStateException e) {
            assertEquals("DebugList detected a failure to acquire the writeLock prior to a write operation", e.getMessage());
        }
    }

    private static class RecorderRunnable implements Runnable {
        private final Runnable delegate;
        private RuntimeException runtimeException;

        RecorderRunnable(Runnable runnable) { this.delegate = runnable; }

        @Override
        public void run() {
            try {
                delegate.run();
            } catch (RuntimeException re) {
                this.runtimeException = re;
            }
        }

        public RuntimeException getRuntimeException() { return runtimeException; }
    }
}
