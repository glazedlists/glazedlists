/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.impl.io;

import ca.odell.glazedlists.event.ListEvent;
import ca.odell.glazedlists.event.ListEventListener;
import ca.odell.glazedlists.io.ByteCoder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Encodes List events as they arrive.
 *
 * @author <a href="mailto:jesse@swank.ca">Jesse Wilson</a>
 */
public class EventEncoderListener implements ListEventListener {
    
    /** the Bufferlo encodings */
    public List encodings = new ArrayList();
    
    /** the delegate coder for elements */
    public ByteCoder byteCoder = null;
    
    /**
     * Create a new EventEncoderListener.
     */
    public EventEncoderListener(ByteCoder byteCoder) {
        this.byteCoder = byteCoder;
    }
    
    /**
     * Handles a change by adding the encoding of the change.
     */
    public void listChanged(ListEvent listChanges) {
        try {
            Bufferlo encoding = ListEventToBytes.toBytes(listChanges, byteCoder);
            encodings.add(encoding);
        } catch(IOException e) {
            throw new IllegalStateException(e.getMessage());
        }
    }

    /**
     * Get the list of encodings, one per event received.
     */
    public List getEncodings() {
        return encodings;
    }
}
