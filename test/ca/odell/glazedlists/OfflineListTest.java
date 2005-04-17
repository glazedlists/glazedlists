/*             Glazed Lists  http://publicobject.com/glazedlists/             */                        
/*        Copyright 2003-2005 publicobject.com, O'Dell Engineering Ltd.       */
package ca.odell.glazedlists;

import junit.framework.TestCase;
import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;
import java.io.IOException;

/**
 * Tests FileList class it focus on implemened
 * set(int, Object), add(int, Object), remove(int)
 * and get(int) methods.
 *
 * <p>It passes Serializable and null elements.
 *
 * @author Petr Kuzel
 */
public class OfflineListTest extends TestCase {

    public void testAll() {
        List gold = new ArrayList();
        List file = null;
        try {
            file = new OfflineList();
        } catch (IOException e) {
            // let test fail
        }

        gold.add("INIT");
        file.add("INIT");

        for (int i = 0; i<1000; i++) {
            gold.add(i, "loop" + i);
            file.add(i, "loop" + i);
        }

        compareLists(gold, file);

        for (int i = 3; i<7; i++) {
            gold.set(i, "replace " + i);
            file.set(i, "replace " + i);
        }

        compareLists(gold, file);

        for (int i = 700; i<768; i++) {
            gold.remove(i);
            file.remove(i);
        }

        compareLists(gold, file);

        gold.add(null);
        file.add(null);

        compareLists(gold, file);
    }

    private void compareLists(List gold, List copy) {
        Iterator it = gold.iterator();
        Iterator it2 = copy.iterator();

        while (it.hasNext()) {
            Object next = it.next();
            assertTrue("Copy misses some elements!", it2.hasNext());
            Object n = it2.next();
            if (next != null) {
                assertTrue("Element difference! " + n, next.equals(n));
            } else {
                assertTrue("Element difference (null expected)! " + n, n == null);
            }
        }

        assertTrue("Copy contains extra elements!", it.hasNext() == it2.hasNext());
    }
}
