/* Glazed Lists                                                 (c) 2003-2005 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists;

// for being a JUnit test case
import junit.framework.*;
// the Glazed Lists' change objects
import ca.odell.glazedlists.event.*;

/**
 * Validates that {@link EventList}s can recover from {@link RuntimeException}s.
 *
 * @author <a href="mailto:jesse@odel.on.ca">Jesse Wilson</a>
 */
public class RuntimeExceptionTest extends TestCase {

    /** the exception fired by the event lists */
    private RuntimeException luckyException = new RuntimeException();
    
    /** list to fire events */
    private BasicEventList source = new BasicEventList();

    /** listener that throws exceptions */
    private ExceptionThrower exceptionThrower = new ExceptionThrower();

    /** listener that validates events are received */
    private ListConsistencyListener listConsistencyListener = new ListConsistencyListener(source, "exceptions");
    
    /**
     * Prepare for the test.
     */
    public void setUp() {
        // do nothing
    }

    /**
     * Clean up after the test.
     */
    public void tearDown() {
        // do nothing
    }

    /**
     * Verifies that an Exception thrown by a ListEventListener is rethrown.
     */
    public void testExceptionRethrown() {
        source.addListEventListener(exceptionThrower);
        source.addListEventListener(listConsistencyListener);
        
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
    public void testExceptionRethrownListenerSecond() {
        source.addListEventListener(listConsistencyListener);
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
    public void testMultipleExceptions() {
        source.addListEventListener(exceptionThrower);
        source.addListEventListener(listConsistencyListener);
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
    class ExceptionThrower implements ListEventListener {
        private RuntimeException nextException = null;
        public void setNextException(RuntimeException nextException) {
            this.nextException = nextException;
        }
        public void listChanged(ListEvent listChanges) {
            if(nextException == null) return;
            RuntimeException toThrow = nextException;
            nextException = null;
            throw toThrow;
        }
    }
}
