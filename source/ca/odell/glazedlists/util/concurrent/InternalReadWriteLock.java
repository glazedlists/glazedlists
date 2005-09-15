/* Glazed Lists                                                 (c) 2003-2005 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.util.concurrent;


/**
 * A LockPair is a set of two locks that are locked and unlocked in
 * seqeunce. To prevent the locks from causing a deadlock, they are locked
 * in the order: ( first, second ), and unlocked in the order ( second, first ).
 */
class LockPair implements Lock {

    /** the locks to delegate to */
    private Lock first;
    private Lock second;

    /**
     * Creates a lock pair that uses the specified locks in sequence.
     */
    public LockPair(Lock first, Lock second) {
        this.first = first;
        this.second = second;
    }

    /**
     * Acquires the lock.
     */
    public void lock() {
        first.lock();
        second.lock();
    }

    /**
     * Acquires the lock only if it is free at the time of invocation.
     */
    public boolean tryLock() {
        boolean firstSuccess = first.tryLock();
        if(!firstSuccess) return false;

        boolean secondSuccess = second.tryLock();
        if(!secondSuccess) {
            first.unlock();
            return false;
        }

        return true;
    }

    /**
     * Releases the lock.
     */
    public void unlock() {
        second.unlock();
        first.unlock();
    }
}
