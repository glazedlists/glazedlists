/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists;

import ca.odell.glazedlists.FunctionList.Function;
import ca.odell.glazedlists.event.ListEventListener;
import ca.odell.glazedlists.event.ListEventPublisher;
import ca.odell.glazedlists.gui.AdvancedTableFormat;
import ca.odell.glazedlists.gui.TableFormat;
import ca.odell.glazedlists.gui.WritableTableFormat;
import ca.odell.glazedlists.impl.Diff;
import ca.odell.glazedlists.impl.FunctionListMap;
import ca.odell.glazedlists.impl.GlazedListsImpl;
import ca.odell.glazedlists.impl.GroupingListMultiMap;
import ca.odell.glazedlists.impl.ListCollectionListModel;
import ca.odell.glazedlists.impl.ObservableConnector;
import ca.odell.glazedlists.impl.ReadOnlyList;
import ca.odell.glazedlists.impl.SimpleFunctionList;
import ca.odell.glazedlists.impl.SyncListener;
import ca.odell.glazedlists.impl.ThreadSafeList;
import ca.odell.glazedlists.impl.TypeSafetyListener;
import ca.odell.glazedlists.impl.WeakReferenceProxy;
import ca.odell.glazedlists.impl.beans.BeanConnector;
import ca.odell.glazedlists.impl.beans.BeanFunction;
import ca.odell.glazedlists.impl.beans.BeanTableFormat;
import ca.odell.glazedlists.impl.beans.BeanTextFilterator;
import ca.odell.glazedlists.impl.beans.BeanThresholdEvaluator;
import ca.odell.glazedlists.impl.beans.StringBeanFunction;
import ca.odell.glazedlists.impl.filter.StringTextFilterator;
import ca.odell.glazedlists.impl.functions.ConstantFunction;
import ca.odell.glazedlists.impl.matchers.FixedMatcherEditor;
import ca.odell.glazedlists.impl.sort.BeanPropertyComparator;
import ca.odell.glazedlists.impl.sort.BooleanComparator;
import ca.odell.glazedlists.impl.sort.ComparableComparator;
import ca.odell.glazedlists.impl.sort.ComparatorChain;
import ca.odell.glazedlists.impl.sort.ReverseComparator;
import ca.odell.glazedlists.matchers.Matcher;
import ca.odell.glazedlists.matchers.MatcherEditor;
import ca.odell.glazedlists.matchers.Matchers;
import ca.odell.glazedlists.util.concurrent.ReadWriteLock;

import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Observable;
import java.util.Set;
import java.util.SortedSet;

