package com.compomics.util.pride.prideobjects;

import com.compomics.util.pride.PrideObject;
import java.util.ArrayList;

/**
 * An object for storing ContactGroup details.
 *
 * @author Harald Barsnes
 */
public class ContactGroup implements PrideObject {

    /**
     * Empty default constructor
     */
    public ContactGroup() {
    }

    /**
     * Serialization number for backward compatibility.
     */
    static final long serialVersionUID = -9182316910747747823L; // @TODO: has to be updated!!!
    /**
     * The contacts.
     */
    private ArrayList<Contact> contacts;
    /**
     * The contact group name.
     */
    private String groupName;

    /**
     * Create a new ContactGroup object.
     *
     * @param contacts the list of contacts
     * @param groupName the contact group name
     */
    public ContactGroup(ArrayList<Contact> contacts, String groupName) {
        this.contacts = contacts;
        this.groupName = groupName;
    }

    /**
     * Returns the contacts.
     *
     * @return the name
     */
    public ArrayList<Contact> getContacts() {
        return contacts;
    }

    /**
     * Set the contacts.
     *
     * @param contacts the contacts
     */
    public void setContacts(ArrayList<Contact> contacts) {
        this.contacts = contacts;
    }

    /**
     * Returns the contact group name.
     *
     * @return the group name
     */
    public String getName() {
        return groupName;
    }

    /**
     * Set the contact group name.
     *
     * @param groupName the group name to set
     */
    public void setName(String groupName) {
        this.groupName = groupName;
    }

    public String getFileName() {
        return groupName;
    }
}
