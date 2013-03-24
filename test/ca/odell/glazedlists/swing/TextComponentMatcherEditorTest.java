/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.swing;

import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.TextFilterator;
import ca.odell.glazedlists.impl.filter.TextMatcher;
import ca.odell.glazedlists.matchers.SimpleMatcherEditorListener;

import javax.swing.JTextField;
import javax.swing.text.AbstractDocument;
import javax.swing.text.PlainDocument;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Test {@link TextComponentMatcherEditor}.
 *
 * @author <a href="mailto:jesse@swank.ca">Jesse Wilson</a>
 */
public class TextComponentMatcherEditorTest extends SwingTestCase {

    private SimpleMatcherEditorListener eventCounter;

    @Before
    public void setUp() {
        eventCounter = new SimpleMatcherEditorListener();
    }

    @After
    public void tearDown() {
        eventCounter = null;
    }

    @Test
    public void testConstructors() {
        JTextField textComponent = new JTextField();
        AbstractDocument document = (AbstractDocument) textComponent.getDocument();
        int initialDocumentListenerCount = document.getDocumentListeners().length;
        TextFilterator<String> textFilterator = GlazedLists.toStringTextFilterator();
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
    @Test
    public void testPrePopulated() {
        eventCounter.assertNoEvents(0);

        // test the text field
        JTextField prePopulatedTextField = new JTextField();
        prePopulatedTextField.setText("ABC");
        TextComponentMatcherEditor<String> textMatcherEditor = new TextComponentMatcherEditor<String>(prePopulatedTextField, GlazedLists.toStringTextFilterator(), true);
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
    @Test
    public void testChangeDocument() {
        eventCounter.assertNoEvents(0);

        // test the text field
        JTextField textField = new JTextField();
        TextComponentMatcherEditor<String> textMatcherEditor = new TextComponentMatcherEditor<String>(textField, GlazedLists.toStringTextFilterator(), true);
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
    @Test
    public void testNonLive() {
        eventCounter.assertNoEvents(0);

        // test the text field
        JTextField textField = new JTextField();
        TextComponentMatcherEditor<String> textMatcherEditor = new TextComponentMatcherEditor<String>(textField, GlazedLists.toStringTextFilterator(), false);
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
    @Test
    public void testDispose() {
        eventCounter.assertNoEvents(0);

        // test both live and dead
        boolean[] liveOptions = { true, false };
        for (int b = 0; b < liveOptions.length; b++) {

            // test the text field
            JTextField textField = new JTextField();
            TextComponentMatcherEditor<String> textMatcherEditor = new TextComponentMatcherEditor<String>(textField, GlazedLists.toStringTextFilterator(), liveOptions[b]);
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

    @Test
    public void testDocumentSwap() throws Exception {
        final JTextField textField = new JTextField();
        final AbstractDocument documentA = (AbstractDocument) textField.getDocument();
        documentA.insertString(0, "documentA", null);
        final AbstractDocument documentB = new PlainDocument();
        documentB.insertString(0, "documentB", null);

        final int originalDocumentAListenerCount = documentA.getDocumentListeners().length;
        final int originalDocumentBListenerCount = documentB.getDocumentListeners().length;

        // using the textField in a TextComponentMatcherEditor will install a DocumentListener
        TextComponentMatcherEditor<String> textMatcherEditor = new TextComponentMatcherEditor<String>(textField, GlazedLists.toStringTextFilterator(), true);
        assertEquals(originalDocumentAListenerCount+1, documentA.getDocumentListeners().length);
        assertEquals(originalDocumentBListenerCount, documentB.getDocumentListeners().length);
        TextMatcher textMatcher = (TextMatcher) textMatcherEditor.getMatcher();
        assertEquals("documentA", textMatcher.getSearchTerms()[0].getText());

        // replace DocumentA with DocumentB, which should update the filter with the text from DocumentB
        textField.setDocument(documentB);
        assertEquals(0, documentA.getDocumentListeners().length);
        assertEquals(originalDocumentBListenerCount+3, documentB.getDocumentListeners().length);
        textMatcher = (TextMatcher) textMatcherEditor.getMatcher();
        assertEquals("documentB", textMatcher.getSearchTerms()[0].getText());

        // changing the text in DocumentB should alter the filter
        documentB.replace(0, documentB.getLength(), "blah", null);
        textMatcher = (TextMatcher) textMatcherEditor.getMatcher();
        assertEquals("blah", textMatcher.getSearchTerms()[0].getText());
    }

    @Test
    public void testListeners() {
        final JTextField textField = new JTextField();
        final AbstractDocument document = (AbstractDocument) textField.getDocument();

        final int originalPropertyChangeListenerCount = textField.getPropertyChangeListeners().length;
        final int originalActionListenerCount = textField.getActionListeners().length;
        final int originalDocumentListenerCount = document.getDocumentListeners().length;

        // live
        TextComponentMatcherEditor<String> textMatcherEditor = new TextComponentMatcherEditor<String>(textField, GlazedLists.toStringTextFilterator(), true);
        assertEquals(originalPropertyChangeListenerCount+1, textField.getPropertyChangeListeners().length);
        assertEquals(originalActionListenerCount, textField.getActionListeners().length);
        assertEquals(originalDocumentListenerCount+1, document.getDocumentListeners().length);

        textMatcherEditor.dispose();
        assertEquals(originalPropertyChangeListenerCount, textField.getPropertyChangeListeners().length);
        assertEquals(originalActionListenerCount, textField.getActionListeners().length);
        assertEquals(originalDocumentListenerCount, document.getDocumentListeners().length);

        // not live
        textMatcherEditor = new TextComponentMatcherEditor<String>(textField, GlazedLists.toStringTextFilterator(), false);
        assertEquals(originalPropertyChangeListenerCount+1, textField.getPropertyChangeListeners().length);
        assertEquals(originalActionListenerCount+1, textField.getActionListeners().length);
        assertEquals(originalDocumentListenerCount, document.getDocumentListeners().length);

        textMatcherEditor.dispose();
        assertEquals(originalPropertyChangeListenerCount, textField.getPropertyChangeListeners().length);
        assertEquals(originalActionListenerCount, textField.getActionListeners().length);
        assertEquals(originalDocumentListenerCount, document.getDocumentListeners().length);
    }
}