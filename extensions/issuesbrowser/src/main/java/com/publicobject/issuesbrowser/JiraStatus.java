/* Glazed Lists                                                 (c) 2003-2011 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package com.publicobject.issuesbrowser;

import java.awt.Color;

/**
 * <code>JiraStatus</code> enumerates all standard status values for the Jira-System.
 *
 * @author Holger Brands
 */
public enum JiraStatus implements Status {

    OPEN("Open", Color.RED),
    IN_PROGRESS("In Progress", Color.ORANGE),
    REOPENED("Reopened", Color.MAGENTA),
    RESOLVED("Resolved", Color.GREEN, false),
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
    private JiraStatus(String name, Color color) {
        this(name, color, true);
    }

    /**
     * Constructor with status name.
     *
     * @param name the status name
     * @param color the status color
     */
    private JiraStatus(String name, Color color, boolean active) {
        this.name = name;
        this.color = color;
        this.active = active;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getId() {
        return getName();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getName() {
        return name;
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
