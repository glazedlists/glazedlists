/* Glazed Lists                                                 (c) 2003-2014 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.javafx;

import ca.odell.glazedlists.TextFilterator;
import ca.odell.glazedlists.matchers.TextMatcherEditor;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputControl;

import javax.swing.text.Document;

/**
 * A MatcherEditor that matches Objects that contain the filter text located
 * within the text-property of a {@link TextInputControl}.
 * This matcher is fully concrete and is expected to be used by JavaFx applications.
 *
 * <p>The {@link TextInputControlMatcherEditor} constructors require that a {@link TextInputControl}
 * and {@link TextFilterator} be specified.
 *
 * <p>The MatcherEditor registers itself as a {@link ChangeListener} on the
 * text-property of the text control for live-filtering. For non-live filtering, the text control must be of type {@link TextField},
 * because the matcher editor then registers as {@link EventHandler} on the action-property of the text field.
 *
 * If this MatcherEditor must be garbage collected before the underlying
 * {@link TextInputControl}, the listeners can be unregistered by calling {@link #dispose()}.
 *
 * @author Holger Brands
 */
public class TextInputControlMatcherEditor<E> extends TextMatcherEditor<E> {

    /** the text component. */
    private final TextInputControl textControl;

    /** whether we're listening to each keystroke */
    private boolean live;

    /** The listener attached to the text control. */
    private final FilterHandler filterHandler = new FilterHandler();

    /**
     * Creates a TextMatcherEditor bound to the {@link TextInputControl} with the given
     * <code>textFilterator</code>.
     *
     * @param textControl the text component that is the source of text filter values
     * @param textFilterator an object capable of producing Strings from the
     *      objects being filtered. If <code>textFilterator</code> is
     *      <code>null</code> then all filtered objects are expected to
     *      implement {@link ca.odell.glazedlists.TextFilterable}.
     */
    public TextInputControlMatcherEditor(TextInputControl textControl, TextFilterator<? super E> textFilterator) {
        this(textControl, textFilterator, true);
    }

    /**
     * Creates a TextMatcherEditor bound to the {@link TextInputControl} with the given
     * <code>textFilterator</code>.
     *
     * @param textControl the text component that is the source of text filter values
     * @param textFilterator an object capable of producing Strings from the
     *      objects being filtered. If <code>textFilterator</code> is
     *      <code>null</code> then all filtered objects are expected to
     *      implement {@link ca.odell.glazedlists.TextFilterable}.
     * @param live <code>true</code> to filter by a change to the text property of the textinput control
     *      . Note that non-live filtering is only supported if <code>textControl</code> is a {@link TextField}.
     * @throws IllegalArgumentException if the <code>textControl</code>
     *      is not a {@link TextField} and non-live filtering is specified.
     */
    public TextInputControlMatcherEditor(TextInputControl textControl, TextFilterator<? super E> textFilterator, boolean live) {
        super(textFilterator);
        if (textControl == null) {
            throw new IllegalArgumentException("TextInputControl is undefined");
        }
        this.textControl = textControl;
        this.live = live;
        registerListeners(live);
        // if the text control is non-empty to begin with!
        refilter();
    }

    /**
     * Whether filtering occurs by the keystroke or not.
     */
    public boolean isLive() {
        return live;
    }

    /**
     * Toggle between filtering by the keystroke and not.
     *
     * @param live <code>true</code> to filter by keystroke (change of the text property) or <code>false</code>
     *      to filter only when ENTER is pressed within the {@link TextField} (action event).
     *      Note that non-live filtering is only supported if <code>textControl</code> is a {@link TextField}.
     */
    public void setLive(boolean live) {
        if (live == this.live) {
            return;
        }
        deregisterListeners(this.live);
        this.live = live;
        registerListeners(this.live);
    }


    /**
     * Listen live or on action event.
     */
    private void registerListeners(boolean live) {
        if (live) {
            textControl.textProperty().addListener(filterHandler);
        } else {
            if (!(textControl instanceof TextField)) {
                throw new IllegalArgumentException("Non-live filtering supported only for TextField (argument class " + textControl.getClass().getName() + ")");
            }
            TextField textField = (TextField) textControl;
            textField.onActionProperty().set(filterHandler);
        }
    }

    /**
     * Stop listening.
     */
    private void deregisterListeners(boolean live) {
        if (live) {
            textControl.textProperty().removeListener(filterHandler);
        } else {
            TextField textField = (TextField) textControl;
            textField.onActionProperty().set(null);
        }
    }

    /**
     * @return the test input control this matcher editor is connected to.
     */
    public TextInputControl getTextControl() {
        return textControl;
    }

    /**
     * A cleanup method which stops this MatcherEditor from listening to
     * changes on the underlying {@link Document}, thus freeing the
     * MatcherEditor or Document to be garbage collected.
     */
    public void dispose() {
        deregisterListeners(live);
    }

    /**
     * Update the filter text from the contents of the text control.
     */
    private void refilter() {
        final int mode = getMode();
        final String text = textControl.getText();
        final String[] filters;

        // in CONTAINS mode we treat the string as whitespace delimited
        if (mode == CONTAINS) {
            filters = text.split("[ \t]");
        } else if (mode == STARTS_WITH || mode == REGULAR_EXPRESSION || mode == EXACT) {
            filters = new String[] {text};
        } else {
            throw new IllegalStateException("Unknown mode: " + mode);
        }
        setFilterText(filters);
    }

    /**
     * This class responds to any change in the text of the control or to an
     * action event by setting the filter text of this TextMatcherEditor to the
     * contents of the text control.
     */
    private class FilterHandler implements ChangeListener<String>, EventHandler<ActionEvent>{

        @Override
        public void changed(ObservableValue<? extends String> paramObservableValue, String paramT1, String paramT2) {
            refilter();
        }

        @Override
        public void handle(ActionEvent paramT) {
            refilter();
        }
    }
}
