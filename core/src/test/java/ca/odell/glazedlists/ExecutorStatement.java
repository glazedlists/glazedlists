/* Glazed Lists                                                 (c) 2003-2013 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists;

import org.junit.runners.model.Statement;

import java.util.concurrent.Executor;

/**
 * A statement implementation that executes a given {@link Statement} with a
 * supplied {@link Executor}.
 *
 * @author Holger Brands
 */
public class ExecutorStatement extends Statement {

    private final Statement statement;
    private final Executor executor;

    private Throwable statementException;
    private Throwable invocationException;

    /**
     * Constructor with Statement and Executor.
     *
     * @param aExecutor the executor which executes the statement
     * @param aStatement the statement to run
     */
    public ExecutorStatement(Executor aExecutor, Statement aStatement) {
        executor = aExecutor;
        statement = aStatement;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void evaluate() throws Throwable {
        final Runnable runnable = new Runnable() {
            @Override
            public void run() {
                try {
                    statement.evaluate();
                } catch (Throwable e) {
                    statementException = e;
                }
            }
        };
        try {
            executor.execute(runnable);
        } catch (Throwable e) {
            invocationException = e;
        }
        // if an exception was thrown by the statement during evaluation,
        // then re-throw it to fail the test
        if (statementException != null) {
            throw statementException;
        }
        // if an exception was thrown by executing the runnable in the GUI thread,
        // then re-throw it to fail the test
        if (invocationException != null) {
            throw invocationException;
        }
    }
}