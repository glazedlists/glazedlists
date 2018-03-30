/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.impl.pmap;

// NIO is used for CTP
import ca.odell.glazedlists.impl.io.Bufferlo;

/**
 * Listens for the loading of a value.
 *
 * @author <a href="mailto:jesse@swank.ca">Jesse Wilson</a>
 *
 * @deprecated The io extension and its types are deprecated.
 *             This extension becomes unsupported and will be removed
 *             from the official distribution with the next major release.
 */
@FunctionalInterface
@Deprecated
public interface ValueCallback {

    /**
     * Handles a value being completely loaded into memory and ready to read.
     */
    public void valueLoaded(Chunk member, Bufferlo value);
}