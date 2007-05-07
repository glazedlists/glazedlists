package ca.odell.glazedlists.impl.adt;

import ca.odell.glazedlists.GlazedLists;

import java.util.Comparator;

/**
 * @author jessewilson
 */
public class KeyedCollectionForHashableValuesTest extends AbstractKeyedCollectionTest {

    public static Comparator comparableComparator = (Comparator) GlazedLists.comparableComparator();

    public KeyedCollectionForHashableValuesTest() {
        super(new KeyedCollectionForHashableValues(comparableComparator));
    }
}
