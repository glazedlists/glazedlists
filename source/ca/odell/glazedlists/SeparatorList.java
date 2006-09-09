/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists;

import ca.odell.glazedlists.event.ListEvent;
import ca.odell.glazedlists.impl.Grouper;
import ca.odell.glazedlists.impl.adt.Barcode;
import ca.odell.glazedlists.impl.adt.BarcodeIterator;
import ca.odell.glazedlists.impl.adt.barcode2.Element;
import ca.odell.glazedlists.impl.adt.barcode2.SimpleTree;
import ca.odell.glazedlists.impl.adt.barcode2.SimpleTreeIterator;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * A list that adds separator objects before each group of elements.
 *
 * <p><strong>Warning:</strong> this class won't work very well with generics
 * because separators are mixed in, which will be a different class than the
 * other list elements.
 *
 * <p><strong>Developer Preview</strong> this class is still under heavy development
 * and subject to API changes. It's also really slow at the moment and won't scale
 * to lists of size larger than a hundred or so efficiently.
 *
 * @author <a href="mailto:jesse@swank.ca">Jesse Wilson</a>
 */
public class SeparatorList<E> extends TransformedList<E, E> {

    /** delegate to an inner class to insert the separators */
    private SeparatorInjectorList<E> separatorSource;
    private static final Object SEPARATOR = Barcode.BLACK;
    private static final Object SOURCE_ELEMENT = Barcode.WHITE;

    /** how many elements before we get a separator, such as 1 or 2 */
    private int minimumSizeForSeparator;

    /** manage collapsed elements */
    private Barcode collapsedElements;


    /**
     * Create a {@link SeparatorList}...
     */
    public SeparatorList(EventList<E> source, Comparator<E> comparator, int minimumSizeForSeparator, int defaultLimit) {
        super(new SeparatorInjectorList<E>(new SortedList<E>(source, comparator), comparator, defaultLimit));
        this.separatorSource = (SeparatorInjectorList<E>)super.source;
        this.minimumSizeForSeparator = minimumSizeForSeparator;

        // prepare the collapsed elements
        rebuildCollapsedElements();

        // handle changes to the separators list
        this.separatorSource.addListEventListener(this);
    }

    /**
     * Rebuild the entire collapsed elements barcode.
     */
    private void rebuildCollapsedElements() {
        collapsedElements = new Barcode();
        collapsedElements.addBlack(0, separatorSource.size());
        int groupCount = separatorSource.insertedSeparators.colourSize(SEPARATOR);
        for(int i = 0; i < groupCount; i++) {
            updateGroup(i, groupCount, false);
        }
    }

    /** {@inheritDoc} */
    public int size() {
        return collapsedElements.colourSize(Barcode.BLACK);
    }

    /** {@inheritDoc} */
    protected int getSourceIndex(int mutationIndex) {
        return collapsedElements.getIndex(mutationIndex, Barcode.BLACK);
    }

    /**
     * Set the {@link Comparator} used to determine how elements are split
     * into groups.
     *
     * <p>Performance Note: sorting will take <code>O(N * Log N)</code> time.
     *
     * <p><strong><font color="#FF0000">Warning:</font></strong> This method is
     * thread ready but not thread safe. See {@link EventList} for an example
     * of thread safe code.
     */
    public void setComparator(Comparator<E> comparator) {
        // this implementation loses selection, but that's the best we can do
        // with the current limitations of the Glazed Lists ListEventAssembler.
        // What we really need here is the ability to fire an event that contains
        // both reordering and structure change information.
        updates.beginEvent(false);

        // remove all
        updates.addDelete(0, size() - 1);

        // make the change to the sorted source, the grouper will respond but
        // the {@link SeparatorInjectorList} doesn't fire any events forward when
        // its main Comparator is changed
        SortedList<E> sortedList = (SortedList<E>)separatorSource.source;
        sortedList.setComparator(comparator);

        // rebuild which elements are collapsed out
        rebuildCollapsedElements();

        // insert all again
        updates.addInsert(0, size() - 1);
        updates.commitEvent();
    }

