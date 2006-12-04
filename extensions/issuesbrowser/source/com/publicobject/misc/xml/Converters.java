/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package com.publicobject.misc.xml;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;

/**
 * This factory class produces common implementations of the
 * {@link Converter} interface.
 *
 * @author James Lemieux
 */
public final class Converters {

    private Converters() {}

    /**
     * Returns a {@link Converter} that returns the input object. This is
     * allows us to use the "Null Object Pattern" with converters.
     */
    public static <T> Converter<T,T> identityConverter() {
        return new IdentityConverter<T>();
    }

    /**
     * Returns a {@link Converter} capable of converting date Strings into
     * {@link java.util.Date} objects using any of the given <code>formats</code>.
     */
    public static Converter<String,Date> date(DateFormat[] formats) {
        return new DateConverter(formats);
    }

    /**
     * Returns a {@link Converter} capable of converting date Strings into
     * {@link java.util.Date} objects using the given <code>formats</code>.
     */
    public static Converter<String,Date> date(DateFormat format) {
        return date(new DateFormat[] { format });
    }

    /**
     * Returns a {@link Converter} capable of converting integer Strings into
     * {@link Integer} objects.
     */
    public static Converter<String,Integer> integer() {
        return new IntegerConverter();
    }

    /**
     * Returns a {@link Converter} capable of trimming whitespace from the
     * beginning and end of String objects.
     */
    public static Converter<String,String> trim() {
        return new TrimConverter();
    }

    /**
     * Returns a {@link Converter} capable of trimming whitespace from the
     * beginning and end of String objects and then
     * {@link String#intern interning} the resulting String.
     */
    public static Converter<String,String> trimAndIntern() {
        return new TrimAndInternConverter();
    }

    private static class DateConverter implements Converter<String, Date> {
        private final DateFormat[] format;

        public DateConverter(DateFormat[] format) {
            this.format = format;
        }

        public Date convert(String value) {
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

    private static class IntegerConverter implements Converter<String,Integer> {
        public Integer convert(String value) {
            return Integer.valueOf(value.trim());
        }
    }

    private static class TrimConverter implements Converter<String,String> {
        public String convert(String value) {
            return value.trim();
        }
    }

    private static class TrimAndInternConverter implements Converter<String,String> {
        public String convert(String value) {
            return value.trim().intern();
        }
    }

    private static class IdentityConverter<T> implements Converter<T,T> {
        public T convert(T value) {
            return value;
        }
    }
}