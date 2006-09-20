/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.swing;

import ca.odell.glazedlists.*;
import ca.odell.glazedlists.gui.TableFormat;
import ca.odell.glazedlists.impl.GlazedListsImpl;
import ca.odell.glazedlists.impl.filter.TextMatcher;
import ca.odell.glazedlists.impl.swing.ComboBoxPopupLocationFix;
import ca.odell.glazedlists.matchers.Matcher;
import ca.odell.glazedlists.matchers.Matchers;
import ca.odell.glazedlists.matchers.TextMatcherEditor;

import javax.swing.*;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.plaf.UIResource;
import javax.swing.plaf.basic.ComboPopup;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.reflect.Method;
import java.text.Format;
import java.text.ParsePosition;
import java.util.Comparator;
import java.util.List;

/**
 * This class {@link #install}s support for filtering and autocompletion into
 * a standard {@link JComboBox}. It also acts as a factory class for
 * {@link #createTableCellEditor creating autocompleting table cell editors}.
 *
 * <p>All autocompletion behaviour provided is meant to mimic the functionality
 * of the Firefox address field. To be explicit, the following is a list of
 * expected behaviours which are installed:
 *
 * <p><strong>Typing into the ComboBox Editor</strong>
 * <ol>
 *   <li> typing any value into the editor when the popup is invisible causes
 *        the popup to appear and its contents to be filtered according to the
 *        editor's text. It also autocompletes to the first item that is
 *        prefixed with the editor's text and selects that item within the popup.
 *   <li> typing any value into the editor when the popup is visible causes
 *        the popup to be refiltered according to the editor's text and
 *        reselects an appropriate autocompletion item
 *   <li> typing the down or up arrow keys in the editor when the popup is
 *        invisible causes the popup to appear and its contents to be filtered
 *        according to the editor's text. It also autocompletes to the first
 *        item that is prefixed with the editor's text and selects that item
 *        within the popup.
 *   <li> typing the up arrow key when the popup is visible and the selected
 *        element is the first element causes the autocompletion to be cleared
 *        and the popup's selection to be removed
 *   <li> typing the up arrow key when no selection exists causes the last
 *        element of the popup to become selected and used for autocompletion
 *   <li> typing the down arrow key when the popup is visible and the selected
 *        element is the last element causes the autocompletion to be cleared
 *        and the popup's selection to be removed
 *   <li> typing the down arrow key when no selection exists causes the first
 *        element of the popup to become selected and used for autocompletion
 * </ol>
 *
 * <strong>Clicking on the Arrow Button</strong>
 * <ol>
 *   <li> clicking the arrow button when the popup is invisible causes the
 *        popup to appear and its contents to be shown unfiltered
 *   <li> clicking the arrow button when the popup is visible causes the popup
 *        to be hidden
 * </ol>
 *
 * <strong>Sizing the ComboBox Popup</strong>
 * <ol>
 *   <li> the popup is always <strong>at least</strong> as wide as the
 *        autocompleting {@link JComboBox}, but may be wider to accomodate a
 *        {@link JComboBox#getPrototypeDisplayValue() prototype display value}
 *        if a non-null prototype display value exists
 *   <li> as items are filtered in the ComboBoxModel, the popup height is
 *        adjusted to display between 0 and {@link JComboBox#getMaximumRowCount()}
 *        rows before scrolling the popup
 * </ol>
 *
 * <strong>JComboBox ActionEvents</strong>
 * <p>A single {@link ActionEvent} is fired from the JComboBox in these situations:
 * <ol>
 *   <li> the user hits the enter key
 *   <li> the selected item within the popup is changed (which can happen due
 *        to a mouse click, a change in the autocompletion term, or using the
 *        arrow keys)
 *   <li> the JComboBox loses focus and contains a value that does not appear
 *        in the ComboBoxModel
 * </ol>
 *
 * <strong>Null Values in the ComboBoxModel</strong>
 * <p><code>null</code> values located in the ComboBoxModel are considered
 * identical to empty Strings ("") for the purposes of locating autocompletion
 * terms.<br><br></p>
 *
 * <strong>ComboBoxEditor Focus</strong>
 * <p>When the ComboBoxEditor gains focus it selects the text it contains if
 * {@link #getSelectsTextOnFocusGain()} returns <tt>true</tt>; otherwise it
 * does nothing.<br><br></p>
 *
 * <strong>Extracting String Values</strong>
 * <p>Each value in the ComboBoxModel must be converted to a String for many
 * reasons: filtering, setting into the ComboBoxEditor, displaying in the
 * renderer, etc. By default, JComboBox relies on {@link Object#toString()}
 * to map elements to their String equivalents. Sometimes, however, toString()
 * is not a reliable or desirable mechanism to use. To deal with this problem,
 * AutoCompleteSupport provides an install method that takes a {@link Format}
 * object which is used to do all converting back and forth between Strings and
 * ComboBoxModel objects.</p>
 *
 * <p>In order to achieve all of the autocompletion and filtering behaviour,
 * the following occurs when {@link #install} is called:
 *
 * <ul>
 *   <li> the JComboBox will be made editable
 *   <li> the JComboBox will have a custom ComboBoxModel installed on it
 *        containing the given items
 *   <li> the ComboBoxEditor will be wrapped with functionality and set back
 *        into the JComboBox as the editor
 *   <li> the JTextField which is the editor component for the JComboBox
 *        will have a DocumentFilter installed on its backing Document
 * </ul>
 *
 * The strategy of this support class is to alter all of the objects which
 * influence the behaviour of the JComboBox in one single context. With that
 * achieved, it greatly reduces the cross-functional communication required to
 * customize the behaviour of JComboBox for filtering and autocompletion.
 *
 * <p><strong><font color="#FF0000">Warning:</font></strong> This class must be
 * mutated from the Swing Event Dispatch Thread. Failure to do so will result in
 * an {@link IllegalStateException} thrown from any one of:
 *
 * <ul>
 *   <li> {@link #install(JComboBox, EventList)}
 *   <li> {@link #install(JComboBox, EventList, TextFilterator)}
 *   <li> {@link #install(JComboBox, EventList, TextFilterator, Format)}
 *   <li> {@link #uninstall()}
 *   <li> {@link #setCorrectsCase(boolean)}
 *   <li> {@link #setStrict(boolean)}
 *   <li> {@link #setSelectsTextOnFocusGain(boolean)}
 *   <li> {@link #setFilterMode(int)}
 * </ul>
 *
 * @author James Lemieux
 */
public final class AutoCompleteSupport<E> {

    private static final ParsePosition PARSE_POSITION = new ParsePosition(0);
    private static final Class[] VALUE_OF_SIGNATURE = {String.class};

    //
    // These member variables control behaviour of the autocompletion support
    //

    /**
     * <tt>true</tt> if user-specified text is converted into the same case as
     * the autocompletion term. <tt>false</tt> will leave user specified text
     * unaltered.
     */
    private boolean correctsCase = true;

    /**
     * <tt>true</tt> if the user can specify values that do not appear in the
     * ComboBoxModel; <tt>false</tt> otherwise.
     */
    private boolean strict = false;

    /**
     * <tt>true</tt> if the text in the combobox editor is selected when the
     * editor gains focus; <tt>false</tt> otherwise.
     */
    private boolean selectsTextOnFocusGain = true;

    //
    // These are member variables for convenience
    //

    /** The comboBox being decorated with autocomplete functionality. */
    private JComboBox comboBox;

    /** The popup menu of the decorated comboBox. */
    private JPopupMenu popupMenu;

    /** The popup that wraps the popupMenu of the decorated comboBox. */
    private ComboPopup popup;

    /** The arrow button that invokes the popup. */
    private JButton arrowButton;

    /** The model backing the comboBox. */
    private final AutoCompleteComboBoxModel comboBoxModel;

    /** The custom renderer installed on the comboBox or <code>null</code> if one is not required. */
    private final ListCellRenderer renderer;

    /** The EventList which holds the items present in the comboBoxModel. */
    private final EventList<E> items;

    /** The FilterList which filters the items present in the comboBoxModel. */
    private final FilterList<E> filteredItems;

    /** The Format capable of producing Strings from ComboBoxModel elements and vice versa. */
    private final Format format;

    /** The MatcherEditor driving the FilterList behind the comboBoxModel. */
    private final TextMatcherEditor<E> filterMatcherEditor;

    /**
     * The custom ComboBoxEditor that does NOT assume that the text value can
     * be computed using Object.toString(). (i.e. the default ComboBoxEditor
     * *does* assume that, but we decorate it and remove that assumption)
     */
    private FormatComboBoxEditor comboBoxEditor;

    /** The textfield which acts as the editor of the comboBox. */
    private JTextField comboBoxEditorComponent;

    /** The Document backing the comboBoxEditorComponent. */
    private AbstractDocument document;

    /** A DocumentFilter that controls edits to the document. */
    private final AutoCompleteFilter documentFilter = new AutoCompleteFilter();

    /** The last prefix specified by the user. */
    private String prefix = "";

    /** The Matcher that decides if a ComboBoxModel element is filtered out. */
    private Matcher<String> filterMatcher = Matchers.trueMatcher();

    /** Controls the selection behavior of the JComboBox when it is used in a JTable DefaultCellEditor. */
    private boolean isTableCellEditor;

