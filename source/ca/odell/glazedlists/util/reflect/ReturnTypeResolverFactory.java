/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.util.reflect;

public interface ReturnTypeResolverFactory {

    /** The ReturnTypeResolver factory for this JVM. */
    public static final ReturnTypeResolverFactory DEFAULT = new DelegateReturnTypeResolverFactory();

    /**
     * Create a {@link ReturnTypeResolver}.
     */
    public ReturnTypeResolver createReturnTypeResolver();
}

/**
 * An implementation of {@link ReturnTypeResolverFactory} that the most
 * appropriate ReturnTypeResolver for the current JVM.
 */
class DelegateReturnTypeResolverFactory implements ReturnTypeResolverFactory {

    /** The true JVM-specific ReturnTypeResolver we return. */
    private ReturnTypeResolver returnTypeResolver;

    DelegateReturnTypeResolverFactory() {
        try {
            // if the J2SE 5.0 Type class can be loaded, we're running on a JDK 1.5 VM
            Class.forName("java.lang.reflect.Type");

            // and if we can load our J2SE 5.0 ReturnTypeResolver implementation
            // (i.e. it's not a Glazed Lists 1.4 implementation running on a JDK 1.5 VM)
            // then use the J2SE 5.0 LockFactory implementation
            returnTypeResolver = (ReturnTypeResolver) Class.forName("ca.odell.glazedlists.impl.java15.J2SE50ReturnTypeResolver").newInstance();

        } catch (Throwable t) {
            // otherwise fall back to a J2SE 1.4 LockFactory
            returnTypeResolver = new J2SE14ReturnTypeResolver();
        }
    }

    public ReturnTypeResolver createReturnTypeResolver() {
        return returnTypeResolver;
    }
}