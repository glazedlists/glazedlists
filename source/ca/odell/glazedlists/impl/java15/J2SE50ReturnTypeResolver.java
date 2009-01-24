/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.impl.java15;

import ca.odell.glazedlists.util.reflect.ReturnTypeResolver;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.List;

/**
 * An implementation of {@link ReturnTypeResolver} that adapt's Google's
 * {@link TypeLiteral generic type resolver}.
 *
 * @author James Lemieux
 */
public class J2SE50ReturnTypeResolver implements ReturnTypeResolver {
    public Class<?> getReturnType(Class<?> clazz, Method method) {
        return new TypeLiteral<Object>(clazz).getReturnType(method).getRawType();
    }

    public Class<?> getFirstParameterType(Class<?> clazz, Method method) {
        return new TypeLiteral<Object>(clazz).getParameterTypes(method).get(0).getRawType();
    }
}