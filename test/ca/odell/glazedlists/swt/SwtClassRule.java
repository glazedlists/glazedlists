/* Glazed Lists                                                 (c) 2003-2013 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.swt;

import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.junit.ClassRule;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

/**
 * A {@link TestRule} that initializes the SWT environment (display and shell) on the main thread. Intended to be used as {@link ClassRule}
 * to perform the initialization once per test class.
 *
 * @author Holger Brands
 */
public class SwtClassRule implements TestRule {

    private final SwtHelper swtHelper = new SwtHelper();

    /**
     * {@inheritDoc}
     */
    @Override
    public Statement apply(final Statement base, Description description) {
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                swtHelper.init();
                try {
                    base.evaluate();
                } finally {
                    swtHelper.dispose();
                }
            }

        };
    }

    public final Display getDisplay() {
        return swtHelper.getDisplay();
    }

    public final Shell getShell() {
        return swtHelper.getShell();
    }

    /**
     * <code>SwtHelper</code> initializes a display and a shell. On MacOS they are created on the main thread. On any other OS they are
     * created in a separate thread.
     */
    static class SwtHelper {

        private Display display;

        private boolean displayOwner;

        private Shell shell;

        public void init() throws InterruptedException {
            if (display == null) {
                displayOwner = Display.getCurrent() == null;
                display = Display.getDefault();
            }
            shell = new Shell(display);
        }

        public Display getDisplay() {
            return display;
        }

        public Shell getShell() {
            return shell;
        }

        public void dispose() throws InterruptedException {
            if (shell != null) {
                shell.dispose();
                shell = null;
            }
            if (display != null && displayOwner) {
                display.dispose();
            }
            display = null;
        }
    }
}
