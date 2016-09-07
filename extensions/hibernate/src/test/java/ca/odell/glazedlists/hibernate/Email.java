/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.hibernate;

import java.io.Serializable;

/**
 * Helper class for Email.
 *
 * @author Holger Brands
 */
public class Email implements Serializable {

    private static final long serialVersionUID = 0L;

    /** Id. */
    private Long id;

    /** Email address. */
    private String address;

    /**
     * Default constructor for Hibernate.
     */
    Email() {
        // NOP
    }

    /**
     * Constructor with address
     */
    public Email(String address) {
        this.address = address;
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

    /**
     * Gets the address.
     */
    public String getAddress() {
        return address;
    }

    /**
     * Sets the address.
     */
    public void setAddress(String address) {
        this.address = address;
    }

    /**
     * Equality depends on address.
     */
    @Override
    public boolean equals(Object that) {
        if (!(that instanceof Email)) return false;
        final Email p = (Email) that;
        return this.address.equals(p.address);
    }

    /**
     * Hashcode depends on address.
     */
    @Override
    public int hashCode() {
        return address.hashCode();
    }
}
