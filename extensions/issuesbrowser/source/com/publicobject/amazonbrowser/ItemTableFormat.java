/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package com.publicobject.amazonbrowser;

import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.gui.AdvancedTableFormat;
import ca.odell.glazedlists.gui.WritableTableFormat;

import java.util.Comparator;
import java.util.Date;

/**
 * The ItemTableFormat specifies how an item is displayed in a table.
 *
 * @author James Lemieux
 */
public class ItemTableFormat implements WritableTableFormat<Item>, AdvancedTableFormat<Item> {
    public int getColumnCount() {
        return 6;
    }

    public String getColumnName(int column) {
        switch (column) {
            case 0: return "ASIN";
            case 1: return "Price";
            case 2: return "Title";
            case 3: return "Rating";
            case 4: return "Director";
            case 5: return "Release Date";
            default: return null;
        }
    }

    public Class getColumnClass(int column) {
        switch (column) {
            case 3: return AudienceRating.class;
            case 5: return Date.class;
            default: return String.class;
        }
    }

    public Comparator getColumnComparator(int column) {
        return GlazedLists.comparableComparator();
    }

    public boolean isEditable(Item item, int column) {
        return column == 2;
    }

    public Item setColumnValue(Item item, Object editedValue, int column) {
        switch (column) {
            case 2: item.getItemAttributes().setTitle((String) editedValue); break;
            default: throw new IllegalStateException("column " + column + " is not editable");
        }

        return item;
    }

    public Object getColumnValue(Item item, int column) {
        if(item == null) return null;

        switch (column) {
            case 0: return item.getASIN();
            case 1: return item.getItemAttributes().getListPrice();
            case 2: return item.getItemAttributes().getTitle();
            case 3: return item.getItemAttributes().getAudienceRating();
            case 4: return item.getItemAttributes().getDirector();
            case 5: return item.getItemAttributes().getReleaseDate();
            default: return null;
        }
    }
}