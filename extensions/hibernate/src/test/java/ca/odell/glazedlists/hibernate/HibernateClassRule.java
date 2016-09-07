/* Glazed Lists                                                 (c) 2003-2013 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.hibernate;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.AvailableSettings;
import org.hibernate.cfg.Configuration;
import org.hibernate.cfg.Environment;
import org.hibernate.dialect.Dialect;
import org.hibernate.internal.util.config.ConfigurationHelper;
import org.hibernate.mapping.Collection;
import org.hibernate.mapping.PersistentClass;
import org.hibernate.mapping.Property;
import org.hibernate.mapping.SimpleValue;
import org.hibernate.service.BootstrapServiceRegistry;
import org.hibernate.service.BootstrapServiceRegistryBuilder;
import org.hibernate.service.ServiceRegistryBuilder;
import org.hibernate.service.internal.StandardServiceRegistryImpl;
import org.junit.ClassRule;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import java.sql.Blob;
import java.sql.Clob;
import java.util.Iterator;
import java.util.Properties;

import javax.imageio.spi.ServiceRegistry;

import junit.framework.TestCase;

/**
 * A {@link TestRule} that initializes Hibernate. In particular it determines
 * the Mappings to use from the {@link HibernateConfig} annotation on the test
 * class and builds the session factory prior to running the tests.
 * <p>
 * Intended to be used as {@link ClassRule} to perform the initialization once
 * per test class.
 * </p>
 *
 * @author Holger Brands
 */
public class HibernateClassRule implements TestRule {

    private static final String[] NO_MAPPINGS = new String[0];

    /** Cached SessionFactory. */
    private SessionFactory sessions;

    /** Cached Configuration. */
    private Configuration cfg;

    /** Service Registry. */
    private StandardServiceRegistryImpl serviceRegistry;

    /** Cached Dialect. */
    private Dialect dialect;

    private String baseForMappings = "ca/odell/glazedlists/hibernate/";

    private String[] mappings = NO_MAPPINGS;

