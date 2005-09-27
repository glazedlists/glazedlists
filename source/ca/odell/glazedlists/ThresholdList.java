/* Glazed Lists                                                 (c) 2003-2005 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists;

// the core Glazed Lists package
import ca.odell.glazedlists.event.*;
// to use standard collections
import java.util.*;

/**
 * An {@link EventList} that shows a range of the elements of the source
 * {@link EventList}. Each element in the source {@link EventList} is assigned
 * an integer value via a {@link Evaluator}. This integer is used
 * to determine whether the element fits in the {@link ThresholdList}s range.
 *
 * <p>By modifying the upper and lower thresholds in the range, the list can
 * be filtered in a simple and powerful way.
 *
 * <p>The {@link ThresholdList} lends itself to use with a slider widget for
 * manipulating one of the range's endpoints.
 *
 * <p>One use case for {@link ThresholdList} is in a media player application.
 * By creating a {@link Evaluator} for a song's bitrate, the user could
 * limit results to MP3 files between 192 and 320kbps.
 *
 * <p>Note that the elements in the {@link ThresholdList} will be presented in
 * order sorted by their {@link Evaluator} value.
 *
 * <p>This {@link EventList} supports all write operations.
 *
 * <p><strong><font color="#FF0000">Warning:</font></strong> This class
 * breaks the contract required by {@link java.util.List}. See {@link EventList}
 * for an example.
 *
 * <p><table border="1" width="100%" cellpadding="3" cellspacing="0">
 * <tr class="tableheadingcolor"><td colspan=2><font size="+2"><b>EventList Overview</b></font></td></tr>
 * <tr><td class="tablesubheadingcolor"><b>Writable:</b></td><td>yes</td></tr>
 * <tr><td class="tablesubheadingcolor"><b>Concurrency:</b></td><td>thread ready, not thread safe</td></tr>
 * <tr><td class="tablesubheadingcolor"><b>Performance:</b></td><td>reads: O(log N), writes O(log N), change threshold O(log N)</td></tr>
 * <tr><td class="tablesubheadingcolor"><b>Memory:</b></td><td>72 bytes per element</td></tr>
 * <tr><td class="tablesubheadingcolor"><b>Unit Tests:</b></td><td>N/A</td></tr>
 * <tr><td class="tablesubheadingcolor"><b>Issues:</b></td><td>N/A</td></tr>
 * </table>
 *
 * @author <a href="mailto:kevin@swank.ca">Kevin Maltby</a>
 */
public final class ThresholdList<E> extends TransformedList<E,E> {

    /** the index in the list which corresponds to the lower bound for this list */
    private int lowerThresholdIndex = 0;

    /** the index in the list which corresponds to the upper bound for this list */
    private int upperThresholdIndex = 0;

    /** the lower bound to use to define list containment */
    private int lowerThreshold = 0;

    /** the upper bound to use to define list containment */
    private int upperThreshold = 0;

    /** a local cache of the size of the source list to improve performance */
    private int sourceSize = 0;

    /** the evaluator to use to compare Objects against the threshold */
    private Evaluator<E> evaluator = null;

    /**
     * Creates a {@link ThresholdList} that provides range-filtering based on the
     * specified {@link EventList} based on the specified integer JavaBean property.
     */
    public ThresholdList(EventList<E> source, String propertyName) {
        this(source, (Evaluator<E>) GlazedLists.thresholdEvaluator(propertyName));
    }

    /**
     * Creates a {@link ThresholdList} that provides range-filtering on the
     * specified {@link EventList} using the specified {@link Evaluator}.
     */
    public ThresholdList(EventList<E> source, Evaluator<E> evaluator) {
        super(new SortedList<E>(source, new ThresholdComparator<E>(evaluator)));
        this.source.addListEventListener(this);
        this.evaluator = evaluator;
        sourceSize = source.size();

        // Include all possible elements by default
        setUpperThreshold(Integer.MAX_VALUE);
        setLowerThreshold(Integer.MIN_VALUE);
    }

