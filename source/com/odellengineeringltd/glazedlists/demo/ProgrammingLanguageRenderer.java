import com.odellengineeringltd.glazedlists.jtable.*;
import javax.swing.*;

class ProgrammingLanguageRenderer extends StyledDocumentRenderer {
    
    public ProgrammingLanguageRenderer() {
        super(true);
    }

    public void writeObject(JTable table, Object value, 
        boolean isSelected, boolean hasFocus, int row, int column) {
        
        ProgrammingLanguage language = (ProgrammingLanguage)value;
        
        append(language.getName(), strong);
        append(", ", strong);
        append(language.getYear(), strong);
        append("\n", plain);
        append(language.getDescription(), plain);
        
    }
}
