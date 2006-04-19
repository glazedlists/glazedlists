/* Glazed Lists                                                 (c) 2003-2005 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists;

import junit.framework.TestCase;

import java.util.Map;
import java.util.List;
import java.util.Arrays;
import java.util.Comparator;

/**
 * @author <a href="mailto:jesse@swank.ca">Jesse Wilson</a>
 */
public class MapUpdaterTest extends TestCase {

    public void testSimpleMap() {

        EventList<String> words = new BasicEventList<String>();
        words.add("Jodie");
        words.add("Ashford");
        words.add("Wilson");

        Map<String,String> map = MapUpdater.mapForEventList(words, new FirstLetterFunction(), MapUpdater.IDENTITY_FUNCTION);
        assertEquals(3, map.size());
        assertEquals("Jodie", map.get("J"));
        assertEquals("Ashford", map.get("A"));
        assertEquals("Wilson", map.get("W"));

        words.remove("Ashford");
        assertEquals(2, map.size());

        words.add("Lemieux");
        words.add("Maltby");
        assertEquals(4, map.size());

        words.set(0, "Jesse");
        assertEquals(4, map.size());
        assertEquals("Jesse", map.get("J"));
        assertEquals("Lemieux", map.get("L"));
        assertEquals("Maltby", map.get("M"));
        assertEquals("Wilson", map.get("W"));
    }

    private static final class FirstLetterFunction implements FunctionList.Function<String,String> {
        public String evaluate(String sourceValue) {
            if(sourceValue == null || sourceValue.length() == 0) return "";
            return sourceValue.substring(0, 1);
        }
    }


    public void testClearSrcListWithGrouping() {
        final EventList<String> words = new BasicEventList<String>();
        final GroupingList<String> wordGroups = new GroupingList<String>(words, new FunctionComparator<String, String>(new FirstLetterFunction()));
        Map<String, List<String>> wordIndex = MapUpdater.mapForEventList(
                wordGroups,
                new FunctionChain<List<String>, String, String>(new FirstElementFunction<String>(), new FirstLetterFunction()),
                MapUpdater.IDENTITY_FUNCTION
        );

        assertEquals(0, wordIndex.size());
        words.add("Jodie");
        words.add("Ashford");
        words.add("Wilson");
        assertEquals(3, wordIndex.size());
        assertEquals(Arrays.asList(new String[] { "Jodie" }), wordIndex.get("J"));
        assertEquals(Arrays.asList(new String[] { "Ashford" }), wordIndex.get("A"));
        assertEquals(Arrays.asList(new String[] { "Wilson" }), wordIndex.get("W"));
        words.clear();
    }


    public static class FirstElementFunction<T> implements FunctionList.Function<List<T>, T> {
        public T evaluate(final List<T> sourceValue) {
            return sourceValue != null && sourceValue.size() > 0 ? sourceValue.get(0) : null;
        }
    }

    /**
     * Chain functions.
     */
    public static class FunctionChain<A,B,C> implements FunctionList.Function<A, C> {
        private final FunctionList.Function<A, B> aToB;
        private final FunctionList.Function<B, C> bToC;

        public FunctionChain(final FunctionList.Function<A, B> aToB, final FunctionList.Function<B, C> bToC) {
            this.aToB = aToB;
            this.bToC = bToC;
        }

        public C evaluate(final A sourceValue) {
            return bToC.evaluate(aToB.evaluate(sourceValue));
        }
    }

    public static class FunctionComparator<A,B extends Comparable<B>> implements Comparator<A> {
        private final FunctionList.Function<A, B> function;

        public FunctionComparator(final FunctionList.Function<A, B> function) {
            this.function = function;
        }

        public int compare(final A a1, final A a2) {
            final B bc1 = function.evaluate(a1);
            final B bc2 = function.evaluate(a2);
            return bc1.compareTo(bc2);
        }
    }
}