package ca.odell.glazedlists;

import ca.odell.glazedlists.TreeList.TreeElement;
import ca.odell.glazedlists.impl.testing.GlazedListsTests;
import ca.odell.glazedlists.impl.testing.ListConsistencyListener;
import junit.framework.TestCase;

import java.lang.reflect.Method;
import java.util.*;

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
        source.add(List.class.getMethod("add", new Class[] {Object.class}));
        source.add(List.class.getMethod("add", new Class[] {int.class, Object.class}));
        source.add(List.class.getMethod("set", new Class[] {int.class, Object.class}));
        source.add(List.class.getMethod("remove", new Class[] {int.class}));
        source.add(List.class.getMethod("clear", new Class[] {}));
        source.add(String.class.getMethod("toString", new Class[] {}));
        source.add(Date.class.getMethod("getTime", new Class[] {}));
        source.add(BasicEventList.class.getMethod("add", new Class[] {Object.class}));
        source.add(BasicEventList.class.getMethod("add", new Class[] {int.class, Object.class}));

        TreeList treeList = new TreeList(source, new JavaStructureTreeFormat());
    }

    /**
     * Convert Java methods into paths. For example, {@link Object#toString()}
     * is <code>/java/lang/Object/toString</code>
     */
    class JavaStructureTreeFormat implements TreeList.Format<Object> {

        public boolean supportsChildren(Object element) {
            return (!(element instanceof Method));
        }

        public void getPath(List<Object> path, Object element) {
            Method javaMethod = (Method)element;
            path.addAll(Arrays.asList(javaMethod.getDeclaringClass().getName().split("\\.")));

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

            path.add(signature.toString());
        }
    }

    /**
     * Convert Strings into paths. For example, PUPPY is <code>/P/U/P/P/Y</code>
     */
    class CharacterTreeFormat implements TreeList.Format<String> {
        public boolean supportsChildren(String element) {
            return true;
        }
        public void getPath(List<String> path, String element) {
            for(int c = 0; c < element.length(); c++) {
                path.add(element.substring(c, c + 1));
            }
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
        assertEquals(GlazedListsTests.stringToList("ABF"), ((TreeElement)treeList.get(8)).path());
        source.add("ABG");
        assertEquals(GlazedListsTests.stringToList("ABG"), ((TreeElement)treeList.get(9)).path());
        source.add("ACBA");
        assertEquals(GlazedListsTests.stringToList("ACB"), ((TreeElement)treeList.get(11)).path());
        assertEquals(GlazedListsTests.stringToList("ACBA"), ((TreeElement)treeList.get(12)).path());

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

    public void testSourceChangesOnCollapsedSubtrees() {
        EventList<String> source = new BasicEventList<String>();
        TreeList<String> treeList = new TreeList<String>(source, new CharacterTreeFormat());
        ListConsistencyListener.install(treeList);

        source.add("A");
        treeList.setExpanded(0, false);
        source.add("ABC");
        assertEquals(1, treeList.size());

        treeList.setExpanded(0, true);
        assertTreeStructure(treeList, new String[] {
                "A",
                "AB",
                "ABC",
        });

        treeList.setExpanded(1, false);
        source.add("AD");
        source.addAll(Arrays.asList(new String[] {
                "ABD",
                "ABE",
                "ABF",
                "ABG",
        }));

        assertTreeStructure(treeList, new String[] {
                "A",
                "AB",
                "AD",
        });

        treeList.setExpanded(0, false);
        source.addAll(Arrays.asList(new String[] {
                "ABH",
                "ABI",
        }));
        treeList.setExpanded(0, true);
        assertTreeStructure(treeList, new String[] {
                "A",
                "AB",
                "AD",
        });

        treeList.setExpanded(1, true);
        assertTreeStructure(treeList, new String[] {
                "A",
                "AB",
                "ABC",
                "ABD",
                "ABE",
                "ABF",
                "ABG",
                "ABH",
                "ABI",
                "AD",
        });

        treeList.setExpanded(0, false);
        source.removeAll(Arrays.asList(new String[] {
                "ABF",
                "ABG",
        }));
        treeList.setExpanded(0, true);
        assertTreeStructure(treeList, new String[] {
                "A",
                "AB",
                "ABC",
                "ABD",
                "ABE",
                "ABH",
                "ABI",
                "AD",
        });
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

    public void testCollapseExpand() {
        EventList<String> source = new BasicEventList<String>();
        TreeList<String> treeList = new TreeList<String>(source, new CharacterTreeFormat());
        ListConsistencyListener.install(treeList);

        source.add("ABC");
        source.add("ABD");
        source.add("ABE");
        source.add("ABFG");
        source.add("ABFH");
        source.add("DEF");
        source.add("GJ");
        assertTreeStructure(treeList, new String[] {
                "A",
                "AB",
                "ABC",
                "ABD",
                "ABE",
                "ABF",
                "ABFG",
                "ABFH",
                "D",
                "DE",
                "DEF",
                "G",
                "GJ",
        });

        // collapse 'E'
        treeList.setExpanded(9, false);
        assertTreeStructure(treeList, new String[] {
                "A",
                "AB",
                "ABC",
                "ABD",
                "ABE",
                "ABF",
                "ABFG",
                "ABFH",
                "D",
                "DE",
                "G",
                "GJ",
        });

        // collapse 'F'
        treeList.setExpanded(5, false);
        assertTreeStructure(treeList, new String[] {
                "A",
                "AB",
                "ABC",
                "ABD",
                "ABE",
                "ABF",
                "D",
                "DE",
                "G",
                "GJ",
        });

        // collapse 'B'
        treeList.setExpanded(1, false);
        assertTreeStructure(treeList, new String[] {
                "A",
                "AB",
                "D",
                "DE",
                "G",
                "GJ",
        });

        // collapse 'D'
        treeList.setExpanded(2, false);
        assertTreeStructure(treeList, new String[] {
                "A",
                "AB",
                "D",
                "G",
                "GJ",
        });

        // expand 'B'
        treeList.setExpanded(1, true);
        assertTreeStructure(treeList, new String[] {
                "A",
                "AB",
                "ABC",
                "ABD",
                "ABE",
                "ABF",
                "D",
                "G",
                "GJ",
        });

        // collapse 'A'
        treeList.setExpanded(0, false);
        assertTreeStructure(treeList, new String[] {
                "A",
                "D",
                "G",
                "GJ",
        });

        // expand 'A', 'F'
        treeList.setExpanded(0, true);
        treeList.setExpanded(5, true);
        assertTreeStructure(treeList, new String[] {
                "A",
                "AB",
                "ABC",
                "ABD",
                "ABE",
                "ABF",
                "ABFG",
                "ABFH",
                "D",
                "G",
                "GJ",
        });
    }

    public void testSourceUpdateEvents_FixMe() {
        EventList<String> source = new BasicEventList<String>();
        TreeList<String> treeList = new TreeList<String>(source, new CharacterTreeFormat());
        ListConsistencyListener.install(treeList);

        source.add("ABC");
        source.add("DEF");
        treeList.setExpanded(0, false);
        source.set(0, "ABD");
        assertTreeStructure(treeList, new String[] {
                "A",
                "D",
                "DE",
                "DEF",
        });

        source.set(1, "ABE");
        assertTreeStructure(treeList, new String[] {
                "A",
        });
    }
}
