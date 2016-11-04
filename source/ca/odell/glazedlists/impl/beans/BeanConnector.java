/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.impl.beans;

import ca.odell.glazedlists.ObservableElementChangeHandler;
import ca.odell.glazedlists.ObservableElementList;
import ca.odell.glazedlists.matchers.Matcher;
import ca.odell.glazedlists.matchers.Matchers;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.EventListener;

/**
 * An {@link ObservableElementList.Connector} for the Java beans'
 * {@link PropertyChangeListener}.
 *
 * @author <a href="mailto:jesse@swank.ca">Jesse Wilson</a>
 * @author James Lemieux
 */
public class BeanConnector<E> implements ObservableElementList.Connector<E> {

    /** The method to use when installing a PropertyChangeListener on an object. */
    private Method addListenerMethod;

    /** The method to use when uninstalling a PropertyChangeListener on an object. */
    private Method removeListenerMethod;

    /** The list which contains the elements being observed via this {@link ObservableElementList.Connector}. */
    private ObservableElementChangeHandler<? extends E> list;

    /** The PropertyChangeListener to install on each list element. */
    protected PropertyChangeListener propertyChangeListener = this.createPropertyChangeListener();

    /** Matches PropertyChangeEvents to deliver to the ObservableElementList. */
    private Matcher<PropertyChangeEvent> eventMatcher = Matchers.trueMatcher();

    /**
     * Reflection is used to install/uninstall the {@link #propertyChangeListener}
     * on list elements, so we cache the Object[] used in the reflection call
     * for a speed increase.
     */
    private final Object[] reflectionParameters = {propertyChangeListener};

    /**
     * The types taken by the methods which add and remove PropertyChangeListeners.
     */
    private static final Class[] REFLECTION_TYPES = {PropertyChangeListener.class};

    /**
     * Constructs a new Connector which uses reflection to add and remove a
     * PropertyChangeListener from instances of the <code>beanClass</code>. The
     * methods for adding and removing PropertyChangeListener from instances of
     * the given <code>beanClass</code> are assumed to follow the naming
     * convention:
     *
     * <ul>
     *  <li> add*(PropertyChangeListener) to add PropertyChangeListeners
     *  <li> remove*(PropertyChangeListener) to remove PropertyChangeListeners
     * </ul>
     *
     * where the * may be replaced with any string of valid java identifier
     * characters.
     *
     * @param beanClass the Class of all list elements within the {@link ObservableElementList}
     * @throws IllegalArgumentException if <code>beanClass</code> does not contain methods
     *      matching the format described
     */
    public BeanConnector(Class<E> beanClass) {
        final Method[] methods = beanClass.getMethods();
        for (int m = 0; m < methods.length; m++) {
            if(methods[m].getParameterTypes().length != 1) continue;
            if(methods[m].getParameterTypes()[0] != PropertyChangeListener.class) continue;
            if(methods[m].getName().startsWith("add")) this.addListenerMethod = methods[m];
            if(methods[m].getName().startsWith("remove")) this.removeListenerMethod = methods[m];
        }

        if (this.addListenerMethod == null || this.removeListenerMethod == null)
            throw new IllegalArgumentException("Couldn't find listener methods for " + beanClass.getName());
    }

    /**
     * Constructs a new Connector which uses reflection to add and remove a
     * PropertyChangeListener from instances of the <code>beanClass</code>. The
     * methods for adding and removing PropertyChangeListener from instances of
     * the given <code>beanClass</code> are assumed to follow the naming
     * convention:
     *
     * <ul>
     *  <li> add*(PropertyChangeListener) to add PropertyChangeListeners
     *  <li> remove*(PropertyChangeListener) to remove PropertyChangeListeners
     * </ul>
     *
     * where the * may be replaced with any string of valid java identifier
     * characters.
     *
     * @param beanClass the Class of all list elements within the {@link ObservableElementList}
     * @param eventMatcher the matcher for matching those PropertyChangeEvents, which should be
     *        delivered to the ObservableElementList
     * @throws IllegalArgumentException if <code>beanClass</code> does not contain methods
     *      matching the format described
     */
    public BeanConnector(Class<E> beanClass, Matcher<PropertyChangeEvent> eventMatcher) {
        this(beanClass);
        setEventMatcher(eventMatcher);
    }


