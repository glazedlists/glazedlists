/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists;

// for being a JUnit test case
import ca.odell.glazedlists.matchers.Matcher;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * The ListModificationTest verifies that modifications made to lists are
 * performed correctly. It also verifies that modifications made to transformation
 * lists have  the correct side effects on their source lists.
 *
 * @see <a href="https://glazedlists.dev.java.net/issues/show_bug.cgi?id=44">Bug 44</a>
 *
 * @author <a href="mailto:jesse@swank.ca">Jesse Wilson</a>
 */
public class ListModificationTest {


    /**
     * Tests that clearing a transformed list works and has the desired
     * side effects on the original list.
     */
    @Test
    public void testSubListClear() {
        List<String> alphabet = new BasicEventList<>();
        alphabet.add("A");
        alphabet.add("B");
        alphabet.add("C");
        alphabet.add("D");
        alphabet.add("E");
        alphabet.add("F");

        List<String> controlList = new ArrayList<>();
        controlList.addAll(alphabet);

        alphabet.subList(1, 3).clear();
        controlList.remove(1);
        controlList.remove(1);

        assertEquals(controlList, alphabet);
    }

    public static void main(String[] args) {
        new ListModificationTest().testSubListClear();
    }

    /**
     * Tests that clearing a transformed list works and has the desired
     * side effects on the original list.
     */
    @Test
    public void testFilterListClear() {
        EventList<String> alphabet = new BasicEventList<>();
        alphabet.add("A");
        alphabet.add("B");
        alphabet.add("C");
        alphabet.add("D");
        alphabet.add("E");
        alphabet.add("F");

        List<String> controlList = new ArrayList<>();
        controlList.addAll(alphabet);

        List<String> vowels = new FilterList<>(alphabet, new VowelMatcher());
        vowels.clear();
        controlList.remove(0);
        controlList.remove(3);

        assertEquals(controlList, alphabet);
    }

    /**
     * Tests that removing from a transformed list works and has the desired
     * side effects on the original list.
     */
    @Test
    public void testRemove() {
        EventList<String> alphabet = new BasicEventList<>();
        alphabet.add("A");
        alphabet.add("B");
        alphabet.add("C");
        alphabet.add("D");
        alphabet.add("E");
        alphabet.add("F");

        List<String> controlList = new ArrayList<>();
        controlList.addAll(alphabet);

        List<String> vowels = new FilterList<>(alphabet, new VowelMatcher());
        vowels.remove("C");
        vowels.remove("A");
        controlList.remove("A");

        assertEquals(controlList, alphabet);
    }

    /**
     * Tests that removing from a transformed list works and has the desired
     * side effects on the original list.
     */
    @Test
    public void testRemoveAll() {
        EventList<String> alphabet = new BasicEventList<>();
        alphabet.add("A");
        alphabet.add("B");
        alphabet.add("C");
        alphabet.add("D");
        alphabet.add("E");
        alphabet.add("F");

        List<String> controlList = new ArrayList<>();
        controlList.addAll(alphabet);

        List<String> vowels = new FilterList<>(alphabet, new VowelMatcher());

        List<String> toRemove = new ArrayList<>();
        toRemove.add("C");
        toRemove.add("A");
        vowels.removeAll(toRemove);
        controlList.remove("A");

        assertEquals(controlList, alphabet);
    }

    /**
     * Tests that retaining from a transformed list works and has the desired
     * side effects on the original list.
     */
    @Test
    public void testRetainAll() {
        EventList<String> alphabet = new BasicEventList<>();
        alphabet.add("A");
        alphabet.add("B");
        alphabet.add("C");
        alphabet.add("D");
        alphabet.add("E");
        alphabet.add("F");

        List<String> controlList = new ArrayList<>();
        controlList.addAll(alphabet);

        List<String> vowels = new FilterList<>(alphabet, new VowelMatcher());

        List<String> toRetain = new ArrayList<>();
        toRetain.add("C");
        toRetain.add("E");
        vowels.retainAll(toRetain);
        controlList.remove("A");

        assertEquals(controlList, alphabet);
    }

    /**
     * A simple filter that filters out anything that is not a vowel.
     */
    static class VowelMatcher implements Matcher<String> {
        @Override
        public boolean matches(String letter) {
            if(letter.length() != 1) return false;
            return ("AEIOUaeiou".indexOf(letter) != -1);
        }
    }
}
