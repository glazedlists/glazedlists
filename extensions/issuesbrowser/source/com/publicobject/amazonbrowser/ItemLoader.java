/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package com.publicobject.amazonbrowser;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import com.publicobject.misc.Exceptions;

import javax.swing.*;
import java.io.IOException;
import java.net.NoRouteToHostException;
import java.net.UnknownHostException;
import java.security.AccessControlException;

public class ItemLoader implements Runnable {

    private String keywords = null;
    private Thread itemLoaderThread = null;
    private EventList<Item> itemsList = null;
    private final JProgressBar progressBar;

    public ItemLoader(EventList<Item> issuesList, JProgressBar progressBar) {
        this.itemsList = GlazedLists.threadSafeList(issuesList);
        this.progressBar = progressBar;
    }

    public void setKeywords(String keywords) {
        synchronized(this) {
            this.keywords = keywords;

            System.out.println("interrupting Item Loader Thread");
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
                    System.out.println("Item Loader Thread sleeping...");
                    if(keywords == null) wait();
                    System.out.println("Item Loader Thread awoke!");
                    if (Thread.interrupted()) {
                        System.out.println("cleared interrupted status in run() for " + Thread.currentThread().getName());
                        System.out.println(Thread.currentThread().getName() + " now says this about interrupted: " + Thread.currentThread().isInterrupted());
                    }

                    // we should still be asleep
                    if(keywords == null) continue;

                    // we have keywords to search with
                    currentKeywords = keywords;
                    keywords = null;
                }

                AmazonECSXMLParser.searchAndLoadItems(itemsList, currentKeywords, progressBar);

            // handling interruptions is really gross
            } catch (UnknownHostException e) {
                Exceptions.getInstance().handle(e);

            } catch (NoRouteToHostException e) {
                Exceptions.getInstance().handle(e);

            } catch (AccessControlException e) {
                Exceptions.getInstance().handle(e);

            } catch (IOException e) {
                System.out.println("IOException");
                if(e.getCause() instanceof InterruptedException) {
                    // do nothing, we were just interrupted as expected
                } else if(e.getMessage().equals("Parsing failed java.lang.InterruptedException")) {
                    // do nothing, we were just interrupted as expected
                } else {
                    Exceptions.getInstance().handle(e);
                }

            } catch (RuntimeException e) {
                System.out.println("RuntimeException");
                if(e.getCause() instanceof InterruptedException) {
                    // do nothing, we were just interrupted as expected
                } else if(e.getCause() instanceof IOException && e.getCause().getMessage().equals("Parsing failed Lock interrupted")) {
                    // do nothing, we were just interrupted as expected
                } else {
                    throw e;
                }

            } catch (InterruptedException e) {
                // do nothing, we were just interrupted as expected
                System.out.println("INTERRUPTED EXCEPTION DETECTED!");
            }
            System.out.println("LOOP");
        }
    }
}