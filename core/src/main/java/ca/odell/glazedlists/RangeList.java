/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists;

import ca.odell.glazedlists.event.ListEvent;

/**
 * This {@link EventList} shows values from a continuous range of indices from
 * a source {@link EventList}. It can be used to limit the length of a list to
 * a desired size.
 *
 * <p><strong><font color="#FF0000">Warning:</font></strong> This class is
 * thread ready but not thread safe. See {@link EventList} for an example
 * of thread safe code.
 *
 * <p><table border="1" width="100%" cellpadding="3" cellspacing="0">
 * <tr class="TableHeadingColor"><td colspan=2><font size="+2"><b>EventList Overview</b></font></td></tr>
 * <tr><td class="TableSubHeadingColor"><b>Writable:</b></td><td>yes</td></tr>
 * <tr><td class="TableSubHeadingColor"><b>Concurrency:</b></td><td>thread ready, not thread safe</td></tr>
 * <tr><td class="TableSubHeadingColor"><b>Performance:</b></td><td>reads: O(1), writes O(1), change range O(1)</td></tr>
 * <tr><td class="TableSubHeadingColor"><b>Memory:</b></td><td>0 bytes per element</td></tr>
 * <tr><td class="TableSubHeadingColor"><b>Unit Tests:</b></td><td>N/A</td></tr>
 * <tr><td class="TableSubHeadingColor"><b>Issues:</b></td><td>
 *   <a href="https://glazedlists.dev.java.net/issues/show_bug.cgi?id=238">238</a>
 *   <a href="https://glazedlists.dev.java.net/issues/show_bug.cgi?id=278">278</a>
 * </td></tr>
 * </table>
 *
 * @author <a href="mailto:jesse@swank.ca">Jesse Wilson</a>
 */
public class RangeList<E> extends TransformedList<E, E> {

    /** the user-specified range of the source list to include */
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
    public RangeList(EventList<E> source) {
        super(source);

        currentStartIndex = 0;
        currentEndIndex = source.size();

        source.addListEventListener(this);
    }

    /** {@inheritDoc} */
    @Override
    public final void listChanged(ListEvent<E> listChanges) {
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
            E oldValue =listChanges.getOldValue();
            E newValue = null;
            if (changeIndex < source.size()) {
                newValue = source.get(changeIndex);
            }

            if(changeType == ListEvent.DELETE) {
                if(changeIndex < currentStartIndex) {
                    currentStartIndex--;
                    currentEndIndex--;
                } else if(changeIndex < currentEndIndex) {
                    currentEndIndex--;
                    updates.elementDeleted(changeIndex - currentStartIndex, oldValue);
                }
            } else if(changeType == ListEvent.INSERT) {
                if(changeIndex < currentStartIndex) {
                    currentStartIndex++;
                    currentEndIndex++;
                } else if(changeIndex < currentEndIndex) {
                    currentEndIndex++;
                    updates.elementInserted(changeIndex - currentStartIndex, newValue);
                }
            } else if(changeType == ListEvent.UPDATE) {
                if(changeIndex >= currentStartIndex && changeIndex < currentEndIndex) {
                    updates.elementUpdated(changeIndex - currentStartIndex, oldValue, newValue);
                }
            }
        }

        // adjust the displayed range to accomodate for the source changes
        adjustRange();

