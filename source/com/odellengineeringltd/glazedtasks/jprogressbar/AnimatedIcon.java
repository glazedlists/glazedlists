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
// for responding to user actions
import java.awt.event.*;

/**
 * A task listener that displays an animated icon when work is being done
 * and a static icon when no work is being done.
 *
 * @author <a href="mailto:jesse@odel.on.ca">Jesse Wilson</a>
 */
public class AnimatedIcon implements TaskListener, MouseListener {
    
    /** the task manager to get status from */
    private TaskManager taskManager;
    
    /** the label that hosts the busy or not-busy icons */
    private JLabel iconHost;
    
    /** the icons for the different states */
    private Icon idleIcon;
    private Icon busyIcon;
    
    /** the tasks panel to show when clicked */
    private CurrentTasksPanel tasksPanel;
    
    /**
     * Creates a new progress bar that monitors the specified task manager.
     */
    public AnimatedIcon(TaskManager taskManager, Icon idleIcon, Icon busyIcon) {
        this.taskManager = taskManager;
        
        this.idleIcon = idleIcon;
        this.busyIcon = busyIcon;
        iconHost = new JLabel(idleIcon);
        tasksPanel = new CurrentTasksPanel(taskManager);
        iconHost.addMouseListener(this);
        
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

    /**
     * Open the dialog when the user clicks the icon.
     */
    public void mouseClicked(MouseEvent mouseEvent) {
        tasksPanel.getTasksFrame().show();
    }
    /**
     * These methods are necessary to implement the MouseListener
     * interface.
     */
    public void mouseEntered(MouseEvent mouseEvent) { }
    public void mouseExited(MouseEvent mouseEvent) { }
    public void mousePressed(MouseEvent mouseEvent) { }
    public void mouseReleased(MouseEvent mouseEvent) { }
}
