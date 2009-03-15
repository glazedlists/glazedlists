/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package com.publicobject.issuesbrowser.swing;

import java.awt.Color;
import java.util.Hashtable;

import javax.swing.DefaultBoundedRangeModel;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.matchers.MatcherEditor;
import ca.odell.glazedlists.matchers.RangeMatcherEditor;

import com.publicobject.issuesbrowser.Issue;

/**
 * @author <a href="mailto:jesse@swank.ca">Jesse Wilson</a>
 */
public class PriorityMatcherEditor implements FilterComponent<Issue>, ChangeListener {

    private static final Integer MIN_PRIORITY = new Integer(0);
    private static final Integer MAX_PRIORITY = new Integer(100);

    private final RangeMatcherEditor<Integer,Issue> rangeMatcherEditor;
    private final JSlider slider;

    public PriorityMatcherEditor() {
        slider = new JSlider(new DefaultBoundedRangeModel(0, 0, 0, 100));
        slider.addChangeListener(this);

        rangeMatcherEditor = new RangeMatcherEditor<Integer,Issue>(GlazedLists.<Integer,Issue>filterator("priority.rating"));

        // priority slider
        slider.setOpaque(false);
        slider.setSnapToTicks(true);
        slider.setPaintLabels(true);
        slider.setPaintTicks(true);
        slider.setMajorTickSpacing(25);
        slider.setForeground(Color.BLACK);
        slider.setFocusable(false);

        final Hashtable<Integer,JLabel> prioritySliderLabels = new Hashtable<Integer,JLabel>();
        prioritySliderLabels.put(MIN_PRIORITY, new JLabel("Low"));
        prioritySliderLabels.put(MAX_PRIORITY, new JLabel("High"));
        slider.setLabelTable(prioritySliderLabels);
    }

    @Override
    public String toString() {
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
}