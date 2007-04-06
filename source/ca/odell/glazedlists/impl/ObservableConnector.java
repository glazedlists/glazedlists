/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.impl;

import ca.odell.glazedlists.ObservableElementList;

import java.util.EventListener;
import java.util.Observable;
import java.util.Observer;

/**
 * An {@link ObservableElementList.Connector} for the archaic {@link Observable}
 * base class which is rarely used in applications, but apparently used within
 * the <a href="http://eclipsetrader.sourceforge.net/">Eclipse Trader</a>
 * framework, which some Glazed Lists users are using.
 *
 * @author James Lemieux
 */
public class ObservableConnector<E extends Observable> implements ObservableElementList.Connector<E>, Observer, EventListener {

    /** The list which contains the elements being observed via this {@link ObservableElementList.Connector}. */
    private ObservableElementList<? extends E> list;

    /**
     * This method is called whenever the observed object is changed. It
     * responds by notifying the associated ObservableElementList that the given
     * {@link Observable} has been changed.
     *
     * @param o the Observable that has been updated
     * @param arg an argument passed to observers which is ignored here
     */
    public void update(Observable o, Object arg) {
        ((ObservableElementList) list).elementChanged(o);
    }

    /**
     * Start observing the specified <code>element</code>.
     *
     * @param element the element to be observed
     * @return the listener that was installed on the <code>element</code>
     *      to be used as a parameter to {@link #uninstallListener(Object, EventListener)}
     */
    public EventListener installListener(E element) {
        element.addObserver(this);
        return this;
    }

    /**
     * Stop observing the specified <code>element</code>.
     *
     * @param element the observed element
     * @param listener the listener that was installed on the <code>element</code>
     *      in {@link #installListener(Object)}
     */
    public void uninstallListener(E element, EventListener listener) {
        element.deleteObserver(this);
    }

    /** {@inheritDoc} */
    public void setObservableElementList(ObservableElementList<? extends E> list) {
        this.list = list;
    }
}