    /**
     * Go from the current group (assumed to be black) to the next black group
     * to follow. This works by finding a white follower, then a black follower
     * of that one.
     *
     * @return <code>true</code> if the next group was found, or <code>false</code>
     *      if there was no such group and the iterator is now in an unspecified
     *      location, not necessarily the end of the barcode.
     */
    private static boolean nextBlackGroup(BarcodeIterator iterator) {
        // step to an intermediate white group
        if(!iterator.hasNextWhite()) return false;
        iterator.nextWhite();

        // then to the following black group to get a completely different group
        if(!iterator.hasNextBlack()) return false;
        iterator.nextBlack();

        // success!
        return true;
    }

    /** {@inheritDoc} */
    public void listChanged(ListEvent<E> listChanges) {
        updates.beginEvent(true);

        // when the source changes order, forward a reordering if no elements
        // go from being outside the limit filter to inside it
        if(listChanges.isReordering()) {
            boolean canReorder = true;

            for(SimpleTreeIterator<SeparatorInjectorList<E>.GroupSeparator> i = new SimpleTreeIterator<SeparatorInjectorList<E>.GroupSeparator>(separatorSource.separators); i.hasNext(); ) {
                i.next();
                Element<SeparatorInjectorList<E>.GroupSeparator> node = i.node();
                int limit = node.get().getLimit();
                if(limit == 0) continue;
                if(limit >= separatorSource.size()) continue;
                if(limit >= node.get().size()) continue;
                canReorder = false;
                break;
            }

            // forward the reorder event, this requires a lot of rework because
            // we're mapping backwards and fowards unnecessarily
            if(canReorder) {
                int[] previousIndices = listChanges.getReorderMap();
                int[] reorderMap = new int[collapsedElements.colourSize(Barcode.BLACK)];

                // walk through the unfiltered elements, adjusting the indices
                // for each group of unfiltered (black) elements
                BarcodeIterator i = collapsedElements.iterator();
                int groupStartSourceIndex = 0;
                while(true) {

                    // we already know where this group starts, now we calculate
                    // where it ends, and how many indices it's offset by in the view
                    boolean newGroupFound;
                    int groupEndSourceIndex;
                    int leadingCollapsedElements;
                    if(i.hasNextWhite()) {
                        i.nextWhite();
                        groupEndSourceIndex = i.getIndex();
                        newGroupFound = true;
                        leadingCollapsedElements = i.getWhiteIndex();
                    } else {
                        newGroupFound = false;
                        groupEndSourceIndex = collapsedElements.size();
                        leadingCollapsedElements = collapsedElements.whiteSize();
                    }

                    // update the reorder map for each element in this group
                    for(int j = groupStartSourceIndex; j < groupEndSourceIndex; j++) {
                        reorderMap[j - leadingCollapsedElements] = previousIndices[j] - leadingCollapsedElements;
                    }

                    // prepare the next iteration: find the start of the next group
                    if(newGroupFound && i.hasNextBlack()) {
                        i.nextBlack();
                        groupStartSourceIndex = i.getIndex();
                    } else {
                        break;
                    }
                }
                updates.reorder(reorderMap);

            // fire insert/delete pairs. This loses selection because currently
            // Glazed Lists lacks the ability to fire a mix of move and insert/update
            // events
            } else {
                int size = collapsedElements.colourSize(Barcode.BLACK);
                if(size > 0) {
                    updates.addDelete(0, size - 1);
                    updates.addInsert(0, size - 1);
                }
            }

        // handle other changes by adjusting the limits as necessary
        } else {

            // keep this around, it's handy
            int groupCount = separatorSource.insertedSeparators.colourSize(SEPARATOR);

            // first update the barcode, optimistically
            while(listChanges.next()) {
                int changeIndex = listChanges.getIndex();
                int changeType = listChanges.getType();

                // if we're inserting something new, always fire an insert event,
                // even if we need to revoke it later
                if(changeType == ListEvent.INSERT) {
                    collapsedElements.add(changeIndex, Barcode.BLACK, 1);
                    int viewIndex = collapsedElements.getColourIndex(changeIndex, Barcode.BLACK);
                    updates.addInsert(viewIndex);

                // updates are probably already accurate, don't change the state
                } else if(changeType == ListEvent.UPDATE) {
                    // if its visible, fire an update event
                    if(collapsedElements.get(changeIndex) == Barcode.BLACK) {
                        int viewIndex = collapsedElements.getColourIndex(changeIndex, Barcode.BLACK);
                        updates.addUpdate(viewIndex);
                    }

                // fire a delete event if this is a visible element being deleted
                } else if(changeType == ListEvent.DELETE) {
                    Object oldColor = collapsedElements.get(changeIndex);
                    if(oldColor == Barcode.BLACK) {
                        int viewIndex = collapsedElements.getColourIndex(changeIndex, Barcode.BLACK);
                        updates.addDelete(viewIndex);
                    }
                    collapsedElements.remove(changeIndex, 1);
                }
            }

            // Now make sure our limits are correct, which they may not be
            // due to the fact that we made a lot of guesses in the first pass.
            // Note that this is really slow and needs some work for performance
            // reasons
            listChanges.reset();
            while(listChanges.next()) {
                int changeIndex = listChanges.getIndex();
                int changeType = listChanges.getType();

                if(changeType == ListEvent.INSERT) {
                    int group = separatorSource.insertedSeparators.getColourIndex(changeIndex, true, SEPARATOR);
                    updateGroup(group, groupCount, true);
                } else if(changeType == ListEvent.UPDATE) {
                    int group = separatorSource.insertedSeparators.getColourIndex(changeIndex, true, SEPARATOR);
                    // it's possible that this impacts the previous group!
                    if(group > 0) updateGroup(group - 1, groupCount, true);
                    updateGroup(group, groupCount, true);
                    // it's possible that this impacts the next group
                    if(group < groupCount - 1) updateGroup(group + 1, groupCount, true);

                } else if(changeType == ListEvent.DELETE) {
                    // if there is a group that this came from, update it
                    if(changeIndex < separatorSource.insertedSeparators.size()) {
                        int group = separatorSource.insertedSeparators.getColourIndex(changeIndex, true, SEPARATOR);
                        updateGroup(group, groupCount, true);
                    }
                }
            }
        }

        updates.commitEvent();
    }

