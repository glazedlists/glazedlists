package ca.odell.glazedlists.hibernate;

/**
 * <code>EventListTypeTest</code>.
 */
@HibernateConfig(mappings = {"User.hbm.xml"})
public class EventListTypeTest extends AbstractEventListTypeTest {

    @Override
    protected Class<?> getExpectedPersistentCollectionClass() {
        return PersistentEventList.class;
    }
}
