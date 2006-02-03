/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists;

import java.util.*;
// for weak reference proxying
import ca.odell.glazedlists.event.ListEventListener;
// for access to volatile classes
import ca.odell.glazedlists.impl.*;
import ca.odell.glazedlists.impl.filter.StringTextFilterator;
import ca.odell.glazedlists.impl.sort.*;
import ca.odell.glazedlists.impl.beans.*;
import ca.odell.glazedlists.impl.matchers.*;
// implemented interfaces
import ca.odell.glazedlists.gui.*;
import ca.odell.glazedlists.matchers.*;
import java.util.Comparator;

/**
 * A factory for creating all sorts of objects to be used with Glazed Lists.
 *
 * @author <a href="mailto:jesse@odel.on.ca">Jesse Wilson</a>
 */
public final class GlazedLists {

    /**
     * A dummy constructor to prevent instantiation of this class
     */
    private GlazedLists() {
        throw new UnsupportedOperationException();
    }

    // Utility Methods // // // // // // // // // // // // // // // // // // //

    /**
     * Replace the complete contents of the target {@link EventList} with the complete
     * contents of the source {@link EventList} while making as few list changes
     * as possible.
     *
     * <p>In a multi-threaded environment, it is necessary that the caller obtain
     * the write lock for the target list before this method is invoked. If the
     * source list is an {@link EventList}, its read lock must also be acquired.
     *
     * <p>This method shall be used when it is necessary to update an EventList
     * to a newer state while minimizing the number of change events fired. It
     * is desirable over {@link List#clear() clear()}; {@link List#addAll(Collection) addAll()}
     * because it will not cause selection to be lost if unnecessary. It is also
     * useful where firing changes may be expensive, such as when they will cause
     * writes to disk or the network.
     *
     * <p>This is implemented using Eugene W. Myer's paper, "An O(ND) Difference
     * Algorithm and Its Variations", the same algorithm found in GNU diff.
     *
     * <p>Note that the runtime of this method is significantly less efficient
     * in both time and memory than the {@link #replaceAllSorted sorted} version
     * of replaceAll.
     *
     * @param updates whether to fire update events for Objects that are equal in
     *      both {@link List}s.
     */
    public static <E> void replaceAll(EventList<E> target, List<E> source, boolean updates) {
        Diff.replaceAll(target, source, updates);
    }

    /**
     * Overloaded version of {@link #replaceAll(EventList,List,boolean)} that uses
     * a {@link Comparator} to determine equality rather than
     * {@link Object#equals(Object) equals()}.
     *
     * @param comparator the {@link Comparator} to determine equality between
     *      elements. This {@link Comparator} must return <code>0</code> for
     *      elements that are equal and nonzero for elements that are not equal.
     *      Sort order is not used.
     */
    public static <E> void replaceAll(EventList<E> target, List<E> source, boolean updates, Comparator<E> comparator) {
        Diff.replaceAll(target, source, updates, comparator);
    }


    /**
     * Replace the complete contents of the target {@link EventList} with the complete
     * contents of the source {@link Collection} while making as few list changes
     * as possible.
     *
     * <p>Unlike the {@link #replaceAll general} versions of this method, the
     * <i>sorted</i> version <strong>requires that both the input and the output
     * are sorted collections</strong>, and that they're sorted with the
     * {@link Comparator} specified. If they're sorted in {@link Comparator natural}
     * order, use {@link #comparableComparator()}.
     *
     * <p>In a multi-threaded environment, it is necessary that the caller obtain
     * the write lock for the target list before this method is invoked. If the
     * source list is an {@link EventList}, its read lock must also be acquired.
     *
     * <p>This method shall be used when it is necessary to update an EventList
     * to a newer state while minimizing the number of change events fired. It
     * is desirable over {@link List#clear() clear()}; {@link List#addAll(Collection) addAll()}
     * because it will not cause selection to be lost if unnecessary. It is also
     * useful where firing changes may be expensive, such as when they will cause
     * writes to disk or the network.
     *
     * <p>Note that this method is significantly more efficient in both
     * time and memory than the {@link #replaceAll general} version of replaceAll.
     *
     * @see Collections#sort
     * @see SortedSet
     *
     * @param target an EventList sorted with the {@link Comparator} specified.
     *     Its contents will be replaced with those in <code>source</code>.
     * @param source a collection sorted with the {@link Comparator} specified.
     * @param comparator defines the sort order for both target and source. It
     *     should also define identity. Ie, elements that compare to 0 by
     *     this comparator represent the same logical element in the list. If
     *     <code>null</code>, the {@link #comparableComparator} will be used,
     *     which means that all elements must implement {@link Comparable}.
     * @param updates whether to fire update events for Objects that are equal in
     *      both {@link List}s.
     */
    public static <E> void replaceAllSorted(EventList<E> target, Collection<E> source, boolean updates, Comparator<E> comparator) {
        GlazedListsImpl.replaceAll(target, source, updates, comparator);
    }

