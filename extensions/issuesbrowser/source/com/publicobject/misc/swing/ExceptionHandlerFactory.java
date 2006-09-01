/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package com.publicobject.misc.swing;

import com.publicobject.misc.Exceptions;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.net.ConnectException;
import java.net.NoRouteToHostException;
import java.net.UnknownHostException;
import java.security.AccessControlException;
import java.text.MessageFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This static factory class produces implementations of the
 * {@link Exceptions.Handler} that are appropriate for handling various types
 * of exceptions. The handlers typically display messages to the user in a
 * modal dialog. Specific factory methods exist for each type of supported
 * exception.
 *
 * @author James Lemieux
 */
public final class ExceptionHandlerFactory {

    private ExceptionHandlerFactory() {}

    /**
     * Returns an {@link Exceptions.Handler} capable of identifying and
     * handling an {@link UnknownHostException}.
     */
    public static Exceptions.Handler unknownHostExceptionHandler(Component parentComponent) {
        return new UnknownHostExceptionHandler(parentComponent);
    }

    /**
     * Returns an {@link Exceptions.Handler} capable of identifying and
     * handling a {@link ConnectException}.
     */
    public static Exceptions.Handler connectExceptionHandler(Component parentComponent) {
        return new ConnectExceptionHandler(parentComponent);
    }

    /**
     * Returns an {@link Exceptions.Handler} capable of identifying and
     * handling a {@link NoRouteToHostException}.
     */
    public static Exceptions.Handler noRouteToHostExceptionHandler(Component parentComponent) {
        return new NoRouteToHostExceptionHandler(parentComponent);
    }

    /**
     * Returns an {@link Exceptions.Handler} capable of identifying and
     * handling an {@link AccessControlException}.
     */
    public static Exceptions.Handler accessControlExceptionHandler(Component parentComponent) {
        return new AccessControlExceptionHandler(parentComponent);
    }

    /**
     * Returns an {@link Exceptions.Handler} capable of identifying and
     * handling an {@link IOException} indicating an HTTP 500 error code.
     */
    public static Exceptions.Handler ioExceptionCode500Handler(Component parentComponent) {
        return new IOExceptionCode500Handler(parentComponent);
    }

    /**
     * An Exceptions.Handler for UnknownHostExceptions that displays an
     * informative message stating how to configure Java to use a proxy
     * server.
     */
    private static class UnknownHostExceptionHandler extends AbstractCannotConnectExceptionHandler {
        public UnknownHostExceptionHandler(Component parentComponent) {
            super(parentComponent);
        }

        public boolean recognize(Exception e) {
            return e instanceof UnknownHostException;
        }
    }

    /**
     * An Exceptions.Handler for ConnectExceptions that displays an
     * informative message stating how to configure Java to use a proxy
     * server.
     */
    private static class ConnectExceptionHandler extends AbstractCannotConnectExceptionHandler {
        public ConnectExceptionHandler(Component parentComponent) {
            super(parentComponent);
        }

        public boolean recognize(Exception e) {
            return e instanceof ConnectException;
        }
    }

    /**
     * An Exceptions.Handler for NoRouteToHostException that displays an
     * informative message stating the probable cause and how to configure
     * Java to use a proxy server.
     */
    private static class NoRouteToHostExceptionHandler implements Exceptions.Handler {
        private final Component parentComponent;

        protected NoRouteToHostExceptionHandler(Component parentComponent) {
            this.parentComponent = parentComponent;
        }

        public boolean recognize(Exception e) {
            return e instanceof NoRouteToHostException;
        }

        public void handle(Exception e) {
            final String title = "Unable to find a route to the Host";

            final String message;
            if (isWindowsOS()) {
                // explain how to configure a Proxy Server for Java on Windows
                message = "Typically, the remote host cannot be reached because of an\n" +
                          "intervening firewall, or if an intermediate router is down.\n\n" +
                          "If connecting to the Internet via a proxy server,\n" +
                          "ensure you have configured Java correctly in\n" +
                          "Control Panel \u2192 Java \u2192 General \u2192 Network Settings...\n\n" +
                          "You must restart this application if you adjust the settings.";
            } else {
                message = "Please check your Internet connection settings.";
            }

            // explain how to configure a Proxy Server for Java on Windows
            SwingUtilities.invokeLater(new ShowMessageDialogRunnable(parentComponent, title, message));
        }
    }

