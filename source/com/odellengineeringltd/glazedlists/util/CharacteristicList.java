/**
 * Glazed Lists
 * http://glazedlists.dev.java.net/
 *
 * COPYRIGHT 2003 O'DELL ENGINEERING LTD.
 */
package com.odellengineeringltd.glazedlists.util;

// the core Glazed Lists package
import com.odellengineeringltd.glazedlists.*;
import com.odellengineeringltd.glazedlists.event.*;

/**
 * A CharacteristicList is a simple MutationList views a particular
 * characteristic of its source list.
 *
 * <p>For example, suppose I have a list of email addresses:
 * <ol type="1">
 *     <li>jesse@swank.ca
 *     <li>joe@odellengineeringltd.com
 *     <li>lyndite@yahoo.com
 *     <li>jumpyjodes@hotmail.com
 *     <li>gtcale@uwaterloo.ca
 *     <li>zaxxon@yahoo.com
 * </ol>
 *
 * <p>I can extend <code>CharacteristicList</code> to provide a list of domain
 * names from the specified email address:
 * <ol type="1">
 *     <li>swank.ca
 *     <li>odellengineeringltd.com
 *     <li>yahoo.com
 *     <li>hotmail.com
 *     <li>uwaterloo.ca
 *     <li>yahoo.com
 * </ol>
 *
 * <p>In this case, the <code>getCharacteristic()</code> method will return the
 * part of the email address that follows the "@" symbol.
 *
 * @author <a href="mailto:jesse@odel.on.ca">Jesse Wilson</a>
 */
public abstract class CharacteristicList extends MutationList implements ListEventListener, EventList {

    /**
     * Creates a new CharacteristicList.
     */
    protected CharacteristicList(EventList source) {
        super(source);
        source.addListEventListener(this);
    }
    
    /**
     * Returns the element at the specified position in this list. This gets
     * the characteristic of the object in the specified index of the source
     * list.
     */
    public final Object get(int index) {
        Object baseObject = source.get(index);
        return getCharacteristic(baseObject);
    }
    
    /**
     * Gets the characteristic from the source object to present in this
     * view.
     *
     * @param baseObject the object to derive the characteristic from. This
     *      object may be null.
     */
    public abstract Object getCharacteristic(Object baseObject);
    
    /**
     * Returns the number of elements in this list.
     */
    public final int size() {
        return source.size();
    }
    
    /**
     * When this list is changed, it passes on the changes to all listeners.
     */
    public final void listChanged(ListEvent listChanges) {
        // just pass on the changes
        updates.beginEvent();
        while(listChanges.next()) {
            updates.addChange(listChanges.getType(), listChanges.getIndex());
        }
        updates.commitEvent();
    }
}
