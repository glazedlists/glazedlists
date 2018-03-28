/**
 *
 */
package ca.odell.glazedlists;

import ca.odell.glazedlists.adt.SortedListWithIndexedTree;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;

import java.util.Random;

/**
 * @author hbrands
 *
 */
@State(Scope.Benchmark)
//@BenchmarkMode(Mode.AverageTime)
//@OutputTimeUnit(TimeUnit.MICROSECONDS)
public class SortedListBenchmark {

   @Param({ "0", "100000" })
   private int baseSize;

   private int changeSize;

   private EventList<Integer> base;

   private Random dice = new Random(0);

   @Setup
   public void setUp() {
       if (baseSize == 0) {
           changeSize = 10000;
       }
       if (baseSize == 100000) {
           changeSize = 0;
       }

       base = new BasicEventList<>();
       for(int i = 0; i < baseSize; i++) {
           base.add(new Integer(dice.nextInt(Integer.MAX_VALUE)));
       }
   }

   @Benchmark
   @Warmup(iterations = 5)
   @Measurement(iterations = 10)
   @Fork(1)
   public EventList<Integer> testIndexedTree() {
       EventList<Integer> baseCopy = GlazedLists.eventList(base);
       EventList<Integer> sortedBase = new SortedListWithIndexedTree<>(baseCopy);
       doTest(baseCopy, sortedBase);
       return sortedBase;
   }

   @Benchmark
   @Warmup(iterations = 5)
   @Measurement(iterations = 10)
   @Fork(1)
   public EventList<Integer> testBarcode() {
       EventList<Integer> baseCopy = GlazedLists.eventList(base);
       EventList<Integer> sortedBase = new SortedList<>(baseCopy);
       doTest(baseCopy, sortedBase);
       return sortedBase;
   }

   private void doTest(EventList<Integer> baseCopy, EventList<Integer> sortedBase) {
       for(int i = 0; i < changeSize; i++) {
           baseCopy.add(new Integer(Integer.MAX_VALUE));
       }
       // get all values
       for(int i = 0; i < sortedBase.size(); i++) {
           sortedBase.get(i);
       }
       // remove N
       for(int i = 0; i < changeSize; i++) {
           baseCopy.remove(baseCopy.size() - 1);
       }
   }
}
