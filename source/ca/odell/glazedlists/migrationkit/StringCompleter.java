/**
 * Glazed Lists
 * http://glazedlists.dev.java.net/
 *
 * COPYRIGHT 2003 O'DELL ENGINEERING LTD.
 */
package ca.odell.glazedlists.migrationkit;

/**
 * A mechanism for looking up the best way to anticipate what
 * the user is trying to type in a text field. Implementors are
 * given a prefix and should return a best String that starts with
 * that prefix. For example, a String Completer for cities in
 * Ontario may return "Mississauga" given the prefix "Mi"
 * and "Milton" given the prefix "Mil". The implementors may use
 * any mechanism necessary to guess, such as database, preconstructed
 * tables or even software-ESP.
 *
 * @deprecated This class will not be available in future releases of Glazed Lists.
 *      It exists to help users migrate between Glazed Lists < 0.8 and Glazed Lists >= 0.9.
 *
 * @author <a href="mailto:jesse@odel.on.ca">Jesse Wilson</a>
 */
public interface StringCompleter {

    /**
     * Takes a String and return a longer string which has the
     * supplied string as a prefix.
     */
    public String getCompleted(String prefix);
}
