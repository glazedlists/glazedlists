/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists;

import ca.odell.glazedlists.event.ListEvent;

/**
 * A list that allows nested events to be managed externally. This is very useful
 * in testing handling of sophisticated events, because it allows big events
 * to be composed from simpler ones.
 */
public class ExternalNestingEventList<E> extends TransformedList<E, E> {
    private final boolean forward;

    public ExternalNestingEventList(EventList<E> source) {
        this(source, false);
    }

    public ExternalNestingEventList(EventList<E> source, boolean forward) {
        super(source);
        this.forward = forward;
        source.addListEventListener(this);
    }

    public void beginEvent(boolean allowNested) {
        updates.beginEvent(allowNested);
    }

    public void commitEvent() {
        updates.commitEvent();
    }

    protected boolean isWritable() {
        return true;
    }

    public void listChanged(ListEvent<E> listChanges) {
        if(forward) {
            updates.forwardEvent(listChanges);
        } else {
            // don't forward() the event, just add the changes
            if(listChanges.isReordering()) {
                updates.reorder(listChanges.getReorderMap());
            } else {
                while(listChanges.next()) {
                    final int type = listChanges.getType();
                    final int index = listChanges.getIndex();
                    final E oldValue = listChanges.getOldValue();
                    final E newValue = listChanges.getNewValue();

                    switch (type) {
                        case ListEvent.INSERT: updates.elementInserted(index, newValue); break;
                        case ListEvent.UPDATE: updates.elementUpdated(index, oldValue, newValue); break;
                        case ListEvent.DELETE: updates.elementDeleted(index, oldValue); break;
                        default: throw new IllegalStateException("Unknown type: " + type);
                    }
                }
            }
        }
    }
}