/**
 * Glazed Lists
 * http://glazedlists.dev.java.net/
 *
 * COPYRIGHT 2003 O'DELL ENGINEERING LTD.
 */
package ca.odell.glazedlists.net;

// NIO is used for BRP
import java.util.*;
import java.nio.*;
import java.io.*;

/**
 * A resource is a dynamic Object that can publish its changes as a series of deltas.
 * It is also possible to construct a resource using a shapshot.
 *
 * @author <a href="mailto:jesse@odel.on.ca">Jesse Wilson</a>
 */
public interface BRPResource {

    /**
     */
    public ByteBuffer toSnapshot();

    /**
     */
    public void fromSnapshot(ByteBuffer snapshot);
    
    /**
     */
    public void update(ByteBuffer delta);
    
    /**
     */
    public void addResourceListener(BRPResourceListener listener);
    
    /**
     */
    public void removeResourceListener(BRPResourceListener listener);
    
}
