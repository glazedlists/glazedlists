/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package com.publicobject.misc.xml;

import java.util.*;

/**
 * An XMLTagPath simply represents a position within an XML Document. Much like
 * a directory represents a logical position relative to the root of a filesystem:
 *
 * <p>\files\code\source\tests
 *
 * <p>an XMLTagPath represents a valid path of tags from the root of an XML
 * Document that specifies a position with that Document, such as:
 *
 * <ul>
 *   <li>&lt;customer&gt; &lt;fullname&gt; &lt;firstname&gt;
 *   <li>&lt;customer&gt; &lt;fullname&gt; &lt;intial&gt;
 *   <li>&lt;customer&gt; &lt;fullname&gt; &lt;lastname&gt;
 *   <li>&lt;customer&gt; &lt;fullname&gt;
 *   <li>&lt;customer&gt;
 * </ul>
 *
 * <p>within an XML Document like:
 *
 * <pre>
 *   &lt;customer&gt;
 *     &lt;fullname&gt;
 *       &lt;firstname&gt;James&lt;/firstname&gt;
 *       &lt;initial&gt;P&lt;/initial&gt;
 *       &lt;lastname&gt;Lemieux&lt;/lastname&gt;
 *     &lt;/fullname&gt;
 *   &lt;/customer&gt;
 * </pre>
 *
 * An XMLTagPath can also be used to represent the path to an attribute
 * within an opening XML tag. Attribute based XMLTagPath objects are only
 * created using {@link #attribute(String)}.
 *
 * <p>This class is intentionally immutable. All methods produce new XMLTagPath objects.
 *
 * @author James Lemieux
 */
public final class XMLTagPath {

    /** A special XMLTagPath representing the Document before even the first tag is processed. */
    private static final XMLTagPath NEW_PATH = new XMLTagPath(new String[0], null);

    /** Indicates the path represents the opening tag, like &lt;customer&gt; and not &lt;/customer&gt;. */
    public static final Object START_TAG = "Start ";

    /** Indicates the path represents the closing tag, like &lt;/customer&gt; and not &lt;customer&gt;. */
    public static final Object END_TAG = "End ";

    /** The list of tag names specifying the location of this XMLTagPath relative to the Document root. */
    private final List<String> parts;

    /** The optional name of an attribute within the tag. (Only applicable for start tags) */
    private final String attribute;

    /**
     * One of either {@link #START_TAG} or {@link #END_TAG} which indicates whether
     * the last entry in {@link #parts} refers to the opening or closing tag.
     */
    private final Object location;

    /**
     * A convenience constructor to work with parts as Arrays, internally.
     */
    private XMLTagPath(String[] parts, Object location) {
        this(parts == null ? null : Arrays.asList(parts), location);
    }

    /**
     * Constructs a new XMLTagPath where the given <code>parts</code> represent
     * a list of XML tag names starting with the root Document tag. The
     * <code>location</code> value describes whether the last part in the list
     * of <code>parts</code> refers to the open or close tag.
     *
     * @param parts a List of individual XML tag names in order of occurrence
     *      from the root Document tag
     * @param location one of either {@link #START_TAG} or {@link #END_TAG} which
     *      indicates whether the last entry in <code>parts</code> refers to
     *      the opening or closing tag.
     */
    public XMLTagPath(List<String> parts, Object location) {
        this(parts, location, null);
    }

    /**
     * Constructs a new XMLTagPath representing an attribute within a start
     * tag.
     *
     * @param parts a List of individual XML tag names in order of occurrence
     *      from the root Document tag
     * @param location one of either {@link #START_TAG} or {@link #END_TAG} which
     *      indicates whether the last entry in <code>parts</code> refers to
     *      the opening or closing tag.
     * @param attribute the name of the attribute within the specified tag
     */
    private XMLTagPath(List<String> parts, Object location, String attribute) {
        if (parts == null) {
            throw new IllegalArgumentException("parts may not be null");
        }
        if (location != START_TAG && attribute != null) {
            throw new IllegalArgumentException("only start tags may have attributes");
        }

        this.parts = Collections.unmodifiableList(parts);
        this.location = location;
        this.attribute = attribute;
    }

    /**
     * A factory method that allows the start tag path to be specified as a
     * single space delimited String.
     */
    public static XMLTagPath startTagPath(String spaceDelimitedPath) {
        return new XMLTagPath(spaceDelimitedPath.split(" "), START_TAG);
    }

    /**
     * A factory method that allows the end tag path to be specified as a
     * single space delimited String.
     */
    public static XMLTagPath endTagPath(String spaceDelimitedPath) {
        return new XMLTagPath(spaceDelimitedPath.split(" "), END_TAG);
    }

    /**
     * Returns A special XMLTagPath that does not contain any parts. It can be
     * thought of as representing the entire Document.
     */
    public static XMLTagPath newPath() {
        return NEW_PATH;
    }

    /**
     * Produces a new XMLTagPath by appending the given <code>part</code> to
     * this XMLTagPath.
     */
    public XMLTagPath child(String part) {
        final LinkedList<String> newParts = new LinkedList<String>(parts);
        newParts.add(part);
        return new XMLTagPath(newParts, location);
    }

    /**
     * Produces a new XMLTagPath representing the given <code>attribute</code>
     * within the current tag.
     */
    public XMLTagPath attribute(String attribute) {
        return new XMLTagPath(new LinkedList<String>(parts), location, attribute);
    }

    /**
     * Produces a new XMLTagPath by removing the last <code>part</code> from
     * this XMLTagPath.
     */
    public XMLTagPath parent() {
        final LinkedList<String> newParts = new LinkedList<String>(parts);
        newParts.removeLast();
        return new XMLTagPath(newParts, location);
    }

    /**
     * Produces a new XMLTagPath by changing the location field of this
     * XMLTagPath to be {@link #START_TAG}.
     */
    public XMLTagPath start() {
        return location == START_TAG ? this : new XMLTagPath(parts, START_TAG);
    }

    /**
     * Produces a new XMLTagPath by changing the location field of this
     * XMLTagPath to be {@link #END_TAG}.
     */
    public XMLTagPath end() {
        return location == END_TAG ? this : new XMLTagPath(parts, END_TAG);
    }

    /** @inheritDoc */
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final XMLTagPath xmlTagPath = (XMLTagPath) o;

        if (location != null ? !location.equals(xmlTagPath.location) : xmlTagPath.location != null) return false;
        if (attribute != null ? !attribute.equals(xmlTagPath.attribute) : xmlTagPath.attribute != null) return false;
        if (!parts.equals(xmlTagPath.parts)) return false;

        return true;
    }

    /** @inheritDoc */
    public int hashCode() {
        int result;
        result = parts.hashCode();
        result = 29 * result + (location != null ? location.hashCode() : 0);
        result = 29 * result + (attribute != null ? attribute.hashCode() : 0);
        return result;
    }

    /** @inheritDoc */
    public String toString() {
        final StringBuffer formattedPath = new StringBuffer(location.toString());

        for (Iterator i = parts.iterator(); i.hasNext();) {
            formattedPath.append(i.next());
            if (i.hasNext()) formattedPath.append(" / ");
        }

        // "$" delimits an attribute from the XML tags within an XMLTagPath
        if (attribute != null)
            formattedPath.append(" $ ").append(attribute);

        return formattedPath.toString();
    }
}