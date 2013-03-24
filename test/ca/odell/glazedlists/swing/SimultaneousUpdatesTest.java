/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.swing;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.FilterList;
import ca.odell.glazedlists.impl.testing.ListConsistencyListener;
import ca.odell.glazedlists.matchers.AbstractMatcherEditor;
import ca.odell.glazedlists.matchers.Matcher;

import java.util.Random;

import javax.swing.SwingUtilities;

import org.junit.Test;

/**
 * Make sure we can handle multiple updates from different sources.
 *
 * @author <a href="mailto:jesse@swank.ca">Jesse Wilson</a>
 */
public class SimultaneousUpdatesTest {

    /**
     * This test verifies that we can have two threads changing the data and
     * everything will still be consistent as long as proper locks are held.
     */
    @Test
    public synchronized void testCompetingWriters() {

        // prepare a list with a Swing view
        EventList<Integer> list = new BasicEventList<Integer>();
        EvenOrAllMatcherEditor matcherEditor = new EvenOrAllMatcherEditor();
        FilterList<Integer> filterList = new FilterList<Integer>(list, matcherEditor);
        EventList<Integer> swingSafe = GlazedListsSwing.swingThreadProxyList(filterList);

        // make sure everything's always consistent
        ListConsistencyListener.install(swingSafe);

        // write using a background thread
        Thread backgroundThread = new Thread(new AddThenRemoveOnList(list));
        backgroundThread.start();

        // also write using a foreground thread
        try {
            for(int i = 0; i < 500; i++) {
                SwingUtilities.invokeLater(new FilterUnfilter(matcherEditor));
                wait(10);
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        // join the other thread to finish up
        try {
            backgroundThread.join();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private static class FilterUnfilter implements Runnable {
        private EvenOrAllMatcherEditor matcherEditor;
        public FilterUnfilter(EvenOrAllMatcherEditor matcherEditor) {
            this.matcherEditor = matcherEditor;
        }
        public synchronized void run() {
            matcherEditor.setEven();
            matcherEditor.setAll();
        }
    }

    /**
     * Add five values, then remove them.
     */
    private static class AddThenRemoveOnList implements Runnable {
        private final Random dice = new Random(15);
        private EventList<Integer> list;
        public AddThenRemoveOnList(EventList<Integer> list) {
            this.list = list;
        }
        public synchronized void run() {
            try {
                for(int j = 0; j < 500; ) {
                    list.getReadWriteLock().writeLock().lock();
                    try {
                        int i = j;
                        j += dice.nextInt(3);
                        for(; i < j; i++) {
                            list.add(new Integer(i));
                        }
                    } finally {
                        list.getReadWriteLock().writeLock().unlock();
                    }
                    wait(10);
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * Match only odd or even values.
     */
    private static class EvenOrAllMatcherEditor extends AbstractMatcherEditor<Integer> {
        public void setAll() {
            fireMatchAll();
        }
        public void setEven() {
            fireChanged(new EvenMatcherEditor());
        }
        private static class EvenMatcherEditor implements Matcher<Integer> {
            public boolean matches(Integer item) {
                return item.intValue() % 2 == 0;
            }
        }
    }
}
