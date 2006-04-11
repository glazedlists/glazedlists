/* Glazed Lists                                                 (c) 2003-2005 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package com.publicobject.glazedlists.japex;

/**
 * @author <a href="mailto:jesse@swank.ca">Jesse Wilson</a>
 */
public class TreeDeltasDriver extends ListEventAssemblerDriver {

    public void initializeDriver() {
        System.setProperty("GlazedLists.ListEventAssemblerDelegate", "treedeltas");
    }

    public static void main(String[] args) {
        TreeDeltasDriver driver = new TreeDeltasDriver();
        driver.initializeDriver();
        driver.prepare(null);
        for(int i = 0; i < 20; i++) {
            driver.run(null);
        }
    }
}