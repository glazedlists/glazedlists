package ca.odell.glazedlists;

import ca.odell.glazedlists.impl.testing.GlazedListsTests;
import ca.odell.glazedlists.impl.testing.ListConsistencyListener;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Verifies that TreeList behaves as expected.
 *
 * @author <a href="mailto:jesse@swank.ca">Jesse Wilson</a>
 */
public class TreeListTest {

    public static final TreeList.Format<String> COMPRESSED_CHARACTER_TREE_FORMAT = new CharacterTreeFormat();
    public static final TreeList.Format<String> UNCOMPRESSED_CHARACTER_TREE_FORMAT = new CharacterTreeFormat(null);

    /**
     * Can we build a tree list?
     */
    @Test
    public void testCreateAndDispose() throws Exception {
        BasicEventList<Object> source = new BasicEventList<Object>();
        source.add(List.class.getMethod("add", new Class[] {Object.class}));
        source.add(List.class.getMethod("add", new Class[] {int.class, Object.class}));
        source.add(List.class.getMethod("set", new Class[] {int.class, Object.class}));
        source.add(List.class.getMethod("remove", new Class[] {int.class}));
        source.add(List.class.getMethod("clear", new Class[] {}));
        source.add(String.class.getMethod("toString", new Class[] {}));
        source.add(Date.class.getMethod("getTime", new Class[] {}));
        source.add(BasicEventList.class.getMethod("add", new Class[] {Object.class}));
        source.add(BasicEventList.class.getMethod("add", new Class[] {int.class, Object.class}));

        // create a sorted treelist
        TreeList<Object> treeList = new TreeList<Object>(source, new JavaStructureTreeFormat(), TreeList.<Object>nodesStartExpanded());
        treeList.dispose();

        // create an unsorted treelist
        treeList = new TreeList<Object>(source, new JavaStructureTreeFormat(), TreeList.<Object>nodesStartExpanded());
        treeList.dispose();
    }

    /**
     * Convert Java methods into paths. For example, {@link Object#toString()}
     * is <code>/java/lang/Object/toString</code>
     */
    static class JavaStructureTreeFormat implements TreeList.Format<Object> {

        @Override
        public boolean allowsChildren(Object element) {
            return (!(element instanceof Method));
        }

        @Override
        public void getPath(List<Object> path, Object element) {
            Method javaMethod = (Method)element;
            path.addAll(Arrays.asList(javaMethod.getDeclaringClass().getName().split("\\.")));

            StringBuffer signature = new StringBuffer();
            signature.append(javaMethod.getName());

            // print the arguments list
            signature.append("(");
            Class<?>[] parameterTypes = javaMethod.getParameterTypes();
            for(int c = 0; c < parameterTypes.length; c++) {
                if(c > 0) {
                    signature.append(", ");
                }
                String[] parameterClassNameParts = parameterTypes[c].getName().split("\\.");
                signature.append(parameterClassNameParts[parameterClassNameParts.length - 1]);
            }
            signature.append(")");

            path.add(signature.toString());
        }


        @Override
        public Comparator<Object> getComparator(int depth) {
            return (Comparator)GlazedLists.comparableComparator();
        }
    }

    @Test
    public void testAddsAndRemoves() {
        BasicEventList<String> source = new BasicEventList<String>();
        source.add("ABC");
        source.add("ABD");
        source.add("ABEFG");
        source.add("ABEFH");
        source.add("ACD");
        source.add("ACE");
        TreeList<String> treeList = new TreeList<String>(source, COMPRESSED_CHARACTER_TREE_FORMAT, TreeList.<String>nodesStartExpanded());
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
        ListConsistencyListener<String> listConsistencyListener = ListConsistencyListener.install(treeList);
        listConsistencyListener.setPreviousElementTracked(false);

        // make some modifications, they should be handled in place
        source.add("ABF");
        assertEquals(GlazedListsTests.stringToList("ABF"), treeList.getTreeNode(8).path());
        source.add("ABG");
        assertEquals(GlazedListsTests.stringToList("ABG"), treeList.getTreeNode(9).path());
        source.add("ACBA");
        assertEquals(GlazedListsTests.stringToList("ACB"), treeList.getTreeNode(11).path());
        assertEquals(GlazedListsTests.stringToList("ACBA"), treeList.getTreeNode(12).path());

        // now some removes
        source.remove("ABD");
        assertEquals(14, treeList.size());
        source.remove("ACD");
        assertEquals(13, treeList.size());
        source.remove("ABEFH");
        assertEquals(12, treeList.size());
    }

    @Test
    public void testRemoveByIndexRealNodeWithVirtualParents() {
        BasicEventList<String> source = new BasicEventList<String>();
        source.add("ABC");
        TreeList<String> treeList = new TreeList<String>(source, COMPRESSED_CHARACTER_TREE_FORMAT, TreeList.<String>nodesStartExpanded());
        assertTreeStructure(treeList, new String[] {
                "A",
                "AB",
                "ABC"
        });
        assertEquals(3, treeList.size());
        assertTrue(treeList.getTreeNode(0).isVirtual());
        assertTrue(treeList.getTreeNode(1).isVirtual());
        assertFalse(treeList.getTreeNode(2).isVirtual());
        assertEquals("C", treeList.remove(2));
        assertEquals(0, treeList.size());
        assertTreeStructure(treeList, new String[] { });
    }

    @Test
    public void testRemoveByIndexRealNodeWithRealParents() {
        BasicEventList<String> source = new BasicEventList<String>();
        source.add("A");
        source.add("AB");
        source.add("ABC");
        TreeList<String> treeList = new TreeList<String>(source, COMPRESSED_CHARACTER_TREE_FORMAT, TreeList.<String>nodesStartExpanded());
        assertTreeStructure(treeList, new String[] {
                "A",
                "AB",
                "ABC"
        });
        assertEquals(3, treeList.size());
        assertFalse(treeList.getTreeNode(0).isVirtual());
        assertFalse(treeList.getTreeNode(1).isVirtual());
        assertFalse(treeList.getTreeNode(2).isVirtual());
        assertEquals("C", treeList.remove(2));
        assertEquals(2, treeList.size());
        assertTreeStructure(treeList, new String[] {
                "A",
                "AB"
        });
    }

    @Test
    public void testRemoveByIndexRealNodeWithMixedParents() {
        BasicEventList<String> source = new BasicEventList<String>();
        source.add("A");
        source.add("AB");
        source.add("ABC");
        TreeList<String> treeList = new TreeList<String>(source, COMPRESSED_CHARACTER_TREE_FORMAT, TreeList.<String>nodesStartExpanded());
        assertTreeStructure(treeList, new String[] {
                "A",
                "AB",
                "ABC"
        });
        assertEquals(3, treeList.size());
        assertFalse(treeList.getTreeNode(0).isVirtual());
        assertFalse(treeList.getTreeNode(1).isVirtual());
        assertFalse(treeList.getTreeNode(2).isVirtual());
        // removing real parent with real child causes creation of virtual parent
        assertEquals("B", treeList.remove(1));
        assertEquals(3, treeList.size());
        assertFalse(treeList.getTreeNode(0).isVirtual());
        assertTrue(treeList.getTreeNode(1).isVirtual());
        assertFalse(treeList.getTreeNode(2).isVirtual());
        assertTreeStructure(treeList, new String[] {
                "A",
                "AB",
                "ABC"
        });
        assertEquals("C", treeList.remove(2));
        assertEquals(1, treeList.size());
        assertTreeStructure(treeList, new String[] {
                "A"
        });
    }

