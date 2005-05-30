/* Glazed Lists                                                 (c) 2003-2005 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
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
     * Handles a change in a resource contained by the specified delta. This method
     * will be called while holding the Resource's write lock.
     */
    public void resourceUpdated(Resource resource, Bufferlo delta);
}