    //
    // These listeners work together to enforce different aspects of the autocompletion behaviour
    //

    /**
     * The MouseListener which is installed on the {@link #arrowButton} and
     * is responsible for clearing the filter and then showing / hiding the
     * {@link #popup}.
     */
    private ArrowButtonMouseListener arrowButtonMouseListener;

    /**
     * A listener which reacts to changes in the ComboBoxModel by
     * resizing the popup appropriately to accomodate the new data.
     */
    private final ListDataListener listDataHandler = new ListDataHandler();

    /**
     * We ensure the popup menu is sized correctly each time it is shown.
     * Namely, we respect the prototype display value of the combo box, if
     * it has one. Regardless of the width of the combo box, we attempt to
     * size the popup to accomodate the width of the prototype display value.
     */
    private final PopupMenuListener popupSizerHandler = new PopupSizer();

    /**
     * An unfortunately necessary fixer for a misplaced popup.
     */
    private ComboBoxPopupLocationFix popupLocationFix;

    /**
     * We ensure that selecting an item from the popup via the mouse never
     * attempts to autocomplete for fear that we will replace the user's
     * newly selected item and the item will effectively be unselectable.
     */
    private final MouseListener popupMouseHandler = new PopupMouseHandler();

    /** Handles the special case of the backspace key in strict mode and the enter key. */
    private final KeyListener strictModeBackspaceHandler = new AutoCompleteKeyHandler();

    /** Handles selecting the text in the comboBoxEditorComponent when it gains focus. */
    private final FocusListener selectTextOnFocusGainHandler = new SelectTextOnFocusGainHandler();


    //
    // These listeners watch for invalid changes to the JComboBox which break our autocompletion
    //

    /**
     * Watches for changes of the Document which backs comboBoxEditorComponent and uninstalls
     * our DocumentFilter from the old Document and reinstalls it on the new.
     */
    private final DocumentWatcher documentWatcher = new DocumentWatcher();

    /** Watches for changes of the ComboBoxModel and reports them as violations. */
    private final ModelWatcher modelWatcher = new ModelWatcher();

    /** Watches for changes of the ComboBoxUI and reinstalls the autocompletion support. */
    private final UIWatcher uiWatcher = new UIWatcher();

    //
    // These booleans control when certain changes are to be respected and when they aren't
    //

    /** <tt>true</tt> indicates document changes should not be post processed
     * (i.e. just commit changes to the Document and do not cause any side-effects). */
    private boolean doNotPostProcessDocumentChanges = false;

    /** <tt>true</tt> indicates attempts to filter the ComboBoxModel should be ignored. */
    private boolean doNotFilter = false;

    /** <tt>true</tt> indicates attempts to change the document should be ignored. */
    private boolean doNotChangeDocument = false;

    /** <tt>true</tt> indicates attempts to select an autocompletion term should be ignored. */
    private boolean doNotAutoComplete = false;

    /** <tt>true</tt> indicates attempts to toggle the state of the popup should be ignored.
     * In general, the only time we should toggle the state of a popup is due to a users keystroke
     * (and not programmatically setting the selected item, for example). */
    private boolean doNotTogglePopup = true;

    /** <tt>true</tt> indicates attempts to clear the filter when hiding the popup should be ignored.
     * This is because sometimes we hide and reshow a popup in rapid succession and we want to avoid
     * the work to unfiltering/refiltering it.
     */
    private boolean doNotClearFilterOnPopupHide = false;

    //
    // Values present before {@link #install} executes - and are restored when {@link @uninstall} executes
    //

    /** The original setting of the editable field on the comboBox. */
    private final boolean originalComboBoxEditable;

    /** The original model installed on the comboBox. */
    private ComboBoxModel originalModel;

    /** The original ListCellRenderer installed on the comboBox. */
    private ListCellRenderer originalRenderer;

    //
    // Values present before {@link #decorateCurrentUI} executes - and are restored when {@link @undecorateOriginalUI} executes
    //

    /** The original Actions associated with the up and down arrow keys. */
    private Action originalSelectNextAction;
    private Action originalSelectPreviousAction;
    private Action originalSelectNext2Action;
    private Action originalSelectPrevious2Action;
    private Action originalAquaSelectNextAction;
    private Action originalAquaSelectPreviousAction;

    /**
     * This private constructor creates an AutoCompleteSupport object which adds
     * autocompletion functionality to the given <code>comboBox</code>. In
     * particular, a custom {@link ComboBoxModel} is installed behind the
     * <code>comboBox</code> containing the given <code>items</code>. The
     * <code>filterator</code> is consulted in order to extract searchable
     * text from each of the <code>items</code>. Non-null <code>format</code>
     * objects are used to convert ComboBoxModel elements to Strings and back
     * again for various functions like filtering, editing, and rendering.
     *
     * @param comboBox the {@link JComboBox} to decorate with autocompletion
     * @param items the objects to display in the <code>comboBox</code>
     * @param filterator extracts searchable text strings from each item
     * @param format converts combobox elements into strings and vice versa
     */
    private AutoCompleteSupport(JComboBox comboBox, EventList<E> items, TextFilterator<E> filterator, Format format) {
        this.comboBox = comboBox;
        this.originalComboBoxEditable = comboBox.isEditable();
        this.originalModel = comboBox.getModel();
        this.items = items;
        this.format = format;

        // only build a custom renderer if the user specified their own Format but has not installed a custom renderer of their own
        final boolean defaultRendererInstalled = comboBox.getEditor() instanceof UIResource;
        this.renderer = format != null && defaultRendererInstalled ? new StringFunctionRenderer() : null;

        // is this combo box a TableCellEditor?
        this.isTableCellEditor = Boolean.TRUE.equals(comboBox.getClientProperty("JComboBox.isTableCellEditor"));

        // build the ComboBoxModel capable of filtering its values
        this.filterMatcherEditor = new TextMatcherEditor<E>(filterator == null ? new DefaultTextFilterator() : filterator);
        this.filterMatcherEditor.setMode(TextMatcherEditor.STARTS_WITH);
        this.filteredItems = new FilterList<E>(items, this.filterMatcherEditor);
        this.comboBoxModel = new AutoCompleteComboBoxModel(this.filteredItems);

        // customize the comboBox
        this.comboBox.setModel(this.comboBoxModel);
        this.comboBox.setEditable(true);
        decorateCurrentUI();

        // react to changes made to the key parts of JComboBox which affect autocompletion
        this.comboBox.addPropertyChangeListener("UI", this.uiWatcher);
        this.comboBox.addPropertyChangeListener("model", this.modelWatcher);
        this.comboBoxEditorComponent.addPropertyChangeListener("document", this.documentWatcher);
    }

    /**
     * A convenience method to ensure {@link AutoCompleteSupport} is being
     * accessed from the Event Dispatch Thread.
     */
    private static void checkAccessThread() {
        if (!SwingUtilities.isEventDispatchThread())
            throw new IllegalStateException("AutoCompleteSupport must be accessed from the Swing Event Dispatch Thread, but was called on Thread \"" + Thread.currentThread().getName() + "\"");
    }

    /**
     * A convenience method to unregister and return all {@link ActionListener}s
     * currently installed on the given <code>comboBox</code>. This is the only
     * technique we can rely on to prevent the <code>comboBox</code> from
     * broadcasting {@link ActionEvent}s at inappropriate times.
     *
     * This method is the logical inverse of {@link #registerAllActionListeners}.
     */
    private static ActionListener[] unregisterAllActionListeners(JComboBox comboBox) {
        final ActionListener[] listeners = comboBox.getActionListeners();
        for (int i = 0; i < listeners.length; i++)
            comboBox.removeActionListener(listeners[i]);

        return listeners;
    }

    /**
     * A convenience method to register all of the given <code>listeners</code>
     * with the given <code>comboBox</code>.
     *
     * This method is the logical inverse of {@link #unregisterAllActionListeners}.
     */
    private static void registerAllActionListeners(JComboBox comboBox, ActionListener[] listeners) {
        for (int i = 0; i < listeners.length; i++)
            comboBox.addActionListener(listeners[i]);
    }

    /**
     * A convenience method to search through the given JComboBox for the
     * JButton which toggles the popup up open and closed.
     */
    private static JButton findArrowButton(JComboBox c) {
        for (int i = 0, n = c.getComponentCount(); i < n; i++) {
            final Component comp = c.getComponent(i);
            if (comp instanceof JButton)
                return (JButton) comp;
        }

        return null;
    }

