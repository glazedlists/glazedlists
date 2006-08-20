/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.hibernate;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;

import java.util.HashMap;
import java.util.Map;

/**
 * Helper class for User.
 * 
 * @author Holger Brands
 */
public class User {
    
    /** Username is Id. */
    private String userName;

    /** List of email addresses. */
    private EventList<Email> emailAddresses = new BasicEventList<Email>();

    /**
     * Default constructor for hibernate.
     */
    User() {
    }

    /**
     * Constructor with name. 
     */
    public User(String userName) {
        this.userName = userName;
    }

    /**
     * Gets the user name.
     */
    public String getUserName() {
        return userName;
    }

    /**
     * Sets the user name.
     */
    public void setUserName(String userName) {
        this.userName = userName;
    }

    /**
     * Gets the email addresses.
     */
    public EventList<Email> getEmailAddresses() {
        return emailAddresses;
    }

    /**
     * Sets the email addresses.
     */
    public void setEmailAddresses(EventList<Email> emailAddresses) {
        this.emailAddresses = emailAddresses;
    }
}
