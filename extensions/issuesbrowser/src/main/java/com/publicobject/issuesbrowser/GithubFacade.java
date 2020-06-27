/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package com.publicobject.issuesbrowser;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;

import org.kohsuke.github.GHIssue;
import org.kohsuke.github.GHIssueState;
import org.kohsuke.github.GHMyself;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * A simple facade over the Github API for Java for the IssuesBrowser needs.
 *
 * @see <a href="https://github-api.kohsuke.org/">Github API for Java</a>
 * @author Holger Brands
 */
public class GithubFacade {

    private GitHub github;

    private GHMyself myself;

    private List<GHRepository> publicRepoList;

    private Map<String, GHIssue> currentIssuesMap = new HashMap<>();

    public List<GHRepository> getPublicRepoList() {
        return publicRepoList;
    }

    public List<String> getPublicRepoListNames() {
        return getPublicRepoList().stream().map(GHRepository::getFullName).collect(Collectors.toList());
    }

    /**
     * Default constructor establishes a connection to Github and authenticates via Github API for Java.
     * <p> Credentials are obtained from "~/.github" properties file or from the System Environment Properties.
     *
     * @see <a href="https://github-api.kohsuke.org/">Github API for Java</a>
     */
    public GithubFacade() {
        try {

            Logger logger = Logger.getLogger("org.kohsuke.github");
            logger.setLevel(Level.FINE);
            ConsoleHandler handler = new ConsoleHandler();
            // PUBLISH this level
            handler.setLevel(Level.FINE);
            logger.addHandler(handler);

            github = GitHub.connect();
            myself = github.getMyself();
            publicRepoList = myself.listRepositories(20, GHMyself.RepositoryListFilter.PUBLIC).toList();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /**
     * populate the given target list with converted Github issues represented by the given project.
     *
     * @param target issie list
     * @param project Github project
     */
    public void loadIssues(EventList<Issue> target, Project project) {
        currentIssuesMap.clear();
        Date loadingStarted = new Date();
        GithubIssueMapper mapper = new GithubIssueMapper(project);
        try {
            GHRepository repo = github.getRepository(project.getName());
            for (GHIssue ghIssue : repo.listIssues(GHIssueState.ALL)) {
                Issue newIssue = mapper.mapIssue(ghIssue, loadingStarted);
                target.add(newIssue);
                currentIssuesMap.put(newIssue.getId(), ghIssue);
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public void loadComments(Issue target, Project project) {
        GHIssue ghIssue = currentIssuesMap.get(target.getId());
        GithubIssueMapper mapper = new GithubIssueMapper(project);
        try {
            int commentsCount = ghIssue.getCommentsCount();
            if (commentsCount > 0 && target.getDescriptions().isEmpty()) {
                mapper.mapCommentsToDescriptions(ghIssue, target);
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static void main(String[] args) {
        GithubFacade github = new GithubFacade();
        List<String> reposNames = github.getPublicRepoListNames();
        if (!reposNames.isEmpty()) {
            String name = reposNames.get(0);
            Project project = new Project(name, "Test", IssueTrackingSystem.getGithub());
            EventList<Issue> targetList = new BasicEventList<>(100);
            github.loadIssues(targetList, project);
            System.out.println(targetList.size() + " issues loaded");

        }
    }
}
