/**
 * Glazed Lists
 * http://glazedlists.dev.java.net/
 *
 * COPYRIGHT 2003 O'DELL ENGINEERING LTD.
 */
package ca.odell.glazedlists;

// the core Glazed Lists package
import ca.odell.glazedlists.event.*;
// need access to volatile implementation classes
import ca.odell.glazedlists.util.impl.*;
// to use standard collections
import java.util.*;

/**
 * A ThresholdList is a transformation list that provides a dynamic
 * view of a range of values in a list.  This range is defined to
 * contain all elements at or above the lower threshold and all
 * all elements at or below the upper threshold.  This allows a user
 * to filter data from their list in a powerful and sophisticated manner.
 *
 * <p>This list is designed to respond to a slider widget or combo box
 * that sets either one or both of the thresholds on a range of values.
 * This allows a user to easily refine their view of a list to contain
 * only values in a valid range.  The perfect use case for this list is
 * within a media player or P2P application. A user might want to filter
 * out all songs that aren't at least at a bitrate of 192 kbps and at most
 * a bitrate of 320 kbps to eliminate low quality MP3s and WAV files.
 *
 * <p><strong>Warning:</strong> this is a technology preview and is
 * subject to API changes. 
 *
 * @author <a href="mailto:kevin@swank.ca">Kevin Maltby</a>
 */
public final class ThresholdList extends TransformedList implements ListEventListener {

    /** the index in the sorted list which corresponds to the lower bound for this list */
    private int lowerThresholdIndex = -1;

    /** the index in the sorted list which corresponds to the upper bound for this list */
    private int upperThresholdIndex = -1;

    /** the lower bound to use to define list containment */
    private int lowerThreshold = 0;

    /** the upper bound to use to define list containment */
    private int upperThreshold = 0;

    /** a local cache of the size of the sorted list to improve performance */
    private int sourceSize = 0;

    /** the evaluator to use to compare Objects against the threshold */
    private ThresholdEvaluator evaluator = null;

    /** the explicitly sorted sorted list */
    private SortedList sorted = null;

    public boolean debug = false;

    /**
     * Creates a new ThresholdList that is range-filtered view
     * of a specified list.
     */
    public ThresholdList(EventList source, ThresholdEvaluator evaluator) {
        super(new SortedList(source, new ThresholdComparator(evaluator)));
        this.sorted = (SortedList)super.source;
        this.sorted.addListEventListener(this);
        this.evaluator = evaluator;
        sourceSize = sorted.size();

        // Include all possible elements by default
        setUpperThreshold(Integer.MAX_VALUE);
        setLowerThreshold(Integer.MIN_VALUE);
    }