    /** {@inheritDoc} */
    public void listChanged(ListEvent<E> listChanges) {

        // recache the source size
        sourceSize = source.size();

        // Make all of the changes happen atomically
        updates.beginEvent();

        while(listChanges.next()) {
            int sortedIndex = listChanges.getIndex();
            int type = listChanges.getType();

            int value = Integer.MIN_VALUE;

            if(type != ListEvent.DELETE && sourceSize > 0) {
                value = evaluator.evaluate(source.get(sortedIndex));
            }

            // The index is below the lower threshold and is not an edge case
            if(sortedIndex < lowerThresholdIndex - 1) {
                if(type == ListEvent.INSERT) {
                    lowerThresholdIndex ++;
                    upperThresholdIndex ++;
                } else if(type == ListEvent.DELETE) {
                    lowerThresholdIndex --;
                    upperThresholdIndex --;
                } else if(type == ListEvent.UPDATE) {
                    // Do Nothing
                }

            // The index could result in a lower threshold edge case
            } else if(sortedIndex == lowerThresholdIndex - 1) {
                int transformedIndex = sortedIndex - Math.max(lowerThresholdIndex, 0);
                if(type == ListEvent.INSERT) {
                    if(value < lowerThreshold) {
                        lowerThresholdIndex ++;
                        upperThresholdIndex++;

                    } else if(value > upperThreshold) {
                        // Do nothing

                    } else {
                        upperThresholdIndex ++;
                        updates.addInsert(transformedIndex);
                    }

                // Handle DELETE for the edge condition
                } else if(type == ListEvent.DELETE) {
                    lowerThresholdIndex --;
                    upperThresholdIndex --;

                // Handle UPDATE for the edge condition
                } else if(type == ListEvent.UPDATE) {
                    if(value >= lowerThreshold) {
                        lowerThresholdIndex --;
                        updates.addInsert(0);
                    }
                }

            // The index affects the current view of the list
            } else if(sortedIndex >= lowerThresholdIndex && sortedIndex <= upperThresholdIndex) {
                int transformedIndex = sortedIndex - Math.max(lowerThresholdIndex, 0);
                if(type == ListEvent.INSERT) {
                    upperThresholdIndex ++;
                    // The value is beyond the threshold so ignore it
                    if(value < lowerThreshold) {
                        lowerThresholdIndex ++;

                    } else if(value > upperThreshold) {
                        throw new IllegalStateException();

                    // The value is below the threshold so forward an insert
                    } else {
                        updates.addInsert(transformedIndex);
                    }

                // Just forward deletes
                } else if(type == ListEvent.DELETE) {
                    upperThresholdIndex --;
                    updates.addDelete(transformedIndex);

                // Inspect the updated value to see what kind of event to forward
                } else if(type == ListEvent.UPDATE) {
                    // The value is beyond the lower threshold so delete
                    if(value < lowerThreshold) {
                        lowerThresholdIndex ++;
                        updates.addDelete(transformedIndex);

                    // The value is beyond the upper threshold so delete
                    } else if(value > upperThreshold) {
                        upperThresholdIndex --;
                        updates.addDelete(transformedIndex);

                    // The value is still below the threshold so forward an update
                    } else {
                        updates.addUpdate(transformedIndex);
                    }
                }

            // The index could result in an upper threshold edge case
            } else if(sortedIndex == upperThresholdIndex + 1) {
                int transformedIndex = sortedIndex - Math.max(lowerThresholdIndex, 0);
                if(type == ListEvent.INSERT) {
                    if(value >= lowerThreshold && value <= upperThreshold) {
                        upperThresholdIndex ++;
                        updates.addInsert(transformedIndex);
                    } else if(sortedIndex == lowerThresholdIndex && value <= upperThreshold) {
                        lowerThresholdIndex ++;
                        upperThresholdIndex ++;
                    }
                } else if(type == ListEvent.DELETE) {
                    // Do Nothing
                } else if(type == ListEvent.UPDATE) {
                    if(value <= upperThreshold) {
                        upperThresholdIndex ++;
                        updates.addInsert(transformedIndex);
                    }
                }

            // The index is above the upper threshold and is not and edge case
            } else if(sortedIndex > upperThresholdIndex + 1) {
                // Do nothing
            } else {
                throw new IllegalStateException();
            }
        }

        updates.commitEvent();

    }

