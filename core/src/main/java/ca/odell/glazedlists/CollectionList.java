/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists;

import ca.odell.glazedlists.event.ListEvent;
import ca.odell.glazedlists.event.ListEventListener;
import ca.odell.glazedlists.impl.adt.Barcode;
import ca.odell.glazedlists.impl.adt.barcode2.Element;
import ca.odell.glazedlists.impl.adt.barcode2.SimpleTree;
import ca.odell.glazedlists.impl.adt.barcode2.SimpleTreeIterator;

import java.util.Collections;
import java.util.List;

/**
 * A list that acts like a tree in that it contains child elements to nodes
 * contained in another list. An example usage would be to wrap a parent list
 * containing albums and use the CollectionList to display the songs on the
 * album.
 *
 * <p>The actual mapping from the parent list to the child list (album to
 * songs in the above example) is done by a {@link CollectionList.Model} that
 * is provided to the constructor.
 *
 * <p>Note that any {@link EventList}s returned by the {@link CollectionList.Model}
 * must use the same {@link ca.odell.glazedlists.event.ListEventPublisher} and
 * {@link ca.odell.glazedlists.util.concurrent.ReadWriteLock} as this
 * {@link CollectionList}. This is necessary in order for CollectionList to
 * operate correctly under mult-threaded conditions. An
 * {@link IllegalArgumentException} will be raised if this invariant is violated.
 *
 * <p><strong><font color="#FF0000">Warning:</font></strong> This class is
 * thread ready but not thread safe. See {@link EventList} for an example
 * of thread safe code.
 *
 * <p><table border="1" width="100%" cellpadding="3" cellspacing="0">
 * <tr class="TableHeadingColor"><td colspan=2><font size="+2"><b>EventList Overview</b></font></td></tr>
 * <tr><td class="TableSubHeadingColor"><b>Writable:</b></td><td>only {@link #set(int,Object)} and {@link #remove(int)}</td></tr>
 * <tr><td class="TableSubHeadingColor"><b>Concurrency:</b></td><td>thread ready, not thread safe</td></tr>
 * <tr><td class="TableSubHeadingColor"><b>Performance:</b></td><td>reads: O(log N), writes O(log N)</td></tr>
 * <tr><td class="TableSubHeadingColor"><b>Memory:</b></td><td>96 bytes per element</td></tr>
 * <tr><td class="TableSubHeadingColor"><b>Unit Tests:</b></td><td>N/A</td></tr>
 * <tr><td class="TableSubHeadingColor"><b>Issues:</b></td><td>
 *   <a href="https://glazedlists.dev.java.net/issues/show_bug.cgi?id=96">96</a>
 *   <a href="https://glazedlists.dev.java.net/issues/show_bug.cgi?id=162">162</a>
 *   <a href="https://glazedlists.dev.java.net/issues/show_bug.cgi?id=257">257</a>
 *   <a href="https://glazedlists.dev.java.net/issues/show_bug.cgi?id=265">265</a>
 * </td></tr>
 * </table>
 *
 * @see CollectionList.Model
 *
 * @author <a href="mailto:rob@starlight-systems.com">Rob Eden</a>
 * @author <a href="mailto:jesse@swank.ca">Jesse Wilson</a>
 */
public class CollectionList<S, E> extends TransformedList<S, E> implements ListEventListener<S> {

    /** This is a hack - we need a temporary value when inserting into IndexedTrees, and this is the one we use. */
    private final ChildElement<E> EMPTY_CHILD_ELEMENT = new SimpleChildElement(Collections.<E>emptyList(), null);

    /** used to extract children */
    private final Model<S, E> model;

    /**
     * Barcode containing the node mappings. There is a black node for each parent
     * followed by a white node for each of its children.
     */
    private final Barcode barcode = new Barcode();

    /** the Lists and EventLists that this is composed of */
    private final SimpleTree<ChildElement<E>> childElements = new SimpleTree<ChildElement<E>>();

    /**
     * Create a {@link CollectionList} with its contents being the children of
     * the elements in the specified source {@link EventList}.
     *
     * @param source an EventList of Objects from each of which the
     *      <code>model</code> can extract child elements
     * @param model the logic for extracting children from each
     *      <code>source</code> element
     */
    public CollectionList(EventList<S> source, Model<S, E> model) {
        super(source);
        if(model == null) throw new IllegalArgumentException("model cannot be null");

        this.model = model;

        // sync the current size and indexes
        for(int i = 0, n = source.size(); i < n; i++) {
            List<E> children = model.getChildren(source.get(i));

            // update the list of child lists
            Element<ChildElement<E>> node = childElements.add(i, EMPTY_CHILD_ELEMENT, 1);
            node.set(createChildElementForList(children, node));

            // update the barcode
            barcode.addBlack(barcode.size(), 1);
            if(!children.isEmpty()) barcode.addWhite(barcode.size(), children.size());
        }

        // listen for changes
        source.addListEventListener(this);
    }

