/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists;

import ca.odell.glazedlists.impl.testing.GlazedListsTests;
import ca.odell.glazedlists.impl.testing.ListConsistencyListener;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Validate that FreezableList freezes and thaws as expected.
 *
 * @author <a href="mailto:jesse@swank.ca">Jesse Wilson</a>
 */
public class FreezableListTest {

    @Test
    public void testFreezableList() {
        EventList<String> source = new BasicEventList<>();
        FreezableList<String> freezable = new FreezableList<>(source);
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
