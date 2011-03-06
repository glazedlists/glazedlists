package com.publicobject.issuesbrowser;

import java.io.IOException;
import java.io.InputStream;

import ca.odell.glazedlists.EventList;

/**
 * <code>IssueTrackingSystem</code> singleton for easy switching of used issue tracking system.<p>
 * Currently supported are Jira and Issuzilla.
 *
 * @author Holger Brands
 */
public abstract class IssueTrackingSystem {

    /** default constructor. */
    IssueTrackingSystem() {
        // NOP
    }

    /**
     * Parses the XML document on the specified input stream into a List
     * of issues for the given project.
     * @param target the target list of issues
     * @param source the input stream to read from
     * @param owner the owning project
     * @throws IOException when XML parser reports error
     */
    public abstract void loadIssues(EventList<Issue> target, InputStream source, Project owner) throws IOException;

    /**
     * Parses the XML document obtained from the issue tracking system into a List
     * of issues for the given project.
     * @param target the target list of issues
     * @param owner the owning project
     * @throws IOException when XML parser reports error
     */
    public abstract void loadIssues(EventList<Issue> target, Project owner) throws IOException;

    /**
     * @return the supported stati of this issue tracking system
     */
    public abstract Status[] getSupportedStati();

    /**
     * Converts a status name to the cossesponding status instance.
     *
     * @param name the status name
     * @return the found status instance
     * @throws IllegalStateException if no status could be found for the given name
     */
    public Status statusFor(String name) {
        final Status[] supportedStati = getSupportedStati();
        for (Status status : supportedStati) {
            if (status.getName().equals(name)) {
                return status;
            }
        }
        throw new IllegalStateException("Unknown status name: " + name);
    }

    /**
     * @return the currently used issue tracking system
     */
    public static IssueTrackingSystem getInstance() {
//        return new Issuezilla();
        return new Jira();
    }

    /**
     * <code>Issuezilla</code> implementation.
     *
     * @author Holger Brands
     */
    private static class Issuezilla extends IssueTrackingSystem {
        @Override
        public Status[] getSupportedStati() {
            return IssuezillaStatus.values();
        }

        @Override
        public void loadIssues(EventList<Issue> target, InputStream source, Project owner)
                throws IOException {
            IssuezillaXMLParser.loadIssues(target, source, owner);
        }

        @Override
        public void loadIssues(EventList<Issue> target, Project owner) throws IOException {
            IssuezillaXMLParser.loadIssues(target, owner.getXMLUri(), owner);
        }
    }

    /**
     * <code>Jira</code> implementation.
     *
     * @author Holger Brands
     */
    private static class Jira extends IssueTrackingSystem {
        @Override
        public Status[] getSupportedStati() {
            return JiraStatus.values();
        }

        @Override
        public void loadIssues(EventList<Issue> target, InputStream source, Project owner)
                throws IOException {
            JiraXMLParser.loadIssues(target, source, owner);
        }

        @Override
        public void loadIssues(EventList<Issue> target, Project owner) throws IOException {
            JiraXMLParser.loadIssues(target, owner);
        }
    }
}
