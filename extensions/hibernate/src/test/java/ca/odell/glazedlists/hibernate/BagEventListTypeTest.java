package ca.odell.glazedlists.hibernate;

/**
 * <code>BagEventListTypeTest</code>.
 */
@HibernateConfig(mappings = {"UserBag.hbm.xml"})
public class BagEventListTypeTest extends AbstractEventListTypeTest {

    @Override
    protected Class<?> getExpectedPersistentCollectionClass() {
        return PersistentBagEventList.class;
    }

}
