/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.impl.java15;

import ca.odell.glazedlists.util.reflect.ReturnTypeResolver;
import ca.odell.glazedlists.impl.java15.TypeResolver;
import ca.odell.glazedlists.impl.java15.MoreTypes;

import java.lang.reflect.Method;
import java.lang.reflect.Type;

/**
 * An implementation of {@link ReturnTypeResolver} that adapt's Google's
 * {@link TypeResolver generic type resolver}.
 *
 * @author James Lemieux
 */
public class J2SE50ReturnTypeResolver implements ReturnTypeResolver {
    public Class<?> getReturnType(Class clazz, Method method) {
        // get the raw return type
        final Type type = new TypeResolver(clazz).getReturnType(method);

        // convert the raw return type to a Class type
        return MoreTypes.getRawType(type);
    }
}