/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package com.publicobject.misc.swing;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;

/**
 * Provide access to webstart services if they're available at runtime. If they're
 * not available, fail gracefully.
 *
 * @author <a href="mailto:jesse@swank.ca">Jesse Wilson</a>
 */
public class WebStart {

    private final Object basicService;
    private final Method showDocument;

    private WebStart(Object basicService, Method showDocument) {
        this.basicService = basicService;
        this.showDocument = showDocument;
    }

    public static WebStart tryCreate() {
        try {
            // prepare the basic service, this will fail if webstart isn't available
            Class serviceManagerClass = Class.forName("javax.jnlp.ServiceManager");
            Method lookupMethod = serviceManagerClass.getMethod("lookup", new Class[] { String.class });
            Object basicService = lookupMethod.invoke(null, new Object[] { "javax.jnlp.BasicService" });
            Class basicServiceClass = Class.forName("javax.jnlp.BasicService");
            Method showDocumentMethod = basicServiceClass.getMethod("showDocument", new Class[] { URL.class });
            return new WebStart(basicService, showDocumentMethod);
        } catch(ClassNotFoundException e) {
            return null;
        } catch(NoSuchMethodException e) {
            return null;
        } catch(IllegalAccessException e) {
            return null;
        } catch(InvocationTargetException e) {
            return null;
        }
    }

    public boolean openUrl(URL url) {
        try {
            showDocument.invoke(basicService, new Object[] { url });
            return true;
        } catch(IllegalAccessException e) {
            return false;
        } catch(InvocationTargetException e) {
            return false;
        }
    }
}