/* Glazed Lists                                                 (c) 2003-2014 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package com.publicobject.issuesbrowser.javafx;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.javafx.EventObservableList;
import ca.odell.glazedlists.javafx.GlazedListsFx;
import ca.odell.glazedlists.matchers.Matcher;

import com.publicobject.issuesbrowser.Issue;
import com.publicobject.issuesbrowser.LabelsMatcherEditor;
import com.publicobject.issuesbrowser.StringValueMatcher;

import javafx.collections.ObservableList;
import javafx.scene.control.Control;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SelectionMode;

import java.util.List;

/**
 * A LabelsMatcherEditor with JavaFx support.
 *
 * @author Holger Brands
 */
public final class JavaFxLabelsMatcherEditor extends LabelsMatcherEditor {

    /** a widget for selecting labels. */
    private ListView<String> labelSelect;

    /** scroll through labels. */
    private ScrollPane scrollPane;

    /** JavaFx-Thread proxy list. */
    private EventList<String> threadProxyAllLabels;

    /** JAvaFX observable list adapted from source evetn list. */
    private ObservableList<String> observableAllLabels;

    /**
     * Builds a {@link ListView} of all labels related to issues. Based on the label selection, the
     * issue list will be filtered according to the label association.
     * @param source the issue list
     */
    public JavaFxLabelsMatcherEditor(EventList<Issue> source) {
        super(source);
        final EventList<String> allLabels = getLabelsList();
        threadProxyAllLabels = GlazedListsFx.threadProxyList(allLabels);
        observableAllLabels = new EventObservableList<>(threadProxyAllLabels);
        labelSelect = new ListView<>();
        labelSelect.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        labelSelect.setItems(observableAllLabels);
        scrollPane = new ScrollPane();
        scrollPane.setContent(labelSelect);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        getSelectedLabels().addListener(new LabelSelectionChangeListener());
    }

    /**
     * @return the currently selected labels
     */
    private ObservableList<String> getSelectedLabels() {
        return labelSelect.getSelectionModel().getSelectedItems();
    }

    /**
     * Get the widget for selecting labels.
     */
    public ListView<String> getLabelSelect() {
        return labelSelect;
    }

    /**
     * @return the {@link ListView} inside {@link ScrollPane} for display
     */
    public Control getControl() {
        return scrollPane;
    }

    /**
     * A listener to respond to changes in label selection from the ListView
     */
    private final class LabelSelectionChangeListener implements javafx.collections.ListChangeListener<String> {

        @Override
        public void onChanged(javafx.collections.ListChangeListener.Change<? extends String> c) {
            final List<String> selectedLabels = getSelectedLabels();
            // if we have all or no labels selected, match all labels
            if(selectedLabels.isEmpty() || selectedLabels.size() == observableAllLabels.size()) {
                fireMatchAll();
                return;
            }

            // match the selected subset of labels
            final StringValueMatcher newLabelMatcher = new StringValueMatcher(selectedLabels, Issue::getKeywords);

            // get the previous matcher. If it wasn't a user matcher, it must
            // have been an 'everything' matcher, so the new matcher must be
            // a constrainment of that
            final Matcher<Issue> previousMatcher = getMatcher();
            if(!(previousMatcher instanceof StringValueMatcher)) {
                fireConstrained(newLabelMatcher);
                return;
            }
            final StringValueMatcher previousUserMatcher = (StringValueMatcher) previousMatcher;

            // Figure out what type of change to fire. This is an optimization over
            // always calling fireChanged() because it allows the FilterList to skip
            // extra elements by knowing how the new matcher relates to its predecessor
            boolean relaxed = newLabelMatcher.isRelaxationOf(previousMatcher);
            boolean constrained = previousUserMatcher.isRelaxationOf(newLabelMatcher);
            if(relaxed && constrained) {
                return;
            }

            if(relaxed) {
                fireRelaxed(newLabelMatcher);
            } else if(constrained) {
                fireConstrained(newLabelMatcher);
            } else {
                fireChanged(newLabelMatcher);
            }
        }
    }
}
