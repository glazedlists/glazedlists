/**
 *
 */
package ca.odell.glazedlists;

import ca.odell.glazedlists.event.ListEvent;
import ca.odell.glazedlists.impl.gui.ThreadProxyEventList;
import ca.odell.glazedlists.impl.testing.AtLeastMatcherEditor;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * @author hbrands
 */
@State(Scope.Thread)
//@BenchmarkMode(Mode.AverageTime)
//@OutputTimeUnit(TimeUnit.MICROSECONDS)
public class ThreadProxyBenchmark {

    @Param({ "50", "500", "5000" })
    private int baseSize;

    private EventList<Integer> base;
    private EventList<Integer> filtered;
    private AtLeastMatcherEditor matcherEditor;

    private Random dice = new Random(0);

    @Setup(Level.Invocation)
    public void setUp() {
        matcherEditor = new AtLeastMatcherEditor();
        base = new BasicEventList<>();
        for (int i = 0; i < baseSize; i++) {
            base.add(new Integer(dice.nextInt(1000)));
        }
        filtered = new FilterList<>(base, matcherEditor);
    }

    @Benchmark
    @Warmup(iterations = 5)
    @Measurement(iterations = 10)
    @Fork(1)
    public EventList<Integer> testIterateListEvent() {
        EventList<Integer> threadProxied = new IterateListEventThreadProxy<>(filtered);
        matcherEditor.setMinimum(dice.nextInt(1000));
        return threadProxied;
    }

    @Benchmark
    @Warmup(iterations = 5)
    @Measurement(iterations = 10)
    @Fork(1)
    public EventList<Integer> testCopySource() {
        EventList<Integer> threadProxied = new ClearAddAllThreadProxy<>(filtered);
        matcherEditor.setMinimum(dice.nextInt(1000));
        return threadProxied;
    }

    @Benchmark
    @Warmup(iterations = 5)
    @Measurement(iterations = 10)
    @Fork(1)
    public EventList<Integer> testSyncCopy() {
        EventList<Integer> threadProxied = new SyncCopyThreadProxy<>(filtered);
        matcherEditor.setMinimum(dice.nextInt(1000));
        return threadProxied;
    }

    /**
     * A thread proxy that uses clear and add all to update from one list to another.
     */
    public static class ClearAddAllThreadProxy<T> extends ThreadProxyEventList<T> {
        public ClearAddAllThreadProxy(EventList<T> source) {
            super(source);
        }

        @Override
        protected void schedule(Runnable runnable) {
            runnable.run();
        }

        @Override
        protected List<T> applyChangeToCache(EventList<T> source, ListEvent<T> listChanges, List<T> localCache) {
            return new ArrayList<>(source);
        }
    }

    /**
     * A thread proxy that iterates the change to update from one list to another.
     */
    public static class IterateListEventThreadProxy<T> extends ThreadProxyEventList<T> {
        public IterateListEventThreadProxy(EventList<T> source) {
            super(source);
        }

        @Override
        protected void schedule(Runnable runnable) {
            runnable.run();
        }

        @Override
        protected List<T> applyChangeToCache(EventList<T> source, ListEvent<T> listChanges, List<T> localCache) {
            while (listChanges.next()) {
                final int sourceIndex = listChanges.getIndex();
                final int changeType = listChanges.getType();

                switch (changeType) {
                case ListEvent.DELETE:
                    localCache.remove(sourceIndex);
                    break;
                case ListEvent.INSERT:
                    localCache.add(sourceIndex, source.get(sourceIndex));
                    break;
                case ListEvent.UPDATE:
                    localCache.set(sourceIndex, source.get(sourceIndex));
                    break;
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

        @Override
        protected void schedule(Runnable runnable) {
            runnable.run();
        }

        @Override
        protected List<T> applyChangeToCache(EventList<T> source, ListEvent<T> listChanges, List<T> localCache) {
            List<T> result = new ArrayList<>(source.size());

            // cacheOffset == the difference between localCache and result
            int resultIndex = 0;
            int cacheOffset = 0;

            while (true) {

                // find the next change (or the end of the list)
                int changeIndex;
                int changeType;
                if (listChanges.next()) {
                    changeIndex = listChanges.getIndex();
                    changeType = listChanges.getType();
                } else {
                    changeIndex = source.size();
                    changeType = -1;
                }

                // perform all the updates before this change
                for (; resultIndex < changeIndex; resultIndex++) {
                    result.add(resultIndex, localCache.get(resultIndex + cacheOffset));
                }

                // perform this change
                if (changeType == ListEvent.DELETE) {
                    cacheOffset++;
                } else if (changeType == ListEvent.UPDATE) {
                    result.add(resultIndex, source.get(changeIndex));
                    resultIndex++;
                } else if (changeType == ListEvent.INSERT) {
                    result.add(resultIndex, source.get(changeIndex));
                    resultIndex++;
                    cacheOffset--;
                } else if (changeType == -1) {
                    break;
                }
            }

            return result;
        }
    }

}