    /**
     * @return <tt>false</tt> because we cannot support {@link #add(int, Object)};
     *      though we do support {@link #set(int, Object)} and {@link #remove(int)}
     */
    @Override
    protected boolean isWritable() {
        return false;
    }

    /** {@inheritDoc} */
    @Override
    public int size() {
        // size of the child nodes only
        return barcode.whiteSize();
    }

    /** {@inheritDoc} */
    @Override
    public E get(int index) {
        // get the child
        final ChildElement<E> childElement = getChildElement(index);
        final int childIndexInParent = barcode.getWhiteSequenceIndex(index);
        return childElement.get(childIndexInParent);
    }

    /** {@inheritDoc} */
    @Override
    public E set(int index, E value) {
        // set on the child
        final ChildElement<E> childElement = getChildElement(index);
        final int childIndexInParent = barcode.getWhiteSequenceIndex(index);
        return childElement.set(childIndexInParent, value);
    }

    /** {@inheritDoc} */
    @Override
    public E remove(int index) {
        // remove from the child
        final ChildElement<E> childElement = getChildElement(index);
        final int childIndexInParent = barcode.getWhiteSequenceIndex(index);
        return childElement.remove(childIndexInParent);
    }

    /**
     * Return the index of the first child in the CollectionList for the given
     * parent index. This can be very useful for things like selecting the
     * children in a CollectionList when the parent is selected in another list.
     *
     * @see #childEndingIndex
     */
    public int childStartingIndex(int parentIndex) {
        if(parentIndex < 0) throw new IndexOutOfBoundsException("Invalid index: " + parentIndex);
        if(parentIndex >= source.size()) throw new IndexOutOfBoundsException("Invalid index: " + parentIndex);

        // get the index of the next node
        // find the index of the black node with that index
        final int parentFullIndex = barcode.getIndex(parentIndex, Barcode.BLACK);
        final int childFullIndex = parentFullIndex + 1;

        // ff this node has no children, the next node index will be past the size or black
        if(childFullIndex >= barcode.size()) return -1;
        if(barcode.get(childFullIndex) != Barcode.WHITE) return -1;

        // return the child index
        final int childIndex = childFullIndex - (parentIndex+1);
        assert(barcode.getWhiteIndex(childFullIndex) == childIndex);
        return childIndex;
    }

    /**
     * Return the index of the last child in the CollectionList for the given
     * parent index. This can be very useful for things like selecting the
     * children in a CollectionList when the parent is selected in another list.
     *
     * @see #childStartingIndex
     */
    public int childEndingIndex(int parentIndex) {
        if(parentIndex < 0) throw new IndexOutOfBoundsException("Invalid index: " + parentIndex);
        if(parentIndex >= source.size()) throw new IndexOutOfBoundsException("Invalid index: " + parentIndex);

        // Get the index of the next node
        // Find the index of the black node with that index
        final int nextParentFullIndex = (parentIndex == barcode.blackSize() - 1) ? barcode.size() : barcode.getIndex(parentIndex + 1, Barcode.BLACK);
        final int lastWhiteBeforeNextParent = nextParentFullIndex - 1;

        // If this node has no children, the next node index will be past the size or black
        if(barcode.get(lastWhiteBeforeNextParent) == Barcode.BLACK) return -1;

        // return the child index
        final int childIndex = lastWhiteBeforeNextParent - (parentIndex+1);
        assert(barcode.getWhiteIndex(lastWhiteBeforeNextParent) == childIndex);
        return childIndex;
    }

    /**
     * Handle changes in the parent list. We'll need to update our node list
     * sizes.
     */
    @Override
    public void listChanged(ListEvent<S> listChanges) {
        // need to process the changes so that our size caches are up to date.
        updates.beginEvent();
        while(listChanges.next()) {
            int index = listChanges.getIndex();
            int type = listChanges.getType();

            // insert means we'll need to insert a new node in the array
            if(type == ListEvent.INSERT) {
                handleInsert(index);

            } else if(type == ListEvent.DELETE) {
                handleDelete(index);

            // treat like a delete and then an add:
            } else if(type == ListEvent.UPDATE) {
                handleDelete(index);
                handleInsert(index);
            }
        }
        updates.commitEvent();
    }

