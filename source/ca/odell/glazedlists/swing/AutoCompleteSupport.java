/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.swing;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.FilterList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.TextFilterator;
import ca.odell.glazedlists.impl.filter.TextMatcher;
import ca.odell.glazedlists.matchers.Matcher;
import ca.odell.glazedlists.matchers.Matchers;
import ca.odell.glazedlists.matchers.TextMatcherEditor;

import javax.accessibility.Accessible;
import javax.swing.*;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.plaf.ComboBoxUI;
import javax.swing.plaf.basic.BasicComboBoxUI;
import javax.swing.plaf.basic.ComboPopup;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/**
 * This class installs support for filtering and autocompletion into a standard
 * {@link JComboBox}. All behaviour provided is meant to mimic the
 * functionality of the Firefox address field. In order to achieve all of the
 * autocompletion and filtering behaviour, the following occurs when
 * {@link #install} is called:
 *
 * <ul>
 *   <li> the JComboBox will be made editable
 *   <li> the JComboBox will have a custom UI delegate installed on it
 *   <li> the JComboBox will have a custom ComboBoxModel installed on it
 *        containing the given items
 *   <li> the JTextField which is the editor component for the JComboBox
 *        will have a DocumentFilter installed on its backing Document
 * </ul>
 *
 * The strategy of this support class is to alter all of the objects which
 * influence the behaviour of the JComboBox's popup in one single context.
 * With that achieved, it greatly reduces the cross-functional communication
 * required to customize the behaviour of JComboBox for filtering and
 * autocompletion.
 *
 * @author James Lemieux
 */
public final class AutoCompleteSupport<E> {

    private static final boolean DEBUG = false;

    //
    // These member variables control behaviour of the autocompletion support
    //

    /**
     * The preferred number of rows to use when calculating the vertical
     * dimension of the combo box popup.
     */
    private int maximumRowCount;

    /**
     * <tt>true</tt> if user specified text is converted into the same case as
     * the first matched element. <tt>false</tt> will leave user specified text
     * unaltered.
     */
    protected boolean correctsCase = true;

    //
    // These are member variables for convenience
    //

    /** The comboBox being decorated with autocomplete functionality. */
    private JComboBox comboBox;

    /** The model backing the comboBox. */
    private final FilteringComboBoxModel comboBoxModel;

    /** The FilterList which holds the items present in the comboBoxModel. */
    private final FilterList<E> filteredItems;

    /** The MatcherEditor driving the FilterList behind the comboBoxModel. */
    private final TextMatcherEditor<E> filterMatcherEditor;

    /** The textfield which acts as the editor of the comboBox. */
    private final JTextField comboBoxEditor;

    /** The Document backing the comboBoxEditor. */
    private AbstractDocument document;

    /** A DocumentFilter that controls edits to the Document behind the comboBoxEditor. */
    private final ComboCompleterFilter documentFilter = new ComboCompleterFilter();

    /** The last prefix specified by the user. */
    private String prefix = "";

    /** This matcher determines if the user-defined prefix matches the beginning of a given String. */
    private Matcher<String> prefixMatcher = Matchers.trueMatcher();

    //
    // These listeners watch for invariant violations in the JComboBox and report them
    //

    /**
     * Watches for changes of the Document which backs comboBoxEditor and uninstalls
     * our DocumentFilter from the old Document and reinstalls it on the new.
     */
    private final DocumentWatcher documentWatcher = new DocumentWatcher();

    /** Watches for changes of the ComboBoxModel and reports them as violations. */
    private final ModelWatcher modelWatcher = new ModelWatcher();

    /** Watches for changes of the ComboBoxUI and reports them as violations. */
    private final UIWatcher uiWatcher = new UIWatcher();

    //
    // These booleans control when certain changes are to be respected and when they aren't
    //

    /** <tt>true</tt> indicates attempts to filter the combo box items should be ignored. */
    private boolean ignoreFilterUpdates = false;

    /** <tt>&gt; 0</tt> indicates attempts to change the document should be ignored. */
    private boolean ignoreDocumentChanges;

    //
    // Values present when install() executed - these are restored in dispose()
    //

