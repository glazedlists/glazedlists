/**
 * Glazed Lists
 * http://glazedlists.dev.java.net/
 *
 * COPYRIGHT 2003 O'DELL ENGINEERING LTD.
 */
package ca.odell.glazedlists.util.concurrent;

// the Glazed Lists for testing the internal lock
import ca.odell.glazedlists.*;
import ca.odell.glazedlists.test.*;
// standard collections
import java.util.*;

/**
 * An internal read-write lock manages a pair of read-write locks. This allows
 * modification at two levels: at the source level and at the local/internal
 * level. By providing a new internal level, internal changes can be made without
 * requiring the full locking of the source level. This is done by locking the
 * source level with only a read-lock, while locking the local/internal level with
 * a write lock.
 *
 * <p>This is useful for classes such as <code>SortedList</code>, where changes
 * to the internal structure occur without modifying the source structure.
 *
 * @author <a href="mailto:jesse@odel.on.ca">Jesse Wilson</a>
 */
public final class InternalReadWriteLock implements ReadWriteLock {
    
    /** the locks to delegate to */
    private Lock readLock;
    private Lock writeLock;
    private Lock internalLock;
    
    /**
     * Creates a new InternalReadWriteLock that uses the specified locks for
     * the source and internal.
     */
    public InternalReadWriteLock(ReadWriteLock source, ReadWriteLock internal) {
        readLock = new LockPair(source.readLock(), internal.readLock());
        writeLock = new LockPair(source.writeLock(), internal.writeLock());
        internalLock = new LockPair(source.readLock(), internal.writeLock());
    }
    
    /**
     * Return the lock used for reading.
     */
    public Lock readLock() {
        return readLock;
    }

    /**
     * Return the lock used for writing.
     */
    public Lock writeLock() {
        return writeLock;
    }

    /**
     * Return the lock used for reading.
     */
    public Lock internalLock() {
        return internalLock;
    }

    /**
     * Runs a test to see if concurrent access is allowed by performing multiple
     * sorts using different threads and a common source list.
     */
    public static void main(String[] args) {
        Sorter.start();
    }
}


/**
 * A LockPair is a set of two locks that are locked and unlocked in
 * seqeunce. To prevent the locks from causing a deadlock, they are locked
 * in the order: ( first, second ), and unlocked in the order ( second, first ).
 */
class LockPair implements Lock {
    
    /** the locks to delegate to */
    private Lock first;
    private Lock second;
    
    /**
     * Creates a lock pair that uses the specified locks in sequence.
     */
    public LockPair(Lock first, Lock second) {
        this.first = first;
        this.second = second;
    }

    /**
     * Acquires the lock.
     */
    public void lock() {
        first.lock();
        second.lock();
    }
    
    /**
     * Acquires the lock only if it is free at the time of invocation.
     */
    public boolean tryLock() {
        boolean firstSuccess = first.tryLock();
        if(!firstSuccess) return false;
            
        boolean secondSuccess = second.tryLock();
        if(!secondSuccess) {
            first.unlock();
            return false;
        }
        
        return true;
    }
    
    /**
     * Releases the lock.
     */
    public void unlock() {
        second.unlock();
        first.unlock();
    }
}

/**
 * A test class, the sorter continuously re-sorts its own view of the list.
 */
class Sorter implements Runnable {
    
    /** the ID of this sorter */
    private int id;
    
    /** the source list */
    private EventList listToSort;
    
    /** for generating random values to sort */
    private static Random random = new Random();
    
    /**
     * Creates a new sorter with the specified ID.
     */
    public Sorter(EventList listToSort, int id) {
        this.listToSort = listToSort;
        this.id = id;
    }

    /**
     * When run, the sorter sorts a random sublist of the source list.
     */
    public void run() {
        for(int i = 0; i < 10000; i++) {
            EventList subList = (EventList)listToSort.subList(0, random.nextInt(listToSort.size()));
            System.out.println("> Sorting a list of size: " + subList.size());
            SortedList sorted = new SortedList(subList, new IntArrayComparator(random.nextInt(2)));
            System.out.println("< Sorted  a list of size: " + subList.size());
        }
    }

    /**
     * Starts multiple threads continuously sorting their own views of the
     * same list.
     */
    public static void start() {

        // populate a list with 10,000 2-integer arrays
        EventList concurrentAccessList = new BasicEventList();
        for(int i = 0; i < 10000; i++) {
            concurrentAccessList.add(new int[] { random.nextInt(10000), random.nextInt(10000) });
        }
        
        // start two threads continuously sorting that array
        for(int i = 0; i < 8; i++) {
            new Thread(new Sorter(concurrentAccessList, i)).start();
        }
    }
}

