/**
 * Glazed Lists
 * http://glazedlists.dev.java.net/
 *
 * COPYRIGHT 2003 O'DELL ENGINEERING LTD.
 */
package ca.odell.glazedlists.migrationkit;

// for showing caution with a border
import javax.swing.border.*;
import javax.swing.BorderFactory;
import java.awt.Color;
import javax.swing.UIManager;
import javax.swing.JComponent;

/**
 * Adds a border to a component when set in caution mode, and space in normal
 * mode.
 *
 * @author <a href="mailto:jesse@odel.on.ca">Jesse Wilson</a>
 */
public class ComponentCaution {

    /** the default text field border */
    private Border defaultBorder;
    
    /** the component to apply borders to */
    private JComponent component;
    
    /** the stateful borders: defaults */
    private static final int BORDER_THICKNESS = 2;
    private static final Color CAUTION_BORDER_COLOR = Color.RED;
    private static final Color EMPTY_BORDER_COLOR = UIManager.getColor("Panel.background");
    private static Border DEFAULT_CAUTION_BORDER = BorderFactory.createMatteBorder(BORDER_THICKNESS, BORDER_THICKNESS, BORDER_THICKNESS, BORDER_THICKNESS, CAUTION_BORDER_COLOR); 
    private static Border DEFAULT_PLAIN_BORDER = BorderFactory.createMatteBorder(BORDER_THICKNESS, BORDER_THICKNESS, BORDER_THICKNESS, BORDER_THICKNESS, EMPTY_BORDER_COLOR);
    
    /** the border to use is a compound border, using the initial border and a stateful border */
    private Border cautionBorder;
    private Border plainBorder;
    
    /**
     * Creates a caution component for the specified component using the
     * default border color and thickness.
     */
    public ComponentCaution(JComponent component) {
        this.component = component;
        defaultBorder = component.getBorder();
        cautionBorder = BorderFactory.createCompoundBorder(DEFAULT_CAUTION_BORDER, defaultBorder);
        plainBorder = BorderFactory.createCompoundBorder(DEFAULT_PLAIN_BORDER, defaultBorder);
        component.setBorder(plainBorder);
    }
    
    /**
     * Sets the text field to use the caution border.
     */
    public void setCaution(boolean caution) {
        if(caution) component.setBorder(cautionBorder);
        else component.setBorder(plainBorder);
    }
}
