package com.publicobject.misc.xml;

/**
 * Push a new business element onto the working stack.
 *
 * <p>This is designed to set a context that deeper processors
 * can attach values to.
 *
 * @author jessewilson
 */
@FunctionalInterface
public interface PushProcessor<E> {

    public E evaluate();

}
