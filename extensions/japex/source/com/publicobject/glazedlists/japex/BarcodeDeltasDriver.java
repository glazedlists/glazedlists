/* Glazed Lists                                                 (c) 2003-2005 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package com.publicobject.glazedlists.japex;

/**
 * @author <a href="mailto:jesse@swank.ca">Jesse Wilson</a>
 */
public class BarcodeDeltasDriver extends ListEventAssemblerDriver {

    public void initializeDriver() {
        System.setProperty("GlazedLists.ListEventAssemblerDelegate", "barcodedeltas");
    }
}