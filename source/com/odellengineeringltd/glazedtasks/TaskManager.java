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
// for having lists of tasks, task runners and contexts
import java.util.*;

/**
 * A task manager provides an interface for tasks to be sheduled
 * for execution and for GUI progress bars to display the progress
 * of such tasks.
 *
 * @author <a href="mailto:jesse@odel.on.ca">Jesse Wilson</a>
 */
public class TaskManager {

    /** the contexts for the tasks */
    private List taskContexts = new ArrayList();
    
    /** the currently idle task runners */
    private List idleTaskRunners = new ArrayList();
    
    /** whether there are any tasks currently busy */
    private boolean busy = false;
    
    /** listeners to receive updates when tasks are updated. */
    private List taskListeners = new ArrayList();
    
    /** timer to notify when fixed-rate tasks need executing */
    private Timer timer = new Timer(true);
    
    /**
     * Default constructor.
     */
    public TaskManager() {
    }
    
    /**
     * Directs the task manager to execute the specified
     * task on an available thread and to display the task's progress
     * in a Swing progress bar.
     *
     * @return the task context of the newly running task. Use this to cancel
     *      the task if necessary.
     */
    public synchronized TaskContext runTask(Task task) {
        // get a task runner to run this task
        TaskRunner taskRunner;
        if(idleTaskRunners.size() > 0) {
            taskRunner = (TaskRunner)idleTaskRunners.remove(idleTaskRunners.size() - 1);
        } else {
            taskRunner = new TaskRunner();
        }
        
        // create a context for this task
        TaskContext context = new TaskContext(this, task, taskRunner);
        taskContexts.add(context);
        task.setTaskContext(context);
        
        // start this task
        taskRunner.runTask(task, context);
        
        // update the displayed progress bars
        taskUpdated(context);
        
        return context;
    }
    
    /**
     * Schedules the task manager to execute the specified task at
     * the specified fixed rate.
     *
     * @return a TimerTask (from java.util, not a Glazed Task) that can
     *      be cancelled if this task should no longer be scheduled.
     */
    public synchronized TimerTask scheduleTask(Task task, long period) {
        TimerTask reminder = new ReminderTimerTask(task);
        timer.scheduleAtFixedRate(reminder, 0, period);
        return reminder;
    }
    
    /**
     * A reminder timer task reminds the task manager to execute a
     * specified task when it is run. It should be run on a Timer
     * to enable the repeated execution of tasks that require
     * repetition.
     */
    class ReminderTimerTask extends TimerTask {

        /** the task to remind execution is needed */
        private Task task;
        /** the context of the currently running task */
        private TaskContext taskContext = null;
        
        /**
         * Creates a new reminder task that reminds the task
         * manager to execute the specified task.
         */
        public ReminderTimerTask(Task task) {
            this.task = task;
        }
        
        /**
         * Each time the reminder task is executed, it simply requests
         * that the task manager run its task.
         */
        public synchronized void run() {
            taskContext = runTask(task);
        }
        
        /**
         * When a reminder timer task is cancelled, it first cancels
         * the timer's repeated execution of the task. It then attempts
         * to cancel the task's current execution, if the task is currently
         * executing.
         */
        public synchronized boolean cancel() {
            boolean result = super.cancel();
            if(taskContext.isCancellable()) taskContext.cancelTask();
            return result;
        }
    }
    
    /**
     * When a task runner is finished running a task, the task runner
     * is re-added to the pool so that it can run more tasks when they
     * are needed.
     */
    public synchronized void taskRunnerFree(TaskRunner taskRunner) {
        idleTaskRunners.add(taskRunner);
    }
    
    /**
     * Gets all of the active task contexts as a list.
     */
    public synchronized List getTaskContexts() {
        return taskContexts;
    }

    /**
     * When a task is updated, the progress bar task context notifies the
     * manager so that it can update the GUI progress bar.
     */
    public synchronized void taskUpdated(TaskContext taskContext) {
        SwingUtilities.invokeLater(new TaskListenerNotifier(taskContext));
    }
    
    /**
     * A simple class that notifies task listeners. This executes
     * on the event dispatch thread.
     */
    class TaskListenerNotifier implements Runnable {
        
        /** the task that has been updated */
        private TaskContext task;
        
        /**
         * Creates a new TaskListenerNotifier that notifies listeners
         * that the specified task has been updated.
         */
        public TaskListenerNotifier(TaskContext task) {
            this.task = task;
        }
        
        /**
         * When the task manager is executed on the event dispatch thread,
         * it sends notifcation of update evenets to all listening TaskListeners.
         */
        public void run() {
            for(Iterator i = taskListeners.iterator(); i.hasNext(); ) {
                TaskListener taskListener = (TaskListener)i.next();
                taskListener.taskUpdated(task);
            }
        }
    }
    
    /**
     * Sets the specified listener to receive events when any task in this
     * manager is updated.
     */
    public void addTaskListener(TaskListener taskListener) {
        taskListeners.add(taskListener);
    }
}
