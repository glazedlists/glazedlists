/* Glazed Lists                                                 (c) 2003-2014 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.javafx;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.TransformedList;

/**
 * A factory for creating all sorts of JavaFx related objects to be used with Glazed Lists.
 *
 * @author Holger Brands
 */
public class GlazedListsFx {

    /**
     * Wraps the source in an {@link EventList} that fires all of its update
     * events from the JavaFX-application thread.
     */
    public static <E> TransformedList<E, E> threadProxyList(EventList<E> source) {
        return new JavaFxThreadProxyEventList<>(source);
    }
}
