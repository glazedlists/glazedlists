/**
 * Glazed Lists
 * http://glazedlists.dev.java.net/
 *
 * COPYRIGHT 2003 O'DELL ENGINEERING LTD.
 */
package com.odellengineeringltd.glazedlists.util;

// for implementing Java Collections List
import java.util.*;

/**
 * A sparse list is a list that is optimized for holding several
 * values that are null.
 *
 * A sparse list can has a compressed view - this is a second list
 * that contains no null values. It is an error to modify the compressed
 * view.
 *
 * The sparse list also has methods to get the compressed index from
 * the natural index, and the natural index from the compressed
 * index.
 *
 * @author <a href="mailto:jesse@odel.on.ca">Jesse Wilson</a>
 */
public class SparseList extends AbstractList {
    
    /** the root node of the tree, this may be replaced by a delete */
    private SparseListNode root = null;
    
    /** a view of this list with no nulls */
    private CompressedList compressedList = new CompressedList();
    
    /** the total size can include trailing nulls beyond the tree */
    private int size = 0;

    /**
     * Gets the size of this tree.
     */
    public int size() {
        return size;
    }
    
    /**
     * Clears the sparse list.
     */
    public void clear() {
        size = 0;
        root = null;
    }
    
    /**
     * Sets the value at the specified location. 
     *
     * This lazy implementation simply performs a remove and then
     * an add. This should be sufficient since the tree does not
     * punish size-changing operations too severely. This also
     * avoids the ugly cases of set() where a null is being
     * replaced by an Object and an Object being replaced by a null.
     *
     * @return the previous value at that location.
     */
    public Object set(int index, Object value) {
        Object previous = remove(index);
        add(index, value);
        return previous;
    }
    
    /**
     * Deletes the node with the specified sort-order from the tree.
     */
    public Object remove(int index) {
        if(index >= size) throw new IndexOutOfBoundsException("cannot remove from tree of size " + size + " at " + index);
        Object value = get(index);

        // remove from the main tree
        if(root != null && index < root.size()) {
            root.remove(index);

        // remove from the trailing nulls
        } else {
            size--;
        }
        
        return value;
    }
    
    /**
     * Inserts the specified value at the specified location.
     */
    public void add(int index, Object value) {
        if(index > size) throw new IndexOutOfBoundsException("cannot insert into a tree of size " + size + " at " + index);

        // insert nulls as space
        if(value == null) {
            insertNulls(index, 1);
            return;
        }
        // ensure the node exists
        if(root == null) {
            root = new SparseListNode(this, null);
        }

        // when the insert is within the trailing nulls, remove them and re-insert them
        if(index > root.size()) {

            // the base size is the size of the tree
            int baseSize = root.size();

            // the interned nulls is the count of nulls moving into the tree
            int internedNulls = index - baseSize;
            childSizeChanged(-1 * internedNulls);
            root.insert(baseSize, value);

            // replace the removed nulls
            insertNulls(baseSize, internedNulls);

        // when the insert is within the main tree
        } else {
            root.insert(index, value);
        }
    }

    /**
     * Inserts a sequence of nulls into the list.
     */
    public void insertNulls(int index, int length) {
        if(index > size) throw new IndexOutOfBoundsException("cannot insert into a tree of size " + size + " at " + index);

        // insert the space after all else
        if(root == null || index >= root.size()) {
            size = size + length;
            return;
        }
        // ensure the node exists
        if(root == null) {
            root = new SparseListNode(this, null);
        }
        
        // insert in the main tree
        root.insertSpace(index, length);
    }
    
    /**
     * Gets the value at the specified index.
     */
    public Object get(int index) {
        if(index >= size) throw new IndexOutOfBoundsException("cannot get from a tree of size " + size + " at " + index);

        // get from the main tree
        if(root != null && index < root.size()) {
            SparseListNode node = root.getNodeByIndex(index);
            if(node == null) return null;
            return node.getValue();

        // get from the trailing nulls
        } else {
            return null;
        }
    }
    
    /**
     * Sets the root of this tree to be the specified node. This is
     * used by the owned SparseListNodes when the root changes due to a
     * rotation.
     */
    void setRootNode(SparseListNode root) {
        this.root = root;
    }

    /**
     * Changes the size of the tree. This is used by the owned
     * SparseListNodes when the tree changes due to a removal.
     *
     * @param difference the amount of nodes that the tree has changed
     *      by. This is positive for adds and negative for removes.
     */
    void childSizeChanged(int difference) {
        size = size + difference;
    }
    
    /**
     * Gets the list in tree form by its contents.
     */
    public String toString() {
        return listToString(this) + "/" + root;
    }
    
    /**
     * Verifies that the tree has a consistent state.
     */
    void validate() {
        if(root != null) root.validate();
    }
    
    /**
     * Gets the compressed view of this list.
     */
    public List getCompressedList() {
        return compressedList;
    }
    
    /**
     * A CompressedList is a read-only view of the list where all the nulls
     * have been removed.
     */
    class CompressedList extends AbstractList {
        
        /**
         * Gets the value at the specified index.
         */
        public Object get(int index) {
            if(root == null) throw new IndexOutOfBoundsException("cannot get from tree of size 0 at " + index);
            SparseListNode node = root.getNodeByCompressedIndex(index);
            return node.getValue();
        }
        
