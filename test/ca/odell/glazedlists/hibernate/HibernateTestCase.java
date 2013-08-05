/* Glazed Lists                                                 (c) 2003-2013 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.hibernate;

import org.hibernate.Interceptor;
import org.hibernate.Session;
import org.junit.ClassRule;
import org.junit.Rule;

/**
 * Base class for Hibernate-related test classses. By using the {@link HibernateClassRule} and
 * {@link HibernateTestRule}, it ensures proper initialisation and cleanup of the testcases.
 *
 * @author hbrands
 */
public abstract class HibernateTestCase {
    /**
     * ClassRule for determining the mappings and building the session factory.
     */
    @ClassRule
    public static HibernateClassRule hibernateClassRule = new HibernateClassRule();

    /**
     * TestRule that wraps the test methods for proper cleanup.
     */
    @Rule
    public HibernateTestRule hibernateTestRule = new HibernateTestRule(hibernateClassRule);

    /**
     * Opens a new Hibernate session.
     *
     * @return the created session.
     */
    protected Session openSession() {
        return hibernateTestRule.openSession();
    }

    /**
     * Opens a new Hibernate session with an interceptor.
     *
     * @param interceptor the interceptor
     * @return the created session
     */
    protected Session openSession(Interceptor interceptor) {
        return hibernateTestRule.openSession(interceptor);
    }
}
