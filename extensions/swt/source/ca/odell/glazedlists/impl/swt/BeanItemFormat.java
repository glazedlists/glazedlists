/* Glazed Lists                                                 (c) 2003-2012 */
/* http://publicobject.com/glazedlists/                      publicboject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.impl.swt;

import ca.odell.glazedlists.impl.beans.BeanProperty;
import ca.odell.glazedlists.swt.DefaultItemFormat;
import ca.odell.glazedlists.swt.ItemFormat;

/**
 * An {@link ItemFormat} that uses Reflection on a specified JavaBeans property
 * to obtain a value to format a list element.
 *
 * @author Holger Brands
 */
public final class BeanItemFormat<E> implements ItemFormat<E> {

	/** The name of the JavaBean property to use */
	private String propertyName = null;

	/** The easy way to work with JavaBean-like object properties */
	private BeanProperty<E> beanProperty;

	/** The delegate {@link ItemFormat} for formatting the property value. */
	private ItemFormat delegateItemFormat;

	/**
	 * Creates a new {@link ItemFormat} that uses the string value of a JavaBean
	 * property as the formatted value of a list element. If the list element or
	 * the propery value is <code>null</code>, the emtpy string is returned.
	 *
	 * @param propertyName the JavaBean property name
	 */
	public BeanItemFormat(String propertyName) {
		this(propertyName, "");
	}

	/**
	 * Creates a new {@link ItemFormat} that uses the string value of a JavaBean
	 * property as the formatted value of a list element. If the list element or
	 * the propery value is <code>null</code>, the given value is returned.
	 *
	 * @param propertyName the JavaBean property name
	 * @param valueForNullElement
	 *            string value to be used for a <code>null</code> element or
	 *            property value
	 */
	public BeanItemFormat(String propertyName, String valueForNullElement) {
		this.propertyName = propertyName;
		delegateItemFormat = new DefaultItemFormat(valueForNullElement);
	}

	/** @inheritDoc */
	@Override
    public String format(E element) {
		if (element == null) {
			return delegateItemFormat.format(element);
		}
		if (beanProperty == null) {
			loadPropertyDescriptors(element);
		}
		final Object property = beanProperty.get(element);
		return delegateItemFormat.format(property);
	}

	/**
	 * Loads the property descriptors which are used to invoke property access
	 * methods using the property names.
	 */
	private void loadPropertyDescriptors(E beanObject) {
		final Class beanClass = beanObject.getClass();
		beanProperty = new BeanProperty<E>(beanClass, propertyName, true, false);
	}
}