/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.impl.beans;

import ca.odell.glazedlists.ObservableElementList;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;
import java.util.EventListener;

/**
 * An {@link ObservableElementList.Connector} for the Java beans'
 * {@link PropertyChangeListener}.
 *
 * @author <a href="mailto:jesse@swank.ca">Jesse Wilson</a>
 * @author James Lemieux
 */
public class JavaBeanEventListConnector<E> implements ObservableElementList.Connector<E> {

    /** The method to use when installing a PropertyChangeListener on an object. */
    private Method addListenerMethod = null;

    /** The method to use when uninstalling a PropertyChangeListener on an object. */
    private Method removeListenerMethod = null;

    /** The list which contains the elements being observed via this {@link ObservableElementList.Connector}. */
    private ObservableElementList<E> list = null;

    /** The PropertyChangeListener to install on each list element. */
    protected PropertyChangeListener propertyChangeListener = this.createPropertyChangeListener();

    /**
     * Reflection is used to install/uninstall the {@link #propertyChangeListener}
     * on list elements, so we cache the Object[] used in the reflection call
     * for a speed increase.
     */
    private Object[] reflectionArguments = {this.propertyChangeListener};

    /**
     * Reflection is used to install/uninstall the {@link #propertyChangeListener}
     * on list elements, so we cache the Class[] used in the reflection call
     * for a speed increase.
     */
    private static final Class[] reflectionParameters = {PropertyChangeListener.class};

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
    public JavaBeanEventListConnector(Class<E> beanClass) {
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
    public JavaBeanEventListConnector(Class<E> beanClass, String addListenerMethodName, String removeListenerMethodName) {
        try {
            this.addListenerMethod = beanClass.getMethod(addListenerMethodName, reflectionParameters);
            this.removeListenerMethod = beanClass.getMethod(removeListenerMethodName, reflectionParameters);
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException("Failed to find method " + e.getMessage() + " in " + beanClass);
        }
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
    public EventListener installListener(E element) {
        try {
            this.addListenerMethod.invoke(element, this.reflectionArguments);
            return this.propertyChangeListener;
        } catch (IllegalAccessException iae) {
            throw new RuntimeException(iae);
        } catch (InvocationTargetException ite) {
            throw new RuntimeException(ite.getCause());
        }
    }

    /**
     * Start listening for PropertyChangeEvents from the specified
     * <code>element</code>. The PropertyChangeListener is installed using
     * reflection.
     *
     * @param element the element to be observed
     * @param listener the listener that was installed on the <code>element</code>
     *      in {@link #installListener(Object)}
     * @throws RuntimeException if the reflection call fails to successfully
     *      uninstall the PropertyChangeListener
     */
    public void uninstallListener(E element, EventListener listener) {
        try {
            this.removeListenerMethod.invoke(element, this.reflectionArguments);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e.getCause());
        }
    }

    /** {@inheritDoc} */
    public void setObservableElementList(ObservableElementList<E> list) {
        this.list = list;
    }

    /**
     * A local factory method to produce the PropertyChangeListener which will
     * be installed on list elements.
     */
    protected PropertyChangeListener createPropertyChangeListener() {
        return new PropertyChangeHandler();
    }

    /**
     * The PropertyChangeListener which notifies the
     * {@link ObservableElementList} within this Connector of changes to
     * list elements.
     */
    public class PropertyChangeHandler implements PropertyChangeListener {
        public void propertyChange(PropertyChangeEvent event) {
            list.elementChanged((E) event.getSource());
        }
    }
}