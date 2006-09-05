package com.publicobject.amazonbrowser;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.ObservableElementList;
import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.GlazedLists;

import javax.swing.event.SwingPropertyChangeSupport;
import java.beans.PropertyChangeListener;

public class TreeCriterion {

    public static final EventList<TreeCriterion> ALL_CRITERIA = new ObservableElementList<TreeCriterion>(new BasicEventList<TreeCriterion>(), GlazedLists.beanConnector(TreeCriterion.class));
    static {
        ALL_CRITERIA.add(new TitleCriterion());
        ALL_CRITERIA.add(new AudienceRatingCriterion());
        ALL_CRITERIA.add(new PriceCriterion());
        ALL_CRITERIA.add(new ReleaseDateCriterion());
    }

    private final SwingPropertyChangeSupport support = new SwingPropertyChangeSupport(this);

    private final String name;
    private boolean active;

    public TreeCriterion(String name, boolean active) {
        this.name = name;
        this.active = active;
    }

    public void addPropertyChangeListener(PropertyChangeListener l) {
        support.addPropertyChangeListener(l);
    }

    public void removePropertyChangeListener(PropertyChangeListener l) {
        support.removePropertyChangeListener(l);
    }

    public String getName() {
        return name;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        if (this.active == active) return;

        this.active = active;
        support.firePropertyChange("active", !active, active);
    }

    public String toString() {
        return name;
    }

    private static final class TitleCriterion extends TreeCriterion {
        public TitleCriterion() {
            super("Title", true);
        }
    }

    private static final class AudienceRatingCriterion extends TreeCriterion {
        public AudienceRatingCriterion() {
            super("Audience Rating", true);
        }
    }

    private static final class PriceCriterion extends TreeCriterion {
        public PriceCriterion() {
            super("Price", false);
        }
    }

    private static final class ReleaseDateCriterion extends TreeCriterion {
        public ReleaseDateCriterion() {
            super("Release Date", false);
        }
    }
}