    @Test
    public void testRemoveByIndexVirtualNodeNotSupported() {
        BasicEventList<String> source = new BasicEventList<String>();
        source.add("ABC");
        TreeList<String> treeList = new TreeList<String>(source, COMPRESSED_CHARACTER_TREE_FORMAT, TreeList.<String>nodesStartExpanded());
        assertTreeStructure(treeList, new String[] {
                "A",
                "AB",
                "ABC"
        });
        assertEquals(3, treeList.size());
        assertTrue(treeList.getTreeNode(0).isVirtual());
        assertTrue(treeList.getTreeNode(1).isVirtual());
        assertFalse(treeList.getTreeNode(2).isVirtual());
        try {
        	treeList.remove(1);
        	fail("removing virtual node should not succeed, because it's not supported (yet)");
        } catch (IndexOutOfBoundsException ex) {
        	// expected, only real nodes can be removed like that currently
        }
    }

    /** Test for <a href="https://glazedlists.dev.java.net/issues/show_bug.cgi?id=489">bug 489</a> */
    @Ignore("Fix me")
    @Test
    public void testDeletionIssues_FixMe() {
        EventList<String> source = new BasicEventList<String>();
        TreeList<String> treeList = new TreeList<String>(source,
                COMPRESSED_CHARACTER_TREE_FORMAT, TreeList.NODES_START_EXPANDED);
        ListConsistencyListener<String> listConsistencyListener = ListConsistencyListener
                .install(treeList);
        listConsistencyListener.setPreviousElementTracked(false);

        source.add("A");
        source.add("AB");
        assertTreeStructure(treeList, new String[] {"A", "AB",});

        source.add(0, "A");
        assertTreeStructure(treeList, new String[] {"A", "A", "AB",});
        source.remove(1);
        assertTreeStructure(treeList, new String[] {"A", "AB",});
        source.set(1, "AB");

        assertTreeStructure(treeList, new String[] {"A", "AB",});

    }

