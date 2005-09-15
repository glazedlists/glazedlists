package ca.odell.glazedlists.demo.issuebrowser;

// GlazedLists
import ca.odell.glazedlists.*;
// Loading problems
import java.io.*;

/**
 * This loads issues by project as they are requested. When a new project is
 * requested, a working project may be interrupted. This may have violent side
 * effects such as InterruptedExceptions printed to the console by certain
 * XML parsing libraries that aren't exactly interruption friendly.
 *
 * <p>Issues are streamed to the issues list as they are loaded.
 *
 * @author <a href="jesse@odel.on.ca">Jesse Wilson</a>
 */
public class IssueLoader implements Runnable {
    
    private Project project = null;
    private Throbber throbber = null;
    private Thread issueLoaderThread = null;
    private EventList issuesList = null;
    
    public IssueLoader(EventList issuesList, Throbber throbber) {
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
    
    public void start() {
        issueLoaderThread = new Thread(this);
        // ensure the loader thread doesn't compete too aggressively with the EDT
        issueLoaderThread.setPriority(Thread.NORM_PRIORITY);
        issueLoaderThread.start();
    }

    public void run() {
        // loop forever, loading projects
        Project currentProject = null;
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
                IssuezillaXMLParser.loadIssues(issuesList, currentProject.getXMLUri());

                // stop the progress bar
                throbber.setOff();

            // handling interruptions is really gross
            } catch(IOException e) {
                if(e.getCause() instanceof InterruptedException) {
                    // do nothing, we were just interrupted as expected
                } else if(e.getMessage().equals("Parsing failed java.lang.InterruptedException")) {
                    // do nothing, we were just interrupted as expected
                } else {
                    e.printStackTrace();
                }
            } catch(RuntimeException e) {
                if(e.getCause() instanceof InterruptedException) {
                    // do nothing, we were just interrupted as expected
                } else if(e.getCause() instanceof IOException && e.getCause().getMessage().equals("Parsing failed Lock interrupted")) {
                    // do nothing, we were just interrupted as expected
                } else {
                    throw e;
                }
            } catch(InterruptedException e) {
                // do nothing, we were just interrupted as expected
            }
        }
    }
}

