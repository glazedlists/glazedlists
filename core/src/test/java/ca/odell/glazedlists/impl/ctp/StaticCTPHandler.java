/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.impl.ctp;

import ca.odell.glazedlists.impl.io.Bufferlo;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

/**
 * A CTPHandler where all data is known beforehand.
 */
class StaticCTPHandler implements CTPHandler {

    /** the actions to be performed on this connection */
    private List tasks = new ArrayList();

    /** whether this connection has connected */
    private boolean ready = false;

    /** whether this connection has disconnected */
    private boolean closed = false;

    /** the reason this connection was closed */
    private Exception closeReason = null;

    /** the connection being handled */
    private CTPConnection connection = null;

    /** the incoming data */
    private Bufferlo incoming = new Bufferlo();

    /**
     * Add expected incoming data.
     */
    public void addExpected(String data) {
        tasks.add(new Expected(data));
    }

    /**
     * Add queued outgoing data.
     */
    public void addEnqueued(String data) {
        tasks.add(new Enqueued(data));
    }

    /**
     * Notify that this connection is ready for use.
     */
    @Override
    public synchronized void connectionReady(CTPConnection source) {
        if(ready) throw new IllegalStateException("Connection already ready");
        ready = true;
        this.connection = source;
        handlePendingTasks();
    }

    /**
     * Handle the specified incoming data.
     */
    @Override
    public synchronized void receiveChunk(CTPConnection source, Bufferlo data) {
        incoming.append(data);

        // read all the expected elements from the data
        try {
            while(incoming.length() > 0) {
                Expected expected = (Expected)tasks.get(0);
                boolean consumed = expected.tryConsume(incoming);
                if(!consumed) return;

                tasks.remove(0);
                handlePendingTasks();
            }
        } catch(IOException e) {
            throw new RuntimeException(e);
        } catch(ParseException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Notify that this connection is no longer ready for use.
     */
    @Override
    public synchronized void connectionClosed(CTPConnection source, Exception closeReason) {
        if(closed) throw new IllegalStateException("Connection already closed");
        closed = true;
        connection = null;
        if(!tasks.isEmpty()) throw new IllegalStateException("Closed prematurely! " + tasks.size() + " Pending tasks " + tasks + ", reason " + closeReason);
        this.closeReason = closeReason;
        notifyAll();
    }

    /**
     * Handle pending tasks that can be performed immediately.
     */
    private void handlePendingTasks() {
        while(tasks.size() > 0 && tasks.get(0) instanceof Enqueued) {
            Enqueued enqueued = (Enqueued)tasks.remove(0);
            connection.sendChunk(enqueued.getData());
        }
        if(tasks.isEmpty()) {
            notifyAll();
        }
    }

    /**
     * Close this connection.
     */
    public synchronized void close() {
        if(closed) return;
        if(!ready) throw new IllegalStateException("Connection not established");
        connection.close();
    }

    /**
     * Ensures that this handler has completed its tasks. If it has not, a RuntimeException
     * shall be thrown.
     */
    public synchronized void assertComplete(long timeout) {
        if(!tasks.isEmpty()) {
            try {
                wait(timeout);
            } catch(InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        if(!tasks.isEmpty()) throw new IllegalStateException(tasks.size() + " uncompleted tasks " + tasks + ", pending data \"" + incoming + "\"");
    }

    /**
     * Ensures that this handler has been closed.
     */
    public synchronized Exception assertClosed(long timeout) {
        if(!closed) {
            try {
                wait(timeout);
            } catch(InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        if(!closed) throw new IllegalStateException("Open connection with " + tasks.size() + " uncompleted tasks");
        return closeReason;
    }
}

/**
 * Models an expected incoming chunk.
 */
class Expected {

    /** the expected data */
    private String expected = null;

    /**
     * Creates a new expectation of the specified data.
     */
    public Expected(String charData) {
        this.expected = charData;
    }

    /**
     * Consumes the specified data, which must match the expected data. If this does
     * not match, an Exception was thrown.
     *
     * @return the number of bytes remaining to be consumed.
     */
    public boolean tryConsume(Bufferlo lunch) throws IOException, ParseException {
        if(lunch.length() < expected.length()) return false;

        lunch.consume(expected);
        expected = null;
        return true;
    }

    /**
     * Whether this has been satisfied.
     */
    private boolean done() {
        return (expected == null);
    }

    /**
     * Print the expected string.
     */
    @Override
    public String toString() {
        if(expected.length() > 30) return "Expected \"" + expected.length() + ":" + expected.substring(0, 30) + "\"";
        else return "Expected \"" + expected + "\"";
    }
}

/**
 * Models an outgoing chunk.
 */
class Enqueued {

    private Bufferlo data = new Bufferlo();

    public Enqueued(String charData) {
        data.write(charData);
    }

    public Bufferlo getData() {
        return data;
    }

    @Override
    public String toString() {
        if(data.length() > 30) return "Enqueued \"" + data.length() + ":" + data.toString().substring(0, 30) + "\"";
        else return "Enqueued \"" + data + "\"";
    }
}

/**
 * A static handler for handing predictable test data.
 */
class StaticCTPHandlerFactory implements CTPHandlerFactory {
    private List handlers = new ArrayList();
    public void addHandler(CTPHandler handler) {
        handlers.add(handler);
    }
    @Override
    public CTPHandler constructHandler() {
        if(handlers.isEmpty()) throw new IllegalStateException("No more handlers");
        return (CTPHandler)handlers.remove(0);
    }
}