    @Override
    public Statement apply(final Statement base, final Description description) {
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                init(description);
                try {
                    base.evaluate();
                } finally {
                    dispose();
                }
            }
        };
    }

    /**
     * Initializes the mappings and the session factory.
     * @param description the junit description
     * @throws Throwable on error
     */
    private void init(Description description) throws Throwable {
        initMappings(description);
        initSessionFactory();
    }

    /**
     * Initializes the mappings from the {@link HibernateConfig} annotation on the test class.
     * @param description the junit description
     * @throws Throwable on error
     */
    private void initMappings(Description description) throws Throwable {
        final HibernateConfig config = description.getAnnotation(HibernateConfig.class);
        if (config == null) {
            throw new IllegalStateException("HibernateConfig annotation is missing on test class "
                    + description.getClassName());
        }
        final String baseForMappings = config.baseForMappings();
        if (baseForMappings != null && !baseForMappings.isEmpty()) {
            this.baseForMappings = baseForMappings;
        }
        final String[] mappings = config.mappings();
        if (mappings != null) {
            this.mappings = mappings;
        }
    }

    /**
     * Resets the mappings and closes the session factory.
     *
     * @throws Throwable on error
     */
    private void dispose() throws Throwable {
        disposeSessionFactory();
        disposeMappings();
    }

    /** Initializes the session factory. */
    private void initSessionFactory() throws Throwable {
        buildSessionFactory(getMappings());
    }

    /** CLoses and resets the session factory. */
    private void disposeSessionFactory() {
        if (getSessions() != null) {
            getSessions().close();
            setSessions(null);
        }
        if (serviceRegistry != null) {
            serviceRegistry.destroy();
            serviceRegistry = null;
        }
    }

    /** Resets the mappings. */
    private void disposeMappings() {
        baseForMappings = "ca/odell/glazedlists/hibernate/";
        mappings = NO_MAPPINGS;
    }

    /**
     * Recreates the session factory.
     * @throws Throwable
     */
    public void rebuildSessionFactory() throws Throwable {
        disposeSessionFactory();
        initSessionFactory();
    }

    private String getBaseForMappings() {
        return baseForMappings;
    }

    private String[] getMappings() {
        return mappings.clone();
    }

    private boolean overrideCacheStrategy() {
        return true;
    }

    private String getCacheConcurrencyStrategy() {
        return "nonstrict-read-write";
    }

    /**
     * Should the schema be dropped and recreated?
     */
    private boolean recreateSchema() {
        return true;
    }

    /**
     * Should the session factory b rebuilt when a test failure occurs?
     */
    boolean rebuildSessionFactoryOnError() {
        return true;
    }

    private void setSessions(SessionFactory sessions) {
        this.sessions = sessions;
    }

    /**
     * @return gets the sessino factory
     */
    public SessionFactory getSessions() {
        return sessions;
    }

    private void setDialect(Dialect dialect) {
        this.dialect = dialect;
    }

    /**
     * @return gets the dialect
     */
    public Dialect getDialect() {
        return dialect;
    }

    private void setCfg(Configuration cfg) {
        this.cfg = cfg;
    }

    /**
     * @return gets the hibernate configuration
     */
    public Configuration getCfg() {
        return cfg;
    }

    /**
     * Creates a new SessionFactory with the supplied mapping files.
     *
     * @param files the mapping files
     */
    private void buildSessionFactory(String[] files) throws Exception {

        if (getSessions() != null) {
            throw new IllegalStateException("Found SessionFactory at test start");
        }
        if (serviceRegistry != null) {
            throw new IllegalStateException("Found ServiceRegistry at test start");
        }

        setDialect(Dialect.getDialect());
        setCfg(createConfiguration());

        serviceRegistry = createServiceRegistry(getCfg());

        for (int i = 0; i < files.length; i++) {
            if (!files[i].startsWith("net/")) {
                files[i] = getBaseForMappings() + files[i];
            }
            getCfg().addResource(files[i], TestCase.class.getClassLoader());
        }
        getCfg().buildMappings();

        applyCacheSettings(getCfg());

        setSessions(getCfg().buildSessionFactory(serviceRegistry));
    }

    /**
     * Cretes a new Hibernate {@link Configuration}.
     *
     * @return the created configuration
     */
    private Configuration createConfiguration() {
        final Configuration result = new Configuration();
        result.setProperty(AvailableSettings.USE_NEW_ID_GENERATOR_MAPPINGS, "true");
        if (recreateSchema()) {
            result.setProperty(AvailableSettings.HBM2DDL_AUTO, "create-drop");
        }
        result.setProperty(AvailableSettings.DIALECT, getDialect().getClass().getName());
        return result;
    }

    /**
     * Creates a new Hibernate {@link ServiceRegistry}.
     *
     * @param configuration the used configuration
     * @return the created service registry
     */
    private static StandardServiceRegistryImpl createServiceRegistry(Configuration configuration) {
        Properties properties = new Properties();
        properties.putAll( configuration.getProperties() );
        Environment.verifyProperties( properties );
        ConfigurationHelper.resolvePlaceHolders( properties );

        BootstrapServiceRegistry bootstrapServiceRegistry =
                new BootstrapServiceRegistryBuilder().build();
        ServiceRegistryBuilder registryBuilder = new ServiceRegistryBuilder(
                bootstrapServiceRegistry).applySettings(properties);
        return (StandardServiceRegistryImpl) registryBuilder.buildServiceRegistry();
    }

    private void applyCacheSettings(Configuration configuration) {
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
                        if ("blob".equals(type) || "clob".equals(type)) {
                            hasLob = true;
                        }
                        if (Blob.class.getName().equals(type) || Clob.class.getName().equals(type)) {
                            hasLob = true;
                        }
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
    }
}