    /** The original setting of the editable field on the comboBox. */
    private final boolean originalComboBoxEditable;

    /** The original editor for the comboBox. */
    private ComboBoxEditor originalComboBoxEditor;

    /** The original model installed on the comboBox. */
    private ComboBoxModel originalModel;

    /** The original UI delegate installed on the comboBox. */
    private ComboBoxUI originalUI;

    /** The original maximum row count of the comboBox. */
    private int originalMaximumRowCount;

    /**
     * This private constructor creates an AutoCompleteSupport object which adds
     * autocompletion functionality to the given <code>comboBox</code>. In
     * particular, a custom {@link ComboBoxModel} is installed behind the
     * <code>comboBox</code> containing the given <code>items</code>. The
     * <code>filterator</code> is consulted in order to extract searchable
     * text from each of the <code>items</code>.
     *
     * @param comboBox the {@link JComboBox} to decorate with autocompletion
     * @param items the objects to display in the <code>comboBox</code>
     * @param filterator extracts searchable text strings from each item
     */
    private AutoCompleteSupport(JComboBox comboBox, EventList<E> items, TextFilterator<E> filterator) {
        // record some original settings of comboBox
        this.comboBox = comboBox;
        this.originalUI = comboBox.getUI();
        this.originalComboBoxEditable = comboBox.isEditable();
        this.originalModel = comboBox.getModel();
        this.originalComboBoxEditor = comboBox.getEditor();
        this.originalMaximumRowCount = comboBox.getMaximumRowCount();

        // build the ComboBoxModel capable of filtering its values
        this.filterMatcherEditor = new TextMatcherEditor<E>(filterator);
        this.filterMatcherEditor.setMode(TextMatcherEditor.STARTS_WITH);
        this.filteredItems = new FilterList<E>(items, filterMatcherEditor);
        this.comboBoxModel = new FilteringComboBoxModel(filteredItems);

        // customize the comboBox
        this.comboBox.setModel(this.comboBoxModel);
        this.maximumRowCount = this.comboBox.getMaximumRowCount();
        this.comboBox.setUI(new AutoCompleteComboBoxUI((BasicComboBoxUI) this.comboBox.getUI()));
        this.comboBox.addPropertyChangeListener("UI", this.uiWatcher);
        this.comboBox.setEditable(true);
        this.comboBox.addPropertyChangeListener("model", this.modelWatcher);
        this.comboBoxEditor = (JTextField) comboBox.getEditor().getEditorComponent();
        this.comboBoxEditor.addPropertyChangeListener("document", this.documentWatcher);

        // customize the existing Document behind the editor JTextField
        this.document = (AbstractDocument) this.comboBoxEditor.getDocument();
        this.document.setDocumentFilter(this.documentFilter);
    }

    /**
     * Installs support for autocompletion into the <code>comboBox</code> and
     * returns the support object that is actually providing those facilities.
     * The support object is returned so that the caller may invoke
     * {@link #dispose} at some later time to remove the autocompletion
     * features.
     *
     * <p>This method assumes that the <code>items</code> can be converted into
     * reasonable String representations via {@link Object#toString()}.
     *
     * @param comboBox the {@link JComboBox} to decorate with autocompletion
     * @param items the objects to display in the <code>comboBox</code>
     * @return an instance of the support class that is providing autocomplete
     *      features
     */
    public static <E> AutoCompleteSupport install(JComboBox comboBox, EventList<E> items) {
        return install(comboBox, items, GlazedLists.toStringTextFilterator());
    }

