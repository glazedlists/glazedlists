/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.impl.event;

import ca.odell.glazedlists.event.ListEvent;
import ca.odell.glazedlists.event.ObjectChange;
import ca.odell.glazedlists.impl.adt.IntArrayList;

import java.util.ArrayList;
import java.util.Collections;
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
    /** the change types */
    private IntArrayList types = new IntArrayList();
    /** the impacted values */
    private List<List<ObjectChange<E>>> values = new ArrayList<>();

    /**
     * @param startIndex the first updated element, inclusive
     * @param endIndex the last index, exclusive
     */
    public boolean update(int startIndex, int endIndex) {
        return this.addChange(startIndex, ListEvent.UPDATE, Collections.nCopies(endIndex-startIndex, ObjectChange
                .unknownChange()));
    }

    /**
     * @param startIndex the first inserted element, inclusive
     * @param endIndex the last index, exclusive
     */
    public boolean insert(int startIndex, int endIndex) {
        return this.addChange(startIndex, ListEvent.INSERT, Collections.nCopies(endIndex-startIndex, ObjectChange
                .unknownChange()));
    }

    /**
     * @param startIndex the index of the first element to remove
     * @param endIndex the last index, exclusive
     */
    public boolean delete(int startIndex, int endIndex) {
        return this.addChange(startIndex, ListEvent.DELETE, Collections.nCopies(endIndex-startIndex, ObjectChange
                .unknownChange()));
    }

    /**
     * Add this change to the list, or return <code>false</code> if that failed
     * because the change is not in increasing order.
     *
     * @return true if the change was successfully applied, or <code>false</code>
     *      if no change was made because this change could not be handled.
     * @deprecated  use {@link #addChange(int, int, List)}
     */
    @Deprecated
    public boolean addChange(int type, int startIndex, int endIndex, E oldValue, E newValue) {
        return this.addChange(startIndex, type, Collections.nCopies(endIndex-startIndex, ObjectChange.create(oldValue,
                newValue)));
    }

    public boolean addChange(int startIndex, int changeType, List<ObjectChange<E>> events) {
        // remind ourselves of the most recent change
        int lastType;
        int lastStartIndex;
        int lastEndIndex;
        int lastChangedIndex;
        int size = types.size();
        List<ObjectChange<E>> lastChange;
        if(size == 0) {
            lastType = -1;
            lastStartIndex = -1;
            lastEndIndex = 0;
            lastChangedIndex = 0;
        } else {
            lastType = types.get(size - 1);
            lastStartIndex = starts.get(size - 1);
            lastChange = values.get(size-1);
            lastEndIndex = lastStartIndex + lastChange.size();
            lastChangedIndex = (lastType == ListEvent.DELETE) ? lastStartIndex : lastEndIndex;
        }

        // this change breaks the linear-ordering requirement, convert
        // to a more powerful list blocks manager
        if(startIndex < lastChangedIndex) {
            return false;
            // concatenate this change on to the previous one
        } else if(lastChangedIndex == startIndex && lastType == changeType) {
            try {
                values.get(size - 1).addAll(events);
            }catch(UnsupportedOperationException ex){
                final List<ObjectChange<E>> newList = new ArrayList<>(size + events.size());
                newList.addAll(values.get(size-1));
                newList.addAll(events);
                values.set(size-1, newList);
            }
            return true;
            // add this change to the end of the list
        } else {
            starts.add(startIndex);
            types.add(changeType);
            values.add(events);
            return true;
        }
    }

    public boolean isEmpty() {
        return types.isEmpty();
    }

    public void reset() {
        starts.clear();
        types.clear();
        values.clear();
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
            int end = start + values.get(i).size();
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

        private List<ObjectChange<E>> blockChanges = null;

        public Iterator copy() {
            Iterator result = new Iterator();
            result.blockIndex = blockIndex;
            result.offset = offset;
            result.startIndex = startIndex;
            result.endIndex = endIndex;
            result.type = type;
            result.blockChanges = blockChanges;
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
            if(blockChanges == null) throw new IllegalStateException("The ListEvent is not currently in a state to " +
                    "return a old value");
            return this.blockChanges.get(this.offset).getOldValue();
        }

        public E getNewValue() {
            if(blockChanges == null) throw new IllegalStateException("The ListEvent is not currently in a state to " +
                    "return a new value");
            return this.blockChanges.get(this.offset).getNewValue();
        }

        public List<ObjectChange<E>> getBlockChanges(){
            if(blockChanges == null) throw new IllegalStateException("The ListEvent is not currently in a state to " +
                    "return a new value");
            return this.blockChanges;
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
                blockChanges = Collections.unmodifiableList(values.get(blockIndex));
                endIndex = startIndex + blockChanges.size();
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
                blockChanges = Collections.unmodifiableList(values.get(blockIndex));
                endIndex = startIndex + blockChanges.size();
                type = types.get(blockIndex);
                blockChanges = values.get(blockIndex);
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