    // Comparators // // // // // // // // // // // // // // // // // // // //

    /** Provide Singleton access for all Comparators with no internal state */
    private static Comparator<Boolean> booleanComparator = null;
    private static Comparator<Comparable> comparableComparator = null;
    private static Comparator<Comparable> reversedComparable = null;

    /**
     * Creates a {@link Comparator} that uses Reflection to compare two instances
     * of the specified {@link Class} by the given JavaBean property.  The JavaBean
     * property must implement {@link Comparable}.
     */
    public static <T> Comparator<T> beanPropertyComparator(Class<T> className, String property) {
        return beanPropertyComparator(className, property, comparableComparator());
    }

    /**
     * Creates a {@link Comparator} that uses Reflection to compare two instances
     * of the specified {@link Class} by the given JavaBean property.  The JavaBean
     * property is compared using the provided {@link Comparator}.
     */
    public static <T> Comparator<T> beanPropertyComparator(Class<T> className, String property, Comparator propertyComparator) {
        return new BeanPropertyComparator<T>(className, property, propertyComparator);
    }

    /**
     * Creates a {@link Comparator} for use with {@link Boolean} objects.
     */
    public static Comparator<Boolean> booleanComparator() {
        if(booleanComparator == null) booleanComparator = new BooleanComparator();
        return booleanComparator;
    }

    /**
     * Creates a {@link Comparator} that compares {@link String} objects in
     * a case-insensitive way.  This {@link Comparator} is equivalent to using
     * {@link String#CASE_INSENSITIVE_ORDER} and exists here for convenience.
     */
    public static Comparator<String> caseInsensitiveComparator() {
        return String.CASE_INSENSITIVE_ORDER;
    }

    /**
     * Creates a chain of {@link Comparator}s that applies the provided
     * {@link Comparator}s in the sequence specified until differences or
     * absoulute equality.is determined.
     */
    public static <T> Comparator<T> chainComparators(List<Comparator<T>> comparators) {
        return new ComparatorChain<T>(comparators);
    }

    /**
     * Creates a {@link Comparator} that compares {@link Comparable} objects.
     */
    public static Comparator<Comparable> comparableComparator() {
        if(comparableComparator == null) comparableComparator = new ComparableComparator();
        return comparableComparator;
    }

    /**
     * Creates a reverse {@link Comparator} that works for {@link Comparable} objects.
     */
    public static Comparator<Comparable> reverseComparator() {
        if(reversedComparable == null) reversedComparable = reverseComparator(comparableComparator());
        return reversedComparable;
    }

    /**
     * Creates a reverse {@link Comparator} that inverts the given {@link Comparator}.
     */
    public static <T> Comparator<T> reverseComparator(Comparator<T> forward) {
        return new ReverseComparator<T>(forward);
    }

    // TableFormats // // // // // // // // // // // // // // // // // // // //

    /**
     * Creates a {@link TableFormat} that binds JavaBean properties to
     * table columns via Reflection.
     */
    public static <T> TableFormat<T> tableFormat(String[] propertyNames, String[] columnLabels) {
        return new BeanTableFormat<T>(null, propertyNames, columnLabels);
    }

    /**
     * Creates a {@link TableFormat} that binds JavaBean properties to
     * table columns via Reflection.
     *
     * @param baseClass the class of the Object to divide into columns. If specified,
     *      the returned class will provide implementation of
     *      {@link AdvancedTableFormat#getColumnClass(int)} and
     *      {@link AdvancedTableFormat#getColumnComparator(int)} by examining the
     *      classes of the column value.
     */
    public static <T> TableFormat<T> tableFormat(Class<T> baseClass, String[] propertyNames, String[] columnLabels) {
        return new BeanTableFormat<T>(baseClass, propertyNames, columnLabels);
    }

