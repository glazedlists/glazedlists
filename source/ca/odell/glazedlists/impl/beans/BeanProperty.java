/**
 * Glazed Lists
 * http://glazedlists.dev.java.net/
 *
 * COPYRIGHT 2003 O'DELL ENGINEERING LTD.
 */
package ca.odell.glazedlists.impl.beans;

import java.util.*;
import java.lang.reflect.*;

/**
 * Models a getter and setter for an abstract property.
 *
 * @author <a href="mailto:jesse@odel.on.ca">Jesse Wilson</a>
 */
public class BeanProperty {

    /** the target class */
    private Class memberClass = null;
    /** the property name */
    private String propertyName = null;

    /** the chain of methods for the getter */
    private List getterChain = null;

    /** the chain of methods for the setter */
    private List setterChain = null;

    /** commonly used paramters */
    private static Object[] EMPTY_ARGUMENTS = new Object[0];
    private static Class[] EMPTY_PARAMETER_TYPES = new Class[0];

    /**
     * Creates a new {@link BeanProperty} that gets the specified property from the
     * specified class.
     */
    public BeanProperty(Class memberClass, String propertyName, boolean readable, boolean writable) {
        this.memberClass = memberClass;
        this.propertyName = propertyName;

        // look up the common chain
        String[] propertyParts = propertyName.split("\\.");
        List commonChain = new ArrayList();
        Class currentClass = memberClass;
        for(int p = 0; p < propertyParts.length - 1; p++) {
            Method partGetter = findGetterMethod(currentClass, propertyParts[p]);
            commonChain.add(partGetter);
            currentClass = partGetter.getReturnType();
        }

        // look up the final getter
        if(readable) {
            getterChain = new ArrayList();
            getterChain.addAll(commonChain);
            getterChain.add(findGetterMethod(currentClass, propertyParts[propertyParts.length - 1]));
        }

        // look up the final setter
        if(writable) {
            setterChain = new ArrayList();
            setterChain.addAll(commonChain);
            setterChain.add(findSetterMethod(currentClass, propertyParts[propertyParts.length - 1]));
        }
    }

    /**
     * Finds a getter of the specified property on the specified class.
     */
    private Method findGetterMethod(Class targetClass, String property) {
        Method result = null;

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

        throw new IllegalArgumentException("Failed to find getter for property \"" + property + "\" of class " + targetClass);
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

        throw new IllegalArgumentException("Failed to find setter for property \"" + property + "\" of class " + targetClass);
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
            throw new IllegalArgumentException("Getter \"" + method + "\" is void");
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
            throw new IllegalArgumentException("Setter \"" + method + "\" does not take one paramter");
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
    public Class getPropertyClass() {
         return memberClass;
    }

    /**
     * Gets the name of the property that this getter extracts.
     */
    public String getPropertyName() {
         return propertyName;
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
    public Object get(Object member) {
        if(!isReadable()) throw new IllegalStateException("Property " + propertyName + " of " + memberClass + " not readable");

        try {
            // do all the getters in sequence
            Object currentMember = member;
            for(int i = 0; i < getterChain.size(); i++) {
                Method currentMethod = (Method)getterChain.get(i);
                currentMember = currentMethod.invoke(currentMember, EMPTY_ARGUMENTS);
                if(currentMember == null) return null;
            }

            // return the result of the last getter
            return currentMember;
        } catch(IllegalAccessException e) {
            throw new SecurityException(e.getMessage());
        } catch(InvocationTargetException e) {
            throw new UndeclaredThrowableException(e.getCause());
        }
    }

    /**
     * Gets the value of this property for the specified Object.
     */
    public Object set(Object member, Object newValue) {
        if(!isWritable()) throw new IllegalStateException("Property " + propertyName + " of " + memberClass + " not writable");

        try {
            // everything except the last setter chain element is a getter
            Object currentMember = member;
            for(int i = 0; i < setterChain.size() - 1; i++) {
                Method currentMethod = (Method)setterChain.get(i);
                currentMember = currentMethod.invoke(currentMember, EMPTY_ARGUMENTS);
                if(currentMember == null) return null;
            }

            // do the remaining setter
            Method setterMethod = (Method)setterChain.get(setterChain.size() - 1);
            Object result = setterMethod.invoke(currentMember, new Object[] { newValue });
            return result;
        } catch(IllegalAccessException e) {
            throw new SecurityException(e.getMessage());
        } catch(InvocationTargetException e) {
            throw new UndeclaredThrowableException(e.getCause());
        }
    }
}
