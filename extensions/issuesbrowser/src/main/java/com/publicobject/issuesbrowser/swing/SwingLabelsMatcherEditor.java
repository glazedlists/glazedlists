/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package com.publicobject.issuesbrowser.swing;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.TransformedList;
import ca.odell.glazedlists.matchers.MatcherEditor;
import ca.odell.glazedlists.swing.AdvancedListSelectionModel;
import ca.odell.glazedlists.swing.DefaultEventListModel;
import ca.odell.glazedlists.swing.GlazedListsSwing;

import com.publicobject.issuesbrowser.Issue;
import com.publicobject.issuesbrowser.LabelsMatcherEditor;
import com.publicobject.misc.swing.NoFocusRenderer;

import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JScrollPane;

/**
 * A {@link LabelsMatcherEditor} with Swing support.
 *
 * @author Holger Brands
 */
class SwingLabelsMatcherEditor extends LabelsMatcherEditor implements FilterComponent<Issue> {

    /** a widget for selecting labels. */
    private JList<String> labelSelect;

    /** scroll through labels. */
    private JScrollPane scrollPane;

    /** ThreadProxyList for labels list */
    private TransformedList<String, String> allLabelsProxyList;

    /**
     * Create a filter list that filters the specified source list, which must contain only Issue
     * objects.
     */
    public SwingLabelsMatcherEditor(EventList<Issue> source) {
        super(source);

        // create a JList that contains components
        final EventList<String> allComponents = getLabelsList();
        allLabelsProxyList = GlazedListsSwing.swingThreadProxyList(allComponents);
        final DefaultEventListModel<String> labelsListModel = new DefaultEventListModel<>(allLabelsProxyList);
        labelSelect = new JList<>(labelsListModel);
        labelSelect.setPrototypeCellValue("jessewilson");
        labelSelect.setVisibleRowCount(10);
        // turn off cell focus painting
        labelSelect.setCellRenderer(new NoFocusRenderer<String>(labelSelect.getCellRenderer()));

        // create an EventList containing the JList's selection
        final AdvancedListSelectionModel<String> labelSelectionModel = GlazedListsSwing
                .eventSelectionModel(allLabelsProxyList);
        labelSelect.setSelectionModel(labelSelectionModel);
        setSelectionList(labelSelectionModel.getSelected());

        // scroll through selected labels
        scrollPane = new JScrollPane(labelSelect, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
    }

    /**
     * Get the widget for selecting labels.
     */
    public JList<String> getLabelSelect() {
        return labelSelect;
    }

    @Override
    public String toString() {
        return "Labels";
    }

    @Override
    public JComponent getComponent() {
        return scrollPane;
    }

    @Override
    public MatcherEditor<Issue> getMatcherEditor() {
        return this;
    }

    @Override
    public void dispose() {
        allLabelsProxyList.dispose();
        super.dispose();
    }
}