package com.publicobject.amazonbrowser;

import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.TreeList;
import ca.odell.glazedlists.gui.AdvancedTableFormat;
import ca.odell.glazedlists.gui.WritableTableFormat;

import java.util.Comparator;
import java.util.Date;
import java.util.List;

/**
 * The ItemTableFormat specifies how an item is displayed in a table.
 *
 * @author James Lemieux
 */
public class ItemTableFormat implements WritableTableFormat<TreeList.TreeElement<Item>>, AdvancedTableFormat<TreeList.TreeElement<Item>> {
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

    public boolean isEditable(TreeList.TreeElement<Item> baseObject, int column) {
        return column == 2;
    }

    public TreeList.TreeElement<Item> setColumnValue(TreeList.TreeElement<Item> baseObject, Object editedValue, int column) {
        final List<Item> treePath = baseObject.path();
        final Item item = treePath.get(treePath.size()-1);

        switch (column) {
            case 2: item.getItemAttributes().setTitle((String) editedValue); break;
            default: throw new IllegalStateException("column " + column + " is not editable");
        }

        return baseObject;
    }

    public Object getColumnValue(TreeList.TreeElement<Item> baseObject, int column) {
        if (baseObject == null) return null;

        final List<Item> treePath = baseObject.path();
        final Item item = treePath.get(treePath.size()-1);

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