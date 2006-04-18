/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.swing;

// for being a JUnit test case
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.matchers.SimpleMatcherEditorListener;

import javax.swing.*;

/**
 * Test {@link TextComponentMatcherEditor}.
 *
 * @author <a href="mailto:jesse@odel.on.ca">Jesse Wilson</a>
 */
public class TextComponentMatcherEditorTest extends SwingTestCase {

    private SimpleMatcherEditorListener eventCounter;

    /**
     * Tests the user interface. This is a mandatory method in SwingTestCase classes.
     */
    public void testGui() {
        super.testGui();
    }

    public void guiSetUp() {
        eventCounter = new SimpleMatcherEditorListener();
    }

    public void guiTearDown() {
        eventCounter = null;
    }

    /**
     * Test that this thing works, even if our document is preloaded with data.
     */
    public void guiTestPrePopulated() {
        TextComponentMatcherEditor textMatcherEditor = null;
        eventCounter.assertNoEvents(0);

        // test the text field
        JTextField prePopulatedTextField = new JTextField();
        prePopulatedTextField.setText("ABC");
        textMatcherEditor = new TextComponentMatcherEditor(prePopulatedTextField, GlazedLists.toStringTextFilterator(), true);
        assertTrue(textMatcherEditor.getMatcher().matches("ABCDE"));
        assertFalse(textMatcherEditor.getMatcher().matches("BCDEF"));

        // test the document
        textMatcherEditor = new TextComponentMatcherEditor(prePopulatedTextField.getDocument(), GlazedLists.toStringTextFilterator());
        assertTrue(textMatcherEditor.getMatcher().matches("ABCDE"));
        assertFalse(textMatcherEditor.getMatcher().matches("BCDEF"));
    }

    /**
     * Test that this thing works when the document is changed.
     */
    public void guiTestChangeDocument() {
        TextComponentMatcherEditor textMatcherEditor = null;
        eventCounter.assertNoEvents(0);

        // test the text field
        JTextField textField = new JTextField();
        textMatcherEditor = new TextComponentMatcherEditor(textField, GlazedLists.toStringTextFilterator(), true);
        assertTrue(textMatcherEditor.getMatcher().matches("ABCDE"));
        textField.setText("DEF");
        assertFalse(textMatcherEditor.getMatcher().matches("ABCDE"));
        assertTrue(textMatcherEditor.getMatcher().matches("TONEDEF"));

        // test the document
        textField = new JTextField();
        textMatcherEditor = new TextComponentMatcherEditor(textField.getDocument(), GlazedLists.toStringTextFilterator());
        assertTrue(textMatcherEditor.getMatcher().matches("ABCDE"));
        textField.setText("HIJ");
        assertFalse(textMatcherEditor.getMatcher().matches("BCDEF"));
        assertTrue(textMatcherEditor.getMatcher().matches("HIJESSE"));
    }

    /**
     * Test that this thing works when live is turned off.
     */
    public void guiTestNonLive() {
        TextComponentMatcherEditor textMatcherEditor = null;
        eventCounter.assertNoEvents(0);

        // test the text field
        JTextField textField = new JTextField();
        textMatcherEditor = new TextComponentMatcherEditor(textField, GlazedLists.toStringTextFilterator(), false);
        assertTrue(textMatcherEditor.getMatcher().matches("ABCDE"));
        textField.setText("DEF");
        assertTrue(textMatcherEditor.getMatcher().matches("ABCDE"));
        assertTrue(textMatcherEditor.getMatcher().matches("TONEDEF"));
        textField.postActionEvent();
        assertFalse(textMatcherEditor.getMatcher().matches("ABCDE"));
        assertTrue(textMatcherEditor.getMatcher().matches("TONEDEF"));
    }

    /**
     * Test that this thing works with dispose.
     */
    public void guiTestDispose() {
        TextComponentMatcherEditor textMatcherEditor = null;
        eventCounter.assertNoEvents(0);

        // test both live and dead
        boolean[] liveOptions = { true, false };
        for(int b = 0; b < liveOptions.length; b++) {

            // test the text field
            JTextField textField = new JTextField();
            textMatcherEditor = new TextComponentMatcherEditor(textField, GlazedLists.toStringTextFilterator(), liveOptions[b]);
            textField.setText("DEF");
            textField.postActionEvent();
            assertTrue(textMatcherEditor.getMatcher().matches("TONEDEF"));
            assertFalse(textMatcherEditor.getMatcher().matches("STUPID"));

            textMatcherEditor.dispose();
            textField.setText("STU");
            textField.postActionEvent();
            assertTrue(textMatcherEditor.getMatcher().matches("TONEDEF"));
            assertFalse(textMatcherEditor.getMatcher().matches("STUPID"));
        }
    }
}
