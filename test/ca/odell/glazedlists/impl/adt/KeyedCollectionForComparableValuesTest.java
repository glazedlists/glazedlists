package ca.odell.glazedlists.impl.adt;

import ca.odell.glazedlists.GlazedLists;

import java.util.Comparator;

/**
 * @author jessewilson
 */
public class KeyedCollectionForComparableValuesTest extends AbstractKeyedCollectionTest {

    public static Comparator comparableComparator = (Comparator) GlazedLists.comparableComparator();

    public KeyedCollectionForComparableValuesTest() {
        super(new KeyedCollectionForComparableValues<NamePosition, String>(comparableComparator, comparableComparator));
    }
}
