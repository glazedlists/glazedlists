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
import javax.swing.*;
// for lists of task contexts
import java.util.*;
// for laying out using GridBag
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
// for super-sized labels
import java.awt.Font;

/**
 * A task listener that displays a panel that has a progress bar for each
 * task in progress.
 *
 * @author <a href="mailto:jesse@odel.on.ca">Jesse Wilson</a>
 */
public class CurrentTasksPanel implements TaskListener {
    
    /** the task manager to get status from */
    private TaskManager taskManager;
    
    /** the main panel to display tasks in */
    private JFrame tasksFrame;
    private JPanel tasksPanel;
    
    /** the map of tasks to their TaskPanel */
    private Map tasksAndPanels = new HashMap();
    
    /**
     * Creates a new progress bar that monitors the specified task manager.
     */
    public CurrentTasksPanel(TaskManager taskManager) {
        this.taskManager = taskManager;
        
        tasksPanel = new JPanel();
        tasksPanel.setLayout(new BoxLayout(tasksPanel, BoxLayout.Y_AXIS));
        tasksFrame = new JFrame();
        tasksFrame.setTitle("Tasks");
        tasksFrame.setSize(300, 400);
        JScrollPane tasksScroll = new JScrollPane(tasksPanel, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        tasksFrame.getContentPane().add(tasksScroll);
        
        taskManager.addTaskListener(this);
    }
    
    /**
     * Gets the panel that includes all of the current and failed
     * tasks.
     */
    public JFrame getTasksFrame() {
        return tasksFrame;
    }
    
    /**
     * When there is a change in one of the progress tasks, this is called
     * to notify the Progress Bar of the change.
     */
    public synchronized void taskUpdated(TaskContext task) {
        // the panel for this task
        TaskPanel taskPanel;
        
        // when we're already actively tracking this task
        if(tasksAndPanels.containsKey(task)) {
            taskPanel = (TaskPanel)tasksAndPanels.get(task);
        
        // when we're not yet actively tracking this task
        } else {
            // don't bother to add and remove a task
            if(task.isTaskFinished()) return;
            
            // add a task panel for this task
            taskPanel = new TaskPanel(task);
            tasksAndPanels.put(task, taskPanel);
            tasksPanel.add(taskPanel.getPanel());
            // this ugly swing forces a repaint on the panel
            tasksPanel.invalidate(); tasksPanel.repaint(); tasksFrame.getContentPane().validate();
        }
        
        // update the panel for this task
        taskPanel.taskUpdated();
        
        // if the task is complete, remove the task panel
        if(task.isTaskFinished() && task.getFinishedException() == null) {
            tasksPanel.remove(taskPanel.getPanel());
            // this ugly swing forces a repaint on the panel
            tasksPanel.invalidate(); tasksPanel.repaint(); tasksFrame.getContentPane().validate();
        }
    }
    
    /**
     * Each task is given is own task panel.
     */
    class TaskPanel {
        
        /** the widgets that contains information on this task */
        private JLabel taskLabel;
        private JLabel actionCaptionLabel;
        private JProgressBar progress;
        private JPanel panel;
        
        /** the task to display */
        private TaskContext task;
        
        /**
         * Create a new TaskPanel that displays information on the
         * specified task.
         */
        public TaskPanel(TaskContext task) {
            this.task = task;
            
            // construct the widgets: task label
            taskLabel = new JLabel(" ");
            Font taskLabelFont = taskLabel.getFont();
            Font taskLabelLarger = taskLabelFont.deriveFont((float)(1.1*taskLabelFont.getSize2D()));
            taskLabel.setFont(taskLabelLarger);
            // construct the widgets: action label
            actionCaptionLabel = new JLabel(" ");
            // construct the widgets: progress bar
            progress = new JProgressBar(JProgressBar.HORIZONTAL, 0, 1000);
            
            // lay out the widgets in a panel
            panel = new JPanel();
            panel.setLayout(new GridBagLayout());
            GridBagConstraints cell = new GridBagConstraints();
            cell.gridx = 0;
            cell.gridy = GridBagConstraints.RELATIVE;
            cell.anchor = GridBagConstraints.WEST;
            cell.weightx = 0.0;
            cell.weighty = 0.0;
            panel.add(taskLabel, cell);
            panel.add(actionCaptionLabel, cell);
            cell.fill = GridBagConstraints.HORIZONTAL;
            cell.weightx = 1.0;
            panel.add(progress, cell);
            
            // set the specific values for the widgets
            taskUpdated();
        }
        
        /**
         * When the task is updated, this task panel can update
         * its state.
         */
        public void taskUpdated() {
            // refresh the task name label
            String taskName = task.getTask().toString();
            if(taskName == null || taskName == "") taskName = "Task";
            taskLabel.setText(taskName);

            // refresh the action caption label
            String actionCaption = task.getActionCaption();
            if(actionCaption == null || actionCaption == "") actionCaption = "Working";
            actionCaptionLabel.setText(actionCaption);
            
            // refresh the progress bar
            if(task.isBusy()) {
                progress.setIndeterminate(true);
            } else {
                progress.setIndeterminate(false);
                progress.setValue((int)(1000 * task.getProgress()));
            }
        }
        
        /**
         * Fetch the panel that contains this tasks current
         * information.
         */
        public JPanel getPanel() {
            return panel;
        }
    }
}
