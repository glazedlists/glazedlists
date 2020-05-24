/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package com.publicobject.issuesbrowser;

import ca.odell.glazedlists.CollectionList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.UniqueList;
import ca.odell.glazedlists.event.ListEvent;
import ca.odell.glazedlists.event.ListEventListener;
import ca.odell.glazedlists.matchers.AbstractMatcherEditor;
import ca.odell.glazedlists.matchers.Matcher;

/**
 * An LabelsMatcherEditor is a matcher editor that filters based on the selected
 * label.
 *
 * @author Holger Brands
 */
public class LabelsMatcherEditor extends AbstractMatcherEditor<Issue> {

    /** a list of labels */
    private CollectionList<Issue, String> labelForIssues;
    private UniqueList<String> allLabels;

    /** a list that maintains selection */
    private EventList<String> selectedLabels;

    /**
     * Create a filter list that filters the specified source list, which
     * must contain only Issue objects.
     */
    public LabelsMatcherEditor(EventList<Issue> source) {
        // create a unique label list from the source issues list
        labelForIssues = new CollectionList<>(source, Issue::getKeywords);
        allLabels = UniqueList.create(labelForIssues);
    }

    /**
     * Sets the selection driven EventList which triggers filter changes.
     */
    public void setSelectionList(EventList<String> labelsSelectedList) {
        this.selectedLabels = labelsSelectedList;
        labelsSelectedList.addListEventListener(new SelectionChangeEventList());
    }

    /**
     * Allow access to the unique list of labels
     */
    public EventList<String> getLabelsList() {
        return allLabels;
    }

    public void dispose() {
        allLabels.dispose();
        labelForIssues.dispose();
    }

    /**
     * An EventList to respond to changes in selection from the ListEventViewer.
     */
    private final class SelectionChangeEventList implements ListEventListener<String> {

        /** {@inheritDoc} */
        @Override
        public void listChanged(ListEvent<String> listChanges) {
            // if we have all or no labels selected, match all labels
            if(selectedLabels.isEmpty() || selectedLabels.size() == allLabels.size()) {
                fireMatchAll();
                return;
            }

            // match the selected subset of labels
            final StringValueMatcher newComponentMatcher = new StringValueMatcher(selectedLabels, Issue::getKeywords);

            // get the previous matcher. If it wasn't a component matcher, it must
            // have been an 'everything' matcher, so the new matcher must be
            // a constrainment of that
            final Matcher<Issue> previousMatcher = getMatcher();
            if(!(previousMatcher instanceof StringValueMatcher)) {
                fireConstrained(newComponentMatcher);
                return;
            }
            final StringValueMatcher previousUserMatcher = (StringValueMatcher)previousMatcher;

            // Figure out what type of change to fire. This is an optimization over
            // always calling fireChanged() because it allows the FilterList to skip
            // extra elements by knowing how the new matcher relates to its predecessor
            boolean relaxed = newComponentMatcher.isRelaxationOf(previousMatcher);
            boolean constrained = previousUserMatcher.isRelaxationOf(newComponentMatcher);
            if(relaxed && constrained) {
                return;
            }

            if(relaxed) {
                fireRelaxed(newComponentMatcher);
            } else if(constrained) {
                fireConstrained(newComponentMatcher);
            } else {
                fireChanged(newComponentMatcher);
            }
        }
    }
}