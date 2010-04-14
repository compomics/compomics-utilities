/*
 * Copyright (C) Lennart Martens
 * 
 * Contact: lennart.martens AT UGent.be (' AT ' to be replaced with '@')
 */

/*
 * Created by IntelliJ IDEA.
 * User: Lennart
 * Date: 24-dec-02
 * Time: 17:19:40
 */
package com.compomics.util.db.components;
import org.apache.log4j.Logger;

import com.compomics.util.db.DBMetaData;

/*
 * CVS information:
 *
 * $Revision: 1.4 $
 * $Date: 2007/07/06 09:41:53 $
 */

/**
 * This class represents the instance variables for a generated DBAccessor.
 *
 * @author Lennart Martens
 */
public class InstanceVariables {
	// Class specific log4j logger for InstanceVariables instances.
	Logger logger = Logger.getLogger(InstanceVariables.class);

    /**
     * The Strings that contain the code for the instance variables that will
     * be contained in the generated DBAccessor object.
     */
    private String[] iVars = null;

    /**
     * This constructor will create all the code for the instance variables from the given
     * DBMetaData.
     *
     * @param   aMeta   DBMetaData with the metdata for which to generate instance variables.
     */
    public InstanceVariables(DBMetaData aMeta) {
        this.generateVars(aMeta);
    }

    /**
     * This method will generate the actual code for the instance vars.
     *
     * @param   aMeta   DBMetaData with the metdata for which to generate instance variables.
     */
    private void generateVars(DBMetaData aMeta) {
        // Initialize the code for the vars.
        int lCount = aMeta.getColumnCount();
        this.iVars = new String[lCount*2];

        // Generate the code.
        for(int i=0;i<lCount;i++) {
            String type = aMeta.getConvertedColumnType(i);
            String name = aMeta.getColumnName(i);
            name = name.substring(0, 1).toUpperCase() + name.substring(1).toLowerCase();
            iVars[i+lCount] = "\t/**\n\t * This variable represents the key for the '" + aMeta.getColumnName(i) + "' column.\n\t */\n\tpublic static final String " + (Character.isDigit(name.charAt(0))?"i"+name.toUpperCase():name.toUpperCase()) + " = \"" + name.toUpperCase() + "\";";
            StringBuffer lsb = new StringBuffer("\t/**\n\t * This variable represents the contents for the '" + aMeta.getColumnName(i) + "' column.\n\t */\n\tprotected " + type + " i" + name + " = ");
            if(Character.isUpperCase(type.charAt(0)) || type.endsWith("[]") || type.indexOf(".") >= 0) {
                lsb.append("null;\n");
            } else {
                if(type.equals("int")) {
                    lsb.append("Integer.MIN_VALUE;\n");
                } else if(type.equals("long")) {
                    lsb.append("Long.MIN_VALUE;\n");
                } else if(type.equals("double")) {
                    lsb.append("Double.MIN_VALUE;\n");
                } else if(type.equals("char")) {
                    lsb.append("Character.MIN_VALUE;\n");
                } else if(type.equals("boolean")) {
                    lsb.append("false;\n");
                }
            }
            iVars[i] = lsb.toString();
        }
    }

    /**
     * This method generates the code for the instance variables.
     *
     * @return  String  with the code.
     */
    public String toString() {
        StringBuffer lsb = new StringBuffer("\n\t/**\n\t * This variable tracks changes to the object.\n\t */\n\tprotected boolean iUpdated = false;\n\n");
        lsb.append("\t/**\n\t * This variable can hold generated primary key columns.\n\t */\n\tprotected Object[] iKeys = null;\n\n");

        for(int i=0;i<iVars.length;i++) {
            lsb.append(iVars[i] + "\n\n");
        }

        return lsb.toString() + "\n";
    }
}
