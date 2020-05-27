/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.impl.testing;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.FunctionList;
import ca.odell.glazedlists.event.ListEvent;
import ca.odell.glazedlists.event.ListEventListener;
import ca.odell.glazedlists.matchers.Matcher;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

/**
 * A factory class useful for testing!
 *
 * @author <a href="mailto:jesse@swank.ca">Jesse Wilson</a>
 */
public class GlazedListsTests {

    private static final Comparator<String> FIRST_LETTER_COMPARATOR = new FirstLetterComparator();
    private static final Comparator<String> LAST_LETTER_COMPARATOR = new LastLetterComparator();
    private static final FunctionList.Function<String, String> FIRST_LETTER_FUNCTION = new FirstLetterFunction();
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

        List<String> result = new ArrayList<>(strings.length);
        for (int i = 0; i < strings.length; i++) {
            result.add(strings[i]);
        }
        return result;
    }

    /**
     * Convert the characters of the specified String to a list.
     */
    public static List<String> stringToList(CharSequence chars) {
        List<String> result = new ArrayList<>(chars.length());
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
        List<List<String>> result = new ArrayList<>();
        String[] strings = chars.toString().split(",");
        for (int i = 0; i < strings.length; i++)
            result.add(stringToList(strings[i]));

        return result;
    }

    /**
     * Convert an array of Strings into a List of characters.
     */
    public static List<String> stringsToList(CharSequence[] data) {
        List<String> result = new ArrayList<>();
        for (int i = 0; i < data.length; i++)
            result.addAll(stringToList(data[i]));

        return result;
    }

    /**
     * Convert the specified int[] array to a List of Integers.
     */
    public static List<Integer> intArrayToIntegerCollection(int[] values) {
        List<Integer> result = new ArrayList<>();
        for (int i = 0; i < values.length; i++)
            result.add(new Integer(values[i]));

        return result;
    }

    /**
     * This matcher matches everything greater than its minimum.
     */
    public static Matcher<Number> matchAtLeast(int minimum) {
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

    private static class AtLeastMatcher implements Matcher<Number> {
        private final int minimum;
        public AtLeastMatcher(int minimum) {
            this.minimum = minimum;
        }
        @Override
        public boolean matches(Number value) {
            return value.intValue() >= minimum;
        }
    }

    /**
     * Returns a comparator for comparing Strings based solely on their first
     * character. <code>null</code> Strings are not tolerated and the
     * Comparator will throw {@link NullPointerException}.
     */
    public static Comparator<String> getFirstLetterComparator() {
        return FIRST_LETTER_COMPARATOR;
    }
    private static class FirstLetterComparator implements Comparator<String> {
        @Override
        public int compare(String o1, String o2) {
            return o1.charAt(0) - o2.charAt(0);
        }
    }

    /**
     * Returns a comparator for comparing Strings based solely on their last
     * character. <code>null</code> Strings are not tolerated and the
     * Comparator will throw {@link NullPointerException}.
     */
    public static Comparator<String> getLastLetterComparator() {
        return LAST_LETTER_COMPARATOR;
    }
    private static class LastLetterComparator implements Comparator<String> {
        @Override
        public int compare(String o1, String o2) {
            return o1.charAt(o1.length()-1) - o2.charAt(o2.length()-1);
        }
    }

    /**
     * A comparator for comparing integer arrays, which are particularly well
     * suited to sorting and filtering tests.
     */
    public static Comparator<int[]> intArrayComparator(int index) {
        return new IntArrayComparator(index);
    }
    private static class IntArrayComparator implements Comparator<int[]> {
        public int index;
        public IntArrayComparator(int index) {
            this.index = index;
        }
        @Override
        public int compare(int[] a, int[] b) {
            return a[index] - b[index];
        }
    }

    public static FunctionList.Function<String, String> getFirstLetterFunction() {
        return FIRST_LETTER_FUNCTION;
    }

    private static final class FirstLetterFunction implements FunctionList.Function<String, String> {
        @Override
        public String evaluate(String sourceValue) {
            return String.valueOf(sourceValue.charAt(0));
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

    public static <E> Runnable createJerkyAddRunnable2(EventList<E> list, E value, long count, long pause) {
        return new JerkyAddRunnable2(list, value, count, pause);
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

        @Override
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

    private static final class JerkyAddRunnable2 implements Runnable {
        private final EventList list;
        private final Object value;
        private final long count;
        private final long pause;

        public JerkyAddRunnable2(EventList list, Object value, long count, long pause) {
            if (count < 1)
                throw new IllegalArgumentException("count must be non-negative");
            if (pause < 1)
                throw new IllegalArgumentException("pause must be non-negative");

            this.list = list;
            this.value = value;
            this.count = count;
            this.pause = pause;
        }

        @Override
        public void run() {
            long run = 0;
            while (run < count) {
                // acquire the write lock and add a new element
                this.list.getReadWriteLock().writeLock().lock();
                try {
                    this.list.add(this.value);
                    run++;
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

        @Override
        public void listChanged(ListEvent<E> listChanges) {
            count++;
        }
        public int getCountAndReset() {
            int result = count;
            count = 0;
            return result;
        }
    }

    /**
     * This listener records the source of the last ListEvent received. This is
     * useful for testing ListEventListener serialization.
     */
    public static class SerializableListener implements ListEventListener, Serializable {
        private static EventList lastSource = null;

        @Override
        public void listChanged(ListEvent listChanges) {
            lastSource = listChanges.getSourceList();
        }
        public static EventList getLastSource() {
            return lastSource;
        }
    }

    /**
     * This listener is not serializable, but it shouldn't prevent serialization on an observing
     * {@link EventList}.
     */
    public static class UnserializableListener implements ListEventListener {
        private static EventList lastSource = null;

        @Override
        public void listChanged(ListEvent listChanges) {
            lastSource = listChanges.getSourceList();
        }
        public static EventList getLastSource() {
            return lastSource;
        }
    }
}