    /**
     * Installs support for autocompletion into the <code>comboBox</code> and
     * returns the support object that is actually providing those facilities.
     * The support object is returned so that the caller may invoke
     * {@link #dispose} at some later time to remove the autocompletion
     * features.
     *
     * <p>The <code>filterator</code> will be used to extract searchable text
     * strings from each of the <code>items</code>.
     *
     * The following must be true in order to successfully install support for
     * autocomplete on a {@link JComboBox}:
     *
     * <ul>
     *   <li> The JComboBox must use a JTextField as its editor
     *   <li> The JTextField must us an AbstractDocument as its model
     *   <li> The JComboBox UI delegate must be a subclass of BasicComboBoxUI
     * </ul>
     *
     * @param comboBox the {@link JComboBox} to decorate with autocompletion
     * @param items the objects to display in the <code>comboBox</code>
     * @param filterator extracts searchable text strings from each item
     * @return an instance of the support class that is providing autocomplete
     *      features
     */
    public static <E> AutoCompleteSupport<E> install(JComboBox comboBox, EventList<E> items, TextFilterator<E> filterator) {
        if (!(comboBox.getEditor().getEditorComponent() instanceof JTextField))
            throw new IllegalArgumentException("comboBox must use a JTextField as its editor component");

        if (!(comboBox.getUI() instanceof BasicComboBoxUI))
            throw new IllegalArgumentException("comboBox UI must be a subclass of " + BasicComboBoxUI.class);

        if (!(((JTextField) comboBox.getEditor().getEditorComponent()).getDocument() instanceof AbstractDocument))
            throw new IllegalArgumentException("comboBox must use a JTextField backed by an AbstractDocument as its editor component");

        return new AutoCompleteSupport<E>(comboBox, items, filterator);
    }

    /**
     * This method is used to report environmental invariants which are
     * violated when the user adjusts the combo box in a way that is
     * incompatible with the requirements for autocompletion. A message can be
     * specified which will be dumped out to {@link System#err} before the
     * autocompletion support is uninstalled.
     *
     * @param message a message to the programmer explaining the environmental
     *      invariant that was violated
     */
    private void throwIllegalStateException(String message) {
        final StringBuffer exceptionMsg = new StringBuffer(message);
        exceptionMsg.append("\n");

        exceptionMsg.append("In order for AutoCompleteSupport to continue to " +
                "work, the following invariants must be maintained after " +
                "AutoCompleteSupport.install() has been called:\n" +
                "* the ComboBoxModel may not be removed\n" +
                "* the ComboBoxUI may not be removed\n" +
                "* the ComboBoxEditor may not be removed\n" +
                "* the AbstractDocument behind the JTextField can be changed but must be changed to some subclass of AbstractDocument\n" +
                "* the DocumentFilter on the AbstractDocument behind the JTextField may not be removed\n"
        );

        this.dispose();

        throw new IllegalStateException(exceptionMsg.toString());
    }

    /** Return the maximum number of rows the popup can display. */
    public int getMaximumRowCount() {
        return maximumRowCount;
    }
    /** Set the maximum number of rows the popup can display. */
    public void setMaximumRowCount(int maximumRowCount) {
        this.maximumRowCount = maximumRowCount;
        this.updateMaximumRowCount();
    }

    /**
     * Updates the maximum number of rows to display in the combobox popup
     * to be the minimum of either the item count or the maximum row count.
     */
    private void updateMaximumRowCount() {
        comboBox.setMaximumRowCount(Math.min(maximumRowCount, comboBox.getItemCount()));
    }

    /**
     * Return <tt>true</tt> if user specified strings are converted to the
     * case of the element they match; <tt>false</tt> otherwise.
     */
    public boolean getCorrectsCase() {
        return correctsCase;
    }
    /**
     * If <code>correctCase</code> is <tt>true</tt>, user specified strings
     * will be converted to the case of the element they match. Otherwise
     * they will be left unaltered.
     */
    public void setCorrectsCase(boolean correctCase) {
        this.correctsCase = correctCase;
    }

