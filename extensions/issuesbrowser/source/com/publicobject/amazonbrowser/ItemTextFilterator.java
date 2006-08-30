/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package com.publicobject.amazonbrowser;

import ca.odell.glazedlists.TextFilterator;

import java.util.List;
import java.util.Date;

/**
 * Provide text filter strings for {@link Item} objects.
 *
 * @author James Lemieux
 */
public class ItemTextFilterator implements TextFilterator<Item> {

    public void getFilterStrings(List<String> baseList, Item element) {
        final ItemAttributes attribs = element.getItemAttributes();
        final ListPrice listPrice = attribs.getListPrice();
        final Date releaseDate = attribs.getReleaseDate();
        final AudienceRating audienceRating = attribs.getAudienceRating();

        if (listPrice != null)
            baseList.add(listPrice.getFormattedPrice());

        if (audienceRating != null)
            baseList.add(audienceRating.toString());

        if (releaseDate != null)
            baseList.add(Item.TABLE_DATE_FORMAT.format(releaseDate));

        baseList.add(attribs.getTitle());
        baseList.add(attribs.getDirector());
    }
}