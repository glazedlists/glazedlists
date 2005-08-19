/* Glazed Lists                                                 (c) 2003-2005 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.impl;

import java.util.*;
// the core glazed lists packages
import ca.odell.glazedlists.event.*;
import ca.odell.glazedlists.*;

/**
 * This {@link ListEventListener} updates a plain old {@link List} so that
 * its contents match those of a source {@link EventList}.
 *
 * @author <a href="mailto:jesse@swank.ca">Jesse Wilson</a>
 */
public class SyncListener<E> implements ListEventListener<E> {

    /** the list to sync against the {@link EventList}. */
    private List<E> target;

    /** remember sync list size to attempt to detect drifts */
    private int targetSize;

    /**
     * Create a {@link SyncListener} that listens for changes on the
     * specified source {@link EventList} and copies its data to the
     * specified target {@link List}.
     */
    public SyncListener(EventList<E> source, List<E> target) {
        this.target = target;
        target.clear();
        target.addAll(source);

        // attempt to detect drifts
        targetSize = target.size();

        // handle changes
        source.addListEventListener(this);
    }


    /** {@inheritDoc} */
    public void listChanged(ListEvent<E> listChanges) {
        EventList<E> source = listChanges.getSourceList();

        // if the list sizes don't match, we have a problem
        if(target.size() != targetSize) {
            throw new IllegalStateException("Synchronize EventList target has been modified");
        }

        // update the target list with the EventList
        while(listChanges.next()) {
            int index = listChanges.getIndex();
            int type = listChanges.getType();

            if(type == ListEvent.INSERT) {
                target.add(index, source.get(index));
                targetSize++;
            } else if(type == ListEvent.UPDATE) {
                target.set(index, source.get(index));
            } else if(type == ListEvent.DELETE) {
                target.remove(index);
                targetSize--;
            }
        }
    }
}