    @Test
    public void testSubtreeSize() {

        // try a simple hierarchy
        BasicEventList<String> source = new BasicEventList<String>();
        source.add("ABCD");
        source.add("ABEFG");
        source.add("ACDC");
        source.add("ACE");
        TreeList<String> treeList = new TreeList<String>(source, COMPRESSED_CHARACTER_TREE_FORMAT, TreeList.<String>nodesStartExpanded());
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

    @Test
    public void testSetComparator() {

        // try a simple hierarchy
        BasicEventList<String> source = new BasicEventList<String>();
        source.add("ABCd");
        source.add("ABCe");
        source.add("ABCF");
        source.add("ABCG");
        SortedList<String> sortedSource = new SortedList<String>(source, null);

        TreeList<String> treeList = new TreeList<String>(sortedSource, COMPRESSED_CHARACTER_TREE_FORMAT, TreeList.<String>nodesStartExpanded());
        assertTreeStructure(treeList, new String[] {
                "A",
                "AB",
                "ABC",
                "ABCd",
                "ABCe",
                "ABCF",
                "ABCG",
        });

        sortedSource.setComparator(GlazedLists.reverseComparator());
        assertTreeStructure(treeList, new String[] {
                "A",
                "AB",
                "ABC",
                "ABCe",
                "ABCd",
                "ABCF",
                "ABCG",
        });
    }

    @Test
    public void testVirtualParentsAreCleanedUp() {
        EventList<String> source = new BasicEventList<String>();
        TreeList<String> treeList = new TreeList<String>(source, COMPRESSED_CHARACTER_TREE_FORMAT, TreeList.<String>nodesStartExpanded());
        ListConsistencyListener<String> listConsistencyListener = ListConsistencyListener.install(treeList);
        listConsistencyListener.setPreviousElementTracked(false);

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

    @Test
    public void testInsertRealOverVirtualParent() {
        EventList<String> source = new BasicEventList<String>();
        TreeList<String> treeList = new TreeList<String>(source, COMPRESSED_CHARACTER_TREE_FORMAT, TreeList.<String>nodesStartExpanded());
        ListConsistencyListener<String> listConsistencyListener = ListConsistencyListener.install(treeList);
        listConsistencyListener.setPreviousElementTracked(false);

        source.add("ABCDEF");
        source.add("ABC");
        assertEquals(6, treeList.size());
    }

    @Test
    public void testStructureChangingUpdates() {
        EventList<String> source = new BasicEventList<String>();
        TreeList<String> treeList = new TreeList<String>(source, COMPRESSED_CHARACTER_TREE_FORMAT, TreeList.<String>nodesStartExpanded());
        ListConsistencyListener<String> listConsistencyListener = ListConsistencyListener.install(treeList);
        listConsistencyListener.setPreviousElementTracked(false);

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

    @Test
    public void testClear() {
        EventList<String> source = new BasicEventList<String>();
        TreeList<String> treeList = new TreeList<String>(source, COMPRESSED_CHARACTER_TREE_FORMAT, TreeList.<String>nodesStartExpanded());
        ListConsistencyListener<String> listConsistencyListener = ListConsistencyListener.install(treeList);
        listConsistencyListener.setPreviousElementTracked(false);

        source.add("ABC");
        source.add("DEF");
        source.add("ABH");
        source.add("DZK");
        source.clear();
        assertEquals(0, treeList.size());
    }

    @Test
    public void testSourceChangesOnCollapsedSubtrees() {
        EventList<String> source = new BasicEventList<String>();
        TreeList<String> treeList = new TreeList<String>(source, COMPRESSED_CHARACTER_TREE_FORMAT, TreeList.<String>nodesStartExpanded());
        ListConsistencyListener<String> listConsistencyListener = ListConsistencyListener.install(treeList);
        listConsistencyListener.setPreviousElementTracked(false);

        source.add("A");
        assertEquals(1, treeList.size());
        assertEquals(1, treeList.getAllNodesList().size());
        assertFullTreeStructure(treeList, new String[] {
                "A",
        });

        treeList.setExpanded(0, false);
        assertEquals(1, treeList.size());
        assertEquals(1, treeList.getAllNodesList().size());
        assertFullTreeStructure(treeList, new String[] {
                "A",
        });

        source.add("ABC");
        assertEquals(1, treeList.size());
        assertEquals(3, treeList.getAllNodesList().size());
        assertFullTreeStructure(treeList, new String[] {
                "A",
                "AB",
                "ABC",
        });

        treeList.setExpanded(0, true);
        assertEquals(3, treeList.size());
        assertEquals(3, treeList.getAllNodesList().size());
        assertTreeStructure(treeList, new String[] {
                "A",
                "AB",
                "ABC",
        });
        assertFullTreeStructure(treeList, new String[] {
                "A",
                "AB",
                "ABC",
        });

        treeList.setExpanded(1, false);
        assertEquals(2, treeList.size());
        assertEquals(3, treeList.getAllNodesList().size());

        source.add("AD");
        assertEquals(3, treeList.size());
        assertEquals(4, treeList.getAllNodesList().size());

        source.addAll(Arrays.asList(
                "ABD",
                "ABE",
                "ABF",
                "ABG"
        ));
        assertEquals(3, treeList.size());
        assertEquals(8, treeList.getAllNodesList().size());
        assertTreeStructure(treeList, new String[] {
                "A",
                "AB",
                "AD",
        });
        assertFullTreeStructure(treeList, new String[] {
                "A",
                "AB",
                "ABC",
                "ABD",
                "ABE",
                "ABF",
                "ABG",
                "AD",
        });

        treeList.setExpanded(0, false);
        assertEquals(1, treeList.size());
        assertEquals(8, treeList.getAllNodesList().size());

        source.addAll(Arrays.asList(
                "ABH",
                "ABI"
        ));
        assertEquals(1, treeList.size());
        assertEquals(10, treeList.getAllNodesList().size());

        treeList.setExpanded(0, true);
        assertEquals(3, treeList.size());
        assertEquals(10, treeList.getAllNodesList().size());
        assertTreeStructure(treeList, new String[] {
                "A",
                "AB",
                "AD",
        });
        assertFullTreeStructure(treeList, new String[] {
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

        treeList.setExpanded(1, true);
        assertEquals(10, treeList.size());
        assertEquals(10, treeList.getAllNodesList().size());
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
        assertFullTreeStructure(treeList, new String[] {
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
        assertEquals(1, treeList.size());
        assertEquals(10, treeList.getAllNodesList().size());

        source.removeAll(Arrays.asList(
                "ABF",
                "ABG"
        ));
        assertEquals(1, treeList.size());
        assertEquals(8, treeList.getAllNodesList().size());

        treeList.setExpanded(0, true);
        assertEquals(8, treeList.size());
        assertEquals(8, treeList.getAllNodesList().size());
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
        assertFullTreeStructure(treeList, new String[] {
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

    public void assertTreeStructure(TreeList<String> treeList, String[] structure) {
        assertEquals(Arrays.asList(structure), nodeListAsString(treeList.getNodesList()));
    }

    public void assertFullTreeStructure(TreeList<String> treeList, String[] fullStructure) {
        assertEquals(Arrays.asList(fullStructure), nodeListAsString(treeList.getAllNodesList()));
    }

    private static List<String> nodeListAsString(List<TreeList.Node<String>> nodeList) {
        List<String> nodeListAsStrings = new ArrayList<String>();
        for(Iterator<TreeList.Node<String>> i = nodeList.iterator(); i.hasNext(); ) {
            TreeList.Node<String> node = i.next();
            StringBuffer asString = new StringBuffer(node.path().size());
            for(Iterator<String> n = node.path().iterator(); n.hasNext(); ) {
                asString.append(n.next());
            }
            nodeListAsStrings.add(asString.toString());
        }

        return nodeListAsStrings;
    }

    @Test
    public void testCollapseExpand() {
        EventList<String> source = new BasicEventList<String>();
        TreeList<String> treeList = new TreeList<String>(source, COMPRESSED_CHARACTER_TREE_FORMAT, TreeList.<String>nodesStartExpanded());
        ListConsistencyListener<String> listConsistencyListener = ListConsistencyListener.install(treeList);
        listConsistencyListener.setPreviousElementTracked(false);

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

    @Test
    public void testSourceUpdateEvents() {
        EventList<String> source = new BasicEventList<String>();
        TreeList<String> treeList = new TreeList<String>(source, COMPRESSED_CHARACTER_TREE_FORMAT, TreeList.<String>nodesStartExpanded());
        ListConsistencyListener<String> listConsistencyListener = ListConsistencyListener.install(treeList);
        listConsistencyListener.setPreviousElementTracked(false);

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

    @Test
    public void testDeletedRealParentIsReplacedByVirtualParent() {
        EventList<String> source = new BasicEventList<String>();
        TreeList<String> treeList = new TreeList<String>(source, COMPRESSED_CHARACTER_TREE_FORMAT, TreeList.<String>nodesStartExpanded());
        ListConsistencyListener<String> listConsistencyListener = ListConsistencyListener.install(treeList);
        listConsistencyListener.setPreviousElementTracked(false);

        source.add("A");
        source.add("ABC");
        source.remove(0);
        assertTreeStructure(treeList, new String[] {
                "A",
                "AB",
                "ABC",
        });
    }

    @Test
    public void testTreeEditing() {
        EventList<String> source = new BasicEventList<String>();
        TreeList<String> treeList = new TreeList<String>(source, COMPRESSED_CHARACTER_TREE_FORMAT, TreeList.<String>nodesStartExpanded());
        ListConsistencyListener<String> listConsistencyListener = ListConsistencyListener.install(treeList);
        listConsistencyListener.setPreviousElementTracked(false);

        source.add("ABC");
        source.add("ABD");
        source.add("EFG");
        source.add("EFK");
        treeList.setExpanded(0, false);
        assertTreeStructure(treeList, new String[] {
                "A",
                "E",
                "EF",
                "EFG",
                "EFK",
        });

        treeList.set(3, "EFH");
        treeList.set(4, "EFL");
        treeList.add(4, "EFI");
        assertTreeStructure(treeList, new String[] {
                "A",
                "E",
                "EF",
                "EFH",
                "EFI",
                "EFL",
        });
    }

    @Test
    public void testTreeSortingUnsortedTree() {
        EventList<String> source = new BasicEventList<String>();
        SortedList<String> sortedSource = SortedList.create(source);
        TreeList<String> treeList = new TreeList<String>(sortedSource, UNCOMPRESSED_CHARACTER_TREE_FORMAT, TreeList.<String>nodesStartExpanded());
        ListConsistencyListener<String> listConsistencyListener = ListConsistencyListener.install(treeList);
        listConsistencyListener.setPreviousElementTracked(false);

        source.add("ABC");
        source.add("ABD");
        source.add("ABE");
        source.add("FGH");
        source.add("FGI");

        sortedSource.setComparator(GlazedLists.reverseComparator());
        assertTreeStructure(treeList, new String[] {
                "F",
                "FG",
                "FGI",
                "FGH",
                "A",
                "AB",
                "ABE",
                "ABD",
                "ABC",
        });
    }

    @Test
    public void testTreeSorting() {
        EventList<String> source = new BasicEventList<String>();
        SortedList<String> sortedSource = SortedList.create(source);
        TreeList<String> treeList = new TreeList<String>(sortedSource, COMPRESSED_CHARACTER_TREE_FORMAT, TreeList.<String>nodesStartExpanded());
        ListConsistencyListener<String> listConsistencyListener = ListConsistencyListener.install(treeList);
        listConsistencyListener.setPreviousElementTracked(false);

        source.add("ABc");
        source.add("ABd");
        source.add("ABe");
        source.add("FGh");
        source.add("FGi");

        sortedSource.setComparator(GlazedLists.reverseComparator());
        assertTreeStructure(treeList, new String[] {
                "A",
                "AB",
                "ABe",
                "ABd",
                "ABc",
                "F",
                "FG",
                "FGi",
                "FGh",
        });
    }

    @Test
    public void testInsertInReverseOrder() {
        EventList<String> source = new BasicEventList<String>();
        TreeList<String> treeList = new TreeList<String>(source, COMPRESSED_CHARACTER_TREE_FORMAT, TreeList.<String>nodesStartExpanded());
        ListConsistencyListener<String> listConsistencyListener = ListConsistencyListener.install(treeList);
        listConsistencyListener.setPreviousElementTracked(false);

        source.add("LBAA");
        source.add("LAAA");
    }

    @Test
    public void testNonSiblingsBecomeSiblings() {
        EventList<String> source = new BasicEventList<String>();
        TreeList<String> treeList = new TreeList<String>(source, UNCOMPRESSED_CHARACTER_TREE_FORMAT, TreeList.<String>nodesStartExpanded());
        ListConsistencyListener<String> listConsistencyListener = ListConsistencyListener.install(treeList);
        listConsistencyListener.setPreviousElementTracked(false);

        source.add("ABC");
        source.add("EFG");
        source.add("ABD");
        assertTreeStructure(treeList, new String[] {
                "A",
                "AB",
                "ABC",
                "E",
                "EF",
                "EFG",
                "A",
                "AB",
                "ABD",
        });

        source.remove("EFG");
        assertTreeStructure(treeList, new String[] {
                "A",
                "AB",
                "ABC",
                "ABD",
        });
    }

    @Test
    public void testSiblingsBecomeNonSiblings() {
        EventList<String> source = new BasicEventList<String>();
        TreeList<String> treeList = new TreeList<String>(source, UNCOMPRESSED_CHARACTER_TREE_FORMAT, TreeList.<String>nodesStartExpanded());
        ListConsistencyListener<String> listConsistencyListener = ListConsistencyListener.install(treeList);
        listConsistencyListener.setPreviousElementTracked(false);

        source.add("ABC");
        source.add("ABD");
        source.add(1, "EFG");
        assertTreeStructure(treeList, new String[] {
                "A",
                "AB",
                "ABC",
                "E",
                "EF",
                "EFG",
                "A",
                "AB",
                "ABD",
        });
    }

    @Test
    public void testSiblingsBecomeNonSiblingsWithCollapsedNodes() {
        EventList<String> source = new BasicEventList<String>();
        TreeList<String> treeList = new TreeList<String>(source, UNCOMPRESSED_CHARACTER_TREE_FORMAT, TreeList.<String>nodesStartExpanded());
        ListConsistencyListener<String> listConsistencyListener = ListConsistencyListener.install(treeList);
        listConsistencyListener.setPreviousElementTracked(false);

        source.add("ABC");
        source.add("ABD");
        treeList.setExpanded(1, false); // collapse 'B'
        assertTreeStructure(treeList, new String[] {
                "A",
                "AB",
        });

        source.add(1, "EFG");
        assertTreeStructure(treeList, new String[] {
                "A",
                "AB",
                "E",
                "EF",
                "EFG",
                "A",
                "AB",
        });

        treeList.setExpanded(6, true);
        assertTreeStructure(treeList, new String[] {
                "A",
                "AB",
                "E",
                "EF",
                "EFG",
                "A",
                "AB",
                "ABD",
        });
    }

    /**
     * This test validates that when multiple sets of parents are restored, all
     * the appropriate virtual nodes are assigned the appropriate new parents.
     */
    @Test
    public void testInsertMultipleParents() {
        TransactionList<String> source = new TransactionList<String>(new BasicEventList<String>());
        TreeList<String> treeList = new TreeList<String>(source, UNCOMPRESSED_CHARACTER_TREE_FORMAT, TreeList.<String>nodesStartExpanded());
        ListConsistencyListener<String> listConsistencyListener = ListConsistencyListener.install(treeList);
        listConsistencyListener.setPreviousElementTracked(false);

        source.beginEvent(false);
            source.add("ABC");
            source.add("ABD");
            source.add("ABE");
        source.commitEvent();

        source.beginEvent();
            source.addAll(0, Arrays.asList("A", "AB"));
            source.addAll(3, Arrays.asList("A", "AB"));
        source.commitEvent();

        assertTreeStructure(treeList, new String[] {
                "A",
                "AB",
                "ABC",
                "A",
                "AB",
                "ABD",
                "ABE",
        });
    }

    @Test
    public void testAttachSiblings() {
        EventList<String> source = new BasicEventList<String>();
        TreeList<String> treeList = new TreeList<String>(source, UNCOMPRESSED_CHARACTER_TREE_FORMAT, TreeList.<String>nodesStartExpanded());
        ListConsistencyListener<String> listConsistencyListener = ListConsistencyListener.install(treeList);
        listConsistencyListener.setPreviousElementTracked(false);

        source.add("A");
        source.add("BCD");
        source.add("BCF");
        assertTreeStructure(treeList, new String[] {
                "A",
                "B",
                "BC",
                "BCD",
                "BCF",
        });
    }

    @Test
    public void testDeleteParentAndOneChild() {
        EventList<String> source = new BasicEventList<String>();
        TreeList<String> treeList = new TreeList<String>(source, UNCOMPRESSED_CHARACTER_TREE_FORMAT, TreeList.<String>nodesStartExpanded());
        ListConsistencyListener<String> listConsistencyListener = ListConsistencyListener.install(treeList);
        listConsistencyListener.setPreviousElementTracked(false);

        source.add("P");
        source.add("PD");
        source.add("PU");
        source.add("PI");
        source.add("PIS");
        source.add("PIU");
        source.add("PY");
        source.removeAll(Arrays.asList("PI", "PIS"));
        assertTreeStructure(treeList, new String[] {
                "P",
                "PD",
                "PU",
                "PI",
                "PIU",
                "PY",
        });
    }

    @Test
    public void testReplaceVirtualWithRealWithSiblings() {
        EventList<String> source = new BasicEventList<String>();
        TreeList<String> treeList = new TreeList<String>(source, UNCOMPRESSED_CHARACTER_TREE_FORMAT, TreeList.<String>nodesStartExpanded());
        ListConsistencyListener<String> listConsistencyListener = ListConsistencyListener.install(treeList);
        listConsistencyListener.setPreviousElementTracked(false);

        source.add("ABC");
        source.add("DEF");
        source.add("GHI");
        source.addAll(1, Arrays.asList("D", "DE"));
        assertTreeStructure(treeList, new String[] {
                "A",
                "AB",
                "ABC",
                "D",
                "DE",
                "DEF",
                "G",
                "GH",
                "GHI",
        });
    }


    @Test
    public void testAddSubtreePlusSiblings() {
        EventList<String> source = new BasicEventList<String>();
        TreeList<String> treeList = new TreeList<String>(source, UNCOMPRESSED_CHARACTER_TREE_FORMAT, TreeList.<String>nodesStartExpanded());
        ListConsistencyListener<String> listConsistencyListener = ListConsistencyListener.install(treeList);
        listConsistencyListener.setPreviousElementTracked(false);

        source.add("ABC");
        source.add("AWX");
        source.addAll(1, Arrays.asList("AE", "AEF", "AW"));
        assertTreeStructure(treeList, new String[] {
                "A",
                "AB",
                "ABC",
                "AE",
                "AEF",
                "AW",
                "AWX",
        });
    }

    @Test
    public void testInsertUpdateDeleteOnCollapsed() {
        EventList<String> source = new BasicEventList<String>();
        TreeList<String> treeList = new TreeList<String>(source, UNCOMPRESSED_CHARACTER_TREE_FORMAT, TreeList.<String>nodesStartExpanded());
        ListConsistencyListener<String> listConsistencyListener = ListConsistencyListener.install(treeList);
        listConsistencyListener.setPreviousElementTracked(false);

        source.add("ABC");
        source.add("ABD");
        source.add("ABEF");
        treeList.setExpanded(1, false);
        source.add("ABEG");
        source.add(3, "ABH"); // to split siblings ABEF, ABEG
        source.remove(1);
        source.set(0, "ABI");
        assertTreeStructure(treeList, new String[] {
                "A",
                "AB",
        });

        treeList.setExpanded(1, true);
        assertTreeStructure(treeList, new String[] {
                "A",
                "AB",
                "ABI",
                "ABE",
                "ABEF",
                "ABH",
                "ABE",
                "ABEG",
        });

        treeList.setExpanded(1, false);
        source.remove(2); // to join siblings ABEF, ABEG

        treeList.setExpanded(1, true);
        assertTreeStructure(treeList, new String[] {
                "A",
                "AB",
                "ABI",
                "ABE",
                "ABEF",
                "ABEG",
        });
    }

    @Test
    public void testInsertVirtualParentsOnCollapsed() {
        EventList<String> source = new BasicEventList<String>();
        TreeList<String> treeList = new TreeList<String>(source, UNCOMPRESSED_CHARACTER_TREE_FORMAT, TreeList.<String>nodesStartExpanded());
        ListConsistencyListener<String> listConsistencyListener = ListConsistencyListener.install(treeList);
        listConsistencyListener.setPreviousElementTracked(false);

        source.add("ABE");
        treeList.setExpanded(1, false);
        source.add("ABEFGH");

        treeList.setExpanded(1, true);
        assertTreeStructure(treeList, new String[] {
                "A",
                "AB",
                "ABE",
                "ABEF",
                "ABEFG",
                "ABEFGH",
        });
    }

    @Test
    public void testReplaceHiddenVirtualParentWithReal() {
        EventList<String> source = new BasicEventList<String>();
        TreeList<String> treeList = new TreeList<String>(source, UNCOMPRESSED_CHARACTER_TREE_FORMAT, TreeList.<String>nodesStartExpanded());
        ListConsistencyListener<String> listConsistencyListener = ListConsistencyListener.install(treeList);
        listConsistencyListener.setPreviousElementTracked(false);

        source.add("ABC");
        treeList.setExpanded(0, false);
        source.addAll(0, Arrays.asList(
                "A",
                "AB"
        ));

        treeList.setExpanded(0, true);
        assertTreeStructure(treeList, new String[] {
                "A",
                "AB",
                "ABC",
        });
    }

    @Test
    public void testSortingSource() {
        EventList<String> source = new BasicEventList<String>();
        SortedList<String> sortedList = new SortedList<String>(source, null);
        TreeList<String> treeList = new TreeList<String>(sortedList, UNCOMPRESSED_CHARACTER_TREE_FORMAT, TreeList.<String>nodesStartExpanded());
        ListConsistencyListener<String> listConsistencyListener = ListConsistencyListener.install(treeList);
        listConsistencyListener.setPreviousElementTracked(false);

        source.add("A");
        source.add("DEF");
        source.add("ABC");
        sortedList.setComparator(GlazedLists.comparableComparator());
        assertTreeStructure(treeList, new String[] {
                "A",
                "AB",
                "ABC",
                "D",
                "DE",
                "DEF",
        });

    }

    @Test
    public void testSortingSourceWithVirtualParentsBetween() {
        EventList<String> source = new BasicEventList<String>();
        SortedList<String> sortedList = new SortedList<String>(source, null);
        TreeList<String> treeList = new TreeList<String>(sortedList, UNCOMPRESSED_CHARACTER_TREE_FORMAT, TreeList.<String>nodesStartExpanded());
        ListConsistencyListener<String> listConsistencyListener = ListConsistencyListener.install(treeList);
        listConsistencyListener.setPreviousElementTracked(false);

        source.addAll(Arrays.asList(
                "PB",
                "PNMA",
                "PNM",
                "PNMA",
                "PDEB"
        ));
        sortedList.setComparator(new LastCharComparator());
    }

    @Test
    public void testObsoleteVirtualParentsWithinMovedNodes() {
        EventList<String> source = new BasicEventList<String>();
        SortedList<String> sortedList = new SortedList<String>(source, null);
        TreeList<String> treeList = new TreeList<String>(sortedList, UNCOMPRESSED_CHARACTER_TREE_FORMAT, TreeList.<String>nodesStartExpanded());
        ListConsistencyListener<String> listConsistencyListener = ListConsistencyListener.install(treeList);
        listConsistencyListener.setPreviousElementTracked(false);

        source.addAll(Arrays.asList(
                "AD",
                "ADCE",
                "ADCA"
        ));
        sortedList.setComparator(new LastCharComparator());
        sortedList.setComparator(null);
    }

    private class LastCharComparator implements Comparator<String> {
        @Override
        public int compare(String a, String b) {
            return lastCharOf(a) - lastCharOf(b);
        }
        private char lastCharOf(String s) {
            return s.charAt(s.length() - 1);
        }
    }

    @Test
    public void testVisibilityOnParentMergeFollowerCollapsed() {
        EventList<String> source = new BasicEventList<String>();
        TreeList<String> treeList = new TreeList<String>(source, UNCOMPRESSED_CHARACTER_TREE_FORMAT, TreeList.<String>nodesStartExpanded());
        ListConsistencyListener<String> listConsistencyListener = ListConsistencyListener.install(treeList);
        listConsistencyListener.setPreviousElementTracked(false);

        source.add("ABC");
        source.add("DEF");
        source.add("ABD");
        treeList.setExpanded(7, false);
        source.remove(1);

        assertTreeStructure(treeList, new String[] {
                "A",
                "AB",
                "ABC",
                "ABD",
        });
    }

    @Test
    public void testVisibilityOnParentMergeLeaderCollapsed() {
        EventList<String> source = new BasicEventList<String>();
        TreeList<String> treeList = new TreeList<String>(source, UNCOMPRESSED_CHARACTER_TREE_FORMAT, TreeList.<String>nodesStartExpanded());
        ListConsistencyListener<String> listConsistencyListener = ListConsistencyListener.install(treeList);
        listConsistencyListener.setPreviousElementTracked(false);

        source.add("ABC");
        source.add("DEF");
        source.add("ABD");
        treeList.setExpanded(1, false);
        source.remove(1);

        assertTreeStructure(treeList, new String[] {
                "A",
                "AB",
                "ABC",
                "ABD",
        });
    }

    @Test
    public void testSplitChildrenHoldsSiblings() {
        EventList<String> source = new BasicEventList<String>();
        TreeList<String> treeList = new TreeList<String>(source, UNCOMPRESSED_CHARACTER_TREE_FORMAT, TreeList.<String>nodesStartExpanded());
        ListConsistencyListener<String> listConsistencyListener = ListConsistencyListener.install(treeList);
        listConsistencyListener.setPreviousElementTracked(false);

        source.addAll(Arrays.asList(
                "PMMP",
                "PMMP",
                "PSU"
        ));

        source.add(1, "PDDS");

        assertTreeStructure(treeList, new String[] {
                "P",
                "PM",
                "PMM",
                "PMMP",
                "PD",
                "PDD",
                "PDDS",
                "PM",
                "PMM",
                "PMMP",
                "PS",
                "PSU",
        });

    }

    @Test
    public void testAttachSiblingsToStrippedSiblings() {
        EventList<String> source = new BasicEventList<String>();
        TreeList<String> treeList = new TreeList<String>(source, UNCOMPRESSED_CHARACTER_TREE_FORMAT, TreeList.<String>nodesStartExpanded());
        ListConsistencyListener<String> listConsistencyListener = ListConsistencyListener.install(treeList);
        listConsistencyListener.setPreviousElementTracked(false);

        source.addAll(Arrays.asList(
                "PMMP",
                "PMMP",
                "PSU"
        ));

        source.add(1, "PP");

        assertTreeStructure(treeList, new String[] {
                "P",
                "PM",
                "PMM",
                "PMMP",
                "PP",
                "PM",
                "PMM",
                "PMMP",
                "PS",
                "PSU",
        });

    }

    @Test
    public void testInsertAncestorAfterChild() {
        EventList<String> source = new BasicEventList<String>();
        TreeList<String> treeList = new TreeList<String>(source, UNCOMPRESSED_CHARACTER_TREE_FORMAT, TreeList.<String>nodesStartExpanded());
        ListConsistencyListener<String> listConsistencyListener = ListConsistencyListener.install(treeList);
        listConsistencyListener.setPreviousElementTracked(false);

        source.addAll(Arrays.asList(
                "PSU",
                "PMMA"
        ));

        // when we're inserting the 'p' node, we need to be careful about the
        // virtual ancestry of the existing 'pmma' node that precedes it, making
        // sure not to attach p as a child of a deeper node
        source.addAll(1, Arrays.asList(
                "PDDS",
                "P"
        ));

        assertTreeStructure(treeList, new String[] {
                "P",
                "PS",
                "PSU",
                "PD",
                "PDD",
                "PDDS",
                "P",
                "PM",
                "PMM",
                "PMMA",
        });
    }

    @Test
    public void testSiblingsAttachedToNewParentsFromSplitNodes() {
        EventList<String> source = new BasicEventList<String>();
        TreeList<String> treeList = new TreeList<String>(source, UNCOMPRESSED_CHARACTER_TREE_FORMAT, TreeList.<String>nodesStartExpanded());
        ListConsistencyListener<String> listConsistencyListener = ListConsistencyListener.install(treeList);
        listConsistencyListener.setPreviousElementTracked(false);

        source.addAll(Arrays.asList(
                "PMMU",
                "PMMP",
                "PN"
        ));

        // this insert causes ancestry to be added for the existing node 'pmmp',
        // which needs to be attached as a sibling to the parent node 'pm'.
        source.addAll(1, Arrays.asList(
                "PDDS",
                "P"
        ));

        assertTreeStructure(treeList, new String[] {
                "P",
                "PM",
                "PMM",
                "PMMU",
                "PD",
                "PDD",
                "PDDS",
                "P",
                "PM",
                "PMM",
                "PMMP",
                "PN",
        });
    }

    @Test
    public void testAddExtraRoot() {
        EventList<String> source = new BasicEventList<String>();
        TreeList<String> treeList = new TreeList<String>(source, UNCOMPRESSED_CHARACTER_TREE_FORMAT, TreeList.<String>nodesStartExpanded());
        ListConsistencyListener<String> listConsistencyListener = ListConsistencyListener.install(treeList);
        listConsistencyListener.setPreviousElementTracked(false);

        source.addAll(Arrays.asList(
                "PMMU",
                "PMMP",
                "PMMP",
                "PN"
        ));

        source.addAll(1, Arrays.asList(
                "P"
        ));

        assertTreeStructure(treeList, new String[] {
                "P",
                "PM",
                "PMM",
                "PMMU",
                "P",
                "PM",
                "PMM",
                "PMMP",
                "PMMP",
                "PN",
        });
    }

    @Test
    public void testAddParentAndSibling() {
        EventList<String> source = new BasicEventList<String>();
        TreeList<String> treeList = new TreeList<String>(source, UNCOMPRESSED_CHARACTER_TREE_FORMAT, TreeList.<String>nodesStartExpanded());
        ListConsistencyListener<String> listConsistencyListener = ListConsistencyListener.install(treeList);
        listConsistencyListener.setPreviousElementTracked(false);

        source.addAll(Arrays.asList(
                "AC",
                "A"
        ));

        source.addAll(0, Arrays.asList(
                "A",
                "AB"
        ));

        assertTreeStructure(treeList, new String[] {
                "A",
                "AB",
                "AC",
                "A",
        });
    }

    @Test
    public void testRebuildSiblingsInUnnaturalOrder() {
        TransactionList<String> source = new TransactionList<String>(new BasicEventList<String>());
        TreeList<String> treeList = new TreeList<String>(source, UNCOMPRESSED_CHARACTER_TREE_FORMAT, TreeList.<String>nodesStartExpanded());
        ListConsistencyListener<String> listConsistencyListener = ListConsistencyListener.install(treeList);
        listConsistencyListener.setPreviousElementTracked(false);

        source.beginEvent();
        source.addAll(Arrays.asList(
                "PMMP",
                "PMMS"
        ));
        source.commitEvent();

        source.beginEvent();
        source.add(1, "P");
        source.add(3, "PDDV");
        source.commitEvent();

        assertTreeStructure(treeList, new String[] {
                "P",
                "PM",
                "PMM",
                "PMMP",
                "P",
                "PM",
                "PMM",
                "PMMS",
                "PD",
                "PDD",
                "PDDV",
        });
    }

    @Test
    public void testReorderIntoInfiniteLoop() {
        EventList<String> source = new BasicEventList<String>();
        SortedList<String> sortedSource = new SortedList<String>(source, null);
        TreeList<String> treeList = new TreeList<String>(sortedSource, new CharacterTreeFormat(new NullCompartor<String>()), TreeList.<String>nodesStartExpanded());
        ListConsistencyListener<String> listConsistencyListener = ListConsistencyListener.install(treeList);
        listConsistencyListener.setPreviousElementTracked(false);

        source.add("AC");
        source.add("AB");
        source.add("AD");

        sortedSource.setComparator(GlazedLists.comparableComparator());

        assertTreeStructure(treeList, new String[] {
                "A",
                "AB",
                "AC",
                "AD",
        });
    }

    private static final class NullCompartor<E> implements Comparator<E> {
        @Override
        public int compare(E o1, E o2) {
            return 0;
        }
    }

    @Test
    public void testRemoveHiddenCollapsedSubtrees() {
        BasicEventList<String> source = new BasicEventList<String>();
        TreeList<String> treeList = new TreeList<String>(source, UNCOMPRESSED_CHARACTER_TREE_FORMAT, TreeList.<String>nodesStartExpanded());
        ListConsistencyListener<String> listConsistencyListener = ListConsistencyListener.install(treeList);
        listConsistencyListener.setPreviousElementTracked(false);

        source.addAll(Arrays.asList(
                "L",
                "LL",
                "LLG",
                "LL",
                "LLM"
        ));

        treeList.setExpanded(1, false);
        treeList.setExpanded(0, false);

        source.remove(3);

        assertTreeStructure(treeList, new String[] {
                "L",
        });

        treeList.setExpanded(0, true);
        assertTreeStructure(treeList, new String[] {
                "L",
                "LL",
                "LLG",
                "LLM",
        });
    }

    @Test
    public void testCollapsedByDefaultOnInsert() {
        BasicEventList<String> source = new BasicEventList<String>();
        TreeList<String> treeList = new TreeList<String>(source, UNCOMPRESSED_CHARACTER_TREE_FORMAT, TreeList.<String>nodesStartCollapsed());
        ListConsistencyListener<String> listConsistencyListener = ListConsistencyListener.install(treeList);
        listConsistencyListener.setPreviousElementTracked(false);

        source.addAll(Arrays.asList(
                "ABC",
                "ABD",
                "EFG"
        ));
        assertTreeStructure(treeList, new String[] {
                "A",
                "E",
        });
        treeList.setExpanded(0, true);
        treeList.setExpanded(1, true);
        assertTreeStructure(treeList, new String[] {
                "A",
                "AB",
                "ABC",
                "ABD",
                "E",
        });
    }

    @Test
    public void testCollapsedByDefaultOnCreate() {
        BasicEventList<String> source = new BasicEventList<String>();
        source.addAll(Arrays.asList(
                "ABC",
                "ABD",
                "EFG"
        ));

        TreeList<String> treeList = new TreeList<String>(source, UNCOMPRESSED_CHARACTER_TREE_FORMAT, TreeList.<String>nodesStartCollapsed());
        ListConsistencyListener<String> listConsistencyListener = ListConsistencyListener.install(treeList);
        listConsistencyListener.setPreviousElementTracked(false);

        assertTreeStructure(treeList, new String[] {
                "A",
                "E",
        });
        treeList.setExpanded(0, true);
        treeList.setExpanded(1, true);
        assertTreeStructure(treeList, new String[] {
                "A",
                "AB",
                "ABC",
                "ABD",
                "E",
        });
    }

    @Test
    public void testCollapsedByDefaultForSplits() {
        BasicEventList<String> source = new BasicEventList<String>();
        TreeList<String> treeList = new TreeList<String>(source, UNCOMPRESSED_CHARACTER_TREE_FORMAT, TreeList.<String>nodesStartCollapsed());
        ListConsistencyListener<String> listConsistencyListener = ListConsistencyListener.install(treeList);
        listConsistencyListener.setPreviousElementTracked(false);

        source.addAll(Arrays.asList(
                "ABCD",
                "ABCE"
        ));
        treeList.setExpanded(0, true);
        source.add(1, "EFGH");
        assertTreeStructure(treeList, new String[] {
                "A",
                "AB",
                "E",
                "A",
                "AB",
        });
    }

    @Test
    public void testInsertParentWithVisibleChildGetsExpandedState() {
        BasicEventList<String> source = new BasicEventList<String>();
        DefaultExternalExpansionModel<String> expansionProvider = new DefaultExternalExpansionModel<String>(TreeList.<String>nodesStartExpanded());
        TreeList<String> treeList = new TreeList<String>(source, UNCOMPRESSED_CHARACTER_TREE_FORMAT, expansionProvider);
        ListConsistencyListener<String> listConsistencyListener = ListConsistencyListener.install(treeList);
        listConsistencyListener.setPreviousElementTracked(false);

        source.add("ABC");
        source.add("ABD");

        expansionProvider.setExpanded("B", GlazedListsTests.stringToList("AB"), false);
        source.add(1, "AB");
        assertTreeStructure(treeList, new String[] {
                "A",
                "AB",
                "ABC",
                "AB",
                "ABD",
        });
    }

    /**
     * Validate that a virtual parent's state is maintained even if all the
     * children are deleted new children inserted in a single event.
     */
    @Ignore("Fix me")
    @Test
    public void testDeleteAndReinsertLeafRetainsParentState_FixMe() {
        TransactionList<String> source = new TransactionList<String>(new BasicEventList<String>());

        TreeList<String> treeList = new TreeList<String>(source, UNCOMPRESSED_CHARACTER_TREE_FORMAT, TreeList.<String>nodesStartExpanded());
        ListConsistencyListener<String> listConsistencyListener = ListConsistencyListener.install(treeList);
        listConsistencyListener.setPreviousElementTracked(false);


        source.beginEvent(false);
            source.add("ABC");
        source.commitEvent();

        treeList.setExpanded(1, false);

        source.beginEvent(false);
            source.remove(0);
            source.add("ABD");
        source.commitEvent();

        assertFalse(treeList.isExpanded(1));
    }

    /**
     * Make sure the expansion model provides the correct visibility
     * for new nodes that whose child nodes are already exist.
     */
    @Test
    public void testExpansionModelWithInsertedNodes() {
        BasicEventList<String> source = new BasicEventList<String>();
        TreeList<String> treeList = new TreeList<String>(source, UNCOMPRESSED_CHARACTER_TREE_FORMAT, TreeList.<String>nodesStartCollapsed());
        ListConsistencyListener<String> listConsistencyListener = ListConsistencyListener.install(treeList);
        listConsistencyListener.setPreviousElementTracked(false);

        source.add(0, "PMMU");
        source.add(0, "PMMU");

        treeList.setExpanded(0, true);
        treeList.setExpanded(1, true);
        treeList.setExpanded(2, true);

        source.addAll(1, Arrays.asList(
                "PMM",
                "PMMP"
        ));
    }

    /**
     * Make sure the expansion model provides the correct visibility
     * for new nodes that whose child nodes are already exist.
     */
    @Test
    public void testInsertCollapsedParentWithExpandedChild() {
        TransactionList<String> source = new TransactionList<String>(new BasicEventList<String>());

        TreeList<String> treeList = new TreeList<String>(source, UNCOMPRESSED_CHARACTER_TREE_FORMAT, TreeList.<String>nodesStartCollapsed());
        ListConsistencyListener<String> listConsistencyListener = ListConsistencyListener.install(treeList);
        listConsistencyListener.setPreviousElementTracked(false);

        source.beginEvent(false);
            source.add(0, "PMMU");
            source.add(1, "PMMU");
        source.commitEvent();

        treeList.setExpanded(0, true);
        treeList.setExpanded(1, true);
        treeList.setExpanded(2, true);

        source.beginEvent(false);
            source.add(1, "PMM");
            source.add(2, "PMMP");
            source.add(4, "PMMS");
        source.commitEvent();
    }

    /**
     * We originally had an assert() in the setExpaneded(Node) method, which failed
     * because we would call it in the process of fixing the tree. This validates that
     * the setExpanded() method can work while the tree is still changing.
     */
    @Test
    public void testExpandingParentWhileTreeIsInvalid() {
        TransactionList<String> source = new TransactionList<String>(new BasicEventList<String>());

        TreeList<String> treeList = new TreeList<String>(source, UNCOMPRESSED_CHARACTER_TREE_FORMAT, TreeList.<String>nodesStartCollapsed());
        ListConsistencyListener<String> listConsistencyListener = ListConsistencyListener.install(treeList);
        listConsistencyListener.setPreviousElementTracked(false);

        source.beginEvent(false);
            source.add(0, "ABC");
            source.add(1, "ABE");
            source.add(2, "BBC");
            source.add(3, "BBE");
        source.commitEvent();

        treeList.setExpanded(0, true);
        treeList.setExpanded(1, true);
        treeList.setExpanded(4, true);
        treeList.setExpanded(5, true);

        source.beginEvent(false);
            source.add(1, "AB");
            source.add(2, "ABD");
            source.add(4, "ABF");
            source.add(6, "BB");
            source.add(7, "BBD");
            source.add(9, "BBF");
        source.commitEvent();
    }

    @Test
    public void testUpdatingElementMovesIt() {
        EventList<String> source = new BasicEventList<String>();

        TreeList<String> treeList = new TreeList<String>(source, new CharacterTreeFormat(String.CASE_INSENSITIVE_ORDER), TreeList.<String>nodesStartExpanded());
        ListConsistencyListener<String> listConsistencyListener = ListConsistencyListener.install(treeList);
        listConsistencyListener.setPreviousElementTracked(false);

        source.add(0, "ABC");
        source.add(1, "BCD");
        source.add(2, "CDE");
        source.add(3, "CDF");
        source.add(4, "DEF");

        source.set(3, "ABD");
    }

    @Test
    public void testUpdatingElementsRetainExpandCollapseState() {
        EventList<String> source = new BasicEventList<String>();

        TreeList<String> treeList = new TreeList<String>(source, new CharacterTreeFormat(String.CASE_INSENSITIVE_ORDER), TreeList.<String>nodesStartExpanded());
        ListConsistencyListener<String> listConsistencyListener = ListConsistencyListener.install(treeList);
        listConsistencyListener.setPreviousElementTracked(false);

        source.add(0, "AB");
        source.add(1, "ABC");
        source.add(2, "ABD");
        source.add(3, "ABE");
        treeList.setExpanded(2, false);
        treeList.setExpanded(1, false);
        source.set(0, "AB");
        source.set(1, "ABC");
        assertTreeStructure(treeList, new String[] {
                "A",
                "AB",
        });
        treeList.setExpanded(1, true);
        assertTreeStructure(treeList, new String[] {
                "A",
                "AB",
                "ABC",
                "ABD",
                "ABE",
        });
        assertFalse(treeList.isExpanded(2));
    }

    @Test
    public void testTemporaryVirtualNodesAreRemoved() {
        TransactionList<String> source = new TransactionList<String>(new BasicEventList<String>());
        TreeList<String> treeList = new TreeList<String>(source, UNCOMPRESSED_CHARACTER_TREE_FORMAT, TreeList.<String>nodesStartExpanded());
        ListConsistencyListener<String> listConsistencyListener = ListConsistencyListener.install(treeList);
        listConsistencyListener.setPreviousElementTracked(false);

        source.beginEvent(false);
            source.add("MEPB");
            source.add("MEME");
            source.add("MEPM");
            source.add("MEPD");
            source.add("MEMN");
        source.commitEvent();

        source.beginEvent();
            source.clear();
            source.add("MEPB");
            source.add("MEPD");
            source.add("MEME");
            source.add("MEPM");
            source.add("MEMN");
        source.commitEvent();

        assertTreeStructure(treeList, new String[] {
                "M",
                "ME",
                "MEP",
                "MEPB",
                "MEPD",
                "MEM",
                "MEME",
                "MEP",
                "MEPM",
                "MEM",
                "MEMN",
        });
    }

    /**
     * This test isn't a spec - when the Comparator and equals() disagree, whom
     * should we trust?
     */
    @Ignore("Fix me")
    @Test
    public void testComparatorWeakerThanEquals() {
        TransactionList<String> source = new TransactionList<String>(new BasicEventList<String>());
        TreeList<String> treeList = new TreeList<String>(source, new CharacterTreeFormat(String.CASE_INSENSITIVE_ORDER), TreeList.<String>nodesStartExpanded());
        ListConsistencyListener<String> listConsistencyListener = ListConsistencyListener.install(treeList);
        listConsistencyListener.setPreviousElementTracked(false);

        source.beginEvent(false);
            source.add("AJU");
            source.add("aJD");
            source.add("AKG");
        source.commitEvent();

        assertTreeStructure(treeList, new String[] {
                "a",
                "aJ",
                "aJD",
                "AJU",
                "AK",
                "AKG",
        });

        source.beginEvent();
            source.clear();
            source.add("aJD");
            source.add("AJU");
            source.add("AKG");
        source.commitEvent();

        assertTreeStructure(treeList, new String[] {
                "a",
                "aJ",
                "aJD",
                "AJU",
                "AK",
                "AKG",
        });
    }

    @Ignore("Fix me")
    @Test
    public void testComparatorOrdering_FixMe() {
        TreeList.NodeComparator<String> nodeComparator = new TreeList.NodeComparator<String>(new CharacterTreeFormat(String.CASE_INSENSITIVE_ORDER));

        TreeList.Node<String> abc = new TreeList.Node<String>(false, GlazedListsTests.stringToList("ABC"));
        TreeList.Node<String> abcd = new TreeList.Node<String>(false, GlazedListsTests.stringToList("ABCd"));

        assertTrue(nodeComparator.compare(abc, abcd) < 0);
    }

    /**
     * Convert Strings into paths. For example, PUPPY is <code>/P/U/P/P/Y</code>
     *
     * <p>Lowercase values cannot have children.
     */
    private static class CharacterTreeFormat implements TreeList.Format<String> {
        private final Comparator<String> comparator;

        public CharacterTreeFormat() {
            this.comparator = GlazedLists.comparableComparator();
        }

        public CharacterTreeFormat(Comparator<String> comparator) {
            this.comparator = comparator;
        }

        @Override
        public boolean allowsChildren(String element) {
            return Character.isUpperCase(element.charAt(0));
        }
        @Override
        public void getPath(List<String> path, String element) {
            path.addAll(GlazedListsTests.stringToList(element));
        }

        @Override
        public Comparator<String> getComparator(int depth) {
            return comparator;
        }
    }
}
