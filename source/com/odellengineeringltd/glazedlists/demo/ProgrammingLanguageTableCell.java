/**
 * Glazed Lists
 * http://glazedlists.dev.java.net/
 *
 * COPYRIGHT 2003 O'DELL ENGINEERING LTD.
 */
package com.odellengineeringltd.glazedlists.demo;

import com.odellengineeringltd.glazedlists.swing.*;
import javax.swing.*;

/**
 * @author <a href="mailto:jesse@odel.on.ca">Jesse Wilson</a>
 */
public class ProgrammingLanguageTableCell implements TableFormat {

    public int getFieldCount() {
        return 1;
    }

    public String getFieldName(int column) { 
        return "Programming Language";
    }
    
    public Object getFieldValue(Object baseObject, int column) {
        ProgrammingLanguage lang = (ProgrammingLanguage)baseObject;
        return lang;
    }
    
    public void configureTable(JTable table) {
        table.getColumnModel().getColumn(0).setCellRenderer(new ProgrammingLanguageRenderer());
    }

}
