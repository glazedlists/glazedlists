/**
 * O'Dell Business System 2
 * COPYRIGHT 2003 O'DELL ENGINEERING LTD.
 */
package com.odellengineeringltd.glazedtasks;

/**
 * A simple implementation of task. This provides a convenient base class
 * from which other tasks can be derived.
 *
 * @author <a href="mailto:jesse@odel.on.ca">Jesse Wilson</a>
 */
public abstract class AbstractTask implements Task {
    
    /** the task context for setting progress */
    protected TaskContext taskContext;
    
    /**
     * The task context is this task's access to the outside world.
     */
    public void setTaskContext(TaskContext taskContext) {
        this.taskContext = taskContext;
    }

    /**
     * Unset the task context when it becomes unavailable.
     */
    public void unsetTaskContext() { 
        this.taskContext = null;
    }
}
