/* Glazed Lists                                                      (c) 2005 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
/*                                                          StarLight Systems */
package ca.odell.glazedlists.util.concurrent;

import junit.framework.TestCase;
import ca.odell.glazedlists.JobQueue;


/**
 * Make sure we can interrupt threads while locking is working.
 *
 * @author <a href="mailto:jesse@swank.ca">Jesse Wilson</a>
 */
public class LockInterruptionTest extends TestCase {

    public void testDefaultLockInterrupt() {
        testLockInterrupt(LockFactory.DEFAULT.createReadWriteLock());
    }

    public void testJava14LockInterrupt() {
        testLockInterrupt(new J2SE12LockFactory().createReadWriteLock());
    }

    /**
     * Make sure that our locks don't throw interrupted exceptions.
     */
    private void testLockInterrupt(final ReadWriteLock lock) {
        JobQueue jobQueue = new JobQueue();
        jobQueue.start();

        lock.writeLock().lock();

        // interrupt that thread
        jobQueue.interrupt();

        // create a thread that will block waiting on Lock.lock()
        JobQueue.Job result = jobQueue.invokeLater(new LockALockRunnable(lock.writeLock()));

        // make sure there's contention on the lock
        sleep();

        // release the lock, this will cause our other job to finish
        lock.writeLock().unlock();

        // flush the queue
        jobQueue.flush();

        // validate that our thread didn't throw interrupted exception
        assertNull(result.getThrown());

        // validate that the thread is still interrupted
        jobQueue.invokeAndWait(new FailIfNotInterruptedRunnable());
    }

    /**
     * Wait a bit to give a chance for thread contention.
     */
    private void sleep() {
        synchronized(this) {
            try {
                wait(1000);
            } catch(InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * We'd prefer anonymous inner classes here, but alas for Java 1.4 generics
     * we can't do anonymous inner classes.
     */
    private static class LockALockRunnable implements Runnable {
        private Lock lock;
        public LockALockRunnable(Lock lock) {
            this.lock = lock;
        }
        public void run() {
            lock.lock();
        }
    }

    /**
     * We'd prefer anonymous inner classes here, but alas for Java 1.4 generics
     * we can't do anonymous inner classes.
     */
    private static class FailIfNotInterruptedRunnable implements Runnable {
        public void run() {
            if(!Thread.interrupted()) {
                fail();
            }
        }
    }
}