    /**
     * This method removes autocompletion support from the {@link JComboBox}
     * it was installed on. This method is useful when the {@link EventList} of
     * items that backs the combo box must outlive the combo box itself.
     * Calling this method will return the combo box to its original state
     * before autocompletion was installed, and it will be available for
     * garbage collection independently of the {@link EventList} of items.
     */
    public void dispose() {
        if (this.comboBox == null)
            throw new IllegalStateException("This AutoCompleteSupport has already been disposed");

        // 1. stop listening for changes
        this.comboBox.removePropertyChangeListener("UI", this.uiWatcher);
        this.comboBox.removePropertyChangeListener("model", this.modelWatcher);
        this.comboBoxEditor.removePropertyChangeListener("document", this.documentWatcher);

        // 2. restore the original model to the JComboBox
        this.comboBox.setModel(this.originalModel);
        this.originalModel = null;

        // 3. restore the original UI delegate to the JComboBox
        this.comboBox.setUI(this.originalUI);
        this.originalUI = null;

        // 5. restore the original ComboBoxEditor to the JComboBox
        this.comboBox.setEditor(this.originalComboBoxEditor);
        this.originalComboBoxEditor = null;

        // 6. restore the original editable flag to the JComboBox
        this.comboBox.setEditable(originalComboBoxEditable);

        // 7. dispose of our FilterList so that it is severed from the given items EventList
        this.filteredItems.dispose();

        // 8. restore the original maximum row count to the JComboBox
        this.comboBox.setMaximumRowCount(originalMaximumRowCount);
        originalMaximumRowCount = -1;

        // null out the comboBox to indicate that this support class is disposed
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
        if (ignoreFilterUpdates) return;

        // ignore attempts to change the text in the combo box editor while
        // the filtering is taking place
        ignoreDocumentChanges = true;
        try {
            if (DEBUG) System.out.println("setting new filter to: '" + newFilter + "'");
            filterMatcherEditor.setFilterText(new String[] {newFilter});
        } finally {
            ignoreDocumentChanges = false;
        }
    }

    /**
     * This method updates the {@link #prefix} to be the current value in the
     * combo box editor.
     */
    private void savePrefixSnapshot() throws BadLocationException {
        prefix = document.getText(0, document.getLength());
        if (DEBUG) System.out.println("adjusted new prefix to: '" + prefix + "'");

        if (prefix.length() == 0)
            prefixMatcher = Matchers.trueMatcher();
        else
            prefixMatcher = new TextMatcher<String>(new String[] {prefix}, GlazedLists.toStringTextFilterator(), TextMatcherEditor.STARTS_WITH);
    }

    /**
     * A small convenience method to try showing the combo box popup.
     */
    private void togglePopup() {
        if (comboBoxModel.getSize() == 0) {
            if (DEBUG) System.out.println("hiding popup");
            comboBox.hidePopup();

        } else if (comboBox.isShowing() && !comboBox.isPopupVisible()) {
            if (DEBUG) System.out.println("showing popup");
            comboBox.showPopup();
        }
    }

    /**
     * This special version of EventComboBoxModel simply marks a flag to
     * indicate the items in the ComboBoxModel should not be filtered as a
     * side-effect of setting the selected item.
     */
    private class FilteringComboBoxModel extends EventComboBoxModel<E> {
        public FilteringComboBoxModel(EventList<E> source) {
            super(source);
        }
        public void setSelectedItem(Object selected) {
            ignoreFilterUpdates = true;
            try {
                if (DEBUG) System.out.println("setting selected item popup to: '" + selected + "'");
                super.setSelectedItem(selected);

                // Windows L&F likes to selectAll() text in the comboBoxEditor when a new item
                // is selected, but that interferes with our autocompletion usability, so we
                // clear the selection here
                final int caretPos = comboBoxEditor.getCaretPosition();
                comboBoxEditor.select(caretPos, caretPos);
            } finally {
                ignoreFilterUpdates = false;
            }
        }
    }

    /**
     * This class is the crux of the entire solution. This custom
     * DocumentFilter controls all edits which are attempted against the combo
     * box editor's Document. It is our hook to either control when to respect
     * edits as well as the effect the edit has on autocompletion.
     */
    private class ComboCompleterFilter extends DocumentFilter {
        public void replace(FilterBypass filterBypass, int offset, int length, String string, AttributeSet attributeSet) throws BadLocationException {
            // this short-circuit helps with the PlasticLookAndFeel behaviour. Hitting the enter key in Plastic
            // will cause the popup to reopen because the Plastic ComboBoxEditor forwards on unecessary updates
            // to the document, including ones where the text isn't really changing
            if (offset == 0 && document.getLength() == length && string != null && string.equals(comboBoxEditor.getText()))
                return;

            if (ignoreDocumentChanges) return;
            super.replace(filterBypass, offset, length, string, attributeSet);
            processDocumentChange(filterBypass, attributeSet);
        }

