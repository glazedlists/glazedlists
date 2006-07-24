/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package com.publicobject.glazedlists.japex;

import com.sun.japex.JapexDriverBase;
import com.sun.japex.TestCase;

import java.util.*;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.FilterList;
import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.event.ListEvent;
import ca.odell.glazedlists.impl.gui.ThreadProxyEventList;
import ca.odell.glazedlists.matchers.Matcher;
import ca.odell.glazedlists.matchers.AbstractMatcherEditor;

/**
 * This Japex driver simulates a thread proxy. This thread proxy itself isn't
 * actually multithreaded.
 *
 * @author <a href="mailto:jesse@swank.ca">Jesse Wilson</a>
 */
public class ThreadProxyDriver extends JapexDriverBase {

    private GreaterThanMatcherEditor<Integer> matcherEditor;
    private Random dice = new Random(0);

    public void initializeDriver() {
        // do nothing
    }

    public void prepare(TestCase testCase) {

        // decide our update strategy
        String updateStrategy = getParam("GlazedLists.ThreadProxyUpdateStrategy");

        // prepare a matcher, we change this every iteration
        String distinctValuesCSV = testCase.getParam("distinctValues");
        matcherEditor = new GreaterThanMatcherEditor<Integer>();
        matcherEditor.setValue(new Integer(0));

        // create a short pipeline, ending in a thread proxied list
        EventList<Integer> base = new BasicEventList<Integer>();
        EventList<Integer> filtered = new FilterList<Integer>(base, matcherEditor);

        if("iteratelistevent".equals(updateStrategy)) {
            EventList<Integer> threadProxied = new IterateListEventThreadProxy<Integer>(filtered);
        } else if("clearaddall".equals(updateStrategy)) {
            EventList<Integer> threadProxied = new ClearAddAllThreadProxy<Integer>(filtered);
        } else if("synccopy".equals(updateStrategy)) {
            EventList<Integer> threadProxied = new SyncCopyThreadProxy<Integer>(filtered);
        }

        // fill it with data
        int baseSize = testCase.getIntParam("baseSize");
        for(int i = 0; i < baseSize; i++) {
            base.add(new Integer(dice.nextInt(1000)));
        }
    }

    /**
     * Warmup is exactly the same as the run method.
     */
    public void warmup(TestCase testCase) {
        matcherEditor.setValue(new Integer(dice.nextInt(1000)));
    }

    /**
     * Execute the specified testcase one time.
     */
    public void run(TestCase testCase) {
        matcherEditor.setValue(new Integer(dice.nextInt(1000)));
    }

    public void finish(TestCase testCase) {
        // do nothing
    }

    public void terminateDriver() {
        // do nothing
    }

    /**
     * Match a single value.
     */
    private static class GreaterThanMatcherEditor<T extends Comparable> extends AbstractMatcherEditor<T> {
        public void setValue(T value) {
            fireChanged(new GreaterThanMatcher<T>(value));
        }
        private static class GreaterThanMatcher<T extends Comparable> implements Matcher<T> {
            private T value;
            public GreaterThanMatcher(T value) {
                this.value = value;
            }
            public boolean matches(T item) {
                return item.compareTo(value) > 0;
            }
        }
    }

    /**
     * A thread proxy that uses clear and add all to update from one list to
     * another.
     */
    public static class ClearAddAllThreadProxy<T> extends ThreadProxyEventList<T> {
        public ClearAddAllThreadProxy(EventList<T> source) {
            super(source);
        }
        protected void schedule(Runnable runnable) {
            runnable.run();
        }
        protected List applyChangeToCache(List<T> source, ListEvent<T> listChanges, List<T> localCache) {
            return new ArrayList(source);
        }
    }

    /**
     * A thread proxy that iterates the change to update from one list to another.
     */
    public static class IterateListEventThreadProxy<T> extends ThreadProxyEventList<T> {
        public IterateListEventThreadProxy(EventList<T> source) {
            super(source);
        }
        protected void schedule(Runnable runnable) {
            runnable.run();
        }
        protected List applyChangeToCache(List<T> source, ListEvent<T> listChanges, List<T> localCache) {
            while(listChanges.next()) {
                final int sourceIndex = listChanges.getIndex();
                final int changeType = listChanges.getType();

                switch (changeType) {
                    case ListEvent.DELETE: localCache.remove(sourceIndex); break;
                    case ListEvent.INSERT: localCache.add(sourceIndex, source.get(sourceIndex)); break;
                    case ListEvent.UPDATE: localCache.set(sourceIndex, source.get(sourceIndex)); break;
                }
            }
            return localCache;
        }
    }


    /**
     * A thread proxy that iterates the change creating a new list in the process.
     */
    private static class SyncCopyThreadProxy<T> extends ThreadProxyEventList<T> {
        public SyncCopyThreadProxy(EventList<T> source) {
            super(source);
        }
        protected void schedule(Runnable runnable) {
            runnable.run();
        }
        public List applyChangeToCache(List<T> source, ListEvent<T> listChanges, List<T> localCache) {
            List<T> result = new ArrayList<T>(source.size());

            // cacheOffset == the difference between localCache and result
            int resultIndex = 0;
            int cacheOffset = 0;

            while(true) {

                // find the next change (or the end of the list)
                int changeIndex;
                int changeType;
                if(listChanges.next()) {
                    changeIndex = listChanges.getIndex();
                    changeType = listChanges.getType();
                } else {
                    changeIndex = source.size();
                    changeType = -1;
                }

                // perform all the updates before this change
                for(; resultIndex < changeIndex; resultIndex++) {
                    result.add(resultIndex, localCache.get(resultIndex + cacheOffset));
                }

                // perform this change
                if(changeType == ListEvent.DELETE) {
                    cacheOffset++;
                } else if(changeType == ListEvent.UPDATE) {
                    result.add(resultIndex, source.get(changeIndex));
                    resultIndex++;
                } else if(changeType == ListEvent.INSERT) {
                    result.add(resultIndex, source.get(changeIndex));
                    resultIndex++;
                    cacheOffset--;
                } else if(changeType == -1) {
                    break;
                }
            }

            return result;
        }
    }
}
