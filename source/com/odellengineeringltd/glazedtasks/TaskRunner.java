/**
 * Glazed Lists
 * http://glazedlists.dev.java.net/
 *
 * COPYRIGHT 2003 O'DELL ENGINEERING LTD.
 */
package com.odellengineeringltd.glazedtasks;

// for running tasks in the Swing thread
import javax.swing.SwingUtilities;
import java.lang.reflect.InvocationTargetException;

/**
 * A task runner owns a single thread and executes a sequence
 * of tasks as they are requested to be executed.
 *
 * This uses a set of variables to keep track of state and to prevent
 * a task from skipping execution or from not being cancelled correctly.
 *
 * The execute state tracks the next state that the worker thread will
 * be executing in. This will either be idle, executing a task, or waiting
 * for the swing thread to execute a task.
 *
 * The currentCallSequence is a count of how many times the current task
 * has had its doTask() method called. This is so that a task can be
 * executed with an increasing parameter in order for that task to tell
 * which stage it is in. Generally these calls alternate between executions
 * with the Swing thread and executions with the worker thread.
 *
 * The tasksStopped is a count of how many tasks have been stopped thus far.
 * A task may be stopped due to interruption/cancellation, completion or an
 * error within the task. The threads use the tasksStopped in order to verify
 * that a task has not been stopped on another thread while it was busy executing.
 *
 * @author <a href="mailto:jesse@odel.on.ca">Jesse Wilson</a>
 */
public class TaskRunner implements Runnable {
    
    /** the thread that this task runner uses most of the time */
    private Thread workerThread;
    
    /** the execute state decides what to do when run is called */
    private static final int READY = 0;
    private static final int EXECUTE_WORKER = 10;
    private static final int EXECUTE_EVENT_DISPATCH = 20;
    
    /** the current task to execute and its state */
    private Task currentTask = null;
    private TaskContext currentTaskContext = null;
    private int currentCallSequence = 0;
    private int executeState;
    private int tasksStopped = 0;
    
    /** the runnable to run the event thread events on */
    private Runnable eventThreadRunnable = new EventThreadRunnable();
    
    /**
     * Creates a new TaskRunner that runs tasks on its internal thread.
     */
    public TaskRunner() {
        workerThread = new Thread(this, "Worker Thread");
        workerThread.start();
        
        executeState = READY;
    }
    
    /**
     * Starts the task runner to execute the specified task on its thread.
     * The task will be executed until complete or an error occurs.
     * The task runner may not be asked to run any more tasks until this
     * task is marked complete in the TaskManager.taskComplete() method.
     */
    public synchronized void runTask(Task task, TaskContext taskContext) {
        if(executeState != READY || currentTask != null) throw new RuntimeException("The task runner can not run a task when it is not idle!");
        
        // set the task and awaken the thread
        currentTask = task;
        currentTaskContext = taskContext;
        currentCallSequence = 0;
        
        System.out.println("[tasks] Interrupting worker thread to run " + task);
        workerThread.interrupt();
    }
    
    /**
     * Cancels the currently executing task.
     *
     * This simply interrupts the task-running thread in hopes that it will
     * recognize that it has been cancelled and clean up after itself.
     */
    public synchronized void cancelTask() {
        if(executeState == READY || currentTask == null) throw new RuntimeException("The task runner can not cancel a task when it is idle!");

        System.out.println("[tasks] Cancelling " + currentTask);
        workerThread.interrupt();
    }
    