    /**
     * Creates a {@link TableFormat} that binds JavaBean properties to
     * table columns via Reflection. The returned {@link TableFormat} implements
     * {@link WritableTableFormat} and may be used for an editable table.
     */
    public static <T> TableFormat<T> tableFormat(String[] propertyNames, String[] columnLabels, boolean[] editable) {
        return new BeanTableFormat<T>(null, propertyNames, columnLabels, editable);
    }

    /**
     * Creates a {@link TableFormat} that binds JavaBean properties to
     * table columns via Reflection. The returned {@link TableFormat} implements
     * {@link WritableTableFormat} and may be used for an editable table.
     *
     * @param baseClass the class of the Object to divide into columns. If specified,
     *      the returned class will provide implementation of
     *      {@link AdvancedTableFormat#getColumnClass(int)} and
     *      {@link AdvancedTableFormat#getColumnComparator(int)} by examining the
     *      classes of the column value.
     */
    public static <T> TableFormat<T> tableFormat(Class<T> baseClass, String[] propertyNames, String[] columnLabels, boolean[] editable) {
        return new BeanTableFormat<T>(baseClass, propertyNames, columnLabels, editable);
    }


    // TextFilterators // // // // // // // // // // // // // // // // // // //

    private static StringTextFilterator stringTextFilterator = null;

    /**
     * Creates a {@link TextFilterator} that searches the given JavaBean
     * properties.
     */
    public static <E> TextFilterator<E> textFilterator(String[] propertyNames) {
        return new BeanTextFilterator<E>(propertyNames);
    }

    /**
     * Creates a {@link TextFilterator} that searches against an Object's
     * {@link Object#toString() toString()} value.
     */
    public static TextFilterator toStringTextFilterator() {
        if(stringTextFilterator == null) stringTextFilterator = new StringTextFilterator();
        return stringTextFilterator;
    }


    // ThresholdEvaluators // // // // // // // // // // // // // // // // // //

    /**
     * Creates a {@link ThresholdList.Evaluator} that uses Reflection to utilize an
     * integer JavaBean property as the threshold evaluation.
     */
    public static <E> ThresholdList.Evaluator<E> thresholdEvaluator(String propertyName) {
        return new BeanThresholdEvaluator<E>(propertyName);
    }

    // CollectionListModels   // // // // // // // // // // // // // // // // //

    /**
     * Creates a {@link CollectionList.Model} that where {@link List}s or {@link EventList}s
     * are the elements of a parent {@link EventList}. This can be used to compose
     * {@link EventList}s from other {@link EventList}s.
     */
    public static <E> CollectionList.Model<List<E>,E> listCollectionListModel() {
        return new ListCollectionListModel<E>();
    }


    // EventLists // // // // // // // // // // // // // // // // // // // // //

    /**
     * Creates a new {@link EventList} which contains the contents of the specified
     * {@link Collection}. The {@link EventList}'s order will be determined by
     * {@link Collection#iterator() contents.iterator()}.
     */
    public static <E> EventList<E> eventList(Collection<? extends E> contents) {
        final EventList<E> result = new BasicEventList<E>();
        if(contents != null) result.addAll(contents);
        return result;
    }

    /**
     * Wraps the source in an {@link EventList} that does not allow writing operations.
     *
     * <p>The returned {@link EventList} is useful for programming defensively. A
     * {@link EventList} is useful to supply an unknown class read-only access
     * to your {@link EventList}.
     *
     * <p>The returned {@link EventList} will provides an up-to-date view of its source
     * {@link EventList} so changes to the source {@link EventList} will still be
     * reflected. For a static copy of any {@link EventList} it is necessary to copy
     * the contents of that {@link EventList} into an {@link ArrayList}.
     *
     * <p><strong><font color="#FF0000">Warning:</font></strong> This returned EventList
     * is thread ready but not thread safe. See {@link EventList} for an example
     * of thread safe code.
     */
    public static <E> TransformedList<E, E> readOnlyList(EventList<E> source) {
        return new ReadOnlyList<E>(source);
    }

