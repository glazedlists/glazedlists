/* Glazed Lists                                                 (c) 2003-2014 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package com.publicobject.issuesbrowser.javafx;

import ca.odell.glazedlists.javafx.TextInputControlMatcherEditor;

import com.publicobject.issuesbrowser.Issue;
import com.publicobject.issuesbrowser.IssueTextFilterator;
import com.publicobject.issuesbrowser.Priority;
import com.publicobject.issuesbrowser.Status;

import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.util.Callback;

import java.text.DateFormat;
import java.util.Date;

/**
 * Factory for various JavaFx GUI components.
 *
 * @author Holger Brands
 */
public class ViewComponentFactory {

    private ViewComponentFactory() {
        // NOP
    }

    private static final Font TEXT_FONT = Font.font("Verdana", FontWeight.BOLD, -1);

    public static Text newFilterText() {
        final Text result = new Text("Filter Text");
        result.setFont(TEXT_FONT);
        return result;
    }

    public static TextInputControlMatcherEditor<Issue> newTextMatcherEditor() {
        return new TextInputControlMatcherEditor<>(new TextField(), new IssueTextFilterator());
    }

    public static Text newUsersText() {
        final Text result = new Text("Users");
        result.setFont(TEXT_FONT);
        return result;
    }

    public static Text newPriorityText() {
        final Text result = new Text("Minimum Priority");
        result.setFont(TEXT_FONT);
        return result;
    }
    public static Text newLowPriorityText() {
        final Text result = new Text("Low");
//        result.setFont(TEXT_FONT);
        return result;
    }

    public static Text newHighPriorityText() {
        final Text result = new Text("High");
//        result.setFont(TEXT_FONT);
        return result;
    }


    public static PriorityMatcherEditor newPriorityMatcherEditor() {
        return new PriorityMatcherEditor();
    }

    public static TableView<Issue> newIssueTable() {
        final TableView<Issue> result = new TableView<>();

        TableColumn<Issue, String> idCol = new TableColumn<>();
        idCol.setText("ID");
        idCol.setMinWidth(80);
        idCol.setResizable(false);
//        idCol.setSortable(false);
        idCol.setCellValueFactory(new PropertyValueFactory<Issue, String>("id"));

        TableColumn<Issue, String> typeCol = new TableColumn<>();
        typeCol.setText("Type");
        typeCol.setMinWidth(100);
        typeCol.setCellValueFactory(new PropertyValueFactory<Issue, String>("issueType"));

        TableColumn<Issue, Date> createdCol = new TableColumn<>();
        createdCol.setText("Created");
        createdCol.setMinWidth(80);
        createdCol.setCellValueFactory(new PropertyValueFactory<Issue, Date>("creationTimestamp"));
        createdCol.setCellFactory(newDateCellFactory(createdCol));

        TableColumn<Issue, Date> modifiedCol = new TableColumn<>();
        modifiedCol.setText("Modified");
        modifiedCol.setMinWidth(80);
        modifiedCol.setCellValueFactory(new PropertyValueFactory<Issue, Date>("deltaTimestamp"));
        modifiedCol.setCellFactory(newDateCellFactory(modifiedCol));

        TableColumn<Issue, Priority> prioCol = new TableColumn<>();
        prioCol.setText("Priority");
        prioCol.setMinWidth(50);
        prioCol.setResizable(false);
        prioCol.setCellValueFactory(new PropertyValueFactory<Issue, Priority>("priority"));

        TableColumn<Issue, Status> statusCol = new TableColumn<>();
        statusCol.setText("Status");
        statusCol.setMinWidth(80);
        statusCol.setCellValueFactory(new PropertyValueFactory<Issue, Status>("status"));

        TableColumn<Issue, String> resolutionCol = new TableColumn<>();
        resolutionCol.setText("Result");
        resolutionCol.setMinWidth(90);
        resolutionCol.setCellValueFactory(new PropertyValueFactory<Issue, String>("resolution"));

        TableColumn<Issue, String> summaryCol = new TableColumn<>();
        summaryCol.setText("Summary");
        summaryCol.setMinWidth(600);
        summaryCol.setCellValueFactory(new PropertyValueFactory<Issue, String>("shortDescription"));

        result.getColumns().addAll(idCol, typeCol, createdCol, modifiedCol, prioCol, statusCol, resolutionCol, summaryCol);
        return result;
    }


    private static Callback<TableColumn<Issue, Date>, TableCell<Issue, Date>> newDateCellFactory(
            TableColumn<Issue, Date> col) {
        return new Callback<TableColumn<Issue, Date>, TableCell<Issue, Date>>() {
            private final DateFormat dateFormatter = DateFormat.getDateInstance();
            @Override
            public TableCell<Issue, Date> call(TableColumn<Issue, Date> col) {
                return new TableCell<Issue, Date>() {
                    @Override
                    protected void updateItem(Date item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setText(null);
                        } else {
                          setText(format(item));
                        }
                    }

                    private String format(Date item) {
                        return dateFormatter.format(item);
                    }
                };
            }
        };
    }


}
