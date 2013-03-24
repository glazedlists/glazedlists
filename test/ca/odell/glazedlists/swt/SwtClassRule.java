/* Glazed Lists                                                 (c) 2003-2013 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.swt;

import java.util.concurrent.CountDownLatch;

import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.junit.ClassRule;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

/**
 * A {@link TestRule} that initializes the SWT environment (display and shell)
 * in a separate thread. Intended to be used as {@link ClassRule} to perform the
 * initialization once per test class.
 *
 * @author Holger Brands
 */
public class SwtClassRule implements TestRule {

    private SwtThread guiThread;

    /**
     * {@inheritDoc}
     */
    @Override
    public Statement apply(final Statement base, Description description) {
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                init();
                try {
                    base.evaluate();
                } finally {
                    dispose();
                }
            }

        };
    }

    private void init() throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);
        guiThread = new SwtThread(latch);
        guiThread.start();
        latch.await();
    }

    private void dispose() throws InterruptedException {
        guiThread.terminate();
        guiThread.join();
    }

    public final Display getDisplay() {
        return guiThread.getDisplay();
    }

    public final Shell getShell() {
        return guiThread.getShell();
    }

    private static class SwtThread extends Thread {
        private Display display;
        private Shell shell;
        private CountDownLatch latch;

        private volatile boolean stopped;

        public SwtThread(CountDownLatch latch) {
            super("SWT-Thread");
            this.latch = latch;
        }

        public Display getDisplay() {
            return display;
        }

        public Shell getShell() {
            return shell;
        }

        @Override
        public void run() {
            display = new Display();
            shell = new Shell(display);
            latch.countDown();
            while (!stopped) {
                if (!display.readAndDispatch()) {
                    display.sleep();
                }
            }
            display.dispose();
        }

        public void terminate() {
            stopped = true;
            display.wake();
        }
    }
}