        public void insertString(FilterBypass filterBypass, int offset, String string, AttributeSet attributeSet) throws BadLocationException {
            if (ignoreDocumentChanges) return;
            super.insertString(filterBypass, offset, string, attributeSet);
            processDocumentChange(filterBypass, attributeSet);
        }

        public void remove(FilterBypass filterBypass, int offset, int length) throws BadLocationException {
            if (ignoreDocumentChanges) return;
            super.remove(filterBypass, offset, length);
            processDocumentChange(null, null);
        }

        /**
         * This method generically processes changes to the ComboBox editor's Document.
         * The generic algorithm, regardless of the type of change, is as follows:
         *
         * <ol>
         *   <li> save the prefix as the user has entered it
         *   <li> filter the combo box items against the prefix
         *   <li> update the text in the combo box editor with an autocomplete suggestion
         *   <li> try to show the popup, if possible
         * </ol>
         */
        private void processDocumentChange(FilterBypass filterBypass, AttributeSet attributeSet) throws BadLocationException {
            savePrefixSnapshot();
            applyFilter(prefix);
            updateComboBox(filterBypass, attributeSet);
            togglePopup();
        }

        /**
         * This method will attempt to locate a reasonable autocomplete item
         * from all combo box items and select it. It will also populate the
         * combo box editor with the remaining text which matches the
         * autocomplete item and select it.
         */
        private void updateComboBox(FilterBypass filterBypass, AttributeSet attributeSet) throws BadLocationException {
            // make sure the prefix is not "" (in which case we skip the search)
            if (prefix.length() > 0) {
                // search the combobox model for a value that starts with our prefix
                for (int i = 0; i < comboBoxModel.getSize(); i++) {
                    final String item = comboBoxModel.getElementAt(i).toString();

                    // if the user-specified prefix matches the item's prefix
                    // we have found an appropriate item to select
                    if (prefixMatcher.matches(item)) {
                        if (filterBypass != null) {
                            // either keep the user's prefix or replace it with the item's prefix
                            // depending on whether we correct the case
                            if (correctsCase) {
                                filterBypass.replace(0, prefix.length(), item, attributeSet);
                            } else {
                                final String itemSuffix = item.substring(prefix.length());
                                filterBypass.insertString (prefix.length(), itemSuffix, attributeSet);
                            }
                        }

                        // select the matched item
                        ignoreDocumentChanges = true;
                        comboBox.setSelectedIndex(i);
                        ignoreDocumentChanges = false;

                        // select the text after the prefix but before the end of the text
                        // (it represents the autocomplete text)
                        if (DEBUG) System.out.println("selecting characters from " + prefix.length() + " to " + document.getLength());
                        comboBoxEditor.select(prefix.length(), document.getLength());

                        return;
                    }
                }
            }

            // reset the selection since we couldn't find the prefix in the model
            // (this has the side-effect of scrolling the popup to the top)
            ignoreDocumentChanges = true;
            comboBox.setSelectedIndex(-1);
            ignoreDocumentChanges = false;
        }
    }


    /**
     * This UI Delegate exists in order to decorate the regular ComboBoxUI
     * implementation with custom logic that bends it toward autocompletion
     * and filtering.
     *
     * It is very important that decoration be used rather than providing a
     * custom UI delegate because we wish to only alter the behaviour of the
     * JComboBox but NOT the look.
     *
     * @author James Lemieux
     */
    private class AutoCompleteComboBoxUI extends BasicComboBoxUI {

        /** The regular UI delegate of the JComboBox, which we are decorating. */
        private final BasicComboBoxUI delegate;

        /**
         * A listener which reacts to changes in the combo box model by
         * resizing the combo box to recompute the preferred size of the popup.
         */
        private final ListDataListener listDataHandler = new ListDataHandler();

        /**
         * The MouseListener which is installed on the {@link #arrowButton} and
         * is responsible for clearing the filter and then showing / hiding the
         * {@link #popup}.
         */
        private ArrowButtonMouseListener arrowButtonMouseListener;

        // Control the selection behavior of the JComboBox when it is used
        // in the JTable DefaultCellEditor.
        private boolean isTableCellEditor;

