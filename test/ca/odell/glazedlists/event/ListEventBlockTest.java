/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.event;

import junit.framework.TestCase;

import java.util.List;
import java.util.ArrayList;

import ca.odell.glazedlists.*;

/**
 * @author <a href="mailto:jesse@swank.ca">Jesse Wilson</a>
 */
public class ListEventBlockTest extends TestCase {

    public void testSortListEventBlocks() {
        ExternalNestingEventList list = new ExternalNestingEventList(new BasicEventList());
        ListConsistencyListener.install(list);

        list.beginEvent(true);
        list.addAll(GlazedListsTests.stringToList("AAAAAAAAAAAAAAAAAAAAAAAAAAAAA"));
        list.commitEvent();

        list.beginEvent(false);
        list.add(0, "A");
        list.add(3, "A");
        list.add(4, "A");
        list.add(10, "A");
        list.add(3, "A");
        list.add(14, "A");
        list.add(7, "A");
        list.add(5, "A");
        list.add(3, "A");
        list.add(18, "A");
        list.commitEvent();
    }

    public void testSortListEventBlocks2() {
        ExternalNestingEventList list = new ExternalNestingEventList(new BasicEventList());
        ListConsistencyListener.install(list);

        list.beginEvent(true);
        list.addAll(GlazedListsTests.stringToList("AAAAAAAAAAAAAAAAAAAAAAAAAAAAA"));
        list.commitEvent();

        list.beginEvent(true);
        list.set(3, "A");
        list.remove(3);
        list.set(1, "A");
        list.commitEvent();
    }

    public void testSortListEventBlocks3() {
        ExternalNestingEventList list = new ExternalNestingEventList(new BasicEventList());
        ListConsistencyListener.install(list);

        list.beginEvent(true);
        list.addAll(GlazedListsTests.stringToList("AAAAAAAAAAAAAAAAAAAAAAAAAAAAA"));
        list.commitEvent();

        list.beginEvent(true);
        list.remove(2);
        list.set(0, "A");
        list.add(2, "A");
        list.commitEvent();
    }
}