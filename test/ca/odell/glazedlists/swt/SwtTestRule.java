/* Glazed Lists                                                 (c) 2003-2013 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.swt;

import ca.odell.glazedlists.ExecuteOnMainThread;
import ca.odell.glazedlists.ExecutorStatement;

import java.util.concurrent.Executor;

import org.eclipse.swt.widgets.Display;
import org.junit.Rule;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

/**
 * A {@link TestRule} implementation for use as {@link Rule}, that performs all
 * test methods on the Swt display thread. An exception to this are test methods
 * which are annotated with {@link ExecuteOnMainThread}.
 *
 * @author Holger Brands
 */
public class SwtTestRule implements TestRule {

    private SwtExecutor swtExecutor;

    /**
     * Creates a TestRule, which uses a {@link SwtExecutor} to perform statements.
     *
     * @param swtClassRule
     */
	public SwtTestRule(SwtClassRule swtClassRule) {
	    swtExecutor = new SwtExecutor(swtClassRule);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Statement apply(Statement statement, Description description) {
		if (description.getAnnotation(ExecuteOnMainThread.class) != null) {
			return statement;
		} else {
			return new ExecutorStatement(swtExecutor, statement);
		}
	}

    /**
     * {@link Executor} implementation that performs a {@link Runnable} on the
     * SWT display thread via {@link Display#syncExec(Runnable)}.
     *
     * @author hbrands
     */
	private static class SwtExecutor implements Executor {
	    private SwtClassRule swtClassRule;

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
