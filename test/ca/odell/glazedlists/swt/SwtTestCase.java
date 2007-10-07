/* Glazed Lists                                                 (c) 2003-2007 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/

package ca.odell.glazedlists.swt;

import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import junit.framework.TestCase;

/**
 * Baseclass for SWT testcases.
 * 
 * @author hbrands
 */
public class SwtTestCase extends TestCase {

    /** The display. */
    protected Display display;
    
    /** The shell. */
    protected Shell shell;
    
    /** {@inheritedDoc} */
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        display = new Display();
        shell = new Shell(display);
    }

    /** {@inheritedDoc} */
    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        display.dispose();
        assertTrue("Shell is not disposed!", shell.isDisposed());
    }
}
