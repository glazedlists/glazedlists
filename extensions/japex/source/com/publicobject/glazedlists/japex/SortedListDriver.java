/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package com.publicobject.glazedlists.japex;

import com.sun.japex.*;
import ca.odell.glazedlists.*;

import java.util.*;

/**
 * Validate the performance of <code>SortedList</code>
 *
 * @author <a href="mailto:jesse@swank.ca">Jesse Wilson</a>
 */
public class SortedListDriver extends JapexDriverBase {

    private int baseSize;
    private int changeSize;
    private EventList<Integer> base;
    private EventList<Integer> sortedBase;

    public void initializeDriver() {
        // do nothing
    }

    public void prepare(TestCase testCase) {
        baseSize = testCase.getIntParam("baseSize");
        changeSize = testCase.getIntParam("changeSize");
        String backingTree = getParam("backingTree");

        base = new BasicEventList<Integer>();

        if("indexedTree".equals(backingTree)) {
            sortedBase = new SortedListWithIndexedTree<Integer>(base);
        } else if("barcode2".equals(backingTree)) {
            sortedBase = new SortedList<Integer>(base);
        } else {
            throw new IllegalArgumentException("Invalid sortedList parameter, " + backingTree);
        }

        Random dice = new Random(0);
        for(int i = 0; i < baseSize; i++) {
            base.add(new Integer(dice.nextInt(Integer.MAX_VALUE)));
        }
    }

    /**
     * Warmup is exactly the same as the run method.
     */
    public void warmup(TestCase testCase) {
        executeTestCase(testCase);
    }

    /**
     * Execute the specified testcase one time.
     */
    public void run(TestCase testCase) {
        executeTestCase(testCase);
    }

    private void executeTestCase(TestCase testCase) {
        // get 1000 values
        for(int i = 0; i < 1000; i++) {
            int index = i % sortedBase.size();
            sortedBase.get(index);
        }
        // add N
        for(int i = 0; i < changeSize; i++) {
            base.add(new Integer(Integer.MAX_VALUE));
        }
        // get 1000 values
        for(int i = 0; i < 1000; i++) {
            int index = i % sortedBase.size();
            sortedBase.get(index);
        }
        // remove N
        for(int i = 0; i < changeSize; i++) {
            base.remove(base.size() - 1);
        }
        // get 1000 values
        for(int i = 0; i < 1000; i++) {
            int index = i % sortedBase.size();
            sortedBase.get(index);
        }
    }

    public void finish(TestCase testCase) {
        // do nothing
    }

    public void terminateDriver() {
        // do nothing
    }

    public static void main(String[] args) {
        SortedListDriver driver;
        Random dice = new Random(0);

        // prepare
        driver = new SortedListDriver();
        driver.baseSize = 1000;
        driver.changeSize = 100;
        driver.base = new BasicEventList<Integer>();
        driver.sortedBase = new SortedList<Integer>(driver.base);
        for(int i = 0; i < driver.baseSize; i++) {
            driver.base.add(new Integer(dice.nextInt(Integer.MAX_VALUE)));
        }

        // run
        for(int i = 0; i < 5000; i++) {
            driver.run(null);
        }

        // prepare
        driver = new SortedListDriver();
        driver.baseSize = 1000;
        driver.changeSize = 100;
        driver.base = new BasicEventList<Integer>();
        driver.sortedBase = new SortedListWithIndexedTree<Integer>(driver.base);
        for(int i = 0; i < driver.baseSize; i++) {
            driver.base.add(new Integer(dice.nextInt(Integer.MAX_VALUE)));
        }

        // run
        for(int i = 0; i < 5000; i++) {
            driver.run(null);
        }
    }
}