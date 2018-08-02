package ca.odell.glazedlists;

import ca.odell.glazedlists.matchers.Matchers;

import java.util.Comparator;
import java.util.Random;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;

/**
 * <code>SeparatorListBenchmark</code>.
 */
@State(Scope.Benchmark)
public class SeparatorListBenchmark {

    @Param({"10", "100"})
    private int baseSize;

    @Param({"20", "250", "500"})
    private int groupElementCount;

    private EventList<Element> base;

    private Random dice = new Random(0);

    @Setup
    public void setUp() {

        base = new BasicEventList<>();
        for (int i = 0; i < baseSize; i++) {
            for (int j = 0; j < groupElementCount; j++) {
                base.add(new Element(i, j));
            }
        }
    }

    @Benchmark
    @Warmup(iterations = 5)
    @Measurement(iterations = 10)
    @Fork(1)
    public EventList<Element> testSeparatorListCrudNew() {
        EventList<Element> baseCopy = GlazedLists.eventList(base);
        EventList<Element> sepBase = new SeparatorList<>(baseCopy, elementComparator(), 0, Integer.MAX_VALUE);
        doTest(baseCopy, sepBase);
        return sepBase;
    }

    @Benchmark
    @Warmup(iterations = 5)
    @Measurement(iterations = 10)
    @Fork(1)
    public EventList<Element> testSeparatorListCrudTransactionNew() {
        EventList<Element> baseCopy = GlazedLists.eventList(base);
        TransactionList<Element> transactionList = new TransactionList<>(baseCopy);
        EventList<Element> sepBase = new SeparatorList<>(transactionList, elementComparator(), 0, Integer.MAX_VALUE);
        doTestTransaction(transactionList, sepBase);
        return sepBase;
    }

    @Benchmark
    @Warmup(iterations = 5)
    @Measurement(iterations = 10)
    @Fork(1)
    public EventList<Element> testSeparatorListFilterNew() {
        EventList<Element> baseCopy = GlazedLists.eventList(base);
        FilterList<Element> filterList = new FilterList<>(baseCopy);
        EventList<Element> sepBase = new SeparatorList<>(filterList, elementComparator(), 0, Integer.MAX_VALUE);
        for (int i = 0; i < 10; i++) {
            filterList.setMatcher(Matchers.falseMatcher());
            filterList.setMatcher(Matchers.trueMatcher());
        }
        return sepBase;
    }

    private void doTestTransaction(TransactionList<Element> transactionList, EventList<Element> sepBase) {
        transactionList.beginEvent();
        insert(transactionList);
        transactionList.commitEvent();
        transactionList.beginEvent();
        update(transactionList);
        transactionList.commitEvent();
        transactionList.beginEvent();
        delete(transactionList);
        transactionList.commitEvent();
        // transactionList.beginEvent();
        // clear(transactionList);
        // transactionList.commitEvent();
    }

    private void doTest(EventList<Element> baseList, EventList<Element> sepBase) {
        insert(baseList);
        update(baseList);
        delete(baseList);
    }

    private void insert(EventList<Element> baseList) {
        for (int i = 0; i < baseSize / 2; i++) {
            int group = dice.nextInt(baseSize) + baseSize;
            if (group % 2 == 0) {
                group = -group;
            }
            for (int j = 0; j < groupElementCount; j++) {
                baseList.add(new Element(group, j));
            }
        }
    }

    private void update(EventList<Element> baseList) {
        for (int i = 0; i < baseSize / 2; i++) {
            int group = dice.nextInt(baseSize);
            for (int j = 0; j < groupElementCount / 2; j++) {
                int index = dice.nextInt(baseSize * groupElementCount);
                if (index < baseList.size()) {
                    baseList.set(index, new Element(group, -j));
                }
            }
        }
    }

    private void delete(EventList<Element> baseList) {
        for (int i = 0; i < baseSize / 2; i++) {
            int startIndex = dice.nextInt(baseList.size() - groupElementCount);
            for (int j = 0; j < groupElementCount; j++) {
                int index = startIndex + j;
                if (index >= 0 && index < baseList.size()) {
                    baseList.remove(index);
                }
            }
        }
    }

    private Comparator<Element> elementComparator() {
        return new Comparator<Element>() {
            @Override
            public int compare(Element o1, Element o2) {
                int x = o1.getGroup();
                int y = o2.getGroup();
                return (x < y) ? -1 : ((x == y) ? 0 : 1);
            }
        };
    }

    private class Element {
        private final int group;

        private final int id;

        public Element(int group, int id) {
            this.group = group;
            this.id = id;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (o == null || getClass() != o.getClass())
                return false;

            Element that = (Element) o;

            return id == that.id;
        }

        @Override
        public String toString() {
            return "Element{" +
                    "group=" + group +
                    ", id=" + id +
                    '}';
        }

        @Override
        public int hashCode() {
            return id;
        }

        public int getGroup() {
            return group;
        }
    }

}
