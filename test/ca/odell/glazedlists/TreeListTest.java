package ca.odell.glazedlists;

import junit.framework.TestCase;

import java.util.*;
import java.lang.reflect.Method;

/**
 * Verifies that EventList matches the List API.
 *
 * @author <a href="mailto:jesse@swank.ca">Jesse Wilson</a>
 */
public class TreeListTest extends TestCase {

    /**
     * Can we build a tree list?
     */
    public void testCreate() throws Exception {
        BasicEventList<Method> source = new BasicEventList<Method>();
        source.add(List.class.getMethod("add", Object.class));
        source.add(List.class.getMethod("add", int.class, Object.class));
        source.add(List.class.getMethod("set", int.class, Object.class));
        source.add(List.class.getMethod("remove", int.class));
        source.add(List.class.getMethod("clear"));
        source.add(String.class.getMethod("toString"));
        source.add(Date.class.getMethod("getTime"));
        source.add(BasicEventList.class.getMethod("add", Object.class));
        source.add(BasicEventList.class.getMethod("add", int.class, Object.class));

        TreeList treeList = new TreeList(source, new JavaStructureTreeFormat());
    }

    /**
     * Convert Java methods into paths. For example, {@link Object#toString()}
     * is <code>/java/lang/Object/toString</code>
     */
    class JavaStructureTreeFormat implements TreeList.Format {
        public List getPath(Object object) {
            Method javaMethod = (Method)object;
            List result = new ArrayList();
            result.addAll(Arrays.asList(javaMethod.getDeclaringClass().getName().split("\\.")));

            StringBuffer signature = new StringBuffer();
            signature.append(javaMethod.getName());

            // print the arguments list
            signature.append("(");
            Class<?>[] parameterTypes = javaMethod.getParameterTypes();
            for(int c = 0; c < parameterTypes.length; c++) {
                if(c > 0) signature.append(", ");
                String[] parameterClassNameParts = parameterTypes[c].getName().split("\\.");
                signature.append(parameterClassNameParts[parameterClassNameParts.length - 1]);
            }
            signature.append(")");

            result.add(signature.toString());
            return result;
        }
    }

    /**
     * Convert Strings into paths. For example, PUPPY is <code>/P/U/P/P/Y</code>
     */
    class CharacterTreeFormat implements TreeList.Format<String> {
        public List<String> getPath(String element) {
            List<String> result = new ArrayList<String>(element.length());
            for(int c = 0; c < element.length(); c++) {
                result.add(element.substring(c, c + 1));
            }
            return result;
        }
    }


    public void testAddsAndRemoves() {
        BasicEventList<String> source = new BasicEventList<String>();
        source.add("ABC");
        source.add("ABD");
        source.add("ABEFG");
        source.add("ABEFH");
        source.add("ACD");
        source.add("ACE");
        TreeList<String> treeList = new TreeList<String>(source, new CharacterTreeFormat());
        assertTreeStructure(treeList, new String[] {
                "A",
                "AB",
                "ABC",
                "ABD",
                "ABE",
                "ABEF",
                "ABEFG",
                "ABEFH",
                "AC",
                "ACD",
                "ACE",
        });

        // observe that we're firing the right events
        ListConsistencyListener.install(treeList);

        // make some modifications, they should be handled in place
        source.add("ABF");
        assertEquals(GlazedListsTests.stringToList("ABF"), treeList.get(8).path());
        source.add("ABG");
        assertEquals(GlazedListsTests.stringToList("ABG"), treeList.get(9).path());
        source.add("ACBA");
        assertEquals(GlazedListsTests.stringToList("ACB"), treeList.get(11).path());
        assertEquals(GlazedListsTests.stringToList("ACBA"), treeList.get(12).path());

        // now some removes
        source.remove("ABD");
        assertEquals(14, treeList.size());
        source.remove("ACD");
        assertEquals(13, treeList.size());
        source.remove("ABEFH");
        assertEquals(12, treeList.size());
    }

