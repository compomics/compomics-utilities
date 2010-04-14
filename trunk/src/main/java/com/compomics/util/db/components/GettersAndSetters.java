/*
 * Copyright (C) Lennart Martens
 * 
 * Contact: lennart.martens AT UGent.be (' AT ' to be replaced with '@')
 */

/*
 * Created by IntelliJ IDEA.
 * User: Lennart
 * Date: 24-dec-02
 * Time: 16:27:25
 */
package com.compomics.util.db.components;
import org.apache.log4j.Logger;

import com.compomics.util.db.DBMetaData;

/*
 * CVS information:
 *
 * $Revision: 1.3 $
 * $Date: 2007/07/06 09:41:53 $
 */

/**
 * This class will generate (and subsequently output upon request) all relevant getters and setters
 * for a DBAccessor.
 *
 * @author Lennart Martens
 */
public class GettersAndSetters {
	// Class specific log4j logger for GettersAndSetters instances.
	Logger logger = Logger.getLogger(GettersAndSetters.class);

    /**
     * This variable holds all the getters for the DBAccessor object.
     */
    private String[] iGetters = null;

    /**
     * This variable holds all the setters for the DBAccessor object.
     */
    private String[] iSetters = null;

    /**
     * The constructor takes care of generating all getters and setters,
     * based on the speicifed metadata.
     *
     * @param   aMeta   DBMetaData with the metadata to base the getters and setters on.
     */
    public GettersAndSetters(DBMetaData aMeta) {
        this.createGetters(aMeta);
        this.createSetters(aMeta);
    }

    /**
     * This method creates all the getters for the specified MetaData.
     *
     * @param   aMeta   DBMetaData to create getters for.
     */
    private void createGetters(DBMetaData aMeta) {
        // Initialize getters variable.
        this.iGetters = new String[aMeta.getColumnCount()];
        // Cycle all columns.
        for(int i=0;i<aMeta.getColumnCount();i++) {
            // Get the name for the variable.
            String name = aMeta.getColumnName(i);
            name = name.substring(0, 1).toUpperCase() + name.toLowerCase().substring(1);
            String type = aMeta.getConvertedColumnType(i);

            this.iGetters[i] = "\t/**\n\t * This method returns the value for the '" + name + "' column\n\t * \n\t * @return\t" + type + "\twith the value for the " + name + " column.\n\t */\n\tpublic " + type + " get" + name + "() {\n\t\treturn this.i" + name + ";\n\t}";
        }
    }

    /**
     * This method creates all the setters for the specified MetaData.
     *
     * @param   aMeta   DBMetaData to create setters for.
     */
    private void createSetters(DBMetaData aMeta) {
        // Initialize getters variable.
        this.iSetters = new String[aMeta.getColumnCount()];
        // Cycle all columns.
        for(int i=0;i<aMeta.getColumnCount();i++) {
            // Get the name for the variable.
            String name = aMeta.getColumnName(i);
            name = name.substring(0, 1).toUpperCase() + name.toLowerCase().substring(1);
            String type = aMeta.getConvertedColumnType(i);

            this.iSetters[i] = "\t/**\n\t * This method sets the value for the '" + name + "' column\n\t * \n\t * @param\ta" + name + "\t" + type + " with the value for the " + name + " column.\n\t */\n\tpublic void set" + name + "(" + type + " a" + name + ") {\n\t\tthis.i" + name + " = a" + name + ";\n\t\tthis.iUpdated = true;\n\t}";
        }
    }

    /**
     * This method generates the code for the getters and setters.
     *
     * @return  String  with the code.
     */
    public String toString() {
        StringBuffer lsb = new StringBuffer("\n");

        for(int i=0;i<iGetters.length;i++) {
            lsb.append(iGetters[i] + "\n\n");
        }

        for(int i=0;i<iSetters.length;i++) {
            lsb.append(iSetters[i] + "\n\n");
        }

        return lsb.toString() + "\n";
    }
}
