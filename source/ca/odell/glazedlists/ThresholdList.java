/**
 * Glazed Lists
 * http://glazedlists.dev.java.net/
 *
 * COPYRIGHT 2003 O'DELL ENGINEERING LTD.
 */
package ca.odell.glazedlists;

// the core Glazed Lists package
import ca.odell.glazedlists.event.*;
// to use standard collections
import java.util.*;

/**
 * An {@link EventList} that shows a range of the elements of the source
 * {@link EventList}. Each element in the source {@link EventList} is assigned
 * an integer value via a {@link ThresholdEvaluator}. This integer is used
 * to determine whether the element fits in the {@link ThresholdList}s range.
 *
 * <p>By modifying the upper and lower thresholds in the range, the list can
 * be filtered in a simple and powerful way.
 *
 * <p>The {@link ThresholdList} lends itself to use with a slider widget for
 * manipulating one of the range's endpoints.
 *
 * <p>One use case for {@link ThresholdList} is in a media player application.
 * By creating a {@link ThresholdEvaluator} for a song's bitrate, the user could
 * limit results to MP3 files between 192 and 320kbps.
 *
 * <p>Note that the elements in the {@link ThresholdList} will be presented in
 * order sorted by their {@link ThresholdEvaluator} value.
 *
 * <p>This {@link EventList} supports all write operations.
 *
 * <p><strong><font color="#FF0000">Warning:</font></strong> This class is
 * thread ready but not thread safe. See {@link EventList} for an example
 * of thread safe code.
 *
 * <p><strong><font color="#FF0000">Warning:</font></strong> This class
 * breaks the contract required by {@link java.util.List}. See {@link EventList}
 * for an example.
 *
 * @author <a href="mailto:kevin@swank.ca">Kevin Maltby</a>
 */
public final class ThresholdList extends TransformedList {

    /** the index in the list which corresponds to the lower bound for this list */
    private int lowerThresholdIndex = -1;

    /** the index in the list which corresponds to the upper bound for this list */
    private int upperThresholdIndex = -1;

    /** the lower bound to use to define list containment */
    private int lowerThreshold = 0;

    /** the upper bound to use to define list containment */
    private int upperThreshold = 0;

    /** a local cache of the size of the source list to improve performance */
    private int sourceSize = 0;

    /** the evaluator to use to compare Objects against the threshold */
    private ThresholdEvaluator evaluator = null;

    /**
     * Creates a {@link ThresholdList} that provides range-filtering based on the
     * specified {@link EventList} based on the specified integer JavaBean property.
     */
    public ThresholdList(EventList source, String propertyName) {
        this(source, GlazedLists.thresholdEvaluator(propertyName));
    }

    /**
     * Creates a {@link ThresholdList} that provides range-filtering on the
     * specified {@link EventList} using the specified {@link ThresholdEvaluator}.
     */
    public ThresholdList(EventList source, ThresholdEvaluator evaluator) {
        super(new SortedList(source, new ThresholdComparator(evaluator)));
        this.source.addListEventListener(this);
        this.evaluator = evaluator;
        sourceSize = source.size();

        // Include all possible elements by default
        setUpperThreshold(Integer.MAX_VALUE);
        setLowerThreshold(Integer.MIN_VALUE);
    }

    /** {@inheritDoc} */
    public void listChanged(ListEvent listChanges) {

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
                    } else if(sortedIndex == lowerThresholdIndex) {
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
            lowerThresholdIndex = -1;
            return;

        // Threshold is unchanged
        } else if(threshold == lowerThreshold) {
            return;

        // Threshold is changed
        } else {
            newListIndex = ((SortedList)source).indexOfSimulated(new Integer(threshold));
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
            updates.addInsert(0, Math.max(lowerThresholdIndex, 0) - newListIndex - 1);

        // The threshold is higher
        } else if(newListIndex > lowerThresholdIndex) {
            updates.addDelete(0, newListIndex - Math.max(lowerThresholdIndex, 0) - 1);
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
        if((newListIndex == size() && upperThresholdIndex == size() - 1) ||
           (newListIndex == size() - 1 && upperThresholdIndex == size())) {
            upperThresholdIndex = newListIndex;
            return;
        }

        // Changes are necessary so prepare an event
        updates.beginEvent();

        // The threshold is lower
        if(newListIndex < upperThresholdIndex) {
            updates.addDelete(newListIndex + 1, upperThresholdIndex - Math.max(lowerThresholdIndex, 0));

        // The threshold is higher
        } else if(newListIndex > upperThresholdIndex) {
            updates.addInsert(upperThresholdIndex - Math.max(lowerThresholdIndex, 0) + 1, newListIndex);
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
        // Fast fail if the object isn't within the thresholds
        int objectEvaluation = evaluator.evaluate(object);
        if(objectEvaluation > upperThreshold || objectEvaluation < lowerThreshold) {
            return false;
        }
        return source.contains(object);
    }

    /** {@inheritDoc} */
    public int indexOf(Object object) {
        // Fast fail if the object isn't within the thresholds
        int objectEvaluation = evaluator.evaluate(object);
        if(objectEvaluation > upperThreshold || objectEvaluation < lowerThreshold) {
            return -1;
        }
        return source.indexOf(object);
    }

    /** {@inheritDoc} */
    public int lastIndexOf(Object object) {
        // Fast fail if the object isn't within the thresholds
        int objectEvaluation = evaluator.evaluate(object);
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
     * A ThresholdComparator is a simple helper class that wraps
     * a <code>ThresholdEvaluator</code> with a <code>Comparator</code> to
     * be used for sorting of the <code>ThresholdList</code>.
     */
    private static final class ThresholdComparator implements Comparator {

        /** the underlying evaluator **/
        private ThresholdEvaluator evaluator = null;

        /**
         * Creates a new ThresholdComparator
         */
        ThresholdComparator(ThresholdEvaluator evaluator) {
            this.evaluator = evaluator;
        }

        /**
         * Compares two <code>Object</code>s, and compares them using the result
         * given when each <code>Object</code> is evaluated using the underlying
         * <code>ThresholdEvaluator</code>.
         *
         * <p>This method is dual-mode as in the case of the Objects passed being
         * <code>Integer</code>s, it returns the value of
         * <code>((Integer)alpha).intValue() - ((Integer)beta).intValue()</code>.
         * This is necessary so that a threshold value can be compared against an
         * <code>Object</code>, and vice versa.  This can cause problems however
         * if the underlying <code>ThresholdEvaluator</code> were to return the negation
         * of an <code>Integer</code>.
         */
        public int compare(Object alpha, Object beta) {
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
         * the same underlying <code>ThresholdEvaluator</code>.
         */
        public boolean equals(Object object) {
            if(object == null || !(object instanceof ThresholdComparator)) {
            return false;
            }
            ThresholdComparator other = (ThresholdComparator)object;
            return this.evaluator == other.evaluator;
        }
    }
}
