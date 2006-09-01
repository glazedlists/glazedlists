/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.hibernate;

import junit.framework.TestCase;
import org.hibernate.HibernateException;
import org.hibernate.Interceptor;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.cfg.Environment;
import org.hibernate.dialect.Dialect;
import org.hibernate.engine.SessionFactoryImplementor;
import org.hibernate.mapping.Collection;
import org.hibernate.mapping.PersistentClass;
import org.hibernate.mapping.Property;
import org.hibernate.mapping.SimpleValue;

import java.sql.Blob;
import java.sql.Clob;
import java.util.Iterator;

/**
 * Base class for Hibernate related test cases.
 * <p> Inspired by and adapted from the TestCase class from the Hibernate distribution.
 * 
 * @author Holger Brands
 */
public abstract class AbstractHibernateTestCase extends TestCase {

    /** Cached SessionFactory. */
    private static SessionFactory sessions;

    /** Cached Configuration. */
    private static Configuration cfg;

    /** Cached Dialect. */
    private static Dialect dialect;

    /** Last executed test class. */
    private static Class lastTestClass;

    /** Current session. */
    private Session session;

    
    /**
     * Constructor with name.
     */
    public AbstractHibernateTestCase(String x) {
        super(x);
    }

    /**
     * Should the schema be dropped and recreated?
     */
    protected boolean recreateSchema() {
        return true;
    }

    /**
     * Additional configuration for sublasses.
     */
    protected void configure(Configuration cfg) {
        // NOP
    }

    /**
     * Creates a new SessionFactory with the supplied mapping files.
     * 
     * @param files the mapping files
     */
    private void buildSessionFactory(String[] files) throws Exception {

        if (getSessions() != null)
            getSessions().close();

        setDialect(Dialect.getDialect());
        if (!appliesTo(getDialect())) {
            return;
        }

        try {

            setCfg(new Configuration());

            if (recreateSchema()) {
                cfg.setProperty(Environment.HBM2DDL_AUTO, "create-drop");
            }

            for (int i = 0; i < files.length; i++) {
                if (!files[i].startsWith("net/"))
                    files[i] = getBaseForMappings() + files[i];
                getCfg().addResource(files[i], TestCase.class.getClassLoader());
            }

            configure(cfg);

            if (getCacheConcurrencyStrategy() != null) {

                Iterator iter = cfg.getClassMappings();
                while (iter.hasNext()) {
                    PersistentClass clazz = (PersistentClass) iter.next();
                    Iterator props = clazz.getPropertyClosureIterator();
                    boolean hasLob = false;
                    while (props.hasNext()) {
                        Property prop = (Property) props.next();
                        if (prop.getValue().isSimpleValue()) {
                            String type = ((SimpleValue) prop.getValue()).getTypeName();
                            if ("blob".equals(type) || "clob".equals(type))
                                hasLob = true;
                            if (Blob.class.getName().equals(type)
                                    || Clob.class.getName().equals(type))
                                hasLob = true;
                        }
                    }
                    if (!hasLob && !clazz.isInherited() && overrideCacheStrategy()) {
                        cfg.setCacheConcurrencyStrategy(clazz.getEntityName(),
                                getCacheConcurrencyStrategy());
                    }
                }

                iter = cfg.getCollectionMappings();
                while (iter.hasNext()) {
                    Collection coll = (Collection) iter.next();
                    cfg.setCollectionCacheConcurrencyStrategy(coll.getRole(),
                            getCacheConcurrencyStrategy());
                }

            }

            setSessions(getCfg().buildSessionFactory( /* new TestInterceptor() */));

            afterSessionFactoryBuilt();
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * For subclasses to override in order to perform extra "stuff" only when SF (re)built...
     */
    protected void afterSessionFactoryBuilt() throws Exception {
        // NOP
    }

    protected boolean overrideCacheStrategy() {
        return true;
    }

    protected String getBaseForMappings() {
        return "ca/odell/glazedlists/hibernate/";
    }

    public String getCacheConcurrencyStrategy() {
        return "nonstrict-read-write";
    }

    /**
     * Setup of testcase.
     * <p>Builds session factory if it's the first execution of test class. 
     */
    protected void setUp() throws Exception {
        if (getSessions() == null || lastTestClass != getClass()) {
            buildSessionFactory(getMappings());
            lastTestClass = getClass();
        }
    }

    /**
     * Handles test run.
     * <p>Closes open sessions and optionally drops session factory after test failure.
     */
    protected void runTest() throws Throwable {
        final boolean stats = ((SessionFactoryImplementor) sessions).getStatistics()
                .isStatisticsEnabled();
        try {
            if (stats)
                sessions.getStatistics().clear();

            super.runTest();

            if (stats)
                sessions.getStatistics().logSummary();

            if (session != null && session.isOpen()) {
                if (session.isConnected())
                    session.connection().rollback();
                session.close();
                session = null;
                fail("unclosed session");
            } else {
                session = null;
            }
        } catch (Throwable e) {
            try {
                if (session != null && session.isOpen()) {
                    if (session.isConnected())
                        session.connection().rollback();
                    session.close();
                }
            } catch (Exception ignore) {
            }
            try {
                if (dropAfterFailure() && sessions != null) {
                    sessions.close();
                    sessions = null;
                }
            } catch (Exception ignore) {
            }
            throw e;
        }
    }

    /**
     * Should session factory be closed after test failure?
     */
    protected boolean dropAfterFailure() {
        return true;
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
        session = getSessions().openSession(interceptor);
        return session;
    }

    /**
     * Provide the list of mapping files for this test case.
     * @return mapping files names relative to the spefified base
     * 
     * @see #getBaseForMappings()
     */
    protected abstract String[] getMappings();

    private void setSessions(SessionFactory sessions) {
        AbstractHibernateTestCase.sessions = sessions;
    }

    protected SessionFactory getSessions() {
        return AbstractHibernateTestCase.sessions;
    }

    private void setDialect(Dialect dialect) {
        AbstractHibernateTestCase.dialect = dialect;
    }

    protected Dialect getDialect() {
        return AbstractHibernateTestCase.dialect;
    }

    protected static void setCfg(Configuration cfg) {
        AbstractHibernateTestCase.cfg = cfg;
    }

    protected static Configuration getCfg() {
        return AbstractHibernateTestCase.cfg;
    }

    /**
     * Intended to indicate that this test class as a whole is intended for a dialect or series of
     * dialects. Skips here (appliesTo = false), therefore simply indicate that the given tests
     * target a particular feature of the current database...
     * 
     * @param dialect
     */
    public boolean appliesTo(Dialect dialect) {
        return true;
    }
}