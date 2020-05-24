/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package com.publicobject.issuesbrowser;

import org.kohsuke.github.GHIssue;
import org.kohsuke.github.GHIssueComment;
import org.kohsuke.github.GHIssueState;
import org.kohsuke.github.GHLabel;
import org.kohsuke.github.GHMilestone;
import org.kohsuke.github.GHUser;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Date;
import java.util.stream.Collectors;

/**
 * A helper class to map Github issues to the IssuesBrowser {@link Issue} representation.
 *
 * @author Holger Brands
 */
public class GithubIssueMapper {

    private Project project;

    public GithubIssueMapper(Project project) {
        this.project = project;
    }

    public Issue mapIssue(GHIssue ghIssue, Date lastDateForStateChange) throws IOException {
        try {
            Issue result = new Issue(project);
            result.setId(Long.toString(ghIssue.getNumber()));
            result.setShortDescription(ghIssue.getTitle());
            result.setStatus(ghIssue.getState().name());
            result.setIssueType(ghIssue.isPullRequest() ? "PR" : "ISSUE");
            result.setPriority(Priority.P3);
            result.setCreationTimestamp(ghIssue.getCreatedAt());
            result.setDeltaTimestamp(ghIssue.getUpdatedAt());
            result.setReporter(ghIssue.getUser().getLogin());
            GHUser assignee = ghIssue.getAssignee();
            if (assignee != null) {
                result.setAssignedTo(assignee.getLogin());
            }

            GHMilestone ghMilestone = ghIssue.getMilestone();
            if (ghMilestone != null) {
                result.setTargetMilestone(ghMilestone.getTitle());
            }
            for (GHLabel label : ghIssue.getLabels()) {
                result.getKeywords().add(label.getName());
            }
            mapCommentsToDescriptions(ghIssue, result);
            // add activity for closed issues
            mapActivityForClose(ghIssue, result);

            // compute the timeline of state changes now that we have loaded the entire Issue
            result.getStateChanges().addAll(Issue.computeStateChanges(result, lastDateForStateChange));
            return result;
        } catch (Exception e) {
            System.err.println("Error while mapping issue:");
            e.printStackTrace();
            System.err.println(ghIssue);
            throw e;
        }
    }


    private static void mapCommentsToDescriptions(GHIssue fromIssue, Issue toIssue) throws IOException {
        toIssue.getDescriptions().addAll(
                fromIssue.getComments().stream()
                    .map(GithubIssueMapper::toDescription)
                    .collect(Collectors.toList()));
    }

    private static Description toDescription(GHIssueComment ghComment) {
        try {
            Description result = new Description();
            result.setText(ghComment.getBody());
            result.setWhen(ghComment.getCreatedAt());
            result.setWho(ghComment.getUser().getLogin());
            return result;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static void mapActivityForClose(GHIssue fromIssue, Issue toIssue) throws IOException {
        Date closedAt = fromIssue.getClosedAt();
        if (closedAt != null) {
            Activity closedActivity = new Activity("issue_status", GHIssueState.CLOSED.name());
            closedActivity.setWhen(closedAt);
            GHUser closedByUser = fromIssue.getClosedBy();
            if (closedByUser != null) {
                closedActivity.setUser(closedByUser.getLogin());
            }
            toIssue.getActivities().add(closedActivity);
        }
    }

}
