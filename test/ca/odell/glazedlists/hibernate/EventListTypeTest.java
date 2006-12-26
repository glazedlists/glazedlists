/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.hibernate;

import ca.odell.glazedlists.impl.testing.GlazedListsTests;
import org.hibernate.FetchMode;
import org.hibernate.Hibernate;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.util.List;

/**
 * Tests mapping and persisting BasicEventLists with Hibernate. Tested classes are
 * {@link EventListType} and {@link PersistentEventList}.
 * 
 * @author Holger Brands
 */
public class EventListTypeTest extends AbstractHibernateTestCase {

    /**
     * Constructor with name.
     */
    public EventListTypeTest(String name) {
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
        User u = createAndPersistUserWithNickNames();

        // load user in new session
        Session s = openSession();
        Transaction t = s.beginTransaction();

        // load saved user again
        u = loadUser(s, lazy);
        assertTrue(u != null);

        final GlazedListsTests.ListEventCounter<String> listener = new GlazedListsTests.ListEventCounter<String>();

        if (lazy) {
            // lazy collection still uninitialized
            assertFalse(Hibernate.isInitialized(u.getNickNames()));
            // call EventList methods            
            u.getNickNames().addListEventListener(listener);
            u.getNickNames().removeListEventListener(listener);
            u.getNickNames().addListEventListener(listener);
            u.getNickNames().getReadWriteLock();
            u.getNickNames().getPublisher();

            // lazy collection should still be uninitialized
            assertFalse(Hibernate.isInitialized(u.getNickNames()));
            assertEquals(0, listener.getCountAndReset());

            // trigger initialization        
            assertEquals(2, u.getNickNames().size());
            assertTrue(Hibernate.isInitialized(u.getNickNames()));
            // ATTENTION: Hibernate calls add and set for each list element
            assertEquals(4, listener.getCountAndReset());
        } else {
            // collection should be initialized
            assertTrue(Hibernate.isInitialized(u.getNickNames()));
            u.getNickNames().addListEventListener(listener);
        }
        assertEquals(PersistentEventList.class, u.getNickNames().getClass());
        // test manipulating list
        u.getNickNames().remove(1);
        u.getNickNames().add(1, "Headbanger");
        u.getNickNames().remove(0);

        t.commit();
        s.close();
        assertEquals(3, listener.getCountAndReset());

        s = openSession();
        t = s.beginTransaction();
        u = loadUser(s, lazy);

        assertEquals(1, u.getNickNames().size());
        assertEquals("Headbanger", u.getNickNames().get(0));
        assertTrue(Hibernate.isInitialized(u.getNickNames()));

        // delete user
        s.delete(u);
        t.commit();
        s.close();

        // no user should be found
        s = openSession();
        t = s.beginTransaction();
        u = loadUser(s, lazy);
        assertEquals(u, null);
        t.commit();
        s.close();        
    }
    
    /**
     * Tests a lazy loaded, one-to-many, unidirectional association with entity collection. 
     */
    public void testLazyOneToManyUniDirectionalAssociation() {
        doTestOneToManyUniDirectionalAssociation(true);
    }

    /**
     * Tests an eager loaded, one-to-many, unidirectional association with entity collection. 
     */
    public void testEagerOneToManyUniDirectionalAssociation() {
        doTestOneToManyUniDirectionalAssociation(false);
    }

    /**
     * Gets mapping files for this test case. 
     */
    protected String[] getMappings() {
        return new String[] { "User.hbm.xml" };
    }