    /**
     * Update all elements in the specified group. We need to refine this method
     * since currently it does a linear scan through the group's elements, and
     * that just won't do for performance requirements.
     */
    private void updateGroup(int group, int groupCount, boolean fireEvents) {
        Separator separator = separatorSource.separators.get(group).get();
        int limit = separator.getLimit();

        // fix up this separator
        int separatorStart = separatorSource.insertedSeparators.getIndex(group, SEPARATOR);
        int nextGroup = group + 1;
        int separatorEnd = nextGroup == groupCount ? separatorSource.insertedSeparators.size() : separatorSource.insertedSeparators.getIndex(nextGroup, SEPARATOR);
        int size = separatorEnd - separatorStart - 1;

        // if this is too small to show a separator
        if(size < minimumSizeForSeparator) {
            // remove the separator
            setVisible(separatorStart, Barcode.WHITE, fireEvents);
            // everything else must be visible
            for(int i = separatorStart + 1; i < separatorEnd; i++) {
                setVisible(i, Barcode.BLACK, fireEvents);
            }

        // if this is different than the limit
        } else {
            // show the separator
            setVisible(separatorStart, Barcode.BLACK, fireEvents);
            // show everything up to the limit and nothing after
            for(int i = separatorStart + 1; i < separatorEnd; i++) {
                boolean withinLimit = i - separatorStart <= limit;
                setVisible(i, withinLimit ? Barcode.BLACK : Barcode.WHITE, fireEvents);
            }
        }
    }

