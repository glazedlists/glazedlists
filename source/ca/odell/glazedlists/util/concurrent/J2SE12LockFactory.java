/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.util.concurrent;

import java.util.HashMap;
import java.util.Map;


/*
  File: ReentrantWriterPreferenceReadWriteLock.java

  Originally written by Doug Lea and released into the public domain.
  This may be used for any purposes whatsoever without acknowledgment.
  Thanks for the assistance and support of Sun Microsystems Labs,
  and everyone contributing, testing, and using this code.

  History:
  Date       Who                What
  26aug1998  dl                 Create public version
   7sep2000  dl                 Readers are now also reentrant
  19jan2001  dl                 Allow read->write upgrades if the only reader
  10dec2002  dl                 Throw IllegalStateException on extra release
*/

/**
 * A writer-preference ReadWriteLock that allows both readers and
 * writers to reacquire
 * read or write locks in the style of a ReentrantLock.
 * Readers are not allowed until all write locks held by
 * the writing thread have been released.
 * Among other applications, reentrancy can be useful when
 * write locks are held during calls or callbacks to methods that perform
 * reads under read locks.
 * <p>
 * <b>Sample usage</b>. Here is a code sketch showing how to exploit
 * reentrancy to perform lock downgrading after updating a cache:
 * <pre>
 * class CachedData {
 *   Object data;
 *   volatile boolean cacheValid;
 *   ReentrantWriterPreferenceReadWriteLock rwl = ...
 *
 *   void processCachedData() {
 *     rwl.readLock().acquire();
 *     if (!cacheValid) {
 *
 *        // upgrade lock:
 *        rwl.readLock().release();   // must release first to obtain writelock
 *        rwl.writeLock().acquire();
 *        if (!cacheValid) { // recheck
 *          data = ...
 *          cacheValid = true;
 *        }
 *        // downgrade lock
 *        rwl.readLock().acquire();  // reacquire read without giving up lock
 *        rwl.writeLock().release(); // release write, still hold read
 *     }
 *
 *     use(data);
 *     rwl.readLock().release();
 *   }
 * }
 * </pre>
 *
 *
 * <p>[<a href="http://gee.cs.oswego.edu/dl/classes/EDU/oswego/cs/dl/util/concurrent/intro.html"> Introduction to this package. </a>]
 */
class ReentrantWriterPreferenceReadWriteLock extends WriterPreferenceReadWriteLock {

    /** Number of acquires on write lock by activeWriter_ thread */
    protected long writeHolds_ = 0;

    /** Number of acquires on read lock by any reader thread */
    protected Map<Thread, Integer> readers_ = new HashMap<Thread, Integer>();

    /** cache/reuse the special Integer value one to speed up readlocks */
    protected static final Integer IONE = new Integer(1);

    protected boolean allowReader() {
        return (activeWriter_ == null && waitingWriters_ == 0) ||
        activeWriter_ == Thread.currentThread();
    }

    protected synchronized boolean startRead() {
        Thread t = Thread.currentThread();
        Integer c = readers_.get(t);
        if (c != null) { // already held -- just increment hold count
            readers_.put(t, new Integer(((Integer)(c)).intValue()+1));
            ++activeReaders_;
            return true;
        }
        else if (allowReader()) {
            readers_.put(t, IONE);
            ++activeReaders_;
            return true;
        }
        else
            return false;
    }

    protected synchronized boolean startWrite() {
        if (activeWriter_ == Thread.currentThread()) { // already held; re-acquire
            ++writeHolds_;
            return true;
        }
        else if (writeHolds_ == 0) {
            if (activeReaders_ == 0 ||
                (readers_.size() == 1 &&
                 readers_.get(Thread.currentThread()) != null)) {
                activeWriter_ = Thread.currentThread();
                writeHolds_ = 1;
                return true;
            }
            else
                return false;
        }
        else
            return false;
    }

    protected synchronized Signaller endRead() {
        Thread t = Thread.currentThread();
        Integer c = readers_.get(t);
        if (c == null)
            throw new IllegalMonitorStateException("Attempted to unlock a readlock which was not locked. Please ensure the readlock is always locked and unlocked symmetrically.");
            --activeReaders_;
        if (c != IONE) { // more than one hold; decrement count
            int h = ((Integer)(c)).intValue()-1;
            Integer ih = (h == 1) ? IONE : new Integer(h);
            readers_.put(t, ih);
            return null;
        }
        else {
            readers_.remove(t);

            if (writeHolds_ > 0) // a write lock is still held by current thread
                return null;
            else if (activeReaders_ == 0 && waitingWriters_ > 0)
                return writerLock_;
            else
                return null;
        }
    }

