/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists;

import junit.framework.TestCase;

import java.util.Collections;

import ca.odell.glazedlists.impl.testing.GlazedListsTests;
import ca.odell.glazedlists.impl.testing.ListConsistencyListener;

/**
 * Test {@link RangeList}.
 *
 * @author <a href="mailto:jesse@swank.ca">Jesse Wilson</a>
 */
public class RangeListTest extends TestCase {

    public void testAddAll() {
        EventList<String> source = new BasicEventList<String>();
        RangeList<String> rangeList = new RangeList<String>(source);
        ListConsistencyListener.install(rangeList);

        assertEquals(0, rangeList.size());

        rangeList.addAll(GlazedListsTests.stringToList("JESSE"));
        assertEquals(GlazedListsTests.stringToList("JESSE"), rangeList);
    }

    public void testChangeSource() {
        EventList<String> source = new BasicEventList<String>();
        RangeList<String> rangeList = new RangeList<String>(source);
        ListConsistencyListener.install(rangeList);

        rangeList.setHeadRange(2, 4);
        assertEquals(0, rangeList.size());

        source.addAll(GlazedListsTests.stringToList("JESSE"));
        assertEquals(GlazedListsTests.stringToList("SS"), rangeList);

        rangeList.setHeadRange(0, 5);
        source.set(0, "M");
        source.set(4, "Y");
        // total string: MESSY
        assertEquals(GlazedListsTests.stringToList("MESSY"), rangeList);

        source.addAll(0, GlazedListsTests.stringToList("JA"));
        // total string: JAMESSY
        assertEquals(GlazedListsTests.stringToList("JAMES"), rangeList);

        source.removeAll(GlazedListsTests.stringToList("S"));
        // total string: JAMEY
        assertEquals(GlazedListsTests.stringToList("JAMEY"), rangeList);

        source.removeAll(GlazedListsTests.stringToList("ME"));
        // total string: JAY
        assertEquals(GlazedListsTests.stringToList("JAY"), rangeList);

        rangeList.setHeadRange(1, 7);
        assertEquals(GlazedListsTests.stringToList("AY"), rangeList);

        source.addAll(1, GlazedListsTests.stringToList("M"));
        source.addAll(3, GlazedListsTests.stringToList("LTB"));
        // total string: JMALTBY
        assertEquals(GlazedListsTests.stringToList("MALTBY"), rangeList);

        source.addAll(5, GlazedListsTests.stringToList("ERNATIVE"));
        // total string: JMALTERNATIVEBY
        assertEquals(GlazedListsTests.stringToList("MALTER"), rangeList);

        rangeList.setHeadRange(2, 13);
        assertEquals(GlazedListsTests.stringToList("ALTERNATIVE"), rangeList);

        rangeList.setHeadRange(-9, -3);
        assertEquals(GlazedListsTests.stringToList("NATIVE"), rangeList);

        source.subList(0, 7).clear();
        // total string: NATIVEBY
        assertEquals(GlazedListsTests.stringToList("NATIVE"), rangeList);

        source.subList(6, 8).clear();
        // total string: NATIVE
        assertEquals(GlazedListsTests.stringToList("NATI"), rangeList);

        source.remove(0);
        // total string: ATI
        assertEquals(GlazedListsTests.stringToList("ATI"), rangeList);

        source.clear();
        source.addAll(GlazedListsTests.stringToList("ROUGHRIDERS"));
        // total string: ROUGHRIDERS
        rangeList.setHeadRange(0, 5);
        assertEquals(GlazedListsTests.stringToList("ROUGH"), rangeList);

        source.removeAll(GlazedListsTests.stringToList("AEIOU"));
        // total string: RGHRDRS
        assertEquals(GlazedListsTests.stringToList("RGHRD"), rangeList);
    }

    public void testTailRange() {
        EventList<String> source = new BasicEventList<String>();
        source.addAll(GlazedListsTests.stringToList("ABCDEFGHIJKLMNOPQRSTUVWXYZ"));

        RangeList<String> rangeList = new RangeList<String>(source);

        ListConsistencyListener.install(rangeList);

        assertEquals(source, rangeList);

        rangeList.setTailRange(5, 0);
        assertEquals(GlazedListsTests.stringToList("VWXYZ"), rangeList);

        rangeList.setTailRange(3, 1);
        assertEquals(GlazedListsTests.stringToList("XY"), rangeList);
    }

    public void testRangeAdjust() {
        EventList<String> source = new BasicEventList<String>();
        source.addAll(GlazedListsTests.stringToList("ABCDEFGHIJKLMNOPQRSTUVWXYZ"));

        RangeList<String> rangeList = new RangeList<String>(source);
        ListConsistencyListener.install(rangeList);

        assertEquals(source, rangeList);

        rangeList.setHeadRange(1, 2);
        assertEquals(GlazedListsTests.stringToList("B"), rangeList);

        // shift right, exclusive
        rangeList.setHeadRange(-5, -1);
        assertEquals(GlazedListsTests.stringToList("WXYZ"), rangeList);

        // expand left
        rangeList.setHeadRange(-10, -1);
        assertEquals(GlazedListsTests.stringToList("RSTUVWXYZ"), rangeList);

        // shift left, inclusive
        rangeList.setHeadRange(-13, -4);
        assertEquals(GlazedListsTests.stringToList("OPQRSTUVW"), rangeList);

        // shrink
        rangeList.setHeadRange(-10, -7);
        assertEquals(GlazedListsTests.stringToList("RST"), rangeList);

        // shift left, exclusive
        rangeList.setHeadRange(-15, -12);
        assertEquals(GlazedListsTests.stringToList("MNO"), rangeList);

        // expand right
        rangeList.setHeadRange(-15, -9);
        assertEquals(GlazedListsTests.stringToList("MNOPQR"), rangeList);

        // shift right, inclusive
        rangeList.setHeadRange(-13, -6);
        assertEquals(GlazedListsTests.stringToList("OPQRSTU"), rangeList);

        // grow
        rangeList.setHeadRange(-15, -2);
        assertEquals(GlazedListsTests.stringToList("MNOPQRSTUVWXY"), rangeList);

        // beyond right end
        rangeList.setHeadRange(30, 40);
        assertEquals(Collections.EMPTY_LIST, rangeList);

        // normal
        rangeList.setHeadRange(0, 3);
        assertEquals(GlazedListsTests.stringToList("ABC"), rangeList);

        // past left end
        rangeList.setHeadRange(-30, -40);
        assertEquals(Collections.EMPTY_LIST, rangeList);

        // normal
        rangeList.setHeadRange(0, 3);
        assertEquals(GlazedListsTests.stringToList("ABC"), rangeList);
    }

