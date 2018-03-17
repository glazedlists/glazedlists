package ca.odell.glazedlists;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;

@State(Scope.Benchmark)
public class BasicEventListBenchmark {

    /**
     * for remove test
     */
    private final BasicEventList<Integer> list = new BasicEventList<>();

    public BasicEventListBenchmark() {
        while (this.list.size() < 1000) {
            this.list.add(this.list.size());
        }
    }

    @Benchmark
    @Warmup(iterations = 3)
    @Measurement(iterations = 10)
    @Fork(3)
    public void testAdd() {
        BasicEventList<Integer> list = new BasicEventList<>();

        while (list.size() < 500) {
            list.add(list.size());
        }

        while (list.size() < 1000) {
            list.add(0, list.size());
        }
    }

    @Benchmark
    @Warmup(iterations = 3)
    @Measurement(iterations = 10)
    @Fork(3)
    public void testRemove() {

        while (list.size() < 500) {
            list.add(list.size());
        }

        while (list.size() < 1000) {
            list.add(0, list.size());
        }

        while (list.size() > 500) {
            list.remove(0);
        }

        while (list.size() > 0) {
            list.remove(list.size() - 1);
        }
    }
}
