/* Glazed Lists                                                      (c) 2005 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
/*                                                          StarLight Systems */
package ca.odell.glazedlists.util.concurrent;

/**
 * This factory provides an implementation of {@link Lock} that is optimized
 * for the current platform.
 *
 * <p>To override the default {@link Lock} implementation used by this class,
 * set the <code>glazedlists.lockfactory</code> system property to the fully
 * qualified classname of a class that extends {@link LockFactory}. This can
 * be used to exercise <code>java.util.concurrent</code> when that package
 * is available (ie. Java 5 or better).
 *
 * @author <a "mailto:rob@starlight-systems.com">Rob Eden</a>
 */
public interface LockFactory {

	/** System property key */
	public static final String LOCKING_PROPERTY = "glazedlists.lockfactory";

    /** the current Lock factory */
	public static final LockFactory DEFAULT = SimpleLockFactory.createDefaultLockFactory();

    /**
     * Create a {@link ReadWriteLock}.
     *
     * <p>The default implementation returns an implementation that has been
     * derived from Doug Lea's <a href="http://gee.cs.oswego.edu/dl/classes/EDU/oswego/cs/dl/util/concurrent/intro.html">util.concurrent</a>.
     */
    public ReadWriteLock createReadWriteLock();

    /**
     * Create a {@link Lock}.
     */
    public Lock createLock();

}

/**
 * A simple implementation of {@link LockFactory} that has been
 * derived from Doug Lea's <a href="http://gee.cs.oswego.edu/dl/classes/EDU/oswego/cs/dl/util/concurrent/intro.html">util.concurrent</a>.
 */
class SimpleLockFactory implements LockFactory {

    /**
     * Determine which LockFactory implementation to delegate to at
     * class initialization time.
     */
    public static LockFactory createDefaultLockFactory() {

        // if the user has specified their own LockFactory, use that
        String userLockFactoryClassname = "";
        try {
            userLockFactoryClassname = System.getProperty(LOCKING_PROPERTY, null);
            if (userLockFactoryClassname != null) {
                return (LockFactory)Class.forName(userLockFactoryClassname).newInstance();
            }
        } catch(SecurityException e) {
            // accessing the System property failed! Just use the default lock.
        } catch(Exception e) {
            System.err.println("Unable to load user-defined lock factory, \"" + userLockFactoryClassname + "\", default will be used.");
            e.printStackTrace();
        } catch (NoClassDefFoundError e) {
            System.err.println("Unable to load user-defined lock factory, \"" + userLockFactoryClassname + "\", default will be used.");
            e.printStackTrace();
        }

        // use the default implementation if we couldn't find a better one
        return new SimpleLockFactory();
    }

    /**
     * Create a {@link ReadWriteLock}.
     *
     * <p>The default implementation returns an implementation that has been
     * derived from Doug Lea's <a href="http://gee.cs.oswego.edu/dl/classes/EDU/oswego/cs/dl/util/concurrent/intro.html">util.concurrent</a>.
     */
    public ReadWriteLock createReadWriteLock() {
        return new J2SE12ReadWriteLock();
    }

    /**
     * Create a {@link Lock}.
     *
     * <p>The default implementation returns an implementation that has been
     * derived from Doug Lea's <a href="http://gee.cs.oswego.edu/dl/classes/EDU/oswego/cs/dl/util/concurrent/intro.html">util.concurrent</a>.
     */
    public Lock createLock() {
        return new J2SE12ReadWriteLock().writeLock();
    }
}
