/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.swing;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.FilterList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.TextFilterator;
import ca.odell.glazedlists.matchers.TextMatcherEditor;

import javax.swing.*;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.plaf.basic.BasicComboPopup;
import javax.swing.plaf.basic.ComboPopup;
import javax.swing.plaf.metal.MetalComboBoxUI;
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

    /** The comboBox being decorated with autocomplete functionality. */
    private final JComboBox comboBox;
    /** The model backing the comboBox. */
    private final FilteringComboBoxModel comboBoxModel;
    /** The FilterList which holds the items present in the comboBoxModel. */
    private final FilterList<E> filteredItems;
    /** The MatcherEditor driving the FilterList behind the comboBoxModel. */
    private final TextMatcherEditor<E> filterMatcherEditor;

    /** The textfield which acts as the editor of the comboBox. */
    private final JTextField comboBoxEditor;
    /** The original Document backing the comboBoxEditor. */
    private final Document originalDocument;
    /** The Document backing the comboBoxEditor. */
    private final AbstractDocument document;

    /** The last prefix specified by the user. */
    private String prefix = "";

    /** <tt>true</tt> indicates the comboBoxModel is currently adjusting the selected item. */
    private boolean ignoreFilterUpdates = false;
    /** <tt>true</tt> indicates attempts to change the document should be ignored. */
    private boolean ignoreDocumentChanges = false;

    /**
     * This private constructor creates an AutoCompleteSupport object which adds
     * autocompletion functionality to the given <code>comboBox</code>. In
     * particular, a custom {@link ComboBoxModel} is installed behind the
     * <code>comboBox</code> containing the given <code>items</code>. The
     * <code>textFilterator</code> is consulted in order to extract searchable
     * text from each of the <code>items</code>.
     *
     * @param comboBox the {@link JComboBox} to decorate with autocompletion
     * @param items the objects to display in the <code>comboBox</code>
     * @param textFilterator extract searchable text strings from each item
     */
    private AutoCompleteSupport(JComboBox comboBox, EventList<E> items, TextFilterator<E> textFilterator) {
        this.filterMatcherEditor = new TextMatcherEditor<E>(textFilterator);
        this.filterMatcherEditor.setMode(TextMatcherEditor.STARTS_WITH);
        this.filteredItems = new FilterList<E>(items, filterMatcherEditor);

        this.comboBox = comboBox;
        this.comboBox.setEditable(true);
//        this.comboBox.setUI(new DelegatingPopupComboBoxUI(this.comboBox.getUI(), 10));
        this.comboBox.setUI(new DynamicPopupComboBoxUI(10));
        this.comboBoxModel = new FilteringComboBoxModel<E>(filteredItems);
        this.comboBox.setModel(this.comboBoxModel);

        this.comboBoxEditor = (JTextField) comboBox.getEditor().getEditorComponent();

        this.originalDocument = this.comboBoxEditor.getDocument();
        if (this.originalDocument instanceof AbstractDocument) {
            // customize the existing Document with our DocumentFilter
            this.document = (AbstractDocument) this.comboBoxEditor.getDocument ();
            this.document.setDocumentFilter(new ComboCompleterFilter());
        } else {
            // install a new Document with our DocumentFilter
            this.document = new PlainDocument();
            this.document.setDocumentFilter(new ComboCompleterFilter());
            this.comboBoxEditor.setDocument(this.document);
        }
    }

    public static AutoCompleteSupport install(JComboBox comboBox, EventList<String> items) {
        if (!(comboBox.getEditor().getEditorComponent() instanceof JTextField))
            throw new IllegalArgumentException("comboBox must use a JTextField as its editor component");
        return new AutoCompleteSupport<String>(comboBox, items, GlazedLists.toStringTextFilterator());
    }

    private void updateFilter(String newFilterValue) {
        if (ignoreFilterUpdates)
            return;

        ignoreDocumentChanges = true;
        try {
            filterMatcherEditor.setFilterText(new String[] {newFilterValue});
        } finally {
            ignoreDocumentChanges = false;
        }
    }

    private void updatePrefix() throws BadLocationException {
        prefix = document.getText(0, document.getLength());
    }

    private class FilteringComboBoxModel<E> extends EventComboBoxModel<E> {
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



    private class ComboCompleterFilter extends DocumentFilter {
        // The text in the input field before we started last looking for matches
        protected boolean caseSensitive = false;
        protected boolean correctsCase = true;
        protected int firstSelectedIndex = -1;

        public void replace(FilterBypass filterBypass, int offset, int length, String string, AttributeSet attributeSet) throws BadLocationException {
            if (ignoreDocumentChanges)
                return;

            super.replace(filterBypass, offset, length, string, attributeSet);

            updatePrefix();
            firstSelectedIndex = -1;

            // search for appropriate item in the combobox model
            for (int i = 0; i < comboBoxModel.getSize(); i++) {
                final String item = comboBoxModel.getElementAt(i).toString();

                // if the prefix is longer than the current item
                if (item.length() < prefix.length())
                    continue;

                final String itemPrefix = item.substring(0, prefix.length());

                // if the prefix of the item matches exactly
                if (equal(itemPrefix, prefix)) {
                    firstSelectedIndex = i;

                    // make the Document match the case of the item as necessary
                    final String itemSuffix = item.substring(prefix.length());
                    if (correctsCase)
                        filterBypass.replace(0, prefix.length(), item, attributeSet);
                    else
                        filterBypass.insertString(prefix.length(), itemSuffix, attributeSet);
                    break;
                }
            }

            // show the combobox if it isn't yet showing
            if (!comboBox.isPopupVisible())
                comboBox.showPopup();

            // if we found an item in the combobox that matches our prefix, select it
            if (firstSelectedIndex != -1) {
                comboBox.setSelectedIndex(firstSelectedIndex);

                filterBypass.replace(0, document.getLength(), comboBoxEditor.getText(), attributeSet);
                comboBoxEditor.select(prefix.length(), document.getLength());
            }

            updateFilter(prefix);
            updatePrefix();
        }

        public void insertString(FilterBypass filterBypass, int offset, String string, AttributeSet attributeSet) throws BadLocationException {
            if (ignoreDocumentChanges)
                return;

            // show the combobox if it isn't yet showing
            if (!comboBox.isPopupVisible())
                comboBox.showPopup ();

            super.insertString(filterBypass, offset, string, attributeSet);

            updatePrefix();
            updateFilter(prefix);
        }

        public void remove(FilterBypass filterBypass, int offset, int length) throws BadLocationException {
            if (ignoreDocumentChanges)
                return;

            // show the combobox if it isn't yet showing
            if (!comboBox.isPopupVisible())
                comboBox.showPopup ();

            super.remove(filterBypass, offset, length);

            updatePrefix();
            updateFilter(prefix);
        }

        private boolean equal(String s1, String s2) {
            return caseSensitive ? s1.equals(s2) : s1.equalsIgnoreCase(s2);
        }

        public void setCaseSensitive(boolean caseSensitive) {
            this.caseSensitive = caseSensitive;
        }
        public boolean isCaseSensitive() {
            return caseSensitive;
        }

        /**
         * Will change the user entered part of the string to match the case of the matched item.
         * <p/>
         * e.g.
         * "europe/lONdon" would be corrected to "Europe/London"
         * <p/>
         * This option only makes sense if case sensitive is turned off
         */
        public void setCorrectsCase(boolean correctCase) {
            this.correctsCase = correctCase;
        }
        public boolean getCorrectsCase() {
            return correctsCase;
        }
    }



//    /**
//     * This UI Delegate exists in order to create a popup for the combobox that is
//     * sized more appropriately. This UI Delegate calculates the preferred width of
//     * the popup to be the maximum preferred width for all combobox selections.
//     * This allows the selection to be rendered succinctly in the combobox, but
//     * available selections to be rendered in a larger fashion in the popup. This
//     * can be particularly useful for table cells using combobox table cell
//     * editors.
//     *
//     * This UI Delegate also allows a maximum number of rows to be optionally
//     * specified for the ComboBox Popup via the constructor. The popup may be
//     * sized smaller than the maximum number of rows, if fewer combo box items
//     * exist, but will never be larger.
//     *
//     * @author James Lemieux
//     */
//    private class DelegatingPopupComboBoxUI extends ComboBoxUI {
//        private JComboBox comboBox;
//
//        private final ComboBoxUI delegateUI;
//
//        private final PropertyChangeListener propertyChangeHandler = new PropertyChangeHandler();
//
//        /**
//         * The maximum number of rows to use when calculating the height
//         * of the combo box popup.
//         */
//        private int maximumRowCount = -1;
//
//        /** A listener which reacts to changes in the combo box model. */
//        private ListDataListener listDataHandler = new ListDataHandler();
//
//        /**
//         * @param maximumRowCount the maximum number of rows to show in the combo
//         *         box popup.
//         */
//        public DelegatingPopupComboBoxUI(ComboBoxUI delegateUI, int maximumRowCount) {
//            this.delegateUI = delegateUI;
//            this.maximumRowCount = maximumRowCount;
//        }
//
//        public void installUI(JComponent c) {
//            this.delegateUI.installUI(c);
//            this.comboBox = (JComboBox) c;
//            this.comboBox.addPropertyChangeListener(this.propertyChangeHandler);
//            this.comboBox.getModel().addListDataListener(this.listDataHandler);
//            this.installKeyboardActions();
//
//            JPopupMenu popupMenu = fetchDelegatePopup();
//        }
//
//        private JPopupMenu fetchDelegatePopup() {
//            int index = 0;
//            Accessible child = this.delegateUI.getAccessibleChild(comboBox, index);
//            while (child != null) {
//                if (child instanceof JPopupMenu)
//                    return (JPopupMenu) child;
//
//                child = this.delegateUI.getAccessibleChild(comboBox, ++index);
//            }
//            return null;
//        }
//
//        public void uninstallUI(JComponent c) {
//            this.delegateUI.uninstallUI(c);
//            this.comboBox.getModel().removeListDataListener(this.listDataHandler);
//            this.maximumRowCount = 0;
//            // todo really do this
////            this.uninstallKeyboardActions();
//        }
//
//        public void setPopupVisible(JComboBox c, boolean v) {
//            if (v)
//                updateFilter("");
//
//            this.delegateUI.setPopupVisible(c, v);
//        }
//
//        public boolean isPopupVisible(JComboBox c) {
//            return this.delegateUI.isPopupVisible(c);
//        }
//
//        public boolean isFocusTraversable(JComboBox c) {
//            return this.delegateUI.isFocusTraversable(c);
//        }
//
//        public void paint(Graphics g, JComponent c) {
//            delegateUI.paint(g, c);
//        }
//
//        public void update(Graphics g, JComponent c) {
//            delegateUI.update(g, c);
//        }
//
//        public Dimension getPreferredSize(JComponent c) {
//            return delegateUI.getPreferredSize(c);
//        }
//
//        public Dimension getMinimumSize(JComponent c) {
//            return delegateUI.getMinimumSize(c);
//        }
//
//        public Dimension getMaximumSize(JComponent c) {
//            return delegateUI.getMaximumSize(c);
//        }
//
//        public boolean contains(JComponent c, int x, int y) {
//            return delegateUI.contains(c, x, y);
//        }
//
//        public int getAccessibleChildrenCount(JComponent c) {
//            return delegateUI.getAccessibleChildrenCount(c);
//        }
//
//        public Accessible getAccessibleChild(JComponent c, int i) {
//            return delegateUI.getAccessibleChild(c, i);
//        }
//
//        private class PropertyChangeHandler implements PropertyChangeListener {
//            public void propertyChange(PropertyChangeEvent evt) {
//                // if the ComboBoxModel changed, start listening for data changes in the new one
//                if ("model".equals(evt.getPropertyName())) {
//                    final ComboBoxModel oldModel = (ComboBoxModel) evt.getOldValue();
//                    final ComboBoxModel newModel = (ComboBoxModel) evt.getNewValue();
//
//                    oldModel.removeListDataListener(listDataHandler);
//                    newModel.addListDataListener(listDataHandler);
//                }
//            }
//        }
//
//        /**
//         * Updates the maximum number of rows to display in the combobox popup
//         * to be the minimum of either the item count or the maximum row count.
//         */
//        protected void updateMaximumRowCount() {
//            if (this.maximumRowCount > 0)
//                this.comboBox.setMaximumRowCount(Math.min(this.maximumRowCount, this.comboBox.getItemCount()));
//        }
//
//        protected void installKeyboardActions() {
//            final ActionMap actionMap = comboBox.getActionMap();
//            final Action delegateAction = actionMap.get("selectNext");
//            actionMap.put("selectNext", new DownAction(delegateAction));
//        }
//
////        protected ComboPopup createPopup() {
////            final BasicComboPopup popup = new BasicComboPopup( this.comboBox) {
////                /**
////                 * This method is only called when the arrow button which shows
////                 * and hides the popup is clicked. We exploit this fact and use
////                 * it as a hook to clear the filter which is filtering the drop
////                 * down. This emulates the behaviour of FireFox's URL selector.
////                 */
////                protected void togglePopup() {
////                    if (!isVisible())
////                        updateFilter("");
////
////                    super.togglePopup();
////                }
////
////                /**
////                 * Override the method which computes the bounds of the popup in
////                 * order to inject logic which sizes it more appropriately. The
////                 * preferred width of the popup is the maximum preferred width of
////                 * any item and the height is the combobox's maximum row count.
////                 *
////                 * @param px starting x location
////                 * @param py starting y location
////                 * @param pw starting width
////                 * @param ph starting height
////                 * @return a rectangle which represents the placement and size of the popup
////                 */
////                protected Rectangle computePopupBounds(int px, int py, int pw, int ph) {
////                    final Dimension popupSize = getPreferredPopupSize();
////                    final int maximumRowCount = this.comboBox.getMaximumRowCount();
////                    popupSize.setSize(popupSize.width, getPopupHeightForRowCount(maximumRowCount));
////
////                    if (this.comboBox.getSize().width-2 > popupSize.width)
////                        // use the combobox width (-2?) as a minimum width to respect
////                        popupSize.width = this.comboBox.getSize().width-2;
////                    else
////                        // pad 20 pixels wide for the popup scrollbar which might be shown
////                        popupSize.width += 20;
////
////                    return super.computePopupBounds(px, py, popupSize.width, popupSize.height);
////                }
////            };
////
////            popup.getAccessibleContext().setAccessibleParent(this.comboBox);
////            return popup;
////        }
//
//        /**
//         * This method listens the to the combo box model and updates the maximum
//         * row count of the combo box based on the new item count in the new model.
//         */
//        private class ListDataHandler implements ListDataListener {
//            public void contentsChanged(ListDataEvent e) {
//                // ensure this event doesn't simply indicate the selection changed
//                if (e.getIndex0() != -1 && e.getIndex1() != -1)
//                    updateMaximumRowCount();
//            }
//
//            public void intervalAdded(ListDataEvent e) {
//                updateMaximumRowCount();
//            }
//
//            public void intervalRemoved(ListDataEvent e) {
//                updateMaximumRowCount();
//            }
//        }
//
//        private class DownAction extends AbstractAction {
//            private final Action delegateAction;
//
//            public DownAction(Action delegateAction) {
//                this.delegateAction = delegateAction;
//            }
//
//            public Action getDelegateAction() {
//                return this.delegateAction;
//            }
//
//            public void actionPerformed(ActionEvent e) {
//                // if the this is going to cause the comboBox to the opened,
//                // filter the contents based on the last known prefix
//                if (!comboBox.isPopupVisible())
//                    updateFilter(prefix);
//
//                if (this.delegateAction != null) {
//                    ignoreFilterUpdates = true;
//                    try {
//                        this.delegateAction.actionPerformed(e);
//                    } finally {
//                        ignoreFilterUpdates = false;
//                    }
//                }
//            }
//        }
//    }

    /**
     * This UI Delegate exists in order to create a popup for the combobox that is
     * sized more appropriately. This UI Delegate calculates the preferred width of
     * the popup to be the maximum preferred width for all combobox selections.
     * This allows the selection to be rendered succinctly in the combobox, but
     * available selections to be rendered in a larger fashion in the popup. This
     * can be particularly useful for table cells using combobox table cell
     * editors.
     *
     * This UI Delegate also allows a maximum number of rows to be optionally
     * specified for the ComboBox Popup via the constructor. The popup may be
     * sized smaller than the maximum number of rows, if fewer combo box items
     * exist, but will never be larger.
     *
     * @author James Lemieux
     */
    private class DynamicPopupComboBoxUI extends MetalComboBoxUI {
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

        /** A listener which reacts to changes in the combo box model. */
        private ListDataListener listDataHandler = new ListDataHandler();

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
            this.comboBox.getModel ().addListDataListener(this.listDataHandler);
        }

        public void uninstallUI(JComponent c) {
            this.comboBox.getModel().removeListDataListener(this.listDataHandler);
            this.preferredPopupWidth = 0;
            this.maximumRowCount = 0;
            super.uninstallUI(c);
        }

        public PropertyChangeListener createPropertyChangeListener() {
            return new CustomPropertyChangeHandler( super.createPropertyChangeListener());
        }

        private class CustomPropertyChangeHandler implements PropertyChangeListener {
            private final PropertyChangeListener decorated;
            public CustomPropertyChangeHandler(PropertyChangeListener decorated) {
                this.decorated = decorated;
            }
            public void propertyChange(PropertyChangeEvent evt) {
                decorated.propertyChange(evt);

                // if the ComboBoxModel changed, start listening for data changes in the new one
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
            for(int i = 0; i < this.comboBox.getItemCount(); i++) {
                Component c = this.comboBox.getRenderer().getListCellRendererComponent(listBox, comboBox.getItemAt(i), 0, false, false);
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
            final Dimension size = this.comboBox.getPreferredSize ();
            return new Dimension(this.getPreferredPopupWidth(), size.height);
        }

        /**
         * Updates the maximum number of rows to display in the combobox popup
         * to be the minimum of either the item count or the maximum row count.
         */
        protected void updateMaximumRowCount() {
            if (this.maximumRowCount > 0)
                this.comboBox.setMaximumRowCount(Math.min(this.maximumRowCount, this.comboBox.getItemCount ()));
        }

        protected void installKeyboardActions() {
            super.installKeyboardActions();

            final ActionMap actionMap = comboBox.getActionMap();
            final Action delegateAction = actionMap.get("selectNext");
            actionMap.put("selectNext", new DownAction(delegateAction));
        }

        protected ComboPopup createPopup() {
            final BasicComboPopup popup = new CustomComboPopup( this.comboBox);
            popup.getAccessibleContext().setAccessibleParent(this.comboBox);
            return popup;
        }

        private class CustomComboPopup extends BasicComboPopup {
            public CustomComboPopup(JComboBox combo) {
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
                    updateFilter("");

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

                if (this.comboBox.getSize().width-2 > popupSize.width)
                    // use the combobox width (-2?) as a minimum width to respect
                    popupSize.width = this.comboBox.getSize ().width-2;
                else
                    // pad 20 pixels wide for the popup scrollbar which might be shown
                    popupSize.width += 20;

                return super.computePopupBounds (px, py, popupSize.width, popupSize.height);
            }
        }

        /**
         * This method listens the to the combo box model and updates the maximum
         * row count of the combo box based on the new item count in the new model.
         */
        private class ListDataHandler implements ListDataListener {
            public void contentsChanged(ListDataEvent e) {
                // ensure this event doesn't simply indicate the selection changed
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

        private class DownAction extends AbstractAction {
            private final Action delegateAction;

            public DownAction(Action delegateAction) {
                this.delegateAction = delegateAction;
            }

            public void actionPerformed(ActionEvent e) {
                // if the this is going to cause the comboBox to the opened,
                // filter the contents based on the last known prefix
                if (!comboBox.isPopupVisible())
                    updateFilter(prefix);

                if (this.delegateAction != null)
                    this.delegateAction.actionPerformed(e);
            }
        }
    }
}