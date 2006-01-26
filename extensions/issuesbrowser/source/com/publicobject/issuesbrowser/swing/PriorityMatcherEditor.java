/* Glazed Lists                                                 (c) 2003-2005 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package com.publicobject.issuesbrowser.swing;

import ca.odell.glazedlists.matchers.AbstractMatcherEditor;
import ca.odell.glazedlists.matchers.MatcherEditor;
import ca.odell.glazedlists.matchers.Matcher;
import ca.odell.glazedlists.swing.GlazedListsSwing;

import javax.swing.*;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;

import com.publicobject.issuesbrowser.Issue;

import java.util.Hashtable;
import java.awt.*;

/**
 * @author <a href="mailto:jesse@swank.ca">Jesse Wilson</a>
 */
public class PriorityMatcherEditor extends AbstractMatcherEditor implements FilterComponent<Issue>, ChangeListener {

    private JSlider slider;

    public PriorityMatcherEditor() {
        BoundedRangeModel model = new DefaultBoundedRangeModel(0, 0, 0, 100);
        slider = new JSlider(model);
        slider.addChangeListener(this);

        // priority slider
        slider.setOpaque(false);
        slider.setSnapToTicks(true);
        slider.setPaintLabels(true);
        slider.setPaintTicks(true);
        slider.setForeground(UIManager.getColor("Label.foreground"));
        slider.setMajorTickSpacing(25);
        slider.setForeground(Color.BLACK);
        
        Hashtable<Integer, JLabel> prioritySliderLabels = new Hashtable<Integer, JLabel>();
        prioritySliderLabels.put(new Integer(0), new JLabel("Low"));
        prioritySliderLabels.put(new Integer(100), new JLabel("High"));
        slider.setLabelTable(prioritySliderLabels);
    }

    public String getName() {
        return "Priority";
    }

    public JComponent getComponent() {
        return slider;
    }

    public MatcherEditor<Issue> getMatcherEditor() {
        return this;
    }

    public void stateChanged(ChangeEvent changeEvent) {
        fireChanged(new PriorityMatcher(slider.getValue()));
    }

    /**
     * Match issues by priority, on a scale from 0 to 100.
     */
    private static class PriorityMatcher implements Matcher<Issue> {
        final int minimumPriority;

        public PriorityMatcher(int minimumPriority) {
            this.minimumPriority = minimumPriority;
        }

        public boolean matches(Issue item) {
            Issue issue = (Issue)item;
            int priority = issue.getPriority().getRating();
            return priority >= minimumPriority;
        }
    }
}