    /**
     * Decorate all necessary areas of the current UI to install autocompletion
     * support. This method is called in the constructor and when the comboBox's
     * UI delegate is changed.
     */
    private void decorateCurrentUI() {
        // record some original settings of comboBox
        this.originalRenderer = comboBox.getRenderer();
        this.popupMenu = (JPopupMenu) comboBox.getUI().getAccessibleChild(comboBox, 0);
        this.popup = (ComboPopup) popupMenu;
        this.arrowButton = findArrowButton(comboBox);

        // if an arrow button was found, decorate the ComboPopup's MouseListener
        // with logic that unfilters the ComboBoxModel when the arrow button is pressed
        if (this.arrowButton != null) {
            this.arrowButton.removeMouseListener(popup.getMouseListener());
            this.arrowButtonMouseListener = new ArrowButtonMouseListener(popup.getMouseListener());
            this.arrowButton.addMouseListener(arrowButtonMouseListener);
        }

        // start listening for model changes (due to filtering) so we can resize the popup vertically
        this.comboBox.getModel().addListDataListener(listDataHandler);

        // calculate the popup's width according to the prototype value, if one exists
        this.popupMenu.addPopupMenuListener(popupSizerHandler);

        // fix the popup's location
        this.popupLocationFix = ComboBoxPopupLocationFix.install(this.comboBox);

        // start suppressing autocompletion when selecting values from the popup with the mouse
        this.popup.getList().addMouseListener(popupMouseHandler);

        // record the original Up/Down arrow key Actions
        final ActionMap actionMap = comboBox.getActionMap();
        this.originalSelectNextAction = actionMap.get("selectNext");
        this.originalSelectPreviousAction = actionMap.get("selectPrevious");
        this.originalSelectNext2Action = actionMap.get("selectNext2");
        this.originalSelectPrevious2Action = actionMap.get("selectPrevious2");
        this.originalAquaSelectNextAction = actionMap.get("aquaSelectNext");
        this.originalAquaSelectPreviousAction = actionMap.get("aquaSelectPrevious");

        final Action upAction = new MoveAction(-1);
        final Action downAction = new MoveAction(1);

        // install custom actions for the arrow keys in all non-Apple L&Fs
        actionMap.put("selectPrevious", upAction);
        actionMap.put("selectNext", downAction);
        actionMap.put("selectPrevious2", upAction);
        actionMap.put("selectNext2", downAction);

        // install custom actions for the arrow keys in the Apple Aqua L&F
        actionMap.put("aquaSelectPrevious", upAction);
        actionMap.put("aquaSelectNext", downAction);

        // install a custom ComboBoxEditor that decorates the existing one, but uses the
        // convertToString(...) method to produce text for the editor component (rather than .toString())
        this.comboBoxEditor = new FormatComboBoxEditor(comboBox.getEditor());
        this.comboBox.setEditor(comboBoxEditor);

        // add a DocumentFilter to the Document backing the editor JTextField
        this.comboBoxEditorComponent = (JTextField) comboBox.getEditor().getEditorComponent();
        this.document = (AbstractDocument) comboBoxEditorComponent.getDocument();
        this.document.setDocumentFilter(documentFilter);

        // install a custom renderer on the combobox, if we have built one
        if (this.renderer != null)
            comboBox.setRenderer(renderer);

        // add a KeyListener to the ComboBoxEditor which handles the special case of backspace when in strict mode
        this.comboBoxEditorComponent.addKeyListener(strictModeBackspaceHandler);
        // add a FocusListener to the ComboBoxEditor which selects all text when focus is gained
        this.comboBoxEditorComponent.addFocusListener(selectTextOnFocusGainHandler);
    }

    /**
     * Remove all customizations installed to various areas of the current UI
     * in order to uninstall autocompletion support. This method is invoked
     * after the comboBox's UI delegate is changed.
     */
    private void undecorateOriginalUI() {
        // if an arrow button was found, remove our custom MouseListener and
        // reinstall the normal popup MouseListener
        if (this.arrowButton != null) {
            this.arrowButton.removeMouseListener(arrowButtonMouseListener);
            this.arrowButton.addMouseListener(arrowButtonMouseListener.getDecorated());
        }

        // stop listening for model changes
        this.comboBox.getModel().removeListDataListener(listDataHandler);

        // restore the original ComboBoxEditor if our custom ComboBoxEditor is still installed
        if (this.comboBox.getEditor() == comboBoxEditor)
            this.comboBox.setEditor(comboBoxEditor.getDelegate());

        // stop adjusting the popup's width according to the prototype value
        this.popupMenu.removePopupMenuListener(popupSizerHandler);

        // stop fixing the combobox's popup location
        this.popupLocationFix.uninstall();

        // stop suppressing autocompletion when selecting values from the popup with the mouse
        this.popup.getList().removeMouseListener(popupMouseHandler);

        final ActionMap actionMap = comboBox.getActionMap();
        // restore the original actions for the arrow keys in all non-Apple L&Fs
        actionMap.put("selectPrevious", originalSelectPreviousAction);
        actionMap.put("selectNext", originalSelectNextAction);
        actionMap.put("selectPrevious2", originalSelectPrevious2Action);
        actionMap.put("selectNext2", originalSelectNext2Action);

        // restore the original actions for the arrow keys in the Apple Aqua L&F
        actionMap.put("aquaSelectPrevious", originalAquaSelectPreviousAction);
        actionMap.put("aquaSelectNext", originalAquaSelectNextAction);

        // remove the DocumentFilter from the Document backing the editor JTextField
        this.document.setDocumentFilter(null);

        // remove the KeyListener from the ComboBoxEditor which handles the special case of backspace when in strict mode
        this.comboBoxEditorComponent.removeKeyListener(strictModeBackspaceHandler);
        // remove the FocusListener from the ComboBoxEditor which selects all text when focus is gained
        this.comboBoxEditorComponent.removeFocusListener(selectTextOnFocusGainHandler);

        // remove the custom renderer if it is still installed
        if (this.comboBox.getRenderer() == renderer)
            this.comboBox.setRenderer(originalRenderer);

        // erase some original settings of comboBox
        this.originalRenderer = null;
        this.comboBoxEditor = null;
        this.comboBoxEditorComponent = null;
        this.document = null;
        this.popupMenu = null;
        this.popup = null;
        this.arrowButton = null;
    }

    /**
     * Installs support for autocompletion into the <code>comboBox</code> and
     * returns the support object that is actually providing those facilities.
     * The support object is returned so that the caller may invoke
     * {@link #uninstall} at some later time to remove the autocompletion
     * features.
     *
     * <p>This method assumes that the <code>items</code> can be converted into
     * reasonable String representations via {@link Object#toString()}.
     *
     * <p>The following must be true in order to successfully install support
     * for autocompletion on a {@link JComboBox}:
     *
     * <ul>
     *   <li> The JComboBox must use a {@link JTextField} as its editor component
     *   <li> The JTextField must use an {@link AbstractDocument} as its model
     * </ul>
     *
     * @param comboBox the {@link JComboBox} to decorate with autocompletion
     * @param items the objects to display in the <code>comboBox</code>
     * @return an instance of the support class providing autocomplete features
     * @throws IllegalStateException if this method is called from any Thread
     *      other than the Swing Event Dispatch Thread
     */
    public static <E> AutoCompleteSupport<E> install(JComboBox comboBox, EventList<E> items) {
        return install(comboBox, items, null);
    }

    /**
     * Installs support for autocompletion into the <code>comboBox</code> and
     * returns the support object that is actually providing those facilities.
     * The support object is returned so that the caller may invoke
     * {@link #uninstall} at some later time to remove the autocompletion
     * features.
     *
     * <p>This method assumes that the <code>items</code> can be converted into
     * reasonable String representations via {@link Object#toString()}.
     *
     * <p>The <code>filterator</code> will be used to extract searchable text
     * strings from each of the <code>items</code>. A <code>null</code>
     * filterator implies the item's toString() method should be used when
     * filtering it.
     *
     * <p>The following must be true in order to successfully install support
     * for autocompletion on a {@link JComboBox}:
     *
     * <ul>
     *   <li> The JComboBox must use a {@link JTextField} as its editor component
     *   <li> The JTextField must use an {@link AbstractDocument} as its model
     * </ul>
     *
     * @param comboBox the {@link JComboBox} to decorate with autocompletion
     * @param items the objects to display in the <code>comboBox</code>
     * @param filterator extracts searchable text strings from each item;
     *      <code>null</code> implies the item's toString() method should be
     *      used when filtering it
     * @return an instance of the support class providing autocomplete features
     * @throws IllegalStateException if this method is called from any Thread
     *      other than the Swing Event Dispatch Thread
     */
    public static <E> AutoCompleteSupport<E> install(JComboBox comboBox, EventList<E> items, TextFilterator<E> filterator) {
        return install(comboBox, items, filterator, null);
    }

