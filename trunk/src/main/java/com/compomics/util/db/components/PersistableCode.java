/*
 * Copyright (C) Lennart Martens
 * 
 * Contact: lennart.martens AT UGent.be (' AT ' to be replaced with '@')
 */

/*
 * Created by IntelliJ IDEA.
 * User: Lennart
 * Date: 30-dec-02
 * Time: 15:14:00
 */
package com.compomics.util.db.components;
import org.apache.log4j.Logger;

import com.compomics.util.db.DBMetaData;

import java.sql.Types;

/*
 * CVS information:
 *
 * $Revision: 1.6 $
 * $Date: 2009/06/08 08:48:20 $
 */

/**
 * This class generates the code to make a DBAccessor an implementation of the Persistable
 * interface.
 *
 * @author Lennart Martens
 */
public class PersistableCode {

    // Class specific log4j logger for PersistableCode instances.
    Logger logger = Logger.getLogger(PersistableCode.class);

    /**
     * This variable holds the generated code.
     */
    private String iCode = null;

    /**
     * This constructor allows the generation of the code for the implementation of
     * the Persistable interface for a DBAccessor class, based on the specified metadata.
     *
     * @param   aMeta   DBMetaData  with the metadata to generate the code for.
     */
    public PersistableCode(DBMetaData aMeta) {
        // Some variables.
        String table = aMeta.getTableName();
        String[] names = aMeta.getColumnNames();
        String[] types = aMeta.getConvertedColumnTypes();

        // Start generating, first the query.
        StringBuffer query = new StringBuffer("INSERT INTO " + table + " (");
        for(int i = 0; i < names.length; i++) {
            String name = names[i];
            // Prefix all but the first with a comma.
            if(i>0) {
                query.append(", ");
            }
            query.append(name);
        }
        query.append(") values(");
        for(int i = 0; i < names.length; i++) {
            // Prefix all but the first with a comma.
            if(i>0) {
                query.append(", ");
            }
            // We add either '?' (set to correct values later), or the defaults for the
            // USERNAME, CREATIONDATE and MODIFICATIONDATE columns.
            if(names[i].equalsIgnoreCase("username")) {
                query.append("CURRENT_USER");
            } else if(names[i].equalsIgnoreCase("creationdate") || names[i].equalsIgnoreCase("modificationdate")) {
                query.append("CURRENT_TIMESTAMP");
            } else {
                query.append("?");
            }
        }
        query.append(")");

        // Now to generate the code.
        StringBuffer lsb = new StringBuffer(
                "\t/**\n\t * This method allows the caller to insert the data represented by this\n\t * object in a persistent store.\n");
        lsb.append("\t *\n\t * @param   aConn Connection to the persitent store.\n\t */\n");
        lsb.append("\tpublic int persist(Connection aConn) throws SQLException {\n");
        lsb.append("\t\tPreparedStatement lStat = aConn.prepareStatement(\"" + query.toString() + "\");\n");
        int paramCount = 0;
        for(int i = 0; i < names.length; i++) {
            String lCol = names[i];
            String type = types[i];
            String name = "i" + lCol.substring(0,1).toUpperCase() + lCol.substring(1).toLowerCase();
            // See if we have a 'username', 'creationdate' or 'modificationdate' field (ignoring case).
            if(lCol.equalsIgnoreCase("username") || lCol.equalsIgnoreCase("creationdate") || lCol.equalsIgnoreCase("modificationdate")) {
                // Do nothing here as these have already been initialized.
                continue;
            } else {
                // Increment parameter counter.
                paramCount++;
                // See if we have an Object type, or something else.
                if(Character.isUpperCase(type.charAt(0)) || type.indexOf(".") >= 0) {
                    lsb.append("\t\tif(" + name + " == null) {\n");
                    lsb.append("\t\t\tlStat.setNull(" + paramCount + ", " + aMeta.getCodedColumnType(lCol) + ");\n");
                    lsb.append("\t\t} else {\n\t");
                    lsb.append("\t\tlStat.setObject(" + paramCount + ", " + name + ");\n");
                    lsb.append("\t\t}\n");
                } else {
                    if(type.equals("byte[]")) {
                        // A Byte[] is set using an inputstream.
                        lsb.append("\t\tif(" + name + " == null) {\n");
                        lsb.append("\t\t\tlStat.setNull(" + paramCount + ", " + aMeta.getCodedColumnType(lCol) + ");\n");
                        lsb.append("\t\t} else {\n");
                        lsb.append("\t\t\tByteArrayInputStream bais" + i + " = new ByteArrayInputStream(" + name + ");\n");
                        lsb.append("\t\t\tlStat.setBinaryStream(" + paramCount + ", bais" + i + ", " + name + ".length);\n");
                        lsb.append("\t\t}\n");
                    } else if(type.equals("int")) {
                        lsb.append("\t\tif(" + name + " == Integer.MIN_VALUE) {\n");
                        lsb.append("\t\t\tlStat.setNull(" + paramCount + ", " + aMeta.getCodedColumnType(lCol) + ");\n");
                        lsb.append("\t\t} else {\n");
                        lsb.append("\t\t\tlStat.setInt(" + paramCount + ", " + name + ");\n");
                        lsb.append("\t\t}\n");
                    } else if(type.equals("long")) {
                        lsb.append("\t\tif(" + name + " == Long.MIN_VALUE) {\n");
                        lsb.append("\t\t\tlStat.setNull(" + paramCount + ", " + aMeta.getCodedColumnType(lCol) + ");\n");
                        lsb.append("\t\t} else {\n");
                        lsb.append("\t\t\tlStat.setLong(" + paramCount + ", " + name + ");\n");
                        lsb.append("\t\t}\n");
                    } else if(type.equals("double")) {
                        lsb.append("\t\tif(" + name + " == Double.MIN_VALUE) {\n");
                        lsb.append("\t\t\tlStat.setNull(" + paramCount + ", " + aMeta.getCodedColumnType(lCol) + ");\n");
                        lsb.append("\t\t} else {\n");
                        lsb.append("\t\t\tlStat.setDouble(" + paramCount + ", " + name + ");\n");
                        lsb.append("\t\t}\n");
                    } else if(type.equals("char")) {
                        lsb.append("\t\tif(" + name + " == Character.MIN_VALUE) {\n");
                        lsb.append("\t\t\tlStat.setNull(" + paramCount + ", " + aMeta.getCodedColumnType(lCol) + ");\n");
                        lsb.append("\t\t} else {\n");
                        lsb.append("\t\t\tlStat.setObject(" + paramCount + ", new Character(" + name + "));\n");
                        lsb.append("\t\t}\n");
                    } else if(type.equals("boolean")) {
                        lsb.append("\t\tlStat.setBoolean(" + paramCount + ", " + name + ");\n");
                    }
                }
            }
        }
        lsb.append("\t\tint result = lStat.executeUpdate();\n\n");
        lsb.append("\t\t// Retrieving the generated keys (if any).\n");
        lsb.append("\t\tResultSet lrsKeys = lStat.getGeneratedKeys();\n");
        lsb.append("\t\tResultSetMetaData lrsmKeys = lrsKeys.getMetaData();\n");
        lsb.append("\t\tint colCount = lrsmKeys.getColumnCount();\n");
        lsb.append("\t\tiKeys = new Object[colCount];\n");
        lsb.append("\t\twhile(lrsKeys.next()) {\n");
        lsb.append("\t\t\tfor(int i=0;i<iKeys.length;i++) {\n");
        lsb.append("\t\t\t\tiKeys[i] = lrsKeys.getObject(i+1);\n");
        lsb.append("\t\t\t}\n");
        lsb.append("\t\t}\n");
        lsb.append("\t\tlrsKeys.close();\n");
        lsb.append("\t\tlStat.close();\n");

        // Get the primary key columns.
        String[] pkCols = aMeta.getPrimaryKeyColumns();
        // If there is only one primary key column, we automatically
        // know which one is updated!
        if(pkCols.length == 1) {
            // See what column type it is.
            int pkColType = aMeta.getCodedColumnType(pkCols[0]);
            // Only generate the auto-retrieval of the PK if the column type is INT.
            if(pkColType == Types.INTEGER) {
                // Generate the code that checks whether we have exactly one auto-generated key.
                lsb.append("\t\t// Verify that we have a single, generated key.\n");
                lsb.append("\t\tif(iKeys != null && iKeys.length == 1 && iKeys[0] != null) {\n");
                lsb.append("\t\t\t// Since we have exactly one key specified, and only\n" +
                           "\t\t\t// one Primary Key column, we can infer that this was the\n" +
                           "\t\t\t// generated column, and we can therefore initialize it here.\n");
                lsb.append("\t\t\ti" + pkCols[0].substring(0,1).toUpperCase() + pkCols[0].substring(1).toLowerCase() + " = ((Number) iKeys[0]).longValue();\n");
                lsb.append("\t\t}\n");
            }
        }

        lsb.append("\t\tthis.iUpdated = false;\n");
        lsb.append("\t\treturn result;\n");
        lsb.append("\t}\n");
        lsb.append("\n\t/**\n\t * This method will return the automatically generated key for the insert if \n");
        lsb.append("\t * one was triggered, or 'null' otherwise.\n");
        lsb.append("\t *\n");
        lsb.append("\t * @return\tObject[]\twith the generated keys.\n");
        lsb.append("\t */\n");
        lsb.append("\tpublic Object[] getGeneratedKeys() {\n");
        lsb.append("\t\treturn this.iKeys;\n");
        lsb.append("\t}\n");
        this.iCode = lsb.toString();
    }

    /**
     * This method will report on the generated code.
     *
     * @return  String  with the generated code.
     */
    public String toString() {
        return "\n" + iCode + "\n";
    }
}
