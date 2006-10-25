// Copyright 2006 Google Inc. All Rights Reserved.

package ca.odell.glazedlists.util.concurrent;

/**
 * An implementation of {@link ca.odell.glazedlists.util.concurrent.LockFactory} that has been derived from Doug Lea's
 * <a href="http://gee.cs.oswego.edu/dl/classes/EDU/oswego/cs/dl/util/concurrent/intro.html">util.concurrent</a>.
 */
final class J2SE14LockFactory implements LockFactory {

    /**
     * Create a {@link ca.odell.glazedlists.util.concurrent.ReadWriteLock}.
     *
     * <p>The default implementation returns an implementation that has been
     * derived from Doug Lea's <a href="http://gee.cs.oswego.edu/dl/classes/EDU/oswego/cs/dl/util/concurrent/intro.html">util.concurrent</a>.
     */
    public ReadWriteLock createReadWriteLock() {
        return new J2SE14ReadWriteLock();
    }

    /**
     * Create a {@link ca.odell.glazedlists.util.concurrent.Lock}.
     *
     * <p>The default implementation returns an implementation that has been
     * derived from Doug Lea's <a href="http://gee.cs.oswego.edu/dl/classes/EDU/oswego/cs/dl/util/concurrent/intro.html">util.concurrent</a>.
     */
    public Lock createLock() {
        return new J2SE14ReadWriteLock().writeLock();
    }
}
