/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package com.publicobject.filebrowser;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.GlazedLists;

import java.io.File;

import com.publicobject.misc.util.concurrent.JobQueue;

/**
 * The model for our {@link FileBrowser}. This model uses a worker
 * thread of its own to do background file loading tasks.
 *
 * <p>Currently we aggressively loads all files, but eventually it would be nice
 * to lazily load files and populate them on demand.
 *
 * @author <a href="mailto:jesse@swank.ca">Jesse Wilson</a>
 */
public class FileBrowserModel implements Runnable {

    private Entry root;
    private EventList<Entry> files = GlazedLists.threadSafeList(new BasicEventList<Entry>());

    private JobQueue jobQueue = new JobQueue();

    public FileBrowserModel(String[] args) {
        String rootString = args.length > 0 ? args[0] : null;
        if (rootString == null) {
            rootString = System.getProperty("user.dir");
        }

        this.root = new Entry(new File(rootString), null);
    }

    @Override
    public void run() {
        jobQueue.invokeLater(new LoadChildren(root, files));
        jobQueue.run();
    }

    private class LoadChildren implements Runnable {
        private Entry parent;
        private EventList<Entry> sink;


        public LoadChildren(Entry entry, EventList<Entry> sink) {
            this.parent = entry;
            this.sink = sink;
        }

        @Override
        public void run() {
            File[] children = parent.getFile().listFiles();
            for(int f = 0; f < children.length; f++) {
                Entry childEntry = new Entry(children[f], parent);
                sink.add(childEntry);

                if(childEntry.isDirectory() && sink.size() < 10000) {
                    jobQueue.invokeLater(new LoadChildren(childEntry, sink));
                }
            }
        }
    }

    public EventList<Entry> getEntries() {
        return files;
    }

    public Entry getRoot() {
        return root;
    }

}