    /** @inheritDoc */
    @Override
    public void dispose() {
        super.dispose();

        // iterate over all child elements and dispose them
        final SimpleTreeIterator<ChildElement<E>> treeIterator = new SimpleTreeIterator<ChildElement<E>>(childElements);

        while(treeIterator.hasNext()) {
            treeIterator.next();
            treeIterator.value().dispose();
        }
    }

    /**
     * Helper for {@link #listChanged(ListEvent)} when inserting.
     */
    private void handleInsert(int parentIndex) {
        // find the index of the black node with that index
        int absoluteIndex = getAbsoluteIndex(parentIndex);

        // find the size of the new node and add it to the total
        S parent = source.get(parentIndex);
        List<E> children = model.getChildren(parent);

        // update the list of child lists
        Element<ChildElement<E>> node = childElements.add(parentIndex, EMPTY_CHILD_ELEMENT, 1);
        node.set(createChildElementForList(children, node));

        // update the barcode
        barcode.addBlack(absoluteIndex, 1);
        if(!children.isEmpty()) barcode.addWhite(absoluteIndex + 1, children.size());

        // add events
        int childIndex = absoluteIndex - parentIndex;
        for(int i = 0; i < children.size(); i++) {
            E element = children.get(i);
            updates.elementInserted(childIndex, element);
        }
    }

    /**
     * Helper for {@link #listChanged(ListEvent)} when deleting.
     */
    private void handleDelete(int sourceIndex) {
        // find the index of the black node with that index
        final int parentIndex = getAbsoluteIndex(sourceIndex);
        final int nextParentIndex = getAbsoluteIndex(sourceIndex + 1);
        final int childCount = nextParentIndex - parentIndex - 1; // subtract one for the parent

        // record the delete events first while the deleted values still exist
        if(childCount > 0) {
            int firstDeletedChildIndex = parentIndex - sourceIndex;
            int firstNotDeletedChildIndex = firstDeletedChildIndex + childCount;

            for (int i = firstDeletedChildIndex; i < firstNotDeletedChildIndex; i++) {
                updates.elementDeleted(firstDeletedChildIndex, get(i));
            }
        }

        // update the list of child lists
        Element<ChildElement<E>> removedChildElement = childElements.get(sourceIndex);
        childElements.remove(removedChildElement);
        removedChildElement.get().dispose();

        // update the barcode
        barcode.remove(parentIndex, 1 + childCount); // delete the parent and all children
    }

    /**
     * Get the child element for the specified child index.
     */
    private ChildElement<E> getChildElement(int childIndex) {
        if(childIndex < 0) throw new IndexOutOfBoundsException("Invalid index: " + childIndex);
        if(childIndex >= size()) throw new IndexOutOfBoundsException("Index: " + childIndex + ", Size: " + size());

        // get the child element
        int parentIndex = barcode.getBlackBeforeWhite(childIndex);
        return childElements.get(parentIndex).get();
    }

    /**
     * Create a {@link ChildElement} for the specified List.
     */
    private ChildElement<E> createChildElementForList(List<E> children, Element<ChildElement<E>> node) {
        if(children instanceof EventList)
            return new EventChildElement((EventList<E>) children, node);
        else
            return new SimpleChildElement(children, node);
    }

    /**
     * Get the absolute index for the specified parent index. This may be
     * virtual if the parent index is one greater than the last element. This
     * is useful for calculating the size of a range by using the location of
     * its follower.
     */
    private int getAbsoluteIndex(int parentIndex) {
        if(parentIndex < barcode.blackSize())
            return barcode.getIndex(parentIndex, Barcode.BLACK);

        if(parentIndex == barcode.blackSize())
            return barcode.size();

        throw new IndexOutOfBoundsException();
    }

    /**
     * Models a list held by the CollectionList.
     */
    private interface ChildElement<E> {
        public E get(int index);
        public E remove(int index);
        public E set(int index, E element);
        public void dispose();
    }

    /**
     * Provides the logic to map a parent object (e.g. an album) to its
     * children (e.g. the songs on the album). Serves basically the same
     * purpose as {@link javax.swing.tree.TreeModel} does to a JTree in Swing.
     *
     * @see CollectionList
     * @see GlazedLists#listCollectionListModel()
     */
    @FunctionalInterface
    public interface Model<E,S> {

        /**
         * Return a list of the child nodes for a parent node.
         *
         * @param parent the parent node.
         * @return a List containing the child nodes.
         */
        public List<S> getChildren(E parent);
    }

