/* Glazed Lists                                                 (c) 2003-2011 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package com.publicobject.issuesbrowser;

import java.awt.Color;

/**
 * <code>GithubStatus</code> enumerates all standard status values for the Github IssueTracker.
 *
 * @author Holger Brands
 */
public enum GithubStatus implements Status {

    OPEN("Open", Color.RED),
    CLOSED("Closed", Color.YELLOW, false);

    /** status name. */
    private String name;

    /** status color. */
    private Color color;

    /** indicates an issue, that is not yet done (resolved, verified or closed). */
    private boolean active;

    /**
     * Constructor with status name.
     *
     * @param name the status name
     * @param color the status color
     */
    private GithubStatus(String name, Color color) {
        this(name, color, true);
    }

    /**
     * Constructor with status name.
     *
     * @param name the status name
     * @param color the status color
     */
    private GithubStatus(String name, Color color, boolean active) {
        this.name = name;
        this.color = color;
        this.active = active;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getId() {
        return name();
//        return getName();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getName() {
        return name();
//        return name;
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
