/**
 * Glazed Lists
 * http://glazedlists.dev.java.net/
 *
 * COPYRIGHT 2003 O'DELL ENGINEERING LTD.
 */
package ca.odell.glazedlists;

import java.util.*;
// for access to volatile classes
import ca.odell.glazedlists.impl.*;
import ca.odell.glazedlists.impl.sort.*;
import ca.odell.glazedlists.impl.beans.*;
import ca.odell.glazedlists.impl.gui.*;
import ca.odell.glazedlists.impl.swt.*;
// implemented interfaces
import ca.odell.glazedlists.gui.*;
import ca.odell.glazedlists.TextFilterator;
import ca.odell.glazedlists.ThresholdEvaluator;
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


    // Comparators // // // // // // // // // // // // // // // // // // // //

    /** Provide Singleton access for all Comparators with no internal state */
    private static Comparator booleanComparator = null;
    private static Comparator comparableComparator = null;
    private static Comparator reversedComparable = null;

    /**
     * Creates a {@link Comparator} that uses Reflection to compare two instances
     * of the specified {@link Class} by the given JavaBean property.  The JavaBean
     * property must implement {@link Comparable}.
     */
    public static Comparator beanPropertyComparator(Class className, String property) {
        return beanPropertyComparator(className, property, comparableComparator());
    }

    /**
     * Creates a {@link Comparator} that uses Reflection to compare two instances
     * of the specified {@link Class} by the given JavaBean property.  The JavaBean
     * property is compared using the provided {@link Comparator}.
     */
    public static Comparator beanPropertyComparator(Class className, String property, Comparator propertyComparator) {
        return new BeanPropertyComparator(className, property, propertyComparator);
    }

    /**
     * Creates a {@link Comparator} for use with {@link Boolean} objects.
     */
    public static Comparator booleanComparator() {
        if(booleanComparator == null) booleanComparator = new BooleanComparator();
        return booleanComparator;
    }

    /**
     * Creates a {@link Comparator} that compares {@link String} objects in
     * a case-insensitive way.  This {@link Comparator} is equivalent to using
     * {@link String#CASE_INSENSITIVE_ORDER} and exists here for convenience.
     */
    public static Comparator caseInsensitiveComparator() {
        return String.CASE_INSENSITIVE_ORDER;
    }

    /**
     * Creates a chain of {@link Comparator}s that applies the provided
     * {@link Comparator}s in the sequence specified until differences or
     * absoulute equality.is determined.
     */
    public static Comparator chainComparators(List comparators) {
        return new ComparatorChain(comparators);
    }

    /**
     * Creates a {@link Comparator} that compares {@link Comparable} objects.
     */
    public static Comparator comparableComparator() {
        if(comparableComparator == null) comparableComparator = new ComparableComparator();
        return comparableComparator;
    }

    /**
     * Creates a reverse {@link Comparator} that works for {@link Comparable} objects.
     */
    public static Comparator reverseComparator() {
        if(reversedComparable == null) reversedComparable = reverseComparator(comparableComparator());
        return reversedComparable;
    }

    /**
     * Creates a reverse {@link Comparator} that inverts the given {@link Comparator}.
     */
    public static Comparator reverseComparator(Comparator forward) {
        return new ReverseComparator(forward);
    }

    // TableFormats // // // // // // // // // // // // // // // // // // // //

    /**
     * Creates a {@link TableFormat} that binds JavaBean properties to
     * table columns via Reflection.
     */
    public static TableFormat tableFormat(String[] propertyNames, String[] columnLabels) {
        return new BeanTableFormat(null, propertyNames, columnLabels);
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
    public static TableFormat tableFormat(Class baseClass, String[] propertyNames, String[] columnLabels) {
        return new BeanTableFormat(baseClass, propertyNames, columnLabels);
    }
    
    /**
     * Creates a {@link TableFormat} that binds JavaBean properties to
     * table columns via Reflection. The returned {@link TableFormat} implements
     * {@link WritableTableFormat} and may be used for an editable table.
     */
    public static TableFormat tableFormat(String[] propertyNames, String[] columnLabels, boolean[] editable) {
        return new BeanTableFormat(null, propertyNames, columnLabels, editable);
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
    public static TableFormat tableFormat(Class baseClass, String[] propertyNames, String[] columnLabels, boolean[] editable) {
        return new BeanTableFormat(baseClass, propertyNames, columnLabels, editable);
    }


    // TextFilterators // // // // // // // // // // // // // // // // // // //

    /**
     * Creates a {@link TextFilterator} that searches the given JavaBean
     * properties.
     */
    public static TextFilterator textFilterator(String[] propertyNames) {
        return new BeanTextFilterator(propertyNames);
    }


    // ThresholdEvaluators // // // // // // // // // // // // // // // // // //

    /**
     * Creates a {@link ThresholdEvaluator} that uses Reflection to utilize an
     * integer JavaBean property as the threshold evaluation.
     */
    public static ThresholdEvaluator thresholdEvaluator(String propertyName) {
        return new BeanThresholdEvaluator(propertyName);
    }


    // EventLists // // // // // // // // // // // // // // // // // // // // //

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
    public static TransformedList readOnlyList(EventList source) {
        return new ReadOnlyList(source);
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
    public static TransformedList threadSafeList(EventList source) {
        return new ThreadSafeList(source);
    }
}