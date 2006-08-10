/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists;

import java.util.*;
// for being a JUnit test case
import junit.framework.*;

import ca.odell.glazedlists.event.ListEventAssembler;
import ca.odell.glazedlists.event.ListEventPublisher;
import ca.odell.glazedlists.util.concurrent.ReadWriteLock;
import ca.odell.glazedlists.util.concurrent.LockFactory;

/**
 * A CompositeListTest tests the functionality of the CompositeList.
 *
 * @author <a href="mailto:jesse@swank.ca">Jesse Wilson</a>
 */
public class CompositeListTest extends TestCase {

    /**
     * Verifies that a single source works.
     */
    public void testSingleSource() {
        CompositeList<String> fastFood = new CompositeList<String>();
        ListConsistencyListener.install(fastFood);

        EventList<String> wendys = fastFood.createMemberList();
        wendys.add("Classic Single");
        wendys.add("Chili");
        wendys.add("Frosty");
        wendys.add("Junior Bacon Cheeseburger");

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
        CompositeList<String> fastFood = new CompositeList<String>();
        ListConsistencyListener.install(fastFood);

        List<String> fastFoodVerify = new ArrayList<String>();

        EventList<String> wendys = fastFood.createMemberList();
        wendys.add("Classic Single");
        wendys.add("Chili");
        wendys.add("Frosty");
        wendys.add("Junior Bacon Cheeseburger");

        EventList<String> mcDonalds = fastFood.createMemberList();
        mcDonalds.add("McDLT");
        mcDonalds.add("McPizza");
        mcDonalds.add("McSalad Shaker");
        mcDonalds.add("Royal with Cheese");

        EventList<String> tacoBell = fastFood.createMemberList();
        tacoBell.add("Fries Supreme");
        tacoBell.add("Bean Burrito");

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
        CompositeList<String> fastFood = new CompositeList<String>();
        ListConsistencyListener.install(fastFood);
        
        EventList<String> wendys = fastFood.createMemberList();
        EventList<String> mcDonalds = fastFood.createMemberList();
        EventList<String> tacoBell = fastFood.createMemberList();

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
     * Verifies that multiple copies of the same list can be added.
     */
    public void testMultipleCopies() {
        List<String> fastFoodVerify = new ArrayList<String>();
        CompositeList<String> fastFood = new CompositeList<String>();

        EventList<String> wendys = fastFood.createMemberList();
        wendys.add("Spicy Chicken Sandwich");
        EventList<String> mcDonalds = fastFood.createMemberList();
        mcDonalds.add("Arch Deluxe");
        mcDonalds.add("McLean Deluxe");

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
        CompositeList<String> aToB = new CompositeList<String>();
        ListConsistencyListener.install(aToB);

        EventList<String> alpha = aToB.createMemberList();
        alpha.add("A");
        EventList<String> beta = aToB.createMemberList();
        beta.add("B");

        aToB.addMemberList(alpha);
        aToB.removeMemberList(alpha);
        aToB.addMemberList(beta);
    }

    /**
     * Test that when {@link CompositeList} is constructed with a publisher and read/write
     * lock, it uses them and produces member lists which also use them.
     */
    public void testPublisherAndLockConstructor() {
        final ReadWriteLock sharedLock = LockFactory.DEFAULT.createReadWriteLock();
        final ListEventPublisher sharedPublisher = ListEventAssembler.createListEventPublisher();
        
        final EventList<Object> alpha = new BasicEventList<Object>(sharedPublisher, sharedLock);
        final EventList<Object> beta = new BasicEventList<Object>(sharedPublisher, sharedLock);

        final CompositeList<Object> uber = new CompositeList<Object>(sharedPublisher, sharedLock);
        uber.addMemberList(alpha);
        uber.addMemberList(beta);

        final EventList<Object> gamma = uber.createMemberList();
        uber.addMemberList(gamma);
        assertSame(sharedLock, alpha.getReadWriteLock());
        assertSame(sharedLock, beta.getReadWriteLock());
        assertSame(sharedLock, gamma.getReadWriteLock());
        assertSame(sharedLock, uber.getReadWriteLock());
        
        assertSame(sharedPublisher, alpha.getPublisher());
        assertSame(sharedPublisher, beta.getPublisher());
        assertSame(sharedPublisher, gamma.getPublisher());
        assertSame(sharedPublisher, uber.getPublisher());
        
    }

    /**
     * Tests that when EventLists are added as members, an IllegalArgumentException should be thrown
     * if they don'to share the same lock and publisher with the CompositeList.
     */
    public void testAddMemberList() {
        final ReadWriteLock sharedLock = LockFactory.DEFAULT.createReadWriteLock();
        final ListEventPublisher sharedPublisher = ListEventAssembler.createListEventPublisher();
        
        final CompositeList<Object> uber = new CompositeList<Object>(sharedPublisher, sharedLock);        
        final EventList<Object> alpha = new BasicEventList<Object>();
        final EventList<Object> beta = new BasicEventList<Object>(sharedLock);
        final EventList<Object> gamma = new BasicEventList<Object>(sharedPublisher, sharedLock);
        try {
            uber.addMemberList(alpha);
            fail("Expected IllegalArgumentException for addMemberList");
        } catch (IllegalArgumentException ex) {
            // expected
        }        
        try {
            uber.addMemberList(beta);
            fail("Expected IllegalArgumentException for addMemberList");
        } catch (IllegalArgumentException ex) {
            // expected
        }        
        uber.addMemberList(gamma);
        assertSame(sharedLock, gamma.getReadWriteLock());
        assertSame(sharedLock, uber.getReadWriteLock());
        assertSame(sharedPublisher, gamma.getPublisher());
        assertSame(sharedPublisher, uber.getPublisher());        
    }
    
    /**
     * Tests that after disposing {@link CompositeList}, all installed ListEventListeners
     * have been removed, e.g. changes to member lists are ignored.
     */
    public void testDispose() {
        final CompositeList<String> composite = new CompositeList<String>();
        
        final EventList<String> memberListOne = composite.createMemberList();
        memberListOne.addAll(GlazedListsTests.stringToList("ABC"));
        final EventList<String> memberListTwo = composite.createMemberList();
        memberListTwo.addAll(GlazedListsTests.stringToList("DEF"));
        composite.addMemberList(memberListOne);
        composite.addMemberList(memberListTwo);
        final GlazedListsTests.ListEventCounter<String> eventCounter =
                new GlazedListsTests.ListEventCounter<String>();
        composite.addListEventListener(eventCounter);

        // modify member lists
        memberListOne.add("D");
        assertEquals(1, eventCounter.getCountAndReset());
        memberListTwo.remove("D");
        assertEquals(1, eventCounter.getCountAndReset());

        // dispose CompositeList, no ListEvents are expected further on
        composite.dispose();

        // modify member lists after dispose
        memberListOne.remove("D");
        assertEquals(0, eventCounter.getCountAndReset());
        memberListTwo.add(0, "D");
        assertEquals(0, eventCounter.getCountAndReset());
    }
}