        /**
         * Gets the size of the compressed list. This is a shortcut to
         * the number of non-null elements are in the main list.
         */
        public int size() {
            return root.treeSize();
        }
    }
    
    /**
     * Gets the index of the node with the specified compressedIndex.
     * Compressed index is defined as the index of that element in
     * the compressed list. This is the index of the node if all nulls
     * were to be removed from the list.
     */
    public int getIndex(int compressedIndex) {
        if(root == null) throw new IndexOutOfBoundsException("cannot get from a tree of size 0 at " + compressedIndex);
        SparseListNode node = root.getNodeByCompressedIndex(compressedIndex);
        return node.getIndex();
    }
    
    /**
     * Gets the compressed index of the node with the specified natural
     * index. The parameter index specifies the index in the natural tree
     * and this returns the index in the compressed tree. This method will
     * throw an IllegalArgumentException if the value with the specified
     * index is null.
     */
    public int getCompressedIndex(int index) {
        if(index >= size) throw new IndexOutOfBoundsException("cannot get from a tree of size " + size + " at " + index);
        
        // get from the main tree
        if(root != null && index < root.size()) {
            SparseListNode node = root.getNodeByIndex(index);
            if(node == null) throw new IllegalArgumentException("Cannot get compressed index of " + index + ", that value is compressed out");
            return node.getCompressedIndex();

        // get from the trailing nulls
        } else {
            throw new IllegalArgumentException("Cannot get compressed index of " + index + ", that value is compressed out");
        }
    }
    
    /**
     * Utility method for displaying a list that has many nulls
     * in a console-friendly fashion.
     */
    private static String listToString(List list) {
        StringBuffer result = new StringBuffer();
        for(int i = 0; i < list.size(); i++) {
            if(i != 0) result.append(" ");
            Object current = list.get(i);
            if(current == null) result.append("-");
            else result.append(current);
        }
        return result.toString();
    }
    
    
    /**
     * Test method for the sublist.
     */
    public static void main(String[] args) {
        if(args.length != 5) {
            System.out.println("Usage: SparseList <% null> <operations> <cycles> <seed> <list type>");
            System.out.println("List Type : \"ArrayList\" or \"SparseList\"");
            return;
        }
        
        // parse the parameters
        int percentNull = Integer.parseInt(args[0]);
        int operations = Integer.parseInt(args[1]);
        int cycles = Integer.parseInt(args[2]);
        int seed = Integer.parseInt(args[3]);
        String listType = args[4];
        List testList = null;
        if(listType.equals("ArrayList")) {
            testList = new ArrayList();
        } else if(listType.equals("SparseList")) {
            testList = new SparseList();
        } else {
            System.out.println("List type " + listType + " not supported");
            return;
        }
        
        // populate an array of objects with the appropriate percent of null values
        Object[] values = new Object[100];
        int nonNullCount = 100 - percentNull;
        for(int i = 0; i < nonNullCount; i++) {
            values[i] = "*" + i + "*";
        }
        for(int i = nonNullCount; i < 100; i++) {
            values[i] = null;
        }
        
        // populate some arrays with randoms to get us started
        double[][][] randoms = new double[3][operations][cycles];
        Random random = new Random(seed);
        for(int a = 0; a < 3; a++) {
            for(int b = 0; b < operations; b++) {
                for(int c = 0; c < cycles; c++) {
                    randoms[a][b][c] = random.nextDouble();
                }
            }
        }
        
        // prepare to test this list
        System.out.print(listType);
        long startTime = System.currentTimeMillis();
        
        // run the operations once for each cycle
        for(int c = 0; c < cycles; c++) {
            testList.clear();

            // run operations on each list
            for(int i = 0; i < operations; i++) {
                // figure out the random operation for this particular element
                int insertLocation = (int)((double)testList.size() * randoms[0][i][c]);
                int insertValue = (int)((double)values.length * randoms[1][i][c]);
                int operation = (int)((double)4 * randoms[2][i][c]);
                
                // do the operation
                if(operation <= 1 || testList.size() == 0) {
                    testList.add(insertLocation, values[insertValue]);
                } else if(operation == 2) {
                    testList.remove(insertLocation);
                } else if(operation == 3) {
                    testList.set(insertLocation, values[insertValue]);
                }
            }
        }
        
        // summarize the results of this test
        long finishTime = System.currentTimeMillis();
        System.out.println(" time = " + (finishTime - startTime));
    }
    
    /**
     * Verifies that two lists match, throwing an exception if they
     * do not.
     */
    public static void verifyListsEqual(List alpha, List beta) {
        if(alpha.size() != beta.size()) throw new IllegalStateException("Sizes do not match " + alpha.size() + " != " + beta.size());
        
        for(int i = 0; i < alpha.size(); i++) {
            if(((alpha.get(i) == null) == (beta.get(i) == null)) || (alpha.get(i).equals(beta.get(i)))) {
                // do nothing
            } else {
                System.out.println("\n-----");
                System.out.println("ALPHA: " + listToString(alpha));
                System.out.println("BETA:  " + listToString(beta));
                throw new IllegalStateException("Mismatch at " + i + ", " + alpha.get(i) + " != " + beta.get(i));
            }
        }
    }
}
