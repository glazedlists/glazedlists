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

}
