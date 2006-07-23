package com.publicobject.amazonbrowser;

import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.SeparatorList;
import ca.odell.glazedlists.gui.AdvancedTableFormat;
import ca.odell.glazedlists.gui.WritableTableFormat;

import java.util.Comparator;
import java.util.Date;

import com.publicobject.issuesbrowser.Priority;
import com.publicobject.issuesbrowser.Issue;

/**
 * The ItemTableFormat specifies how an item is displayed in a table.
 *
 * @author James Lemieux
 */
public class ItemTableFormat implements WritableTableFormat, AdvancedTableFormat {

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

    public boolean isEditable(Object baseObject, int column) {
        return false;
    }

    public Object setColumnValue(Object baseObject, Object editedValue, int column) {
        return null;
    }

    public Object getColumnValue(Object baseObject, int column) {
        if (baseObject == null) return null;

        final Item item = (Item) baseObject;
        final ListPrice listPrice = item.getItemAttributes().getListPrice();

        switch (column) {
            case 0: return item.getASIN();
            case 1: return listPrice == null ? null : listPrice.getFormattedPrice();
            case 2: return item.getItemAttributes().getTitle();
            case 3: return item.getItemAttributes().getAudienceRating();
            case 4: return item.getItemAttributes().getDirector();
            case 5: return item.getItemAttributes().getReleaseDate();
            default: return null;
        }
    }
}