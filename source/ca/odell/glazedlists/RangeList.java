/* Glazed Lists                                                 (c) 2003-2005 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists;

import ca.odell.glazedlists.event.ListEvent;

/**
 * This {@link EventList} shows values from a continuous range of indices from
 * a source {@link EventList}. It can be used to limit the length of a list to
 * a desired size.
 *
 * @author <a href="mailto:jesse@swank.ca">Jesse Wilson</a>
 */
public class RangeList extends TransformedList {

    private int desiredStart = 0;
    private int desiredEnd = -1;

    /** the first index in this list, inclusive */
    private int currentStartIndex;

    /** the last index in this list, exclusive */
    private int currentEndIndex;

    /**
     * Create a new {@link RangeList} that limits the specified {@link EventList}
     * to a desired range.
     */
    protected RangeList(EventList source) {
        super(source);

        currentStartIndex = 0;
        currentEndIndex = source.size();

        source.addListEventListener(this);
    }

    /** {@inheritDoc} */
    public void listChanged(ListEvent listChanges) {
        // This EventList handles changes to the source EventList using a
        // two-phase approach:
        // 1. The change event is iterated and the current bound indices are
        //    offset to reflect the change. Each change event within the
        //    range of indices is forwarded.
        // 2. In the second phase, during setRange(), the current indices
        //    are adjusted back to their previously set 'desired values'
        //    if possible.

        updates.beginEvent(true);

        // propagate the event and offset local indices
        while(listChanges.next()) {
            int changeType = listChanges.getType();
            int changeIndex = listChanges.getIndex();

            if(changeType == ListEvent.DELETE) {
                if(changeIndex < currentStartIndex) {
                    currentStartIndex--;
                    currentEndIndex--;
                } else if(changeIndex < currentEndIndex) {
                    currentEndIndex--;
                    updates.addDelete(changeIndex - currentStartIndex);
                }
            } else if(changeType == ListEvent.INSERT) {
                if(changeIndex < currentStartIndex) {
                    currentStartIndex++;
                    currentEndIndex++;
                } else if(changeIndex < currentEndIndex) {
                    currentEndIndex++;
                    updates.addInsert(changeIndex - currentStartIndex);
                }
            } else if(changeType == ListEvent.UPDATE) {
                updates.addUpdate(changeIndex - currentStartIndex);
            }
        }

        // adjust the displayed range
        setRange(desiredStart, desiredEnd);

        updates.commitEvent();
    }

    /**
     * Set the range of values displayed by this {@link RangeList}.
     *
     * @param desiredStart the first index of the source {@link EventList} to show, inclusive
     * @param desiredEnd the last index of the source {@link EventList} to show, exclusive
     */
    public void setRange(int desiredStart, int desiredEnd) {
        this.desiredStart = desiredStart;
        this.desiredEnd = desiredEnd;

        updates.beginEvent(true);

        // translate the positive or negative desired values to indices
        int desiredStartIndex = desiredStart >= 0 ? desiredStart : source.size() + desiredStart + 1;
        int desiredEndIndex = desiredEnd >= 0 ? desiredEnd : source.size() + desiredEnd + 1;
        // adjust the start index to the size of the list
        if(desiredStartIndex < 0) desiredStartIndex = 0;
        else if(desiredStartIndex > source.size()) desiredStartIndex = source.size();
        // adjust the end index to the size of the list
        if(desiredEndIndex < desiredStartIndex) desiredEndIndex = desiredStartIndex;
        else if(desiredEndIndex > source.size()) desiredEndIndex = source.size();

        // insert before the beginning
        if(desiredStartIndex < currentStartIndex) {
            updates.addInsert(0, currentStartIndex - desiredStartIndex - 1);

        // delete thru to the new beginning
        } else if(currentStartIndex < desiredStartIndex && currentStartIndex < currentEndIndex) {
            int deleteThru = Math.min(desiredStartIndex, currentEndIndex);
            updates.addDelete(0, deleteThru - currentStartIndex - 1);
        }
        currentStartIndex = desiredStartIndex;

        // delete past the end
        if(desiredEndIndex < currentEndIndex) {
            int deleteFrom = Math.max(desiredEndIndex, currentStartIndex);
            updates.addDelete(deleteFrom - currentStartIndex, currentEndIndex - currentStartIndex - 1);

        // insert thru to the new end
        } else if(currentEndIndex < desiredEndIndex && desiredStartIndex < desiredEndIndex) {
            int insertFrom = Math.max(currentEndIndex, currentStartIndex);
            updates.addInsert(insertFrom - currentStartIndex, desiredEndIndex - currentStartIndex - 1);
        }
        currentEndIndex = desiredEndIndex;

        updates.commitEvent();
    }

    /**
     * Set the range to include the specified indicies, offset from the end of
     * the source {@link EventList}. For example, to show the last five values, use:
     * <code>RangeList.setTailRange(5, 0);</code>
     *
     * <p>To include the 3rd last and 2nd last values, use:
     * <code>RangeList.setTailRange(3, 1);</code>.
     */
    public void setTailRange(int desiredStart, int desiredEnd) {
        setRange(-desiredStart - 1, -desiredEnd - 1);
    }

    /** {@inheritDoc} */
    public int size() {
        return currentEndIndex - currentStartIndex;
    }

    /** {@inheritDoc} */
    protected int getSourceIndex(int mutationIndex) {
        return mutationIndex + currentStartIndex;
    }
}