    /**
     * Manages a standard List that does not implement {@link EventList}.
     */
    private class SimpleChildElement implements ChildElement<E> {
        private final List<E> children;
        private final Element<ChildElement<E>> node;

        public SimpleChildElement(List<E> children, Element<ChildElement<E>> node) {
            this.children = children;
            this.node = node;
        }

        @Override
        public E get(int index) {
            return children.get(index);
        }

        @Override
        public E remove(int index) {
            E removed = children.remove(index);

            // update the barcode
            int parentIndex = childElements.indexOfNode(node, (byte)0);
            int absoluteIndex = getAbsoluteIndex(parentIndex);
            int firstChildIndex = absoluteIndex + 1;
            barcode.remove(firstChildIndex + index, 1);

            // forward the offset event
            int childOffset = absoluteIndex - parentIndex;
            updates.beginEvent();
            updates.elementDeleted(index + childOffset, removed);
            updates.commitEvent();

            // all done
            return removed;
        }

        @Override
        public E set(int index, E element) {
            E replaced = children.set(index, element);

            // forward the offset event
            int parentIndex = childElements.indexOfNode(node, (byte)0);
            int absoluteIndex = getAbsoluteIndex(parentIndex);
            int childOffset = absoluteIndex - parentIndex;
            updates.beginEvent();
            updates.elementUpdated(index + childOffset, replaced);
            updates.commitEvent();

            // all done
            return replaced;
        }

        @Override
        public void dispose() {
            // do nothing
        }
    }

    /**
     * Monitors changes to a member EventList and forwards changes to all
     * listeners of the CollectionList.
     */
    private class EventChildElement implements ChildElement<E>, ListEventListener<E> {
        private final EventList<E> children;
        private final Element<ChildElement<E>> node;

        public EventChildElement(EventList<E> children, Element<ChildElement<E>> node) {
            this.children = children;
            this.node = node;

            // ensure the EventList of children uses the same publisher as the CollectionList
            if(!getPublisher().equals(children.getPublisher()))
                throw new IllegalArgumentException("If a CollectionList.Model returns EventLists, those EventLists must use the same ListEventPublisher as the CollectionList");

            // ensure the EventList of children uses the same locks as the CollectionList
            if(!getReadWriteLock().equals(children.getReadWriteLock()))
                throw new IllegalArgumentException("If a CollectionList.Model returns EventLists, those EventLists must use the same ReadWriteLock as the CollectionList");

            children.getPublisher().setRelatedSubject(this, CollectionList.this);
            children.addListEventListener(this);
        }

        @Override
        public E get(int index) {
            return children.get(index);
        }

        @Override
        public E remove(int index) {
            // events will be fired from this call
            return children.remove(index);
        }

        @Override
        public E set(int index, E element) {
            // events will be fired from this call
            return children.set(index, element);
        }

        @Override
        public void listChanged(ListEvent<E> listChanges) {
            int parentIndex = childElements.indexOfNode(node, (byte)1);
            int absoluteIndex = getAbsoluteIndex(parentIndex);
            int nextNodeIndex = getAbsoluteIndex(parentIndex+1);

            // update the barcode
            int firstChildIndex = absoluteIndex + 1;
            int previousChildrenCount = nextNodeIndex - firstChildIndex;
            if(previousChildrenCount > 0) barcode.remove(firstChildIndex, previousChildrenCount);
            if(!children.isEmpty()) barcode.addWhite(firstChildIndex, children.size());

            // get the offset of this child list
            int childOffset = absoluteIndex - parentIndex;

            // forward the offset event
            updates.beginEvent();
            while(listChanges.next()) {
                int index = listChanges.getIndex();
                int type = listChanges.getType();

                int overallIndex = index + childOffset;
                switch (type) {
                    case ListEvent.INSERT: updates.elementInserted(overallIndex, listChanges.getNewValue()); break;
                    case ListEvent.UPDATE: updates.elementUpdated(overallIndex, listChanges.getOldValue(), listChanges.getNewValue()); break;
                    case ListEvent.DELETE: updates.elementDeleted(overallIndex, listChanges.getOldValue()); break;
                }
            }
            updates.commitEvent();
        }

        @Override
        public void dispose() {
            children.removeListEventListener(this);
            children.getPublisher().clearRelatedSubject(this);
        }

        @Override
        public String toString() {
            return "[" + childElements.indexOfNode(node, (byte)0) + ":" + children + "]";
        }
    }
}