        /**
         * Build a UI delegate that decorates the given <code>delegate</code>
         * with autocompletion and filtering behaviour. The look of the
         * <code>delegate</code> UI should remain unchanged.
         *
         * @param delegate the delegate defining the look of this UI
         */
        public AutoCompleteComboBoxUI(BasicComboBoxUI delegate) {
            this.delegate = delegate;
        }

        public void installUI(JComponent c) {
            comboBox = (JComboBox) c;
            delegate.installUI(c);
            popup = (ComboPopup) delegate.getAccessibleChild(c, 0);
            arrowButton = findArrowButton(comboBox);

            // is this combo box a cell editor?
            final Boolean inTable = (Boolean) c.getClientProperty("JComboBox.isTableCellEditor");
            if (inTable != null)
                isTableCellEditor = Boolean.TRUE.equals(inTable);

            // if an arrow button was found, decorate the ComboPopup's
            // MouseListener with logic that unfilters the ComboBoxModel when
            // the arrow button is pressed
            if (arrowButton != null) {
                arrowButton.removeMouseListener(popup.getMouseListener());
                arrowButtonMouseListener = new ArrowButtonMouseListener(popup.getMouseListener());
                arrowButton.addMouseListener(arrowButtonMouseListener);
            }

            // start listening for model changes (due to filtering) so we can resize the popup appropriately
            comboBox.getModel().addListDataListener(listDataHandler);

            // decorate the normal DownAction and UpActions with logic that clears the
            // filters before proceeding with the normal Action
            final ActionMap actionMap = comboBox.getActionMap();

            if (actionMap.get("selectNext") != null) {
                actionMap.put("selectPrevious", new UpAction());
                actionMap.put("selectNext", new DownAction());

            } else if (actionMap.get("aquaSelectNext") != null) {
                // install custom action keys for the Apple LAF
                actionMap.put("aquaSelectPrevious", new UpAction());
                actionMap.put("aquaSelectNext", new DownAction());
            }
        }

        /**
         * A convenience method to search through the given component for the
         * JButton which toggles the popup up open and closed.
         */
        private JButton findArrowButton(JComponent c) {
            for (int i = 0; i < c.getComponentCount(); i++) {
                final Component comp = c.getComponent(i);
                if (comp instanceof JButton)
                    return (JButton) comp;
            }

            return null;
        }

        public void uninstallUI(JComponent c) {
            // reinstall the normal MouseListener for the arrowButton
            if (arrowButton != null) {
                arrowButton.addMouseListener(arrowButtonMouseListener.getDecorated());
                arrowButton.removeMouseListener(arrowButtonMouseListener);
            }

            // stop listening for model changes
            comboBox.getModel().removeListDataListener(listDataHandler);

            // clear our member variable state
            comboBox = null;
            popup = null;
            arrowButton = null;

            // the delegate will uninstall our decorated keyboard actions
            // so we don't worry about them here
            delegate.uninstallUI(c);
        }

        // todo override these with some custom logic ?
        protected void selectNextPossibleValue() {
//            ignoreDocumentChanges = true;

            final int nextIndex = (isTableCellEditor ? listBox.getSelectedIndex() : comboBox.getSelectedIndex()) + 1;

            if (nextIndex < comboBox.getModel().getSize()) {
                if (isTableCellEditor) {
                    listBox.setSelectedIndex(nextIndex);
                    listBox.ensureIndexIsVisible(nextIndex);
                } else {
                    comboBox.setSelectedIndex(nextIndex);
                }
                comboBox.repaint();
            }

//            ignoreDocumentChanges = false;
        }

        protected void selectPreviousPossibleValue() {
//            ignoreDocumentChanges = true;

            final int previousIndex = (isTableCellEditor ? listBox.getSelectedIndex() : comboBox.getSelectedIndex()) - 1;

            if (previousIndex >= 0) {
                if (isTableCellEditor) {
                    listBox.setSelectedIndex(previousIndex);
                    listBox.ensureIndexIsVisible(previousIndex);
                } else {
                    comboBox.setSelectedIndex(previousIndex);
                }
                comboBox.repaint();
            }

//            ignoreDocumentChanges = false;
        }

