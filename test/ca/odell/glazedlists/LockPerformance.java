/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

/**
 * Utility class for analyzing the performance of the Lock implementations.
 *
 * @author James Lemieux
 */
public class LockPerformance {

    private static final Random dice = new Random();

    private static final EventList<Integer> sharedList = new BasicEventList<Integer>();

    private static volatile int totalReadDelay = 0;
    private static volatile int totalWriteDelay = 0;

    private static int numReads = 200000;
    private static int numWrites = 200000;

    private static List<ReaderRunnable> readerRunnables = new ArrayList<ReaderRunnable>();
    private static List<Thread> readerThreads = new ArrayList<Thread>();

    private static List<WriterRunnable> writerRunnables = new ArrayList<WriterRunnable>();
    private static List<Thread> writerThreads = new ArrayList<Thread>();

    /**
     * Execute a performance test that is specified on the command line.
     */
    public static void main(String[] args) throws Exception {
        final Integer element = new Integer(1);
        for (int i = 0; i < 100; i++)
            sharedList.add(element);

        for (int i = 0; i < 50; i++) {
            final ReaderRunnable rr = new ReaderRunnable();
            readerRunnables.add(rr);
            readerThreads.add(new Thread(rr));
        }

        for (int i = 0; i < 1; i++) {
            final WriterRunnable wr = new WriterRunnable();
            writerRunnables.add(wr);
            writerThreads.add(new Thread(wr));
        }

        for(Iterator i = readerThreads.iterator(); i.hasNext(); ) {
            ((Thread)i.next()).start();
        }

        for(Iterator i = writerThreads.iterator(); i.hasNext(); ) {
            ((Thread)i.next()).start();
        }

        for(Iterator i = readerThreads.iterator(); i.hasNext(); ) {
            ((Thread)i.next()).join();
        }

        for(Iterator i = writerThreads.iterator(); i.hasNext(); ) {
            ((Thread)i.next()).join();
        }

        System.out.println("totalReadDelay = " + totalReadDelay + "ms (" + elapsedTime(totalReadDelay) + ")");
        System.out.println("totalWriteDelay = " + totalWriteDelay + "ms (" + elapsedTime(totalWriteDelay) + ")");
    }

    private static String elapsedTime(long ms) {
        return ms + " ms";
    }

    private static class ReaderRunnable implements Runnable {
        private int writes = 0;

        @Override
        public void run() {
            while (writes++ < numWrites) {
                long startTime = System.currentTimeMillis();
                sharedList.getReadWriteLock().readLock().lock();
                long diff = System.currentTimeMillis() - startTime;
                totalReadDelay += (diff < 0 ? 0 : diff);
                try {
                    final int index = dice.nextInt(sharedList.size());
                    sharedList.get(index);
                } finally {
                    sharedList.getReadWriteLock().readLock().unlock();
                }
            }
        }
    }

    private static class WriterRunnable implements Runnable {
        private int reads = 0;

        @Override
        public void run() {
            while (reads++ < numReads) {
                long startTime = System.currentTimeMillis();
                sharedList.getReadWriteLock().writeLock().lock();
                long diff = System.currentTimeMillis() - startTime;
                totalWriteDelay += (diff < 0 ? 0 : diff);
                try {
                    final int index = dice.nextInt(sharedList.size());
                    sharedList.set(index, new Integer(2));
                } finally {
                    sharedList.getReadWriteLock().writeLock().unlock();
                }
            }
        }
    }
}