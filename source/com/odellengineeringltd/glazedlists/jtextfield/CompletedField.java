/**
 * Glazed Lists
 * http://opensource.odellengineeringltd.com/glazedlists/
 *
 * COPYRIGHT 2003 O'DELL ENGINEERING LTD.
 */
package com.odellengineeringltd.glazedlists.jtextfield;

// for being a specialized JTextField
import javax.swing.JTextField;
// for extending the PlainDocument with completion support 
import javax.swing.text.PlainDocument;
import javax.swing.text.Document;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
// for creating completed fields with list back ends
import com.odellengineeringltd.glazedlists.*;

/**
 * A special text field that auto-completes words that are entered into it.
 *
 * @author <a href="mailto:jesse@odel.on.ca">Jesse Wilson</a>
 */
public class CompletedField extends JTextField {

    /** used to lookup completions for a provided word */
    private StringCompleter completer = null;

    /**
     * Construct a completed field in the same way as a JTextField. This
     * adds itself as a DocumentListener to respond to changes for auto-completion.
     */
    public CompletedField() {
        super();
    }
    public CompletedField(String text) {
        super(text);
    }
    public CompletedField(String text, int columns) {
        super(text, columns);
    }
    public CompletedField(int columns) {
        super(columns);
    }
    /**
     * Creates a completed field that uses the values in the specified list
     * as possible completions.
     */
    public CompletedField(EventList source) {
        super();
        setStringCompleter(new ListStringCompleter(source));
    }
    /**
     * Creates a completed field that uses the values of the Strings in the
     * specified array as all possible completions.
     */
    public CompletedField(String[] completions) {
        super();
        setStringCompleter(new ArrayStringCompleter(completions));
    }
    
    /**
     * Sets the mechanism to be used to look up completions on prefixes.
     */
    public void setStringCompleter(StringCompleter completer) {
        this.completer = completer;
    }

    /**
     * The model for a completedField is always a CompletingDocument.
     */
    protected Document createDefaultModel() {
 	    return new CompletingDocument();
    }

    /**
     * Takes a prefix String and returns a completion of that String, or
     * the original String if no completion is found. Note that this returns
     * the prefix and postfix of a completed string. In effect, if the
     * supplied parameter was "Water" the return value could be "Waterloo".
     */
    private String getCompleted(String prefix) {
        if(prefix.length() == 0) return prefix;
        if(completer == null) return prefix;
        else return completer.getCompleted(prefix);
    }

    /**
     * Document class takes Insert events from the keyboard and figures out
     * if a completion can be made.
     *
     * @see <a href="http://java.sun.com/j2se/1.3/docs/api/javax/swing/JTextField.html">JTextField</a>
     */
    class CompletingDocument extends PlainDocument {
        
        public CompletingDocument() {
            super();
        }

        /**
         * Upon insertion, see if we can insert more by making a completion.
         *
         * We have three parts: the original text, the inserted text, and the
         * completion.
         */
        public void insertString(int offset, String inserted, AttributeSet attributeSet) 
 	        throws BadLocationException {
            
            if(inserted == null || inserted.length() == 0) {
 		        return;
 	        }

            // get the length of the field before the insert
            int originalLength = getLength(); 
            
            // ensure that this insert is at the end 
            if(offset != originalLength) {
                // forget trying to do a completion
                super.insertString(offset, inserted, attributeSet);
                return;
            }
            
            // so the insert is at the end, before text is the text after
            // this insertion but before any completion
            String beforeCompletion = getText(0, originalLength) + inserted;
            
            // get the length of the field after the insert
            int beforeCompletionLength = beforeCompletion.length();
            
            // find a completion
            String completed = getCompleted(beforeCompletion);
            String extendedInsert = completed.substring(originalLength);
            
            // do the insert of a bigger block
            super.insertString(offset, extendedInsert, attributeSet);
            
            // add the selection of the completion
            setSelectionStart(beforeCompletionLength);
            setSelectionEnd(completed.length());
        }
    }
}
