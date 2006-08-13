/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package com.publicobject.amazonbrowser;

import ca.odell.glazedlists.TreeList;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

/**
 * The ItemTableFormat specifies how Items are formatted as a hierarchy.
 * Specifically, it maps a single Item to a List of Items, each of which
 * represent a node in the hierarchy's path to the given <code>Item</code>.
 *
 * <p>At the moment, the hierarchy is 1 level deep with the first level
 * representing the first letter of the Item's title.
 *
 * @author James Lemieux
 */
public class ItemTreeFormat implements TreeList.Format<Item> {

    /**
     * A map from each character to a synthetic Item in the hierarchy. This
     * acts as a cache to prevent building redundant hierarchy Items.
     */
    private final Map<Character, Item> syntheticItemCache = new HashMap<Character, Item>();

    /**
     * Returns a List of two Items. The first Item is a synthetic Item created
     * by extracting the first character from the title of the given
     * <code>item</code>. The second Item is the <code>item</code> unmodified.
     */
    public List<Item> getPath(Item item) {
        // return a two-Item list representing the path to the given item
        final List<Item> path = new ArrayList<Item>(2);
        path.add(getOrCreateSyntheticItem(item));
        path.add(item);
        return path;
    }

    /**
     * A convenience method to lazily populate the {@link #syntheticItemCache}
     * and return an appropriate synthetic Item for the given <code>item</code>.
     */
    private Item getOrCreateSyntheticItem(Item item) {
        // extract the first char from the title of the item
        final String title = item.getItemAttributes().getTitle();
        final Character firstChar = title.length() == 0 ? null : new Character(title.charAt(0));

        // check the cache
        Item pathItem = syntheticItemCache.get(firstChar);

        // populate the cache if we missed
        if (pathItem == null) {
            pathItem = createSyntheticItem(firstChar);
            syntheticItemCache.put(firstChar, pathItem);
        }

        return pathItem;
    }

    /**
     * A convenience method to build and return a synthetic Item whose Item is
     * the given <code>character</code>.
     */
    private static Item createSyntheticItem(Character character) {
        final ItemAttributes itemAttributes = new ItemAttributes();
        itemAttributes.setTitle(character == null ? "" : character.toString());

        final Item item = new Item();
        item.setItemAttributes(itemAttributes);

        return item;
    }
}