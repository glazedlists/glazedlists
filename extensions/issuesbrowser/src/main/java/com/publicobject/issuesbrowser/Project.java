/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package com.publicobject.issuesbrowser;

// GlazedLists
import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;

import com.publicobject.issuesbrowser.IssueTrackingSystem.Github;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Models a project on Java.net.
 *
 * @author <a href="jesse@swank.ca">Jesse Wilson</a>
 */
public class Project {

    /** the sample projects */
    private static EventList<Project> projects;

    static {
		projects = new BasicEventList<>();
		projects.add(new Project("glazedlists", "Glazed Lists", IssueTrackingSystem.getJavaNetJira()));
		projects.add(new Project("glassfish", "Glassfish - Open Source Application Server for Java EE 6", IssueTrackingSystem.getJavaNetJira()));
		projects.add(new Project("javacc", "Java Compiler Compiler", IssueTrackingSystem.getJavaNetJira()));
		projects.add(new Project("eventbus", "Eventbus - Pub-sub Event broadcasting mechanism", IssueTrackingSystem.getJavaNetJira()));
		projects.add(new Project("jaxb", "The Standard Implementation for JAXB", IssueTrackingSystem.getJavaNetJira()));
		projects.add(new Project("jogl", "JOGL Java OpenGL Bindings", IssueTrackingSystem.getJavaNetJira()));
		projects.add(new Project("appframework", "Swing Application Framework", IssueTrackingSystem.getJavaNetJira()));
		projects.add(new Project("jdic", "JavaDesktop Integration Components", IssueTrackingSystem.getJavaNetJira()));
		projects.add(new Project("jdnc", "JavaDesktop Network Components", IssueTrackingSystem.getJavaNetJira()));
		projects.add(new Project("swingx", "SwingX", IssueTrackingSystem.getJavaNetJira()));
		projects.add(new Project("binding", "JGoodies Binding", IssueTrackingSystem.getJavaNetJira()));
		projects.add(new Project("kenai", "Kenai (kenai.com)", IssueTrackingSystem.getKenaiJira()));
		projects.add(new Project("MNG", "Maven (apache.org)", IssueTrackingSystem.getApacheJira()));
//		projects.add(new Project("subclipse", "Subclipse (tigris.org)", IssueTrackingSystem.getTigrisIssuezilla()));

    }

    private IssueTrackingSystem owner;
    private String projectName;
    private String projectTitle;
    private String fileName;

    public Project(String projectName, String projectTitle, IssueTrackingSystem owner) {
        this.projectName = projectName;
        this.projectTitle = projectTitle;
        this.owner = owner;
    }

    /**
     * @return the project name
     */
    public String getName() {
        return projectName;
    }

    /**
     * @return the owning issue tracker
     */
    public IssueTrackingSystem getOwner() {
        return owner;
    }

    /**
     * @return returns the URL for displaying details of the given issue
     */
    public String getIssueDetailUri(Issue issue) {
        return getOwner().urlFor(issue);
    }

    /**
     * @return returns the query URL for returning issues as XML document
     */
    public String getIssueQueryUri() {
        return getOwner().queryUrlFor(this);
    }

    /**
     * Optional, the filename defines a file to load issues from rather than
     * a webservice.
     */
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
    public String getFileName() {
        return fileName;
    }

    public boolean isValid() {
        return (projectName != null) && (getOwner() != null);
    }

    @Override
    public String toString() {
        return projectTitle;
    }

    /**
     * Get a list of all projects.
     */
    public static EventList<Project> getProjects() {
        return projects;
    }

    /**
     * Get a list of all projects.
     */
    public static EventList<Project> getGithubProjects() {
        Github github = IssueTrackingSystem.getGithub();
        List<String> repoNames = github.getPublicRepoListNames();
        return repoNames.stream()
                .map(name -> new Project(name, name, github))
                .collect(Collectors.toCollection(BasicEventList::new));
    }
}