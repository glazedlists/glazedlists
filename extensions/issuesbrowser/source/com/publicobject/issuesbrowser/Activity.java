package com.publicobject.issuesbrowser;

import java.util.Date;

/**
 * Data pertaining to an {@link Issue}'s activity record.
 */
public class Activity {
    private String user = null;
    private Date when = null;
    private String field = null;
    private String fieldDescription = null;
    private String oldValue = null;
    private String newValue = null;

    /**
     * user who performed the change action
     */
    public String getUser() { return user; }
    public void setUser(String user) { this.user = user; }

    /**
     * date the described change was made
     */
    public Date getWhen() { return when; }
    public void setWhen(Date when) { this.when = when; }

    /**
     * name of db field (in fielddefs)
     */
    public String getField() { return field; }
    public void setField(String field) { this.field = field; }

    /**
     * description of the database field that changed
     */
    public String getFieldDescription() { return fieldDescription; }
    public void setFieldDescription(String fieldDescription) { this.fieldDescription = fieldDescription; }

    /**
     * value before the change
     */
    public String getOldValue() { return oldValue; }
    public void setOldValue(String oldValue) { this.oldValue = oldValue; }

    /**
     * value after the change
     */
    public String getNewValue() { return newValue; }
    public void setNewValue(String newValue) { this.newValue = newValue; }
}