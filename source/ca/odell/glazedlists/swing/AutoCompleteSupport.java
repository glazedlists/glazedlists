package ca.odell.glazedlists.swing;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.FilterList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.TextFilterator;
import ca.odell.glazedlists.impl.filter.TextMatcher;
import ca.odell.glazedlists.matchers.Matcher;
import ca.odell.glazedlists.matchers.Matchers;
import ca.odell.glazedlists.matchers.TextMatcherEditor;

import javax.swing.*;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.plaf.ComboBoxUI;
import javax.swing.plaf.basic.BasicComboBoxUI;
import javax.swing.plaf.basic.BasicComboPopup;
import javax.swing.plaf.basic.ComboPopup;
import javax.swing.text.*;
import java.awt.*;
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
 */
public final class AutoCompleteSupport<E> {

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

    /** <tt>true</tt> indicates attempts to change the document should be ignored. */
    private boolean ignoreDocumentChanges = false;

    //
    // Values present when install() executed - these are restored in dispose()
    //

    /** The original setting of the editable field on the comboBox. */
    private final boolean originalComboBoxEditable;

    /** The original editor for the comboBox. */
    private final ComboBoxEditor originalComboBoxEditor;

    /** The original model installed on the comboBox. */
    private ComboBoxModel originalModel;

    /** The original UI delegate installed on the comboBox. */
    private ComboBoxUI originalUI;

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

        // build the ComboBoxModel capable of filtering its values
        this.filterMatcherEditor = new TextMatcherEditor<E>(filterator);
        this.filterMatcherEditor.setMode(TextMatcherEditor.STARTS_WITH);
        this.filteredItems = new FilterList<E>(items, filterMatcherEditor);
        this.comboBoxModel = new FilteringComboBoxModel(filteredItems);

        // customize the comboBox
        this.comboBox.setUI(new DynamicPopupComboBoxUI(10));
        this.comboBox.addPropertyChangeListener("UI", this.uiWatcher);
        this.comboBox.setEditable(true);
        this.comboBox.setModel(this.comboBoxModel);
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
     * @param comboBox the {@link JComboBox} to decorate with autocompletion
     * @param items the objects to display in the <code>comboBox</code>
     * @param filterator extracts searchable text strings from each item
     * @return an instance of the support class that is providing autocomplete
     *      features
     */
    public static <E> AutoCompleteSupport<E> install(JComboBox comboBox, EventList<E> items, TextFilterator<E> filterator) {
        if (!(comboBox.getEditor().getEditorComponent() instanceof JTextField))
            throw new IllegalArgumentException("comboBox must use a JTextField as its editor component");

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
                "* the Document behind the JTextField can be changed but must be changed to some subclass of AbstractDocument\n" +
                "* the DocumentFilter on the Document behind the JTextField may not be removed\n"
        );

        this.dispose();

        throw new IllegalStateException(exceptionMsg.toString());
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

        // 5. restore the original Document behind the combo box editor
        this.comboBox.setEditor(this.originalComboBoxEditor);
        this.comboBoxEditor.setDocument(this.document);

        // 6. restore the original editable flag
        this.comboBox.setEditable(originalComboBoxEditable);

        // 7. dispose of our FilterList so that it is severed from the given items EventList
        this.filteredItems.dispose();

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

