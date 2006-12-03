/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package com.publicobject.misc.xml;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.impl.beans.BeanProperty;
import ca.odell.glazedlists.matchers.Matcher;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.Map;

/**
 * This factory class produces common implementations of the {@link Processor}
 * interface.
 *
 * @author James Lemieux
 */
public final class Processors {

    private Processors() {}

    /**
     * Returns a {@link Processor} that uses reflection to instantiate a new
     * Object via a no-arg constructor in the given <code>clazz</code>. It then
     * associates the new Object in the XML context with the XMLTagPath for the
     * end of the current path.
     *
     * <p>Typical usage of this Processor would be to build a new Customer
     * object each time &lt;customer&gt; is seen. The new Customer
     * objects would then be placed in the parse context Map using the
     * {@link XMLTagPath} of &lt;/customer&gt;.
     */
    public static Processor createNewObject(Class clazz) {
        return createNewObject(clazz, null, null);
    }

    /**
     * Returns a {@link Processor} that uses reflection to instantiate a new
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
    public static Processor createNewObject(Class clazz, Class[] params, Object[] args) {
        return new CreateNewObjectProcessor(clazz, params, args);
    }

    /**
     * Returns a {@link Processor} that retrieves an Object from the parse
     * context Map using the end Tag of the current XMLTagPath and then adds
     * that Object to the target {@link EventList} that is also located in the
     * parse context Map using the well known key {@link XMLTagPath#newPath()}.
     *
     * <p>Typical usage of this Processor would be to insert a newly built
     * Customer object into the target EventList each time &lt;/customer&gt;
     * is seen. The Customer object to insert would be associated with the path
     * to &lt;/customer&gt; in the parse context map.
     */
    public static Processor addObjectToTargetList() {
        return new AddObjectToTargetListProcessor();
    }

    /**
     * Returns a {@link Processor} that retrieves an object from the parse
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
    public static Processor setterMethod(Class clazz, String propertyName) {
        return setterMethod(clazz, propertyName, null);
    }

    /**
     * Functions exactly the same as {@link #setterMethod(Class, String)}
     * with the exception that the raw String value is run through a
     * {@link Converter} first to produce a more appropriate argument to the
     * setter.
     */
    public static Processor setterMethod(Class clazz, String propertyName, Converter converter) {
        return new CallSetterMethodProcessor(new BeanProperty(clazz, propertyName, false, true), converter);
    }

    /**
     * Returns a {@link Processor} that retrieves an object from the parse
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
    public static Processor addToCollection(Class clazz, String propertyName) {
        return addToCollection(clazz, propertyName, null, null);
    }

    /**
     * Functions exactly the same as {@link #addToCollection(Class, String)}
     * with the exception that the value is run through a {@link Converter}
     * first to produce a more appropriate object to add to the {@link Collection}.
     */
    public static Processor addToCollection(Class clazz, String propertyName, Converter converter) {
        return addToCollection(clazz, propertyName, converter, null);
    }

    /**
     * Functions exactly the same as {@link #addToCollection(Class, String, Converter)}
     * with the exception that the value is only added if it is matched by the
     * given <code>matcher</code>.
     */
    public static Processor addToCollection(Class clazz, String propertyName, Converter converter, Matcher matcher) {
        return new AddToCollectionProcessor(new BeanProperty(clazz, propertyName, true, false), converter, matcher);
    }

    /**
     * Find the object to operate at from the specified tag path. This is
     * naturally the object for the parent tag in the stack, but sometimes
     * it can be a higher level parent.
     */
    private static Object getSetterOwner(Map<XMLTagPath, Object> context, XMLTagPath path) {
        for(XMLTagPath key = path.parent(); key != null; key = key.parent()) {
            Object setterOwner = context.get(key.end());
            if(setterOwner != null) {
                return setterOwner;
            }
        }
        throw new IllegalStateException("No target object for path, " + path);
    }

    private static class CreateNewObjectProcessor implements Processor {
        private final Constructor constructor;
        private final Object[] args;

        public CreateNewObjectProcessor(Class clazz, Class[] params, Object[] args) {
            try {
                this.args = args;
                this.constructor = clazz.getConstructor(params);
            } catch (NoSuchMethodException e) {
                throw new RuntimeException(e);
            }
        }

        public void process(XMLTagPath path, Map<XMLTagPath, Object> context) {
            try {
                // use reflection to create a new Object and place it into the parse context Map
                context.put(path.end(), constructor.newInstance(args));
            } catch (InstantiationException e) {
                throw new RuntimeException(e);
            } catch (InvocationTargetException e) {
                throw new RuntimeException(e);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private static class AddObjectToTargetListProcessor implements Processor {
        public void process(XMLTagPath path, Map<XMLTagPath, Object> context) {
            // locate the target EventList within the parse context Map using the well known special key
            final EventList targetList = (EventList) context.get(XMLTagPath.newPath());

            // locate the newly built object keyed by the starting tag of the current XMLTagPath
            final Object o = context.get(path.end());

            // add the object to the targetList in a thread-safe manner
            targetList.getReadWriteLock().writeLock().lock();
            try {
                targetList.add(o);
            } finally {
                targetList.getReadWriteLock().writeLock().unlock();
            }
        }
    }


    private static class CallSetterMethodProcessor implements Processor {
        private final BeanProperty beanProperty;
        private final Converter converter;

        public CallSetterMethodProcessor(BeanProperty beanProperty, Converter converter) {
            this.beanProperty = beanProperty;
            this.converter = converter;
        }

        public void process(XMLTagPath path, Map<XMLTagPath, Object> context) {
            // locate the value that was just collected
            Object newValue = context.get(path.textKey());

            // if a converter has been specified, run the value through the converter
            if (converter != null)
                newValue = converter.convert(newValue.toString());

            // look up the object we will call setXXX(...) on
            final Object setterOwner = getSetterOwner(context, path);

            // call setXXX(...) on the setterOwner
            beanProperty.set(setterOwner, newValue);
        }

    }

    private static class AddToCollectionProcessor implements Processor {
        private final BeanProperty beanProperty;
        private final Converter converter;
        private final Matcher matcher;

        public AddToCollectionProcessor(BeanProperty beanProperty, Converter converter, Matcher matcher) {
            this.beanProperty = beanProperty;
            this.converter = converter;
            this.matcher = matcher;
        }

        public void process(XMLTagPath path, Map<XMLTagPath, Object> context) {
            // locate the value that was just collected
            Object newValue = context.get(path.textKey());

            // if a converter has been specified, run the value through the converter
            if (converter != null)
                newValue = converter.convert(newValue.toString());

            // if newValue doesn't pass the matcher, return early
            if (matcher != null && !matcher.matches(newValue))
                return;

            // look up the object from which we will get the List
            final Object setterOwner = getSetterOwner(context, path);

            // get the Collection
            final Collection c = (Collection) beanProperty.get(setterOwner);

            // add the value to the Collection
            c.add(newValue);
        }
    }
}