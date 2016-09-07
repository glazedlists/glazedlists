package ca.odell.glazedlists.impl.adt;

import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.impl.GlazedListsImpl;

import java.util.Comparator;

/**
 * @author jessewilson
 */
public class KeyedCollectionForComparableValuesTest extends AbstractKeyedCollectionTestBase {

    public static Comparator comparableComparator = GlazedLists.comparableComparator();

    public KeyedCollectionForComparableValuesTest() {
        super(GlazedListsImpl.keyedCollection(comparableComparator, comparableComparator));
    }
}