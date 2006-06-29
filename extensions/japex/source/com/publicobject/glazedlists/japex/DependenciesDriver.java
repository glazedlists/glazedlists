/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package com.publicobject.glazedlists.japex;

import com.sun.japex.JapexDriverBase;
import com.sun.japex.TestCase;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.FilterList;
import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.event.ListEventListener;
import ca.odell.glazedlists.event.ListEvent;

/**
 * This simple Japex driver tests how quickly the ListEventPublishers can manage
 * firing events while guaranteeing correct dependency ordering.
 *
 * @author <a href="mailto:jesse@swank.ca">Jesse Wilson</a>
 */
public class DependenciesDriver extends JapexDriverBase {

    private EventList<String> base;

    public void initializeDriver() {
        // do nothing
    }

    public void prepare(TestCase testCase) {
        String listEventPublisher = getParam("GlazedLists.ListEventPublisher");
        System.setProperty("GlazedLists.ListEventPublisher", listEventPublisher);

        base = new BasicEventList<String>();
        EventList<String> lastTransformed = base;

        // transform the list N times
        int transformations = testCase.getIntParam("transformations");
        for(int i = 0; i < transformations; i++) {
            lastTransformed = new FilterList<String>(base);
        }

        // listen to the furtherst transformation N times
        int listeners = testCase.getIntParam("listeners");
        for(int i = 0; i < listeners; i++) {
            lastTransformed.addListEventListener(new NoOpListEventListener());
        }
    }

    /**
     * Warmup is exactly the same as the run method.
     */
    public void warmup(TestCase testCase) {
        executeTestCase(testCase);
    }

    /**
     * Execute the specified testcase one time.
     */
    public void run(TestCase testCase) {
        executeTestCase(testCase);
    }

    private void executeTestCase(TestCase testCase) {
        base.add("A");
        base.remove(0);
    }

    public void finish(TestCase testCase) {
        // do nothing
    }

    public void terminateDriver() {
        // do nothing
    }

    private static class NoOpListEventListener implements ListEventListener {
        public void listChanged(ListEvent listChanges) {
            // do nothing
        }
    }
}