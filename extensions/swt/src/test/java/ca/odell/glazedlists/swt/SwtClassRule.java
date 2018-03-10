/* Glazed Lists                                                 (c) 2003-2013 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.swt;

import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.junit.Assume;
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
        ////////////////////
        // Disabling SWT tests on Mac because they explode due to these errors: (RE - 2018-03-09)
        //
        //   ***WARNING: Display must be created on main thread due to Cocoa restrictions.
        //
        //   org.eclipse.swt.SWTException: Invalid thread access
        //
        //       at org.eclipse.swt.SWT.error(Unknown Source)
        //       at org.eclipse.swt.SWT.error(Unknown Source)
        //       at org.eclipse.swt.SWT.error(Unknown Source)
        //       at org.eclipse.swt.widgets.Display.error(Unknown Source)
        //       at org.eclipse.swt.widgets.Display.createDisplay(Unknown Source)
        //       at org.eclipse.swt.widgets.Display.create(Unknown Source)
        //       at org.eclipse.swt.graphics.Device.<init>(Unknown Source)
        //       at org.eclipse.swt.widgets.Display.<init>(Unknown Source)
        //       at org.eclipse.swt.widgets.Display.<init>(Unknown Source)
        //       at org.eclipse.swt.widgets.Display.getDefault(Unknown Source)
        //       at ca.odell.glazedlists.swt.SwtClassRule$SwtHelper.init(SwtClassRule.java:69)
        //       at ca.odell.glazedlists.swt.SwtClassRule$1.evaluate(SwtClassRule.java:35)
        //       at org.junit.rules.RunRules.evaluate(RunRules.java:20)
        //       at org.junit.runners.ParentRunner.run(ParentRunner.java:309)
        //       at org.junit.runner.JUnitCore.run(JUnitCore.java:160)
        //       at com.intellij.junit4.JUnit4IdeaTestRunner.startRunnerWithArgs(JUnit4IdeaTestRunner.java:68)
        //       at com.intellij.rt.execution.junit.IdeaTestRunner$Repeater.startRunnerWithArgs(IdeaTestRunner.java:47)
        //       at com.intellij.rt.execution.junit.JUnitStarter.prepareStreamsAndStart(JUnitStarter.java:242)
        //       at com.intellij.rt.execution.junit.JUnitStarter.main(JUnitStarter.java:70)
        Assume.assumeFalse( "SWT tests are not supported on Mac",
            System.getProperty("os.name").toLowerCase().startsWith("mac"));

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
