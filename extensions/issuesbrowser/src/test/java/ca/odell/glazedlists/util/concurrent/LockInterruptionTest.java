/* Glazed Lists                                                      (c) 2005 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
/*                                                          StarLight Systems */
package ca.odell.glazedlists.util.concurrent;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import org.junit.Test;

import com.publicobject.misc.util.concurrent.JobQueue;


/**
 * Make sure we can interrupt threads while locking is working.
 *
 * @author <a href="mailto:jesse@swank.ca">Jesse Wilson</a>
 */
public class LockInterruptionTest {

    @Test
    public void testDefaultLockInterrupt() {
        testLockInterrupt(LockFactory.DEFAULT.createReadWriteLock());
    }

    /**
     * Make sure that our locks don't throw interrupted exceptions.
     */
    private void testLockInterrupt(final ReadWriteLock lock) {
        JobQueue jobQueue = new JobQueue();
        Thread jobQueueThread = new Thread(jobQueue);
        jobQueueThread.start();

        lock.writeLock().lock();

        // interrupt that thread
        jobQueueThread.interrupt();

        // create a thread that will block waiting on Lock.lock()
        JobQueue.Job result = jobQueue.invokeLater(new LockALockRunnable(lock.writeLock()));

        // make sure there's contention on the lock
        sleep(1000);

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
    private void sleep(long duration) {
        synchronized(this) {
            try {
                wait(duration);
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
        @Override
        public void run() {
            lock.lock();
        }
    }

    /**
     * We'd prefer anonymous inner classes here, but alas for Java 1.4 generics
     * we can't do anonymous inner classes.
     */
    private static class FailIfNotInterruptedRunnable implements Runnable {
        @Override
        public void run() {
            if(!Thread.interrupted()) {
                fail();
            }
        }
    }
}
