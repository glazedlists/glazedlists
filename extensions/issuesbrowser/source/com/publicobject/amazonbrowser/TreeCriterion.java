/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package com.publicobject.amazonbrowser;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.ObservableElementList;
import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.GlazedLists;

import javax.swing.event.SwingPropertyChangeSupport;
import java.beans.PropertyChangeListener;
import java.util.Date;
import java.util.Map;
import java.util.HashMap;

/**
 * This class represents one of the levels of hierarchy that exist in the
 * treetable of {@link Item} objects displayed in the AmazonBrowser. Each
 * active TreeCriterion contributes to the overall TreeFormat. Available
 * TreeCriterion objects include:
 *
 * <ul>
 *   <li>title
 *   <li>audience rating
 *   <li>price
 *   <li>release date
 * </ul>
 *
 * Note that users are expected to use the list of {@link @ALL_CRITERIA}, and
 * are never supposed to create their own TreeCriterion objects.
 *
 * @author James Lemieux
 */
public abstract class TreeCriterion {

    /** The List of all possible TreeCriterion objects. */
    public static final EventList<TreeCriterion> ALL_CRITERIA = new ObservableElementList<TreeCriterion>(new BasicEventList<TreeCriterion>(), GlazedLists.beanConnector(TreeCriterion.class));
    static {
        ALL_CRITERIA.add(new TitleCriterion());
        ALL_CRITERIA.add(new AudienceRatingCriterion());
        ALL_CRITERIA.add(new PriceCriterion());
        ALL_CRITERIA.add(new ReleaseDateCriterion());
    }

    private final SwingPropertyChangeSupport support = new SwingPropertyChangeSupport(this);

    /** A human readable name used to identify this TreeCriterion. */
    private final String name;

    /** A flag to indicate whether this TreeCriterion is active (and thus participating in the TreeFormat) or not. */
    private boolean active;

    /**
     * A map from the title of each synthetic Item to the actual Item in the
     * hierarchy. This acts as a cache to prevent building redundant synthetic
     * hierarchy Items.
     */
    private final Map<Object, Item> syntheticItemCache = new HashMap<Object, Item>();

    private TreeCriterion(String name, boolean active) {
        this.name = name;
        this.active = active;
    }

    public void addPropertyChangeListener(PropertyChangeListener l) {
        support.addPropertyChangeListener(l);
    }

    public void removePropertyChangeListener(PropertyChangeListener l) {
        support.removePropertyChangeListener(l);
    }

    /**
     * Returns a human readable name used to identify this TreeCriterion.
     */
    public String getName() {
        return name;
    }

    /**
     * Returns <tt>true</tt> if this TreeCriterion is actively participating in
     * the TreeFormat; <tt>false</tt> otherwise.
     */
    public boolean isActive() {
        return active;
    }

    /**
     * Activates or deactivates this TreeCriterion from participating in the
     * TreeFormat that governs what the tree hierarchies will resemble.
     */
    public void setActive(boolean active) {
        if (this.active == active) return;

        this.active = active;
        support.firePropertyChange("active", !active, active);
    }

    /**
     * Implementations of this method should produce a new Item object whose
     * title reflects some aspect of the given <code>item</code> and is
     * appropriate for use as a tree hierarchy node value.
     */
    public abstract Item getPathItem(Item item);

    /**
     * A convenience method to lazily populate the {@link #syntheticItemCache}
     * and return a new Item with the given <code>title</code>.
     */
    Item getOrCreateItem(String title) {
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

    private static final class TitleCriterion extends TreeCriterion {
        public TitleCriterion() {
            super("Title", true);
        }

        /**
         * Returns a synthetic Item whose title is the first Character
         * of the title of the given <code>item</code>.
         */
        public Item getPathItem(Item item) {
            // extract the first char from the title of the item
            final String title = item.getItemAttributes().getTitle();
            return getOrCreateItem(title.length() == 0 ? "" : String.valueOf(title.charAt(0)));
        }
    }

    private static final class AudienceRatingCriterion extends TreeCriterion {
        public AudienceRatingCriterion() {
            super("Audience Rating", true);
        }

        /**
         * Returns a synthetic Item whose title is the name of the
         * {@link AudienceRating}, if one exists, or <code>"Unknown"</code>
         * if an {@link AudienceRating} does not exist.
         */
        public Item getPathItem(Item item) {
            final AudienceRating rating = item.getItemAttributes().getAudienceRating();
            return getOrCreateItem(rating == null ? "Unknown" : rating.getDescription());
        }
    }

    /**
     * Returns a synthetic Item whose title is the name of the
     * {@link AudienceRating}, if one exists, or <code>"Unknown"</code>
     * if an {@link AudienceRating} does not exist.
     */
    private static final class PriceCriterion extends TreeCriterion {
        public PriceCriterion() {
            super("Price", false);
        }

        public Item getPathItem(Item item) {
            return getOrCreateItem("Not Yet Implemented");
        }
    }

    private static final class ReleaseDateCriterion extends TreeCriterion {
        public ReleaseDateCriterion() {
            super("Release Date", true);
        }

        /**
         * Returns a synthetic Item whose title is a String representing the
         * decade of the release date of the <code>item</code>.
         */
        public Item getPathItem(Item item) {
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
    }
}