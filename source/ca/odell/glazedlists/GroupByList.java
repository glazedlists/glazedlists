/* Glazed Lists                                                 (c) 2003-2005 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import ca.odell.glazedlists.event.ListEvent;

/**
 * A GroupByList is similar in function to a SQL Group By clause. It is meant
 * to summarize groups of elements from a source list with a single summary
 * element. Users of this class are expected to provide an implementation of
 * {@link Grouper} which defines the strategy for both grouping lists of source
 * elements into single objects and ungrouping an object into its original
 * source elements.
 *
 * @author <a href="mailto:jesse@odel.on.ca">Jesse Wilson</a>
 * @author James Lemieux
 */
public class GroupByList<E,S> extends TransformedList<E,S> {

	/** The strategy for grouping source elements into summary elements. */
	private final Grouper<E,S> grouper;

	/**
	 * A list of all unique elements within the source list where uniqueness
	 * is determined by the Comparator returned by the Comparator of the
	 * {@link #grouper}.
	 */
	private final UniqueList<S> uniqueSource;

	/** A list of all groups created by the {@link #grouper}. */
	private final List<E> groups = new ArrayList<E>();

	/**
	 * Create a GroupByList that groups the elements of the given
	 * <code>source</code> according to the strategy specified in the given
	 * <code>grouper</code>.
	 */
	public GroupByList(EventList<S> source, Grouper<E,S> grouper) {
		super(new UniqueList<S>(source, grouper.getComparator()));

		// save a reference to the UniqueList between ourselves and the given source
		this.uniqueSource = (UniqueList<S>) super.source;
		this.grouper = grouper;

		// group all unique source elements
		for (int i = 0; i < this.uniqueSource.size(); i++) {
			final List<S> valuesToGroup = this.uniqueSource.getAll(i);
			final E group = this.grouper.group(valuesToGroup);
			this.groups.add(group);
		}

		// Out of the box, UniqueList doesn't fire events if the
		// number of elements collapsed into a single element changes.
		// This notification is absolutely necessary here since we
		// don't use UniqueList for its intended purpose. Instead, we
		// violently abuse it to perform our grouping functionality for us.
		// In the future, refactoring would be nice to avoid this hackery.
//		this.uniqueSource.setFireCountChangeEvents(true);
		this.source.addListEventListener(this);
	}

	public E get(int index) {
		return this.groups.get(index);
	}

	public E set(int index, E value) {
		// record the object that is about to be replaced
		final E replaced = this.get(index);

		// make the replacement of a group generate a single ListEvent
		updates.beginEvent(true);

		// first remove all values at the given index
		this.uniqueSource.remove(index);

		// reinsert all values being grouped
		this.uniqueSource.addAll(index, this.grouper.ungroup(value));

		updates.commitEvent();

		return replaced;
	}

	protected boolean isWritable() {
		return true;
	}

	public void listChanged(ListEvent<S> listChanges) {
		while (listChanges.next()) {
			final int index = listChanges.getIndex();

			switch(listChanges.getType()) {
				case ListEvent.INSERT:
					this.groups.add(index, this.grouper.group(this.uniqueSource.getAll(index))); break;
				case ListEvent.UPDATE:
					this.groups.set(index, this.grouper.group(this.uniqueSource.getAll(index))); break;
				case ListEvent.DELETE:
					this.groups.remove(index); break;
			}
		}

		listChanges.reset();
		updates.forwardEvent(listChanges);
	}

	/**
	 * The Grouper interface defines a strategy for identifying
	 * <strong>which</strong> elements of a list may be successfully grouped
	 * together into a single summary Object ({@link #getComparator()}). It
	 * also defines a strategy for <strong>how</strong> to {@link #group(List) group}
	 * an identified list of elements into a single summry object as well as
	 * how to {@link #ungroup(Object) ungroup} a summary object into its constituent
	 * List.
	 */
	public interface Grouper<E,S> {
		/**
		 * The {@link Comparator} returned from this method is used to
		 * determine which elements in a {@link List} may be successfully grouped into
		 * a summary object. All elements considered equal by the {@link Comparator}
		 * will be grouped.
		 *
		 * <p>Unlike traditional {@link Comparator}s, this compares objects by
		 * the group they belong to rather than by the value of the object itself.
		 */
		public Comparator<S> getComparator();

		/**
		 * Group a {@link List} of elements considered equal by the {@link Comparator}
         * returned by {@link #getComparator()} into a single summary Object.
		 */
		public E group(List<S> elements);

		/**
		 * Return the {@link List} of elements which form the given <code>group</code>.
		 */
		public List<S> ungroup(E group);
	}
}