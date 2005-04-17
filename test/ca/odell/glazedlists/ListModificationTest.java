/* Glazed Lists                                                 (c) 2003-2005 */
/* http://publicobject.com/glazedlists/                      publicboject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists;

// the Glazed Lists
import ca.odell.glazedlists.event.*;
// for being a JUnit test case
import junit.framework.*;
// standard collections
import java.util.*;

/**
 * The ListModificationTest verifies that modifications made to lists are
 * performed correctly. It also verifies that modifications made to transformation
 * lists have  the correct side effects on their source lists.
 *
 * @see <a href="https://glazedlists.dev.java.net/issues/show_bug.cgi?id=44">Bug 44</a>
 *
 * @author <a href="mailto:jesse@odel.on.ca">Jesse Wilson</a>
 */
public class ListModificationTest extends TestCase {

    
    /**
     * Tests that clearing a transformed list works and has the desired
     * side effects on the original list.
     */
    public void testSubListClear() {
        List alphabet = new BasicEventList();
        alphabet.add("A");
        alphabet.add("B");
        alphabet.add("C");
        alphabet.add("D");
        alphabet.add("E");
        alphabet.add("F");
        
        List controlList = new ArrayList();
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
    public void testFilterListClear() {
        EventList alphabet = new BasicEventList();
        alphabet.add("A");
        alphabet.add("B");
        alphabet.add("C");
        alphabet.add("D");
        alphabet.add("E");
        alphabet.add("F");
        
        List controlList = new ArrayList();
        controlList.addAll(alphabet);
        
        List vowels = new VowelFilterList(alphabet);
        vowels.clear();
        controlList.remove(0);
        controlList.remove(3);
        
        assertEquals(controlList, alphabet);
    }
    
    /**
     * Tests that removing from a transformed list works and has the desired
     * side effects on the original list.
     */
    public void testRemove() {
        EventList alphabet = new BasicEventList();
        alphabet.add("A");
        alphabet.add("B");
        alphabet.add("C");
        alphabet.add("D");
        alphabet.add("E");
        alphabet.add("F");
        
        List controlList = new ArrayList();
        controlList.addAll(alphabet);
        
        List vowels = new VowelFilterList(alphabet);
        vowels.remove("C");
        vowels.remove("A");
        controlList.remove("A");
        
        assertEquals(controlList, alphabet);
    }
    
    /**
     * Tests that removing from a transformed list works and has the desired
     * side effects on the original list.
     */
    public void testRemoveAll() {
        EventList alphabet = new BasicEventList();
        alphabet.add("A");
        alphabet.add("B");
        alphabet.add("C");
        alphabet.add("D");
        alphabet.add("E");
        alphabet.add("F");
        
        List controlList = new ArrayList();
        controlList.addAll(alphabet);
        
        List vowels = new VowelFilterList(alphabet);
        
        List toRemove = new ArrayList();
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
    public void testRetainAll() {
        EventList alphabet = new BasicEventList();
        alphabet.add("A");
        alphabet.add("B");
        alphabet.add("C");
        alphabet.add("D");
        alphabet.add("E");
        alphabet.add("F");
        
        List controlList = new ArrayList();
        controlList.addAll(alphabet);
        
        List vowels = new VowelFilterList(alphabet);
        
        List toRetain = new ArrayList();
        toRetain.add("C");
        toRetain.add("E");
        vowels.retainAll(toRetain);
        controlList.remove("A");
        
        assertEquals(controlList, alphabet);
    }

    /**
     * A simple filter that filters out anything that is not a vowel.
     */
    class VowelFilterList extends AbstractFilterList {
        public VowelFilterList(EventList source) {
            super(source);
            handleFilterChanged();
        }
        public boolean filterMatches(Object element) {
            String letter = (String)element;
            if(letter.length() != 1) return false;
            return ("AEIOUaeiou".indexOf(letter) != -1);
        }
    }
}
