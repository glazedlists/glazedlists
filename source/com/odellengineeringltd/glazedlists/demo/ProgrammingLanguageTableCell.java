import com.odellengineeringltd.glazedlists.jtable.*;
import javax.swing.*;

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
