package com.publicobject.misc.xml;

/**
 * A processor that takes a newly discovered object and weaves
 * it into an existing object.
 *
 * <p>For example, this could be used to assign a newly discovered
 * "id" string from an XML document to the id property of a business
 * object.
 *
 * <p>The provided parameters are the two objects of interest -
 * the newly discovered value and the current business object.
 *
 * @author jessewilson
 */
@FunctionalInterface
public interface PopProcessor<B,V> {

    void process(B baseObject, V value);

}
