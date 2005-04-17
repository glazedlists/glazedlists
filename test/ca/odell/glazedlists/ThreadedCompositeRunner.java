/*             Glazed Lists  http://publicobject.com/glazedlists/             */                        
/*        Copyright 2003-2005 publicobject.com, O'Dell Engineering Ltd.       */
package ca.odell.glazedlists;

// for being a JUnit test case
import junit.framework.*;
// the core Glazed Lists package
import ca.odell.glazedlists.util.concurrent.*;
// the Glazed Lists' change objects
import ca.odell.glazedlists.event.*;
// Java collections are used for underlying data storage
import java.util.*;

/**
 * The ThreadedCompositeRunner is a manually-executed test that verifies that
 * the CompositeList is thread safe. It creates a handful of threads that write
 * and read composite lists.
 *
 * <p>To verify that deadlocks are not occuring, each thread updates its time variable
 * with the current time. One extra thread cycles through examining the clock
 * variables and prints the time of the oldest thread. If such a variable never
 * increments, we have a deadlock!
 *
 * @author <a href="mailto:kevin@swank.ca">Kevin Maltby</a>
 */
public class ThreadedCompositeRunner {
    
    /** the global clock of the threaded composite runner */
    private int clock = 0;

    /**
     * A clocked thread is a thread that records the most recent time
     * it performed an operation.
     */
    abstract class ClockedThread implements Runnable {
        private int time = 0;
        public int getTime() {
            synchronized(ThreadedCompositeRunner.this) {
                return time;
            }
        }
        protected void stampTime() {
            synchronized(ThreadedCompositeRunner.this) {
                time = clock;
            }
        }
    }
    
    /**
     * The reader thread alternates between reading a single random value
     * and reading the entire list.
     */
    class ReaderThread extends ClockedThread {
        private EventList listToRead;
        private Random dice = new Random();
        public ReaderThread(EventList listToRead) {
            this.listToRead = listToRead;
        }
        public void run() {
            while(true) {
                // read one element
                listToRead.getReadWriteLock().readLock().lock();
                if(listToRead.size() > 0) {
                    listToRead.get(dice.nextInt(listToRead.size()));
                }
                listToRead.getReadWriteLock().readLock().unlock();
                // read all elements
                listToRead.getReadWriteLock().readLock().lock();
                listToRead.toArray();
                listToRead.getReadWriteLock().readLock().unlock();
                // stamp the time
                stampTime();
                // yield and loop
                Thread.yield();
            }
        }
    }
        
    
    /**
     * The writer thread alternates between inserting a single random value
     * and removing a single random value.
     */
    class WriterThread extends ClockedThread {
        private EventList listToWrite;
        private Random dice = new Random();
        public WriterThread(EventList listToWrite) {
            this.listToWrite = listToWrite;
        }
        public void run() {
            while(true) {
                int repeat = dice.nextInt(1000);
                for(int i = 0; i < repeat; i++) {
                    listToWrite.add(dice.nextInt(i + 1), Boolean.TRUE);
                    // stamp the time
                    stampTime();
                    // yield and loop
                    Thread.yield();
                }
                while(!listToWrite.isEmpty()) {
                    listToWrite.remove(dice.nextInt(listToWrite.size()));
                    // stamp the time
                    stampTime();
                    // yield and loop
                    Thread.yield();
                }
            }
        }
    }
    
    /**
     * The main method starts this experiment.
     */
    public static void main(String[] args) {
        new ThreadedCompositeRunner().run(args);
    }
    
    /**
     * Starts a series of reader and writer threads and verifies that they
     * continue reading and writing.
     */
    public void run(String[] args) {
        if(args.length != 3) {
            System.out.println("Usage: ThreadedCompositeRunner <writer count> <reader count> <composite size>");
            System.out.println("");
            System.out.println("writer count: The number of writer threads, each writing their own BasicEventList");
            System.out.println("reader count: The number of reader threads, each reading a CompositeList");
            System.out.println("composite size: The number of writer lists per CompositeList");
            System.out.println("");
            System.out.println("The application starts threads for the writers and readers. Then one extra thread");
            System.out.println("called the coordinator asks each thread the time that they last completed an");
            System.out.println("operation. This is equivalent to asking each thread whether it has deadlocked yet.");
            System.out.println("A thread that has deadlocked will have a last-operation-time value that never");
            System.out.println("increments. The coordinator prints the oldest last-operation-time and the current");
            System.out.println("time. If the oldest last-operation-time has not incremented since last check, the");
            System.out.println("coordinator only prints the current time. A user can tell that a thread has deadlocked");
            System.out.println("if the clock value is printed repeatedly but the oldest last-operation-time no longer");
            System.out.println("increments.");
            return;
        }
        
        int writerCount = Integer.parseInt(args[0]);
        int readerCount = Integer.parseInt(args[1]);
        int compositeSize = Integer.parseInt(args[2]);
        
        // prepare a random number generator
        Random dice = new Random();

        // create a list of lists
        List lists = new ArrayList();
        
        // accumulate a list of threads
        List threads = new ArrayList();
        
        // start some writer threads 
        for(int i = 0; i < writerCount; i++) {
            BasicEventList list = new BasicEventList();
            lists.add(list);
            WriterThread thread = new WriterThread(list);
            threads.add(thread);
        }
        
        // create some composite lists & reader threads
        List compositeLists = new ArrayList();
        for(int i = 0; i < readerCount; i++) {
            CompositeList compositeList = new CompositeList();
            for(int j = 0; j < compositeSize; j++) {
                compositeList.addMemberList((EventList)lists.get(dice.nextInt(lists.size())));
            }
            compositeLists.add(compositeList);
            // create a reader thread for this composite list
            ReaderThread readerThread = new ReaderThread(compositeList);
            threads.add(readerThread);

        }
        
        // start the threads
        int threadId = 100;
        for(Iterator i = threads.iterator(); i.hasNext(); ) {
            ClockedThread runnable = (ClockedThread)i.next();
            new Thread(runnable, "T" + threadId++).start();
        }
        
        // watch the times
        int lastMinTime = -1;
        while(true) {
            // find the minimum time of all threads
            int minTime = clock;
            for(Iterator i = threads.iterator(); i.hasNext(); ) {
                ClockedThread thread = (ClockedThread)i.next();
                int threadTime = thread.getTime();
                if(threadTime < minTime) minTime = threadTime;
            }
            // print the min time if it has changed
            if(minTime > lastMinTime) {
                System.out.println("CLOCK " + clock + " MINTIME " + minTime);
                lastMinTime = minTime;
            } else {
                if(clock % 100 == 0) System.out.println("CLOCK " + clock);
            }
            // increment the clock
            synchronized(ThreadedCompositeRunner.this) {
                clock++;
            }
            // yield and loop
            Thread.yield();
        }
    }
}