/*
 * Copyright(c) 2002-2004, NEXVU Technologies
 * All rights reserved.
 *
 * Created: Feb 19, 2005 - 11:11:25 PM
 */
package ca.odell.glazedlists.matchers;

import ca.odell.glazedlists.Matcher;


/**
 * A {@link ca.odell.glazedlists.Matcher} that matches if a certain percentage of delegate
 * Matchers match. For example, if the 50% match is required and there are 4 delegate
 * Matchers, 2 must match for an item to match this object. If no delegate {@link Matcher
 * Matchers} are configured, this will always match.
 *
 * @author <a href="mailto:rob@starlight-systems.com">Rob Eden</a>
 */
public class PercentageMatcherSource extends AbstractMatcherSource {

	protected PercentageMatcherSource(Matcher initial_matcher) {
		super(initial_matcher);	// TODO: implement
	}

	//    private volatile double percentage_required;
//
//    /**
//     * {@inheritDoc}
//     *
//     * @param percentage_required See {@link #setPercentageRequired(double)}
//     */
//    public PercentageMatcherSource(double percentage_required, Matcher one, Matcher two) {
//        super(one, two);
//
//        setPercentageRequired(percentage_required);
//    }
//
//    /**
//     * {@inheritDoc}
//     *
//     * @param percentage_required See {@link #setPercentageRequired(double)}
//     */
//    public PercentageMatcherSource(double percentage_required, Matcher[] matchers) {
//        super(matchers);
//
//        setPercentageRequired(percentage_required);
//    }
//
//    /**
//     * {@inheritDoc}
//     *
//     * @param percentage_required See {@link #setPercentageRequired(double)}
//     */
//    public PercentageMatcherSource(double percentage_required, EventList matcher_list) {
//        super(matcher_list);
//
//        setPercentageRequired(percentage_required);
//    }
//
//
//    /**
//     * Set the percentage of delegate matches required for this object to be a match.
//     *
//     * @param percentage_required The percentage of delegates require, for 0 to 1.
//     */
//    public synchronized void setPercentageRequired(double percentage_required) {
//        if (percentage_required > 1.0 || percentage_required < 0.0) {
//            throw new IllegalArgumentException("Percentage required must be between 0.0 and 1.0");
//        }
//
//        double old_percentage_required = this.percentage_required;
//        this.percentage_required = percentage_required;
//
//        if (old_percentage_required > percentage_required)
//            fireRelaxed();
//        else
//            fireConstrained();
//    }
//
//    /**
//     * Get the percentage of delegate matches required for this object to be a match.
//     */
//    public double getPercentageRequired() {
//        return percentage_required;
//    }
//
//
//    public boolean matches(Object item) {
//        Matcher[] delegates = delegates();
//        if (delegates.length == 0) return true;     // always match if no delegates
//
//        int num_matches_required = (int) Math.round(delegates.length * percentage_required);
//
//        // This is a silly case, but if 0, require no matches...
//        if (num_matches_required == 0) return true;
//
//        int num_matches = 0;
//        for (int i = 0; i < delegates.length; i++) {
//            if (delegates[ i ].matches(item)) {
//                num_matches++;
//
//                if (num_matches > num_matches_required) return true;
//            }
//        }
//
//        return false;        // not enough matched
//    }
//
//    public void cleared(Matcher source) {
//        fireRelaxed();
//    }
//
//    public void changed(Matcher source) {
//        fireChanged();
//    }
//
//    public void constrained(Matcher source) {
//        fireConstrained();
//    }
//
//    public void relaxed(Matcher source) {
//        fireRelaxed();
//    }
}
