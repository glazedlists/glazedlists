/**
 * Glazed Lists
 * http://glazedlists.dev.java.net/
 *
 * COPYRIGHT 2003 O'DELL ENGINEERING LTD.
 */
package ca.odell.glazedlists.migrationkit;

// Swing toolkit stuff for displaying widgets
import javax.swing.*;
// for including icons in a JComboBox
import java.net.URL;
import java.awt.Component;
// for building a map from status strings to labels
import java.util.HashMap;
import java.util.Map;

/**
 * To render an image and icon in a JComboBox, use a label as my renderer
 * as seen in The Java Tutorial.
 *
 * This class has been modified to take a Map from status Strings to
 * JLabels. It returns the JLabel for the specified String, or the default
 * JLabel if that String is not found.
 *
 * @deprecated This class will not be available in future releases of Glazed Lists.
 *      It exists to help users migrate between Glazed Lists < 0.8 and Glazed Lists >= 0.9.
 *      Users should visit the new Renderpack project, http://renderpack.dev.java.net.
 *
 * @see <a href="http://java.sun.com/docs/books/tutorial/uiswing/components/combobox.html">Combo Boxes</a>
 *
 * @author <a href="mailto:jesse@odel.on.ca">Jesse Wilson</a>
 */
public class IconComboBoxRenderer extends JLabel implements ListCellRenderer {

    /** A map of the components for each label */
    private Map labelsForValues;
    /** The label to use when the status is not found */
    private JLabel defaultLabel;
        
    /**
     * Create a new ComboBoxRenderer that renders using the specified map
     * of values to labels and default label.
     */
    public IconComboBoxRenderer(Map labelsForValues, JLabel defaultLabel) {
        this.labelsForValues = labelsForValues;
        this.defaultLabel = defaultLabel;
    }

    /**
     * Create a new ComboBoxRenderer that renders using the specified pair
     * of arrays, where the first array contains values and the second array
     * contains labels, and values[i] is labelled with labels[i] for all i.
     */
    public IconComboBoxRenderer(Object[] values, JLabel[] labels, JLabel defaultLabel) {
        // construct a map from values to labels
        labelsForValues = new HashMap();
        for(int v = 0; v < values.length; v++) {
            labelsForValues.put(values[v], labels[v]);
        }
        this.defaultLabel = defaultLabel;
    }

    /**
     * This method finds the image and text corresponding
     * to the selected value and returns the label, set up
     * to display the text and image.
     */
    public Component getListCellRendererComponent(JList list, Object value,
        int index, boolean isSelected, boolean cellHasFocus) {
            
        JLabel label = (JLabel)labelsForValues.get(value);
        if(label == null) {
            label = defaultLabel;
            label.setText(""+value);
        }
            
        if (isSelected) {
            label.setBackground(list.getSelectionBackground());
            label.setForeground(list.getSelectionForeground());
        } else {
            label.setBackground(list.getBackground());
            label.setForeground(list.getForeground());
        }
        return label;
    }
}

