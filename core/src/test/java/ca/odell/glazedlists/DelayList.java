/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists;

import ca.odell.glazedlists.event.ListEvent;

/**
 * This EventList simply delays each read and write operation by the given
 * delay (in milliseconds).
 *
 * @author James Lemieux
 */
public class DelayList<S> extends TransformedList<S,S> {

    private final long delay;

    public DelayList(EventList<S> source, long delay) {
        super(source);

        if (delay < 1)
            throw new IllegalArgumentException("delay is not a non-negative number");
        this.delay = delay;
        source.addListEventListener(this);
    }

    private void delay() {
        try {
            Thread.sleep(this.delay);
        } catch (InterruptedException e) {
            // this is best effort only
        }
    }

    @Override
    public void listChanged(ListEvent<S> listChanges) {
        this.delay();
        updates.forwardEvent(listChanges);
    }

    @Override
    protected boolean isWritable() {
        return true;
    }

    @Override
    public int size() {
        this.delay();
        return super.size();
    }

    @Override
    public S get(int index) {
        this.delay();
        return super.get(index);
    }
}