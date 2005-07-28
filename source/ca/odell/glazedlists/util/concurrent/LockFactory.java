/* Glazed Lists                                                      (c) 2005 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
/*                                                          StarLight Systems */
package ca.odell.glazedlists.util.concurrent;

/**
 * Factory allows creation of the best locks for the VM we're running in. Overriding this
 * class can allow usage of specialized locks. To use a custom factory class, set the
 * class name in the system property <tt>glazedlists.lockfactory</tt>.
 *
 * @author <a "mailto:rob@starlight-systems.com">Rob Eden</a>
 */
public class LockFactory {
	// System properties used by this class
	private static final String LOCKING_PROPERTY = "glazedlists.lockfactory";
	private static final String LOCKING_DEBUG_PROPERTY = LOCKING_PROPERTY + ".debug";


	private static LockFactory instance;

	static {
		// See if we should print a debugging message saying what locks we're using
		boolean debug = false;
		try {
			debug = System.getProperty(LOCKING_DEBUG_PROPERTY, null) != null;
		} catch( SecurityException ex ) {}		// ignore


		LockFactory special_lock_factory = null;

		// See if the user has specified their own lock factory implementation
		try {
			String user_lock_factory = System.getProperty(LOCKING_PROPERTY, null);
			if (user_lock_factory != null) {
				try {
					special_lock_factory = (LockFactory) Class.forName(
						user_lock_factory).newInstance();

					if (debug) System.out.println("*** GlazedLists will use user-defined " +
						"Locking: " + user_lock_factory + " ***");
				}
				catch (Exception ex) {
					System.err.println("Unable to load user-defined lock factory. " +
						"Default will be used.");
					ex.printStackTrace();
				}
				catch (NoClassDefFoundError er) {
					System.err.println("Unable to load user-defined lock factory. " +
						"Default will be used.");
					er.printStackTrace();
				}
			}
		}
		catch(SecurityException ex ) {}			// ignore

		// Set the singleton instance
		if (special_lock_factory != null) {
			instance = special_lock_factory;
		} else {
			if (debug) System.out.println("*** GlazedLists will use 1.2+ Locking ***");
			instance = new LockFactory();
		}
	}


	/**
	 * Create a new {@link ReadWriteLock}.
	 */
	public static ReadWriteLock createReadWriteLock() {
		return instance._internalCreateReadWriteLock();
	}

	/**
	 * Create a new {@link Lock}.
	 */
	public static Lock createLock() {
		return instance._internalCreateLock();
	}


	/**
	 * Should be overridden by LockFactory implementations to return a ReadWriteLock
	 * implementation. The default implementation returns a {@link J2SE12ReadWriteLock},
	 * which works in 1.2+ VM's.
	 */
	protected ReadWriteLock _internalCreateReadWriteLock() {
		return new J2SE12ReadWriteLock();
	}


	/**
	 * Should be overridden by LockFactory implementations to return a Lock implementation.
	 * The default implementation uses a {@link J2SE12ReadWriteLock}, which works in 1.2+
	 * VM's.
	 */
	protected Lock _internalCreateLock() {
		return new J2SE12ReadWriteLock().writeLock();
	}
}
