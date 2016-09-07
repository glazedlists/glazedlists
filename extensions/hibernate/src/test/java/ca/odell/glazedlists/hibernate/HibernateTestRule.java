/* Glazed Lists                                                 (c) 2003-2013 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.hibernate;

import org.hibernate.HibernateException;
import org.hibernate.Interceptor;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.jdbc.Work;
import org.junit.Rule;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * A {@link TestRule} implementation for use as {@link Rule}, that wraps all
 * executions of test methods. It ensures that the current session is closed and
 * the session factory is recreated on error.
 *
 * @author Holger Brands
 */
public class HibernateTestRule implements TestRule {

    /** reference to the Hibernate class rule. */
    private HibernateClassRule hibernateClassRule;

    /** Current session. */
    private Session session;

    /**
     * Construktor with {@link HibernateClassRule}.
     *
     * @param hibernateClassRule the {@link HibernateClassRule}
     */
    public HibernateTestRule(HibernateClassRule hibernateClassRule) {
        this.hibernateClassRule = hibernateClassRule;
    }

    /**
     * @return gets the session factory
     */
    private SessionFactory getSessions() {
        return hibernateClassRule.getSessions();
    }

    /**
     * Recreates the session factory.
     *
     * @throws Throwable on error
     */
    private void recreateSessionFactory() throws Throwable {
        if (hibernateClassRule.rebuildSessionFactoryOnError()) {
            hibernateClassRule.rebuildSessionFactory();
        }
    }

    /**
     * Opens a new Hibernate session.
     */
    public Session openSession() throws HibernateException {
        session = getSessions().openSession();
        return session;
    }

    /**
     * Opens a new Hibernate session with an interceptor.
     */
    public Session openSession(Interceptor interceptor)
            throws HibernateException {
        session = getSessions().withOptions().interceptor(interceptor).openSession();
        return session;
    }

    @Override
    public Statement apply(final Statement base, Description description) {
        return new Statement() {

            @Override
            public void evaluate() throws Throwable {
                runTest(base);
            }
        };
    }

    /**
     * Runs the test method.
     *
     * @param delegate the delegate statement to execute
     * @throws Throwable on error
     */
    private void runTest(Statement delegate) throws Throwable {
        final boolean stats = ((SessionFactoryImplementor) getSessions()).getStatistics()
                .isStatisticsEnabled();
        try {
            if (stats) {
                getSessions().getStatistics().clear();
            }

            delegate.evaluate();

            if (stats) {
                getSessions().getStatistics().logSummary();
            }

            if (session != null && session.isOpen()) {
                cleanupSession();
                throw new IllegalStateException("unclosed session");
            } else {
                session = null;
            }
        } catch (Throwable e) {
            try {
                cleanupSession();
            } catch (Exception ignore) {
            }
            try {
                recreateSessionFactory();
            } catch (Exception ignore) {
                System.err.println("Could not recreate session factory, subsequent tests may fail"
                        + ignore);
            }
            throw e;
        }
    }

    private void cleanupSession() {
        if (session != null && session.isOpen()) {
            if (session.isConnected()) {
                session.doWork(new Rollback());
            }
            session.close();
            session = null;
        }
    }

    private static class Rollback implements Work {
        @Override
        public void execute(Connection connection) throws SQLException {
            connection.rollback();
        }
    }
}
