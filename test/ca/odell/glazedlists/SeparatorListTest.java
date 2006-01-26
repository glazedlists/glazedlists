/* Glazed Lists                                                 (c) 2003-2005 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists;

import junit.framework.TestCase;

/**
 * @author <a href="mailto:jesse@swank.ca">Jesse Wilson</a>
 */
public class SeparatorListTest extends TestCase {

    public void testSimpleSetup() {
        EventList<String> source = new BasicEventList<String>();
        source.addAll(GlazedListsTests.stringToList("AAAABBBDDD"));

        SeparatorList<String> separatorList = new SeparatorList<String>(source);
        ListConsistencyListener consistencyTest = new ListConsistencyListener(separatorList, "separatorList");
        
        System.out.println(separatorList);

        source.addAll(GlazedListsTests.stringToList("AAA"));
        System.out.println(separatorList);

        source.addAll(GlazedListsTests.stringToList("BD"));
        System.out.println(separatorList);

        source.addAll(GlazedListsTests.stringToList("CC"));
        System.out.println(separatorList);

        source.removeAll(GlazedListsTests.stringToList("B"));
        System.out.println(separatorList);

    }
}