/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.impl;

import java.util.Enumeration;
import java.util.Iterator;

/**
 * This simple class adapts a given {@link Iterator} to the Enumeration
 * interface. It is useful when implementing existing JDK interfaces backed by
 * EventLists which must return Enumerations.
 *
 * @author James Lemieux
 */
public final class IteratorAsEnumeration<E> implements Enumeration<E> {

    /** The delegate Iterator which appears to be an Enumeration to the world. */
    private final Iterator<? extends E> iterator;

    /**
     * Construct an Enumeration which is backed by the given
     * <code>iterator</code>.
     *
     * @throws IllegalArgumentException if <code>iterator</code> is <tt>null</tt>
     */
    public IteratorAsEnumeration(Iterator<? extends E> iterator) {
        if (iterator == null)
            throw new IllegalArgumentException("iterator may not be null");

        this.iterator = iterator;
    }

    /** @inheritDoc */
    public boolean hasMoreElements() {
        return iterator.hasNext();
    }

    /** @inheritDoc */
    public E nextElement() {
        return iterator.next();
    }
}