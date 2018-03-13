/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists;

// for being a JUnit test case
import ca.odell.glazedlists.event.ListEvent;
import ca.odell.glazedlists.event.ListEventListener;
import ca.odell.glazedlists.impl.testing.ListConsistencyListener;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Validates that {@link EventList}s can recover from {@link RuntimeException}s.
 *
 * @author <a href="mailto:jesse@swank.ca">Jesse Wilson</a>
 */
public class RuntimeExceptionTest {

    /** the exception fired by the event lists */
    private RuntimeException luckyException = new RuntimeException();

    /** list to fire events */
    private BasicEventList<String> source = new BasicEventList<>();

    /** listener that throws exceptions */
    private ExceptionThrower exceptionThrower = new ExceptionThrower();

    /** listener that validates events are received */
    private ListConsistencyListener listConsistencyListener = ListConsistencyListener.install(source);

    /**
     * Verifies that an Exception thrown by a ListEventListener is rethrown.
     */
    @Test
    public void testExceptionRethrown() {
        source.addListEventListener(exceptionThrower);
        listConsistencyListener = ListConsistencyListener.install(source);

        // make sure the plumbing is working
        source.add("Dan");
        source.add("Frank");
        source.add("Larry");
        listConsistencyListener.assertConsistent();

        // throw an exception, make sure its handled gracefully
        exceptionThrower.setNextException(luckyException);
        try {
            source.add("Adam");
            fail("this statement shouldn't be reached");
        } catch(RuntimeException e) {
            assertTrue(e == luckyException);
            listConsistencyListener.assertConsistent();
        }
    }


    /**
     * Verifies that an Exception thrown by a ListEventListener is rethrown.
     */
    @Test
    public void testExceptionRethrownListenerSecond() {
        listConsistencyListener = ListConsistencyListener.install(source);
        source.addListEventListener(exceptionThrower);

        // make sure the plumbing is working
        source.add("Matt");
        source.add("Kevin");
        source.add("Julie");
        listConsistencyListener.assertConsistent();

        // throw an exception, make sure its handled gracefully
        exceptionThrower.setNextException(luckyException);
        try {
            source.add("Leanne");
            fail("this statement shouldn't be reached");
        } catch(RuntimeException e) {
            assertTrue(e == luckyException);
            listConsistencyListener.assertConsistent();
        }
    }

    /**
     * Verifies that an Exception thrown by a ListEventListener is rethrown.
     */
    @Test
    public void testMultipleExceptions() {
        source.addListEventListener(exceptionThrower);
        listConsistencyListener = ListConsistencyListener.install(source);
        ExceptionThrower exceptionThrower2 = new ExceptionThrower();
        source.addListEventListener(exceptionThrower2);

        // make sure the plumbing is working
        source.add("Leanne");
        source.add("Bev");
        source.add("Jesse");
        listConsistencyListener.assertConsistent();

        // throw an exception, make sure its handled gracefully
        exceptionThrower.setNextException(luckyException);
        exceptionThrower2.setNextException(new NullPointerException());
        try {
            source.add("Eric");
            fail("this statement shouldn't be reached");
        } catch(RuntimeException e) {
            assertTrue(e == luckyException);
            listConsistencyListener.assertConsistent();
        }
    }

    /**
     * ListEventListener that throws an exception on demand.
     */
    static class ExceptionThrower implements ListEventListener<String> {
        private RuntimeException nextException = null;
        public void setNextException(RuntimeException nextException) {
            this.nextException = nextException;
        }
        @Override
        public void listChanged(ListEvent<String> listChanges) {
            if(nextException == null) return;
            RuntimeException toThrow = nextException;
            nextException = null;
            throw toThrow;
        }
    }
}
