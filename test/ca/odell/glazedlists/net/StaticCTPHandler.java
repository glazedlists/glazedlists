/**
 * Glazed Lists
 * http://glazedlists.dev.java.net/
 *
 * COPYRIGHT 2003 O'DELL ENGINEERING LTD.
 */
package ca.odell.glazedlists.net;

import java.util.*;
// for being a JUnit test case
import junit.framework.*;
// NIO is used for CTP
import java.nio.*;
import java.nio.channels.*;
import java.io.UnsupportedEncodingException;

/**
 * A CTPHandler where all data is known beforehand.
 */
class StaticCTPHandler implements CTPHandler {
    private List tasks = new ArrayList();
    public void addExpected(String data) {
        tasks.add(new Expected(data));
    }
    public void addEnqueued(String data) {
        tasks.add(new Enqueued(data));
    }
    public void connectionReady(CTPConnection source) {
        sendEnqueued(source);
    }
    public void receiveChunk(CTPConnection source, ByteBuffer data) {
        if(!data.hasRemaining()) return;
        
        if(tasks.size() == 0) throw new IllegalStateException("Unexpected data " + data);
        Expected expected = (Expected)tasks.get(0);
        int remain = expected.consume(data);
        if(remain == 0) {
            tasks.remove(0);
            sendEnqueued(source);
        }
    }
    public void connectionClosed(CTPConnection source, Exception reason) {
        if(!tasks.isEmpty()) throw new IllegalStateException("Close " + this + " with " + tasks.size() + " events pending: " + tasks);
    }
    private void sendEnqueued(CTPConnection connection) {
        while(tasks.size() > 0 && tasks.get(0) instanceof Enqueued) {
            Enqueued enqueued = (Enqueued)tasks.remove(0);
            connection.sendChunk(enqueued.getData());
        }
        if(tasks.isEmpty()) {
            connection.close();
        }
    }
    public boolean isDone() {
        return tasks.isEmpty();
    }
}

/**
 * Models an expected incoming chunk.
 */
class Expected {
    private byte[] data;
    private int offset = 0;
    public Expected(String charData) {
        try {
            this.data = charData.getBytes("US-ASCII");
        } catch(UnsupportedEncodingException e) {
            throw new IllegalStateException(e.getMessage());
        }
    }
    /**
     * Consumes the specified data, which must match the expected data. If this does
     * not match, an Exception was thrown.
     *
     * @return the number of bytes remaining to be consumed.
     */
    public int consume(ByteBuffer lunch) {
        for( ; offset < data.length && lunch.hasRemaining(); offset++) {
            if(lunch.get() != data[offset]) throw new IllegalStateException();
        }
        return data.length - offset;
    }
    /**
     * Get the number of bytes that this has left to consume.
     */
    public int bytesLeft() {
        return data.length - offset;
    }
}

/**
 * Models an outgoing chunk.
 */
class Enqueued {
    private ByteBuffer data;
    public Enqueued(String charData) {
        try {
            this.data = ByteBuffer.wrap(charData.getBytes("US-ASCII"));
        } catch(UnsupportedEncodingException e) {
            throw new IllegalStateException(e.getMessage());
        }
    }
    public ByteBuffer getData() {
        return data;
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
    public CTPHandler constructHandler() {
        return (CTPHandler)handlers.remove(0);
    }
}