    /**
     * Sets the lower threshold for this list to be the result of calling
     * {@link Evaluator#evaluate(Object) evaluate()} on the given object.
     *
     * <p>This list can be used programmatically rather than hooking it up to
     * a UI component. <strong>Calling this method directly while this list
     * is connected to a particular widget could result in errors.</strong>
     *
     * <p><strong><font color="#FF0000">Warning:</font></strong> This method is
     * thread ready but not thread safe. See {@link EventList} for an example
     * of thread safe code.
     */
    public void setLowerThreshold(E object) {
        setLowerThreshold(evaluator.evaluate(object));
    }

    /**
     * Sets the lower threshold for this list.
     *
     * <p>This list can be used programmatically rather than hooking it up to
     * a UI component. <strong>Calling this method directly while this list
     * is connected to a particular widget could result in errors.</strong>
     *
     * <p><strong><font color="#FF0000">Warning:</font></strong> This method is
     * thread ready but not thread safe. See {@link EventList} for an example
     * of thread safe code.
     */
    public void setLowerThreshold(int threshold) {
        // the index at the new threshold
        int newListIndex = 0;

        // Threshold change is irrelevant
        if(sourceSize == 0) {
            lowerThreshold = threshold;
            lowerThresholdIndex = 0;
            return;

        // Threshold is unchanged
        } else if(threshold == lowerThreshold) {
            return;

        // Threshold is changed
        } else {
            newListIndex = ((SortedList<E>)source).indexOfSimulated(new Integer(threshold));
            // return -1 if the value is before the list
            if(newListIndex == 0) newListIndex = source.indexOf(new Integer(threshold));
        }

        // update the threshold
        lowerThreshold = threshold;

        // the index at the threshold has not changed
        if(newListIndex == lowerThresholdIndex) {
            return;
        }

        // the index at the threshold has changed but no event should be thrown
        if((newListIndex == -1 && lowerThresholdIndex == 0) ||
            (newListIndex == 0 && lowerThresholdIndex == -1)) {

            lowerThresholdIndex = newListIndex;
            return;
        }

        // Changes are necessary so prepare an event
        updates.beginEvent();

        // The threshold is lower
        if(newListIndex < lowerThresholdIndex) {
            // The list was empty and stays that way
            if(newListIndex > upperThresholdIndex) {
                // definite no-op

            // The list was empty and should now contain values
            } else if(lowerThresholdIndex > upperThresholdIndex) {
                // make sure that it should contain new values
                if(!(newListIndex == -1 && upperThresholdIndex == -1)) {
                    updates.addInsert(0, upperThresholdIndex - Math.max(newListIndex, 0));
                }

            // The list contains more values
            } else {
                updates.addInsert(0, lowerThresholdIndex - Math.max(newListIndex, 0) - 1);
            }

        // The threshold is higher
        } else if(newListIndex > lowerThresholdIndex) {
            // The list was empty and stays that way
            if(lowerThresholdIndex > upperThresholdIndex) {
                // definite no-op

            // The list contained values and should now be empty
            } else if(newListIndex > upperThresholdIndex) {
                // make sure the list contained values
                if(!(lowerThresholdIndex == -1 && upperThresholdIndex == -1)) {
                    updates.addDelete(0, upperThresholdIndex - Math.max(lowerThresholdIndex, 0));
                }

            // The list contains fewer values
            } else {
                updates.addDelete(0, newListIndex - Math.max(lowerThresholdIndex, 0) - 1);
            }
        }

        // Update the lowerThresholdIndex and fire the event
        lowerThresholdIndex = newListIndex;
        updates.commitEvent();
    }

    /**
     * Gets the lower threshold for this list
     */
    public int getLowerThreshold() {
        return lowerThreshold;
    }

