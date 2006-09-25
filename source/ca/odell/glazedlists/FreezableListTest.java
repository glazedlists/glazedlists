/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists;

import ca.odell.glazedlists.impl.testing.ListConsistencyListener;
import ca.odell.glazedlists.impl.testing.GlazedListsTests;
import junit.framework.TestCase;

/**
 * Validate that FreezableList freezes and thaws as expected.
 *
 * @author <a href="mailto:jesse@swank.ca">Jesse Wilson</a>
 */
public class FreezableListTest extends TestCase {

    public void testFreezableList() {
        EventList<String> source = new BasicEventList<String>();
        FreezableList<String> freezable = new FreezableList<String>(source);
        ListConsistencyListener.install(freezable);

        source.addAll(GlazedListsTests.stringToList("ROUGHRIDERS"));
        assertEquals(GlazedListsTests.stringToList("ROUGHRIDERS"), freezable);

        freezable.freeze();
        source.removeAll(GlazedListsTests.stringToList("R"));
        assertEquals(GlazedListsTests.stringToList("OUGHIDES"), source);
        assertEquals(GlazedListsTests.stringToList("ROUGHRIDERS"), freezable);

        freezable.thaw();
        source.removeAll(GlazedListsTests.stringToList("R"));
        assertEquals(GlazedListsTests.stringToList("OUGHIDES"), freezable);
    }
}
