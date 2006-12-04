/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package com.publicobject.misc.xml;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.impl.beans.BeanProperty;
import ca.odell.glazedlists.matchers.Matcher;
import ca.odell.glazedlists.matchers.Matchers;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;

/**
 * This factory class produces common implementations of the {@link PushProcessor}
 * and {@link PopProcessor} interfaces.
 *
 * @author James Lemieux
 */
public final class Processors {

    private Processors() {}

    /**
     * Returns a {@link PushProcessor} that uses reflection to instantiate a new
     * Object via a no-arg constructor in the given <code>clazz</code>. It then
     * associates the new Object in the XML context with the XMLTagPath for the
     * end of the current path.
     *
     * <p>Typical usage of this Processor would be to build a new Customer
     * object each time &lt;customer&gt; is seen. The new Customer
     * objects would then be placed in the parse context Map using the
     * {@link XMLTagPath} of &lt;/customer&gt;.
     */
    public static <B> PushProcessor<B> createNewObject(Class<B> clazz) {
        return createNewObject(clazz, null, null);
    }

    /**
     * Returns a {@link PushProcessor} that uses reflection to instantiate a new
     * Object for the given <code>clazz</code> with arguments that match the
     * given <code>params</code> using the given <code>args</code>. It then
     * associates the new Object in the XML context with the XMLTagPath for the
     * end of the current path.
     *
     * <p>Typical usage of this Processor would be to build a new Customer
     * object each time &lt;customer&gt; is seen. The new Customer
     * objects would then be placed in the parse context Map using the
     * {@link XMLTagPath} of &lt;/customer&gt;.
     */
    public static <B> PushProcessor<B> createNewObject(Class<B> clazz, Class[] params, Object[] args) {
        return new CreateNewObjectProcessor<B>(clazz, params, args);
    }

    /**
     * Returns a {@link PopProcessor} that takes the active object and adds it
     * to a collection from the current context object.
     *
     * <p>Typical usage of this Processor would be to insert a newly built
     * Customer object into the target EventList each time &lt;/customer&gt;
     * is seen. The Customer object to insert would be associated with the path
     * to &lt;/customer&gt; in the parse context map.
     */
    public static <B> PopProcessor<EventList<B>, B> addObjectToTargetList() {
        return new AddObjectToTargetListProcessor<B>();
    }

    /**
     * Returns a {@link PopProcessor} that retrieves an object from the parse
     * context Map using the end tag of the parent XMLTagPath, which is
     * assumed to be an instance of the given <code>clazz</code>. It then uses
     * reflection to call a setter method for the given
     * <code>propertyName</code> using the object at the end tag for the current
     * XMLTagPath within the parse context Map.
     *
     * <p>Typical usage of this Processor would be to set the name property of
     * a Customer object when &lt;/name&gt; is seen. The parent XMLTagPath would
     * be the &lt;/customer&gt; and would be used to fetch the
     * partially built Customer object from the parse context Map. That Customer
     * would then have setName(...) called on it with the String value associated
     * to the current XMLTagPath, &lt;/name&gt;.
     */
    public static <B,V> PopProcessor<B,V> setterMethod(Class<B> clazz, String propertyName) {
        return setterMethod(clazz, propertyName, Converters.<V>identityConverter());
    }

    /**
     * Functions exactly the same as {@link #setterMethod(Class, String)}
     * with the exception that the raw String value is run through a
     * {@link Converter} first to produce a more appropriate argument to the
     * setter.
     */
    public static <B,O,C> PopProcessor<B,O> setterMethod(Class<B> clazz, String propertyName, Converter<O,C> converter) {
        return new CallSetterMethodProcessor<B,O,C>(new BeanProperty<B>(clazz, propertyName, false, true), converter);
    }

