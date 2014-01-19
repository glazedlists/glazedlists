/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.impl.functions;

import ca.odell.glazedlists.FunctionList;

/**
 * A function function that always returnss the same value regardless of the
 * input.
 *
 * @author James Lemieux
 */
public class ConstantFunction<E,V> implements FunctionList.Function<E,V> {

    private final V value;

    public ConstantFunction(V value) {
        this.value = value;
    }

    @Override
    public V evaluate(E sourceValue) {
        return value;
    }
}