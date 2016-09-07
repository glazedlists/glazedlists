/* Glazed Lists                                                 (c) 2003-2013 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.swt;

import ca.odell.glazedlists.ExecuteOnNonUiThread;
import ca.odell.glazedlists.ExecutorStatement;

import org.eclipse.swt.widgets.Display;
import org.junit.Rule;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * A {@link TestRule} implementation for use as {@link Rule}, that performs all test methods on the SWT display thread. An exception to this
 * are test methods which are annotated with {@link ExecuteOnNonUiThread}, whcih run in a separate thread.
 *
 * @author Holger Brands
 */
public class SwtTestRule implements TestRule {

    private final SwtExecutor swtExecutor;

    private final Executor singleThreadExecutor = Executors.newSingleThreadExecutor();

    /**
     * Creates a TestRule, which uses a {@link SwtExecutor} to perform statements.
     *
     * @param swtClassRule the corresponding SWT class rule
     */
    public SwtTestRule(SwtClassRule swtClassRule) {
        swtExecutor = new SwtExecutor(swtClassRule);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Statement apply(Statement statement, Description description) {
        if (description.getAnnotation(ExecuteOnNonUiThread.class) != null) {
            return new ExecutorStatement(singleThreadExecutor, statement);
        } else {
            return new ExecutorStatement(swtExecutor, statement);
        }
    }

    /**
     * {@link Executor} implementation that performs a {@link Runnable} on the SWT display thread via {@link Display#syncExec(Runnable)}.
     *
     * @author hbrands
     */
    private static class SwtExecutor implements Executor {
        private final SwtClassRule swtClassRule;

        SwtExecutor(SwtClassRule aSwtClassRule) {
            swtClassRule = aSwtClassRule;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void execute(Runnable runnable) {
            swtClassRule.getDisplay().syncExec(runnable);
        }
    }
}
