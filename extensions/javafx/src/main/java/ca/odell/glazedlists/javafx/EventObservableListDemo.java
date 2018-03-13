/* Glazed Lists                                                 (c) 2003-2013 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.javafx;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.FilterList;
import ca.odell.glazedlists.SortedList;
import ca.odell.glazedlists.matchers.Matcher;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.util.Comparator;
import java.util.Timer;
import java.util.TimerTask;

/**
 * A simple demo application to show interaction between a GlazedList chain and
 * ObservableList. The demo application starts with a list of the values 0 - 9.
 * Then a new value is added every second. To show filtering, elements ending
 * with "5" (5, 15, etc) are filtered from the list.
 */
public class EventObservableListDemo extends Application {
    public static void main(String[] args) {
        Application.launch(EventObservableListDemo.class, args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        stage.setTitle("List Backed by GlazedLists");

        final EventList<String> base_list = new BasicEventList<>();
        for (int i = 0; i < 10; i++) {
            base_list.add(String.valueOf(i));
        }

        // Don't show 5's
        EventList<String> filtered_list = new FilterList<>(base_list, new Matcher<String>() {
            @Override
            public boolean matches(String item) {
                return !item.endsWith("5");
            }
        });

        // Reverse sort
        EventList<String> sorted_list = new SortedList<>(filtered_list,
                new Comparator<String>() {
                    @Override
                    public int compare(String o1, String o2) {
                        int i1 = Integer.parseInt(o1);
                        int i2 = Integer.parseInt(o2);
                        return i2 - i1; // REVERSE
                    }
                });
        ObservableList<String> model = new EventObservableList<>(sorted_list);

        final ListView<String> listView = new ListView<>(model);
        listView.setPrefSize(200, 250);
        listView.setEditable(false);

        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            int counter = 10;

            @Override
            public void run() {
                if (!Platform.isFxApplicationThread()) {
                    Platform.runLater(this);
                    return;
                }

                String value = String.valueOf(counter++);
                base_list.add(value);
                System.out.println("Added " + value);
            }
        }, 1000, 1000);

        StackPane root = new StackPane();
        root.getChildren().add(listView);
        stage.setScene(new Scene(root, 300, 250));
        stage.show();
    }
}
