/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package com.publicobject.issuesbrowser.swing;

import ca.odell.glazedlists.matchers.MatcherEditor;
import ca.odell.glazedlists.matchers.RangeMatcherEditor;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.Filterator;
import ca.odell.glazedlists.EventList;

import javax.swing.*;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;

import com.publicobject.issuesbrowser.Issue;

import java.util.Hashtable;
import java.awt.*;

/**
 * @author <a href="mailto:jesse@swank.ca">Jesse Wilson</a>
 */
public class PriorityMatcherEditor implements FilterComponent<Issue>, ChangeListener {

    private static final Integer MIN_PRIORITY = new Integer(0);
    private static final Integer MAX_PRIORITY = new Integer(100);

    private final RangeMatcherEditor<Integer,Issue> rangeMatcherEditor;
    private final JSlider slider;

    public PriorityMatcherEditor(EventList<Issue> issues) {
        slider = new JSlider(new DefaultBoundedRangeModel(0, 0, 0, 100));
        slider.addChangeListener(this);

        rangeMatcherEditor = new RangeMatcherEditor<Integer,Issue>((Filterator)GlazedLists.filterator(new String[] { "priority.rating" }));

        // priority slider
        slider.setOpaque(false);
        slider.setSnapToTicks(true);
        slider.setPaintLabels(true);
        slider.setPaintTicks(true);
        slider.setMajorTickSpacing(25);
        slider.setForeground(Color.BLACK);
        
        final Hashtable<Integer,JLabel> prioritySliderLabels = new Hashtable<Integer,JLabel>();
        prioritySliderLabels.put(MIN_PRIORITY, new JLabel("Low"));
        prioritySliderLabels.put(MAX_PRIORITY, new JLabel("High"));
        slider.setLabelTable(prioritySliderLabels);
    }

    public String getName() {
        return "Priority";
    }

    public JComponent getComponent() {
        return slider;
    }

    public MatcherEditor<Issue> getMatcherEditor() {
        return rangeMatcherEditor;
    }

    public void stateChanged(ChangeEvent changeEvent) {
        rangeMatcherEditor.setRange(new Integer(slider.getValue()), null);
    }

    public void dispose() {
        // dispose
    }
}