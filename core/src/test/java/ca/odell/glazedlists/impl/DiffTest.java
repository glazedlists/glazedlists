/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.impl;

// for being a JUnit test case
import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.event.ListEvent;
import ca.odell.glazedlists.event.ListEventListener;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * This test verifies that Diff works.
 *
 * @author <a href="mailto:jesse@swank.ca">Jesse Wilson</a>
 */
public class DiffTest {

    /**
     * Tests to verify that Diff performs the correct number of changes.
     */
    @Test
    public void testDiff() {
        assertEquals(4, getChangeCount("algorithm", "logarithm", false));
        assertEquals(5, getChangeCount("abcabba", "cbabac", false));
        assertEquals(0, getChangeCount("Jesse", "JESSE", true));
        assertEquals(8, getChangeCount("Jesse", "JESSE", false));
    }

    /**
     * Tests that diff works for a large number of elements.
     */
    @Test
    public void testMemory() {
        EventList sequence = new BasicEventList(new SparseDifferencesList(new ReallyBigList(1000 * 1000)));
        List modifiedSequence = new SparseDifferencesList(new ReallyBigList(1000 * 1000));
        assertEquals(0, getChangeCount(sequence, modifiedSequence, false, null));

        Random dice = new Random(2);
        for(int i = 0; i < 10; i++) {
            modifiedSequence.set(dice.nextInt(modifiedSequence.size()), new Object());
        }
        assertEquals(20, getChangeCount(sequence, modifiedSequence, false, null));
        assertEquals(sequence, modifiedSequence);
    }

    /**
     * Counts the number of changes to change target to source.
     */
    private int getChangeCount(EventList targetList, List sourceList, boolean updates, Comparator comparator) {
        ListEventCounter counter = new ListEventCounter();
        targetList.addListEventListener(counter);

        if(comparator != null) GlazedLists.replaceAll(targetList, sourceList, false, comparator);
        else GlazedLists.replaceAll(targetList, sourceList, false);

        return counter.getEventCount();
    }

    /**
     * Converts the strings to lists and counts the changes between them.
     *
     * <p>If case sensitivity is specified, an appropriate {@link Comparator} will be
     * used to determine equality between elements.
     */
    private int getChangeCount(String target, String source, boolean caseSensitive) {
        EventList targetList = new BasicEventList();
        targetList.addAll(stringToList(target));
        List sourceList = stringToList(source);

        return getChangeCount(targetList, sourceList, false, caseSensitive ? GlazedLists.caseInsensitiveComparator() : null);
    }

    /**
     * Create a list, where each element is a character from the String.
     */
    private List stringToList(String data) {
        List result = new ArrayList();
        for(int c = 0; c < data.length(); c++) {
            result.add(data.substring(c, c+1));
        }
        return result;
    }

    /**
     * A list that returns the integer index as the row value.
     */
    private static class ReallyBigList extends AbstractList {
        private int size;
        public ReallyBigList(int size) {
            this.size = size;
        }
        @Override
        public Object get(int index) {
            return new Integer(index);
        }

        @Override
        public int size() {
            return size;
        }
        @Override
        public Object remove(int index) {
            size--;
            return new Integer(index);
        }
        @Override
        public void add(int index, Object value) {
            size++;
        }
    }

    /**
     * Decorates a list with a small set of changes.
     */
    private static class SparseDifferencesList extends AbstractList {
        private Map values = new HashMap();
        private List delegate;
        public SparseDifferencesList(List delegate) {
            this.delegate = delegate;
        }
        @Override
        public Object get(int index) {
            Object mapValue = values.get(new Integer(index));
            if(mapValue != null) return mapValue;

            return delegate.get(index);
        }
        @Override
        public int size() {
            return delegate.size();
        }

        @Override
        public Object set(int index, Object value) {
            return values.put(new Integer(index), value);
        }
        @Override
        public void add(int index, Object element) {
            delegate.add(index, element);
            set(index, element);
        }
        @Override
        public Object remove(int index) {
            return delegate.remove(index);
        }
    }

    /**
     * Simple test program for Diff.
     */
    public static void main(String[] args) {
        if (args.length != 2) {
            System.out.println("Usage: LCS <alpha> <beta>");
            return;
        }

        String alpha = args[ 0 ];
        EventList alphaList = new BasicEventList();
        for (int c = 0; c < alpha.length(); c++) {
            alphaList.add(new Character(alpha.charAt(c)));
        }

        String beta = args[ 1 ];
        List betaList = new ArrayList();
        for (int c = 0; c < beta.length(); c++) {
            betaList.add(new Character(beta.charAt(c)));
        }

        drawGrid(new Diff.ListDiffMatcher(alphaList, betaList, null));

        Diff.replaceAll(alphaList, betaList, false, null);
        System.out.println(alphaList);
    }

    /**
     * Draws a simple grid describing the specified matcher.
     */
    public static void drawGrid(Diff.DiffMatcher diffMatcher) {
        System.out.print("      ");
        for (int x = 0; x < diffMatcher.getAlphaLength(); x++) {
            System.out.print(x);
            if (x < 10) System.out.print(" ");
            if (x < 100) System.out.print(" ");
        }
        System.out.println("");
        System.out.print("      ");
        for (int x = 0; x < diffMatcher.getAlphaLength(); x++) {
            System.out.print(diffMatcher.alphaAt(x));
            System.out.print("  ");
        }
        System.out.println("");

        for (int y = 0; y < diffMatcher.getBetaLength(); y++) {
            System.out.print(y);
            if (y < 10) System.out.print(" ");
            if (y < 100) System.out.print(" ");
            System.out.print(" ");
            System.out.print(diffMatcher.betaAt(y));
            System.out.print(" ");
            for (int x = 0; x < diffMatcher.getAlphaLength(); x++) {
                boolean match = diffMatcher.matchPair(x, y);
                if (match)
                    System.out.print("_\\|");
                else
                    System.out.print("__|");
            }
            System.out.println("");
        }
    }


    /**
     * Matcher for Strings.
     */
    private static class StringDiffMatcher implements Diff.DiffMatcher {
        private String alpha;
        private String beta;

        public StringDiffMatcher(String alpha, String beta) {
            this.alpha = alpha;
            this.beta = beta;
        }

        @Override
        public int getAlphaLength() {
            return alpha.length();
        }

        @Override
        public char alphaAt(int index) {
            return alpha.charAt(index);
        }

        @Override
        public char betaAt(int index) {
            return beta.charAt(index);
        }

        @Override
        public int getBetaLength() {
            return beta.length();
        }

        @Override
        public boolean matchPair(int alphaIndex, int betaIndex) {
            return alpha.charAt(alphaIndex) == beta.charAt(betaIndex);
        }
    }


    /**
     * Counts how many ListEvents are received.
     */
    public static class ListEventCounter<E> implements ListEventListener<E> {

        /** count the number of changes per event */
        private List<Integer> changeCounts = new ArrayList<>();

        /**
         * When an event occurs, count that.
         */
        @Override
        public void listChanged(ListEvent<E> listChanges) {
            int changesForEvent = 0;
            while(listChanges.next()) {
                changesForEvent++;
            }
            changeCounts.add(new Integer(changesForEvent));
        }

        /**
         * Gets the number of events that have occured thus far.
         */
        public int getEventCount() {
            return changeCounts.size();
        }

        /**
         * Gets the number of changes for the specified event.
         */
        public int getChangeCount(int event) {
            return changeCounts.get(event).intValue();
        }
    }
}
