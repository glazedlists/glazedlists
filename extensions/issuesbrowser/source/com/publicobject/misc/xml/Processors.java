/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package com.publicobject.misc.xml;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.impl.beans.BeanProperty;

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
     * associates the new Object in the XML context with the given
     * <code>valueObjectKey</code>.
     *
     * <p>Typical usage of this Processor would be to build a new Customer
     * object each time &lt;customer&gt; is seen. The new Customer
     * objects would then be placed in the parse context Map using the
     * {@link XMLTagPath} of &lt;customer&gt;.
     */
    public static Processor createNewObject(Class clazz, XMLTagPath valueObjectKey) {
        return new CreateNewObjectProcessor(clazz, valueObjectKey);
    }

    /**
     * Returns a {@link Processor} that retrieves an Object from the parse
     * context Map using the given <code>valueObjectKey</code> and then adds
     * that Object to the target {@link EventList} that is also located in the
     * parse context Map using the well known key {@link XMLTagPath#newPath()}.
     *
     * <p>Typical usage of this Processor would be to insert a newly build
     * Customer object into the target EventList each time &lt;/customer&gt;
     * is seen. The Customer object to insert would be associated with the path
     * to the &lt;customer&gt;.
     */
    public static Processor addObjectToTargetList(XMLTagPath valueObjectKey) {
        return new AddObjectToTargetListProcessor(valueObjectKey);
    }

    /**
     * Returns a {@link Processor} that retrieves an object from the parse
     * context Map using the given <code>setterObjectKey</code>, which is
     * assumed to be an instance of the given <code>clazz</code>. It then uses
     * reflection to call a setter method for the given
     * <code>propertyName</code> using the object at the given
     * <code>valueObjectKey</code> within the parse context Map.
     *
     * <p>Typical usage of this Processor would be to set the name property of
     * a Customer object when &lt;/name&gt; is seen. The setterObjectKey would
     * be the XMLTagPath of &lt;customer&gt; and would be used to fetch the
     * partially built Customer object from the parse context Map. That Customer
     * would then have setName(...) called on it with the String value associated
     * to the current XMLTagPath.
     */
    public static Processor setterMethod(XMLTagPath setterObjectKey, XMLTagPath valueObjectKey, Class clazz, String propertyName) {
        return setterMethod(setterObjectKey, valueObjectKey, clazz, propertyName, null);
    }

    /**
     * Functions exactly the same as {@link #setterMethod(XMLTagPath, XMLTagPath, Class, String)}
     * with the exception that the raw String value assocaited to the given
     * <code>valueObjectKey</code> is run through a {@link Converter} first to
     * produce a more appropriate argument to the setter.
     */
    public static Processor setterMethod(XMLTagPath setterObjectKey, XMLTagPath valueObjectKey, Class clazz, String propertyName, Converter converter) {
        return new CallSetterMethodProcessor(setterObjectKey, valueObjectKey, new BeanProperty(clazz, propertyName, true, true), converter);
    }

    private static class CreateNewObjectProcessor implements Processor {
        private final Class clazz;
        private final XMLTagPath valueObjectKey;

        public CreateNewObjectProcessor(Class clazz, XMLTagPath valueObjectKey) {
            this.clazz = clazz;
            this.valueObjectKey = valueObjectKey;
        }

        public void process(XMLTagPath path, Map<XMLTagPath, Object> context) {
            try {
                // use reflection to create a new Object and place it into the parse context Map
                context.put(valueObjectKey, clazz.newInstance());
            } catch (InstantiationException e) {
                throw new RuntimeException(e);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private static class AddObjectToTargetListProcessor implements Processor {
        private final XMLTagPath valueObjectKey;

        public AddObjectToTargetListProcessor(XMLTagPath valueObjectKey) {
            this.valueObjectKey = valueObjectKey;
        }

        public void process(XMLTagPath path, Map<XMLTagPath, Object> context) {
            // locate the target EventList within the parse context Map using the well known special key
            final EventList targetList = (EventList) context.get(XMLTagPath.newPath());

            // locate the newly built object keyed by the starting tag of the current XMLTagPath
            final Object o = context.get(valueObjectKey);

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
        private final XMLTagPath setterObjectKey;
        private final XMLTagPath valueObjectKey;
        private final BeanProperty beanProperty;
        private final Converter converter;

        public CallSetterMethodProcessor(XMLTagPath setterObjectKey, XMLTagPath valueObjectKey, BeanProperty beanProperty, Converter converter) {
            this.setterObjectKey = setterObjectKey;
            this.valueObjectKey = valueObjectKey;
            this.beanProperty = beanProperty;
            this.converter = converter;
        }

        public void process(XMLTagPath path, Map<XMLTagPath, Object> context) {
            // locate the String value that was just collected
            Object newValue = context.get(valueObjectKey);

            // if a converter has been specified, run the value through the converter
            if (converter != null)
                newValue = converter.convert(newValue.toString());

            // look up the object we will call setXXX(...) on
            final Object setterOwner = context.get(setterObjectKey);

            // call setXXX(...) on the setterOwner
            beanProperty.set(setterOwner, newValue);
        }
    }
}