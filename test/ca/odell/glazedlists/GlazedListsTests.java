/* Glazed Lists                                                 (c) 2003-2005 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists;

import ca.odell.glazedlists.matchers.Matcher;

import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * A factory class useful for testing!
 *
 * @author <a href="mailto:jesse@swank.ca">Jesse Wilson</a>
 */
public class GlazedListsTests {

    /**
     * A dummy constructor to prevent instantiation of this class
     */
    private GlazedListsTests() {
        throw new UnsupportedOperationException();
    }


    /**
     * Convert the characters of the specified String to a list.
     */
    public static List<Character> stringToList(CharSequence chars) {
        List<Character> result = new ArrayList<Character>(chars.length());
        for (int i = 0; i < chars.length(); i++) {
            result.add(new Character(chars.charAt(i)));
        }
        return result;
    }

    /**
     * Convert an array of Strings into a List of characters.
     */
    public static List<Character> stringsToList(CharSequence[] data) {
        List<Character> result = new ArrayList<Character>();
        for(int s = 0; s < data.length; s++) {
            result.addAll(stringToList(data[s]));
        }
        return result;
    }

    /**
     * Convert the specified int[] array to a List of Integers.
     */
    public static List<Integer> intArrayToIntegerCollection(int[] values) {
        List<Integer> result = new ArrayList<Integer>();
        for(int i = 0; i < values.length; i++) {
            result.add(new Integer(values[i]));
        }
        return result;
    }

    /**
     * Manually apply the specified filter to the specified list.
     */
    public static <E> List<E> filter(List<E> input, Matcher<E> matcher) {
        List<E> result = new ArrayList<E>();
        for(Iterator<E> i = input.iterator(); i.hasNext(); ) {
            E element = i.next();
            if(matcher.matches(element)) result.add(element);
        }
        return result;
    }

    /**
     * This matcher matches everything greater than its minimum.
     */
    public static Matcher<Integer> matchAtLeast(final int minimum) {
        return new Matcher<Integer>() {
            public boolean matches(Integer value) {
                return value.intValue() >= minimum;
            }
        };
    }
}