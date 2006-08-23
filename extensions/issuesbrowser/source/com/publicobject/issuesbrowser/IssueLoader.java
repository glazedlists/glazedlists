/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package com.publicobject.issuesbrowser;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import com.publicobject.misc.Exceptions;
import com.publicobject.misc.Throbber;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.NoRouteToHostException;
import java.net.UnknownHostException;
import java.security.AccessControlException;

/**
 * This loads issues by project as they are requested. When a new project is
 * requested, a working project may be interrupted. This may have violent side
 * effects such as InterruptedExceptions printed to the console by certain
 * XML parsing libraries that aren't exactly interruption friendly.
 *
 * <p>Issues are streamed to the issues list as they are loaded.
 *
 * @author <a href="jesse@swank.ca">Jesse Wilson</a>
 */
public class IssueLoader implements Runnable {

    private Project project = null;
    private Throbber throbber = null;
    private Thread issueLoaderThread = null;
    private EventList<Issue> issuesList = null;

    public IssueLoader(EventList<Issue> issuesList, Throbber throbber) {
        this.issuesList = GlazedLists.threadSafeList(issuesList);
        this.throbber = throbber;
    }

    public void setProject(Project project) {
        synchronized(this) {
            this.project = project;
            issueLoaderThread.interrupt();
            notify();
        }
    }

    public void setFileName(String fileName) {
        synchronized(this) {
            this.project = new Project(null, fileName);
            this.project.setFileName(fileName);
            issueLoaderThread.interrupt();
            notify();
        }
    }

    public void start() {
        issueLoaderThread = new Thread(this, "Issue Loader Thread");
        // ensure the loader thread doesn't compete too aggressively with the EDT
        issueLoaderThread.setPriority(Thread.NORM_PRIORITY);
        issueLoaderThread.start();
    }

    public void run() {
        // loop forever, loading projects
        Project currentProject;
        while(true) {
            try {
                // get a project to load
                synchronized(this) {
                    if(project == null) wait();
                    Thread.interrupted();

                    // we should still be asleep
                    if(project == null) continue;

                    // we have a project to load
                    currentProject = project;
                    project = null;
                }

                // start the progress bar
                throbber.setOn();

                // load the issues
                issuesList.clear();
                if(currentProject.getFileName() != null) {
                    IssuezillaXMLParser.loadIssues(issuesList, new FileInputStream(currentProject.getFileName()), currentProject);
                } else {
                    IssuezillaXMLParser.loadIssues(issuesList, currentProject.getXMLUri(), currentProject);
                }

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

            } catch(InterruptedException e) {
                // do nothing, we were just interrupted as expected

            } finally {
                // stop the progress bar no matter what
                throbber.setOff();
            }
        }
    }
}