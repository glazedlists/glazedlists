/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists;

import junit.framework.TestCase;

import java.util.Arrays;
import java.util.List;
import java.util.Collections;

/**
 * Test {@link RangeList}.
 *
 * @author <a href="mailto:jesse@swank.ca">Jesse Wilson</a>
 */
public class RangeListTest extends TestCase {

    public void testAddAll() {
        EventList source = new BasicEventList();
        RangeList rangeList = new RangeList(source);
        rangeList.addListEventListener(new ListConsistencyListener(rangeList, "Range List", false));

        assertEquals(0, rangeList.size());

        rangeList.addAll(split("J,E,S,S,E"));
        assertEquals(5, rangeList.size());
    }

    public void testChangeSource() {
        EventList source = new BasicEventList();
        source.addAll(split("J,E,S,S,E"));

        RangeList rangeList = new RangeList(source);
        rangeList.addListEventListener(new ListConsistencyListener(rangeList, "Range List", false));

        rangeList.setRange(0, 5);
        source.set(0, "M");
        source.set(4, "Y");
        assertEquals(split("M,E,S,S,Y"), rangeList);

        source.addAll(0, split("J,A"));
        assertEquals(split("J,A,M,E,S"), rangeList);

        source.removeAll(split("S"));
        assertEquals(split("J,A,M,E,Y"), rangeList);

        source.removeAll(split("M,E"));
        assertEquals(split("J,A,Y"), rangeList);

        rangeList.setRange(1, 7);
        assertEquals(split("A,Y"), rangeList);

        source.addAll(1, split("M"));
        source.addAll(3, split("L,T,B"));
        assertEquals(split("M,A,L,T,B,Y"), rangeList);

        source.addAll(5, split("E,R,N,A,T,I,V,E"));
        assertEquals(split("M,A,L,T,E,R"), rangeList);

        rangeList.setRange(2, 13);
        assertEquals(split("A,L,T,E,R,N,A,T,I,V,E"), rangeList);

        rangeList.setRange(-9, -3);
        assertEquals(split("N,A,T,I,V,E"), rangeList);

        source.subList(0, 7).clear();
        assertEquals(split("N,A,T,I,V,E"), rangeList);

        source.subList(6, 8).clear();
        assertEquals(split("N,A,T,I"), rangeList);

        source.remove(0);
        assertEquals(split("A,T,I"), rangeList);

        source.clear();
        source.addAll(split("R,O,U,G,H,R,I,D,E,R,S"));
        rangeList.setRange(0, 5);
        assertEquals(split("R,O,U,G,H"), rangeList);

        source.removeAll(split("A,E,I,O,U"));
        assertEquals(split("R,G,H,R,D"), rangeList);
    }

    public void testTailRange() {
        EventList source = new BasicEventList();
        source.addAll(split("A,B,C,D,E,F,G,H,I,J,K,L,M,N,O,P,Q,R,S,T,U,V,W,X,Y,Z"));

        RangeList rangeList = new RangeList(source);
        rangeList.addListEventListener(new ListConsistencyListener(rangeList, "Range List", false));

        assertEquals(source, rangeList);

        rangeList.setTailRange(5, 0);
        assertEquals(split("V,W,X,Y,Z"), rangeList);

        rangeList.setTailRange(3, 1);
        assertEquals(split("X,Y"), rangeList);
    }

    public void testRangeAdjust() {
        EventList source = new BasicEventList();
        source.addAll(split("A,B,C,D,E,F,G,H,I,J,K,L,M,N,O,P,Q,R,S,T,U,V,W,X,Y,Z"));

        RangeList rangeList = new RangeList(source);
        rangeList.addListEventListener(new ListConsistencyListener(rangeList, "Range List", false));

        assertEquals(source, rangeList);

        rangeList.setRange(1, 2);
        assertEquals(split("B"), rangeList);

        // shift right, exclusive
        rangeList.setRange(-5, -1);
        assertEquals(split("W,X,Y,Z"), rangeList);

        // expand left
        rangeList.setRange(-10, -1);
        assertEquals(split("R,S,T,U,V,W,X,Y,Z"), rangeList);

        // shift left, inclusive
        rangeList.setRange(-13, -4);
        assertEquals(split("O,P,Q,R,S,T,U,V,W"), rangeList);

        // shrink
        rangeList.setRange(-10, -7);
        assertEquals(split("R,S,T"), rangeList);

        // shift left, exclusive
        rangeList.setRange(-15, -12);
        assertEquals(split("M,N,O"), rangeList);

        // expand right
        rangeList.setRange(-15, -9);
        assertEquals(split("M,N,O,P,Q,R"), rangeList);

        // shift right, inclusive
        rangeList.setRange(-13, -6);
        assertEquals(split("O,P,Q,R,S,T,U"), rangeList);

        // grow
        rangeList.setRange(-15, -2);
        assertEquals(split("M,N,O,P,Q,R,S,T,U,V,W,X,Y"), rangeList);

        // beyond right end
        rangeList.setRange(30, 40);
        assertEquals(Collections.EMPTY_LIST, rangeList);

        // normal
        rangeList.setRange(0, 3);
        assertEquals(split("A,B,C"), rangeList);

        // past left end
        rangeList.setRange(-30, -40);
        assertEquals(Collections.EMPTY_LIST, rangeList);

        // normal
        rangeList.setRange(0, 3);
        assertEquals(split("A,B,C"), rangeList);
    }

    private static List split(String commaDelimited) {
        return Arrays.asList(commaDelimited.split(","));
    }
}