/**
 * Glazed Lists
 * http://glazedlists.dev.java.net/
 *
 * COPYRIGHT 2003 O'DELL ENGINEERING LTD.
 */
package com.odellengineeringltd.glazedtasks;

/**
 * A task is a brief routine that can be performed in the background thead
 * and makes progress towards eventual completion. The task may register its
 * current progress towards the goal to keep the user aware that the task
 * is completing. It may be cancelled by the user.
 *
 * A task has a private channel for logs, these logs can be viewed by the
 * user alongside the task itself.
 * 
 * A task is completed by either returning COMPLETE, or by throwing
 * an Exception.
 *
 * @author <a href="mailto:jesse@odel.on.ca">Jesse Wilson</a>
 */
public interface Task {
    
    /** the value the task should return when it is complete */
    public static final int COMPLETE = 0;
    /** the value the task should return when it needds to be called on any thread */
    public static final int REPEAT_ON_WORKER_THREAD = 10;
    /** the value the task should return when it needs to be called on the Swing thread */
    public static final int REPEAT_ON_EVENT_DISPATCH_THREAD = 20;
    
    /**
     * Before a task is started, it is provided with a task context. This is
     * the preferred way for the task to interact with the task's host.
     */
    public void setTaskContext(TaskContext taskContext);
    
    /**
     * After a task is complete, it will have its task context unset. After this
     * it is illegal for the task to call any methods on the task context, which
     * may be reused for another task.
     */
    public void unsetTaskContext();
    
    /**
     * Performs the action of the task. This method shall initially be called
     * using a worker thread, which is not allowed to interact with the user
     * interface. If it is ever necessary for this task to update the user
     * interface before continuing, the task shall return
     * REPEAT_ON_EVENT_DISPATCH_THREAD, and the doTask() method will be called
     * again on the event dispatch thread. For this to work successfully, the
     * task should keep track internally as to where it needs to resume work
     * on the user interface. When user interface work is complete, if the task
     * has additional background work to perform, it can return
     * REPEAT_ON_WORKER_THREAD. This will call the method yet again, but
     * using a worker thread as to not busy the user interface.
     *
     * If this task is cancelled, the thread.interrupt() method will be called.
     * The task can check if it has been interrupted by calling the static
     * method, Thread.interrupted(). If it is ever interrupted it should
     * throw a InterruptedException. The task should clean up any
     * necessary resources before throwing a InterruptedException, because
     * the task shall not be run again.
     *
     * If at any point the task fails, it should throw an exception and the
     * task will be stopped. The exception will be available to the user
     * for their information as to why the task has stopped.
     *
     * The call sequence parameter counts how many times the doTask method has
     * been called in this single execution of the task. For example, if you
     * have a task that performs background work and then GUI work and then
     * background work, the first time this method is called it will be called
     * with a background thread and the callSequence parameter will be 0. The
     * second time the method is called it will be called on the event dispatch
     * thread with a callSequence of 1. The third time the method is called it
     * will be called on a background thread with a callSequence of 2. This can
     * be used to separate your code into the segments that need to be executed
     * on different threads by using if/then/else logic on the callSequence
     * parameter.
     *
     * @param callSequence the number of previous calls made to doTask() in
     *      is execution of this task.
     */
    public int doTask(int callSequence) throws InterruptedException, Exception;
    
    /**
     * Gets the name of this task. The task name should be static and represent
     * what the overall goal of the task is. Examples: "Saving resume.txt" or
     * "Fetching contacts". The task name should be short and to the point.
     * It is possible to attach more verbose log messages using the
     * TaskContext's logger object.
     */
    public String toString();
}
