/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.impl.beans;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.ArrayList;
import java.util.List;

/**
 * Models a getter and setter for an abstract property.
 *
 * @author <a href="mailto:jesse@swank.ca">Jesse Wilson</a>
 */
public class BeanProperty<T> {

    /** the target class */
    private final Class<T> beanClass;
    /** the property name */
    private final String propertyName;
    
    /** the value class */
    private Class valueClass = null;

    /** the chain of methods for the getter */
    private List<Method> getterChain = null;

    /** the chain of methods for the setter */
    private List<Method> setterChain = null;

    /** commonly used paramters */
    private static final Object[] EMPTY_ARGUMENTS = new Object[0];
    private static final Class[] EMPTY_PARAMETER_TYPES = new Class[0];

    /**
     * Creates a new {@link BeanProperty} that gets the specified property from the
     * specified class.
     */
    public BeanProperty(Class<T> beanClass, String propertyName, boolean readable, boolean writable) {
        if (beanClass == null)
            throw new IllegalArgumentException("beanClass may not be null");
        if (propertyName == null)
            throw new IllegalArgumentException("propertyName may not be null");

        this.beanClass = beanClass;
        this.propertyName = propertyName;

        // look up the common chain
        String[] propertyParts = propertyName.split("\\.");
        List<Method> commonChain = new ArrayList<Method>();
        Class currentClass = beanClass;
        for(int p = 0; p < propertyParts.length - 1; p++) {
            Method partGetter = findGetterMethod(currentClass, propertyParts[p]);
            commonChain.add(partGetter);
            currentClass = partGetter.getReturnType();
        }

        // look up the final getter
        if(readable) {
            getterChain = new ArrayList<Method>();
            getterChain.addAll(commonChain);
            Method lastGetter = findGetterMethod(currentClass, propertyParts[propertyParts.length - 1]);
            getterChain.add(lastGetter);
            valueClass = lastGetter.getReturnType();
        }

        // look up the final setter
        if(writable) {
            setterChain = new ArrayList<Method>();
            setterChain.addAll(commonChain);
            Method lastSetter = findSetterMethod(currentClass, propertyParts[propertyParts.length - 1]);
            setterChain.add(lastSetter);
            if(valueClass == null) valueClass = lastSetter.getParameterTypes()[0];
        }
    }

    /**
     * Finds a getter of the specified property on the specified class.
     */
    private Method findGetterMethod(Class targetClass, String property) {
        Method result;

        Class currentClass = targetClass;
        while(currentClass != null) {
            String getProperty = "get" + capitalize(property);
            result = getMethod(currentClass, getProperty, EMPTY_PARAMETER_TYPES);
            if(result != null) {
                validateGetter(result);
                return result;
            }

            String isProperty = "is" + capitalize(property);
            result = getMethod(currentClass, isProperty, EMPTY_PARAMETER_TYPES);
            if(result != null) {
                validateGetter(result);
                return result;
            }
            currentClass = currentClass.getSuperclass();
        }

        throw new IllegalArgumentException("Failed to find getter for property \"" + property + "\" of " + targetClass);
    }

    /**
     * Finds a setter of the specified property on the specified class.
     */
    private Method findSetterMethod(Class targetClass, String property) {
        String setProperty = "set" + capitalize(property);

        // loop through the class and its superclasses
        Class currentClass = targetClass;
        while(currentClass != null) {

            // loop through this class' methods
            Method[] classMethods = currentClass.getMethods();
            for(int m = 0; m < classMethods.length; m++) {
                if(!classMethods[m].getName().equals(setProperty)) continue;
                if(classMethods[m].getParameterTypes().length != 1) continue;
                validateSetter(classMethods[m]);
                return classMethods[m];
            }
            currentClass = currentClass.getSuperclass();
        }

        throw new IllegalArgumentException("Failed to find setter for property \"" + property + "\" of " + targetClass);
    }

    /**
     * Validates that the specified method is okay for reflection. This throws an
     * exception if the method is invalid.
     */
    private void validateGetter(Method method) {
        if(!Modifier.isPublic(method.getModifiers())) {
            throw new IllegalArgumentException("Getter \"" + method + "\" is not public");
        }

        if(Void.TYPE.equals(method.getReturnType())) {
            throw new IllegalArgumentException("Getter \"" + method + "\" returns void");
        }

        if(method.getParameterTypes().length != 0) {
            throw new IllegalArgumentException("Getter \"" + method + "\" has too many parameters; expected 0 but found " + method.getParameterTypes().length);
        }
    }

