/**
 * O'Dell Business System 2
 * COPYRIGHT 2003 O'DELL ENGINEERING LTD.
 */
package com.odellengineeringltd.glazedtasks.jprogressbar;

// the glazed tasks base
import com.odellengineeringltd.glazedtasks.*;
// for running tasks in the Swing thread
import javax.swing.SwingUtilities;
// for having a progress bar
import javax.swing.JProgressBar;
// for lists of task contexts
import java.util.*;

/**
 * The single progress bar that receives updates from all tasks. This
 * is a single widget that displays busy if one or more tasks is busy and
 * as not busy if zero tasks are busy.
 *
 * The methods in this class are thread safe, and it will proxy any
 * calls to the SwingThread as necessary.
 *
 * @author <a href="mailto:jesse@odel.on.ca">Jesse Wilson</a>
 */
public class ProgressBar implements TaskListener {
    
    /** the task manager to get status from */
    private TaskManager taskManager;
    
    /** the progress bar that contains progress status */
    private JProgressBar progressBar;
    
    /**
     * Creates a new progress bar that monitors the specified task manager.
     */
    public ProgressBar(TaskManager taskManager) {
        this.taskManager = taskManager;
        
        progressBar = new JProgressBar();
        
        taskManager.addTaskListener(this);
    }
    
    /**
     * Gets the progress bar.
     */
    public JProgressBar getProgressBar() {
        return progressBar;
    }
    
    /**
     * When there is a change in one of the progress tasks, this is called
     * to notify the Progress Bar of the change.
     */
    public synchronized void taskUpdated(TaskContext task) {
        boolean busy = false;
        for(Iterator i = taskManager.getTaskContexts().iterator(); i.hasNext(); ) {
            TaskContext taskContext = (TaskContext)i.next();
            if(taskContext.isBusy() || taskContext.getProgress() < 1.0) busy = true;
        }
        progressBar.setIndeterminate(busy);
    }
}