    /**
     * Runs tests for a one-to-many, unidirectional association with entity collection.
     * 
     * @param lazy indicates lazy or eager loading of email address collection
     */
    private void doTestOneToManyUniDirectionalAssociation(boolean lazy) {
        // create user with email addresses and persist
        User u = createAndPersistUserWithEmail();

        // load user and emails in new session
        Session s = openSession();
        Transaction t = s.beginTransaction();

        // test that emails are saved in DB
        List emails = s.createCriteria(Email.class).list();
        assertEquals(emails.size(), 2);

        // load saved user again
        u = loadUser(s, lazy);
        assertTrue(u != null);

        final GlazedListsTests.ListEventCounter<Email> listener = new GlazedListsTests.ListEventCounter<Email>();

        if (lazy) {
            // lazy collection still uninitialized
            assertFalse(Hibernate.isInitialized(u.getEmailAddresses()));
            // call EventList methods            
            u.getEmailAddresses().addListEventListener(listener);
            u.getEmailAddresses().removeListEventListener(listener);
            u.getEmailAddresses().addListEventListener(listener);
            u.getEmailAddresses().getReadWriteLock();
            u.getEmailAddresses().getPublisher();

            // lazy collection should still be uninitialized
            assertFalse(Hibernate.isInitialized(u.getEmailAddresses()));
            assertEquals(0, listener.getCountAndReset());

            // trigger initialization
            assertEquals(2, u.getEmailAddresses().size());
            assertTrue(Hibernate.isInitialized(u.getEmailAddresses()));
            // ATTENTION: Hibernate calls add and set for each list element
            assertEquals(4, listener.getCountAndReset());
        } else {
            // collection should be initialized
            assertTrue(Hibernate.isInitialized(u.getEmailAddresses()));
            u.getEmailAddresses().addListEventListener(listener);
        }
        assertEquals(PersistentEventList.class, u.getEmailAddresses().getClass());
        
        // test manipulating list
        u.getEmailAddresses().remove(1);
        u.getEmailAddresses().add(1, new Email("admin@web.de"));
        u.getEmailAddresses().remove(0);

        t.commit();
        s.close();
        assertEquals(3, listener.getCountAndReset());

        s = openSession();
        t = s.beginTransaction();
        u = loadUser(s, lazy);

        assertEquals(1, u.getEmailAddresses().size());
        assertEquals(new Email("admin@web.de"), u.getEmailAddresses().get(0));
        assertTrue(Hibernate.isInitialized(u.getEmailAddresses()));
        emails = s.createCriteria(Email.class).list();
        assertEquals(1, emails.size());
        assertEquals(new Email("admin@web.de"), emails.get(0));

        // delete user
        s.delete(u);
        t.commit();
        s.close();

        // no user should be found
        s = openSession();
        t = s.beginTransaction();
        u = loadUser(s, lazy);
        assertEquals(u, null);

        // no emails should be found
        emails = s.createCriteria(Email.class).list();
        assertEquals(0, emails.size());

        t.commit();
        s.close();
    }

    /**
     * Creates and persists an example user with emial addresses.
     */
    private User createAndPersistUserWithEmail() {
        // create user with email adresses and persist
        Session s = openSession();
        Transaction t = s.beginTransaction();
        User u = new User("admin");
        u.getEmailAddresses().add(new Email("admin@hibernate.org"));
        u.getEmailAddresses().add(new Email("admin@gmail.com"));
        s.persist(u);
        t.commit();
        s.close();
        return u;
    }
    
    /**
     * Creates and persists an example user with nicknames.
     */
    private User createAndPersistUserWithNickNames() {
        // create user with nicknames and persist
        Session s = openSession();
        Transaction t = s.beginTransaction();
        User u = new User("admin");
        u.addNickName("Hacker");
        u.addNickName("Maestro");
        s.persist(u);
        t.commit();
        s.close();
        return u;
    }

    /**
     * Loads the example user.
     * 
     * @param lazy <code>true</code>, if emails shoud be lazy loaded, <code>false</code> otherwise
     */
    private User loadUser(Session s, boolean lazy) {
        if (lazy) {
            return (User) s.createCriteria(User.class).uniqueResult();
        } else {
            return (User) s.createCriteria(User.class)
            	.setFetchMode("emailAddresses", FetchMode.JOIN)
                .setFetchMode("nickNames", FetchMode.JOIN)
            	.uniqueResult();
        }
    }
}
