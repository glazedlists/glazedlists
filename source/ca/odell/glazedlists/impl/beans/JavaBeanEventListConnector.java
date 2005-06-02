/* Glazed Lists                                                 (c) 2003-2005 */
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
 * An {@link ca.odell.glazedlists.ObservableElementList.Connector} for the Java beans'
 * {@link PropertyChangeListener}.
 *
 * @author <a href="mailto:jesse@swank.ca">Jesse Wilson</a>
 */
public class JavaBeanEventListConnector implements ObservableElementList.Connector, PropertyChangeListener {
    private Method addListener = null;
    private Method removeListener = null;
    private ObservableElementList list = null;

    public JavaBeanEventListConnector(Class beanClass, String addListener, String removeListener) {
        try {
            this.addListener = beanClass.getMethod(addListener, new Class[] { PropertyChangeListener.class });
            this.removeListener = beanClass.getMethod(removeListener, new Class[] { PropertyChangeListener.class });
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException("Failed to find method, " + e.getMessage());
        }
    }

    public JavaBeanEventListConnector(Class beanClass) {
        Method[] methods = beanClass.getMethods();
        for(int m = 0; m < methods.length; m++) {
            if(methods[m].getParameterTypes().length != 1) continue;
            if(methods[m].getParameterTypes()[0] != PropertyChangeListener.class) continue;
            if(methods[m].getName().startsWith("add")) addListener = methods[m];
            if(methods[m].getName().startsWith("remove")) removeListener = methods[m];
        }
        if(addListener == null || removeListener == null) throw new IllegalArgumentException("Couldn't find listener methods for " + beanClass.getName());
    }

    public EventListener installListener(Object target) {
        try {
            addListener.invoke(target, new Object[] { this });
            return this;
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e.getCause());
        }
    }

    public void uninstallListener(Object target, EventListener listener) {
        try {
            removeListener.invoke(target, new Object[] { this });
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e.getCause());
        }
    }

    public void setObservableElementList(ObservableElementList list) {
        this.list = list;
    }

    public void propertyChange(PropertyChangeEvent event) {
        list.elementChanged(event.getSource());
    }
}