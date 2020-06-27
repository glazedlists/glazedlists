package com.publicobject.issuesbrowser;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.impl.Preconditions;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * <code>IssueTrackingSystem</code> is a facade for an issue tracking system.<p>
 * Currently supported are Jira and Issuzilla.
 *
 * @author Holger Brands
 */
public abstract class IssueTrackingSystem {

    private static IssueTrackingSystem javaNetJira = new Jira("java.net", "http://java.net/jira");

    private static IssueTrackingSystem kenaiJira = new Jira("Kenai", "http://kenai.com/jira");

    private static IssueTrackingSystem codehausJira = new Jira("Codehaus", "http://jira.codehaus.org");

    private static IssueTrackingSystem tigrisIssuezilla = new Issuezilla("Tigris", "http://@PROJECT@.tigris.org");

    private static IssueTrackingSystem apacheJira = new Jira("Apache", "http://issues.apache.org");

    private static Github github = new Github();

    private static IssueTrackingSystem[] knownIssueTrackers = {javaNetJira, kenaiJira, codehausJira, tigrisIssuezilla, apacheJira};

    private final String name;

    /**
     * Constructor with name.
     */
    IssueTrackingSystem(String name) {
        this.name = name;
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
     * Loads and maps the issue comments on demand when supported by the IssueTrackingSystem.
     * @param issue the issue
     * @param owner the owning project
     * @throws IOException
     * @throws UnsupportedOperationException when not supported
     */
    public abstract void loadComments(Issue issue, Project owner) throws IOException;

    /**
     * @return the supported stati of this issue tracking system
     */
    public abstract Status[] getSupportedStati();

    /**
     * @return name of this issue tracker
     */
    public String getName() {
        return name;
    }

    /**
     * Converts a status name to the cossesponding status instance.
     *
     * @param name the status name
     * @return the found status instance
     * @throws IllegalArgumentException if the status name is unknown
     */
    public Status statusFor(String name) {
        final Status[] supportedStati = getSupportedStati();
        for (Status status : supportedStati) {
            if (status.getName().equals(name)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown status name: " + name);
    }

    /**
     * Returns the URL for showing the details for the given issue.
     *
     * @param issue the issue
     * @return the issue URL
     */
    public String urlFor(Issue issue) {
        Preconditions.checkNotNull(issue, "Issue undefined");
        Preconditions.checkNotNull(issue.getProject(), "Project undefined");
        Preconditions.checkNotNull(issue.getProject().getOwner(), "IssueTracker undefined");
        Preconditions.checkArgument(issue.getProject().getOwner().equals(this), "Issue isn't owned by this issue tracker");

        return buildUrlFor(issue);
    }

    /**
     * Builds the URL for showing the details for the given issue.
     *
     * @param issue the issue
     * @return the issue URL
     */
    protected abstract String buildUrlFor(Issue issue);

    /**
     * Returns the issue query URL for the given project.
     *
     * @param project the project belonging to this issue tracker
     * @return the query URL
     */
    public String queryUrlFor(Project project) {
        Preconditions.checkNotNull(project, "Project undefined");
        Preconditions.checkNotNull(project.getOwner(), "IssueTracker undefined");
        Preconditions.checkArgument(project.getOwner().equals(this), "Issue isn't owned by this issue tracker");

        return buildQueryUrlFor(project);
    }

    /**
     * Builds the issue query URL for the given project.
     *
     * @param project the project belonging to this issue tracker
     * @return the query URL
     */
    protected abstract String buildQueryUrlFor(Project project);

    /**
     * @return the java.net Jira issue tracker
     */
    public static IssueTrackingSystem getJavaNetJira() {
        return javaNetJira;
    }

    /**
     * @return the Kenai Jira issue tracker
     */
    public static IssueTrackingSystem getKenaiJira() {
        return kenaiJira;
    }

    /**
     * @return the Codehaus Jira issue tracker
     */
    public static IssueTrackingSystem getCodehausJira() {
        return codehausJira;
    }

    /**
     * @return the Tigris Issuzilla issue tracker
     */
    public static IssueTrackingSystem getTigrisIssuezilla() {
        return tigrisIssuezilla;
    }

    /**
     * @return the Codehaus Jira issue tracker
     */
    public static IssueTrackingSystem getApacheJira() {
        return apacheJira;
    }

    /**
     * @return the Github IssueTracker
     */
    public static Github getGithub() {
        return github;
    }

    /**
     * Finds a supported isue tracker instance by name.
     *
     * @param name the name
     * @return the found issue tracker
     * @throws IllegalArgumentException if the name is unknown
     */
    public static IssueTrackingSystem findByName(String name) {
        Preconditions.checkNotNull(name, "Name undefined");
        for (IssueTrackingSystem issueTracker : knownIssueTrackers) {
            if (name.equals(issueTracker.getName())) {
                return issueTracker;
            }
        }
        throw new IllegalArgumentException("Unknown issue tracker name: " + name);
    }

    /**
     * <code>Issuezilla</code> implementation.
     *
     * @author Holger Brands
     */
    private static class Issuezilla extends IssueTrackingSystem {

        private static final String PROJECT_PLACEHOLDER = "@PROJECT@";

        private String baseUrlTemplate;

        /**
         * constructor with base URL template. It must contain the {@link #PROJECT_PLACEHOLDER}.
         *
         * @param name Name des IssueTrackers
         * @param baseUrlTemplate the base URL template, for example <code>http://@PROJECT@.tigris.org</code>
         */
        Issuezilla(String name, String baseUrlTemplate) {
            super(name);
            Preconditions.checkNotNull(baseUrlTemplate, "BaseUrl-Template undefined");
            Preconditions.checkArgument(baseUrlTemplate.contains(PROJECT_PLACEHOLDER), "BaseUrl-Template invalid");
            this.baseUrlTemplate = baseUrlTemplate;
        }

        /** {@inheritDoc} */
        @Override
        public Status[] getSupportedStati() {
            return IssuezillaStatus.values();
        }

        /** {@inheritDoc} */
        @Override
        public void loadIssues(EventList<Issue> target, InputStream source, Project owner)
                throws IOException {
            IssuezillaXMLParser.loadIssues(target, source, owner);
        }

        /** {@inheritDoc} */
        @Override
        public void loadIssues(EventList<Issue> target, Project owner) throws IOException {
            IssuezillaXMLParser.loadIssues(target, owner);
        }

        /** {@inheritDoc} */
        @Override
        public String buildUrlFor(Issue issue) {
            return baseUrlFor(issue.getProject()) + "/issues/show_bug.cgi?id=" + issue.getId();
        }

        /** {@inheritDoc} */
        @Override
        public String buildQueryUrlFor(Project project) {
            return baseUrlFor(project) +  "/issues/xml.cgi";
        }

        @Override
        public void loadComments(Issue issue, Project owner) throws IOException {
            // Not supported by Issuezilla
        }

        private String baseUrlFor(Project project) {
            return getBaseUrlTemplate().replace(PROJECT_PLACEHOLDER,
                    (project.getName() == null) ? "" : project.getName());
        }

        private String getBaseUrlTemplate() {
            return baseUrlTemplate;
        }
    }

    /**
     * <code>Jira</code> implementation.
     *
     * @author Holger Brands
     */
    private static class Jira extends IssueTrackingSystem {

        private static final int maxIssueCount = 100;

        private static final String JIRA_QUERY_URL_TEMPLATE =
            "/sr/jira.issueviews:searchrequest-xml/temp/SearchRequest.xml?jqlQuery=project+%3D+@PROJECT@+ORDER+BY+key+DESC&tempMax=" + maxIssueCount;

        private String baseUrl;

        /**
         * constructor with base URL
         *
         * @param name Name des IssueTrackers
         * @param baseUrl the base URL, for example <code>http://java.net/jira</code>
         */
        Jira(String name, String baseUrl) {
            super(name);
            this.baseUrl = baseUrl;
        }

        private String getBaseUrl() {
            return baseUrl;
        }

        /** {@inheritDoc} */
        @Override
        public Status[] getSupportedStati() {
            return JiraStatus.values();
        }

        /** {@inheritDoc} */
        @Override
        public String buildUrlFor(Issue issue) {
            return getBaseUrl() + "/browse/" + issue.getId();
        }

        /** {@inheritDoc} */
        @Override
        public String buildQueryUrlFor(Project project) {
            final String queryUrl = JIRA_QUERY_URL_TEMPLATE.replace("@PROJECT@", project.getName());
            return getBaseUrl() + queryUrl;
        }

        /** {@inheritDoc} */
        @Override
        public void loadIssues(EventList<Issue> target, InputStream source, Project owner)
                throws IOException {
            JiraXMLParser.loadIssues(target, source, owner);
        }

        /** {@inheritDoc} */
        @Override
        public void loadIssues(EventList<Issue> target, Project owner) throws IOException {
            JiraXMLParser.loadIssues(target, owner);
        }

        @Override
        public void loadComments(Issue issue, Project owner) throws IOException {
            // Not supported by JIRA
        }
    }

    public static class Github extends IssueTrackingSystem {

        private static final String QUERY_URL = "/issues?q=is%3Aissue";
        private String baseUrl;
        private GithubFacade githubFacade;

        Github() {
            super("Github");
            baseUrl = "https://github.com/";
            githubFacade = new GithubFacade();
        }

        private String getBaseUrl() {
            return baseUrl;
        }

        public List<String> getPublicRepoListNames() {
            return githubFacade.getPublicRepoListNames();
        }

        @Override
        public void loadIssues(EventList<Issue> target, InputStream source, Project owner) throws IOException {
            loadIssues(target, owner);
        }

        @Override
        public void loadIssues(EventList<Issue> target, Project owner) throws IOException {
            githubFacade.loadIssues(target, owner);
        }

        @Override
        public void loadComments(Issue target, Project owner) throws IOException {
            githubFacade.loadComments(target, owner);
        }

        @Override
        public Status[] getSupportedStati() {
            return GithubStatus.values();
        }

        @Override
        protected String buildUrlFor(Issue issue) {
            return getBaseUrl() + issue.getProject().getName() + "/issues/" + issue.getId();
        }

        @Override
        protected String buildQueryUrlFor(Project project) {
            return getBaseUrl() + project.getName() + QUERY_URL;
        }

    }
}