    protected synchronized Signaller endWrite() {
        if (activeWriter_ == null)
            throw new IllegalMonitorStateException("Attempted to unlock a writelock which was not locked. Please ensure the writelock is always locked and unlocked symmetrically.");

        --writeHolds_;
        if (writeHolds_ > 0)   // still being held
            return null;
        else {
            activeWriter_ = null;
            if (waitingReaders_ > 0 && allowReader())
                return readerLock_;
            else if (waitingWriters_ > 0)
                return writerLock_;
            else
                return null;
        }
    }
}


/*
 File: WriterPreferenceReadWriteLock.java

 Originally written by Doug Lea and released into the public domain.
 This may be used for any purposes whatsoever without acknowledgment.
 Thanks for the assistance and support of Sun Microsystems Labs,
 and everyone contributing, testing, and using this code.

 History:
 Date       Who              What
 11Jun1998  dl               Create public version
 5Aug1998   dl               replaced int counters with longs
 25aug1998  dl               record writer thread
 3May1999   dl               add notifications on interrupt/timeout
 */

/**
 * A ReadWriteLock that prefers waiting writers over
 * waiting readers when there is contention. This class
 * is adapted from the versions described in CPJ, improving
 * on the ones there a bit by segregating reader and writer
 * wait queues, which is typically more efficient.
 * <p>
 * The locks are <em>NOT</em> reentrant. In particular,
 * even though it may appear to usually work OK,
 * a thread holding a read lock should not attempt to
 * re-acquire it. Doing so risks lockouts when there are
 * also waiting writers.
 * <p>[<a href="http://gee.cs.oswego.edu/dl/classes/EDU/oswego/cs/dl/util/concurrent/intro.html"> Introduction to this package. </a>]
 */
class WriterPreferenceReadWriteLock implements ReadWriteLock {

    protected long activeReaders_ = 0;
    protected Thread activeWriter_ = null;
    protected long waitingReaders_ = 0;
    protected long waitingWriters_ = 0;

    protected final ReaderLock readerLock_ = new ReaderLock();
    protected final WriterLock writerLock_ = new WriterLock();

    public Lock writeLock() { return writerLock_; }
    public Lock readLock() { return readerLock_; }

    /*
     A bunch of small synchronized methods are needed
     to allow communication from the Lock objects
     back to this object, that serves as controller
     */
    protected synchronized void cancelledWaitingReader() { --waitingReaders_; }
    protected synchronized void cancelledWaitingWriter() { --waitingWriters_; }


    /** Override this method to change to reader preference */
    protected boolean allowReader() {
        return activeWriter_ == null && waitingWriters_ == 0;
    }

    protected synchronized boolean startRead() {
        boolean allowRead = allowReader();
        if (allowRead)  ++activeReaders_;
        return allowRead;
    }

    protected synchronized boolean startWrite() {

        // The allowWrite expression cannot be modified without
        // also changing startWrite, so is hard-wired

        boolean allowWrite = (activeWriter_ == null && activeReaders_ == 0);
        if (allowWrite)  activeWriter_ = Thread.currentThread();
        return allowWrite;
    }


    /*
     Each of these variants is needed to maintain atomicity
     of wait counts during wait loops. They could be
     made faster by manually inlining each other. We hope that
     compilers do this for us though.
     */

    protected synchronized boolean startReadFromNewReader() {
        boolean pass = startRead();
        if (!pass) ++waitingReaders_;
        return pass;
    }

    protected synchronized boolean startWriteFromNewWriter() {
        boolean pass = startWrite();
        if (!pass) ++waitingWriters_;
        return pass;
    }

    protected synchronized boolean startReadFromWaitingReader() {
        boolean pass = startRead();
        if (pass) --waitingReaders_;
        return pass;
    }

    protected synchronized boolean startWriteFromWaitingWriter() {
        boolean pass = startWrite();
        if (pass) --waitingWriters_;
        return pass;
    }

    /**
     * Called upon termination of a read.
     * Returns the object to signal to wake up a waiter, or null if no such
     */
    protected synchronized Signaller endRead() {
        if (--activeReaders_ == 0 && waitingWriters_ > 0)
            return writerLock_;
        else
            return null;
    }


    /**
     * Called upon termination of a write.
     * Returns the object to signal to wake up a waiter, or null if no such
     */
    protected synchronized Signaller endWrite() {
        activeWriter_ = null;
        if (waitingReaders_ > 0 && allowReader())
            return readerLock_;
        else if (waitingWriters_ > 0)
            return writerLock_;
        else
            return null;
    }


