/* Glazed Lists                                                 (c) 2003-2013 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.swing;

import ca.odell.glazedlists.ExecuteOnMainThread;
import ca.odell.glazedlists.ExecutorStatement;

import java.awt.EventQueue;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.Executor;

import org.junit.Rule;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

/**
 * A {@link TestRule} implementation for use as {@link Rule}, that performs all
 * test methods on the Swing EDT thread. An exception to this are test methods
 * which are annotated with {@link ExecuteOnMainThread}.
 *
 * @author Holger Brands
 */
public class SwingTestRule implements TestRule {

    private final Executor swingExecutor = new SwingExecutor();

    /**
     * {@inheritDoc}
     */
	@Override
	public Statement apply(Statement statement, Description description) {
		if (description.getAnnotation(ExecuteOnMainThread.class) != null) {
			return statement;
		} else {
		    return new ExecutorStatement(swingExecutor, statement);
		}
	}

    /**
     * {@link Executor} implementation that performs a {@link Runnable} on the
     * Swing EDT via {@link EventQueue#invokeAndWait(Runnable)}. All catched
     * exceptions are wrapped as runtime exceptions and rethrown.
     *
     * @author hbrands
     */
	private static class SwingExecutor implements Executor {

	    /**
	     * {@inheritDoc}
	     */
	    @Override
	    public void execute(Runnable runnable) {
	        try {
	            EventQueue.invokeAndWait(runnable);

	        } catch (InterruptedException e) {
	            throw new RuntimeException(e);

	        } catch (InvocationTargetException e) {
	            Throwable rootCause = e;

	            // unwrap all wrapper layers down to the root problem
	            while (rootCause.getCause() != null) {
	                rootCause = rootCause.getCause();
	            }

	            if (rootCause instanceof RuntimeException) {
	                throw (RuntimeException) rootCause;
	            }

	            if (rootCause instanceof Error) {
	                throw (Error) rootCause;
	            }

	            // embed anything else as a RuntimeException
	            throw new RuntimeException(rootCause);
	        }
	    }
	}
}