    /**
     * Wraps the source in an {@link EventList} that obtains a
     * {@link ca.odell.glazedlists.util.concurrent.ReadWriteLock ReadWritLock} for all
     * operations.
     *
     * <p>This provides some support for sharing {@link EventList}s between multiple
     * threads.
     *
     * <p>Using a {@link ThreadSafeList} for concurrent access to lists can be expensive
     * because a {@link ca.odell.glazedlists.util.concurrent.ReadWriteLock ReadWriteLock}
     * is aquired and released for every operation.
     *
     * <p><strong><font color="#FF0000">Warning:</font></strong> Although this class
     * provides thread safe access, it does not provide any guarantees that changes
     * will not happen between method calls. For example, the following code is unsafe
     * because the source {@link EventList} may change between calls to
     * {@link TransformedList#size() size()} and {@link TransformedList#get(int) get()}:
     * <pre> EventList source = ...
     * ThreadSafeList myList = new ThreadSafeList(source);
     * if(myList.size() > 3) {
     *   System.out.println(myList.get(3));
     * }</pre>
     *
     * <p><strong><font color="#FF0000">Warning:</font></strong> The objects returned
     * by {@link TransformedList#iterator() iterator()},
     * {@link TransformedList#subList(int,int) subList()}, etc. are not thread safe.
     *
     * @see ca.odell.glazedlists.util.concurrent
     */
    public static <E> TransformedList<E, E> threadSafeList(EventList<E> source) {
        return new ThreadSafeList<E>(source);
    }

    /**
     * Provides a proxy to another ListEventListener that may go out of
     * scope without explicitly removing itself from the source list's set of
     * listeners.
     *
     * <p>This exists to solve a garbage collection problem. Suppose I have an
     * {@link EventList} <i>L</i> and I request a {@link ListIterator} for <i>L</i>.
     * The {@link ListIterator} must listen for change events to <i>L</i> in order
     * to be consistent. Therefore such an iterator will register
     * itself as a listener for <i>L</i>. When the iterator goes out of scope (as
     * they usually do), it will remain as a listener for <i>L</i>. This prevents
     * the iterator object from ever being garbage collected! But the iterator is
     * never used again. Because iterators can be used very frequently, this will
     * cause an unacceptable memory leak.
     *
     * <p>Instead of adding the iterator directly as a listener for <i>L</i>, add
     * the proxy instead. The proxy will retain a <code>WeakReference</code> to the
     * iterator and forward events to the iterator as long as it is reachable. When
     * theiterator is no longer reachable, the proxy will remove itself from the list
     * of listeners for <i>L</i>. All garbage is then available for collection.
     *
     * @see java.lang.ref.WeakReference
     */
    public static <E> ListEventListener<E> weakReferenceProxy(EventList<E> source, ListEventListener<E> target) {
        return new WeakReferenceProxy<E>(source, target);
    }

    // ObservableElementList Connectors // // // // // // // // // // // // //

    /**
     * Create a new Connector for the {@link ObservableElementList} that works with
     * JavaBeans' {@link java.beans.PropertyChangeListener}. The methods to add
     * and remove listeners are detected automatically by examining the bean class
     * and searching for a method prefixed with "add" or "remove" taking a single
     * {@link java.beans.PropertyChangeListener} argument.
     *
     *
     * @param beanClass a class with both <code>addPropertyChangeListener(PropertyChangeListener)</code>
     *      and <code>removePropertyChangeListener(PropertyChangeListener)</code>,
     *      or similar methods.
     * @return an ObservableElementList.Connector for the specified class
     */
    public static <E> ObservableElementList.Connector<E> beanConnector(Class<E> beanClass) {
        return new JavaBeanEventListConnector<E>(beanClass);
    }

    /**
     * Create a new Connector for the {@link ObservableElementList} that works with
     * JavaBeans' {@link java.beans.PropertyChangeListener}. The methods to add
     * and remove listeners are specified by name. Such methods must take a single
     * {@link java.beans.PropertyChangeListener} argument.
     *
     * @param beanClass a class with both methods as specified.
     * @param addListener a method name such as "addPropertyChangeListener"
     * @param removeListener a method name such as "removePropertyChangeListener"
     * @return an ObservableElementList.Connector for the specified class
     */
    public static <E> ObservableElementList.Connector<E> beanConnector(Class<E> beanClass, String addListener, String removeListener) {
        return new JavaBeanEventListConnector<E>(beanClass, addListener, removeListener);
    }