    /**
     * Installs support for autocompletion into the <code>comboBox</code> and
     * returns the support object that is actually providing those facilities.
     * The support object is returned so that the caller may invoke
     * {@link #uninstall} at some later time to remove the autocompletion
     * features.
     *
     * <p>This method uses the given <code>format</code> to convert the
     * given <code>items</code> into Strings and back again. In other words,
     * this method does <strong>NOT</strong> rely on {@link Object#toString()}
     * to produce a reasonable String representation of each item. Likewise,
     * it does not rely on the existence of a valueOf(String) method for
     * creating items out of Strings as is the default behaviour of JComboBox.
     *
     * <p>It can be assumed that the only methods called on the given <code>format</code> are:
     * <ul>
     *   <li>{@link Format#format(Object)}
     *   <li>{@link Format#parseObject(String, ParsePosition)}
     * </ul>
     *
     * <p>As a convenience, this method will install a custom
     * {@link ListCellRenderer} on the <code>comboBox</code> that displays the
     * String value returned by the <code>format</code>. Though this is only
     * done if the given <code>format</code> is not <code>null</code> and if
     * the <code>comboBox</code> does not already use a custom renderer.
     *
     * <p>The <code>filterator</code> will be used to extract searchable text
     * strings from each of the <code>items</code>. A <code>null</code>
     * filterator implies one of two default strategies will be used. If the
     * <code>format</code> is not null then the String value returned from the
     * <code>format</code> object will be used when filtering a given item.
     * Otherwise, the item's toString() method will be used when it is filtered.
     *
     * <p>The following must be true in order to successfully install support
     * for autocompletion on a {@link JComboBox}:
     *
     * <ul>
     *   <li> The JComboBox must use a {@link JTextField} as its editor component
     *   <li> The JTextField must use an {@link AbstractDocument} as its model
     * </ul>
     *
     * @param comboBox the {@link JComboBox} to decorate with autocompletion
     * @param items the objects to display in the <code>comboBox</code>
     * @param filterator extracts searchable text strings from each item. If the
     *      <code>format</code> is not null then the String value returned from
     *      the <code>format</code> object will be used when filtering a given
     *      item. Otherwise, the item's toString() method will be used when it
     *      is filtered.
     * @param format a Format object capable of converting <code>items</code>
     *      into Strings and back. <code>null</code> indicates the standard
     *      JComboBox methods of converting are acceptable.
     * @return an instance of the support class providing autocomplete features
     * @throws IllegalStateException if this method is called from any Thread
     *      other than the Swing Event Dispatch Thread
     */
    public static <E> AutoCompleteSupport<E> install(JComboBox comboBox, EventList<E> items, TextFilterator<E> filterator, Format format) {
        checkAccessThread();

        final Component editorComponent = comboBox.getEditor().getEditorComponent();
        if (!(editorComponent instanceof JTextField))
            throw new IllegalArgumentException("comboBox must use a JTextField as its editor component");

        if (!(((JTextField) editorComponent).getDocument() instanceof AbstractDocument))
            throw new IllegalArgumentException("comboBox must use a JTextField backed by an AbstractDocument as its editor component");

        if (comboBox.getModel().getClass() == AutoCompleteSupport.AutoCompleteComboBoxModel.class)
            throw new IllegalArgumentException("comboBox is already configured for autocompletion");

        return new AutoCompleteSupport<E>(comboBox, items, filterator, format);
    }

    /**
     * This method is used to report environmental invariants which are
     * violated when the user adjusts the combo box in a way that is
     * incompatible with the requirements for autocompletion. A message can be
     * specified which will be included in the {@link IllegalStateException}
     * that is throw out of this method after the autocompletion support is
     * uninstalled.
     *
     * @param message a message to the programmer explaining the environmental
     *      invariant that was violated
     */
    private void throwIllegalStateException(String message) {
        final String exceptionMsg = message + "\n" +
                "In order for AutoCompleteSupport to continue to " +
                "work, the following invariants must be maintained after " +
                "AutoCompleteSupport.install() has been called:\n" +
                "* the ComboBoxModel may not be removed\n" +
                "* the AbstractDocument behind the JTextField can be changed but must be changed to some subclass of AbstractDocument\n" +
                "* the DocumentFilter on the AbstractDocument behind the JTextField may not be removed\n";

        uninstall();

        throw new IllegalStateException(exceptionMsg);
    }

    /**
     * A convenience method to produce a String from the given
     * <code>comboBoxElement</code>.
     */
    private String convertToString(Object comboBoxElement) {
        if (format != null)
            return format.format(comboBoxElement);

        return comboBoxElement == null ? "" : comboBoxElement.toString();
    }

    /**
     * Returns the autocompleting {@link JComboBox} or <code>null</code> if
     * {@link AutoCompleteSupport} has been {@link #uninstall}ed.
     */
    public JComboBox getComboBox() {
        return this.comboBox;
    }

    /**
     * Returns the {@link TextFilterator} that extracts searchable strings from
     * each item in the {@link ComboBoxModel}.
     */
    public TextFilterator<E> getTextFilterator() {
        return this.filterMatcherEditor.getFilterator();
    }

    /**
     * Returns the filtered {@link EventList} of items which backs the
     * {@link ComboBoxModel} of the autocompleting {@link JComboBox}.
     */
    public EventList<E> getItemList() {
        return this.filteredItems;
    }

    /**
     * Returns <tt>true</tt> if user specified strings are converted to the
     * case of the autocompletion term they match; <tt>false</tt> otherwise.
     */
    public boolean getCorrectsCase() {
        return correctsCase;
    }
    /**
     * If <code>correctCase</code> is <tt>true</tt>, user specified strings
     * will be converted to the case of the element they match. Otherwise
     * they will be left unaltered.
     *
     * <p>Note: this flag only has meeting when strict mode is turned off.
     * When strict mode is on, case is corrected regardless of this setting.
     *
     * @see #setStrict(boolean)
     *
     * @throws IllegalStateException if this method is called from any Thread
     *      other than the Swing Event Dispatch Thread
     */
    public void setCorrectsCase(boolean correctCase) {
        checkAccessThread();
        this.correctsCase = correctCase;
    }

    /**
     * Returns <tt>true</tt> if the user is able to specify values which do not
     * appear in the popup list of suggestions; <tt>false</tt> otherwise.
     */
    public boolean isStrict() {
        return strict;
    }
    /**
     * If <code>strict</code> is <tt>true</tt>, the user can specify values not
     * appearing within the ComboBoxModel. If it is <tt>false</tt> each
     * keystroke must continue to match some value in the ComboBoxModel or it
     * will be discarded.
     *
     * <p>Note: When strict mode is enabled, all user input is corrected to the
     * case of the autocompletion term, regardless of the correctsCase setting.
     *
     * @see #setCorrectsCase(boolean)
     *
     * @throws IllegalStateException if this method is called from any Thread
     *      other than the Swing Event Dispatch Thread
     */
    public void setStrict(boolean strict) {
        checkAccessThread();

        if (this.strict == strict) return;

        this.strict = strict;

        // if strict mode was just turned on, ensure the comboBox contains a
        // value from the ComboBoxModel (i.e. start being strict!)
        if (strict) {
            final String value = comboBoxEditorComponent.getText();
            String strictValue = findAutoCompleteTerm(value);

            // if the value in the editor already IS the autocompletion term,
            // short circuit to avoid broadcasting a needless ActionEvent
            if (value.equals(strictValue)) return;

            // select the first element if no autocompletion term could be found
            if (strictValue == null && !items.isEmpty())
                strictValue = convertToString(items.get(0));

            // return all elements to the ComboBoxModel
            applyFilter("");

            // adjust the editor to contain the autocompletion term
            doNotFilter = true;
            try {
                comboBoxEditorComponent.setText(strictValue);
            } finally {
                doNotFilter = false;
            }
        }
    }

    /**
     * Returns <tt>true</tt> if the combo box editor text is selected when it
     * gains focus; <tt>false</tt> otherwise.
     */
    public boolean getSelectsTextOnFocusGain() {
        return selectsTextOnFocusGain;
    }
    /**
     * If <code>selectsTextOnFocusGain</code> is <tt>true</tt>, all text in the
     * editor is selected when the combo box editor gains focus. If it is
     * <tt>false</tt> the selection state of the editor is not effected by
     * focus changes.
     *
     * @throws IllegalStateException if this method is called from any Thread
     *      other than the Swing Event Dispatch Thread
     */
    public void setSelectsTextOnFocusGain(boolean selectsTextOnFocusGain) {
        checkAccessThread();

        this.selectsTextOnFocusGain = selectsTextOnFocusGain;
    }

    /**
     * Returns the manner in which the contents of the {@link ComboBoxModel}
     * are filtered. This method will return one of
     * {@link TextMatcherEditor#CONTAINS} or {@link TextMatcherEditor#STARTS_WITH}.
     *
     * <p>{@link TextMatcherEditor#CONTAINS} indicates elements of the
     * {@link ComboBoxModel} are matched when they contain the text entered by
     * the user.
     *
     * <p>{@link TextMatcherEditor#STARTS_WITH} indicates elements of the
     * {@link ComboBoxModel} are matched when they start with the text entered
     * by the user.
     *
     * <p>In both modes, autocompletion only occurs when a given item starts
     * with user-specified text. The filter mode only affects the filtering
     * aspect of autocomplete support.
     */
    public int getFilterMode() {
        return filterMatcherEditor.getMode();
    }

    /**
     * Sets the manner in which the contents of the {@link ComboBoxModel} are
     * filtered. The given <code>mode</code> must be one of
     * {@link TextMatcherEditor#CONTAINS} or {@link TextMatcherEditor#STARTS_WITH}.
     *
     * @throws IllegalStateException if this method is called from any Thread
     *      other than the Swing Event Dispatch Thread
     *
     * @see #getFilterMode()
     */
    public void setFilterMode(int mode) {
        checkAccessThread();

        // adjust the MatcherEditor that filters the AutoCompleteComboBoxModel to respect the given mode
        // but ONLY adjust the contents of the model, avoid changing the text in the JComboBox's textfield
        doNotChangeDocument = true;
        try {
            filterMatcherEditor.setMode(mode);
        } finally {
            doNotChangeDocument = false;
        }
    }