    public void testSubtreeSize() {

        // try a simple hierarchy
        BasicEventList<String> source = new BasicEventList<String>();
        source.add("ABCD");
        source.add("ABEFG");
        source.add("ACDC");
        source.add("ACE");
        TreeList<String> treeList = new TreeList<String>(source, new CharacterTreeFormat());
        assertTreeStructure(treeList, new String[] {
                "A",
                "AB",
                "ABC",
                "ABCD",
                "ABE",
                "ABEF",
                "ABEFG",
                "AC",
                "ACD",
                "ACDC",
                "ACE",
        });

        assertEquals(11, treeList.subtreeSize(0, true));
        assertEquals(6, treeList.subtreeSize(1, true));
        assertEquals(2, treeList.subtreeSize(2, true));
        assertEquals(1, treeList.subtreeSize(3, true));
        assertEquals(3, treeList.subtreeSize(4, true));
        assertEquals(2, treeList.subtreeSize(5, true));
        assertEquals(1, treeList.subtreeSize(6, true));
        assertEquals(4, treeList.subtreeSize(7, true));
        assertEquals(2, treeList.subtreeSize(8, true));
        assertEquals(1, treeList.subtreeSize(9, true));
        assertEquals(1, treeList.subtreeSize(10, true));
    }

    public void testVirtualParentsAreCleanedUp() {
        EventList<String> source = new BasicEventList<String>();
        TreeList<String> treeList = new TreeList<String>(source, new CharacterTreeFormat());
        ListConsistencyListener.install(treeList);

        source.add("ABG");
        source.add("ACDEF");
        source.add("AHI");
        assertTreeStructure(treeList, new String[] {
                "A",
                "AB",
                "ABG",
                "AC",
                "ACD",
                "ACDE",
                "ACDEF",
                "AH",
                "AHI",
        });

        source.remove("ACDEF");
        assertTreeStructure(treeList, new String[] {
                "A",
                "AB",
                "ABG",
                "AH",
                "AHI",
        });

        source.remove("ABG");
        assertTreeStructure(treeList, new String[] {
                "A",
                "AH",
                "AHI",
        });

        source.remove("AHI");
        assertEquals(0, treeList.size());

        // but only virtual parents are cleaned up
        source.add("ABC");
        source.add("ABCDEF");
        source.remove("ABCDEF");
        assertEquals(3, treeList.size());
    }

    public void testInsertRealOverVirtualParent() {
        EventList<String> source = new BasicEventList<String>();
        TreeList<String> treeList = new TreeList<String>(source, new CharacterTreeFormat());
        ListConsistencyListener.install(treeList);

        source.add("ABCDEF");
        source.add("ABC");
        assertEquals(6, treeList.size());
    }

    public void testStructureChangingUpdates() {
        EventList<String> source = new BasicEventList<String>();
        TreeList<String> treeList = new TreeList<String>(source, new CharacterTreeFormat());
        ListConsistencyListener.install(treeList);

        source.add("A");
        source.add("DEF");
        source.add("GJ");

        // swap dramatically, moving to a new subtree
        source.set(1, "DHI");
        assertTreeStructure(treeList, new String[] {
                "A",
                "D",
                "DH",
                "DHI",
                "G",
                "GJ",
        });

        // now move deeper
        source.set(1, "DHIJK");
        assertTreeStructure(treeList, new String[] {
                "A",
                "D",
                "DH",
                "DHI",
                "DHIJ",
                "DHIJK",
                "G",
                "GJ",
        });

        // now move shallower
        source.set(1, "DH");
        assertTreeStructure(treeList, new String[] {
                "A",
                "D",
                "DH",
                "G",
                "GJ",
        });

        // now move to another subtree after this one
        source.set(1, "GAB");
        assertTreeStructure(treeList, new String[] {
                "A",
                "G",
                "GA",
                "GAB",
                "GJ",
        });

        // now move to another subtree before this one
        source.set(1, "ABC");
        assertTreeStructure(treeList, new String[] {
                "A",
                "AB",
                "ABC",
                "G",
                "GJ",
        });
    }

    public void testClear() {
        EventList<String> source = new BasicEventList<String>();
        TreeList<String> treeList = new TreeList<String>(source, new CharacterTreeFormat());
        ListConsistencyListener.install(treeList);

        source.add("ABC");
        source.add("DEF");
        source.add("ABH");
        source.add("DZK");
        source.clear();
        assertEquals(0, treeList.size());
    }

    public void assertTreeStructure(TreeList<String>treeList, String[] structure) {

        // convert the list of TreeElements into a list of Strings
        List<String> treeAsStrings = new ArrayList<String>();
        for(Iterator<TreeList.TreeElement<String>> i = treeList.iterator(); i.hasNext(); ) {
            TreeList.TreeElement<String> treeElement = i.next();
            StringBuffer asString = new StringBuffer(treeElement.path().size());
            for(Iterator<String> n = treeElement.path().iterator(); n.hasNext(); ) {
                asString.append(n.next());
            }
            treeAsStrings.add(asString.toString());
        }

        assertEquals(Arrays.asList(structure), treeAsStrings);
    }

}
