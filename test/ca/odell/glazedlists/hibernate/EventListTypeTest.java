/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.hibernate;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.event.ListEvent;
import ca.odell.glazedlists.event.ListEventListener;
import ca.odell.glazedlists.impl.testing.GlazedListsTests;

import java.util.Iterator;
import java.util.List;

import org.hibernate.FetchMode;
import org.hibernate.Hibernate;
import org.hibernate.Session;
import org.hibernate.Transaction;

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
        
        assertNotNull(u);

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
            // ATTENTION: NO ListEvents should be produced by Hibernate's lazy initialization
            assertEquals(0, listener.getCountAndReset());
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
    @Override
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
        assertNotNull(u);

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
            // ATTENTION: NO ListEvents should be produced by Hibernate's lazy initialization
            assertEquals(0, listener.getCountAndReset());
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
        assertNull(u);

        // no emails should be found
        emails = s.createCriteria(Email.class).list();
        assertEquals(0, emails.size());

        t.commit();
        s.close();
    }
    
    /**
     * Tests a lazy loaded, many-to-many association with entity collection. 
     */
    public void testLazyManyToManyAssociation() {
        doTestManyToManyAssociation(true);
    }

    /**
     * Tests an eager loaded, one-to-many, unidirectional association with entity collection. 
     */
    public void testEagerManyToManyAssociation() {
        doTestManyToManyAssociation(false);
    }

    /**
     * Runs tests for a many-to-many association
     * @param lazy indicates lazy or eager loading of collection
     */
    private void doTestManyToManyAssociation(boolean lazy) {
        // create user with roles and persist
        User u = createAndPersistUserWithRoles();

        // load user and roles in new session
        Session s = openSession();
        Transaction t = s.beginTransaction();

        // test that roles are saved in DB
        List roles = s.createCriteria(Role.class).list();
        assertEquals(2, roles.size());

        // load saved user again
        u = loadUser(s, lazy);
        assertNotNull(u);

        final GlazedListsTests.ListEventCounter<Role> listener = new GlazedListsTests.ListEventCounter<Role>();

        if (lazy) {
            // lazy collection still uninitialized
            assertFalse(Hibernate.isInitialized(u.getRoles()));
            // call EventList methods            
            u.getRoles().addListEventListener(listener);
            u.getRoles().removeListEventListener(listener);
            u.getRoles().addListEventListener(listener);
            u.getRoles().getReadWriteLock();
            u.getRoles().getPublisher();

            // lazy collection should still be uninitialized
            assertFalse(Hibernate.isInitialized(u.getRoles()));
            assertEquals(0, listener.getCountAndReset());

            // extra lazy collection should still be uninitialzed
            assertFalse(u.getRoles().isEmpty());
            assertEquals(2, u.getRoles().size());
            assertEquals("Guest", u.getRoles().get(0).getName());
            assertFalse(Hibernate.isInitialized(u.getRoles()));

            // trigger initialization
            u.getRoles().iterator();
            assertTrue(Hibernate.isInitialized(u.getRoles()));
            // ATTENTION: NO ListEvents should be produced by Hibernate's lazy initialization
            assertEquals(0, listener.getCountAndReset());
        } else {
            // collection should be initialized
            assertTrue(Hibernate.isInitialized(u.getRoles()));
            u.getRoles().addListEventListener(listener);
        }
        assertEquals(PersistentEventList.class, u.getRoles().getClass());
        
        // test manipulating list
        u.getRoles().remove(1);
        u.getRoles().add(1, new Role("Developer"));
        u.getRoles().remove(0);

        t.commit();
        s.close();
        assertEquals(3, listener.getCountAndReset());

        s = openSession();
        t = s.beginTransaction();
        u = loadUser(s, lazy);

        assertEquals(1, u.getRoles().size());
        assertEquals("Developer", u.getRoles().get(0).getName());
        u.getRoles().addListEventListener(listener);
        u.getRoles().clear();
        assertEquals(1, listener.getCountAndReset());
        assertEquals(0, u.getRoles().size());
        assertTrue(Hibernate.isInitialized(u.getRoles()));        
        roles = s.createCriteria(Role.class).list();
        assertEquals(3, roles.size());
        assertEquals("Developer", ((Role) roles.get(2)).getName());

        // delete user
        s.delete(u);
        
        // delete roles
        for (Iterator iter = roles.iterator(); iter.hasNext();) {
            final Role role = (Role) iter.next();
            s.delete(role);
        }
                
        t.commit();
        s.close();

        // no user should be found
        s = openSession();
        t = s.beginTransaction();
        u = loadUser(s, lazy);
        assertEquals(u, null);

        // no roles should be found
        roles = s.createCriteria(Role.class).list();
        assertEquals(0, roles.size());
        
        t.commit();
        s.close();
    }

    /**
     * Tests behaviour when Hibernate wraps an EventList.
     */
    public void testListWrapOnSave_FixMe() {
        // create user with nicknames and persist
        Session s = openSession();
        Transaction t = s.beginTransaction();
        User u = new User("admin");
        final ListEventSourceHandler handler1 = new ListEventSourceHandler();
        u.getNickNames().addListEventListener(handler1);
        u.addNickName("Hacker");
        u.addNickName("Maestro");
        assertEquals(BasicEventList.class, u.getNickNames().getClass());
        s.persist(u);
        // Hibernate has wrapped the BasicEventList with a PersistentEventList using
        // EventListType.wrap(...)
        assertEquals(PersistentEventList.class, u.getNickNames().getClass());
        t.commit();
        s.close();
        final ListEventSourceHandler handler2 = new ListEventSourceHandler();
        u.getNickNames().addListEventListener(handler2);        
        u.addNickName("Tricky");
        // delete user again
        s = openSession();
        t = s.beginTransaction();
        s.delete(u);
        t.commit();
        s.close();
        // compare list event source, should be the same
        assertTrue(handler1.source == handler2.source);        
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
     * Creates and persists an example user with roles.
     */
    private User createAndPersistUserWithRoles() {
        // create user with roles and persist
        Session s = openSession();
        Transaction t = s.beginTransaction();
        User u = new User("admin");
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
     * Helper class for capturing source list of list events
     */
    private static class ListEventSourceHandler implements ListEventListener {
        public EventList source;
        
        /** {@inheritDoc} */
        public void listChanged(ListEvent listChanges) {
            if (source == null) {
                source = listChanges.getSourceList();
            } else if (source != listChanges.getSourceList()) {
                    throw new IllegalStateException("SourceList changed");
            }
        }        
    }
}