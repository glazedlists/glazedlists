/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package com.publicobject.amazonbrowser;

import ca.odell.glazedlists.TreeList;

import java.util.*;

/**
 * The ItemTableFormat specifies how Items are formatted as a hierarchy.
 * Specifically, it maps a single Item to a List of Items, each of which
 * represent a node in the hierarchy's path to the given <code>Item</code>.
 *
 * <p>At the moment, the hierarchy is 2 levels deep with the first level
 * representing the first letter of the Item's title, and the second level
 * representing the decade of the release date.
 *
 * @author James Lemieux
 */
public class ItemTreeFormat implements TreeList.Format<Item> {

    /**
     * A map from the title of each synthetic Item to the actual Item in the
     * hierarchy. This acts as a cache to prevent building redundant hierarchy
     * Items.
     */
    private final Map<Object, Item> syntheticItemCache = new HashMap<Object, Item>();

    /**
     * Returns a List of three Items. The first Item is a synthetic Item created
     * by extracting the first character from the title of the given
     * <code>item</code>. The second Item is a synthetic Item created by
     * normalizing the release date to the decade in which it occurred and then
     * using the decade string as the title of the Item. The third Item is the
     * <code>item</code> unmodified.
     */
    public List<Item> getPath(Item item) {
        // return a two-Item list representing the path to the given item
        final List<Item> path = new ArrayList<Item>(3);
        path.add(getOrCreateFirstTitleCharItem(item));
        path.add(getOrCreateDecadeOfReleaseItem(item));
        path.add(item);
        return path;
    }

    /**
     * A convenience method to lazily populate the {@link #syntheticItemCache}
     * and return a new Item with a title representing the decade of the release
     * date of the <code>item</code>.
     */
    private Item getOrCreateDecadeOfReleaseItem(Item item) {
        // extract the release year of the Item
        final Date releaseDate = item.getItemAttributes().getReleaseDate();

        if (releaseDate == null) {
            return getOrCreateItem("Unknown");
        } else {
            // normalize the release year to the decade of the release
            int releaseYear = 1900 + releaseDate.getYear();
            releaseYear /= 10;
            releaseYear *= 10;

            return getOrCreateItem(releaseYear + "s");
        }
    }

    /**
     * A convenience method to lazily populate the {@link #syntheticItemCache}
     * and return a new Item with a title representing the first character of
     * the title of the <code>item</code>.
     */
    private Item getOrCreateFirstTitleCharItem(Item item) {
        // extract the first char from the title of the item
        final String title = item.getItemAttributes().getTitle();
        return getOrCreateItem(title.length() == 0 ? "" : String.valueOf(title.charAt(0)));
    }

    /**
     * A convenience method to lazily populate the {@link #syntheticItemCache}
     * and return a new Item with the given <code>title</code>.
     */
    private Item getOrCreateItem(String title) {
        // check the cache
        Item pathItem = syntheticItemCache.get(title);

        // populate the cache if we missed
        if (pathItem == null) {
            pathItem = createSyntheticItem(title);
            syntheticItemCache.put(title, pathItem);
        }

        return pathItem;
    }

    /**
     * A convenience method to build and return a synthetic Item whose title is
     * the given <code>string</code>.
     */
    private static Item createSyntheticItem(String string) {
        final ItemAttributes itemAttributes = new ItemAttributes();
        itemAttributes.setTitle(string);

        final Item item = new Item();
        item.setItemAttributes(itemAttributes);

        return item;
    }
}