    /**
     * Sets the upper threshold for this list to be the result of calling
     * {@link Evaluator#evaluate(Object) evaluate()} on the given object.
     *
     * <p>This list can be used programmatically rather than hooking it up to
     * a UI component. <strong>Calling this method directly while this list
     * is connected to a particular widget could result in errors.</strong>
     *
     * <p><strong><font color="#FF0000">Warning:</font></strong> This method is
     * thread ready but not thread safe. See {@link EventList} for an example
     * of thread safe code.
     */
    public void setUpperThreshold(E object) {
        setUpperThreshold(evaluator.evaluate(object));
    }

    /**
     * Sets the upper threshold for this list.
     *
     * <p><strong><font color="#FF0000">Warning:</font></strong> This method is
     * thread ready but not thread safe. See {@link EventList} for an example
     * of thread safe code.
     */
    public void setUpperThreshold(int threshold) {

        // the index at the new threshold
        int newListIndex = 0;

        // Threshold change is irrelevant
        if(sourceSize == 0) {
            upperThreshold = threshold;
            upperThresholdIndex = -1;
            return;

        // Threshold is unchanged
        } else if(threshold == upperThreshold) {
            return;

        // Threshold is changed
        } else {
            if(threshold == Integer.MAX_VALUE) {
                newListIndex = sourceSize - 1;
            } else {
                newListIndex = ((SortedList)source).indexOfSimulated(new Integer(threshold+1)) - 1;
            }
        }

        // update the threshold
        upperThreshold = threshold;

        // the index at the threshold has not changed
        if(newListIndex == upperThresholdIndex) {
            return;
        }

        // the index at the threshold has changed but no event should be thrown
        if((newListIndex == sourceSize && upperThresholdIndex == sourceSize - 1) ||
           (newListIndex == sourceSize - 1 && upperThresholdIndex == sourceSize)) {
            upperThresholdIndex = newListIndex;
            return;
        }

        // Changes are necessary so prepare an event
        updates.beginEvent();

        // The threshold is lower
        if(newListIndex < upperThresholdIndex) {
            // The list was empty and stays that way
            if(upperThresholdIndex < lowerThresholdIndex) {
                // definite no-op

            // The list contained values and should now be empty
            } else if(newListIndex < lowerThresholdIndex) {
                // make sure the list contained values
                if(!(lowerThresholdIndex == sourceSize && upperThresholdIndex == sourceSize)) {
                    updates.addDelete(0, upperThresholdIndex - Math.max(lowerThresholdIndex, 0));
                }

            // The list contains fewer values
            } else {
                updates.addDelete(newListIndex + 1, upperThresholdIndex - Math.max(lowerThresholdIndex, 0));
            }

        // The threshold is higher
        } else if(newListIndex > upperThresholdIndex) {
            // The list was empty and stays that way
            if(newListIndex < lowerThresholdIndex) {
                // definite no-op

            // The list was empty and should now contain values
            } else if(upperThresholdIndex < lowerThresholdIndex) {
                updates.addInsert(0, newListIndex - Math.max(lowerThresholdIndex, 0));

            // The list contains more values
            } else {
                updates.addInsert(upperThresholdIndex - Math.max(lowerThresholdIndex, 0) + 1, newListIndex);
            }
        }

        // Update the upperThresholdIndex and fire the event
        upperThresholdIndex = newListIndex;
        updates.commitEvent();
    }

    /**
     * Gets the upper threshold for this list
     */
    public int getUpperThreshold() {
        return upperThreshold;
    }

    /**
     * A convenience method to allow access to the {@link Evaluator}
     * that was provided on construction.
     */
    public Evaluator<E> getEvaluator() {
        return evaluator;
    }

    /** {@inheritDoc} */
    public int size() {
        // Check for the exclusionary edge condition
        if(lowerThresholdIndex == upperThresholdIndex) {
            if(lowerThresholdIndex == -1 || upperThresholdIndex == sourceSize) {
                return 0;
            }
        }
        return Math.min(upperThresholdIndex, sourceSize - 1) - Math.max(lowerThresholdIndex, 0) + 1;
    }

