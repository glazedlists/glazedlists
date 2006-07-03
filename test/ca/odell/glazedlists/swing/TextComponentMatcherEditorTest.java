/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.swing;

import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.TextFilterator;
import ca.odell.glazedlists.matchers.SimpleMatcherEditorListener;

import javax.swing.*;
import javax.swing.text.AbstractDocument;

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

    public void guiTestConstructors() {
        JTextField textComponent = new JTextField();
        AbstractDocument document = (AbstractDocument) textComponent.getDocument();
        int initialDocumentListenerCount = document.getDocumentListeners().length;
        TextFilterator textFilterator = GlazedLists.toStringTextFilterator();
        TextComponentMatcherEditor<String> tcme;

        tcme = new TextComponentMatcherEditor<String>(textComponent, textFilterator);
        assertEquals(0, textComponent.getActionListeners().length);
        assertEquals(initialDocumentListenerCount+1, document.getDocumentListeners().length);
        assertSame(textFilterator, tcme.getFilterator());
        assertTrue(tcme.isLive());

        tcme.dispose();
        tcme = new TextComponentMatcherEditor<String>(textComponent, textFilterator, true);
        assertEquals(0, textComponent.getActionListeners().length);
        assertEquals(initialDocumentListenerCount+1, document.getDocumentListeners().length);
        assertSame(textFilterator, tcme.getFilterator());
        assertTrue(tcme.isLive());

        tcme.dispose();
        tcme = new TextComponentMatcherEditor<String>(textComponent, textFilterator, false);
        assertEquals(1, textComponent.getActionListeners().length);
        assertEquals(initialDocumentListenerCount, document.getDocumentListeners().length);
        assertSame(textFilterator, tcme.getFilterator());
        assertFalse(tcme.isLive());

        tcme.dispose();
        tcme = new TextComponentMatcherEditor<String>(document, textFilterator);
        assertEquals(0, textComponent.getActionListeners().length);
        assertEquals(initialDocumentListenerCount+1, document.getDocumentListeners().length);
        assertSame(textFilterator, tcme.getFilterator());
        assertTrue(tcme.isLive());

        tcme.dispose();
        assertEquals(0, textComponent.getActionListeners().length);
        assertEquals(initialDocumentListenerCount, document.getDocumentListeners().length);
    }

    /**
     * Test that this thing works, even if our document is preloaded with data.
     */
    public void guiTestPrePopulated() {
        TextComponentMatcherEditor<String> textMatcherEditor = null;
        eventCounter.assertNoEvents(0);

        // test the text field
        JTextField prePopulatedTextField = new JTextField();
        prePopulatedTextField.setText("ABC");
        textMatcherEditor = new TextComponentMatcherEditor<String>(prePopulatedTextField, GlazedLists.toStringTextFilterator(), true);
        assertTrue(textMatcherEditor.getMatcher().matches("ABCDE"));
        assertFalse(textMatcherEditor.getMatcher().matches("BCDEF"));

        // test the document
        textMatcherEditor = new TextComponentMatcherEditor<String>(prePopulatedTextField.getDocument(), GlazedLists.toStringTextFilterator());
        assertTrue(textMatcherEditor.getMatcher().matches("ABCDE"));
        assertFalse(textMatcherEditor.getMatcher().matches("BCDEF"));
    }

    /**
     * Test that this thing works when the document is changed.
     */
    public void guiTestChangeDocument() {
        TextComponentMatcherEditor<String> textMatcherEditor = null;
        eventCounter.assertNoEvents(0);

        // test the text field
        JTextField textField = new JTextField();
        textMatcherEditor = new TextComponentMatcherEditor<String>(textField, GlazedLists.toStringTextFilterator(), true);
        assertTrue(textMatcherEditor.getMatcher().matches("ABCDE"));
        textField.setText("DEF");
        assertFalse(textMatcherEditor.getMatcher().matches("ABCDE"));
        assertTrue(textMatcherEditor.getMatcher().matches("TONEDEF"));

        // test the document
        textField = new JTextField();
        textMatcherEditor = new TextComponentMatcherEditor<String>(textField.getDocument(), GlazedLists.toStringTextFilterator());
        assertTrue(textMatcherEditor.getMatcher().matches("ABCDE"));
        textField.setText("HIJ");
        assertFalse(textMatcherEditor.getMatcher().matches("BCDEF"));
        assertTrue(textMatcherEditor.getMatcher().matches("HIJESSE"));
    }

    /**
     * Test that this thing works when live is turned off.
     */
    public void guiTestNonLive() {
        TextComponentMatcherEditor<String> textMatcherEditor = null;
        eventCounter.assertNoEvents(0);

        // test the text field
        JTextField textField = new JTextField();
        textMatcherEditor = new TextComponentMatcherEditor<String>(textField, GlazedLists.toStringTextFilterator(), false);
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
        TextComponentMatcherEditor<String> textMatcherEditor = null;
        eventCounter.assertNoEvents(0);

        // test both live and dead
        boolean[] liveOptions = { true, false };
        for(int b = 0; b < liveOptions.length; b++) {

            // test the text field
            JTextField textField = new JTextField();
            textMatcherEditor = new TextComponentMatcherEditor<String>(textField, GlazedLists.toStringTextFilterator(), liveOptions[b]);
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