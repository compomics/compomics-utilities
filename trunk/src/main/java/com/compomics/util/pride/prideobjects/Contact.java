package com.compomics.util.pride.prideobjects;

import com.compomics.util.pride.PrideObject;
import java.io.*;

/**
 * An object for storing Contact details.
 *
 * @author Harald Barsnes
 */
public class Contact implements PrideObject, Serializable {

    /**
     * Serialization number for backward compatibility.
     */
    static final long serialVersionUID = -9182316910747747823L;
    /**
     * The contact's name.
     */
    private String name;
    /**
     * The contact's e-mail.
     */
    private String eMail;
    /**
     * The contact's institution.
     */
    private String institution;

    /**
     * Create a new Contact object.
     *
     * @param name the contact name
     * @param eMail the contact e-mail
     * @param institution the contact institution
     */
    public Contact(String name, String eMail, String institution) {
        this.name = name;
        this.eMail = eMail;
        this.institution = institution;
    }

    /**
     * Returns the contact name.
     * 
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Set the contact name.
     * 
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returns the contact e-mail.
     * 
     * @return the eMail
     */
    public String getEMail() {
        return eMail;
    }

    /**
     * Set the contact e-mail.
     * 
     * @param eMail the eMail to set
     */
    public void setEMail(String eMail) {
        this.eMail = eMail;
    }

    /**
     * Returns the instituition.
     * 
     * @return the institution
     */
    public String getInstitution() {
        return institution;
    }

    /**
     * Set the instituttion.
     * 
     * @param institution the institution to set
     */
    public void setInstitution(String institution) {
        this.institution = institution;
    }

    public String getFileName() {
        return name;
    }
}
