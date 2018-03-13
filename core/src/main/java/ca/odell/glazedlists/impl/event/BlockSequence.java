/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.impl.event;

import ca.odell.glazedlists.event.ListEvent;
import ca.odell.glazedlists.impl.adt.IntArrayList;

import java.util.ArrayList;
import java.util.List;

/**
 * Manage a very simple list of list event blocks that occur in
 * increasing-only order.
 *
 * @author <a href="mailto:jesse@swank.ca">Jesse Wilson</a>
 */
public class BlockSequence<E> {

    /** the start indices of the change blocks, inclusive */
    private IntArrayList starts = new IntArrayList();
    /** the end indices of the change blocks, exclusive */
    private IntArrayList ends = new IntArrayList();
    /** the change types */
    private IntArrayList types = new IntArrayList();
    /** the impacted values */
    private List<E> oldValues = new ArrayList<>();
    private List<E> newValues = new ArrayList<>();

    /**
     * @param startIndex the first updated element, inclusive
     * @param endIndex the last index, exclusive
     */
    public boolean update(int startIndex, int endIndex) {
        return addChange(ListEvent.UPDATE, startIndex, endIndex, ListEvent.<E>unknownValue(), ListEvent.<E>unknownValue());
    }

    /**
     * @param startIndex the first inserted element, inclusive
     * @param endIndex the last index, exclusive
     */
    public boolean insert(int startIndex, int endIndex) {
        return addChange(ListEvent.INSERT, startIndex, endIndex, ListEvent.<E>unknownValue(), ListEvent.<E>unknownValue());
    }

    /**
     * @param startIndex the index of the first element to remove
     * @param endIndex the last index, exclusive
     */
    public boolean delete(int startIndex, int endIndex) {
        return addChange(ListEvent.DELETE, startIndex, endIndex, ListEvent.<E>unknownValue(), ListEvent.<E>unknownValue());
    }

    /**
     * Add this change to the list, or return <code>false</code> if that failed
     * because the change is not in increasing order.
     *
     * @return true if the change was successfully applied, or <code>false</code>
     *      if no change was made because this change could not be handled.
     */
    public boolean addChange(int type, int startIndex, int endIndex, E oldValue, E newValue) {
        // remind ourselves of the most recent change
        int lastType;
        int lastStartIndex;
        int lastEndIndex;
        int lastChangedIndex;
        int size = types.size();
        E lastOldValue;
        E lastNewValue;
        if(size == 0) {
            lastType = -1;
            lastStartIndex = -1;
            lastEndIndex = 0;
            lastChangedIndex = 0;
            lastOldValue = ListEvent.<E>unknownValue();
            lastNewValue = ListEvent.<E>unknownValue();
        } else {
            lastType = types.get(size - 1);
            lastStartIndex = starts.get(size - 1);
            lastEndIndex = ends.get(size - 1);
            lastChangedIndex = (lastType == ListEvent.DELETE) ? lastStartIndex : lastEndIndex;
            lastOldValue = (lastType == ListEvent.DELETE) ? oldValues.get(size - 1) : ListEvent.<E>unknownValue();
            lastNewValue = newValues.get(size - 1);
        }

        // this change breaks the linear-ordering requirement, convert
        // to a more powerful list blocks manager
        if(startIndex < lastChangedIndex) {
            return false;

        // concatenate this change on to the previous one
        } else if(lastChangedIndex == startIndex && lastType == type && oldValue == lastOldValue && newValue == lastNewValue) {
            int newLength = (lastEndIndex - lastStartIndex) + (endIndex - startIndex);
            ends.set(size - 1, lastStartIndex + newLength);
            return true;

        // add this change to the end of the list
        } else {
            starts.add(startIndex);
            ends.add(endIndex);
            types.add(type);
            oldValues.add(oldValue);
            newValues.add(newValue);
            return true;
        }
    }

    public boolean isEmpty() {
        return types.isEmpty();
    }

    public void reset() {
        starts.clear();
        ends.clear();
        types.clear();
        oldValues.clear();
        newValues.clear();
    }

    public Iterator iterator() {
        return new Iterator();
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        StringBuffer result = new StringBuffer();
        for(int i = 0; i < types.size(); i++) {
            if(i != 0) {
                result.append(", ");
            }

            // write the type
            int type = types.get(i);
            if(type == ListEvent.INSERT) result.append("+");
            else if(type == ListEvent.UPDATE) result.append("U");
            else if(type == ListEvent.DELETE) result.append("X");

            // write the range
            int start = starts.get(i);
            int end = ends.get(i);
            result.append(start);
            if(end != start) {
                result.append("-");
                result.append(end);
            }
        }

        return result.toString();
    }

    /**
     * Iterate through the list of changes in this sequence.
     */
    public class Iterator {

        private int blockIndex = -1;
        private int offset = 0;

        private int startIndex = -1;
        private int endIndex = -1;
        private int type = -1;

        public Iterator copy() {
            Iterator result = new Iterator();
            result.blockIndex = blockIndex;
            result.offset = offset;
            result.startIndex = startIndex;
            result.endIndex = endIndex;
            result.type = type;
            return result;
        }

        public int getIndex() {
            if(type == ListEvent.INSERT || type == ListEvent.UPDATE) {
                return startIndex + offset;
            } else if(type == ListEvent.DELETE) {
                return startIndex;
            } else {
                throw new IllegalStateException();
            }
        }
        public int getBlockStart() {
            if(startIndex == -1) throw new IllegalStateException("The ListEvent is not currently in a state to return a block start index");
            return startIndex;
        }
        public int getBlockEnd() {
            if(endIndex == -1) throw new IllegalStateException("The ListEvent is not currently in a state to return a block end index");
            return endIndex;
        }
        public int getType() {
            if(type == -1) throw new IllegalStateException("The ListEvent is not currently in a state to return a type");
            return type;
        }
        public E getOldValue() {
            return oldValues.get(blockIndex);
        }
        public E getNewValue() {
            return newValues.get(blockIndex);
        }

        /**
         * Move to the next changed index, possibly within the same block.
         */
        public boolean next() {
            // increment within the block
            if(offset + 1 < endIndex - startIndex) {
                offset++;
                return true;

            // increment to the next block
            } else if(blockIndex + 1 < types.size()) {
                blockIndex++;
                offset = 0;
                startIndex = starts.get(blockIndex);
                endIndex = ends.get(blockIndex);
                type = types.get(blockIndex);
                return true;

            // no more left
            } else {
                return false;
            }
        }

        /**
         * Move to the next changed block.
         */
        public boolean nextBlock() {
            // increment to the next block
            if(blockIndex + 1 < types.size()) {
                blockIndex++;
                offset = 0;
                startIndex = starts.get(blockIndex);
                endIndex = ends.get(blockIndex);
                type = types.get(blockIndex);
                return true;

            // no more left
            } else {
                return false;
            }
        }

        /**
         * @return true if theres another changed index
         */
        public boolean hasNext() {
            // increment within the block
            if(offset + 1 < endIndex - startIndex) return true;

            // increment to the next block
            if(blockIndex + 1 < types.size()) return true;

            // no more left
            return false;
        }

        /**
         * @return true if theres another changed block
         */
        public boolean hasNextBlock() {
            // increment to the next block
            if(blockIndex + 1 < types.size()) return true;

            // no more left
            return false;
        }
    }
}