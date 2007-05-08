package ca.odell.glazedlists.impl.adt;

import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.impl.GlazedListsImpl;

import java.util.Comparator;

/**
 * @author jessewilson
 */
public class KeyedCollectionForHashableValuesTest extends AbstractKeyedCollectionTest {

    public static Comparator comparableComparator = (Comparator) GlazedLists.comparableComparator();

    public KeyedCollectionForHashableValuesTest() {
        super(GlazedListsImpl.keyedCollection(comparableComparator));
    }
}