    /**
     * For implementing the ListEventListener interface. When the underlying list
     * changes, this sends notification to listening lists.
     */
    public void listChanged(ListEvent listChanges) {

        // recache the sorted size
        sourceSize = sorted.size();

        // Make all of the changes happen atomically
        updates.beginEvent();

        while(listChanges.next()) {
            int sortedIndex = listChanges.getIndex();
            int type = listChanges.getType();

            int value = Integer.MIN_VALUE;

            if(type != ListEvent.DELETE && sourceSize > 0) {
                value = evaluator.evaluate(sorted.get(sortedIndex));
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
                    if(value <= upperThreshold) {
                        upperThresholdIndex ++;
                        updates.addInsert(transformedIndex);
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
     * a UI component.  <strong>Calling this method directly while this list
     * is connected to a particular widget could result in errors.</strong>
     */
    public void setLowerThreshold(int threshold) {
        getReadWriteLock().writeLock().lock();
        try {
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
                newListIndex = sorted.indexOfSimulated(new Integer(threshold));
                // return -1 if the value is before the list
                if(newListIndex == 0) newListIndex = sorted.indexOf(new Integer(threshold));
            }

            // update the threshold
            lowerThreshold = threshold;

            // the index at the threshold has not changed
            if(newListIndex == lowerThresholdIndex) {
                return;
            }

            // the index at the threshold has changed but no event should be thrown
            if((newListIndex == -1 && lowerThresholdIndex == 0) ||
                (newListIndex == 0 && lowerThresholdIndex == -1) ||
                (newListIndex == size() && lowerThresholdIndex == size() - 1) ||
                (newListIndex == size() - 1 && lowerThresholdIndex == size())) {
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
        } finally {
            getReadWriteLock().writeLock().unlock();
        }
    }

    /**
     * Gets the lower threshold for this list
     */
    public int getLowerThreshold() {
        return lowerThreshold;
    }

    /**
     * Sets the upper threshold for this list.
     */
    public void setUpperThreshold(int threshold) {
        getReadWriteLock().writeLock().lock();
        try {

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
                newListIndex = sorted.indexOfSimulated(new Integer(threshold+1)) - 1;
            }

            // update the threshold
            upperThreshold = threshold;

            // the index at the threshold has not changed
            if(newListIndex == upperThresholdIndex) {
                return;
            }

            // the index at the threshold has changed but no event should be thrown
            if((newListIndex == -1 && upperThresholdIndex == 0) ||
                (newListIndex == 0 && upperThresholdIndex == -1) ||
                (newListIndex == size() && upperThresholdIndex == size() - 1) ||
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
        } finally {
            getReadWriteLock().writeLock().unlock();
        }
    }

    /**
     * Gets the upper threshold for this list
     */
    public int getUpperThreshold() {
        return upperThreshold;
    }

    /**
     * Returns the number of elements in this list.
     *
     * <p>This method is not thread-safe and callers should ensure they have thread-
     * safe access via <code>getReadWriteLock().readLock()</code>.
     */
    public int size() {
        // Check for the exclusionary edge condition
        if(lowerThresholdIndex == upperThresholdIndex) {
            if(lowerThresholdIndex == -1 || upperThresholdIndex == sourceSize) {
                return 0;
            }
        }
        return Math.min(upperThresholdIndex, sourceSize - 1) - Math.max(lowerThresholdIndex, 0) + 1;
    }

    /**
     * Gets the index into the sorted list for the object with the specified
     * index in this list. This is the index such that the following works:
     * <br><code>this.get(i) == sorted.get(getSourceIndex(i))</code> for all
     * values.
     */
    protected int getSourceIndex(int transformationIndex) {
        return transformationIndex + Math.max(lowerThresholdIndex, 0);
    }

    /**
     * Returns true if this list contains the specified element.
     *
     * <p>Like all read-only methods, this method <strong>does not</strong> manage
     * its own thread safety. Callers can obtain thread safe access to this method
     * via <code>getReadWriteLock().readLock()</code>.
     */
    public boolean contains(Object object) {
        // Fast fail if the object isn't within the thresholds
        int objectEvaluation = evaluator.evaluate(object);
        if(objectEvaluation > upperThreshold || objectEvaluation < lowerThreshold) {
            return false;
        }
        return sorted.contains(object);
    }

    /**
     * Returns the index in this list of the first occurrence of the specified
     * element, or -1 if this list does not contain this element.
     *
     * <p>Like all read-only methods, this method <strong>does not</strong> manage
     * its own thread safety. Callers can obtain thread safe access to this method
     * via <code>getReadWriteLock().readLock()</code>.
     */
    public int indexOf(Object object) {
        // Fast fail if the object isn't within the thresholds
        int objectEvaluation = evaluator.evaluate(object);
        if(objectEvaluation > upperThreshold || objectEvaluation < lowerThreshold) {
            return -1;
        }
        return sorted.indexOf(object);
    }

    /**
     * Returns the index in this list of the last occurrence of the specified
     * element, or -1 if this list does not contain this element.
     *
     * <p>Like all read-only methods, this method <strong>does not</strong> manage
     * its own thread safety. Callers can obtain thread safe access to this method
     * via <code>getReadWriteLock().readLock()</code>.
     */
    public int lastIndexOf(Object object) {
        // Fast fail if the object isn't within the thresholds
        int objectEvaluation = evaluator.evaluate(object);
        if(objectEvaluation > upperThreshold || objectEvaluation < lowerThreshold) {
            return -1;
        }
        return sorted.lastIndexOf(object);
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
            return alphaValue - betaValue;
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
