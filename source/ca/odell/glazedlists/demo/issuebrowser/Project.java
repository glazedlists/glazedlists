package ca.odell.glazedlists.demo.issuebrowser;

// GlazedLists

import ca.odell.glazedlists.*;
// For dates and lists
import java.util.*;

/**
 * Models a project on Java.net.
 *
 * @author <a href="jesse@odel.on.ca">Jesse Wilson</a>
 */
public class Project {

    /** the sample projects */
    private static EventList projects;
    static {
		projects = new BasicEventList();
		projects.add(new Project("glazedlists", "Glazed Lists"));
		projects.add(new Project("lg3d-core", "Project Looking Glass Core"));
		projects.add(new Project("java-net", "Java.net Watercooler"));
		projects.add(new Project("javacc", "Java Compiler Compiler"));
		projects.add(new Project("sqlexplorer", "SQLExplorer Eclipse Database Plugin"));
		projects.add(new Project("ofbiz", "Open For Business"));
		projects.add(new Project("jogl", "JOGL Java OpenGL Bindings"));
		projects.add(new Project("sip-communicator", "SIP Communicator"));
		projects.add(new Project("jdic", "JavaDesktop Integration Components"));
		projects.add(new Project("jdnc", "JavaDesktop Network Components"));
    }
    
    private String projectName = null;
    private String projectTitle = null;
    private String url = null;

    public Project(String projectName, String projectTitle) {
        this(projectName, projectTitle, "https://" + projectName + ".dev.java.net/issues/xml.cgi");
    }
    
    public Project(String projectName, String projectTitle, String url) {
        this.projectName = projectName;
        this.projectTitle = projectTitle;
        this.url = url;
    }

    public boolean isValid() {
        return (projectName != null);
    }

    public String getXMLUri() {
        return url;
    }

    public String toString() {
        return projectTitle;
    }
    
    /**
     * Get a list of all projects.
     */
    public static EventList getProjects() {
        return projects;
    }
}