/* Glazed Lists                                                 (c) 2003-2005 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package com.publicobject.glazedlists.japex;

import com.sun.japex.TestCase;

/**
 * @author <a href="mailto:jesse@swank.ca">Jesse Wilson</a>
 */
public class ListEventDeltas2Driver extends ListEventAssemblerDriver {

    public void initializeDriver() {
        System.setProperty("GlazedLists.ListEventAssemblerDelegate", "deltas2");
    }

    public static void main(String[] args) {
        ListEventDeltas2Driver driver = new ListEventDeltas2Driver();
        driver.initializeDriver();
        driver.prepare(null);
        for(int i = 0; i < 20; i++) {
            driver.run(null);
        }
    }
}