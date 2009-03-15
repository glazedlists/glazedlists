/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.hibernate;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.event.ListEventAssembler;
import ca.odell.glazedlists.event.ListEventPublisher;
import ca.odell.glazedlists.impl.testing.GlazedListsTests;
import ca.odell.glazedlists.impl.testing.GlazedListsTests.ListEventCounter;
import ca.odell.glazedlists.impl.testing.GlazedListsTests.SerializableListener;
import ca.odell.glazedlists.impl.testing.GlazedListsTests.UnserializableListener;
import ca.odell.glazedlists.util.concurrent.LockFactory;
import ca.odell.glazedlists.util.concurrent.ReadWriteLock;

import org.hibernate.FetchMode;
import org.hibernate.Hibernate;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.io.IOException;

/**
 * Tests mapping and persisting BasicEventLists with Hibernate using list categories. 
 * Tested classes are {@link EventListType} and {@link PersistentEventList}.
 * 
 * @author Holger Brands
 */
public class EventListTypeCategoryTest extends AbstractHibernateTestCase {

    /**
     * Constructor with name.
     */
    public EventListTypeCategoryTest(String name) {
        super(name);
    }

    /**
     * Tests a lazy loaded collection.
     */
    public void testLazyCollection() {
        doTestCollection(true);
    }

    /**
     * Tests an eager loaded collection.
     */
    public void testEagerCollection() {
        doTestCollection(false);
    }
    
    /**
     * Runs tests for custom collections. 
     * 
     * @param lazy indicates lazy or eager loading of collections
     */
    private void doTestCollection(boolean lazy) {
        // create user and persist
        User u = createAndPersistUser();

        // load user in new session
        Session s = openSession();
        Transaction t = s.beginTransaction();

        // load saved user again
        u = loadUser(s, lazy);
        
        assertNotNull(u);
        if (lazy) {
            // lazy collection still uninitialized
            assertFalse(Hibernate.isInitialized(u.getNickNames()));
            assertFalse(Hibernate.isInitialized(u.getEmailAddresses()));
            assertFalse(Hibernate.isInitialized(u.getRoles()));
            
            // test ReadWriteLock equality
            ReadWriteLock nickNamesLock = u.getNickNames().getReadWriteLock();
            ReadWriteLock emailLock = u.getEmailAddresses().getReadWriteLock();
            assertEquals(nickNamesLock, emailLock);
            assertEquals(TestEventListType2.LOCK, u.getRoles().getReadWriteLock());
            
            // test publisher equality
            ListEventPublisher nickNamesPublisher = u.getNickNames().getPublisher();
            ListEventPublisher emailPublisher = u.getEmailAddresses().getPublisher();
            assertEquals(nickNamesPublisher, emailPublisher);
            assertEquals(TestEventListType2.PUBLISHER, u.getRoles().getPublisher());
            
            // lazy collection should still be uninitialized
            assertFalse(Hibernate.isInitialized(u.getNickNames()));
            assertFalse(Hibernate.isInitialized(u.getEmailAddresses()));
            assertFalse(Hibernate.isInitialized(u.getRoles()));

            // trigger initialization        
            assertEquals(2, u.getNickNames().size());
            assertTrue(Hibernate.isInitialized(u.getNickNames()));
            assertEquals(2, u.getEmailAddresses().size());
            assertTrue(Hibernate.isInitialized(u.getEmailAddresses()));
                        
            // test ReadWriteLock equality again
            nickNamesLock = u.getNickNames().getReadWriteLock();
            emailLock = u.getEmailAddresses().getReadWriteLock();
            assertEquals(nickNamesLock, emailLock);
            assertEquals(TestEventListType2.LOCK, u.getRoles().getReadWriteLock());
            
            // test publisher equality again
            nickNamesPublisher = u.getNickNames().getPublisher();
            emailPublisher = u.getEmailAddresses().getPublisher();
            assertEquals(nickNamesPublisher, emailPublisher);
            assertEquals(TestEventListType2.PUBLISHER, u.getRoles().getPublisher());
        } else {
            // collection should be initialized
            assertTrue(Hibernate.isInitialized(u.getNickNames()));
            assertTrue(Hibernate.isInitialized(u.getEmailAddresses()));
            assertTrue(Hibernate.isInitialized(u.getRoles()));
            // test ReadWriteLock equality
            final ReadWriteLock nickNamesLock = u.getNickNames().getReadWriteLock();
            final ReadWriteLock emailLock = u.getEmailAddresses().getReadWriteLock();
            assertEquals(nickNamesLock, emailLock);
            assertEquals(TestEventListType2.LOCK, u.getRoles().getReadWriteLock());
            
            // test publisher equality
            final ListEventPublisher nickNamesPublisher = u.getNickNames().getPublisher();
            final ListEventPublisher emailPublisher = u.getEmailAddresses().getPublisher();
            assertEquals(nickNamesPublisher, emailPublisher);
            assertEquals(TestEventListType2.PUBLISHER, u.getRoles().getPublisher());
            
        }
        assertEquals(PersistentEventList.class, u.getNickNames().getClass());
        assertEquals(PersistentEventList.class, u.getEmailAddresses().getClass());
        assertEquals(PersistentEventList.class, u.getRoles().getClass());
        
        // delete user
        s.delete(u);
        t.commit();
        s.close();

        s = openSession();
        t = s.beginTransaction();
        u = loadUser(s, lazy);
        assertEquals(u, null);
        t.commit();
        s.close();
    }
    
