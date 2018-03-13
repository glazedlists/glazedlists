/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists;

import ca.odell.glazedlists.event.ListEvent;

import java.util.LinkedList;
import java.util.List;

/**
 * This EventList acts as a pass-through transformation which records
 * information about which Threads were responsible for which operations. After
 * a recording session is complete, it can be used to provide an overall
 * picture of the threaded access a list is permitting. It is especially useful
 * for debugging threading problems. The type of information that can be
 * provided by this ThreadRecorderEventList includes:
 *
 * <ul>
 *   <li>{@link #getReadWriteLog()} returns a list of Strings that are either
 *       <code>"R"</code> or <code>"W"</code>. An example log could be
 *       {"R", "R", "W", "W", "R"}.
 *
 *   <li>{@link #getReadWriteBlockCount()} returns the number of times the
 *       access pattern changed between reading and writing. A ReadWriteLog of
 *       {"R", "R", "W", "W", "R"} would cause this method to return 3 since it
 *       was either reading and writing in blocks 3 times.
 *
 *   <li>{@link #getThreadLog()} returns the name of each Thread which accessed
 *       this ThreadRecorderEventList and the order in which those accesses
 *       occurred.
 * </ul>
 *
 * This EventList is particularly useful as a debugging tool.
 *
 * @author James Lemieux
 */
public class ThreadRecorderEventList<S> extends TransformedList<S,S> {

    private static final String READ_OPERATION = "R";
    private static final String WRITE_OPERATION = "W";

    // the number of times we switch between reading and writing
    private int readWriteBlockCount = 0;

    // a list of Rs and Ws for each atomic read and write
    private final List<String> readWriteLog = new LinkedList<>();

    // a log of the thread names and the order in which they access this list
    private final List<String> threadLog = new LinkedList<>();


    // to detect a change in reading or writing, we store the last operation performed
    private String lastRecordedOperation;
    // to detect a change in the Thread accessing the list, we store the last Thread which accessed the list
    private String lastRecordedThreadName;

    public ThreadRecorderEventList(EventList<S> source) {
        super(source);
        source.addListEventListener(this);
    }

    /**
     * Returns the number of times this list switched between being read and
     * being written.
     */
    public int getReadWriteBlockCount() {
        return readWriteBlockCount;
    }

    /**
     * Returns a log of all Reads and Writes which occurred on this list. The
     * order of this list can only be guaranteed on a well-locked, well-behaved
     * list. An example of a log is: {"R", "R", "W", "W", "R"}.
     */
    public List<String> getReadWriteLog() {
        return readWriteLog;
    }

    /**
     * Returns a log of the name of each Thread which took turns accessing this
     * list. Only changes in the Thread are written to this log. For example,
     * if the Thread with the name "AWT-EventQueue-0" is the only accessor of
     * this list, this log will contain only one entry for "AWT-EventQueue-0".
     * If "AWT-EventQueue-0" and a second Thread "My Writer Thread" both access
     * this list, then the thread log will alternate between the two threads
     * but never contain the same thread name twice in a row.
     */
    public List<String> getThreadLog() {
        return threadLog;
    }

    /**
     * A single synchronized method to update the data we collect about Threaded
     * access to this list.
     *
     * @param operation the type of operation performed; one of
     *      {@link #READ_OPERATION} or {@link #WRITE_OPERATION}
     */
    private synchronized void record(String operation) {
        if (this.lastRecordedOperation != operation)
            readWriteBlockCount++;

        if (this.lastRecordedThreadName != Thread.currentThread().getName()) {
            this.lastRecordedThreadName = Thread.currentThread().getName();
            this.threadLog.add(this.lastRecordedThreadName);
        }

        this.lastRecordedOperation = operation;
        this.readWriteLog.add(operation);
    }

    /** {@inheritDoc} */
    @Override
    public S get(int index) {
        record(READ_OPERATION);
        return super.get(index);
    }

    /** {@inheritDoc} */
    @Override
    protected boolean isWritable() {
        return true;
    }

    /** {@inheritDoc} */
    @Override
    public void listChanged(ListEvent<S> listChanges) {
        // record each of the WRITES one at a time
        while (listChanges.next()) {
            record(WRITE_OPERATION);
        }

        listChanges.reset();
        updates.forwardEvent(listChanges);
    }
}