/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists;

import ca.odell.glazedlists.matchers.Matcher;
import ca.odell.glazedlists.event.ListEventListener;
import ca.odell.glazedlists.event.ListEvent;

import java.util.*;
import java.io.*;

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
     * Convert a String like "Apple Banana Cat" into a single list:
     * { "Apple", "Banana", "Cat" }
     */
    public static List<String> delimitedStringToList(String delimited) {
        final String[] strings = delimited.split("\\s");

        List<String> result = new ArrayList<String>(strings.length);
        for (int i = 0; i < strings.length; i++) {
            result.add(strings[i]);
        }
        return result;
    }

    /**
     * Convert the characters of the specified String to a list.
     */
    public static List<String> stringToList(CharSequence chars) {
        List<String> result = new ArrayList<String>(chars.length());
        for (int i = 0; i < chars.length(); i++) {
            result.add(chars.subSequence(i, i+1).toString());
        }
        return result;
    }

    /**
     * Convert a String like "AA,BB,CDE" into three Lists:
     * { [ A A ], [ B B ], [ C D E ] }
     */
    public static List<List<String>> stringToLists(CharSequence chars) {
        List<List<String>> result = new ArrayList<List<String>>();
        String[] strings = chars.toString().split(",");
        for(int i = 0; i < strings.length; i++) {
            result.add(stringToList(strings[i]));
        }
        return result;
    }

    /**
     * Convert an array of Strings into a List of characters.
     */
    public static List<String> stringsToList(CharSequence[] data) {
        List<String> result = new ArrayList<String>();
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
     * Returns an EventList which delays each read and write operation by
     * the given <code>delay</code> (in milliseconds).
     */
    public static <E> EventList<E> delayList(EventList<E> source, long delay) {
        return new DelayList<E>(source, delay);
    }

    /**
     * This matcher matches everything greater than its minimum.
     */
    public static Matcher<Integer> matchAtLeast(int minimum) {
        return new AtLeastMatcher(minimum);
    }

    /**
     * Serialize the specified object to bytes, then deserialize it back.
     */
    public static <T> T serialize(T object) throws IOException, ClassNotFoundException {
        return (T)fromBytes(toBytes(object));
    }

    public static byte[] toBytes(Object object) throws IOException {
        ByteArrayOutputStream bytesOut = new ByteArrayOutputStream();
        ObjectOutputStream objectsOut = new ObjectOutputStream(bytesOut);
        objectsOut.writeObject(object);
        return bytesOut.toByteArray();
    }

    public static Object fromBytes(byte[] bytes) throws IOException, ClassNotFoundException {
        ByteArrayInputStream bytesIn = new ByteArrayInputStream(bytes);
        ObjectInputStream objectsIn = new ObjectInputStream(bytesIn);
        return objectsIn.readObject();
    }

    public static String toString(byte[] bytes) {
        StringBuffer result = new StringBuffer();
        for(int b = 0; b < bytes.length; b++) {
            result.append(bytes[b] < 0 ? "-" : " ");
            String hexString = Integer.toString(Math.abs(bytes[b]), 16);
            while(hexString.length() < 2) hexString = "0" + hexString;
            result.append("0x").append(hexString).append(", ");
            if(b % 16 == 15) result.append("\n");
        }
        return result.toString();
    }

    private static class AtLeastMatcher implements Matcher<Integer> {
        private final int minimum;
        public AtLeastMatcher(int minimum) {
            this.minimum = minimum;
        }
        public boolean matches(Integer value) {
            return value.intValue() >= minimum;
        }
    }

    /**
     * A comparator for comparing integer arrays, which are particularly well
     * suited to sorting and filtering tests.
     */
    public static Comparator intArrayComparator(int index) {
        return new IntArrayComparator(index);
    }
    private static class IntArrayComparator implements Comparator<int[]> {
        public int index;
        public IntArrayComparator(int index) {
            this.index = index;
        }
        public int compare(int[] a, int[] b) {
            return a[index] - b[index];
        }
    }

    public static Date createDate(int year, int month, int date) {
        Calendar cal = Calendar.getInstance();
        cal.clear();
        cal.set(Calendar.YEAR, year);
        cal.set(Calendar.MONTH, month);
        cal.set(Calendar.DATE, date);
        return cal.getTime();
    }

    /**
     * Create a Runnable which executes the following logic repeatedly for the
     * given duration:
     *
     * <ol>
     *   <li> acquires the write lock for the list
     *   <li> adds the value to the end of the list
     *   <li> releases the write lock for the list
     *   <li> pauses for the given pause (in milliseconds)
     * </ol>
     */
    public static <E> Runnable createJerkyAddRunnable(EventList<E> list, E value, long duration, long pause) {
        return new JerkyAddRunnable(list, value, duration, pause);
    }

    private static final class JerkyAddRunnable implements Runnable {
        private final EventList list;
        private final Object value;
        private final long duration;
        private final long pause;

        public JerkyAddRunnable(EventList list, Object value, long duration, long pause) {
            if (duration < 1)
                throw new IllegalArgumentException("duration must be non-negative");
            if (pause < 1)
                throw new IllegalArgumentException("pause must be non-negative");

            this.list = list;
            this.value = value;
            this.duration = duration;
            this.pause = pause;
        }

        public void run() {
            final long endTime = System.currentTimeMillis() + this.duration;

            while (System.currentTimeMillis() < endTime) {
                // acquire the write lock and add a new element
                this.list.getReadWriteLock().writeLock().lock();
                try {
                    this.list.add(this.value);
                } finally {
                    this.list.getReadWriteLock().writeLock().unlock();
                }

                // pause before adding another element
                try {
                    Thread.sleep(this.pause);
                } catch (InterruptedException e) {
                    // best attempt only
                }
            }
        }
    }

    /**
     * Counts the number of ListEvents fired.
     */
    public static class ListEventCounter<E> implements ListEventListener<E> {
        private int count = 0;

        public void listChanged(ListEvent<E> listChanges) {
            count++;
        }
        public int getCountAndReset() {
            int result = count;
            count = 0;
            return result;
        }
    }
}