    /**
     * Tests serialization of objects with PeristentEventLists.
     */
    public void testSerialize() throws IOException, ClassNotFoundException {
        // create user and persist
        User u = createAndPersistUser();

        // load user in new session
        Session s = openSession();
        Transaction t = s.beginTransaction();

        // load saved user again
        u = loadUser(s, false);        
        assertNotNull(u);
        final EventList<String> nickNames = u.getNickNames();
        final EventList<Email> emails = u.getEmailAddresses();
        
        UnserializableListener listener = new UnserializableListener();
        nickNames.addListEventListener(listener);
        SerializableListener serListener = new SerializableListener();
        nickNames.addListEventListener(serListener);
        nickNames.add("Testit");
        assertEquals(nickNames, UnserializableListener.getLastSource());
        assertEquals(nickNames, SerializableListener.getLastSource());
        
        final User uCopy = GlazedListsTests.serialize(u);
        final EventList<String> nickNamesCopy = uCopy.getNickNames();
        final EventList<Email> emailsCopy = uCopy.getEmailAddresses();
        assertTrue(Hibernate.isInitialized(nickNamesCopy));
        assertTrue(Hibernate.isInitialized(emailsCopy));
        assertEquals(PersistentEventList.class, nickNamesCopy.getClass());
        assertEquals(PersistentEventList.class, emailsCopy.getClass());
        
        assertEquals(nickNames, nickNamesCopy);
        assertEquals(emails, emailsCopy);
        assertEquals(3, nickNamesCopy.size());
        assertEquals(2, emailsCopy.size());
        ListEventCounter counter = new ListEventCounter();
        nickNamesCopy.addListEventListener(counter);
        nickNamesCopy.remove("Hacker");
        assertEquals(1, counter.getCountAndReset());        
        assertEquals(nickNames, UnserializableListener.getLastSource());
        assertEquals(nickNamesCopy, SerializableListener.getLastSource());

        final ReadWriteLock nickNamesLock = nickNamesCopy.getReadWriteLock();
        final ReadWriteLock emailLock = emailsCopy.getReadWriteLock();
        assertEquals(nickNamesLock, emailLock);

        final ListEventPublisher nickNamesPublisher = nickNamesCopy.getPublisher();
        final ListEventPublisher emailPublisher = emailsCopy.getPublisher();
        assertEquals(nickNamesPublisher, emailPublisher);

        // delete user
        s.delete(u);
        t.commit();
        s.close();
    }
    
    /**
     * Tests correct list category registration and clearing
     */
    public void testListCategories() {
        final EventListType type = new EventListType();
        try {
            type.useListCategory("Test", LockFactory.DEFAULT.createReadWriteLock(),
                    ListEventAssembler.createListEventPublisher());
            fail("Expected IllegalStateException");
        } catch (IllegalStateException ex) {
            // expected, because category 'Test' is already registered with different values
        }
        type.useListCategory("Test");
        type.useListCategory("Test2");
        type.useListCategory("Test2", TestEventListType2.LOCK, TestEventListType2.PUBLISHER);
        CategoryEventListFactory.clearCategoryMapping();
        
        type.useListCategory("Test", LockFactory.DEFAULT.createReadWriteLock(),
                ListEventAssembler.createListEventPublisher());
        type.useListCategory("Test2");
        try {
            type.useListCategory("Test2", TestEventListType2.LOCK, TestEventListType2.PUBLISHER);
            fail("Expected IllegalStateException");
        } catch (IllegalStateException ex) {
            // expected
        }
        CategoryEventListFactory.clearCategoryMapping();
    }
    
    /**
     * Gets mapping files for this test case. 
     */
    @Override
    protected String[] getMappings() {
        return new String[] { "CategoryTestUser.hbm.xml" };
    }
    
    /**
     * Creates and persists an example user with nicknames.
     */
    private User createAndPersistUser() {
        // create user with nicknames and persist
        Session s = openSession();
        Transaction t = s.beginTransaction();
        User u = new User("admin");
        u.addNickName("Hacker");
        u.addNickName("Maestro");
        u.getEmailAddresses().add(new Email("admin@hibernate.org"));
        u.getEmailAddresses().add(new Email("admin@gmail.com"));
        u.addRole(new Role("Guest"));
        u.addRole(new Role("Administrator"));        
        s.persist(u);
        t.commit();
        s.close();
        return u;
    }
    

    /**
     * Loads the example user.
     * 
     * @param lazy <code>true</code>, if collections shoud be lazy loaded, <code>false</code> otherwise
     */
    private User loadUser(Session s, boolean lazy) {
        if (lazy) {
            return (User) s.createCriteria(User.class).uniqueResult();
        } else {
            return (User) s.createCriteria(User.class)
            	.setFetchMode("emailAddresses", FetchMode.JOIN)
                .setFetchMode("nickNames", FetchMode.JOIN)
                .setFetchMode("roles", FetchMode.JOIN)
            	.uniqueResult();
        }
    }
    
    /**
     * Custom EventListType to test list categories. 
     */
    public static final class TestEventListType2 extends EventListType {
        /** Lock as constant. */
        public static final ReadWriteLock LOCK = LockFactory.DEFAULT.createReadWriteLock();
        /** Publisher as constant. */
        public static final ListEventPublisher PUBLISHER = ListEventAssembler.createListEventPublisher();
        
        /** Constructor which sets a list category. */
        public TestEventListType2() {
            useListCategory("Test2", LOCK, PUBLISHER);
        }
    }
    
}