    /**
     * Update the visible state of the specified element.
     */
    private void setVisible(int index, Object colour, boolean fireEvents) {
        Object previousColour = collapsedElements.get(index);

        // no change
        if(colour == previousColour) {
            return;

        // hide this element
        } else if(colour == Barcode.WHITE) {
            int viewIndex = collapsedElements.getColourIndex(index, Barcode.BLACK);
            if(fireEvents) updates.addDelete(viewIndex);
            collapsedElements.set(index, Barcode.WHITE, 1);

        // show this element
        } else if(colour == Barcode.BLACK) {
            collapsedElements.set(index, Barcode.BLACK, 1);
            int viewIndex = collapsedElements.getColourIndex(index, Barcode.BLACK);
            if(fireEvents) updates.addInsert(viewIndex);

        } else {
            throw new IllegalArgumentException();
        }
    }

    /**
     * A separator heading the elements of a group.
     */
    public interface Separator<E> {
        /**
         * Get the maximum number of elements in this group to show.
         */
        public int getLimit();

        /**
         * Set the maximum number of elements in this group to show. This is
         * useful to collapse a group (limit of 0), cap the elements of a group
         * (limit of 5) or reverse those actions.
         *
         * <p>This method requires the write lock of the {@link SeparatorList} to be
         * held during invocation.
         */
        public void setLimit(int limit);

        /**
         * Get the {@link List} of all elements in this group.
         *
         * <p>This method requires the read lock of the {@link SeparatorList}
         * to be held during invocation.
         */
        public List<E> getGroup();

        /**
         * A convenience method to get the first element from this group. This
         * is useful to render the separator's name.
         */
        public E first();

        /**
         * A convenience method to get the number of elements in this group. This
         * is useful to render the separator.
         */
        public int size();
    }

    /**
     * This inner class handles the insertion of separators
     * as a separate transformation from the hiding of separators
     * and collapsed elements.
     */
    private static class SeparatorInjectorList<E> extends TransformedList<E, E> {

        /** the grouping service manages finding where to insert groups */
        private final Grouper<E> grouper;

        /**
         * The separators list is black for separators, white for
         * everything else.
         *
         * <p>The following demonstrates the layout of the barcode for the
         * given source list:
         * <pre><code>
         *           INDICES 0         1         2
         *                   012345678901234567890
         *       SOURCE LIST AAAABBBCCCDEFF
         *   GROUPER BARCODE X___X__X__XXX_
         * SEPARATOR BARCODE X____X___X___X_X_X__
         * </pre></code>
         *
         * <p>To read this structure:
         * <li>the grouper barcode is an "X" for the first element in each
         *     group (called uniques), and an "_" for the following
         *     elements (called duplicates).
         * <li>the separator barcode is very similar to the grouper barcode.
         *     In this barcode, there is an "X" for each separator and an "_"
         *     for each element in the source list. We use the structure of the
         *     grouper barcode to derive and maintain the separator barcode.
         *
         * <p>When accessing elements, the separator barcode is queried. If it
         * holds an "X", the element is a separator and that separator is returned.
         * Otherwise if it is an "_", the corresponding source index is obtained
         * (by removing the number of preceding "X" elements) and the element is
         * retrieved from the source list.
         */
        private Barcode insertedSeparators;

        /** a list of {@link Separator}s, one for each separator in the list */
        private SimpleTree<GroupSeparator> separators;

        /** the number of elements to show in each group, such as 0, 5, or {@link Integer.MAX_VALUE} */
        private int defaultLimit;

        /**
         * Create a new {@link UniqueList} that determines groups using the specified
         * {@link Comparator}. Elements that the {@link Comparator} determines are
         * equal will share a common separator.
         *
         * @see GlazedLists#beanPropertyComparator
         */
        public SeparatorInjectorList(SortedList<E> source, Comparator<E> comparator, int defaultLimit) {
            super(source);
            this.defaultLimit = defaultLimit;

            // prepare the groups
            GrouperClient grouperClient = new GrouperClient();
            this.grouper = new Grouper<E>(source, grouperClient);

            // initialize separators state
            rebuildSeparators();

            // handle changes via the grouper
            source.addListEventListener(this);
        }

