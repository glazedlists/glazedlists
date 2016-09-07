/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package com.publicobject.amazonbrowser;

import ca.odell.glazedlists.GlazedLists;

import java.text.DateFormat;

/**
 * Models an Item fetched from Amazon's ECS web service.
 *
 * @author James Lemieux
 */
public class Item implements Comparable<Item> {

    public static final DateFormat TABLE_DATE_FORMAT = DateFormat.getDateInstance(DateFormat.MEDIUM);

    private String asin;
    private String detailPageURL;
    private ItemAttributes itemAttributes;

    /**
     * The Amazon Standard Item Number uniquely identifying this item.
     */
    public String getASIN() { return asin; }
    public void setASIN(String asin) { this.asin = asin; }

    /**
     * The fully qualified URL to the Item's detail page at amazon.com
     */
    public String getDetailPageURL() { return detailPageURL; }
    public void setDetailPageURL(String detailPageURL) { this.detailPageURL = detailPageURL; }

    /**
     * Specific attributes describing the details of this Item.
     */
    public ItemAttributes getItemAttributes() { return itemAttributes; }
    public void setItemAttributes(ItemAttributes itemAttributes) { this.itemAttributes = itemAttributes; }

    /**
     * Items are ordered by their title by default.
     */
    @Override
    public int compareTo(Item o) {
        return GlazedLists.comparableComparator().compare(itemAttributes.getTitle(), o.itemAttributes.getTitle());
    }

    /** inheritDoc */
    @Override
    public String toString() {
        final String asin = getASIN() == null ? "" : (getASIN() + " ");
        final String title = itemAttributes.getTitle() == null ? "" : itemAttributes.getTitle();

        return asin + title;
    }
}