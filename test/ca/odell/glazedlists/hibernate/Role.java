/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.hibernate;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Represents a role a user can have.
 * 
 * @author Holger Brands
 */
public class Role {

    /** Id. */
    private Long id;
    
    /** Role name. */
    private String name;

    /** Users who are in this role. */
    private Collection<User> users = new ArrayList<User>();

    /**
     * Default constructor for hibernate.
     */
    Role() {
    }

    /**
     * Constructor with name. 
     */
    public Role(String name) {
        this.name = name;
    }
    
    /**
     * Gets the Id.
     */
    public Long getId() {
        return id;
    }

    /**
     * Sets the Id.
     */
    private void setId(Long id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Collection<User> getUsers() {
        return users;
    }
    
    public void addUser(User user) {
        users.add(user);
    }
    
    public void removeUser(User user) {
        users.remove(user);
    }
}