        /**
         * Statically build the separators data structures.
         */
        private void rebuildSeparators() {
            // clear the initial state of these separators
            insertedSeparators = new Barcode();
            separators = new SimpleTree<GroupSeparator>();

            // prepare the separator list
            insertedSeparators.add(0, SOURCE_ELEMENT, source.size());
            for(BarcodeIterator i = grouper.getBarcode().iterator(); i.hasNextColour(Grouper.UNIQUE); ) {
                i.nextColour(Grouper.UNIQUE);
                int groupIndex = i.getColourIndex(Grouper.UNIQUE);
                int sourceIndex = i.getIndex();
                insertedSeparators.add(groupIndex + sourceIndex, SEPARATOR, 1);
                Element<GroupSeparator> node = separators.add(groupIndex, new GroupSeparator(), 1);
                node.get().setNode(node);
                node.get().setLimit(defaultLimit);
            }
            // update the cached values in all separators
            for(int i = 0; i < separators.size(); i++) {
                separators.get(i).get().updateCachedValues();
            }
        }

        /** {@inheritDoc} */
        public E get(int index) {
            Object type = insertedSeparators.get(index);
            if(type == SEPARATOR) return (E)separators.get(getSeparatorIndex(index)).get();
            else if(type == SOURCE_ELEMENT) return source.get(getSourceIndex(index));
            else throw new IllegalStateException();
        }

        /** {@inheritDoc} */
        protected int getSourceIndex(int mutationIndex) {
            Object type = insertedSeparators.get(mutationIndex);
            if(type == SEPARATOR) return -1;
            else if(type == SOURCE_ELEMENT) return insertedSeparators.getColourIndex(mutationIndex, SOURCE_ELEMENT);
            else throw new IllegalStateException();
        }
        protected int getSeparatorIndex(int mutationIndex) {
            Object type = insertedSeparators.get(mutationIndex);
            if(type == SEPARATOR) return insertedSeparators.getColourIndex(mutationIndex, SEPARATOR);
            else if(type == SOURCE_ELEMENT) return -1;
            else throw new IllegalStateException();
        }


        /** {@inheritDoc} */
        public int size() {
            return insertedSeparators.size();
        }

        /** {@inheritDoc} */
        public void listChanged(ListEvent<E> listChanges) {
            // when the separator comparator is changed in the source list, let
            // the grouper know so we can rebuild our groups, then bail since
            // the {@link SeparatorList} already knows about this event
            SortedList<E> sortedSource = (SortedList<E>) source;
            Comparator<? super E> sourceComparator = sortedSource.getComparator();
            if(sourceComparator != grouper.getComparator()) {
                grouper.setComparator(sourceComparator);
                rebuildSeparators();
                return;
            }

            updates.beginEvent(true);

            // reorderings should be contained within the existing groups, we
            // need to send these reorderings forward
            if(listChanges.isReordering()) {
                int[] previousIndices = listChanges.getReorderMap();
                int[] reorderMap = new int[insertedSeparators.size()];

                // walk through each group, adjusting indices in the forward
                // reorder map to notify listeners
                int groupStartIndex = -1; // inclusive
                int groupEndIndex = 0; // exclusive
                int group = -1;
                for(int i = 0; i < previousIndices.length; i++) {

                    // if this is the start of a new group, add that to the reorder map
                    if(i == groupEndIndex) {
                        group++;
                        reorderMap[i + group] = i + group;
                        groupStartIndex = groupEndIndex;
                        int nextGroup = group + 1;
                        groupEndIndex = nextGroup < separators.size() ? separators.get(nextGroup).get().start() : insertedSeparators.size();
                    }

                    // make sure the move doesn't leave the group
                    int previousIndex = previousIndices[i];
                    if(previousIndex < groupStartIndex || previousIndex >= groupEndIndex) {
                        throw new IllegalStateException();
                    }

                    // adjust this change within the current group
                    reorderMap[i + group + 1] = previousIndex + group + 1;
                }
                updates.reorder(reorderMap);

            // handle regular changes by adjusting the separators via our grouper
            } else {
                grouper.listChanged(listChanges);
            }

            // update the cached values in all separators
            for(int i = 0; i < separators.size(); i++) {
                separators.get(i).get().updateCachedValues();
            }

            updates.commitEvent();
        }