    /**
     * This method removes autocompletion support from the {@link JComboBox}
     * it was installed on. This method is useful when the {@link EventList} of
     * items that backs the combo box must outlive the combo box itself.
     * Calling this method will return the combo box to its original state
     * before autocompletion was installed, and it will be available for
     * garbage collection independently of the {@link EventList} of items.
     *
     * @throws IllegalStateException if this method is called from any Thread
     *      other than the Swing Event Dispatch Thread
     */
    public void uninstall() {
        checkAccessThread();

        if (this.comboBox == null)
            throw new IllegalStateException("This AutoCompleteSupport has already been uninstalled");

        // 1. stop listening for changes
        this.comboBox.removePropertyChangeListener("UI", this.uiWatcher);
        this.comboBox.removePropertyChangeListener("model", this.modelWatcher);
        this.comboBoxEditorComponent.removePropertyChangeListener("document", this.documentWatcher);

        // 2. undecorate the original UI components
        this.undecorateOriginalUI();

        // 3. restore the original model to the JComboBox
        this.comboBox.setModel(originalModel);
        this.originalModel = null;

        // 4. restore the original editable flag to the JComboBox
        this.comboBox.setEditable(originalComboBoxEditable);

        // 5. dispose of our ComboBoxModel
        this.comboBoxModel.dispose();

        // 6. dispose of our FilterList so that it is severed from the given items EventList
        this.filteredItems.dispose();

        // null out the comboBox to indicate that this support class is uninstalled
        this.comboBox = null;
    }

    /**
     * This method updates the value which filters the items in the
     * ComboBoxModel.
     *
     * @param newFilter the new value by which to filter the item
     */
    private void applyFilter(String newFilter) {
        // break out early if we're flagged to ignore filter updates for the time being
        if (doNotFilter) return;

        // ignore attempts to change the text in the combo box editor while
        // the filtering is taking place
        doNotChangeDocument = true;
        final ActionListener[] listeners = unregisterAllActionListeners(comboBox);
        try {
            filterMatcherEditor.setFilterText(new String[] {newFilter});
        } finally {
            registerAllActionListeners(comboBox, listeners);
            doNotChangeDocument = false;
        }
    }

    /**
     * This method updates the {@link #prefix} to be the current value in the
     * ComboBoxEditor.
     */
    private void updateFilter() {
        prefix = comboBoxEditorComponent.getText();

        if (prefix.length() == 0)
            filterMatcher = Matchers.trueMatcher();
        else
            filterMatcher = new TextMatcher<String>(new String[] {prefix}, GlazedLists.toStringTextFilterator(), TextMatcherEditor.STARTS_WITH);
    }

    /**
     * A small convenience method to try showing the ComboBoxPopup.
     */
    private void togglePopup() {
        // break out early if we're flagged to ignore attempts to toggle the popup state
        if (doNotTogglePopup) return;

        if (comboBoxModel.getSize() == 0)
            comboBox.hidePopup();

        else if (comboBox.isShowing() && !comboBox.isPopupVisible())
            comboBox.showPopup();
    }

    /**
     * Performs a linear scan of ALL ITEMS, regardless of the filtering state
     * of the ComboBoxModel, to locate the autocomplete term. If an exact
     * match of the given <code>value</code> can be found, then the value is
     * returned. If an exact match cannot be found, the first term that
     * <strong>starts with</strong> the given <code>value</code> is returned.
     *
     * <p>If no exact or partial match can be located, <code>null</code> is
     * returned.
     */
    private String findAutoCompleteTerm(String value) {
        // determine if our value is empty
        final boolean prefixIsEmpty = "".equals(value);

        final Matcher<String> valueMatcher = new TextMatcher<String>(new String[] {value}, GlazedLists.toStringTextFilterator(), TextMatcherEditor.STARTS_WITH);

        String partialMatchTerm = null;

        // search the list of ALL items for an autocompletion term for the given value
        for (int i = 0, n = items.size(); i < n; i++) {
            final String itemString = convertToString(items.get(i));

            // if we have an exact match, return the given value immediately
            if (value.equals(itemString))
                return value;

            // if we have not yet located a partial match, check the current itemString for a partial match
            // (to be returned if an exact match cannot be found)
            if (partialMatchTerm == null) {
                if (prefixIsEmpty ? "".equals(itemString) : valueMatcher.matches(itemString))
                    partialMatchTerm = itemString;
            }
        }

        return partialMatchTerm;
    }

    /**
     * This special version of EventComboBoxModel simply marks a flag to
     * indicate the items in the ComboBoxModel should not be filtered as a
     * side-effect of setting the selected item.
     */
    private class AutoCompleteComboBoxModel extends EventComboBoxModel<E> {
        public AutoCompleteComboBoxModel(EventList<E> source) {
            super(source);
        }
        public void setSelectedItem(Object selected) {
            doNotFilter = true;
            // remove all ActionListeners from the JComboBox since setting the selected item
            // would normally notify them, but in normal autocompletion behaviour, we don't want that
            final ActionListener[] listeners = unregisterAllActionListeners(comboBox);
            try {
                super.setSelectedItem(selected);

                // remove any text selection that might exist when an item is selected
                final int caretPos = comboBoxEditorComponent.getCaretPosition();
                comboBoxEditorComponent.select(caretPos, caretPos);
            } finally {
                // reinstall the ActionListeners we removed
                registerAllActionListeners(comboBox, listeners);
                doNotFilter = false;
            }
        }
    }

    /**
     * This class is the crux of the entire solution. This custom DocumentFilter
     * controls all edits which are attempted against the Document of the
     * ComboBoxEditor component. It is our hook to either control when to respect
     * edits as well as the side-effects the edit has on autocompletion and
     * filtering.
     */
    private class AutoCompleteFilter extends DocumentFilter {
        public void replace(FilterBypass filterBypass, int offset, int length, String string, AttributeSet attributeSet) throws BadLocationException {
            if (doNotChangeDocument) return;

            // collect rollback information before performing the replace
            final String valueBeforeEdit = comboBoxEditorComponent.getText();
            final int selectionStart = comboBoxEditorComponent.getSelectionStart();
            final int selectionEnd = comboBoxEditorComponent.getSelectionEnd();

            // this short-circuit corrects the PlasticLookAndFeel behaviour. Hitting the enter key in Plastic
            // will cause the popup to reopen because the Plastic ComboBoxEditor forwards on unnecessary updates
            // to the document, including ones where the text isn't really changing
            final boolean isReplacingAllText = offset == 0 && document.getLength() == length;
            if (isReplacingAllText && valueBeforeEdit.equals(string)) return;

            super.replace(filterBypass, offset, length, string, attributeSet);
            postProcessDocumentChange(filterBypass, attributeSet, valueBeforeEdit, selectionStart, selectionEnd, true);
        }

        public void insertString(FilterBypass filterBypass, int offset, String string, AttributeSet attributeSet) throws BadLocationException {
            if (doNotChangeDocument) return;

            // collect rollback information before performing the insert
            final String valueBeforeEdit = comboBoxEditorComponent.getText();
            final int selectionStart = comboBoxEditorComponent.getSelectionStart();
            final int selectionEnd = comboBoxEditorComponent.getSelectionEnd();

            super.insertString(filterBypass, offset, string, attributeSet);
            postProcessDocumentChange(filterBypass, attributeSet, valueBeforeEdit, selectionStart, selectionEnd, true);
        }

        public void remove(FilterBypass filterBypass, int offset, int length) throws BadLocationException {
            if (doNotChangeDocument) return;

            // collect rollback information before performing the remove
            final String valueBeforeEdit = comboBoxEditorComponent.getText();
            final int selectionStart = comboBoxEditorComponent.getSelectionStart();
            final int selectionEnd = comboBoxEditorComponent.getSelectionEnd();

            super.remove(filterBypass, offset, length);
            postProcessDocumentChange(filterBypass, null, valueBeforeEdit, selectionStart, selectionEnd, isStrict());
        }

        /**
         * This method generically post processes changes to the ComboBox
         * editor's Document. The generic algorithm, regardless of the type of
         * change, is as follows:
         *
         * <ol>
         *   <li> save the prefix as the user has entered it
         *   <li> filter the combo box items against the prefix
         *   <li> update the text in the combo box editor with an autocomplete suggestion
         *   <li> try to show the popup, if possible
         * </ol>
         */
        private void postProcessDocumentChange(FilterBypass filterBypass, AttributeSet attributeSet, String valueBeforeEdit, int selectionStart, int selectionEnd, boolean allowPartialAutoCompletionTerm) throws BadLocationException {
            // break out early if we're flagged to not post process the Document change
            if (doNotPostProcessDocumentChanges) return;

            final String valueAfterEdit = comboBoxEditorComponent.getText();

            // if an autocomplete term could not be found and we're in strict mode, rollback the edit
            if (isStrict() && findAutoCompleteTerm(valueAfterEdit) == null) {
                // indicate the error to the user
                UIManager.getLookAndFeel().provideErrorFeedback(comboBoxEditorComponent);

                // rollback the edit
                doNotPostProcessDocumentChanges = true;
                try {
                    comboBoxEditorComponent.setText(valueBeforeEdit);
                } finally {
                    doNotPostProcessDocumentChanges = false;
                }

                // restore the selection as it existed
                comboBoxEditorComponent.select(selectionStart, selectionEnd);

                // do not continue post processing changes
                return;
            }

            // record the selection before post processing the Document change
            // (we'll use this to decide whether to broadcast an ActionEvent when choosing the next selected index)
            final Object selectedItemBeforeEdit = comboBox.getSelectedItem();

            updateFilter();
            applyFilter(prefix);
            selectAutoCompleteTerm(filterBypass, attributeSet, selectedItemBeforeEdit, allowPartialAutoCompletionTerm);
            togglePopup();
        }

