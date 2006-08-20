/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.hibernate;

import ca.odell.glazedlists.GlazedListsTests;
import ca.odell.glazedlists.impl.testing.ListConsistencyListener;

import org.hibernate.FetchMode;
import org.hibernate.Hibernate;
import org.hibernate.Session;
import org.hibernate.Transaction;

import junit.framework.Test;
import junit.framework.TestSuite;

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
        User u = createAndPersistUser();

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
    private User createAndPersistUser() {
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
            	.uniqueResult();
        }
    }
}
