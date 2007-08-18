/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists;

import ca.odell.glazedlists.impl.testing.ListConsistencyListener;
import junit.framework.TestCase;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

public class DebugListTest extends TestCase {

    private static final Pattern BAD_READER_THREAD_PATTERN = Pattern.compile("DebugList detected an unexpected Thread (.*) attempting to perform a read operation");
    private static final Pattern BAD_WRITER_THREAD_PATTERN = Pattern.compile("DebugList detected an unexpected Thread (.*) attempting to perform a write operation");

    private DebugList<String> list;

    private final List<Runnable> READ_OPERATIONS = Arrays.asList(new Runnable[] {
        new Get(0),
        new Size(),
        new Contains("one"),
        new ContainsAll(Arrays.asList(new String[] {"one", "five"})),
        new IndexOf("two"),
        new LastIndexOf("three"),
        new IsEmpty(),
        new ToArray(),
        new ToArray_Array(new Object[5]),
        new Equals(null),
        new HashCode(),
        new ToString()
    });

    private final List<Runnable> WRITE_OPERATIONS = Arrays.asList(new Runnable[] {
        new Add("six"),
        new Remove("six"),
        new AddAll(Arrays.asList(new String[] {"six", "seven", "eight"})),
        new RemoveAll(Arrays.asList(new String[] {"six", "seven", "eight"})),
        new AddAll_Index(0, Arrays.asList(new String[] {"six", "seven", "eight"})),
        new RetainAll(Arrays.asList(new String[] {"one", "two", "three"})),
        new Set(0, "four"),
        new Add_Index(1, "five"),
        new Remove_Index(0),
        new Clear(),
    });

    protected void setUp() {
        list = new DebugList<String>();
        list.addAll(Arrays.asList(new String[] {"one", "two", "three", "four", "five"}));
        ListConsistencyListener.install(list);
    }

    protected void tearDown() {
        list = null;
    }

    public void testDebugListPassThrough() {
        final EventList sorted = new SortedList(list);

        assertEquals(sorted.size(), list.size());
        assertTrue(sorted.containsAll(list));
        assertTrue(sorted.contains("one"));

        list.remove("one");
        assertFalse(sorted.contains("one"));
    }

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
        for (Iterator<Runnable> i = READ_OPERATIONS.iterator(); i.hasNext();)
            i.next().run();

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
        list.addAll(Arrays.asList(new String[] {"one", "two", "three", "four", "five"}));
        for (Iterator<Runnable> i = WRITE_OPERATIONS.iterator(); i.hasNext();)
            i.next().run();

        // Now, writes from an alterate Thread should fail, since we have specified which writer Threads are valid
        list.clear();
        list.addAll(Arrays.asList(new String[] {"one", "two", "three", "four", "five"}));
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

    public void testReadLockOperations() {
        // DebugList has not yet been told to perform lock checking, so all reads should succeed
        for (Iterator<Runnable> i = READ_OPERATIONS.iterator(); i.hasNext();)
            i.next().run();

        list.setLockCheckingEnabled(true);

        // all read operations should now fail without locks
        for (Iterator<Runnable> i = READ_OPERATIONS.iterator(); i.hasNext();)
            runReadLockFailure(i.next());

        // holding the readLock during reads is acceptable
        list.getReadWriteLock().readLock().lock();
        for (Iterator<Runnable> i = READ_OPERATIONS.iterator(); i.hasNext();)
            i.next().run();
        list.getReadWriteLock().readLock().unlock();

        // holding the writeLock during reads is acceptable
        list.getReadWriteLock().writeLock().lock();
        for (Iterator<Runnable> i = READ_OPERATIONS.iterator(); i.hasNext();)
            i.next().run();
        list.getReadWriteLock().writeLock().unlock();
    }

    public void testWriteLockOperations() {
        // DebugList has not yet been told to perform lock checking, so all writes should succeed
        for (Iterator<Runnable> i = WRITE_OPERATIONS.iterator(); i.hasNext();)
            i.next().run();

        list.clear();
        list.addAll(Arrays.asList(new String[] {"one", "two", "three", "four", "five"}));
        list.setLockCheckingEnabled(true);

        // all write operations should now fail without locks
        for (Iterator<Runnable> i = WRITE_OPERATIONS.iterator(); i.hasNext();)
            runWriteLockFailure(i.next());

        // holding the readLock during writes is unacceptable
        list.getReadWriteLock().readLock().lock();
        for (Iterator<Runnable> i = WRITE_OPERATIONS.iterator(); i.hasNext();)
            runWriteLockFailure(i.next());
        list.getReadWriteLock().readLock().unlock();

        // holding the writeLock during writes is acceptable
        list.getReadWriteLock().writeLock().lock();
        for (Iterator<Runnable> i = WRITE_OPERATIONS.iterator(); i.hasNext();)
            i.next().run();
        list.getReadWriteLock().writeLock().unlock();
    }

