/**
 * Glazed Lists
 * http://glazedlists.dev.java.net/
 *
 * COPYRIGHT 2003 O'DELL ENGINEERING LTD.
 */
package com.odellengineeringltd.glazedtasks.jprogressbar;

// the glazed tasks base
import com.odellengineeringltd.glazedtasks.*;
// for running tasks in the Swing thread
import javax.swing.SwingUtilities;
// for having a progress bar
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.Icon;
// for lists of task contexts
import java.util.*;

/**
 * A task listener that displays an animated icon when work is being done
 * and a static icon when no work is being done.
 *
 * @author <a href="mailto:jesse@odel.on.ca">Jesse Wilson</a>
 */
public class AnimatedIcon implements TaskListener {
    
    /** the task manager to get status from */
    private TaskManager taskManager;
    
    /** the label that hosts the busy or not-busy icons */
    private JLabel iconHost;
    
    /** the icons for the different states */
    private Icon idleIcon;
    private Icon busyIcon;
    
    /**
     * Creates a new progress bar that monitors the specified task manager.
     */
    public AnimatedIcon(TaskManager taskManager, Icon idleIcon, Icon busyIcon) {
        this.taskManager = taskManager;
        
        this.idleIcon = idleIcon;
        this.busyIcon = busyIcon;
        iconHost = new JLabel(idleIcon);
        
        taskManager.addTaskListener(this);
    }
    
    /**
     * Gets the label.
     */
    public JComponent getIcon() {
        return iconHost;
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
        if(busy) iconHost.setIcon(busyIcon);
        else iconHost.setIcon(idleIcon);
    }
}
