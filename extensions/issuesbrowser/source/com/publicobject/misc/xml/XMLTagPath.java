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
 * <p>An <code>XMLTagPath</code> represents either the start, end, body text
 * or attribute of an XML tag.
 *
 * <p>This class is intentionally immutable. All methods produce new
 * <code>XMLTagPath</code> objects.
 *
 * @author James Lemieux
 * @author <a href="mailto:jesse@swank.ca">Jesse Wilson</a>
 */
public final class XMLTagPath {

    /** A virtual attribute representing the tag opening */
    private static final String START = "_START";

    /** A virtual attribute representing the tag closing */
    private static final String END = "_END";

    /** A virtual attribute representing the tag body text */
    private static final String BODY = "_BODY";

    /** The list of tag names specifying the location of this XMLTagPath relative to the Document root. */
    private final List<String> path;

    /**
     * The name of an attribute within the tag, or the type of tag such
     * as {@link #START}, {@link #END} or {@link #BODY} if this is not
     * an attribute tag.
     */
    private final String attribute;

    /**
     * Constructs a new {@link XMLTagPath} representing the body text of
     * the specified root tag.
     */
    public XMLTagPath(String root) {
        this(Collections.singletonList(root), BODY);
    }

    /**
     * General constructor for an arbitrary tag path.
     *
     * @param path the sequence of nested tags defining this path.
     * @param attribute the XML attribute named by this path, or a virtual
     *      attribute such as {@link #START}, {@link #END} or {@link #BODY}.
     */
    private XMLTagPath(List<String> path, String attribute) {
        if (path == null) {
            throw new IllegalArgumentException("path must not be null");
        }
        if (attribute == null) {
            throw new IllegalArgumentException("attribute must not be null");
        }

        this.path = Collections.unmodifiableList(path);
        this.attribute = attribute;
    }

    /**
     * Returns A special {@link XMLTagPath} that does not contain any path. It can be
     * thought of as representing the entire Document.
     */
    static XMLTagPath emptyPath() {
        return new XMLTagPath(Collections.EMPTY_LIST, BODY);
    }

    /**
     * Produces a new XMLTagPath by appending the given <code>tag</code> to
     * this XMLTagPath. The attribute is kept the same.
     */
    public XMLTagPath child(String tag) {
        final List<String> newParts = new ArrayList<String>(path.size() + 1);
        newParts.addAll(path);
        newParts.add(tag);
        return new XMLTagPath(newParts, attribute);
    }

    /**
     * Produces a new XMLTagPath representing the given <code>attribute</code>
     * within the current tag.
     */
    public XMLTagPath attribute(String attribute) {
        if(this.attribute == attribute) return this;
        return new XMLTagPath(path, attribute);
    }
    
    /**
     * Produces a new XMLTagPath by changing the location field of this
     * XMLTagPath to be {@link #START}.
     */
    public XMLTagPath start() {
        return attribute(START);
    }

    /**
     * Produces a new XMLTagPath by changing the location field of this
     * XMLTagPath to be {@link #END}.
     */
    public XMLTagPath end() {
        return attribute(END);
    }

    /**
     * Produces a new XMLTagPath representing the body text for this tag.
     */
    public XMLTagPath body() {
        return attribute(BODY);
    }

    /**
     * The containing tag with the same attribute.
     */
    public XMLTagPath parent() {
        return new XMLTagPath(path.subList(0, path.size() - 1), attribute);
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;

        XMLTagPath that = (XMLTagPath) o;

        if(!attribute.equals(that.attribute)) return false;
        if(!path.equals(that.path)) return false;

        return true;
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        int result;
        result = path.hashCode();
        result = 31 * result + attribute.hashCode();
        return result;
    }

    /** @inheritDoc */
    @Override
    public String toString() {
        final StringBuffer formattedPath = new StringBuffer();

        for (Iterator i = path.iterator(); i.hasNext();) {
            formattedPath.append(i.next());
            if (i.hasNext()) formattedPath.append("/");
        }

        if(attribute == START) {
            formattedPath.append("[start]");
        } else if(attribute == BODY) {
            formattedPath.append("[body]");
        } else if(attribute == END) {
            formattedPath.append("[end]");
        } else {
            formattedPath.append("#").append(attribute);
        }

        return formattedPath.toString();
    }
}