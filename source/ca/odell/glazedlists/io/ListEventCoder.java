/**
 * Glazed Lists
 * http://glazedlists.dev.java.net/
 *
 * COPYRIGHT 2003 O'DELL ENGINEERING LTD.
 */
package ca.odell.glazedlists.io;

import java.util.*;
import ca.odell.glazedlists.*;
import ca.odell.glazedlists.event.*;
import ca.odell.glazedlists.impl.io.*;
import java.nio.*;
import java.io.*;

/**
 * An utility interface for converting Objects to bytes for storage or network
 * transport.
 *
 * @author <a href="mailto:jesse@odel.on.ca">Jesse Wilson</a>
 */
public class ListEventCoder {
    
    /** the virtual event type */
    private static final int CLEAR = -1;
    
    /**
     * Convert the specified ListEvent to bytes.
     */
    public static Bufferlo listEventToBytes(ListEvent listEvent, ByteCoder byteCoder) throws IOException {
        // populate the list of parts
        List parts = new ArrayList();
        while(listEvent.next()) {
            int index = listEvent.getIndex();
            int type = listEvent.getType();
            Object value = null;
            if(type == ListEvent.INSERT || type == ListEvent.UPDATE) value = listEvent.getSourceList().get(index);
            parts.add(new ListEventPart(index, type, value));
        }
        
        return partsToBytes(parts, byteCoder);
    }
    
    /**
     * Convert the List to a ListEvent. This is for snapshots or compressions.
     */
    public static Bufferlo listToBytes(EventList list, ByteCoder byteCoder) throws IOException {
        List parts = new ArrayList();
        
        // start with a clear
        parts.add(new ListEventPart(-1, ListEventCoder.CLEAR, null));
        
        // add all values as adds
        for(int i = 0; i < list.size(); i++) {
            parts.add(new ListEventPart(i, ListEvent.INSERT, list.get(i)));
        }
        
        // get the whole list
        return partsToBytes(parts, byteCoder);
    }
    
    /**
     * Apply the specified list event to the specified target list. The write lock
     * for this list must already be acquired if the list is shared between threads.
     */
    public static void bytesToListEvent(Bufferlo listEvent, EventList target, ByteCoder byteCoder) throws IOException {
        List parts = bytesToParts(listEvent, byteCoder);
        for(Iterator i = parts.iterator(); i.hasNext(); ) {
            ListEventPart part = (ListEventPart)i.next();
            if(part.isDelete()) {
                target.remove(part.getIndex());
            } else if(part.isUpdate()) {
                target.set(part.getIndex(), part.getValue());
            } else if(part.isInsert()) {
                target.add(part.getIndex(), part.getValue());
            } else if(part.isClear()) {
                target.clear();
            }
        }
    }
        
    /**
     * Encode the parts into bytes.
     */
    private static Bufferlo partsToBytes(List parts, ByteCoder delegate) throws IOException {
        // prepare the result
        Bufferlo partsAsBytes = new Bufferlo();
        DataOutputStream dataOut = new DataOutputStream(partsAsBytes.getOutputStream());
        
        // convert each part in sequence
        for(int i = 0; i < parts.size(); i++) {
            ListEventPart part = (ListEventPart)parts.get(i);
            
            // write the index of this part
            dataOut.writeInt(i);
            
            // write the type
            dataOut.writeInt(part.getType());

            // write the index of the change
            if(part.hasIndex()) dataOut.writeInt(part.getIndex());
            
            // write the value
            if(part.hasValue()) {
                Bufferlo valueBuffer = new Bufferlo();
                delegate.encode(part.getValue(), valueBuffer.getOutputStream());
                dataOut.writeInt(valueBuffer.length());
                dataOut.flush();
                partsAsBytes.append(valueBuffer);
            }
        }
        
        // that was easy
        return partsAsBytes;
    }
    
    /**
     * Decode the bytes into parts.
     */
    private static List bytesToParts(Bufferlo partsAsBytes, ByteCoder delegate) throws IOException {
        // prepare the result
        List parts = new ArrayList();

        // read till the end of the file
        DataInputStream dataIn = new DataInputStream(partsAsBytes.getInputStream());
        
        // convert each part in sequence
        while(partsAsBytes.length() > 0) {
            ListEventPart currentPart = new ListEventPart();
            
            // read the index of this part
            int expectedPartIndex = parts.size();
            int partIndex = dataIn.readInt();
            if(partIndex != expectedPartIndex) throw new IOException("Expected " + expectedPartIndex + " but found " + partIndex);
            
            // read in the type of this part
            currentPart.setType(dataIn.readInt());
            
            // read in the index of this change
            if(currentPart.hasIndex()) {
                currentPart.setIndex(dataIn.readInt());
            }
            
            // read the value
            if(currentPart.hasValue()) {
                int valueLength = dataIn.readInt();
                Bufferlo valueBuffer = partsAsBytes.consume(valueLength);
                Object value = delegate.decode(valueBuffer.getInputStream());
                currentPart.setValue(value);
            }
            
            // we've completed one part
            parts.add(currentPart);
        }
        
        // that was easy
        return parts;
    }

    /**
     * A part of a ListEvent that is byte codable.
     */
    static class ListEventPart {
        
        /** the changed index */
        private int index = -1;
        
        /** the type of change, INSERT, UPDATE, DELETE or CLEAR */
        private int type = ListEventCoder.CLEAR;
        
        /** the inserted or updated value */
        private Object value = null;
        
        /**
         * Create a new ByteCodableListEventBlock.
         */
        public ListEventPart(int index, int type, Object value) {
            this.index = index;
            this.type = type;
            this.value = value;
        }
        
        /**
         * Create a new empty ByteCodableListEventBlock.
         */
        public ListEventPart() {
        }
        
        public int getIndex() {
            return index;
        }
        public void setIndex(int index) {
            this.index = index;
        }

        public Object getValue() {
            return value;
        }
        public void setValue(Object value) {
            this.value = value;
        }

        public int getType() {
            return type;
        }
        public void setType(int type) {
            this.type = type;
        }

        public boolean isDelete() {
            return (type == ListEvent.DELETE);
        }
        public boolean isUpdate() {
            return (type == ListEvent.UPDATE);
        }
        public boolean isInsert() {
            return (type == ListEvent.INSERT);
        }
        public boolean isClear() {
            return (type == ListEventCoder.CLEAR);
        }
        public boolean hasIndex() {
            return (isUpdate() || isInsert() || isDelete());
        }
        public boolean hasValue() {
            return (isUpdate() || isInsert());
        }
    }
}
