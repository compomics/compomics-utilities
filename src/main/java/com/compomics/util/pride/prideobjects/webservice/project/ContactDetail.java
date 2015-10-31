package com.compomics.util.pride.prideobjects.webservice.project;

/**
 * The PRIDE ContactDetail object
 *
 * @author Kenneth Verheggen
 */
public class ContactDetail {

    /**
     *
     * the title of the contact person
     */
    String title;
    /**
     *
     * the first name of the contact person
     */
    String firstName;
    /**
     *
     * the last/family name of the contact person
     */
    String lastName;
    /**
     *
     * the affiliation of the contact person
     */
    String affiliation;
    /**
     *
     * the contact's email address
     */
    String email;

    /**
     * Create a new ContactDetail object.
     *
     */
    public ContactDetail() {
    }

    /**
     * Returns the title of the contact
     *
     * @return the title of the contact
     */
    public String getTitle() {
        return title;
    }

    /**
     * Set the title of the contact
     *
     * @param title the title of the contact
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Returns the first name
     *
     * @return the first name
     */
    public String getFirstName() {
        return firstName;
    }

    /**
     * Set the first name of the author
     *
     * @param firstName the first name of the author
     */
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    /**
     * Returns the last name
     *
     * @return the last name
     */
    public String getLastName() {
        return lastName;
    }

    /**
     * Set the last name of the author
     *
     * @param lastName the first name of the author
     */
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    /**
     * Returns the affiliation
     *
     * @return the affiliation
     */
    public String getAffiliation() {
        return affiliation;
    }

    /**
     * Set the affiliation
     *
     * @param affiliation the affiliation
     */
    public void setAffiliation(String affiliation) {
        this.affiliation = affiliation;
    }

    /**
     * Returns the e-mail address
     *
     * @return the e-mail address
     */
    public String getEmail() {
        return email;
    }

    /**
     * Set the e-mail address
     *
     * @param email the e-mail address
     */
    public void setEmail(String email) {
        this.email = email;
    }

}