        /**
         * This method will attempt to locate a reasonable autocomplete item
         * from all combo box items and select it. It will also populate the
         * combo box editor with the remaining text which matches the
         * autocomplete item and select it. If the selection changes and the
         * JComboBox is not a Table Cell Editor, an ActionEvent will be
         * broadcast from the combo box.
         */
        private void selectAutoCompleteTerm(FilterBypass filterBypass, AttributeSet attributeSet, Object selectedItemBeforeEdit, boolean allowPartialAutoCompletionTerm) throws BadLocationException {
            // break out early if we're flagged to ignore attempts to autocomplete
            if (doNotAutoComplete) return;

            // determine if our prefix is empty (in which case we cannot use our filterMatcher to locate an autocompletion term)
            final boolean prefixIsEmpty = "".equals(prefix);

            // search the combobox model for a value that starts with our prefix (called an autocompletion term)
            for (int i = 0, n = comboBoxModel.getSize(); i < n; i++) {
                String itemString = convertToString(comboBoxModel.getElementAt(i));

                // if itemString does not match the prefix, continue searching for an autocompletion term
                if (prefixIsEmpty ? !"".equals(itemString) : !filterMatcher.matches(itemString))
                    continue;

                // record the index and value that are our "best" autocomplete terms so far
                int matchIndex = i;
                String matchString = itemString;

                // search for an *exact* match in the remainder of the ComboBoxModel
                // before settling for the partial match we have just found
                for (int j = i; j < n; j++) {
                    itemString = convertToString(comboBoxModel.getElementAt(j));

                    // if we've located an exact match, use its index and value rather than the partial match
                    if (prefix.equals(itemString)) {
                        matchIndex = j;
                        matchString = itemString;
                        break;
                    }
                }

                // if partial autocompletion terms are not allowed, and we only have a partial term, bail early
                if (!allowPartialAutoCompletionTerm && !prefix.equals(itemString))
                    return;

                // either keep the user's prefix or replace it with the itemString's prefix
                // depending on whether we correct the case
                if (getCorrectsCase() || isStrict()) {
                    filterBypass.replace(0, prefix.length(), matchString, attributeSet);
                } else {
                    final String itemSuffix = matchString.substring(prefix.length());
                    filterBypass.insertString(prefix.length(), itemSuffix, attributeSet);
                }

                // select the autocompletion term
                final boolean silently = isTableCellEditor || GlazedListsImpl.equal(selectedItemBeforeEdit, matchString);
                selectItem(matchIndex, silently);

                // select the text after the prefix but before the end of the text (it represents the autocomplete text)
                comboBoxEditorComponent.select(prefix.length(), document.getLength());

                return;
            }

            // reset the selection since we couldn't find the prefix in the model
            // (this has the side-effect of scrolling the popup to the top)
            final boolean silently = isTableCellEditor || selectedItemBeforeEdit == null;
            selectItem(-1, silently);
        }

        /**
         * Select the item at the given <code>index</code>. If
         * <code>silent</code> is <tt>true</tt>, the JComboBox will not
         * broadcast an ActionEvent.
         */
        private void selectItem(int index, boolean silently) {
            final Object valueToSelect = index == -1 ? null : comboBoxModel.getElementAt(index);

            // if nothing is changing about the selection, return immediately
            if (GlazedListsImpl.equal(comboBoxModel.getSelectedItem(), valueToSelect))
                return;

            doNotChangeDocument = true;
            try {
                if (silently)
                    comboBoxModel.setSelectedItem(valueToSelect);
                else
                    comboBox.setSelectedItem(valueToSelect);
            } finally {
                doNotChangeDocument = false;
            }
        }
    }

    /**
     * Select the item at the given <code>index</code>. This method behaves
     * differently in strict mode vs. non-strict mode.
     *
     * <p>In strict mode, the selected index must always be valid, so using the
     * down arrow key on the last item or the up arrow key on the first item
     * simply wraps the selection to the opposite end of the model.
     *
     * <p>In non-strict mode, the selected index can be -1 (no selection), so we
     * allow -1 to mean "adjust the value of the ComboBoxEditor to be the users
     * text" and only wrap to the end of the model when -2 is reached. In short,
     * <code>-1</code> is interpreted as "clear the selected item".
     * <code>-2</code> is interpreted as "the last element".
     */
    private void selectPossibleValue(int index) {
        if (isStrict()) {
            // wrap the index from past the start to the end of the model
            if (index < 0)
                index = comboBox.getModel().getSize()-1;

            // wrap the index from past the end to the start of the model
            if (index > comboBox.getModel().getSize()-1)
                index = 0;
        } else {
            // wrap the index from past the start to the end of the model
            if (index == -2)
                index = comboBox.getModel().getSize()-1;
        }

        // check if the index is within a valid range
        final boolean validIndex = index >= 0 && index < comboBox.getModel().getSize();

        // if the index isn't valid, select nothing
        if (!validIndex)
            index = -1;

        // adjust only the value in the comboBoxEditorComponent, but leave the comboBoxModel unchanged
        doNotPostProcessDocumentChanges = true;
        try {
            // select the index
            if (isTableCellEditor) {
                // while operating as a TableCellEditor, no ActionListeners must be notified
                // when using the arrow keys to adjust the selection
                final ActionListener[] listeners = unregisterAllActionListeners(comboBox);
                try {
                    comboBox.setSelectedIndex(index);
                } finally {
                    registerAllActionListeners(comboBox, listeners);
                }
            } else {
                comboBox.setSelectedIndex(index);
            }

            // if the original index wasn't valid, we've cleared the selection
            // and must set the user's prefix into the editor
            if (!validIndex) {
                comboBoxEditorComponent.setText(prefix);

                // don't bother unfiltering the popup since we'll redisplay the popup immediately
                doNotClearFilterOnPopupHide = true;
                try {
                    comboBox.setPopupVisible(false);
                } finally {
                    doNotClearFilterOnPopupHide = false;
                }
                comboBox.setPopupVisible(true);
            }
        } finally {
            doNotPostProcessDocumentChanges = false;
        }

        // if the comboBoxEditorComponent's values begins with the user's prefix, highlight the remainder of the value
        final String newSelection = comboBoxEditorComponent.getText();
        if (filterMatcher.matches(newSelection))
            comboBoxEditorComponent.select(prefix.length(), newSelection.length());
    }

    /**
     * The action invoked by hitting the up or down arrow key.
     */
    private class MoveAction extends AbstractAction {
        private final int offset;

        public MoveAction(int offset) {
            this.offset = offset;
        }

        public void actionPerformed(ActionEvent e) {
            if (comboBox.isShowing()) {
                if (comboBox.isPopupVisible()) {
                    selectPossibleValue(comboBox.getSelectedIndex() + offset);
                } else {
                    applyFilter(prefix);
                    comboBox.setPopupVisible(true);
                }
            }
        }
    }

    /**
     * This class listens to the ComboBoxModel and redraws the popup if it
     * must grow or shrink to accomodate the latest list of items.
     */
    private class ListDataHandler implements ListDataListener {
        private int previousItemCount = -1;

        public void contentsChanged(ListDataEvent e) {
            final int newItemCount = comboBox.getItemCount();

            // if the size of the model didn't change, the popup size won't change
            if (previousItemCount == newItemCount) return;

            final int maxPopupItemCount = comboBox.getMaximumRowCount();

            // if the popup is showing, check if it must be resized
            if (popupMenu.isShowing()) {
                if (comboBox.isShowing()) {
                    // if either the previous or new item count is less than the max,
                    // hide and show the popup to recalculate its new height
                    if (newItemCount < maxPopupItemCount || previousItemCount < maxPopupItemCount) {
                        // don't bother unfiltering the popup since we'll redisplay the popup immediately
                        doNotClearFilterOnPopupHide = true;
                        try {
                            comboBox.setPopupVisible(false);
                        } finally {
                            doNotClearFilterOnPopupHide = false;
                        }
                        comboBox.setPopupVisible(true);
                    }
                } else {
                    // if the comboBox is not showing, simply hide the popup to avoid:
                    // "java.awt.IllegalComponentStateException: component must be showing on the screen to determine its location"
                    // this case can occur when the comboBox is used as a TableCellEditor
                    // and is uninstalled (removed from the component hierarchy) before
                    // receiving this callback
                    comboBox.setPopupVisible(false);
                }
            }

            previousItemCount = newItemCount;
        }
        public void intervalAdded(ListDataEvent e) { contentsChanged(e); }
        public void intervalRemoved(ListDataEvent e) { contentsChanged(e); }
    }