    /**
     * Constructs a new Connector which uses reflection to add and remove a
     * PropertyChangeListener from instances of the <code>beanClass</code>
     * using the named methods.
     *
     * @param beanClass the Class of all list elements within the {@link ObservableElementList}
     * @param addListenerMethodName the name of the method which adds PropertyChangeListeners
     *      to the elements within the {@link ObservableElementList}
     * @param removeListenerMethodName the name of the method which removes PropertyChangeListeners
     *      from the elements within the {@link ObservableElementList}
     * @throws IllegalArgumentException if <code>beanClass</code> does not contain the named
     *      methods or if the methods do no take a PropertyChangeListener as the single parameter
     */
    public BeanConnector(Class<E> beanClass, String addListenerMethodName, String removeListenerMethodName) {
        try {
            this.addListenerMethod = beanClass.getMethod(addListenerMethodName, REFLECTION_TYPES);
            this.removeListenerMethod = beanClass.getMethod(removeListenerMethodName, REFLECTION_TYPES);
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException("Failed to find method " + e.getMessage() + " in " + beanClass);
        }
    }

    /**
     * Constructs a new Connector which uses reflection to add and remove a PropertyChangeListener
     * from instances of the <code>beanClass</code> using the named methods.
     *
     * @param beanClass the Class of all list elements within the {@link ObservableElementList}
     * @param addListenerMethodName the name of the method which adds PropertyChangeListeners to the
     *        elements within the {@link ObservableElementList}
     * @param removeListenerMethodName the name of the method which removes PropertyChangeListeners
     *        from the elements within the {@link ObservableElementList}
     * @param eventMatcher the matcher for matching those PropertyChangeEvents, which should be
     *        delivered to the ObservableElementList
     * @throws IllegalArgumentException if <code>beanClass</code> does not contain the named
     *         methods or if the methods do no take a PropertyChangeListener as the single parameter
     */
    public BeanConnector(Class<E> beanClass, String addListenerMethodName,
            String removeListenerMethodName, Matcher<PropertyChangeEvent> eventMatcher) {
        this(beanClass, addListenerMethodName, removeListenerMethodName);
        setEventMatcher(eventMatcher);
    }

    /**
     * Start listening for PropertyChangeEvents from the specified
     * <code>element</code>. The PropertyChangeListener is installed using
     * reflection.
     *
     * @param element the element to be observed
     * @return the listener that was installed on the <code>element</code>
     *      to be used as a parameter to {@link #uninstallListener(Object, EventListener)}
     * @throws RuntimeException if the reflection call fails to successfully
     *      install the PropertyChangeListener
     */
    @Override
    public EventListener installListener(E element) {
        try {
            this.addListenerMethod.invoke(element, this.reflectionParameters);
            return this.propertyChangeListener;
        } catch (IllegalAccessException iae) {
            throw new RuntimeException(iae);
        } catch (InvocationTargetException ite) {
            throw new RuntimeException(ite.getCause());
        }
    }

    /**
     * Stop listening for PropertyChangeEvents from the specified
     * <code>element</code>. The PropertyChangeListener is uninstalled using
     * reflection.
     *
     * @param element the observed element
     * @param listener the listener that was installed on the <code>element</code>
     *      in {@link #installListener(Object)}
     * @throws RuntimeException if the reflection call fails to successfully
     *      uninstall the PropertyChangeListener
     */
    @Override
    public void uninstallListener(E element, EventListener listener) {
        try {
            this.removeListenerMethod.invoke(element, this.reflectionParameters);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e.getCause());
        }
    }

    /** {@inheritDoc} */
    @Override
    public void setObservableElementList(ObservableElementChangeHandler<? extends E> list) {
        this.list = list;
    }

    /**
     * Returns the event matcher. It matches those PropertyChangeEvents, which should be delivered
     * to the ObservableElementList. In other words, it serves as a filter for PropertyChangeEvents.
     */
    public final Matcher<PropertyChangeEvent> getEventMatcher() {
        return eventMatcher;
    }

    /**
     * Sets the event matcher, may not be <code>null</code>. It matches those
     * PropertyChangeEvents, which should be delivered to the ObservableElementList. In other words,
     * it serves as a filter for PropertyChangeEvents.
     */
    private void setEventMatcher(Matcher<PropertyChangeEvent> eventMatcher) {
        if (eventMatcher == null) throw new IllegalArgumentException("Event matcher may not be null.");
        this.eventMatcher = eventMatcher;
    }

    /**
     * A local factory method to produce the PropertyChangeListener which will
     * be installed on list elements.
     */
    protected PropertyChangeListener createPropertyChangeListener() {
        return new PropertyChangeHandler();
    }

    /**
     * The PropertyChangeListener which notifies the {@link ObservableElementList} within this
     * Connector of changes to list elements.
     */
    public class PropertyChangeHandler implements PropertyChangeListener {
        @Override
        public void propertyChange(PropertyChangeEvent event) {
            if (getEventMatcher().matches(event)) {
            	list.elementChanged(event.getSource());
            }
        }
    }
}