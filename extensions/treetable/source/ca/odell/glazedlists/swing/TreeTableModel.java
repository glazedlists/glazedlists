package ca.odell.glazedlists.swing;

import javax.swing.table.TableModel;

public interface TreeTableModel extends TableModel {

    public int getHeight(int rowIndex);
}