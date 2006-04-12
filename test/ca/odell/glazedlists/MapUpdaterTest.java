/* Glazed Lists                                                 (c) 2003-2005 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists;

import junit.framework.TestCase;

import java.util.Map;

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
}