        /**
         * This method listens the to the combo box model and updates the maximum
         * row count of the combo box based on the new item count in the new model.
         */
        private class ListDataHandler implements ListDataListener {
            public void contentsChanged(ListDataEvent e) {
                if (e.getIndex0() != -1 && e.getIndex1() != -1)
                    updateMaximumRowCount();
            }
            public void intervalAdded(ListDataEvent e) {
                updateMaximumRowCount();
            }
            public void intervalRemoved(ListDataEvent e) {
                updateMaximumRowCount();
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

            public MouseListener getDecorated() { return decorated; }

            public void mousePressed(MouseEvent e) {
                applyFilter("");
                decorated.mousePressed(e);
            }
            public void mouseClicked(MouseEvent e) { decorated.mouseClicked(e); }
            public void mouseReleased(MouseEvent e) { decorated.mouseReleased(e); }
            public void mouseEntered(MouseEvent e) { decorated.mouseEntered(e); }
            public void mouseExited(MouseEvent e) { decorated.mouseExited(e); }
        }

        private class DownAction extends AbstractAction {
            public void actionPerformed(ActionEvent e) {
                if (comboBox.isShowing()) {
                    if (comboBox.isPopupVisible()) {
                        selectNextPossibleValue();
                    } else {
                        applyFilter(prefix);
                        comboBox.setPopupVisible(true);
                    }
                }
            }
        }

        private class UpAction extends AbstractAction {
            public void actionPerformed(ActionEvent e) {
                if (comboBox.isShowing()) {
                    if (comboBox.isPopupVisible()) {
                        selectPreviousPossibleValue();
                    } else {
                        applyFilter(prefix);
                        comboBox.setPopupVisible(true);
                    }
                }
            }
        }

        public void addEditor() { delegate.addEditor(); }
        public void removeEditor() { delegate.removeEditor(); }
        public void configureArrowButton() { delegate.configureArrowButton(); }
        public void unconfigureArrowButton() { delegate.unconfigureArrowButton(); }
        public boolean isPopupVisible(JComboBox c) { return delegate.isPopupVisible(c); }
        public void setPopupVisible(JComboBox c, boolean v) { delegate.setPopupVisible(c, v); }
        public boolean isFocusTraversable(JComboBox c) { return delegate.isFocusTraversable(c); }
        public void paint(Graphics g, JComponent c) { delegate.paint(g, c); }
        public Dimension getPreferredSize(JComponent c) { return delegate.getPreferredSize(c); }
        public Dimension getMinimumSize(JComponent c) { return delegate.getMinimumSize(c); }
        public Dimension getMaximumSize(JComponent c) { return delegate.getMaximumSize(c); }
        public int getAccessibleChildrenCount(JComponent c) { return delegate.getAccessibleChildrenCount(c); }
        public Accessible getAccessibleChild(JComponent c, int i) { return delegate.getAccessibleChild(c, i); }
        public void paintCurrentValue(Graphics g, Rectangle bounds, boolean hasFocus) { delegate.paintCurrentValue(g, bounds, hasFocus); }
        public void paintCurrentValueBackground(Graphics g, Rectangle bounds, boolean hasFocus) { delegate.paintCurrentValueBackground(g, bounds, hasFocus); }
        public void update(Graphics g, JComponent c) { delegate.update(g, c); }
        public boolean contains(JComponent c, int x, int y) { return delegate.contains(c, x, y); }
    }

    /**
     * Watch for a change of the ComboBoxUI and report it as a violation.
     */
    private class UIWatcher implements PropertyChangeListener {
        public void propertyChange(PropertyChangeEvent evt) {
            throwIllegalStateException("The ComboBoxUI cannot be changed. It was changed to: " + evt.getNewValue());
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
                throwIllegalStateException("The Document behind the JTextField was changed to no longer be an AbstractDocument. It was a " + newDocument);

            // remove our DocumentFilter from the old document
            document.setDocumentFilter(null);

            // update the document we track internally
            document = (AbstractDocument) newDocument;

            // add our DocumentFilter to the new Document
            document.setDocumentFilter(documentFilter);
        }
    }
}