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
 * @author <a href="mailto:jesse@odel.on.ca">Jesse Wilson</a>
 */
public class TaskRunner implements Runnable {
    
    /** the thread that this task runner uses most of the time */
    private Thread workerThread;
    
    /** the execute state decides what to do when run is called */
    private static final int IDLE = 0;
    private static final int EXECUTE_WORKER = 10;
    private static final int EXECUTE_EVENT_DISPATCH = 20;
    private int executeState;
    
    /** the current task to execute */
    private Task currentTask = null;
    private TaskContext currentTaskContext = null;
    
    /**
     * Creates a new TaskRunner that runs tasks on its internal thread.
     */
    public TaskRunner() {
        workerThread = new Thread(this, "Worker Thread");
        workerThread.start();
        
        executeState = IDLE;
    }
    
    /**
     * Starts the task runner to execute the specified task on its thread.
     * The task will be executed until complete or an error occurs.
     * The task runner may not be asked to run any more tasks until this
     * task is marked complete in the TaskManager.taskComplete() method.
     */
    public synchronized void runTask(Task task, TaskContext taskContext) {
        if(executeState != IDLE || currentTask != null) throw new RuntimeException("The task runner can not run a task when it is not idle!");
        
        // set the task and awaken the thread
        currentTask = task;
        currentTaskContext = taskContext;
        executeState = EXECUTE_WORKER;
        
        System.out.println("Interrupting worker thread");
        workerThread.interrupt();
    }
    
    /**
     * Cancels the currently executing task.
     *
     * This simply interrupts the task-running thread in hopes that it will
     * recognize that it has been cancelled and clean up after itself.
     */
    public synchronized void cancelTask() {
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
     * available, the task manager may notify this task runner before it has
     * returned to the idle state. This is all very unlikely but could cause
     * a task to be cancelled before it is executed. It should be fixed!
     */
    public void run() {
        while(true) {
            
            // always test to see if the current thread has been interrupted
            if(Thread.interrupted()) {
                TaskContext completedContext = currentTaskContext;
                executeState = IDLE;
                currentTask = null;
                currentTaskContext = null;
                completedContext.taskInterrupted(new InterruptedException());

            // when in the IDLE execution state, sleep until that state changes
            } else if(executeState == IDLE) {
                try {
                    while(true) workerThread.sleep(1000);
                } catch(InterruptedException e) {
                    // perhaps the execution state has changed!
                    System.out.println("worker thread interrupted");
                }

            // when in the EXECUTE_WORKER execution state, execute the current task on the worker thread
            } else if(executeState == EXECUTE_WORKER) {
                doTaskOnce();
                
            // when in the EXECUTE_EVENT_DISPATCH execution state, execute the current task on the Swing thread
            } else if(executeState == EXECUTE_EVENT_DISPATCH) {
                // just run it if this is the swing thread
                if(SwingUtilities.isEventDispatchThread()) {
                    doTaskOnce();
                    return;

                // if this is the worker thread, wait for the swing thread
                } else {
                    try {
                        SwingUtilities.invokeAndWait(this);
                    // when the task is cancelled during the event thread, ensure everything is cancelled
                    } catch(InterruptedException e) {
                        synchronized(this) {
                            if(currentTask == null && executeState == IDLE) {
                                continue;
                            } else {
                                TaskContext completedContext = currentTaskContext;
                                executeState = IDLE;
                                currentTask = null;
                                currentTaskContext = null;
                                completedContext.taskInterrupted(e);
                            }
                        }
                    // when running the task threw a runtime exception
                    } catch(InvocationTargetException e) {
                        throw new RuntimeException("Unexpected failure running task " + currentTask, e);
                    }
                }
            }
        }
    }
    
    /**
     * Executes the task on the current thread one time, and prepares the
     * execute state for the next execution.
     */
    private synchronized void doTaskOnce() {
        try {
            // execute the task
            int taskResult = currentTask.doTask();
            
            // prepare for being in the next state
            if(taskResult == Task.COMPLETE) {
                TaskContext completedContext = currentTaskContext;
                executeState = IDLE;
                currentTask = null;
                currentTaskContext = null;
                completedContext.taskComplete();
                
            // when the task has more work to do
            } else if(taskResult == Task.REPEAT_ON_WORKER_THREAD) {
                executeState = EXECUTE_WORKER;

            // when the task has work to do on the Swing thread
            } else if(taskResult == Task.REPEAT_ON_EVENT_DISPATCH_THREAD) {
                executeState = EXECUTE_EVENT_DISPATCH;
            }
            
        // when a task is cancelled, notify the task manager
        } catch(InterruptedException e) {
            TaskContext completedContext = currentTaskContext;
            executeState = IDLE;
            currentTask = null;
            currentTaskContext = null;
            completedContext.taskInterrupted(e);
            
        // when a task completes in error, notify the task manager
        } catch(Exception e) {
            TaskContext completedContext = currentTaskContext;
            executeState = IDLE;
            currentTask = null;
            currentTaskContext = null;
            completedContext.taskFailed(e);
        }
    }
}