    public void testCreateNewDebugList() {
        DebugList<String> list1 = new DebugList<String>();
        DebugList<Integer> list2 = list1.createNewDebugList();

        assertSame(list1.getPublisher(), list2.getPublisher());
        assertSame(list1.getReadWriteLock(), list2.getReadWriteLock());
        assertSame(list1.getReadWriteLock().readLock(), list2.getReadWriteLock().readLock());
        assertSame(list1.getReadWriteLock().writeLock(), list2.getReadWriteLock().writeLock());

        list1.add("James");

        assertEquals(1, list1.size());
        assertEquals(0, list2.size());
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

    //
    // Read Operations
    //

    private class Get implements Runnable {
        private final int index;

        public Get(int index) { this.index = index; }
        public void run() { list.get(index); }
    }

    private class Size implements Runnable {
        public void run() { list.size(); }
    }

    private class Contains implements Runnable {
        private final Object o;

        public Contains(Object o) { this.o = o; }
        public void run() { list.contains(o); }
    }

    private class ContainsAll implements Runnable {
        private final Collection<?> collection;

        public ContainsAll(Collection<?> collection) { this.collection = collection; }
        public void run() { list.containsAll(collection); }
    }

    private class IndexOf implements Runnable {
        private final Object o;

        public IndexOf(Object o) { this.o = o; }
        public void run() { list.indexOf(o); }
    }

    private class LastIndexOf implements Runnable {
        private final Object o;

        public LastIndexOf(Object o) { this.o = o; }
        public void run() { list.lastIndexOf(o); }
    }

    private class IsEmpty implements Runnable {
        public void run() { list.isEmpty(); }
    }

    private class ToArray implements Runnable {
        public void run() { list.toArray(); }
    }

    private class ToArray_Array implements Runnable {
        private final Object[] array;

        public ToArray_Array(Object[] array) { this.array = array; }
        public void run() { list.toArray(array); }
    }

    private class Equals implements Runnable {
        private final Object o;

        public Equals(Object o) { this.o = o; }
        public void run() { list.equals(o); }
    }

    private class HashCode implements Runnable {
        public void run() { list.hashCode(); }
    }

    private class ToString implements Runnable {
        public void run() { list.toString(); }
    }

    //
    // Write Operations
    //

    private class Add implements Runnable {
        private final String o;

        public Add(String o) { this.o = o; }
        public void run() { list.add(o); }
    }

    private class Remove implements Runnable {
        private final String o;

        public Remove(String o) { this.o = o; }
        public void run() { list.remove(o); }
    }

    private class AddAll implements Runnable {
        private final Collection<String> collection;

        public AddAll(Collection<String> collection) {
            this.collection = collection;
        }
        public void run() { list.addAll(collection); }
    }

    private class AddAll_Index implements Runnable {
        private final int index;
        private final Collection<String> collection;

        public AddAll_Index(int index, Collection<String> collection) {
            this.index = index;
            this.collection = collection;
        }
        public void run() { list.addAll(index, collection); }
    }

    private class RemoveAll implements Runnable {
        private final Collection<?> collection;

        public RemoveAll(Collection<?> collection) { this.collection = collection; }
        public void run() { list.removeAll(collection); }
    }

    private class RetainAll implements Runnable {
        private final Collection<?> collection;

        public RetainAll(Collection<?> collection) { this.collection = collection; }
        public void run() { list.retainAll(collection); }
    }

    private class Clear implements Runnable {
        public void run() { list.clear(); }
    }

    private class Set implements Runnable {
        private final int index;
        private final String o;

        public Set(int index, String o) {
            this.index = index;
            this.o = o;
        }
        public void run() { list.set(index, o); }
    }

    private class Add_Index implements Runnable {
        private final int index;
        private final String o;

        public Add_Index(int index, String o) {
            this.index = index;
            this.o = o;
        }
        public void run() { list.add(index, o); }
    }

    private class Remove_Index implements Runnable {
        private final int index;

        public Remove_Index(int index) {
            this.index = index;
        }
        public void run() { list.remove(index); }
    }

    private static class RecorderRunnable implements Runnable {
        private final Runnable delegate;
        private RuntimeException runtimeException;

        RecorderRunnable(Runnable runnable) { this.delegate = runnable; }

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