        updates.commitEvent();
    }

    /**
     * Set the range of values displayed by this {@link RangeList}.
     *
     * @param startIndex the first index of the source {@link EventList} to show, inclusive
     * @param endIndex the last index of the source {@link EventList} to show, exclusive
     *
     * @deprecated 2/15/2006 use {@link #setHeadRange(int, int)} instead. The
     * introduction of {@link #setMiddleRange(int, int)} caused us to want a
     * consistent naming scheme for all set*Range methods.
     */
    @Deprecated
    public void setRange(int startIndex, int endIndex) {
        this.setHeadRange(startIndex, endIndex);
    }

    /**
     * Set the range of values displayed by this {@link RangeList}.
     *
     * @param startIndex the first index of the source {@link EventList} to show, inclusive
     * @param endIndex the last index of the source {@link EventList} to show, exclusive
     */
    public void setHeadRange(int startIndex, int endIndex) {
        this.desiredStart = startIndex;
        this.desiredEnd = endIndex;
        adjustRange();
    }

    /**
     * Set the range to include the specified indices, the startIndex offset from the
     * front of the source {@link EventList} and the endIndex offset from the end
     * of the source {@link EventList}.
     *
     * <p>For example, to include everything but the first element, use
     * <code>RangeList.setMiddleRange(1, 0);</code>.
     *
     * <p>For example, to include everything but the last element, use
     * <code>RangeList.setMiddleRange(0, 1);</code>.
     */
    public void setMiddleRange(int startIndex, int endIndex) {
        this.desiredStart = startIndex;
        this.desiredEnd = -endIndex - 1;
        adjustRange();
    }

    /**
     * Set the range to include the specified indices, offset from the end of
     * the source {@link EventList}. For example, to show the last five values, use:
     * <code>RangeList.setTailRange(5, 0);</code>
     *
     * <p>To include the 3rd last and 2nd last values, use:
     * <code>RangeList.setTailRange(3, 1);</code>.
     */
    public void setTailRange(int startIndex, int endIndex) {
        this.desiredStart = -startIndex - 1;
        this.desiredEnd = -endIndex - 1;
        adjustRange();
    }

    /**
     * Adjust the range of the {@link RangeList} in response to changes in the
     * source list or the desired start and end indices.
     */
    protected final void adjustRange() {
        updates.beginEvent(true);

        // get the new range
        int desiredStartIndex = getStartIndex();
        int desiredEndIndex = getEndIndex();

        // normalize the range so start index is the smallest index and end index is the largest
        if (desiredEndIndex < desiredStartIndex) {
            int temp = desiredEndIndex;
            desiredEndIndex = desiredStartIndex;
            desiredStartIndex = temp;
        }

        // insert before the beginning
        if(desiredStartIndex < currentStartIndex) {
            updates.addInsert(0, currentStartIndex - desiredStartIndex - 1);

        // delete thru to the new beginning
        } else if(currentStartIndex < desiredStartIndex && currentStartIndex < currentEndIndex) {
            int deleteThru = Math.min(desiredStartIndex, currentEndIndex);
            for(int i = currentStartIndex; i < deleteThru; i++) {
                updates.elementDeleted(0, source.get(i));
            }
        }
        currentStartIndex = desiredStartIndex;

        // delete past the end
        if(desiredEndIndex < currentEndIndex) {
            for(int i = desiredEndIndex; i < currentEndIndex; i++) {
                updates.elementDeleted(desiredEndIndex - currentStartIndex, source.get(i));
            }

        // insert thru to the new end
        } else if(currentEndIndex < desiredEndIndex && desiredStartIndex < desiredEndIndex) {
            int insertFrom = Math.max(currentEndIndex, currentStartIndex);
            updates.addInsert(insertFrom - currentStartIndex, desiredEndIndex - currentStartIndex - 1);
        }
        currentEndIndex = desiredEndIndex;

        updates.commitEvent();
    }

    /** {@inheritDoc} */
    @Override
    public final int size() {
        return currentEndIndex - currentStartIndex;
    }

    /** {@inheritDoc} */
    @Override
    protected final int getSourceIndex(int mutationIndex) {
        return mutationIndex + currentStartIndex;
    }

    /** {@inheritDoc} */
    @Override
    protected final boolean isWritable() {
        return true;
    }

    /**
     * Get the first index of the source {@link EventList}
     * that is presented in this {@link RangeList}.
     */
    public int getStartIndex() {
        // translate the positive or negative desired values to indices
        int desiredStartIndex = desiredStart >= 0 ? desiredStart : source.size() + desiredStart + 1;
        // adjust the start index to the size of the list
        if(desiredStartIndex < 0) return 0;
        if(desiredStartIndex > source.size()) return source.size();
        return desiredStartIndex;
    }

    /**
     * Get the first index of the source {@link EventList}
     * that is beyond the range of this {@link RangeList}.
     */
    public int getEndIndex() {
        // translate the positive or negative desired values to indices
        int desiredEndIndex = desiredEnd >= 0 ? desiredEnd : source.size() + desiredEnd + 1;
        // adjust the end index to the size of the list
        int desiredStartIndex = getStartIndex();
        if(desiredEndIndex < desiredStartIndex) return desiredStartIndex;
        if(desiredEndIndex > source.size()) return source.size();
        return desiredEndIndex;
    }
}