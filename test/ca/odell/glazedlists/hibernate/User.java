/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.hibernate;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;

/**
 * Helper class for User.
 * 
 * @author Holger Brands
 */
public class User {
    
    /** Username is Id. */
    private String userName;

    /** List of nicknames. */
    private EventList<String> nickNames = new BasicEventList<String>();

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
    
    /**
     * Gets the nicknames.
     */
    public EventList<String> getNickNames() {
        return nickNames;
    }

    /**
     * Sets the nicknames.
     */
    private void setNickNames(EventList<String> nickNames) {
        this.nickNames = nickNames;
    }
    
    public void addNickName(String nickName) {
        nickNames.add(nickName);
    }

    public void removedNickName(String nickName) {
        nickNames.remove(nickName);
    }
    
}