        if (prefix.length() == 0)
            prefixMatcher = Matchers.trueMatcher();
        else
            prefixMatcher = new TextMatcher<String>(new String[] {prefix}, GlazedLists.toStringTextFilterator(), TextMatcherEditor.STARTS_WITH);
    }

    /**
     * A small convenience method to try showing the combo box popup.
     */
    private void tryToShowPopup() {
        if (comboBox.isShowing() && !comboBox.isPopupVisible())
            comboBox.showPopup();
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
                super.setSelectedItem(selected);
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
        protected boolean correctsCase = true;

        public void replace(FilterBypass filterBypass, int offset, int length, String string, AttributeSet attributeSet) throws BadLocationException {
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
            updateComboBoxEditor(filterBypass, attributeSet);
            tryToShowPopup();
        }

        /**
         * This method will attempt to locate a reasonable autocomplete item
         * from all combo box items and select it. It will also populate the
         * combo box editor with the remaining text which matches the
         * autocomplete item and select it.
         */
        private void updateComboBoxEditor(FilterBypass filterBypass, AttributeSet attributeSet) throws BadLocationException {
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
                    comboBoxEditor.select(prefix.length(), document.getLength());

                    return;
                }
            }
        }

        /**
         * Will change the user entered part of the string to match the case
         * of the matched item.
         *
         * <p/>e.g. "europe/lONdon" would be corrected to "Europe/London"
         */
        public void setCorrectsCase(boolean correctCase) {
            this.correctsCase = correctCase;
        }
        public boolean getCorrectsCase() {
            return correctsCase;
        }
    }

    /**
     * This UI Delegate exists in order to create a popup for the combobox that is
     * sized more appropriately as items are filtered into and out of existence.

     * This UI Delegate calculates the preferred width of the popup to be the
     * maximum preferred width for all combobox items. This allows the selection
     * to be rendered succinctly in the combobox, but available items to be
     * rendered in a wider fashion in the popup. This can be particularly useful

     * for table cells using combobox table cell editors.
     *
     * This UI Delegate also allows a maximum number of rows to be optionally
     * specified for the ComboBox Popup via the constructor. The popup may be
     * sized smaller than the maximum number of rows, if fewer combo box items
     * exist, but will never be larger.
     *
     * @author James Lemieux
     */
    private class DynamicPopupComboBoxUI extends BasicComboBoxUI {
        /**
         * The preferred width of the combo box popup calculated one time in
         * {@link #calculatePopupWidth()} by considering the preferred width
         * of the renderer for each value.
         */
        private int preferredPopupWidth = 0;

        /**
         * The preferred number of rows to use when calculating the dimensions
         * of the combo box popup.
         */
        private int maximumRowCount = -1;

        /**
         * A listener which reacts to changes in the combo box model.
         */
        private final ListDataListener listDataHandler = new ListDataHandler();

        /**
         * @param maximumRowCount the maximum number of rows to show in the combo
         *         box popup.
         */
        public DynamicPopupComboBoxUI(int maximumRowCount) {
            this.maximumRowCount = maximumRowCount;
        }

        public void installUI(JComponent c) {
            super.installUI(c);
            this.getPreferredPopupWidth();
            this.comboBox.getModel().addListDataListener(this.listDataHandler);
        }

        public void uninstallUI(JComponent c) {
            this.comboBox.getModel().removeListDataListener(this.listDataHandler );
            this.preferredPopupWidth = 0;
            this.maximumRowCount = 0;
            super.uninstallUI(c);
        }

        public PropertyChangeListener createPropertyChangeListener() {
            return new ModelPropertyChangeHandler(super.createPropertyChangeListener());
        }

        private class ModelPropertyChangeHandler implements PropertyChangeListener {
            private final PropertyChangeListener decorated;

            public ModelPropertyChangeHandler(PropertyChangeListener decorated) {
                this.decorated = decorated;
            }

            public void propertyChange(PropertyChangeEvent evt) {
                decorated.propertyChange(evt);
                if ("model".equals(evt.getPropertyName())) {
                    final ComboBoxModel oldModel = (ComboBoxModel) evt.getOldValue();
                    final ComboBoxModel newModel = (ComboBoxModel) evt.getNewValue();

                    oldModel.removeListDataListener(listDataHandler);
                    newModel.addListDataListener(listDataHandler);
                }
            }
        }

        /**
         * A convenience method to calculate the preferred width of the popup if
         * it has not get been calculated. The preferred popup width is the maximum
         * preferred width of the renderer after considering all values in the
         * combo box.
         */
        private int calculatePopupWidth() {
            double maxPreferredWidth = 0;
            for (int i = 0; i < this.comboBox.getItemCount(); i++) {
                final Component c = this.comboBox.getRenderer().getListCellRendererComponent(listBox, comboBox.getItemAt(i), 0, false, false);

                if (c.getPreferredSize().getWidth() > maxPreferredWidth)
                    maxPreferredWidth = c.getPreferredSize().getWidth();
            }
            return (int) maxPreferredWidth;
        }

        /**
         * Returns the preferred popup width, caching the value as needed.
         */
        private int getPreferredPopupWidth() {
            if (this.preferredPopupWidth == 0)
                this.preferredPopupWidth = this.calculatePopupWidth();
            return this.preferredPopupWidth ;
        }

        /**
         * Returns the preferred dimensions of the combo box.
         */
        private Dimension getPreferredPopupSize() {
            final Dimension size = this.comboBox.getPreferredSize();
            return new Dimension(this.getPreferredPopupWidth(), size.height);
        }

        /**
         * Updates the maximum number of rows to display in the combobox popup
         * to be the minimum of either the item count or the maximum row count.
         */
        protected void updateMaximumRowCount() {
            if (this.maximumRowCount > 0)
                this.comboBox.setMaximumRowCount(Math.min(this.maximumRowCount, this.comboBox.getItemCount()));
        }

        protected void installKeyboardActions() {
            super.installKeyboardActions();
            final ActionMap actionMap = comboBox.getActionMap();

            // decorate the normal DownAction with our own that removes any
            // filters before proceeding with the normal DownAction
            final Action delegateAction = actionMap.get("selectNext");
            actionMap.put("selectNext", new DownAction(delegateAction));

            // install custom actions for Apple LAFs to avoid a ClassCastException
            actionMap.put("aquaSelectPrevious", new AquaUpAction());
            actionMap.put("aquaSelectNext", new AquaDownAction());
        }

        protected ComboPopup createPopup() {
            final BasicComboPopup popup = new CustomSizedComboPopup(this.comboBox);

            popup.getAccessibleContext().setAccessibleParent(this.comboBox);
            return popup;
        }

        private class CustomSizedComboPopup extends BasicComboPopup {
            public CustomSizedComboPopup(JComboBox combo) {
                super(combo);
            }

            /**
             * This method is only called when the arrow button which shows
             * and hides the popup is clicked. We exploit this fact and use
             * it as a hook to clear the filter which is filtering the drop
             * down. This emulates the behaviour of FireFox's URL selector.
             */
            protected void togglePopup() {
                if (!isVisible())
                    applyFilter("");

                super.togglePopup();
            }

            /**
             * Override the method which computes the bounds of the popup in
             * order to inject logic which sizes it more appropriately. The
             * preferred width of the popup is the maximum preferred width of
             * any item and the height is the combobox's maximum row count.
             *
             * @param px starting x location
             * @param py starting y location
             * @param pw starting width
             * @param ph starting height
             * @return a rectangle which represents the placement and size of the popup
             */
            protected Rectangle computePopupBounds(int px, int py, int pw, int ph) {
                final Dimension popupSize = getPreferredPopupSize();
                final int maximumRowCount = this.comboBox.getMaximumRowCount();
                popupSize.setSize(popupSize.width, getPopupHeightForRowCount(maximumRowCount));
                if (this.comboBox.getSize().width - 2 > popupSize.width)
                    popupSize.width = this.comboBox.getSize().width - 2; else popupSize.width += 20;
                return super.computePopupBounds (px, py, popupSize.width, popupSize.height);
            }
        }

        /**
         * This method listens the to the combo box model and updates the maximum
         * row count of the combo box based on the new item count in the new model.
         */
        private class ListDataHandler implements ListDataListener {
            public void contentsChanged(ListDataEvent e) {
                if (e.getIndex0() != -1 && e.getIndex1() != -1) updateMaximumRowCount();
            }
            public void intervalAdded(ListDataEvent e) {
                updateMaximumRowCount();
            }
            public void intervalRemoved(ListDataEvent e) {
                updateMaximumRowCount();
            }
        }

        /**
         * Decorate the UI Delegate's DownAction with our own that always
         * applies the latest filter before displaying the popup.
         */
        private class DownAction extends AbstractAction {
            private final Action delegateAction;

            public DownAction(Action delegateAction) {
                this.delegateAction = delegateAction;
            }
            public void actionPerformed(ActionEvent e) {
                if (!comboBox.isPopupVisible())
                    applyFilter(prefix);

                if (this.delegateAction != null)
                    this.delegateAction.actionPerformed(e);
            }
        }

        /**
         * Apple's Aqua Look and Feel defines AquaUpAction in a way that
         * assumes the UI delegate to be AquaComboBoxUI. Since we install our
         * own UI, we must replace Apple's AquaUpAction with our own which
         * does not make the same assumption.
         */
        private class AquaUpAction extends AbstractAction {
            public void actionPerformed(ActionEvent e) {
                if (comboBox.isEnabled() && comboBox.isShowing()) {
                    if (comboBox.isPopupVisible()) {
                        int i = listBox.getSelectedIndex();
                        if (i > 0) {
                            listBox.setSelectedIndex(i - 1);
                            listBox.ensureIndexIsVisible(i - 1);
                        }
                        comboBox.repaint();
                    } else {
                        applyFilter(prefix);
                        comboBox.setPopupVisible(true);
                    }
                }
            }
        }

        /**
         * Apple's Aqua Look and Feel defines AquaDownAction in a way that
         * assumes the UI delegate to be AquaComboBoxUI. Since we install our
         * own UI, we must replace Apple's AquaDownAction with our own which
         * does not make the same assumption.
         */
        private class AquaDownAction extends AbstractAction {
            public void actionPerformed(ActionEvent e) {
                if (comboBox.isEnabled() && comboBox.isShowing()) {
                    if (comboBox.isPopupVisible()) {
                        int i = listBox.getSelectedIndex();
                        if (i < comboBox.getModel().getSize() - 1) {
                            listBox.setSelectedIndex(i + 1);
                            listBox.ensureIndexIsVisible(i + 1);
                        }
                        comboBox.repaint();
                    } else {
                        applyFilter(prefix);
                        comboBox.setPopupVisible(true);
                    }
                }
            }
        }
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

            // clear our DocumentFilter from the old document
            document.setDocumentFilter(null);

            // update the document we track internally
            document = (AbstractDocument) newDocument;

            // add our DocumentFilter to the new Document
            document.setDocumentFilter(documentFilter);
        }
    }
}