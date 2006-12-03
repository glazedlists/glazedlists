/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package com.publicobject.misc.xml;

import java.text.DateFormat;
import java.text.ParseException;

/**
 * This factory class produces common implementations of the
 * {@link Converter} interface.
 *
 * @author James Lemieux
 */
public final class Converters {

    private Converters() {}

    /**
     * Returns a {@link Converter} capable of converting date Strings into
     * {@link java.util.Date} objects using any of the given <code>formats</code>.
     */
    public static Converter date(DateFormat[] formats) {
        return new DateConverter(formats);
    }

    /**
     * Returns a {@link Converter} capable of converting date Strings into
     * {@link java.util.Date} objects using the given <code>formats</code>.
     */
    public static Converter date(DateFormat format) {
        return date(new DateFormat[] { format });
    }

    /**
     * Returns a {@link Converter} capable of converting integer Strings into
     * {@link Integer} objects.
     */
    public static Converter integer() {
        return new IntegerConverter();
    }

    /**
     * Returns a {@link Converter} capable of trimming whitespace from the
     * beginning and end of String objects.
     */
    public static Converter trim() {
        return new TrimConverter();
    }

    /**
     * Returns a {@link Converter} capable of trimming whitespace from the
     * beginning and end of String objects and then
     * {@link String#intern interning} the resulting String.
     */
    public static Converter trimAndIntern() {
        return new TrimAndInternConverter();
    }

    private static class DateConverter implements Converter {
        private final DateFormat[] format;

        public DateConverter(DateFormat[] format) {
            this.format = format;
        }

        public Object convert(String value) {
            value = value.trim();

            // Format is most likely a SimpleDateFormat. From the SimpleDateFormat class doc:
            //
            // Date formats are not synchronized. It is recommended to create separate
            // format instances for each thread. If multiple threads access a format
            // concurrently, it must be synchronized externally.
            synchronized (format) {
                for (int i = 0; i < format.length; i++) {
                    try {
                        return format[i].parse(value);
                    } catch (ParseException e) {
                        if (i == format.length-1)
                            throw new RuntimeException(e);
                    }
                }
            }

            // this should never happen
            return null;
        }
    }

    private static class IntegerConverter implements Converter {
        public Object convert(String value) {
            return Integer.valueOf(value.trim());
        }
    }

    private static class TrimConverter implements Converter {
        public Object convert(String value) {
            return value.trim();
        }
    }

    private static class TrimAndInternConverter implements Converter {
        public Object convert(String value) {
            return value.trim().intern();
        }
    }
}