    public void testMiddleRange() {
        EventList<String> source = new BasicEventList<String>();
        source.addAll(GlazedListsTests.stringToList("ABCDEFGHIJKLMNOPQRSTUVWXYZ"));

        RangeList<String> rangeList = new RangeList<String>(source);
        ListConsistencyListener.install(rangeList);

        assertEquals(source, rangeList);

        rangeList.setMiddleRange(1, 1);
        assertEquals(GlazedListsTests.stringToList("BCDEFGHIJKLMNOPQRSTUVWXY"), rangeList);

        source.add(1, "X");
        assertEquals(GlazedListsTests.stringToList("XBCDEFGHIJKLMNOPQRSTUVWXY"), rangeList);

        source.addAll(1, GlazedListsTests.stringToList("XXX"));
        assertEquals(GlazedListsTests.stringToList("XXXXBCDEFGHIJKLMNOPQRSTUVWXY"), rangeList);

        source.add(source.size()-1, "X");
        assertEquals(GlazedListsTests.stringToList("XXXXBCDEFGHIJKLMNOPQRSTUVWXYX"), rangeList);

        source.addAll(source.size()-1, GlazedListsTests.stringToList("XXX"));
        assertEquals(GlazedListsTests.stringToList("XXXXBCDEFGHIJKLMNOPQRSTUVWXYXXXX"), rangeList);

        source.add(0, "?");
        source.add("?");
        assertEquals(GlazedListsTests.stringToList("AXXXXBCDEFGHIJKLMNOPQRSTUVWXYXXXXZ"), rangeList);

        source.set(1, "?");
        source.set(source.size()-2, "?");
        assertEquals(GlazedListsTests.stringToList("?XXXXBCDEFGHIJKLMNOPQRSTUVWXYXXXX?"), rangeList);

        rangeList.set(0, "$");
        rangeList.set(rangeList.size()-1, "$");
        assertEquals(GlazedListsTests.stringToList("$XXXXBCDEFGHIJKLMNOPQRSTUVWXYXXXX$"), rangeList);

        source.remove(0);
        source.remove(source.size()-1);
        assertEquals(GlazedListsTests.stringToList("XXXXBCDEFGHIJKLMNOPQRSTUVWXYXXXX"), rangeList);

        source.removeAll(GlazedListsTests.stringToList("XXXXXXXXX"));
        assertEquals(GlazedListsTests.stringToList("BCDEFGHIJKLMNOPQRSTUVWY"), rangeList);
        assertEquals(GlazedListsTests.stringToList("$BCDEFGHIJKLMNOPQRSTUVWY$"), source);

        source.clear();
        assertEquals(GlazedListsTests.stringToList(""), source);
        assertEquals(GlazedListsTests.stringToList(""), rangeList);

        source.add("J");
        assertEquals(GlazedListsTests.stringToList("J"), source);
        assertEquals(GlazedListsTests.stringToList(""), rangeList);

        source.add("A");
        assertEquals(GlazedListsTests.stringToList("JA"), source);
        assertEquals(GlazedListsTests.stringToList(""), rangeList);

        source.add("M");
        assertEquals(GlazedListsTests.stringToList("JAM"), source);
        assertEquals(GlazedListsTests.stringToList("A"), rangeList);

        source.add("E");
        assertEquals(GlazedListsTests.stringToList("JAME"), source);
        assertEquals(GlazedListsTests.stringToList("AM"), rangeList);

        source.add("S");
        assertEquals(GlazedListsTests.stringToList("JAMES"), source);
        assertEquals(GlazedListsTests.stringToList("AME"), rangeList);
     }

    public void testMiddleRangeZeros() {
        EventList<String> source = new BasicEventList<String>();
        source.addAll(GlazedListsTests.stringToList("ABCDEFGHIJKLMNOPQRSTUVWXYZ"));

        RangeList<String> rangeList = new RangeList<String>(source);
        ListConsistencyListener.install(rangeList);

        assertEquals(source, rangeList);

        rangeList.setMiddleRange(0, 0);
        assertEquals(GlazedListsTests.stringToList("ABCDEFGHIJKLMNOPQRSTUVWXYZ"), rangeList);

        rangeList.setMiddleRange(1, 0);
        assertEquals(GlazedListsTests.stringToList("BCDEFGHIJKLMNOPQRSTUVWXYZ"), rangeList);

        rangeList.setMiddleRange(0, 1);
        assertEquals(GlazedListsTests.stringToList("ABCDEFGHIJKLMNOPQRSTUVWXY"), rangeList);
    }
}