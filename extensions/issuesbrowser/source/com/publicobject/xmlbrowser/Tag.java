/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package com.publicobject.xmlbrowser;

/**
 * Model an XML element.
 *
 * @author <a href="mailto:jesse@swank.ca">Jesse Wilson</a>
 */
public class Tag implements Comparable<Tag> {

    private Tag parent = null;
    private final String qName;
    private String text = "";

    public Tag(String qName) {
        this.parent = null;
        this.qName = qName;
        this.text = "";
    }

    public int compareTo(Tag o) {
        return qName.compareTo(o.qName);
    }

    public String getQName() {
        return qName;
    }

    public Tag getParent() {
        return parent;
    }

    public String getText() {
        return text.trim();
    }

    public Tag createChild(String qName) {
        Tag child = new Tag(qName);
        child.parent = this;
        return child;
    }

    public void append(String text) {
        this.text += text;
    }

    public String toString() {
        return qName;
    }
}
