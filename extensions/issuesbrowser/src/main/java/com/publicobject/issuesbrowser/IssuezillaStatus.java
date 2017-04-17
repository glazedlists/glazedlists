package com.publicobject.issuesbrowser;

import java.awt.Color;

/**
 * <code>IssuezillaStatus</code> enumerates all standard status values for the Issuezilla-System.
 *
 * @author Holger Brands
 */
public enum IssuezillaStatus implements Status {

    NEW(Color.RED),
    UNCONFIRMED(Color.BLUE),
    STARTED(Color.ORANGE),
    REOPENED(Color.MAGENTA),
    RESOLVED(Color.GREEN, false),
    VERIFIED(Color.PINK, false),
    CLOSED(Color.YELLOW, false);

    /** status color. */
    private Color color;

    /** indicates an issue, that is not yet done (resolved, verified or closed). */
    private boolean active;

    /**
     * Constructor with status color.
     *
     * @param color the status color
     */
    private IssuezillaStatus(Color color) {
        this(color, true);
    }

    /**
     * Constructor with status color and state.
     *
     * @param color the status color
     * @param active indicator, if status represents an issue that is not yet done
     */
    private IssuezillaStatus(Color color, boolean active) {
        this.color = color;
        this.active = active;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getId() {
        return name();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getName() {
        return name();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Color getColor() {
        return color;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isActive() {
        return active;
    }
}
