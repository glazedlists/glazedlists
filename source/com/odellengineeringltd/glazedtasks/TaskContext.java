/**
 * Glazed Lists
 * http://glazedlists.dev.java.net/
 *
 * COPYRIGHT 2003 O'DELL ENGINEERING LTD.
 */
package com.odellengineeringltd.glazedtasks;

/**
 * A task context provides methods for a task to tell the outside
 * world about itself. All methods in TaskContext are safe to be called
 * from within a worker or Swing thread.
 *
 * @author <a href="mailto:jesse@odel.on.ca">Jesse Wilson</a>
 */
public class TaskContext {
    
    /** the task manager to notify of updates to the task */
    private TaskManager taskManager;
    
    /** the task that is viewing this context */
    private Task task;
    
    /** the task runner that is running this task */
    private TaskRunner taskRunner;
    
    /** the user state of this task */
    private boolean busy = false;
    private double progress = 0.0;
    private boolean cancellable = false;
    private String actionCaption = "";
    
    /** the context state of this task */
    private boolean taskComplete = false;

    /**
     * Creates a new progress bar task context that notifies the
     * specified task manager of updates to this task.
     */
    public TaskContext(TaskManager taskManager, Task task, TaskRunner taskRunner) {
        this.taskManager = taskManager;
        this.task = task;
        this.taskRunner = taskRunner;
    }

    /**
     * Sets the amount of progress this task has achieved so far, with a value
     * between 0.0 and 1.0. This is traditionally a strictly-increasing value
     * that is displayed as a progress bar. For tasks that have no predetermined
     * information on when they will be complete, they can use the setBusy()
     * method.
     */
    public void setProgress(double progress) {
        if(progress < 0.0 || progress > 1.0) throw new RuntimeException("Progress must be between 0.0 and 1.0");
        this.progress = progress;
        taskManager.taskUpdated(this);
    }
    
    /**
     * When a task is performing work with an indeterminite completion time
     * it should call setBusy() to notify the user that work is being
     * performed.
     */
    public void setBusy(boolean busy) {
        this.busy = busy;
        taskManager.taskUpdated(this);
    }
    
    /**
     * Enables or disables widgets allowing the user to cancel this task.
     * All tasks are cancellable until they are made not cancellable, so
     * for tasks that cannot be cancelled, setCancellable(false) should be
     * called in the task's setTaskContext() method.
     *
     * This method should only be called by the task itself and never by a
     * viewer of the task.
     */
    public void setCancellable(boolean cancellable) {
        this.cancellable = cancellable;
        taskManager.taskUpdated(this);
    }
    
    /**
     * Sets the description of the current action being performed at a micro
     * level. This is different from the toString() method in Task because
     * it is interested in the task at a much more momentary basis. Some
     * examples may be "Wrote 256 of 2028 bytes", "Connecting to database",
     * or "Processing query results".
     */
    public void setActionCaption(String actionCaption) {
        this.actionCaption = actionCaption;
        taskManager.taskUpdated(this);
    }
    
    /**
     * Tests if this task is currently busy.
     */
    public boolean isBusy() {
        return busy;
    }
    /**
     * Gets the progress of this task.
     */
    public double getProgress() {
        return progress;
    }
    
    /**
     * Gets the task that this task context is responsible to run.
     */
    public Task getTask() {
        return task;
    }

    /**
     * When a task runner finishes a task, it tells the task manager so that
     * the manager can clean up the task and possibly re-use the TaskRunner.
     * Implementors should strongly consider synchronizing this method as multiple
     * task runners may complete their tasks simultaneously.
     */
    public synchronized void taskComplete() {
        progress = 1.0;
        taskDone("Complete");
    }
    
    /**
     * When a task runner finishes a task that ended due to a cancellation.
     * Implementors should strongly consider synchronizing this method as multiple
     * task runners may complete their tasks simultaneously.
     */
    public synchronized void taskInterrupted(InterruptedException e) {
        e.printStackTrace();
        taskDone("Cancelled");
    }
    
    /**
     * When a task runner finishes a task that ended due to a failure.
     * Implementors should strongly consider synchronizing this method as multiple
     * task runners may complete their tasks simultaneously.
     */
    public synchronized void taskFailed(Exception e) {
        e.printStackTrace();
        taskDone("Failed");
    }
    
    /**
     * When a task completes this sets all the values to the appropriate
     * done values and returns resources to the task manager.
     */
    private void taskDone(String doneCaption) {
        // update the user state of the task
        busy = false;
        actionCaption = doneCaption;
        cancellable = false;
        
        // update the context state
        taskComplete = true;
        
        // notify the task manager
        taskManager.taskRunnerFree(taskRunner);
        taskManager.taskUpdated(this);
    }
    
    /**
     * Cancels this task. If the task has already completed this method will
     * silently do nothing.
     */
    public synchronized void cancelTask() {
        if(cancellable && !taskComplete) taskRunner.cancelTask();
    }
}
