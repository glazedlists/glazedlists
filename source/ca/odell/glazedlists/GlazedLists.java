/**
 * Glazed Lists
 * http://glazedlists.dev.java.net/
 *
 * COPYRIGHT 2003 O'DELL ENGINEERING LTD.
 */
package ca.odell.glazedlists;

import java.util.*;
// for access to volatile classes
import ca.odell.glazedlists.impl.sort.*;
import ca.odell.glazedlists.impl.io.*;
import ca.odell.glazedlists.impl.beans.*;
// implemented interfaces
import ca.odell.glazedlists.io.ByteCoder;
import ca.odell.glazedlists.gui.TableFormat;
import ca.odell.glazedlists.gui.WritableTableFormat;
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
		return new BeanTableFormat(propertyNames, columnLabels);
	}

    /**
     * Creates a {@link WritableTableFormat} that binds JavaBean properties to
     * optionally writable table columns via Reflection.
     */
    public static WritableTableFormat writableTableFormat(String[] propertyNames, String[] columnLabels, boolean[] editable) {
		return new BeanWritableTableFormat(propertyNames, columnLabels, editable);
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


    // ByteCoders // // // // // // // // // // // // // // // // // // // // // 
    
    /** Provide Singleton access for all ByteCoders with no internal state */
    private static ByteCoder serializableByteCoder = new SerializableByteCoder();
    private static ByteCoder beanXMLByteCoder = new BeanXMLByteCoder();

    /**
     * Creates a {@link ByteCoder} that encodes {@link java.io.Serializable Serializable}
     * Objects using an {@link java.io.ObjectOutputStream}.
     */
    public static ByteCoder serializableByteCoder() {
        if(serializableByteCoder == null) serializableByteCoder = new SerializableByteCoder(); 
        return serializableByteCoder;
    }

    /**
     * Creates a {@link ByteCoder} that uses {@link java.beans.XMLEncoder XMLEncoder} and
     * {@link java.beans.XMLDecoder XMLDecoder} classes from java.beans. Encoded
     * Objects must be JavaBeans.
     */
    public static ByteCoder beanXMLByteCoder() {
        if(beanXMLByteCoder == null) beanXMLByteCoder = new BeanXMLByteCoder(); 
        return beanXMLByteCoder;
    }
}