    /**
     * This class sizes the popup menu of the combo box immediately before
     * it is shown on the screen. In particular, it will adjust the width
     * of the popup to accomodate a prototype display value if the combo
     * box contains one.
     */
    private class PopupSizer implements PopupMenuListener {
        public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
            // if the combo box does not contain a prototype display value, skip our sizing logic
            final Object prototypeValue = comboBox.getPrototypeDisplayValue();
            if (prototypeValue == null) return;

            final JComponent popupComponent = (JComponent) e.getSource();

            // attempt to extract the JScrollPane that scrolls the popup
            if (popupComponent.getComponent(0) instanceof JScrollPane) {
                final JScrollPane scroller = (JScrollPane) popupComponent.getComponent(0);

                // fetch the existing preferred size of the scroller, and we'll check if it is large enough
                final Dimension scrollerSize = scroller.getPreferredSize();

                // calculates the preferred size of the renderer's component for the prototype value
                final Dimension prototypeSize = getPrototypeSize(prototypeValue);

                // add to the preferred width, the width of the vertical scrollbar, when it is visible
                prototypeSize.width += scroller.getVerticalScrollBar().getPreferredSize().width;

                // adjust the preferred width of the scroller, if necessary
                if (prototypeSize.width > scrollerSize.width) {
                    scrollerSize.width = prototypeSize.width;

                    // set the new size of the scroller
                    scroller.setMaximumSize(scrollerSize);
                    scroller.setPreferredSize(scrollerSize);
                    scroller.setMinimumSize(scrollerSize);
                }
            }
        }

        private Dimension getPrototypeSize(Object prototypeValue) {
            // get the renderer responsible for drawing the prototype value
            ListCellRenderer renderer = comboBox.getRenderer();
            if (renderer == null)
                renderer = new DefaultListCellRenderer();

            // get the component from the renderer
            final Component comp = renderer.getListCellRendererComponent(popup.getList(), prototypeValue, -1, false, false);

            // determine the preferred size of the component
            comp.setFont(comboBox.getFont());
            return comp.getPreferredSize();
        }

        public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
            if (doNotClearFilterOnPopupHide) return;

