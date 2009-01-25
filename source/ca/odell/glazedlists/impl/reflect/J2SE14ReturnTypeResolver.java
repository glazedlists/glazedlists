/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.impl.reflect;

import java.lang.reflect.Method;

/**
 * An implementation of the {@link ReturnTypeResolver} interface that is
 * appropriate for JDK 1.4 (before generics existed).
 *
 * @author James Lemieux
 */
final class J2SE14ReturnTypeResolver implements ReturnTypeResolver {
    public Class<?> getReturnType(Class<?> clazz, Method method) {
        return method.getReturnType();
    }

    public Class<?> getFirstParameterType(Class<?> clazz, Method method) {
        return method.getParameterTypes()[0];
    }
}