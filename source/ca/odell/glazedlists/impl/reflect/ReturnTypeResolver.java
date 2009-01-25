/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.impl.reflect;

import java.lang.reflect.Method;

/**
 * With the introduction of generics in JDK 1.5 it became more difficult to
 * know the precise return type of a method declared with a generic return
 * type. For example, {@link java.util.List} declares
 * {@link java.util.List#get get()} to return the generic type of "E". The
 * challenge begins when a subtype of List is created for which E is known, eg:
 *
 * <pre>
 * public class MyStringList extends List<String>
 * </pre>
 *
 * The Method object for MyStringList.get() reports a return type of
 * Object.class. But, it is possible to do better than this, and in fact
 * String.class can be found to be the true return type of the the method.
 *
 * <p>This interface abstracts away different strategies for locating the
 * return type of a given {@link Method} depending on whether the user is
 * running Glazed Lists under a 1.4 or 1.5+ JRE.
 *
 * @author James Lemieux
 */
public interface ReturnTypeResolver {

    /**
     * Locates and returns the most precise return type that can be found for
     * the given <code>method</code>. Note, it may be more precise than the
     * value returned by
     * {@link java.lang.reflect.Method#getReturnType() method.getReturnType()}.
     *
     * @param clazz the specific class for which the method's return type is
     *      requested (the specific class type matters if the return type is
     *      generic)
     * @param method for which a precise return type is requested
     * @return the most precise Class type that is known to be returned by the
     *      given <code>method</code>
     */
    public Class<?> getReturnType(Class<?> clazz, Method method);

    /**
     * Locates and returns the most precise type of the first parameter for the
     * given <code>method</code>. Note that it may be more precise than the
     * value returned by
     * {@link java.lang.reflect.Method#getParameterTypes()}[0].
     *
     * @param clazz the specific class for which the method's parameter type is
     *      requested (the specific class type matters if the return type is
     *      generic)
     * @param method for which a precise parameter type is requested
     * @return the most precise Class type that is known to be the parameter for
     *      the given <code>method</code>
     * @throws IndexOutOfBoundsException if the given method has no parameters.
     */
    public Class<?> getFirstParameterType(Class<?> clazz, Method method);
}