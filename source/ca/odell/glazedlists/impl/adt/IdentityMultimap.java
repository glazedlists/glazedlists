/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.impl.adt;

import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;

/**
 * A poor man's multimap, used only to reduce the complexity code that deals
 * with these otherwise painful structures.
 *
 * @author <a href="mailto:jesse@swank.ca">Jesse Wilson</a>
 */
public class IdentityMultimap<K,V> extends IdentityHashMap<K, List<V>> {
    public void addValue(K key, V value) {
        List<V> values = super.get(key);
        if(values == null) {
            values = new ArrayList<V>(2);
            put(key, values);
        }
        values.add(value);
    }
    public List<V> get(Object key) {
        List<V> values = super.get(key);
        return values == null ? Collections.EMPTY_LIST : values;
    }
    public int count(Object key) {
        List<V> values = super.get(key);
        return values == null ? 0 : values.size();
    }
}