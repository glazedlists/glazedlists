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

import ca.odell.glazedlists.impl.reflect.J2SE50ReturnTypeResolver;
import ca.odell.glazedlists.impl.reflect.ReturnTypeResolver;

/**
 * Models a getter and setter for an abstract property.
 *
 * @author <a href="mailto:jesse@swank.ca">Jesse Wilson</a>
 */
public class BeanProperty<T> {

    private static final ReturnTypeResolver TYPE_RESOLVER = new J2SE50ReturnTypeResolver();

    /** the target class */
    private final Class<T> beanClass;
    /** the property name */
    private final String propertyName;

    /** <tt>true</tt> indicates the getter should simply reflect the value it is given */
    private final boolean identityProperty;

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
        if (propertyName.length() == 0)
            throw new IllegalArgumentException("propertyName may not be empty");

        this.beanClass = beanClass;
        this.propertyName = propertyName;
        this.identityProperty = "this".equals(propertyName);

        if (identityProperty && writable)
            throw new IllegalArgumentException("The identity property name (this) cannot be writable");

        // look up the common chain
        final String[] propertyParts = propertyName.split("\\.");
        final List<Method> commonChain = new ArrayList<Method>(propertyParts.length);
        Class currentClass = beanClass;
        for(int p = 0; p < propertyParts.length - 1; p++) {
            Method partGetter = findGetterMethod(currentClass, propertyParts[p]);
            commonChain.add(partGetter);
            currentClass = TYPE_RESOLVER.getReturnType(currentClass, partGetter);
        }

        // look up the final getter
        if(readable) {
            if (identityProperty) {
                valueClass = beanClass;
            } else {
                getterChain = new ArrayList<Method>();
                getterChain.addAll(commonChain);
                Method lastGetter = findGetterMethod(currentClass, propertyParts[propertyParts.length - 1]);
                getterChain.add(lastGetter);
                valueClass = TYPE_RESOLVER.getReturnType(currentClass, lastGetter);
            }
        }

        // look up the final setter
        if(writable) {
            setterChain = new ArrayList<Method>();
            setterChain.addAll(commonChain);
            Method lastSetter = findSetterMethod(currentClass, propertyParts[propertyParts.length - 1]);
            setterChain.add(lastSetter);
            if(valueClass == null) valueClass = TYPE_RESOLVER.getFirstParameterType(currentClass, lastSetter);
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
        return getterChain != null || identityProperty;
    }

    /**
     * Gets whether this property can be set.
     */
    public boolean isWritable() {
        return setterChain != null;
    }

    /**
     * Gets the value of this property for the specified Object.
     */
    public Object get(T member) {
        if(!isReadable()) throw new IllegalStateException("Property " + propertyName + " of " + beanClass + " not readable");

        // the identity property simply reflects the member back unchanged
        if (identityProperty)
            return member;

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

        Method setterMethod = null;
        try {
            // everything except the last setter chain element is a getter
            Object currentMember = member;
            for(int i = 0, n = setterChain.size() - 1; i < n; i++) {
                Method currentMethod = setterChain.get(i);
                currentMember = currentMethod.invoke(currentMember, EMPTY_ARGUMENTS);
                if(currentMember == null) return null;
            }

            // do the remaining setter
            setterMethod = setterChain.get(setterChain.size() - 1);
            return setterMethod.invoke(currentMember, new Object[] { newValue });
        } catch (IllegalArgumentException e) {
            String message = e.getMessage();

            // improve the message if possible into something like:
            // "MyClass.someMethod(SomeClassType) cannot be called with an instance of WrongClassType"
            if ("argument type mismatch".equals(message) && setterMethod != null)
                message = getSimpleName(setterMethod.getDeclaringClass()) + "." + setterMethod.getName() + "(" + getSimpleName(setterMethod.getParameterTypes()[0]) + ") cannot be called with an instance of " + getSimpleName(newValue.getClass());

            throw new IllegalArgumentException(message);
        } catch(IllegalAccessException e) {
            SecurityException se = new SecurityException();
            se.initCause(e);
            throw se;
        } catch(InvocationTargetException e) {
            throw new UndeclaredThrowableException(e.getCause());
        } catch(RuntimeException e) {
            throw new RuntimeException("Failed to set property \"" + propertyName + "\" of " + beanClass + " to " + (newValue == null ? "null" : "instance of " + newValue.getClass()), e);
        }
    }

    /**
     * This method was backported from the JDK 1.5 version of java.lang.Class.
     *
     * Returns the simple name of the given class as given in the
     * source code. Returns an empty string if the underlying class is
     * anonymous.
     *
     * @return the simple name of the given class
     */
    private static String getSimpleName(Class clazz) {
        Class declaringClass = clazz.getDeclaringClass();
        String simpleName = declaringClass == null ? null : declaringClass.getName();
        if (simpleName == null) { // top level class
            simpleName = clazz.getName();
            return simpleName.substring(simpleName.lastIndexOf(".") + 1); // strip the package name
        }

        // Remove leading "\$[0-9]*" from the name
        int length = simpleName.length();
        if (length < 1 || simpleName.charAt(0) != '$')
            throw new InternalError("Malformed class name");
        int index = 1;
        while (index < length && isAsciiDigit(simpleName.charAt(index)))
            index++;
        // Eventually, this is the empty string iff this is an anonymous class
        return simpleName.substring(index);
    }

    /**
     * This method was backported from the JDK 1.5 version of java.lang.Class.
     *
     * Character.isDigit answers <tt>true</tt> to some non-ascii
     * digits. This one does not.
     */
    private static boolean isAsciiDigit(char c) {
        return '0' <= c && c <= '9';
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;

        final BeanProperty that = (BeanProperty) o;

        if(!beanClass.equals(that.beanClass)) return false;
        if(!propertyName.equals(that.propertyName)) return false;

        return true;
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        int result;
        result = beanClass.hashCode();
        result = 29 * result + propertyName.hashCode();
        return result;
    }
}