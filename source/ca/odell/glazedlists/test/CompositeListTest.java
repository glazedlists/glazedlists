/**
 * Glazed Lists
 * http://glazedlists.dev.java.net/
 *
 * COPYRIGHT 2003 O'DELL ENGINEERING LTD.
 */
package ca.odell.glazedlists.test;

import java.util.*;
// for being a JUnit test case
import junit.framework.*;
// the core Glazed Lists package
import ca.odell.glazedlists.*;
import ca.odell.glazedlists.util.*;

/**
 * A CompositeListTest tests the functionality of the CompositeList.
 *
 * @author <a href="mailto:jesse@odel.on.ca">Jesse Wilson</a>
 */
public class CompositeListTest extends TestCase {

    /**
     * Prepare for the test.
     */
    public void setUp() {
        // do nothing
    }

    /**
     * Clean up after the test.
     */
    public void tearDown() {
        // do nothing
    }

    /**
     * Verifies that the a single source works.
     */
    public void testSingleSource() {
        BasicEventList wendys = new BasicEventList();
        wendys.add("Classic Single");
        wendys.add("Chili");
        wendys.add("Frosty");
        wendys.add("Junior Bacon Cheeseburger");
        
        CompositeList fastFood = new CompositeList();
        fastFood.addMemberList(wendys);
        
        assertEquals(wendys, fastFood);
        
        wendys.add("Sour Cream 'n' Onion Baked Potato");
        wendys.add("Taco Supremo Salad");
        
        assertEquals(wendys, fastFood);
        
        wendys.remove(1);
        fastFood.set(0, "Big Bacon Classic");
        fastFood.remove(1);
        
        assertEquals(wendys, fastFood);
    }

    /**
     * Verifies that multiple sources work.
     */
    public void testMultipleSources() {
        List fastFoodVerify = new ArrayList();

        BasicEventList wendys = new BasicEventList();
        wendys.add("Classic Single");
        wendys.add("Chili");
        wendys.add("Frosty");
        wendys.add("Junior Bacon Cheeseburger");
        
        BasicEventList mcDonalds = new BasicEventList();
        mcDonalds.add("McDLT");
        mcDonalds.add("McPizza");
        mcDonalds.add("McSalad Shaker");
        mcDonalds.add("Royal with Cheese");

        BasicEventList tacoBell = new BasicEventList();
        tacoBell.add("Fries Supreme");
        tacoBell.add("Bean Burrito");
        
        CompositeList fastFood = new CompositeList();
        fastFood.addMemberList(wendys);
        fastFood.addMemberList(mcDonalds);
        fastFood.addMemberList(tacoBell);
        
        fastFoodVerify.clear();
        fastFoodVerify.addAll(wendys);
        fastFoodVerify.addAll(mcDonalds);
        fastFoodVerify.addAll(tacoBell);
        assertEquals(fastFoodVerify, fastFood);
        
        wendys.add("Sour Cream 'n' Onion Baked Potato");
        wendys.add("Taco Supremo Salad");
        
        fastFoodVerify.clear();
        fastFoodVerify.addAll(wendys);
        fastFoodVerify.addAll(mcDonalds);
        fastFoodVerify.addAll(tacoBell);
        assertEquals(fastFoodVerify, fastFood);

        wendys.remove(1);
        fastFood.set(0, "Big Bacon Classic");
        fastFood.remove(1);
        
        fastFoodVerify.clear();
        fastFoodVerify.addAll(wendys);
        fastFoodVerify.addAll(mcDonalds);
        fastFoodVerify.addAll(tacoBell);
        assertEquals(fastFoodVerify, fastFood);
        
        mcDonalds.add("Big Mac");
        fastFoodVerify.clear();
        fastFoodVerify.addAll(wendys);
        fastFoodVerify.addAll(mcDonalds);
        fastFoodVerify.addAll(tacoBell);
        assertEquals(fastFoodVerify, fastFood);
        
        fastFood.removeMemberList(mcDonalds);
        
        fastFoodVerify.clear();
        fastFoodVerify.addAll(wendys);
        fastFoodVerify.addAll(tacoBell);
        assertEquals(fastFoodVerify, fastFood);
    }

    /**
     * Verifies that remove member list does so by reference.
     */
    public void testRemoveByReference() {
        BasicEventList wendys = new BasicEventList();
        BasicEventList mcDonalds = new BasicEventList();
        BasicEventList tacoBell = new BasicEventList();
        
        CompositeList fastFood = new CompositeList();
        fastFood.addMemberList(wendys);
        fastFood.addMemberList(mcDonalds);
        fastFood.addMemberList(tacoBell);

        fastFood.removeMemberList(tacoBell);
        fastFood.removeMemberList(wendys);
        
        assertEquals(mcDonalds, fastFood);
        
        mcDonalds.add("Arch Deluxe");
        assertEquals(mcDonalds, fastFood);
    }

    /**
     * Verifies that multiple copies of the same list can  be added.
     */
    public void testMultipleCopies() {
        List fastFoodVerify = new ArrayList();

        BasicEventList wendys = new BasicEventList();
        wendys.add("Spicy Chicken Sandwich");
        BasicEventList mcDonalds = new BasicEventList();
        mcDonalds.add("Arch Deluxe");
        mcDonalds.add("McLean Deluxe");
        
        CompositeList fastFood = new CompositeList();
        fastFood.addMemberList(wendys);
        fastFood.addMemberList(mcDonalds);

        fastFoodVerify.clear();
        fastFoodVerify.addAll(wendys);
        fastFoodVerify.addAll(mcDonalds);
        assertEquals(fastFoodVerify, fastFood);

        fastFood.addMemberList(wendys);
        fastFood.addMemberList(mcDonalds);

        fastFoodVerify.clear();
        fastFoodVerify.addAll(wendys);
        fastFoodVerify.addAll(mcDonalds);
        fastFoodVerify.addAll(wendys);
        fastFoodVerify.addAll(mcDonalds);
        assertEquals(fastFoodVerify, fastFood);

        fastFood.removeMemberList(wendys);
        fastFood.removeMemberList(wendys);

        fastFoodVerify.clear();
        fastFoodVerify.addAll(mcDonalds);
        fastFoodVerify.addAll(mcDonalds);
        assertEquals(fastFoodVerify, fastFood);
    }
}
