/**
 * Glazed Lists
 * http://glazedlists.dev.java.net/
 *
 * COPYRIGHT 2003 O'DELL ENGINEERING LTD.
 */
package ca.odell.glazedlists.util;

// to access all of the JavaBean tools
import ca.odell.glazedlists.impl.beans.*;

// to support TableFormats
import ca.odell.glazedlists.gui.TableFormat;
import ca.odell.glazedlists.gui.WritableTableFormat;

// to support filtering
import ca.odell.glazedlists.TextFilterator;

// to support ThresholdList
import ca.odell.glazedlists.ThresholdEvaluator;

// for convenience access to ComparatorFactory.beanProperty()
import java.util.Comparator;

/**
 * A factory for creating some tools that utilize the power of JavaBeans and Reflection.
 *
 * @author <a href="mailto:kevin@swank.ca">Kevin Maltby</a>
 */
public final class BeanToolFactory {

    private BeanToolFactory() {
        throw new UnsupportedOperationException();
    }

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

	/**
	 * Creates a {@link TextFilterator} that searches the given JavaBean
	 * properties.
	 */
	public static TextFilterator textFilterator(String[] propertyNames) {
		return new BeanTextFilterator(propertyNames);
	}

    /**
     * Creates a {@link ThresholdEvaluator} that uses Reflection to utilize an
     * integer JavaBean property as the threshold evaluation.
     */
    public static ThresholdEvaluator thresholdEvaluator(String propertyName) {
		return new BeanThresholdEvaluator(propertyName);
	}

    /**
     * Creates a {@link Comparator} that uses Reflection to compare two instances
     * of the specified {@link Class} by the given JavaBean property.  The JavaBean
     * property is compared using the provided {@link Comparator}.
     */
    public static Comparator comparator(Class className, String property, Comparator propertyComparator) {
        return ComparatorFactory.beanProperty(className, property, propertyComparator);
    }
}
