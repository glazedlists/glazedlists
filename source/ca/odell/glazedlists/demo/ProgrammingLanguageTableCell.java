/**
 * Glazed Lists
 * http://glazedlists.dev.java.net/
 *
 * COPYRIGHT 2003 O'DELL ENGINEERING LTD.
 */
package ca.odell.glazedlists.demo;

import ca.odell.glazedlists.swing.*;
import javax.swing.*;

/**
 * @author <a href="mailto:jesse@odel.on.ca">Jesse Wilson</a>
 */
public class ProgrammingLanguageTableCell implements TableFormat {

    public int getColumnCount() {
        return 1;
    }

    public String getColumnName(int column) { 
        return "Programming Language";
    }
    
    public Object getColumnValue(Object baseObject, int column) {
        ProgrammingLanguage lang = (ProgrammingLanguage)baseObject;
        return lang;
    }
    
    public void configureTable(JTable table) {
        table.getColumnModel().getColumn(0).setCellRenderer(new ProgrammingLanguageRenderer());
    }

}