    /**
     * Returns a {@link PopProcessor} that retrieves an object from the parse
     * context Map using the end tag of the parent XMLTagPath, which is
     * assumed to be an instance of the given <code>clazz</code>. It then uses
     * reflection to call a getter method for the given
     * <code>propertyName</code> and assumes that the return type is some sort
     * of {@link Collection}. The object at the end tag for the current
     * XMLTagPath within the parse context Map is added to that {@link Collection}.
     *
     * <p>Typical usage of this Processor would be to add each Item object
     * to a Collection owned by the object, e.g. an Order object, when
     * &lt;/item&gt; is seen. The parent XMLTagPath would be the &lt;/order&gt;
     * and would be used to fetch the partially built Order object from the
     * parse context Map. That Order would then have
     *
     * <p>Collection getItems()
     *
     * <p>called on it to retrieve the Collection of Item objects, and then
     * the latest Item object would be added to that Collection.
     */
    public static <B,V> PopProcessor<B,V> addToCollection(Class<B> clazz, String propertyName) {
        return addToCollection(clazz, propertyName, Converters.<V>identityConverter(), Matchers.<V>trueMatcher());
    }

    /**
     * Functions exactly the same as {@link #addToCollection(Class, String)}
     * with the exception that the value is run through a {@link Converter}
     * first to produce a more appropriate object to add to the {@link Collection}.
     */
    public static <B,V> PopProcessor<B,V> addToCollection(Class<B> clazz, String propertyName, Converter<V,V> converter) {
        return addToCollection(clazz, propertyName, converter, Matchers.<V>trueMatcher());
    }

    /**
     * Functions exactly the same as {@link #addToCollection(Class, String, Converter)}
     * with the exception that the value is only added if it is matched by the
     * given <code>matcher</code>.
     */
    public static <B,V> PopProcessor<B,V> addToCollection(Class<B> clazz, String propertyName, Converter<V,V> converter, Matcher<V> matcher) {
        return new AddToCollectionProcessor<B,V>(new BeanProperty<B>(clazz, propertyName, true, false), converter, matcher);
    }

    private static class CreateNewObjectProcessor<T> implements PushProcessor<T> {
        private final Constructor<T> constructor;
        private final Object[] args;

        public CreateNewObjectProcessor(Class<T> clazz, Class[] params, Object[] args) {
            try {
                this.args = args;
                this.constructor = clazz.getConstructor(params);
            } catch (NoSuchMethodException e) {
                throw new RuntimeException(e);
            }
        }

        public T evaluate() {
            try {
                return constructor.newInstance(args);
            } catch (InstantiationException e) {
                throw new RuntimeException(e);
            } catch (InvocationTargetException e) {
                throw new RuntimeException(e);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private static class AddObjectToTargetListProcessor<T> implements PopProcessor<EventList<T>,T> {
        public void process(EventList<T> baseObject, T value) {
            // add the object to the targetList in a thread-safe manner
            baseObject.getReadWriteLock().writeLock().lock();
            try {
                baseObject.add(value);
            } finally {
                baseObject.getReadWriteLock().writeLock().unlock();
            }
        }
    }

    private static class CallSetterMethodProcessor<T,O,C> implements PopProcessor<T,O> {
        private final BeanProperty<T> beanProperty;
        private final Converter<O,C> converter;

        public CallSetterMethodProcessor(BeanProperty<T> beanProperty, Converter<O,C> converter) {
            if(beanProperty == null) throw new IllegalArgumentException();
            if(converter == null) throw new IllegalArgumentException();
            this.beanProperty = beanProperty;
            this.converter = converter;
        }

        public void process(T baseObject, O value) {
            // if a converter has been specified, run the value through the converter
            C convertedValue = converter.convert(value);

            // call setXXX(...) on the setterOwner
            beanProperty.set(baseObject, convertedValue);
        }
    }

    private static class AddToCollectionProcessor<T,V> implements PopProcessor<T,V> {
        private final BeanProperty<T> beanProperty;
        private final Converter<V,V> converter;
        private final Matcher<V> matcher;

        public AddToCollectionProcessor(BeanProperty<T> beanProperty, Converter<V,V> converter, Matcher<V> matcher) {
            if(beanProperty == null) throw new IllegalArgumentException();
            if(converter == null) throw new IllegalArgumentException();
            if(matcher == null) throw new IllegalArgumentException();
            this.beanProperty = beanProperty;
            this.converter = converter;
            this.matcher = matcher;
        }


        public void process(T baseObject, V value) {
            // if a converter has been specified, run the value through the converter
            value = converter.convert(value);

            // if element doesn't pass the matcher, return early
            if (!matcher.matches(value)) return;

            // get the Collection
            final Collection<V> c = (Collection<V>) beanProperty.get(baseObject);

            // add the value to the Collection
            c.add(value);
        }
    }
}