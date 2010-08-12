/*
 * Copyright (C) Lennart Martens
 * 
 * Contact: lennart.martens AT UGent.be (' AT ' to be replaced with '@')
 */

/*
 * Created by IntelliJ IDEA.
 * User: Lennart
 * Date: 27-dec-02
 * Time: 15:28:01
 */
package com.compomics.util.db.components;
import org.apache.log4j.Logger;

import com.compomics.util.db.DBMetaData;

/*
 * CVS information:
 *
 * $Revision: 1.5 $
 * $Date: 2008/01/23 11:22:53 $
 */

/**
 * This class will generate the code for a default and full constructor for a DBAccessor, 
 * defined by the metadata passed in via the constructor.
 *
 * @author Lennart Martens
 */
public class Constructors {

    // Class specific log4j logger for Constructors instances.
    Logger logger = Logger.getLogger(Constructors.class);

    /**
     * This String will contain the constructors code.
     */
    private String iCode = null;

    /**
     * This constructor takes care of generating the code that represents the
     * constructor for a DBAccessor for the given metadata.
     *
     * @param   aMeta   DBMetaData with the metadata for the DBAccessor to be generated.
     */
    public Constructors(DBMetaData aMeta) {
        // The number of vars we can maximally extract.
        int lCount = aMeta.getColumnCount();
        // The tablename.
        String table = aMeta.getTableName();
        table = table.substring(0, 1).toUpperCase() + table.substring(1).toLowerCase();

        // Default constructor.
        StringBuffer lsb = new StringBuffer(
                "\t/**\n\t * Default constructor.\n\t */\n\tpublic "
                + table + "TableAccessor() {\n\t}\n\n");

        // The code in progress.
        lsb.append("\t/**\n\t * This constructor allows the creation of the '"
                + table + "TableAccessor' object based on a set of values in the HashMap.\n\t *\n");
        lsb.append("\t * @param\taParams\tHashMap with the parameters to initialize this object with.\n"
                + "\t *\t\t<i>Please use only constants defined on this class as keys in the HashMap!</i>\n\t */\n");
        lsb.append("\tpublic " + table + "TableAccessor(HashMap aParams) {\n");
        // The loop to generate all code.
        for(int i=0;i<lCount;i++) {
            String name = aMeta.getColumnName(i);
            name = name.substring(0, 1).toUpperCase() + name.substring(1).toLowerCase();
            lsb.append("\t\tif(aParams.containsKey(" +
                    (Character.isDigit(name.charAt(0))?"i"+name.toUpperCase():name.toUpperCase()) + ")) {\n\t\t\tthis.i" + name);

            // Check the type. If it is Object or array, a cast suffices. If it is a primitive, we'll have to accommodate.
            String type = aMeta.getConvertedColumnType(i);
            if(Character.isLowerCase(type.charAt(0)) && !type.endsWith("[]")) {
                // Primitive. Let's accommodate.
                if(type.equals("int")) {
                    lsb.append(" = ((Integer)aParams.get(" +
                            (Character.isDigit(name.charAt(0))?"i"+name.toUpperCase():name.toUpperCase()) + ")).intValue();");
                } else if(type.equals("long")) {
                    lsb.append(" = ((Long)aParams.get(" +
                            (Character.isDigit(name.charAt(0))?"i"+name.toUpperCase():name.toUpperCase()) + ")).longValue();");
                } else if(type.equals("double")) {
                    lsb.append(" = ((Double)aParams.get(" +
                            (Character.isDigit(name.charAt(0))?"i"+name.toUpperCase():name.toUpperCase()) + ")).doubleValue();");
                } else if(type.equals("char")) {
                    lsb.append(" = ((Character)aParams.get(" +
                            (Character.isDigit(name.charAt(0))?"i"+name.toUpperCase():name.toUpperCase()) + ")).charValue();");
                } else if(type.equals("boolean")) {
                    lsb.append(" = ((Boolean)aParams.get(" +
                            (Character.isDigit(name.charAt(0))?"i"+name.toUpperCase():name.toUpperCase()) + ")).booleanValue();");
                } else {
                    // Do an object cast as well. Better then nothing...
                    lsb.append(" = (" + aMeta.getConvertedColumnType(i) + ")aParams.get(" +
                            (Character.isDigit(name.charAt(0))?"i"+name.toUpperCase():name.toUpperCase()) + ");");
                }
            } else {
                // Object type, cast directly.
                lsb.append(" = (" + aMeta.getConvertedColumnType(i) + ")aParams.get(" +
                        (Character.isDigit(name.charAt(0))?"i"+name.toUpperCase():name.toUpperCase()) + ");");
            }

            lsb.append("\n\t\t}\n");

        }
        lsb.append("\t\tthis.iUpdated = true;\n\t}\n");

        lsb.append("\n\n\t/**\n\t * This constructor allows the creation of the '" + table
                + "TableAccessor' object based on a resultset\n\t * obtained by a 'select * from " + table + "' query.\n\t *\n");
        lsb.append("\t * @param\taResultSet\tResultSet with the required columns to initialize this object with.\n");
        lsb.append("\t * @exception\tSQLException\twhen the ResultSet could not be read.\n\t */\n");
        lsb.append("\tpublic " + table + "TableAccessor(ResultSet aResultSet) throws SQLException {\n");
        // The loop to generate all code.
        for(int i=0;i<lCount;i++) {
            String name = aMeta.getColumnName(i);
            String varName = "this.i" + name.substring(0, 1).toUpperCase() + name.substring(1).toLowerCase();

            // Check the type. If it is Object or array, a cast suffices. If it is a primitive, we'll have to accommodate.
            String type = aMeta.getConvertedColumnType(i);

            // Object types are easy.
            // Others need some specific treatment.
            if(Character.isUpperCase(type.charAt(0)) || type.indexOf(".") >= 0) {
                lsb.append("\t\t" + varName + " = (" + type + ")aResultSet.getObject(\"" + name + "\");\n");
            } else {
                if(type.equals("byte[]")) {
                    lsb.append("\t\tInputStream is" + i + " = aResultSet.getBinaryStream(\"" + name + "\");\n");
                    lsb.append("\t\tVector bytes" + i + " = new Vector();\n");
                    lsb.append("\t\tint reading = -1;\n");
                    lsb.append("\t\ttry {\n");
                    lsb.append("\t\t\twhile((reading = is" + i + ".read()) != -1) {\n");
                    lsb.append("\t\t\t\tbytes" + i + ".add(new Byte((byte)reading));\n");
                    lsb.append("\t\t\t}\n");
                    lsb.append("\t\t\tis" + i + ".close();\n");
                    lsb.append("\t\t} catch(IOException ioe) {\n");
                    lsb.append("\t\t\tbytes" + i + " = new Vector();\n");
                    lsb.append("\t\t}\n");
                    lsb.append("\t\treading = bytes" + i + ".size();\n");
                    lsb.append("\t\t" + varName + " = new byte[reading];\n");
                    lsb.append("\t\tfor(int i=0;i<reading;i++) {\n");
                    lsb.append("\t\t\t" + varName + "[i] = ((Byte)bytes" + i + ".get(i)).byteValue();\n");
                    lsb.append("\t\t}\n");
                } else if(type.equals("int")) {
                    lsb.append("\t\t" + varName + " = aResultSet.getInt(\"" + name + "\");\n");
                } else if(type.equals("long")) {
                    lsb.append("\t\t" + varName + " = aResultSet.getLong(\"" + name + "\");\n");
                } else if(type.equals("double")) {
                    lsb.append("\t\t" + varName + " = aResultSet.getDouble(\"" + name + "\");\n");
                } else if(type.equals("char")) {
                    lsb.append("\t\tString temp" + varName + " = aResultSet.getString(\"" + name + "\");\n");
                    lsb.append("\t\tif(temp" + varName + " != null) {\n");
                    lsb.append("\t\t\t" + varName + " = temp" + varName + ".charAt(0);\n");
                    lsb.append("\t\t} else {\n");
                    lsb.append("\t\t\t" + varName + " = ' ';\n");
                    lsb.append("\t\t}\n");
                } else if(type.equals("boolean")) {
                    lsb.append("\t\t" + varName + " = aResultSet.getBoolean(\"" + name + "\");\n");
                }
            }
        }
        lsb.append("\n\t\tthis.iUpdated = true;\n\t}\n");

        this.iCode = lsb.toString();
    }

    /**
     * This method outputs the generated code in String format.
     *
     * @return  String  with the generated code.
     */
    public String toString() {
        return "\n\n" + this.iCode + "\n";
    }
}
