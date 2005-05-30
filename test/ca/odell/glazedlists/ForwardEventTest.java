/* Glazed Lists                                                 (c) 2003-2005 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists;

// for being a JUnit test case
import junit.framework.*;
// the Glazed Lists' change objects
import ca.odell.glazedlists.event.*;
// Java collections are used for underlying data storage
import java.util.*;

/**
 * Ensures that ListEventAssembler.forwardEvent() works.
 *
 * @author <a href="mailto:jesse@odel.on.ca">Jesse Wilson</a>
 */
public class ForwardEventTest extends TestCase {

    /** the origin of all events */
    private EventList source;
    
    /** gossipy list that forwards everything it hears */
    private ForwardingList forwarding;
    
    /** listens to anything the forwarding list will say, and validates it */
    private ConsistencyTestList test;

    /**
     * Prepare for the test.
     */
    public void setUp() {
        source = new BasicEventList();
        forwarding = new ForwardingList(source);
        test = new ConsistencyTestList(forwarding, "forwarding");
        forwarding.addListEventListener(test);
    }

    /**
     * Clean up after the test.
     */
    public void tearDown() {
        source = null;
        forwarding = null;
        test = null;
    }
    
    /**
     * Tests that forwardEvent works.
     */
    public void testForwarding() {
        source.add("Pepsi");
        source.add("Coke");
        source.add("RC");
        test.assertConsistent();
        
        source.addAll(Arrays.asList(new String[] { "7-up", "Dr. Pepper", "Sprite" }));
        source.retainAll(Arrays.asList(new String[] { "Pepsi", "7-up", "RC" }));
        test.assertConsistent();
    }
    
    
    /**
     * Tests that forwardEvent works.
     */
    public void testNestedForwarding() {
        forwarding.beginEvent();
        source.add("Pepsi");
        source.add("Coke");
        source.add("RC");
        forwarding.commitEvent();
        test.assertConsistent();
        
        forwarding.beginEvent();
        source.addAll(Arrays.asList(new String[] { "7-up", "Dr. Pepper", "Sprite" }));
        source.retainAll(Arrays.asList(new String[] { "Pepsi", "7-up", "RC" }));
        forwarding.commitEvent();
        test.assertConsistent();
    }
    
    /**
     * Simple TransformationList that forwards events.
     */
    static class ForwardingList extends TransformedList {
        public ForwardingList(EventList source) {
            super(source);
            source.addListEventListener(this);
        }
        public void listChanged(ListEvent e) {
            updates.forwardEvent(e);
        }
        public void beginEvent() {
            updates.beginEvent(true);
        }
        public void commitEvent() {
            updates.commitEvent();
        }
    }
}
