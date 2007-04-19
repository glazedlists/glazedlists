/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.hibernate;

import ca.odell.glazedlists.event.ListEventPublisher;
import ca.odell.glazedlists.util.concurrent.ReadWriteLock;

import org.hibernate.FetchMode;
import org.hibernate.Hibernate;
import org.hibernate.Session;
import org.hibernate.Transaction;

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
     * Tests a lazy loaded value collection.
     */
    public void testLazyValueCollection() {
        doTestValueCollection(true);
    }

    /**
     * Tests an eager loaded value collection.
     */
    public void testEagerValueCollection() {
        doTestValueCollection(false);        
    }
    
    /**
     * Runs tests for a value collection of Strings.
     * 
     * @param lazy indicates lazy or eager loading of nick name collection
     */
    private void doTestValueCollection(boolean lazy) {
        // create user with email addresses and persist
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

            // test ReadWriteLock equality
            ReadWriteLock nickNamesLock = u.getNickNames().getReadWriteLock();
            ReadWriteLock emailLock = u.getEmailAddresses().getReadWriteLock();
            assertEquals(nickNamesLock, emailLock);
            
            // test publisher equality
            ListEventPublisher nickNamesPublisher = u.getNickNames().getPublisher();
            ListEventPublisher emailPublisher = u.getEmailAddresses().getPublisher();
            assertEquals(nickNamesPublisher, emailPublisher);
                        
            // lazy collection should still be uninitialized
            assertFalse(Hibernate.isInitialized(u.getNickNames()));

            // trigger initialization        
            assertEquals(2, u.getNickNames().size());
            assertTrue(Hibernate.isInitialized(u.getNickNames()));
            assertEquals(2, u.getEmailAddresses().size());
            assertTrue(Hibernate.isInitialized(u.getEmailAddresses()));
                        
            // test ReadWriteLock equality again
            nickNamesLock = u.getNickNames().getReadWriteLock();
            emailLock = u.getEmailAddresses().getReadWriteLock();
            assertEquals(nickNamesLock, emailLock);
            
            // test publisher equality again
            nickNamesPublisher = u.getNickNames().getPublisher();
            emailPublisher = u.getEmailAddresses().getPublisher();
            assertEquals(nickNamesPublisher, emailPublisher);
            
        } else {
            // collection should be initialized
            assertTrue(Hibernate.isInitialized(u.getNickNames()));
            assertTrue(Hibernate.isInitialized(u.getEmailAddresses()));
            // test ReadWriteLock equality
            final ReadWriteLock nickNamesLock = u.getNickNames().getReadWriteLock();
            final ReadWriteLock emailLock = u.getEmailAddresses().getReadWriteLock();
            assertEquals(nickNamesLock, emailLock);
            
            // test publisher equality
            final ListEventPublisher nickNamesPublisher = u.getNickNames().getPublisher();
            final ListEventPublisher emailPublisher = u.getEmailAddresses().getPublisher();
            assertEquals(nickNamesPublisher, emailPublisher);
            
        }
        assertEquals(PersistentEventList.class, u.getNickNames().getClass());
        assertEquals(PersistentEventList.class, u.getEmailAddresses().getClass());
        
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
     * Gets mapping files for this test case. 
     */
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
    public static final class TestEventListType extends EventListType {
        /** Constructor which sets a list category. */
        public TestEventListType() {
            setListCategory("Test");
        }
    }
    
}