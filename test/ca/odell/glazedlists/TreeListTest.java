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
     * Validates that removeAll() works.
     *
     * @see <a href="https://glazedlists.dev.java.net/issues/show_bug.cgi?id=169">Bug 169</a>
     */
    public void testCreate() throws Exception {
        BasicEventList source = new BasicEventList();
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
        for(Object i : treeList) {
            System.out.println(i);
        }
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

        //    [A]
        //    [A, B]
        //    [A, B, C]
        //    [A, B, D]
        //    [A, B, E]
        //    [A, B, E, F]
        //    [A, B, E, F, G]
        //    [A, B, E, F, H]
        //    [A, C]
        //    [A, C, D]
        //    [A, C, E]
        BasicEventList source = new BasicEventList();
        source.add("ABC");
        source.add("ABD");
        source.add("ABEFG");
        source.add("ABEFH");
        source.add("ACD");
        source.add("ACE");
        TreeList treeList = new TreeList(source, new CharacterTreeFormat());
        assertEquals(11, treeList.size());
        assertEquals(GlazedListsTests.stringToList("ABEF"), treeList.get(5).path());
        assertEquals(GlazedListsTests.stringToList("AC"), treeList.get(8).path());
        assertEquals(GlazedListsTests.stringToList("ACE"), treeList.get(10).path());

        // make some modifications, they should be handled in place
        source.add("ABF");
        assertEquals(GlazedListsTests.stringToList("ABF"), treeList.get(8).path());
        source.add("ABG");
        assertEquals(GlazedListsTests.stringToList("ABG"), treeList.get(9).path());
        source.add("ACBA");
        assertEquals(GlazedListsTests.stringToList("ACB"), treeList.get(11).path());
        assertEquals(GlazedListsTests.stringToList("ACBA"), treeList.get(12).path());

        for(Object i : treeList) {
            System.out.println(i);
        }

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
        BasicEventList source = new BasicEventList();
        source.add("ABCD");
        source.add("ABEFG");
        source.add("ACDC");
        source.add("ACE");

        //    [A]
        //    [A, B]
        //    [A, B, C]
        //    [A, B, C, D]
        //    [A, B, E]
        //    [A, B, E, F]
        //    [A, B, E, F, G]
        //    [A, C]
        //    [A, C, D]
        //    [A, C, D, C]
        //    [A, C, E]

        TreeList treeList = new TreeList(source, new CharacterTreeFormat());
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
}