            // the popup menu is being hidden, so clear the filter to return the ComboBoxModel to its unfiltered state
            applyFilter("");
        }

        public void popupMenuCanceled(PopupMenuEvent e) {}
    }

    /**
     * When the user selects a value from the popup with the mouse, we want to
     * honour their selection *without* attempting to autocomplete it to a new
     * term. Otherwise, it is possible that selections which are prefixes for
     * values that appear higher in the ComboBoxModel cannot be selected by the
     * mouse since they can always be successfully autocompleted to another
     * term.
     */
    private class PopupMouseHandler extends MouseAdapter {
        public void mousePressed(MouseEvent e) {
            doNotAutoComplete = true;
        }

        public void mouseReleased(MouseEvent e) {
            doNotAutoComplete = false;
        }
    }

    /**
     * When the user clicks on the arrow button, we always clear the
     * filtering from the model to emulate Firefox style autocompletion.
     */
    private class ArrowButtonMouseListener implements MouseListener {
        private final MouseListener decorated;

        public ArrowButtonMouseListener(MouseListener decorated) {
            this.decorated = decorated;
        }

        public void mousePressed(MouseEvent e) {
            // clear the filter if we're about to hide or show the popup
            // by clicking on the arrow button (this is EXPLICITLY different
            // than using the up/down arrow keys to show the popup)
            applyFilter("");
            decorated.mousePressed(e);
        }
        public MouseListener getDecorated() { return decorated; }
        public void mouseClicked(MouseEvent e) { decorated.mouseClicked(e); }
        public void mouseReleased(MouseEvent e) { decorated.mouseReleased(e); }
        public void mouseEntered(MouseEvent e) { decorated.mouseEntered(e); }
        public void mouseExited(MouseEvent e) { decorated.mouseExited(e); }
    }

    /**
     * This KeyListener handles the case when the user hits the backspace key
     * and the {@link AutoCompleteSupport} is strict. Normally backspace would
     * delete the selected text, if it existed, or delete the character
     * immediately preceding the cursor. In strict mode the ComboBoxEditor must
     * always contain a value from the ComboBoxModel, so the backspace key
     * <strong>NEVER</strong> alters the Document. Rather, it alters the
     * text selection to include one more character to the left. This is a nice
     * compromise, since the editor continues to retain a valid value from the
     * ComboBoxModel, but the user may type a key at any point to replace the
     * selection with another valid entry.
     *
     * This KeyListener also makes up for a bug in normal JComboBox when
     * handling the enter key. Specifically, hitting enter in an stock
     * JComboBox that is editable produces <strong>TWO</strong> ActionEvents.
     * When the enter key is detected we actually unregister all
     * ActionListeners, process the keystroke as normal, then reregister the
     * listeners and broadcast an event to them, producing a single ActionEvent.
     */
    private class AutoCompleteKeyHandler extends KeyAdapter {
        private ActionListener[] actionListeners;

        public void keyPressed(KeyEvent e) {
            doNotTogglePopup = false;

            // this KeyHandler performs ALL processing of the ENTER key otherwise multiple
            // ActionEvents are fired to ActionListeners by the default JComboBox processing.
            // To control processing of the enter key, we set a flag to avoid changing the
            // editor's Document in any way, and also unregister the ActionListeners temporarily.
            if (e.getKeyChar() == KeyEvent.VK_ENTER) {
                doNotChangeDocument = true;
                this.actionListeners = unregisterAllActionListeners(comboBox);
            }

            // make sure this backspace key does not modify our comboBoxEditorComponent's Document
            if (isTrigger(e))
                doNotChangeDocument = true;
        }

        public void keyTyped(KeyEvent e) {
            if (isTrigger(e)) {
                // if no content exists in the comboBoxEditorComponent, bail early
                if (comboBoxEditorComponent.getText().length() == 0) return;

                // calculate the current beginning of the selection
                int selectionStart = Math.min(comboBoxEditorComponent.getSelectionStart(), comboBoxEditorComponent.getSelectionEnd());

                // if we cannot extend the selection to the left, indicate the error
                if (selectionStart == 0) {
                    UIManager.getLookAndFeel().provideErrorFeedback(comboBoxEditorComponent);
                    return;
                }

                // add one character to the left of the selection
                selectionStart--;

                // select the text from the end of the Document to the new selectionStart
                // (which positions the caret at the selectionStart)
                comboBoxEditorComponent.setCaretPosition(comboBoxEditorComponent.getText().length());
                comboBoxEditorComponent.moveCaretPosition(selectionStart);
            }
        }

        public void keyReleased(KeyEvent e) {
            // resume the ability to modify our comboBoxEditorComponent's Document
            if (isTrigger(e))
                doNotChangeDocument = false;

            // keyPressed(e) has disabled the JComboBox's normal processing of the enter key
            // so now it is time to perform our own processing. We reattach all ActionListeners
            // and simulate exactly ONE ActionEvent in the JComboBox and then reenable Document changes.
            if (e.getKeyChar() == KeyEvent.VK_ENTER) {
                updateFilter();

                // reregister all ActionListeners and then notify them due to the ENTER key
                registerAllActionListeners(comboBox, this.actionListeners);
                comboBox.actionPerformed(new ActionEvent(e.getSource(), e.getID(), null));

                // null out our own reference to the ActionListeners
                this.actionListeners = null;

                // reenable Document changes once more
                doNotChangeDocument = false;
            }

            doNotTogglePopup = true;
        }

        private boolean isTrigger(KeyEvent e) {
            return isStrict() && e.getKeyChar() == KeyEvent.VK_BACK_SPACE;
        }
    }

    /**
     * To emulate Firefox behaviour, all text in the ComboBoxEditor is selected
     * from beginning to end when the ComboBoxEditor gains focus if the value
     * returned from {@link AutoCompleteSupport#getSelectsTextOnFocusGain()}
     * allows this behaviour.
     */
    private class SelectTextOnFocusGainHandler extends FocusAdapter {
        public void focusGained(FocusEvent e) {
            if (getSelectsTextOnFocusGain())
                comboBoxEditorComponent.select(0, comboBoxEditorComponent.getText().length());
        }
    }

    /**
     * Watch for a change of the ComboBoxUI and reinstall the necessary
     * behaviour customizations.
     */
    private class UIWatcher implements PropertyChangeListener {
        public void propertyChange(PropertyChangeEvent evt) {
            undecorateOriginalUI();
            decorateCurrentUI();
        }
    }

    /**
     * Watch for a change of the ComboBoxModel and report it as a violation.
     */
    private class ModelWatcher implements PropertyChangeListener {
        public void propertyChange(PropertyChangeEvent evt) {
            throwIllegalStateException("The ComboBoxModel cannot be changed. It was changed to: " + evt.getNewValue());
        }
    }

    /**
     * Watch the Document behind the editor component in case it changes. If a
     * new Document is swapped in, uninstall our DocumentFilter from the old
     * Document and install it on the new.
     */
    private class DocumentWatcher implements PropertyChangeListener {
        public void propertyChange(PropertyChangeEvent evt) {
            final Document newDocument = (Document) evt.getNewValue();

            if (!(newDocument instanceof AbstractDocument))
                throwIllegalStateException("The Document behind the JTextField was changed to no longer be an AbstractDocument. It was changed to: " + newDocument);

            // remove our DocumentFilter from the old document
            document.setDocumentFilter(null);

            // update the document we track internally
            document = (AbstractDocument) newDocument;

            // add our DocumentFilter to the new Document
            document.setDocumentFilter(documentFilter);
        }
    }

    /**
     * A custom renderer which honours the custom Format given by the user when
     * they invoked the install method.
     */
    private class StringFunctionRenderer extends DefaultListCellRenderer {
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            String string = convertToString(value);

            // JLabels require some text before they can correctly determine their height, so we convert "" to " "
            if (string.length() == 0)
                string = " ";
            return super.getListCellRendererComponent(list, string, index, isSelected, cellHasFocus);
        }
    }

    /**
     * A decorated version of the ComboBoxEditor that does NOT assume that
     * Object.toString() is the proper way to convert values from the
     * ComboBoxModel into Strings for the ComboBoxEditor's component. It uses
     * convertToString(E) instead.
     *
     * We implement the UIResource interface here so that changes in the UI
     * delegate of the JComboBox will *replace* this ComboBoxEditor with one
     * that is correct for the new L&F. We will then react to the change of UI
     * delegate by installing a new FormatComboBoxEditor overtop of the
     * UI Delegate's default ComboBoxEditor.
     */
    private class FormatComboBoxEditor implements ComboBoxEditor, UIResource {

        /** This is the ComboBoxEditor installed by the current UI Delegate of the JComboBox. */
        private final ComboBoxEditor delegate;
        private Object oldValue;

        public FormatComboBoxEditor(ComboBoxEditor delegate) {
            this.delegate = delegate;
        }

        public ComboBoxEditor getDelegate() {
            return delegate;
        }

        /**
         * BasicComboBoxEditor defines this method to call:
         *
         * editor.setText(anObject.toString());
         *
         * we intercept and replace it with our own String conversion logic
         * to remain consistent throughout.
         */
        public void setItem(Object anObject) {
            oldValue = anObject;
            ((JTextField) getEditorComponent()).setText(convertToString(anObject));
        }

        /**
         * BasicComboBoxEditor defines this method to use reflection to try
         * finding a method called valueOf(String) in order to return the
         * item. We attempt to find a user-supplied Format before
         * resorting to the valueOf(String) call.
         */
        public Object getItem() {
            final String oldValueString = convertToString(oldValue);
            final String currentString = ((JTextField) getEditorComponent()).getText();

            // if the String value in the editor matches the String version of
            // the last item that was set in the editor, return the item
            if (GlazedListsImpl.equal(oldValueString, currentString))
                return oldValue;

            // if the user specified a Format, use it
            if (format != null)
                return format.parseObject(currentString, PARSE_POSITION);

            // otherwise, use the default algorithm from BasicComboBoxEditor to produce a value
            if (oldValue != null && !(oldValue instanceof String))  {
                try {
                    final Method method = oldValue.getClass().getMethod("valueOf", VALUE_OF_SIGNATURE);
                    return method.invoke(oldValue, new Object[] {currentString});
                } catch (Exception ex) {
                    // fail silently and return the current string
                }
            }

            return currentString;
        }

        public Component getEditorComponent() { return delegate.getEditorComponent(); }
        public void selectAll() { delegate.selectAll(); }
        public void addActionListener(ActionListener l) { delegate.addActionListener(l); }
        public void removeActionListener(ActionListener l) { delegate.removeActionListener(l); }
    }

    /**
     * This default implementation of the TextFilterator interface uses the
     * same strategy for producing Strings from ComboBoxModel objects as the
     * renderer and editor.
     */
    class DefaultTextFilterator implements TextFilterator<E> {
        public void getFilterStrings(List<String> baseList, E element) {
            baseList.add(convertToString(element));
        }
    }

    /**
     * This factory method creates and returns a {@link DefaultCellEditor}
     * which adapts an autocompleting {@link JComboBox} for use as a Table
     * Cell Editor. The values within the table column are used as
     * autocompletion terms within the {@link ComboBoxModel}.
     *
     * <p>This version of <code>createTableCellEditor</code> assumes that the
     * values stored in the TableModel at the given <code>columnIndex</code>
     * are all {@link Comparable}, and that the natural ordering defined by
     * those {@link Comparable} values also determines which are duplicates
     * (and thus can safely be removed) and which are unique (and thus must
     * remain in the {@link ComboBoxModel}).
     *
     * <p>Note that this factory method is only appropriate for use when the
     * values in the {@link ComboBoxModel} should be the unique set of values
     * in a table column. If some other list of values will be used, do not use
     * this factory method and create the {@link DefaultCellEditor} directly
     * with code similar to this: <p>
     *
     * <pre>
     * EventList comboBoxModelValues = ...
     * JComboBox comboBox = new JComboBox();
     * DefaultCellEditor cellEditor = new DefaultCellEditor(comboBox);
     * AutoCompleteSupport.install(comboBox, comboBoxModelValues);
     * </pre>
     *
     * <p>If the appearance or function of the autocompleting {@link JComboBox}
     * is to be customized, it can be retrieved using
     * {@link DefaultCellEditor#getComponent()}.
     *
     * @param tableFormat specifies how each row object within a table is
     *      broken apart into column values
     * @param tableData the {@link EventList} backing the TableModel
     * @param columnIndex the index of the column for which to return a
     *      {@link DefaultCellEditor}
     * @return a {@link DefaultCellEditor} which contains an autocompleting
     *      combobox whose contents remain consistent with the data in the
     *      table column at the given <code>columnIndex</code>
     */
    public static <E> DefaultCellEditor createTableCellEditor(TableFormat<E> tableFormat, EventList<E> tableData, int columnIndex) {
        return createTableCellEditor(GlazedLists.comparableComparator(), tableFormat, tableData, columnIndex);
    }

    /**
     * This factory method creates and returns a {@link DefaultCellEditor}
     * which adapts an autocompleting {@link JComboBox} for use as a Table
     * Cell Editor. The values within the table column are used as
     * autocompletion terms within the {@link ComboBoxModel}.
     *
     * <p>This version of <code>createTableCellEditor</code> makes no
     * assumption about the values stored in the TableModel at the given
     * <code>columnIndex</code>. Instead, it uses the given
     * <code>uniqueComparator</code> to determine which values are duplicates
     * (and thus can safely be removed) and which are unique (and thus must
     * remain in the {@link ComboBoxModel}).
     *
     * <p>Note that this factory method is only appropriate for use when the
     * values in the {@link ComboBoxModel} should be the unique set of values
     * in a table column. If some other list of values will be used, do not use
     * this factory method and create the {@link DefaultCellEditor} directly
     * with code similar to this: <p>
     *
     * <pre>
     * EventList comboBoxModelValues = ...
     * JComboBox comboBox = new JComboBox();
     * DefaultCellEditor cellEditor = new DefaultCellEditor(comboBox);
     * AutoCompleteSupport.install(comboBox, comboBoxModelValues);
     * </pre>
     *
     * <p>If the appearance or function of the autocompleting {@link JComboBox}
     * is to be customized, it can be retrieved using
     * {@link DefaultCellEditor#getComponent()}.
     *
     * @param uniqueComparator the {@link Comparator} that strips away
     *      duplicate elements from the {@link ComboBoxModel}
     * @param tableFormat specifies how each row object within a table is
     *      broken apart into column values
     * @param tableData the {@link EventList} backing the TableModel
     * @param columnIndex the index of the column for which to return a
     *      {@link DefaultCellEditor}
     * @return a {@link DefaultCellEditor} which contains an autocompleting
     *      combobox whose contents remain consistent with the data in the
     *      table column at the given <code>columnIndex</code>
     */
    public static <E> DefaultCellEditor createTableCellEditor(Comparator uniqueComparator, TableFormat<E> tableFormat, EventList<E> tableData, int columnIndex) {
        // use a function to extract all values for the column
        final FunctionList.Function<E, Object> columnValueFunction = new TableColumnValueFunction<E>(tableFormat, columnIndex);
        final FunctionList<E, Object> allColumnValues = new FunctionList<E, Object>(tableData, columnValueFunction);

        // narrow the list to just unique values within the column
        final EventList<Object> uniqueColumnValues = new UniqueList<Object>(allColumnValues, uniqueComparator);

        // create a DefaultCellEditor backed by a JComboBox
        final DefaultCellEditor cellEditor = new DefaultCellEditor(new JComboBox());
        cellEditor.setClickCountToStart(2);

        // install autocompletion support on the JComboBox
        AutoCompleteSupport.install((JComboBox) cellEditor.getComponent(), uniqueColumnValues);

        return cellEditor;
    }

    /**
     * This function uses a TableFormat and columnIndex to extract all of the
     * values that are displayed in the given table column. These values are
     * used as autocompletion terms when editing a cell within that column.
     */
    private static final class TableColumnValueFunction<E> implements FunctionList.Function<E, Object> {
        private final TableFormat<E> tableFormat;
        private final int columnIndex;

        public TableColumnValueFunction(TableFormat<E> tableFormat, int columnIndex) {
            this.tableFormat = tableFormat;
            this.columnIndex = columnIndex;
        }

        public Object evaluate(E sourceValue) {
            return tableFormat.getColumnValue(sourceValue, columnIndex);
        }
    }
}