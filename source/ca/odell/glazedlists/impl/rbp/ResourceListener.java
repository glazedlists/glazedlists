/**
 * Glazed Lists
 * http://glazedlists.dev.java.net/
 *
 * COPYRIGHT 2003 O'DELL ENGINEERING LTD.
 */
package ca.odell.glazedlists.impl.rbp;

// NIO is used for BRP
import java.util.*;
import java.nio.*;
import java.io.*;
import ca.odell.glazedlists.impl.io.Bufferlo;

/**
 * A resource listener subscribes to the deltas published by a resource.
 *
 * @author <a href="mailto:jesse@odel.on.ca">Jesse Wilson</a>
 */
public interface ResourceListener {

    /**
     * Handles a change in a resource contained by the specified delta.
     */
    public void resourceUpdated(Resource resource, Bufferlo delta);
}