        /**
         * Fire two events, one for the group (the separator) and another for the
         * actual list element.
         */
        private class GrouperClient implements Grouper.Client {
            public void groupChanged(int index, int groupIndex, int groupChangeType, boolean primary, int elementChangeType) {
                boolean fixSeparatorForInsertGroupUpdateElement = false;
                // handle the group change first
                if(groupChangeType == ListEvent.INSERT) {
                    int expandedIndex = index + groupIndex;
                    insertedSeparators.add(expandedIndex, SEPARATOR, 1);
                    updates.addInsert(expandedIndex);
                    // add the separator and link the separator to its node
                    Element<GroupSeparator> node = separators.add(groupIndex, new GroupSeparator(), 1);
                    node.get().setNode(node);
                    node.get().setLimit(defaultLimit);
                } else if(groupChangeType == ListEvent.UPDATE) {
                    int expandedIndex = insertedSeparators.getIndex(groupIndex, SEPARATOR);
                    updates.addUpdate(expandedIndex);
                } else if(groupChangeType == ListEvent.DELETE) {
                    int expandedIndex = insertedSeparators.getIndex(groupIndex, SEPARATOR);
                    insertedSeparators.remove(expandedIndex, 1);
                    updates.addDelete(expandedIndex);
                    // invalidate the node
                    Element<GroupSeparator> node = separators.get(groupIndex);
                    separators.remove(node);
                    node.get().setNode(null);
                    node.get().updateCachedValues();
                    groupIndex--;
                }

                // then handle the element change
                if(elementChangeType == ListEvent.INSERT) {
                    int expandedIndex = index + groupIndex + 1;
                    insertedSeparators.add(expandedIndex, SOURCE_ELEMENT, 1);
                    updates.addInsert(expandedIndex);
                } else if(elementChangeType == ListEvent.UPDATE) {
                    int expandedIndex = index + groupIndex + 1;
                    // if we inserted a separator directly before an existing separator,
                    // we must increase update index for element by one
                    if (groupChangeType == ListEvent.INSERT) {
                        int separatorCount = insertedSeparators.colourSize(SEPARATOR);
                        if (groupIndex + 1 < separatorCount) {
                            // separator at groupIndex is not the last one...
                            int nextSeparatorsIndex = insertedSeparators.getIndex(groupIndex + 1, SEPARATOR);
                            if (nextSeparatorsIndex == expandedIndex) {
                                // separator at groupIndex + 1 is located at the element update position,
                                // so increment update position by one...
                                expandedIndex++;
                                // ...and fix location of separator at groupIndex + 1
                                fixSeparatorForInsertGroupUpdateElement = true;
                            }
                        }
                    }
                    updates.addUpdate(expandedIndex);
                } else if(elementChangeType == ListEvent.DELETE) {
                    int expandedIndex = index + groupIndex + 1;
                    insertedSeparators.remove(expandedIndex, 1);
                    updates.addDelete(expandedIndex);
                }

                if (fixSeparatorForInsertGroupUpdateElement) {
                    // fix special case:
                    // the location of separator at groupIndex + 1 must be increased by one

                    int wrongSeparatorIndex = index + groupIndex + 1;
                    assert wrongSeparatorIndex == insertedSeparators.getIndex(groupIndex + 1, SEPARATOR);

                    insertedSeparators.remove(wrongSeparatorIndex, 1);
                    updates.addDelete(wrongSeparatorIndex);
                    insertedSeparators.add(wrongSeparatorIndex + 1, SEPARATOR, 1);
                    updates.addInsert(wrongSeparatorIndex + 1);
                }

                // Special case out the shift operation. The Grouper automatically
                // handles shifts, but SeparatorList needs to manage them independently.
                // Here we respond to the grouper and the separators barcode getting
                // out of sync, and resolve that problem.
                // If this fix poses a problem, we might want to change the way
                // Grouper works to fire a special flag called 'shift' with the
                // value true whenever the group joined is a RIGHT_GROUP
                int shiftGroupIndex = groupIndex + 1;
                if(groupChangeType == ListEvent.DELETE && elementChangeType != ListEvent.INSERT
                        && shiftGroupIndex < insertedSeparators.colourSize(SEPARATOR)
                        && shiftGroupIndex < grouper.getBarcode().colourSize(Grouper.UNIQUE)) {
                    int collapsedGroupStartIndex = grouper.getBarcode().getIndex(shiftGroupIndex, Grouper.UNIQUE);
                    int separatorsIndex = insertedSeparators.getIndex(shiftGroupIndex , SEPARATOR);
                    //String was = insertedSeparators.toString();
          `          if(collapsedGroupStartIndex + shiftGroupIndex < separatorsIndex) {
                        insertedSeparators.remove(separatorsIndex, 1);
                        updates.addDelete(separatorsIndex);
                        insertedSeparators.add(collapsedGroupStartIndex + shiftGroupIndex, SEPARATOR, 1);
                        updates.addInsert(collapsedGroupStartIndex + shiftGroupIndex);
                        //String now = insertedSeparators.toString();
                        index++;
                        //System.out.println("Changed from " + was + " to " + now);
                    }
                }
            }
        }

        /**
         * Implement the {@link Separator} interface in the most natural way.
         */
        private class GroupSeparator implements Separator<E> {
            private int limit = Integer.MAX_VALUE;
            private int size;
            private E first;

            /**
             * The node allows the separator to figure out which
             * group in the overall list its representing.
             */
            private Element<GroupSeparator> node = null;

            /** {@inheritDoc} */
            public int getLimit() {
                return limit;
            }
            /** {@inheritDoc} */
            public void setLimit(int limit) {
                if(this.limit == limit) return;
                // fail gracefully if the node is null, that means this separator
                // has been removed from the list but its still visible to an editor
                if(node == null) {
                    return;
                }

                this.limit = limit;

                // notify the world of this separator change
                updates.beginEvent();
                int groupIndex = separators.indexOfNode(node, (byte)1);
                int separatorIndex = insertedSeparators.getIndex(groupIndex, SEPARATOR);
                updates.addUpdate(separatorIndex);
                updates.commitEvent();
            }
            /** {@inheritDoc} */
            public List<E> getGroup() {
                if(node == null) return Collections.EMPTY_LIST;
                return source.subList(start(), end());
            }
            /** {@inheritDoc} */
            public E first() {
                return first;
            }
            /** {@inheritDoc} */
            public int size() {
                return size;
            }

            /**
             * Set the {@link IndexedTreeNode} that this {@link Separator} can
             * use to find its index in the overall list of {@link Separator}s;
             */
            public void setNode(Element<GroupSeparator> node) {
                this.node = node;
            }

            /**
             * The first index in the source containing an element from this group.
             */
            private int start() {
                if(this.node == null) throw new IllegalStateException();
                int separatorIndex = separators.indexOfNode(node, (byte)1);
                if(separatorIndex == -1) throw new IllegalStateException();
                int groupStartIndex = insertedSeparators.getIndex(separatorIndex, SEPARATOR);
                return groupStartIndex - separatorIndex;
            }
            /**
             * The last index in the source containing an element from this group.
             */
            private int end() {
                if(this.node == null) throw new IllegalStateException();
                int nextSeparatorIndex = separators.indexOfNode(node, (byte)1) + 1;
                if(nextSeparatorIndex == 0) throw new IllegalStateException();
                int nextGroupStartIndex = nextSeparatorIndex == insertedSeparators.colourSize(SEPARATOR) ? insertedSeparators.size() : insertedSeparators.getIndex(nextSeparatorIndex, SEPARATOR);
                return nextGroupStartIndex - nextSeparatorIndex;
            }

            /**
             * Update the cached {@link #first()} and {@link #size()} values, so that they
             * can be retrieved without the {@link SeparatorList}'s lock.
             */
            public void updateCachedValues() {
                if(node != null) {
                    int start = start();
                    int end = end();
                    this.first = source.get(start);
                    this.size = end - start;
                } else {
                    this.first = null;
                    this.size = 0;
                }
            }

            /** {@inheritDoc} */
            public String toString() {
                return "" + size() + " elements starting with \"" + first() + "\"";
            }
        }
    }
}