    /**
     * Executes this task. This method may be called by at most two
     * threads simultaneously. The method may be executed by the workerThread
     * that this instance owns, and by the swing thread.
     *
     * There is one potential bug where a task is acting on the event dispatch
     * thread, and that task completes. If the task completes on the event
     * dispatch thread, and then tells the manager that the task runner is
     * available, the task manager may interrupt this task runner before it has
     * returned to the idle state. This is all very unlikely but could cause
     * a task to be cancelled before it is executed. It should be fixed!
     */
    public void run() {
        while(true) {

            // fetch the execute state for this iteration
            int iterationState;
            int id;
            synchronized(this) {
                iterationState = executeState;
                id = tasksStopped;
            }
            
            // always start in the ready state and wait for an interrupted exception
            if(iterationState == READY) {
                try {
                    verifyNotInterrupted();
                    while(true) workerThread.sleep(1000);
                } catch(InterruptedException e) {
                    // the execution state has changed!
                }
                synchronized(this) {
                    executeState = EXECUTE_WORKER;
                }

            // when in the EXECUTE_WORKER execution state, execute the current task on the worker thread
            } else if(iterationState == EXECUTE_WORKER) {
                doTaskOnce();
                
            // when in the EXECUTE_EVENT_DISPATCH execution state, execute the current task on the Swing thread
            } else if(iterationState == EXECUTE_EVENT_DISPATCH) {
                try {
                    SwingUtilities.invokeAndWait(eventThreadRunnable);
                    verifyNotInterrupted();
                // when the task is cancelled during the event thread, ensure everything is cancelled
                } catch(InterruptedException e) {
                    taskStopped(e, id);
                // when running the task threw a runtime exception
                } catch(InvocationTargetException e) {
                    throw new RuntimeException("Unexpected failure running task " + currentTask, e);
                }
            }
        }
    }
    
    /**
     * Very simple runnable object for running tasks on the Swing thread.
     * When this is executed it simply directs the event dispatch thread
     * to execute the task once and return.
     */
    class EventThreadRunnable implements Runnable {
        public void run() {
            doTaskOnce();
        }
    }
    
    /**
     * Executes the task on the current thread one time, and prepares the
     * execute state for the next execution.
     */
    private void doTaskOnce() {
        // get the task to run and etcetera
        Task task;
        int callSequence;
        int id;
        synchronized(this) {
            task = currentTask;
            callSequence = currentCallSequence;
            id = tasksStopped;
        }

        try {
            // execute the task
            int taskResult = task.doTask(callSequence);
            
            // prepare for being in the next state
            synchronized(this) {
                // when this task was interrupted on the worker thread
                if(id != tasksStopped) {
                    return;

                // when the task is complete
                } else if(taskResult == Task.COMPLETE) {
                    taskStopped(null, id);
                    return;
                }
                
                // only after ensuring a task hasn't completed, ensure it hasn't been cancelled
                verifyNotInterrupted();
                
                // increment the call sequence for next time
                currentCallSequence++;
                    
                // when the task has more work to do
                if(taskResult == Task.REPEAT_ON_WORKER_THREAD) {
                    executeState = EXECUTE_WORKER;
    
                // when the task has work to do on the Swing thread
                } else if(taskResult == Task.REPEAT_ON_EVENT_DISPATCH_THREAD) {
                    executeState = EXECUTE_EVENT_DISPATCH;
                }
            }
            
        // when a task completes in error, notify the task manager
        } catch(Exception e) {
            taskStopped(e, id);
        }
    }
    
    /**
     * When a task is stopped due to completion, interruption of failure,
     * this method sets the task runner into the appropriate next state
     * and notifies the task context of the completion.
     *
     * This method is safe to call multiple times after a task completes.
     * In that case it will return silently after the TaskRunner has been
     * initially reset.
     * 
     * @param cause the exception that caused the task to stop. This will be
     *      null for tasks that completed successfully, an instance of
     *      InterruptedException for tasks that were interrupted, or another
     *      Exception for tasks that failed.
     * @param id the ID of the task that has been stopped. If this task has
     *      already been stopped, this method will return silently without
     *      stopping any new tasks.
     */
    private synchronized void taskStopped(Exception cause, int id) {
        // if this task has already stopped, return quietly
        if(id != tasksStopped) return;
        
        // reset the task runner's instance variables
        tasksStopped++;
        TaskContext completedContext = currentTaskContext;
        executeState = READY;
        currentTask = null;
        currentTaskContext = null;
        
        // notify the task context that the task has completed
        if(cause == null) {
            completedContext.taskComplete();
        } else if(cause instanceof InterruptedException) {
            completedContext.taskInterrupted((InterruptedException)cause);
        } else {
            completedContext.taskFailed(cause);
        }
    }
    
    /**
     * Ensures that the current thread has not been interrupted.
     * If it has, an InterruptedException will be thrown.
     */
    private void verifyNotInterrupted() throws InterruptedException {
        if(Thread.interrupted()) throw new InterruptedException();
    }
}