    /** {@inheritDoc} */
    protected int getSourceIndex(int transformationIndex) {
        return transformationIndex + Math.max(lowerThresholdIndex, 0);
    }

    /** {@inheritDoc} */
    public boolean contains(Object object) {
        // Note: this technically breaks the contract for contains.
        // evaluator.evaluate(object) may throw a ClassCastException
        int objectEvaluation = evaluator.evaluate((E) object);
        // Fast fail if the object isn't within the thresholds
        if(objectEvaluation > upperThreshold || objectEvaluation < lowerThreshold) {
            return false;
        }
        return source.contains(object);
    }

    /** {@inheritDoc} */
    public int indexOf(Object object) {
        // Note: this technically breaks the contract for indexOf.
        // evaluator.evaluate(object) may throw a ClassCastException
        int objectEvaluation = evaluator.evaluate((E) object);
        // Fast fail if the object isn't within the thresholds
        if(objectEvaluation > upperThreshold || objectEvaluation < lowerThreshold) {
            return -1;
        }
        return source.indexOf(object);
    }

    /** {@inheritDoc} */
    public int lastIndexOf(Object object) {
        // Note: this technically breaks the contract for lastIndexOf.
        // evaluator.evaluate(object) may throw a ClassCastException
        int objectEvaluation = evaluator.evaluate((E) object);
        // Fast fail if the object isn't within the thresholds
        if(objectEvaluation > upperThreshold || objectEvaluation < lowerThreshold) {
            return -1;
        }
        return source.lastIndexOf(object);
    }

    /** {@inheritDoc} */
    public void dispose() {
        SortedList sortedSource = (SortedList)source;
        super.dispose();
        sortedSource.dispose();
    }

    /**
     * Provide an integer value for a given {@link Object} in a
     * {@link ThresholdList}.
     */
    public interface Evaluator<E> {

        /**
         * Returns an integer value for an {@link Object} to be used to
         * compare that object against a threshold.  This value is
         * not relative to any other object unlike a {@link java.util.Comparator}.
         */
        public int evaluate(E object);
    }


    /**
     * A ThresholdComparator is a simple helper class that wraps
     * an {@link Evaluator} with a <code>Comparator</code> to
     * be used for sorting of the <code>ThresholdList</code>.
     */
    private static final class ThresholdComparator<E> implements Comparator<E> {

        /** the underlying evaluator **/
        private Evaluator<E> evaluator = null;

        /**
         * Creates a new ThresholdComparator
         */
        ThresholdComparator(Evaluator<E> evaluator) {
            this.evaluator = evaluator;
        }

        /**
         * Compares two <code>Object</code>s, and compares them using the result
         * given when each <code>Object</code> is evaluated using the underlying
         * {@link Evaluator}.
         *
         * <p>This method is dual-mode as in the case of the Objects passed being
         * <code>Integer</code>s, it returns the value of
         * <code>((Integer)alpha).intValue() - ((Integer)beta).intValue()</code>.
         * This is necessary so that a threshold value can be compared against an
         * <code>Object</code>, and vice versa.  This can cause problems however
         * if the underlying {@link Evaluator} were to return the negation
         * of an <code>Integer</code>.
         */
        public int compare(E alpha, E beta) {
            int alphaValue = 0;
            if(alpha instanceof Integer) alphaValue = ((Integer)alpha).intValue();
            else alphaValue = evaluator.evaluate(alpha);

            int betaValue = 0;
            if(beta instanceof Integer) betaValue = ((Integer)beta).intValue();
            else betaValue = evaluator.evaluate(beta);

            if(alphaValue > betaValue) return 1;
            else if(alphaValue < betaValue) return -1;
            else return 0;
        }

        /**
         * Returns true iff the object passed is a <code>ThresholdComparator</code> with
         * the same underlying {@link Evaluator}.
         */
        public boolean equals(Object object) {
            if(!(object instanceof ThresholdComparator)) {
                return false;
            }
            ThresholdComparator other = (ThresholdComparator)object;
            return this.evaluator == other.evaluator;
        }
    }
}