    /**
     * Reader and Writer requests are maintained in two different
     * wait sets, by two different objects. These objects do not
     * know whether the wait sets need notification since they
     * don't know preference rules. So, each supports a
     * method that can be selected by main controlling object
     * to perform the notifications.  This base class simplifies mechanics.
     */

    protected abstract class Signaller { // base for ReaderLock and WriterLock
        abstract void signalWaiters();
    }

    protected class ReaderLock extends Signaller implements Lock {

        public  void lock() {
            //if (Thread.interrupted()) throw new RuntimeException("Lock interrupted", new InterruptedException());
            InterruptedException ie = null;
            synchronized(this) {
                if (!startReadFromNewReader()) {
                    for (;;) {
                        try {
                            ReaderLock.this.wait();
                            if (startReadFromWaitingReader())
                                return;
                        }
                        catch(InterruptedException ex){
                            cancelledWaitingReader();
                            ie = ex;
                            break;
                        }
                    }
                }
            }
            if (ie != null) {
                // fall through outside synch on interrupt.
                // This notification is not really needed here,
                // but may be in plausible subclasses
                writerLock_.signalWaiters();
                throw new RuntimeException("Lock interrupted, " + ie);
            }
        }


        public void unlock() {
            Signaller s = endRead();
            if (s != null) s.signalWaiters();
        }


        synchronized void signalWaiters() { ReaderLock.this.notifyAll(); }

        public boolean tryLock() {
            long msecs = 0;
            //if (Thread.interrupted()) throw new RuntimeException("Lock interrupted", new InterruptedException());
            InterruptedException ie = null;
            synchronized(this) {
                if (msecs <= 0)
                    return startRead();
                else if (startReadFromNewReader())
                    return true;
                else {
                    long waitTime = msecs;
                    long start = System.currentTimeMillis();
                    for (;;) {
                        try { ReaderLock.this.wait(waitTime);  }
                        catch(InterruptedException ex){
                            cancelledWaitingReader();
                            ie = ex;
                            break;
                        }
                        if (startReadFromWaitingReader())
                            return true;
                        else {
                            waitTime = msecs - (System.currentTimeMillis() - start);
                            if (waitTime <= 0) {
                                cancelledWaitingReader();
                                break;
                            }
                        }
                    }
                }
            }
            // safeguard on interrupt or timeout:
            writerLock_.signalWaiters();
            if (ie != null) throw new RuntimeException("Lock interrupted, " + ie);
            else return false; // timed out
        }
    }

    protected class WriterLock extends Signaller implements Lock {

        public void lock() {
            //if (Thread.interrupted()) throw new RuntimeException("Lock interrupted", new InterruptedException());
            InterruptedException ie = null;
            synchronized(this) {
                if (!startWriteFromNewWriter()) {
                    for (;;) {
                        try {
                            WriterLock.this.wait();
                            if (startWriteFromWaitingWriter())
                                return;
                        }
                        catch(InterruptedException ex){
                            cancelledWaitingWriter();
                            WriterLock.this.notify();
                            ie = ex;
                            break;
                        }
                    }
                }
            }
            if (ie != null) {
                // Fall through outside synch on interrupt.
                // On exception, we may need to signal readers.
                // It is not worth checking here whether it is strictly necessary.
                readerLock_.signalWaiters();
                throw new RuntimeException("Lock interrupted, " + ie);
            }
        }

        public void unlock(){
            Signaller s = endWrite();
            if (s != null) s.signalWaiters();
        }

        synchronized void signalWaiters() { WriterLock.this.notify(); }

        public boolean tryLock() {
            long msecs = 0;
            //if (Thread.interrupted()) throw new RuntimeException("Lock interrupted", new InterruptedException());
            InterruptedException ie = null;
            synchronized(this) {
                if (msecs <= 0)
                    return startWrite();
                else if (startWriteFromNewWriter())
                    return true;
                else {
                    long waitTime = msecs;
                    long start = System.currentTimeMillis();
                    for (;;) {
                        try { WriterLock.this.wait(waitTime);  }
                        catch(InterruptedException ex){
                            cancelledWaitingWriter();
                            WriterLock.this.notify();
                            ie = ex;
                            break;
                        }
                        if (startWriteFromWaitingWriter())
                            return true;
                        else {
                            waitTime = msecs - (System.currentTimeMillis() - start);
                            if (waitTime <= 0) {
                                cancelledWaitingWriter();
                                WriterLock.this.notify();
                                break;
                            }
                        }
                    }
                }
            }

            readerLock_.signalWaiters();
            if (ie != null) throw new RuntimeException("Lock interrupted, " + ie);
            else return false; // timed out
        }
    }
}