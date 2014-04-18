/* Glazed Lists                                                 (c) 2003-2014 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package com.publicobject.issuesbrowser.javafx;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.FilterList;
import ca.odell.glazedlists.UniqueList;
import ca.odell.glazedlists.javafx.EventObservableList;
import ca.odell.glazedlists.javafx.TextInputControlMatcherEditor;
import ca.odell.glazedlists.matchers.Matchers;

import com.publicobject.issuesbrowser.Issue;
import com.publicobject.issuesbrowser.IssueLoader;
import com.publicobject.issuesbrowser.Project;
import com.publicobject.misc.Throbber;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TableView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.util.List;

/**
 * JavaFx Issue Browser - work in progress...
 *
 * @author Holger Brands
 */
public final class IssueBrowser extends Application {
    /** an event list to host the issues */
    private UniqueList<Issue> issuesEventList = UniqueList.create(new BasicEventList<Issue>());

    /** loads issues as requested */
    private IssueLoader issueLoader = new IssueLoader(issuesEventList, new IndeterminateToggler());

    private TableView<Issue> issueTable;

    private DescriptionPanel descriptionArea;

    private JavaFxUsersMatcherEditor usersMatcherEditor = new JavaFxUsersMatcherEditor(issuesEventList);

    private FilterList<Issue> issuesUserFiltered = new FilterList<Issue>(issuesEventList, usersMatcherEditor);

    private FilterList<Issue> issuesTextFiltered = new FilterList<Issue>(issuesUserFiltered, Matchers.trueMatcher());

    private FilterList<Issue> issuesPriorityFiltered = new FilterList<Issue>(issuesTextFiltered);

    /** Adapter for EventList, input for issue table. */
    private EventObservableList<Issue> issuesObservableList = new EventObservableList<Issue>(issuesPriorityFiltered);

    public static void main(String[] args) {
        Application.launch(IssueBrowser.class, args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        final StackPane root = new StackPane();

        SplitPane splitPane1 = new SplitPane();
        splitPane1.setOrientation(Orientation.HORIZONTAL);
        splitPane1.setPrefSize(1024, 600);
        splitPane1.setDividerPosition(0, 0.3);

        SplitPane splitPane2 = new SplitPane();
        splitPane2.setOrientation(Orientation.VERTICAL);
        splitPane2.setPrefSize(512, 600);

        issueTable = ViewComponentFactory.newIssueTable();
        issueTable.setItems(issuesObservableList);

        final ScrollPane descScrollPane = new ScrollPane();
        descriptionArea = new DescriptionPanel();
        descScrollPane.setContent(descriptionArea.getWebView());
        descScrollPane.setFitToWidth(true);
        descScrollPane.setFitToHeight(true);
        splitPane2.getItems().addAll(issueTable, descScrollPane);

        final VBox filterBox = new VBox(10);
        filterBox.setPadding(new Insets(10));
        final Text filterText = ViewComponentFactory.newFilterText();
        final TextInputControlMatcherEditor<Issue> matcherEditor = ViewComponentFactory.newTextMatcherEditor();
        // @todo filter in background
        issuesTextFiltered.setMatcherEditor(matcherEditor);
//        issuesTextFiltered.setMatcherEditor(new ThreadedMatcherEditor<Issue>(matcherEditor));
        final Text usersText = ViewComponentFactory.newUsersText();

        final PriorityMatcherEditor priorityMatcherEditor = ViewComponentFactory.newPriorityMatcherEditor();
        issuesPriorityFiltered.setMatcherEditor(priorityMatcherEditor.getMatcherEditor());
        final Text priorityText = ViewComponentFactory.newPriorityText();
        final Text lowText = ViewComponentFactory.newLowPriorityText();
        final Text highText = ViewComponentFactory.newHighPriorityText();
        final BorderPane prioTickPane = new BorderPane();
        prioTickPane.setLeft(lowText);
        prioTickPane.setRight(highText);
        final VBox prioBox = new VBox();
        prioBox.getChildren().addAll(priorityMatcherEditor.getControl(), prioTickPane);

        filterBox.getChildren().addAll(filterText, matcherEditor.getTextControl(), priorityText,
                prioBox, usersText, usersMatcherEditor.getControl());

        VBox.setVgrow(usersMatcherEditor.getControl(), javafx.scene.layout.Priority.ALWAYS);

        splitPane1.getItems().addAll(filterBox, splitPane2);

        root.getChildren().add(splitPane1);

        initEventHandling();

        stage.setScene(new Scene(root, 1024, 600));
        stage.setTitle("Issues");
        stage.show();

        final Parameters parameters = getParameters();
        final List<String> args = parameters.getRaw();
        // Start the demo
        issueLoader.start();
        if ((args != null) && (args.size() == 2)) {
            issueLoader.fileBasedProject(args.get(0), args.get(1));
        } else {
            issueLoader.setProject(Project.getProjects().get(Project.getProjects().size()-1));
        }
    }

    private void initEventHandling() {
        descriptionArea.issueProperty().bind(issueTable.getSelectionModel().selectedItemProperty());
    }


    /**
     * Toggles the throbber on and off.
     */
    private static class IndeterminateToggler implements Runnable, Throbber {

        /** whether the throbber will be turned on and off */
        private boolean on = false;

        @Override
        public synchronized void setOn() {
            on = true;
            System.out.println("THROB ON");
        }

        @Override
        public synchronized void setOff() {
            on = false;
            System.out.println("THROB OFF");
        }

        @Override
        public synchronized void run() {
            if(on) {
                // TODO
                // throbber.setIcon(throbberActive);
            } else {
                // TODO
                // throbber.setIcon(throbberStatic);
            }
        }
    }

}
