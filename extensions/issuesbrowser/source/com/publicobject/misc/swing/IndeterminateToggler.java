package com.publicobject.misc.swing;

import com.publicobject.misc.Throbber;

import javax.swing.*;

/**
 * Toggles the throbber on and off.
 */
public class IndeterminateToggler implements Runnable, Throbber {

    /** whether the throbber will be turned on or off */
    private boolean on = false;

    /** the throbber to be toggled */
    private final JLabel throbber;

    /** the Icon to display when indeterminate activity is occurring */
    private final Icon activeIcon;

    /** the Icon to display when no activity is occurring */
    private final Icon staticIcon;

    public IndeterminateToggler(JLabel throbber, Icon activeIcon, Icon staticIcon) {
        this.throbber = throbber;
        this.activeIcon = activeIcon;
        this.staticIcon = staticIcon;
    }

    public synchronized void setOn() {
        if (!on) {
            on = true;
            SwingUtilities.invokeLater(this);
        }
    }

    public synchronized void setOff() {
        if (on) {
            on = false;
            SwingUtilities.invokeLater(this);
        }
    }

    public synchronized void run() {
        throbber.setIcon(on ? activeIcon : staticIcon);
    }
}