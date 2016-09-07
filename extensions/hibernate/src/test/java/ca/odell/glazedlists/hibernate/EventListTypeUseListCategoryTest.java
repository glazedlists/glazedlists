/* Glazed Lists                                                 (c) 2003-2013 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.hibernate;

import ca.odell.glazedlists.event.ListEventAssembler;
import ca.odell.glazedlists.util.concurrent.LockFactory;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Tests mapping and persisting BasicEventLists with Hibernate using list categories.
 * Tested classes are {@link EventListType} and {@link PersistentEventList}.
 *
 * @author Holger Brands
 */
@HibernateConfig(mappings={"CategoryTestUser.hbm.xml"})
public class EventListTypeUseListCategoryTest extends HibernateTestCase {

    /**
     * Tests correct list category registration and clearing
     */
    @Test
    public void testListCategories() {
        final EventListType type = new EventListType();
        try {
            type.useListCategory("Test", LockFactory.DEFAULT.createReadWriteLock(),
                    ListEventAssembler.createListEventPublisher());
            fail("Expected IllegalStateException");
        } catch (IllegalStateException ex) {
            // expected, because category 'Test' is already registered with different values
        }
        type.useListCategory("Test");
        type.useListCategory("Test2");
        type.useListCategory("Test2", TestEventListType2.LOCK, TestEventListType2.PUBLISHER);
        CategoryEventListFactory.clearCategoryMapping();

        type.useListCategory("Test", LockFactory.DEFAULT.createReadWriteLock(),
                ListEventAssembler.createListEventPublisher());
        type.useListCategory("Test2");
        try {
            type.useListCategory("Test2", TestEventListType2.LOCK, TestEventListType2.PUBLISHER);
            fail("Expected IllegalStateException");
        } catch (IllegalStateException ex) {
            // expected
        }
        CategoryEventListFactory.clearCategoryMapping();
    }
}