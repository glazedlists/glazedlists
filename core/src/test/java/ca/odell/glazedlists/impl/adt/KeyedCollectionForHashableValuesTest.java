package ca.odell.glazedlists.impl.adt;

import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.impl.GlazedListsImpl;

import java.util.Comparator;

/**
 * @author jessewilson
 */
public class KeyedCollectionForHashableValuesTest extends AbstractKeyedCollectionTestBase {

    public static Comparator comparableComparator = GlazedLists.comparableComparator();

    public KeyedCollectionForHashableValuesTest() {
        super(GlazedListsImpl.keyedCollection(comparableComparator));
    }
}