    // Matchers // // // // // // // // // // // // // // // // // // // // // // //

    /**
     * Create a new Matcher which uses reflection to read properties with the
     * given <code>propertyName</code> from instances of the given
     * <code>beanClass</code> and compare them with the given <code>value</code>.
     *
     * @param beanClass the type of class containing the named bean property
     * @param propertyName the name of the bean property
     * @param value the value to compare with the bean property
     * @return <tt>true</tt> if the named bean property equals the given <code>value</code>
     */
    public static <E> Matcher<E> beanPropertyMatcher(Class<E> beanClass, String propertyName, Object value) {
        return new BeanPropertyMatcher(new BeanProperty<E>(beanClass, propertyName, true, false), value);
    }

    /**
     * Get a {@link MatcherEditor} that is fixed on the specified {@link Matcher}.
     */
    public static <E> MatcherEditor<E> fixedMatcherEditor(Matcher<E> matcher) {
        return new FixedMatcherEditor<E>(matcher);
    }

    // ListEventListeners // // // // // // // // // // // // // // // // // //

    /**
     * Synchronize the specified {@link EventList} to the specified {@link List}.
     * Each time the {@link EventList} is changed, the changes are applied to the
     * {@link List} as well, so that the two lists are always equal.
     *
     * <p>This is useful when a you need to support a {@link List} datamodel
     * but would prefer to manipulate that {@link List} with the convenience
     * of {@link EventList}s:
     * <pre><code>List someList = ...
     *
     * // create an EventList with the contents of someList
     * EventList eventList = GlazedLists.eventList(someList);
     *
     * // propagate changes from eventList to someList
     * GlazedLists.syncEventListToList(eventList, someList);
     *
     * // test it out, should print "true, true, true true"
     * eventList.add("boston creme");
     * System.out.println(eventList.equals(someList));
     * eventList.add("crueller");
     * System.out.println(eventList.equals(someList));
     * eventList.remove("bostom creme");
     * System.out.println(eventList.equals(someList));
     * eventList.clear();
     * System.out.println(eventList.equals(someList));</code></pre>
     *
     * @param source the {@link EventList} which provides the master view.
     *      Each change to this {@link EventList} will be applied to the
     *      {@link List}.
     * @param target the {@link List} to host a copy of the {@link EventList}.
     *      This {@link List} should not be changed after the lists have been
     *      synchronized. Otherwise a {@link RuntimeException} will be thrown
     *      when the drift is detected. This class must support all mutating
     *      {@link List} operations.
     * @return the {@link ListEventListener} providing the link from the
     *      source {@link EventList} to the target {@link List}. To stop the
     *      synchronization, use
     *      {@link EventList#removeListEventListener(ListEventListener)}.
     */
    public static <E> ListEventListener<E> syncEventListToList(EventList<E> source, List<E> target) {
        return new SyncListener<E>(source, target);
    }

    /**
     * Check list elements for type safety after they are added to an EventList
     * using a {@link ListEventListener}. The {@link ListEventListener} which
     * is installed and returned to the caller (which they may uninstall at
     * their leisure) will throw an {@link IllegalArgumentException} if it
     * detects the addition of an element with an unsupported type.
     *
     * <p> This {@link ListEventListener} is typically used as a tool to
     * check invariants of the elements of {@link EventList}s during
     * software development and testing phases.
     *
     * @param source the {@link EventList} on which to provide type safety
     * @param types the set of types to which each list element must be
     *      assignable - note <tt>null</tt> is an acceptable type and
     *      indicates the {@link EventList} expects to contain <tt>null</tt>
     *      elements
     * @return the {@link ListEventListener} providing the which provides type
     *      safety checking on the given <code>source</code>. To stop the
     *      type safety checking, use
     *      {@link EventList#removeListEventListener(ListEventListener)}.
     */
    public static <E> ListEventListener<E> typeSafetyListener(EventList<E> source, Set<Class> types) {
        return new TypeSafetyListener<E>(source, types);
    }
}