    /**
     * An Exceptions.Handler for an AccessControlException when attempting to resolve
     * a hostname to an IP address or connect to that IP. It displays an informative
     * message stating the probable cause and how to configure Java to use a proxy server.
     */
    private static class AccessControlExceptionHandler implements Exceptions.Handler {
        // sample message 1: "access denied (java.net.SocketPermission javacc.dev.java.net resolve)"
        // sample message 2: "access denied (java.net.SocketPermission beavertn-svr-eh.ad.nike.com:8080 connect,resolve)"
        private final Matcher messageMatcher = Pattern.compile(".*access denied \\p{Punct}java.net.SocketPermission (\\S*) (.*)").matcher("");

        private final Component parentComponent;

        protected AccessControlExceptionHandler(Component parentComponent) {
            this.parentComponent = parentComponent;
        }

        public boolean recognize(Exception e) {
            return e instanceof AccessControlException && messageMatcher.reset(e.getMessage()).matches();
        }

        public void handle(Exception e) {
            final String title = "Unable to connect to Host";

            final String message;
            if (isWindowsOS()) {
                final String hostname = messageMatcher.group(1);

                // explain how to configure a Proxy Server for Java on Windows
                message = MessageFormat.format(
                          "Insufficient security privileges to connect to:\n\n\t{0} \n\n" +
                          "If connecting to the Internet via a proxy server,\n" +
                          "ensure you have configured Java correctly in\n" +
                          "Control Panel \u2192 Java \u2192 General \u2192 Network Settings...\n\n" +
                          "You must restart this application if you adjust the settings.", new Object[] {hostname});
            } else {
                message = "Please check your Internet connection settings.";
            }

            // explain how to configure a Proxy Server for Java on Windows
            SwingUtilities.invokeLater(new ShowMessageDialogRunnable(parentComponent, title, message));
        }
    }

    /**
     * An Exceptions.Handler for an IOException containing a HTTP response code
     * of 500 indicating some error occurred within the webserver. All you can
     * do at this point is retry the operation later.
     */
    private static class IOExceptionCode500Handler implements Exceptions.Handler {
        // sample message: "Server returned HTTP response code: 500 for URL: https://javanettasks.dev.java.net/issues/xml.cgi?id=1:2:3:4:5:6:..."
        private final Matcher messageMatcher = Pattern.compile("Server returned HTTP response code: 500 (.*)").matcher("");

        private final Component parentComponent;

        protected IOExceptionCode500Handler(Component parentComponent) {
            this.parentComponent = parentComponent;
        }

        public boolean recognize(Exception e) {
            return e instanceof IOException && messageMatcher.reset(e.getMessage()).matches();
        }

        public void handle(Exception e) {
            final String title = "Internal Server Error";

            // explain that this is not our fault
            final String message = "An error occurred within the webserver.\n" +
                                   "Please retry your operation later.";

            // explain that this is java.net's fault
            SwingUtilities.invokeLater(new ShowMessageDialogRunnable(parentComponent, title, message));
        }
    }

    /**
     * An abstract Exceptions.Handler for all types of Exceptions that indicate
     * a connection to the internet could not be established. It displays an
     * informative message stating how to configure Java to use a proxy
     * server.
     */
    private static abstract class AbstractCannotConnectExceptionHandler implements Exceptions.Handler {
        private final Component parentComponent;

        protected AbstractCannotConnectExceptionHandler(Component parentComponent) {
            this.parentComponent = parentComponent;
        }

        public void handle(Exception e) {
            final String title = "Unable to connect to the Internet";

            final String message;
            if (isWindowsOS()) {
                // explain how to configure a Proxy Server for Java on Windows
                message = "If connecting to the Internet via a proxy server,\n" +
                          "ensure you have configured Java correctly in\n" +
                          "Control Panel \u2192 Java \u2192 General \u2192 Network Settings...\n\n" +
                          "You must restart this application if you adjust the settings.";
            } else {
                message = "Please check your Internet connection settings.";
            }

            SwingUtilities.invokeLater(new ShowMessageDialogRunnable(parentComponent, title, message));
        }
    }

    /**
     * A convenience class to show a message dialog to the user.
     */
    private static class ShowMessageDialogRunnable implements Runnable {
        private final Component parentComponent;
        private final String title;
        private final String message;

        public ShowMessageDialogRunnable(Component parentComponent, String title, String message) {
            this.parentComponent = parentComponent;
            this.title = title;
            this.message = message;
        }

        public void run() {
            JOptionPane.showMessageDialog(parentComponent, message, title, JOptionPane.WARNING_MESSAGE);
        }
    }

    /**
     * Returns <tt>true</tt> if this application is executing on a Windows
     * operating system; <tt>false</tt> otherwise.
     */
    private static boolean isWindowsOS() {
        final String osname = System.getProperty("os.name");
        return osname != null && osname.toLowerCase().indexOf("windows") == 0;
    }
}