    /**
     * Validates that the specified method is okay for reflection. This throws an
     * exception if the method is invalid.
     */
    private void validateSetter(Method method) {
        if(!Modifier.isPublic(method.getModifiers())) {
            throw new IllegalArgumentException("Setter \"" + method + "\" is not public");
        }

        if(method.getParameterTypes().length != 1) {
            throw new IllegalArgumentException("Setter \"" + method + "\" takes the wrong number of parameters; expected 1 but found " + method.getParameterTypes().length);
        }
    }


    /**
     * Returns the specified property with a capitalized first character.
     */
    private String capitalize(String property) {
        StringBuffer result = new StringBuffer();
        result.append(Character.toUpperCase(property.charAt(0)));
        result.append(property.substring(1));
        return result.toString();
    }

    /**
     * Gets the method with the specified name and arguments.
     */
    private Method getMethod(Class targetClass, String methodName, Class[] parameterTypes) {
        try {
            return targetClass.getMethod(methodName, parameterTypes);
        } catch(NoSuchMethodException e) {
            return null;
        }
    }

    /**
     * Gets the base class that this getter accesses.
     */
    public Class<T> getBeanClass() {
         return beanClass;
    }

    /**
     * Gets the name of the property that this getter extracts.
     */
    public String getPropertyName() {
         return propertyName;
    }

    /**
     * Gets the class of the property's value. This is the return type and not
     * necessarily the runtime type of the class.
     */
    public Class getValueClass() {
        return valueClass;
    }

    /**
     * Gets whether this property can get get.
     */
    public boolean isReadable() {
        return (getterChain != null);
    }

    /**
     * Gets whether this property can be set.
     */
    public boolean isWritable() {
        return (setterChain != null);
    }

    /**
     * Gets the value of this property for the specified Object.
     */
    public Object get(T member) {
        if(!isReadable()) throw new IllegalStateException("Property " + propertyName + " of " + beanClass + " not readable");

        try {
            // do all the getters in sequence
            Object currentMember = member;
            for(int i = 0, n = getterChain.size(); i < n; i++) {
                Method currentMethod = getterChain.get(i);
                currentMember = currentMethod.invoke(currentMember, EMPTY_ARGUMENTS);
                if(currentMember == null) return null;
            }

            // return the result of the last getter
            return currentMember;
        } catch(IllegalAccessException e) {
            SecurityException se = new SecurityException();
            se.initCause(e);
            throw se;
        } catch(InvocationTargetException e) {
            throw new UndeclaredThrowableException(e.getCause());
        }
    }

    /**
     * Gets the value of this property for the specified Object.
     */
    public Object set(T member, Object newValue) {
        if(!isWritable()) throw new IllegalStateException("Property " + propertyName + " of " + beanClass + " not writable");

        try {
            // everything except the last setter chain element is a getter
            Object currentMember = member;
            for(int i = 0, n = setterChain.size() - 1; i < n; i++) {
                Method currentMethod = setterChain.get(i);
                currentMember = currentMethod.invoke(currentMember, EMPTY_ARGUMENTS);
                if(currentMember == null) return null;
            }

            // do the remaining setter
            Method setterMethod = setterChain.get(setterChain.size() - 1);
            return setterMethod.invoke(currentMember, new Object[] { newValue });
        } catch(IllegalAccessException e) {
            SecurityException se = new SecurityException();
            se.initCause(e);
            throw se;
        } catch(InvocationTargetException e) {
            throw new UndeclaredThrowableException(e.getCause());
        }
    }

    /** {@inheritDoc} */
    public boolean equals(Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;

        final BeanProperty that = (BeanProperty) o;

        if(!beanClass.equals(that.beanClass)) return false;
        if(!propertyName.equals(that.propertyName)) return false;

        return true;
    }

    /** {@inheritDoc} */
    public int hashCode() {
        int result;
        result = beanClass.hashCode();
        result = 29 * result + propertyName.hashCode();
        return result;
    }
}