/* Glazed Lists                                                 (c) 2003-2007 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.calculation;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

class PropertyChangeCounter implements PropertyChangeListener {
    private int count;

    public void propertyChange(PropertyChangeEvent evt) {
        count++;
    }

    public int getCountAndReset() {
        int result = count;
        count = 0;
        return result;
    }
}