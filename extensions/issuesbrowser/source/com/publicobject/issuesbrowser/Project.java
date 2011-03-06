/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package com.publicobject.issuesbrowser;

// GlazedLists
import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;

/**
 * Models a project on Java.net.
 *
 * @author <a href="jesse@swank.ca">Jesse Wilson</a>
 */
public class Project {

    /** the sample projects */
    private static EventList<Project> projects;

    static {
		projects = new BasicEventList<Project>();
		projects.add(new Project("glazedlists", "Glazed Lists"));
		projects.add(new Project("glassfish", "Open Source Application Server for Java EE 6"));
		projects.add(new Project("javacc", "Java Compiler Compiler"));
		projects.add(new Project("eventbus", "Pub-sub Event broadcasting mechanism"));
		projects.add(new Project("jaxb", "The Standard Implementation for JAXB"));
		projects.add(new Project("jogl", "JOGL Java OpenGL Bindings"));
		projects.add(new Project("appframework", "Swing Application Framework"));
		projects.add(new Project("jdic", "JavaDesktop Integration Components"));
		projects.add(new Project("jdnc", "JavaDesktop Network Components"));
		projects.add(new Project("swingx", "SwingX"));
		projects.add(new Project("binding", "JGoodies Binding"));
    }

    private String projectName;
    private String projectTitle;
    private String fileName;

    public Project(String projectName, String projectTitle) {
        this.projectName = projectName;
        this.projectTitle = projectTitle;
    }

    public String getName() {
        return projectName;
    }

    public String getBaseUri() {
        return "https://" + projectName + ".dev.java.net";
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

    public String getXMLUri() {
        return getBaseUri() + "/issues/xml.cgi";
    }

    public boolean isValid() {
        return (projectName != null);
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
}