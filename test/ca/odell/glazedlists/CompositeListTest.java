/* Glazed Lists                                                 (c) 2003-2005 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists;

import java.util.*;
// for being a JUnit test case
import junit.framework.*;

/**
 * A CompositeListTest tests the functionality of the CompositeList.
 *
 * @author <a href="mailto:jesse@odel.on.ca">Jesse Wilson</a>
 */
public class CompositeListTest extends TestCase {

    /**
     * Verifies that the a single source works.
     */
    public void testSingleSource() {
        EventList<String> wendys = new BasicEventList<String>();
        wendys.add("Classic Single");
        wendys.add("Chili");
        wendys.add("Frosty");
        wendys.add("Junior Bacon Cheeseburger");
        
        CompositeList<String> fastFood = new CompositeList<String>();
        fastFood.addListEventListener(new ListConsistencyListener(fastFood, "fastFood", false));
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
        List<String> fastFoodVerify = new ArrayList<String>();

        EventList<String> wendys = new BasicEventList<String>();
        wendys.add("Classic Single");
        wendys.add("Chili");
        wendys.add("Frosty");
        wendys.add("Junior Bacon Cheeseburger");
        
        EventList<String> mcDonalds = new BasicEventList<String>();
        mcDonalds.add("McDLT");
        mcDonalds.add("McPizza");
        mcDonalds.add("McSalad Shaker");
        mcDonalds.add("Royal with Cheese");

        EventList<String> tacoBell = new BasicEventList<String>();
        tacoBell.add("Fries Supreme");
        tacoBell.add("Bean Burrito");
        
        CompositeList<String> fastFood = new CompositeList<String>();
        fastFood.addListEventListener(new ListConsistencyListener(fastFood, "fastFood", false));
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
        EventList<String> wendys = new BasicEventList<String>();
        EventList<String> mcDonalds = new BasicEventList<String>();
        EventList<String> tacoBell = new BasicEventList<String>();
        
        CompositeList<String> fastFood = new CompositeList<String>();
        fastFood.addListEventListener(new ListConsistencyListener(fastFood, "fastFood", false));
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
        List<String> fastFoodVerify = new ArrayList<String>();

        EventList<String> wendys = new BasicEventList<String>();
        wendys.add("Spicy Chicken Sandwich");
        EventList<String> mcDonalds = new BasicEventList<String>();
        mcDonalds.add("Arch Deluxe");
        mcDonalds.add("McLean Deluxe");
        
        CompositeList<String> fastFood = new CompositeList<String>();
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

    /**
     * Test that {@link CompositeList} is well behaved when only a single element
     * is removed.
     */
    public void testSingleElements() {
        EventList<String> alpha = new BasicEventList<String>();
        alpha.add("A");
        EventList<String> beta = new BasicEventList<String>();
        beta.add("B");

        CompositeList<String> aToB = new CompositeList<String>();
        aToB.addListEventListener(new ListConsistencyListener(aToB, "AtoB", false));
        aToB.addMemberList(alpha);
        aToB.removeMemberList(alpha);
        aToB.addMemberList(beta);
    }
}