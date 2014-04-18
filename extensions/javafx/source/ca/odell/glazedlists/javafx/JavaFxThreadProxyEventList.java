/* Glazed Lists                                                 (c) 2003-2014 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.javafx;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.impl.gui.ThreadProxyEventList;

import javafx.application.Platform;

/**
 * Proxies events from all threads to the JavaFx application thread. This allows
 * any thread to write a source {@link EventList} that will be updated on the
 * JavaFX application thread.
 *
 * @author Holger Brands
 */
public class JavaFxThreadProxyEventList<E> extends ThreadProxyEventList<E> {

    public JavaFxThreadProxyEventList(EventList<E> source) {
        super(source);
    }

    @Override
    protected void schedule(Runnable runnable) {
        if (Platform.isFxApplicationThread()) {
            runnable.run();
        } else {
            Platform.runLater(runnable);
        }
    }
}
