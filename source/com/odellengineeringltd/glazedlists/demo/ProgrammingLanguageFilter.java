import com.odellengineeringltd.glazedlists.*;
import java.awt.event.*;
import javax.swing.*;

class ProgrammingLanguageFilter extends AbstractFilterList implements ItemListener {

    JCheckBox objectOrientedCheck;
    JCheckBox virtualMachineCheck;
    
    public ProgrammingLanguageFilter(EventList source) {
        super(source);

        objectOrientedCheck = new JCheckBox("Object Oriented");
        objectOrientedCheck.addItemListener(this);
        
        virtualMachineCheck = new JCheckBox("Virtual Machine");
        virtualMachineCheck.addItemListener(this);

        handleFilterChanged();
    }
    
    public boolean filterMatches(Object element) {
        ProgrammingLanguage language = (ProgrammingLanguage)element;
        
        if(objectOrientedCheck.isSelected() && !language.isObjectOriented()) return false;
        if(virtualMachineCheck.isSelected() && !language.isVirtualMachine()) return false;
        
        return true;
    }
    
    public void itemStateChanged(ItemEvent e) {
        handleFilterChanged();
    }

    public JCheckBox getObjectOrientedCheckBox() {
        return objectOrientedCheck;
    }

    public JCheckBox getVirtualMachineCheckBox() {
        return virtualMachineCheck;
    }
}

