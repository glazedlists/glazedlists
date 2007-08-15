/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package com.publicobject.filebrowser;


import java.io.File;
import java.util.Date;

/**
 * A wrapped file with cached meta-data.
 *
 * @author <a href="mailto:jesse@swank.ca">Jesse Wilson</a>
 */
public class Entry implements Comparable<Entry> {
    private final File file;
    private final boolean isDirectory;
    private final Entry parent;
    private final Date dateModified;
    private final Date dateCreated;
    private final long size;

    public Entry(File file, Entry parent) {
        this.file = file;
        this.isDirectory = file.isDirectory();
        this.parent = parent;

        this.dateModified = new Date(file.lastModified());
        this.dateCreated = new Date(file.lastModified());
        this.size = file.length();
    }

    public String toString() {
        return file.getName();
    }

    public String getName() {
        return file.getName();
    }

    public Date getDateModified() {
        return dateModified;
    }

    public Date getDateCreated() {
        return dateCreated;
    }

    public long getSize() {
        return size;
    }

    public String getKind() {
        return isDirectory ? "Directory" : "File";
    }

    public Entry getParent() {
        return parent;
    }

    public boolean isDirectory() {
        return isDirectory;
    }

    public File getFile() {
        return file;
    }

    public int compareTo(Entry other) {
        return file.compareTo(other.file);
    }
}
