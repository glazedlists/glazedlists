/* Glazed Lists                                                 (c) 2003-2014 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package com.publicobject.issuesbrowser.javafx;

import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.matchers.MatcherEditor;
import ca.odell.glazedlists.matchers.RangeMatcherEditor;

import com.publicobject.issuesbrowser.Issue;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.Slider;

/**
 * MatcherEditor which uses a slider for priority matching.
 *
 * @author Holger Brands
 */
public class PriorityMatcherEditor implements ChangeListener<Number> {

    private final Slider slider = new Slider();

    private final RangeMatcherEditor<Integer, Issue> rangeMatcherEditor;

    public PriorityMatcherEditor() {
        slider.valueProperty().addListener(this);
        rangeMatcherEditor = new RangeMatcherEditor<Integer, Issue>(
                GlazedLists.<Integer, Issue> filterator("priority.rating"));

        // priority slider
        slider.setMin(0);
        slider.setMax(100);
        slider.setShowTickMarks(true);
        slider.setSnapToTicks(true);
        slider.setMinorTickCount(0);
        slider.setBlockIncrement(25);
        slider.setMajorTickUnit(25);
        // issue https://javafx-jira.kenai.com/browse/RT-18448
        slider.setShowTickLabels(false);
//        slider.setLabelFormatter(new StringConverter<Double>() {
//
//            @Override
//            public String toString(Double priority) {
//                if (priority.intValue() == 0) {
//                    return "Low";
//                } else if (priority.intValue() == 100) {
//                    return "High";
//                }
//                return "";
//            }
//
//            @Override
//            public Double fromString(String value) {
//                return Double.valueOf(value);
//            }
//        });
    }

    public Slider getControl() {
        return slider;
    }

    public MatcherEditor<Issue> getMatcherEditor() {
        return rangeMatcherEditor;
    }

    @Override
    public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
        rangeMatcherEditor.setRange(newValue.intValue(), null);
    }
}