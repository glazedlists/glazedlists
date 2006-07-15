/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package com.publicobject.amazonbrowser;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import com.publicobject.misc.Exceptions;
import com.publicobject.misc.Throbber;

import java.io.IOException;
import java.net.NoRouteToHostException;
import java.net.UnknownHostException;
import java.security.AccessControlException;

public class ItemLoader implements Runnable {

    private String keywords = null;
    private Throbber throbber = null;
    private Thread itemLoaderThread = null;
    private EventList<Item> itemsList = null;

    public ItemLoader(EventList<Item> issuesList, Throbber throbber) {
        this.itemsList = GlazedLists.threadSafeList(issuesList);
        this.throbber = throbber;
    }

    public void setKeywords(String keywords) {
        synchronized(this) {
            this.keywords = keywords;
            itemLoaderThread.interrupt();
            notify();
        }
    }

    public void start() {
        itemLoaderThread = new Thread(this, "Item Loader Thread");
        // ensure the loader thread doesn't compete too aggressively with the EDT
        itemLoaderThread.setPriority(Thread.NORM_PRIORITY);
        itemLoaderThread.start();
    }

    public void run() {
        // loop forever, loading items
        String currentKeywords;
        while (true) {
            try {
                // get a project to load
                synchronized (this) {
                    if(keywords == null) wait();
                    Thread.interrupted();

                    // we should still be asleep
                    if(keywords == null) continue;

                    // we have keywords to search with
                    currentKeywords = keywords;
                    keywords = null;
                }

                // start the progress bar
                throbber.setOn();

                // load the issues
                itemsList.clear();

                AmazonECSXMLParser.searchAndLoadItems(itemsList, currentKeywords);

            // handling interruptions is really gross
            } catch (UnknownHostException e) {
                Exceptions.getInstance().handle(e);

            } catch (NoRouteToHostException e) {
                Exceptions.getInstance().handle(e);

            } catch (AccessControlException e) {
                Exceptions.getInstance().handle(e);

            } catch (IOException e) {
                if(e.getCause() instanceof InterruptedException) {
                    // do nothing, we were just interrupted as expected
                } else if(e.getMessage().equals("Parsing failed java.lang.InterruptedException")) {
                    // do nothing, we were just interrupted as expected
                } else {
                    Exceptions.getInstance().handle(e);
                }

            } catch (RuntimeException e) {
                if(e.getCause() instanceof InterruptedException) {
                    // do nothing, we were just interrupted as expected
                } else if(e.getCause() instanceof IOException && e.getCause().getMessage().equals("Parsing failed Lock interrupted")) {
                    // do nothing, we were just interrupted as expected
                } else {
                    throw e;
                }

            } catch (InterruptedException e) {
                // do nothing, we were just interrupted as expected

            } finally {
                // stop the progress bar no matter what
                throbber.setOff();
            }
        }
    }
}