/**
 * A factory for creating all sorts of objects to be used with Glazed Lists.
 *
 * @author <a href="mailto:jesse@swank.ca">Jesse Wilson</a>
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
     * {@link Comparator} specified. If they're sorted in {@link Comparable natural}
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
     * Creates a {@link Comparator} that uses Reflection to compare two
     * instances of the specified {@link Class} by the given JavaBean
     * properties. The JavaBean <code>property</code> and any extra
     * <code>properties</code> must implement {@link Comparable}.
     *
     * <p>The following example code sorts a List of Customers by first name,
     * with ties broken by last name.
     *
     * <pre>
     *    List<Customer> customers = ...
     *    Comparator<Customer> comparator = GlazedLists.beanPropertyComparator(Customer.class, "firstName", "lastName");
     *    Collections.sort(customers, comparator);
     * </pre>
     *
     * @param clazz the name of the class which defines the accessor method
     *      for the given <code>property</code> and optionaly <code>properties</code>
     * @param property the name of the first Comparable property to be extracted
     *      and used to compare instances of the <code>clazz</code>
     * @param properties the name of optional Comparable properties, each of
     *      which is used to break ties for the prior property.
     */
    public static <T> Comparator<T> beanPropertyComparator(Class<T> clazz, String property, String... properties) {
        // build the Comparator that must exist
        Comparator<T> firstComparator = beanPropertyComparator(clazz, property, comparableComparator());

        // if only one Comparator is specified, return it immediately
        if (properties.length == 0) {
            return firstComparator;
        }

        // build the remaining Comparators
        final List<Comparator<T>> comparators = new ArrayList<Comparator<T>>(properties.length+1);
        comparators.add(firstComparator);

        for (int i = 0; i < properties.length; i++) {
            comparators.add(beanPropertyComparator(clazz, properties[i], comparableComparator()));
        }

        // chain all Comparators together
        return chainComparators(comparators);
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
        if(booleanComparator == null) {
            booleanComparator = new BooleanComparator();
        }
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
     * absolute equality is determined.
     */
    public static <T> Comparator<T> chainComparators(List<Comparator<T>> comparators) {
        return new ComparatorChain<T>(comparators);
    }

    /**
     * Creates a chain of {@link Comparator}s that applies the provided
     * {@link Comparator}s in the sequence specified until differences or
     * absolute equality is determined.
     */
    public static <T> Comparator<T> chainComparators(Comparator<T>... comparators) {
        return chainComparators(Arrays.asList(comparators));
    }

    /**
     * Creates a {@link Comparator} that compares {@link Comparable} objects.
     */
    @SuppressWarnings("unchecked")
    public static <T extends Comparable> Comparator<T> comparableComparator() {
        if(comparableComparator == null) {
            comparableComparator = new ComparableComparator();
        }
        return (Comparator<T>)comparableComparator;
    }

    /**
     * Creates a reverse {@link Comparator} that works for {@link Comparable} objects.
     */
    @SuppressWarnings("unchecked")
    public static <T extends Comparable> Comparator<T> reverseComparator() {
        if(reversedComparable == null) {
            reversedComparable = reverseComparator(comparableComparator());
        }
        return (Comparator<T>)reversedComparable;
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

    private static TextFilterator<Object> stringTextFilterator = null;

    /**
     * Creates a {@link TextFilterator} that searches the given JavaBean
     * properties.
     */
    public static <E> TextFilterator<E> textFilterator(String... propertyNames) {
        return new BeanTextFilterator<Object,E>(propertyNames);
    }

    /**
     * Creates a {@link TextFilterator} that searches the given JavaBean
     * properties.
     */
    public static <E> TextFilterator<E> textFilterator(Class<E> beanClass, String... propertyNames) {
        return new BeanTextFilterator<Object,E>(beanClass, propertyNames);
    }

    /**
     * Creates a {@link TextFilterator} that searches the given JavaBean
     * properties.
     */
    public static <D,E> Filterator<D,E> filterator(String... propertyNames) {
        return new BeanTextFilterator<D,E>(propertyNames);
    }

    /**
     * Creates a {@link TextFilterator} that searches the given JavaBean
     * properties of the specified class.
     */
    public static <D,E> Filterator<D,E> filterator(Class<E> beanClass, String... propertyNames) {
        return new BeanTextFilterator<D,E>(beanClass, propertyNames);
    }

    /**
     * Creates a {@link TextFilterator} that searches against an Object's
     * {@link Object#toString() toString()} value.
     */
    @SuppressWarnings("unchecked")
    public static <E> TextFilterator<E> toStringTextFilterator() {
        if(stringTextFilterator == null) {
            stringTextFilterator = new StringTextFilterator<Object>();
        }
        return (TextFilterator<E>) stringTextFilterator;
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
     * Creates a new {@link EventList} which contains the given elements.
     *
     * @param contents the list elements, if <code>null</code> the result will be an empty list
     * @return the new {@link EventList}
     */
    public static <E> EventList<E> eventListOf(E... contents) {
        return eventList(contents == null ? Collections.<E>emptyList() : Arrays.asList(contents));
    }

    /**
     * Creates a new {@link EventList} which contains the contents of the specified
     * {@link Collection}. The {@link EventList}'s order will be determined by
     * {@link Collection#iterator() contents.iterator()}.
     *
     * @param contents the collection with list elements, if <code>null</code> the result will be
     *            an empty list
     */
    public static <E> EventList<E> eventList(Collection<? extends E> contents) {
        final EventList<E> result = new BasicEventList<E>(contents == null ? 0 : contents.size());
        if(contents != null) {
            result.addAll(contents);
        }
        return result;
    }

    /**
     * Creates a new {@link EventList} with the given {@link ListEventPublisher} and
     * {@link ReadWriteLock} which contains the given elements.
     *
     * @param publisher the {@link ListEventPublisher} for the new list, may be <code>null</code>
     * @param lock the {@link ReadWriteLock} for the new list, may be <code>null</code>
     * @param contents the list elements, if <code>null</code> the result will be an empty list
     * @return the new {@link EventList}
     */
    public static <E> EventList<E> eventListOf(ListEventPublisher publisher, ReadWriteLock lock,
            E... contents) {
        return eventList(publisher, lock, contents == null ? Collections.<E>emptyList() : Arrays
                .asList(contents));
    }

    /**
     * Creates a new {@link EventList} with the given {@link ListEventPublisher} and
     * {@link ReadWriteLock} which contains the contents of the specified
     * {@link Collection}. The {@link EventList}'s order will be determined by
     * {@link Collection#iterator() contents.iterator()}.
     *
     * @param publisher the {@link ListEventPublisher} for the new list, may be <code>null</code>
     * @param lock the {@link ReadWriteLock} for the new list, may be <code>null</code>
     * @param contents the collection with list elements, if <code>null</code> the result will be
     *            an empty list
     */
    public static <E> EventList<E> eventList(ListEventPublisher publisher, ReadWriteLock lock,
            Collection<? extends E> contents) {
        final EventList<E> result = new BasicEventList<E>(
                contents == null ? 0 : contents.size(), publisher, lock);
        if (contents != null) {
            result.addAll(contents);
        }
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
    public static <E> TransformedList<E, E> readOnlyList(EventList<? extends E> source) {
        return new ReadOnlyList<E>((EventList<E>) source);
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
    public static <E> TransformedList<E, E> threadSafeList(EventList<? extends E> source) {
        return new ThreadSafeList<E>((EventList<E>) source);
    }

    /**
     * Returns a {@link TransformedList} that maps each element of the source list to a target
     * element by use of a specified {@link Function}.
     * <p>
     * <strong><font color="#FF0000">Warning:</font></strong> The returned {@link EventList} is
     * thread ready but not thread safe. See {@link EventList} for an example of thread safe
     * code.
     * <p>
     * <strong><font color="#FF0000">Warning:</font></strong>This method will
     * <strong>not</strong> return a full-featured {@link FunctionList}, but a much simpler
     * list implementation, that is not writable.
     *
     * @param source the source list to transform
     * @param function the function to tranform each list element
     * @return a {@link TransformedList} that needs to be disposed after use
     */
    public static <S, E> TransformedList<S, E> transformByFunction(EventList<S> source, Function<S, E> function) {
        return new SimpleFunctionList<S, E>(source, function);
    }

    /**
     * Provides a proxy to another ListEventListener that may go out of scope
     * without explicitly removing itself from the source list's set of
     * listeners.
     *
     * <p>This exists to solve a garbage collection problem. Suppose I have an
     * {@link EventList} <i>L</i> and I obtain a {@link ListIterator} for <i>L</i>.
     * The {@link ListIterator} must listen for change events to <i>L</i> in order
     * to be consistent. Therefore such an iterator will register itself as a
     * listener for <i>L</i>. When the iterator goes out of scope (as they usually
     * do), it will remain as a listener of <i>L</i>. This prevents the iterator
     * object from ever being garbage collected, though the iterator can never be
     * never used again! Because iterators can be used very frequently, this will
     * cause an unacceptable memory leak.
     *
     * <p>Instead of adding the iterator directly as a listener for <i>L</i>, add
     * a proxy instead. The proxy will retain a <code>WeakReference</code> to the
     * iterator and forward events to the iterator as long as it is reachable. When
     * the iterator is no longer reachable, the proxy will remove itself from the
     * list of listeners for <i>L</i>. All garbage is then available for collection.
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
        return new BeanConnector<E>(beanClass);
    }

    /**
     * Create a new Connector for the {@link ObservableElementList} that works with JavaBeans'
     * {@link java.beans.PropertyChangeListener}. The methods to add and remove listeners are
     * detected automatically by examining the bean class and searching for a method prefixed with
     * "add" or "remove" taking a single {@link java.beans.PropertyChangeListener} argument.
     * <p>Use this variant, if you want to control which {@link java.beans.PropertyChangeEvent}s
     * are delivered to the ObservableElementList. You can match or filter events by name.
     * <p>If <code>matchPropertyNames</code> is <code>true</code>, the <code>propertyNames</code>
     * parameter specifies the set of properties by name whose {@link java.beans.PropertyChangeEvent}s
     * should be delivered to the ObservableElementList, e.g. property change events for properties
     * not contained in the specified <code>propertyNames</code> are ignored in this case.
     * If <code>matchPropertyNames</code> is <code>false</code>, then the specified
     * <code>propertyNames</code> are filtered, e.g. all but the specified property change events are
     * delivered to the ObservableElementList.
     *
     * @param beanClass a class with both
     *        <code>addPropertyChangeListener(PropertyChangeListener)</code> and
     *        <code>removePropertyChangeListener(PropertyChangeListener)</code>, or similar
     *        methods.
     * @param matchPropertyNames if <code>true</code>, match property change events against the
     *        specified property names, if <code>false</code> filter them
     * @param propertyNames specifies the properties by name whose {@link java.beans.PropertyChangeEvent}s
     *        should be matched or filtered
     * @return an ObservableElementList.Connector for the specified class
     */
    public static <E> ObservableElementList.Connector<E> beanConnector(Class<E> beanClass, boolean matchPropertyNames, String... propertyNames) {
        final Matcher<PropertyChangeEvent> byNameMatcher = Matchers.propertyEventNameMatcher(matchPropertyNames, propertyNames);
        return beanConnector(beanClass, byNameMatcher);
    }

    /**
     * Create a new Connector for the {@link ObservableElementList} that works with JavaBeans'
     * {@link java.beans.PropertyChangeListener}. The methods to add and remove listeners are
     * detected automatically by examining the bean class and searching for a method prefixed with
     * "add" or "remove" taking a single {@link java.beans.PropertyChangeListener} argument.
     *
     * <p> The event matcher allows filtering of {@link java.beans.PropertyChangeEvent}s.
     * Only matching events are delivered to the ObservableElementList.
     * To create a matcher that matches PropertyChangeEvents by property names, you can use
     * {@link Matchers#propertyEventNameMatcher(boolean, String[])}
     *
     * @param beanClass a class with both
     *        <code>addPropertyChangeListener(PropertyChangeListener)</code> and
     *        <code>removePropertyChangeListener(PropertyChangeListener)</code>, or similar
     *        methods.
     * @param eventMatcher for matching PropertyChangeEvents that will be delivered to the
     *        ObservableElementList
     * @return an ObservableElementList.Connector for the specified class
     */
    public static <E> ObservableElementList.Connector<E> beanConnector(Class<E> beanClass,
            Matcher<PropertyChangeEvent> eventMatcher) {
        return new BeanConnector<E>(beanClass, eventMatcher);
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
        return new BeanConnector<E>(beanClass, addListener, removeListener);
    }

    /**
     * Create a new Connector for the {@link ObservableElementList} that works with
     * JavaBeans' {@link java.beans.PropertyChangeListener}. The methods to add
     * and remove listeners are specified by name. Such methods must take a single
     * {@link java.beans.PropertyChangeListener} argument.
     *
     * <p> The event matcher allows filtering of {@link java.beans.PropertyChangeEvent}s.
     * Only matching events are delivered to the ObservableElementList.
     * To create a matcher that matches PropertyChangeEvents by property names, you can use
     * {@link Matchers#propertyEventNameMatcher(boolean, String[])}
     *
     * @param beanClass a class with both methods as specified.
     * @param addListener a method name such as "addPropertyChangeListener"
     * @param removeListener a method name such as "removePropertyChangeListener"
     * @param eventMatcher for matching PropertyChangeEvents that will be delivered to the
     *        ObservableElementList
     * @return an ObservableElementList.Connector for the specified class
     */
    public static <E> ObservableElementList.Connector<E> beanConnector(Class<E> beanClass,
            String addListener, String removeListener, Matcher<PropertyChangeEvent> eventMatcher) {
        return new BeanConnector<E>(beanClass, addListener, removeListener, eventMatcher);
    }

    /**
     * Create a new Connector for the {@link ObservableElementList} that works
     * with subclasses of the archaic {@link Observable} base class. Each
     * element of the ObservableElementList <strong>must</strong> extend the
     * Observable base class.
     *
     * @return an ObservableElementList.Connector for objects that extend {@link Observable}
     */
    public static <E extends Observable> ObservableElementList.Connector<E> observableConnector() {
        return new ObservableConnector<E>();
    }

    // Matchers // // // // // // // // // // // // // // // // // // // // //

    /**
     * Create a new Matcher which uses reflection to read properties with the
     * given <code>propertyName</code> from instances of the given
     * <code>beanClass</code> and compare them with the given <code>value</code>.
     *
     * @param beanClass the type of class containing the named bean property
     * @param propertyName the name of the bean property
     * @param value the value to compare with the bean property
     * @return <tt>true</tt> if the named bean property equals the given <code>value</code>
     *
     * @deprecated as of 3/3/2006 - this method has been replaced by
     *      {@link Matchers#beanPropertyMatcher}. {@link Matchers} is now
     *      the permanent factory class which creates all basic Matcher
     *      implementations.
     */
    @Deprecated
    public static <E> Matcher<E> beanPropertyMatcher(Class<E> beanClass, String propertyName, Object value) {
        return Matchers.beanPropertyMatcher(beanClass, propertyName, value);
    }

    /**
     * Get a {@link MatcherEditor} that is fixed on the specified {@link Matcher}.
     */
    public static <E> MatcherEditor<E> fixedMatcherEditor(Matcher<E> matcher) {
        return new FixedMatcherEditor<E>(matcher);
    }

    // Functions // // // // // // // // // // // // // // // // // // // // //

    /**
     * Get a {@link FunctionList.Function} that always returns the given
     * <code>value</code>, regardless of its input.
     */
    public static <E,V> FunctionList.Function<E,V> constantFunction(V value) {
        return new ConstantFunction<E,V>(value);
    }

    /**
     * Get a {@link FunctionList.Function} that extracts the property with the
     * given <code>propertyName</code> from objects of the given
     * <code>beanClass</code> and then formats the return value as a String.
     */
    public static <E> FunctionList.Function<E,String> toStringFunction(Class<E> beanClass, String propertyName) {
        return new StringBeanFunction<E>(beanClass, propertyName);
    }

    /**
     * Get a {@link FunctionList.Function} that extracts the property with the
     * given <code>propertyName</code> from objects of the given
     * <code>beanClass</code>.
     */
    public static <E,V> FunctionList.Function<E,V> beanFunction(Class<E> beanClass, String propertyName) {
        return new BeanFunction<E,V>(beanClass, propertyName);
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
	 * using a {@link ListEventListener}. The {@link ListEventListener} which is
	 * installed and returned to the caller (which they may uninstall at their
	 * leisure) will throw an {@link IllegalArgumentException} if it detects the
	 * addition of an element with an unsupported type.
	 *
	 * <p>
	 * This {@link ListEventListener} is typically used as a tool to check
	 * invariants of the elements of {@link EventList}s during software
	 * development and testing phases.
	 *
	 * @param source
	 *            the {@link EventList} on which to provide type safety
	 * @param types
	 *            the set of types to which each list element must be assignable
	 *            - the set itself must not be <tt>null</tt>, but <tt>null</tt>
	 *            is an acceptable type within the set and indicates the
	 *            {@link EventList} expects to contain <tt>null</tt> elements
	 * @return the {@link ListEventListener} providing the safety checking on
	 *         the given <code>source</code>. To stop the type safety checking,
	 *         use {@link EventList#removeListEventListener(ListEventListener)}.
	 */
    public static <E> ListEventListener<E> typeSafetyListener(EventList<E> source, Set<Class> types) {
        return new TypeSafetyListener<E>(source, types);
    }

    /**
     * Synchronize the specified {@link EventList} to a MultiMap that is
     * returned from this method. Each time the {@link EventList} is changed
     * the MultiMap is updated to reflect the change.
     *
     * <p>This can be useful when it is known that an <code>EventList</code>
     * will experience very few mutations compared to read operation and wants
     * to provide a data structure that guarantees fast O(1) reads.
     *
     * <p>The keys of the MultiMap are determined by evaluating each
     * <code>source</code> element with the <code>keyMaker</code> function.
     * This form of the MultiMap requires that the keys produced by the
     * <code>keyMaker</code> are {@link Comparable} and that the natural
     * ordering of those keys also defines the grouping of values. If either
     * of those assumptions are false, consider using
     * {@link #syncEventListToMultiMap(EventList, FunctionList.Function, Comparator)}.
     *
     * <p>If two distinct values, say <code>v1</code> and <code>v2</code> each
     * produce a common key, <code>k</code>, when they are evaluated by the
     * <code>keyMaker</code> function, then a corresponding entry in the
     * MultiMap will resemble:
     *
     * <p><code>k -> {v1, v2}</code>
     *
     * <p>For example, assume the <code>keyMaker</code> function returns the
     * first letter of a name and the <code>source</code> {@link EventList}
     * contains the names:
     *
     * <p><code>{"Andy", "Arthur", "Jesse", "Holger", "James"}</code>
     *
     * <p>The MultiMap returned by this method would thus resemble:
     *
     * <p><code>
     * "A" -> {"Andy", "Arthur"}<br>
     * "H" -> {"Holger"}<br>
     * "J" -> {"Jesse", "James"}<br>
     * </code>
     *
     * <p>It is important to note that all mutating methods on the {@link Map}
     * interface "write through" to the backing {@link EventList} as expected.
     * These mutating methods include:
     *
     * <ul>
     *   <li>the mutating methods of {@link Map#keySet()} and its {@link Iterator}
     *   <li>the mutating methods of {@link Map#values()} and its {@link Iterator}
     *   <li>the mutating methods of {@link Map#entrySet()} and its {@link Iterator}
     *   <li>the {@link java.util.Map.Entry#setValue} method
     *   <li>the mutating methods of {@link Map} itself, including {@link Map#put},
     *       {@link Map#putAll}, {@link Map#remove}, and {@link Map#clear}
     * </ul>
     *
     * For information on MultiMaps go <a href="http://en.wikipedia.org/wiki/Multimap"/>here</a>.
     *
     * @param source the {@link EventList} which provides the master view.
     *      Each change to this {@link EventList} will be applied to the
     *      MultiMap
     * @param keyMaker the {@link FunctionList.Function} which produces a key
     *      for each value in the <code>source</code>. It is imperative that the
     *      keyMaker produce <strong>immutable</strong> objects.
     * @return a MultiMap which remains in sync with changes that occur to the
     *      underlying <code>source</code> {@link EventList}
     */
    public static <K extends Comparable, V> DisposableMap<K, List<V>> syncEventListToMultiMap(EventList<V> source, FunctionList.Function<V, ? extends K> keyMaker) {
        return syncEventListToMultiMap(source, keyMaker, comparableComparator());
    }

    /**
     * Synchronize the specified {@link EventList} to a MultiMap that is
     * returned from this method. Each time the {@link EventList} is changed
     * the MultiMap is updated to reflect the change.
     *
     * <p>This can be useful when it is known that an <code>EventList</code>
     * will experience very few mutations compared to read operation and wants
     * to provide a data structure that guarantees fast O(1) reads.
     *
     * <p>The keys of the MultiMap are determined by evaluating each
     * <code>source</code> element with the <code>keyMaker</code> function.
     * This form of the MultiMap makes no assumptions about the keys of the
     * MultiMap and relies on the given <code>keyGrouper</code> to define the
     * grouping of values.
     *
     * <p>If two distinct values, say <code>v1</code> and <code>v2</code> each
     * produce a common key, <code>k</code>, when they are evaluated by the
     * <code>keyMaker</code> function, then a corresponding entry in the
     * MultiMap will resemble:
     *
     * <p><code>k -> {v1, v2}</code>
     *
     * <p>For example, assume the <code>keyMaker</code> function returns the
     * first letter of a name and the <code>source</code> {@link EventList}
     * contains the names:
     *
     * <p><code>{"Andy", "Arthur", "Jesse", "Holger", "James"}</code>
     *
     * <p>The MultiMap returned by this method would thus resemble:
     *
     * <p><code>
     * "A" -> {"Andy", "Arthur"}<br>
     * "H" -> {"Holger"}<br>
     * "J" -> {"Jesse", "James"}<br>
     * </code>
     *
     * <p>It is important to note that all mutating methods on the {@link Map}
     * interface "write through" to the backing {@link EventList} as expected.
     * These mutating methods include:
     *
     * <ul>
     *   <li>the mutating methods of {@link Map#keySet()} and its {@link Iterator}
     *   <li>the mutating methods of {@link Map#values()} and its {@link Iterator}
     *   <li>the mutating methods of {@link Map#entrySet()} and its {@link Iterator}
     *   <li>the {@link java.util.Map.Entry#setValue} method
     *   <li>the mutating methods of {@link Map} itself, including {@link Map#put},
     *       {@link Map#putAll}, {@link Map#remove}, and {@link Map#clear}
     * </ul>
     *
     * For information on MultiMaps go <a href="http://en.wikipedia.org/wiki/Multimap"/>here</a>.
     *
     * @param source the {@link EventList} which provides the master view.
     *      Each change to this {@link EventList} will be applied to the
     *      MultiMap
     * @param keyMaker the {@link FunctionList.Function} which produces a key
     *      for each value in the <code>source</code>. It is imperative that the
     *      keyMaker produce <strong>immutable</strong> objects.
     * @param keyGrouper the {@link Comparator} which groups together values
     *      that share common keys
     * @return a MultiMap which remains in sync with changes that occur to the
     *      underlying <code>source</code> {@link EventList}
     */
    public static <K, V> DisposableMap<K, List<V>> syncEventListToMultiMap(EventList<V> source, FunctionList.Function<V, ? extends K> keyMaker, Comparator<? super K> keyGrouper) {
        return new GroupingListMultiMap<K, V>(source, keyMaker, keyGrouper);
    }

    /**
     * Synchronize the specified {@link EventList} to a Map that is returned
     * from this method. Each time the {@link EventList} is changed the Map is
     * updated to reflect the change.
     *
     * <p>This can be useful when it is known that an <code>EventList</code>
     * will experience very few mutations compared to read operation and wants
     * to provide a data structure that guarantees fast O(1) reads.
     *
     * <p>The keys of the Map are determined by evaluating each
     * <code>source</code> element with the <code>keyMaker</code> function.
     * The Map implementation assumes that each value has a unique key, and
     * verifies this invariant at runtime, throwing a RuntimeException if it
     * is ever violated.
     *
     * For example, if two distinct values, say <code>v1</code> and
     * <code>v2</code> each produce the key <code>k</code> when they are
     * evaluated by the <code>keyMaker</code> function, an
     * {@link IllegalStateException} is thrown to proactively indicate the
     * error.
     *
     * <p>As for example of normal usage, assume the <code>keyMaker</code>
     * function returns the first letter of a name and the <code>source</code>
     * {@link EventList} contains the names:
     *
     * <p><code>{"Kevin", "Jesse", "Holger"}</code>
     *
     * <p>The Map returned by this method would thus resemble:
     *
     * <p><code>
     * "K" -> "Kevin"<br>
     * "J" -> "Jesse"<br>
     * "H" -> "Holger"<br>
     * </code>
     *
     * <p>It is important to note that all mutating methods on the {@link Map}
     * interface "write through" to the backing {@link EventList} as expected.
     * These mutating methods include:
     *
     * <ul>
     *   <li>the mutating methods of {@link Map#keySet()} and its {@link Iterator}
     *   <li>the mutating methods of {@link Map#values()} and its {@link Iterator}
     *   <li>the mutating methods of {@link Map#entrySet()} and its {@link Iterator}
     *   <li>the {@link java.util.Map.Entry#setValue} method
     * </ul>
     *
     * @param source the {@link EventList} which provides the values of the map.
     *      Each change to this {@link EventList} will be applied to the Map.
     * @param keyMaker the {@link FunctionList.Function} which produces a key
     *      for each value in the <code>source</code>. It is imperative that the
     *      keyMaker produce <strong>immutable</strong> objects.
     * @return a Map which remains in sync with changes that occur to the
     *      underlying <code>source</code> {@link EventList}
     */
    public static <K, V> DisposableMap<K, V> syncEventListToMap(EventList<V> source, FunctionList.Function<V, K> keyMaker) {
        return new FunctionListMap<K, V>(source, keyMaker);
    }
}