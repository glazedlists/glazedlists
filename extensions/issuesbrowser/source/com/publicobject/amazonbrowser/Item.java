/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package com.publicobject.amazonbrowser;

import ca.odell.glazedlists.jfreechart.ValueSegment;
import ca.odell.glazedlists.jfreechart.DefaultValueSegment;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

/**
 * Models an Item fetched from Amazon's ECS web service.
 *
 * @author James Lemieux
 */
public class Item {

    private String asin = null;
    private String detailPageURL = null;
    private ItemAttributes itemAttributes = null;

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
}