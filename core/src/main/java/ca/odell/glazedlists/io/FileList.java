/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.io;

// the core Glazed Lists packages
import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.TransformedList;
import ca.odell.glazedlists.event.ListEvent;
import ca.odell.glazedlists.impl.io.Bufferlo;
import ca.odell.glazedlists.impl.io.ListEventToBytes;
import ca.odell.glazedlists.impl.pmap.Chunk;
import ca.odell.glazedlists.impl.pmap.PersistentMap;
import ca.odell.glazedlists.util.concurrent.ReadWriteLock;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * An {@link EventList} that is persisted to disk.
 *
 * <p><font size="5"><strong><font color="#FF0000">Warning:</font></strong> This
 * class is a technology preview and is subject to API changes.</font>
 *
 * <p><table border="1" width="100%" cellpadding="3" cellspacing="0">
 * <tr class="TableHeadingColor"><td colspan=2><font size="+2"><b>EventList Overview</b></font></td></tr>
 * <tr><td class="TableSubHeadingColor"><b>Writable:</b></td><td>yes</td></tr>
 * <tr><td class="TableSubHeadingColor"><b>Concurrency:</b></td><td>Requires {@link ReadWriteLock} for every access, even for single-threaded use</td></tr>
 * <tr><td class="TableSubHeadingColor"><b>Performance:</b></td><td>N/A</td></tr>
 * <tr><td class="TableSubHeadingColor"><b>Memory:</b></td><td>O(N)</td></tr>
 * <tr><td class="TableSubHeadingColor"><b>Unit Tests:</b></td><td>N/A</td></tr>
 * <tr><td class="TableSubHeadingColor"><b>Issues:</b></td><td>N/A</td></tr>
 * </table>
 *
 * @author <a href="mailto:jesse@swank.ca">Jesse Wilson</a>
 */
public final class FileList extends TransformedList {

    /** the destination file, just for user convenience */
    private File file = null;

    /** how bytes are encoded and decoded */
    private ByteCoder byteCoder;

    /** the underlying storage of ListEvents */
    private PersistentMap storage = null;

    /** the ID of the next update to write to disc */
    private int nextUpdateId = 81;

    /** whether this list can be modified */
    private boolean writable = true;

    /**
     * Create a {@link FileList} that stores its data in the specified file.
     */
    public FileList(File file, ByteCoder byteCoder) throws IOException {
        super(new BasicEventList());
        this.file = file;
        this.byteCoder = byteCoder;

        // store the updates in a persistent map
        storage = new PersistentMap(file);

        // sequence the updates
        SortedMap sequentialUpdates = new TreeMap();
        for(Iterator k = storage.keySet().iterator(); k.hasNext(); ) {
            Integer key = (Integer)k.next();
            Bufferlo valueBuffer = ((Chunk)storage.get(key)).getValue();
            sequentialUpdates.put(key, valueBuffer);
        }

        // replay all the updates from the file
        for(Iterator u = sequentialUpdates.keySet().iterator(); u.hasNext(); ) {
            Integer key = (Integer)u.next();
            Bufferlo update = (Bufferlo)sequentialUpdates.get(key);
            ListEventToBytes.toListEvent(update, this, byteCoder);
        }

        // prepare the next update id to use
        if(!sequentialUpdates.isEmpty()) {
            nextUpdateId = ((Integer)sequentialUpdates.lastKey()).intValue() + 1;
        }

        // now that we're up-to-date, listen for further events
        source.addListEventListener(this);
    }

    /** {@inheritDoc} */
    @Override
    public boolean isWritable() {
        return writable;
    }

    /** {@inheritDoc} */
    @Override
    public void listChanged(ListEvent listChanges) {
        // write the change to disc
        try {
            ListEvent listChangesCopy = listChanges.copy();
            Bufferlo listChangesBytes = ListEventToBytes.toBytes(listChangesCopy, byteCoder);
            storage.put(new Integer(nextUpdateId), new Chunk(listChangesBytes));
            nextUpdateId++;

        } catch(IOException e) {
            throw new IllegalStateException(e.getMessage());
        }

        // forward the event to interested listeners
        updates.forwardEvent(listChanges);
    }

    /**
     * Closes this FileList so that it consumes no disc resources. The list may
     * continue to be read until it is {@link #dispose() disposed}.
     */
    public void close() {
        if(storage != null) storage.close();
        storage = null;
        writable = false;
    }

    /** {@inheritDoc} */
    @Override
    public void dispose() {
        close();
        super.dispose();
    }
}
