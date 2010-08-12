/*
 * Copyright (C) Lennart Martens
 * 
 * Contact: lennart.martens AT UGent.be (' AT ' to be replaced with '@')
 */

/*
 * Created by IntelliJ IDEA.
 * User: Lennart
 * Date: 30-dec-02
 * Time: 14:23:22
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
 * This class generates the code to make a DBAccessor an implementation of the Updateable
 * interface.
 *
 * @author Lennart Martens
 */
public class UpdateableCode {

    // Class specific log4j logger for UpdateableCode instances.
    Logger logger = Logger.getLogger(UpdateableCode.class);

    /**
     * This vraiable holds the generated code.
     */
    private String iCode = null;

    /**
     * This constructor allows the generation of the code for the implementation of
     * the Updateable interface for a DBAccessor class, based on the specified metadata.
     *
     * @param   aMeta   DBMetaData  with the metadata to generate the code for.
     */
    public UpdateableCode(DBMetaData aMeta) {
        // Get some info.
        String table = aMeta.getTableName();
        String[] keys = aMeta.getPrimaryKeyColumns();
        String[] names = aMeta.getColumnNames();
        String[] types = aMeta.getConvertedColumnTypes();

        // Query construction.
        StringBuffer query = new StringBuffer("UPDATE " + table + " SET ");
        for(int i = 0; i < names.length; i++) {
            String name = names[i];
            // All but the first need a preceding comma.
            if(i>0) {
                query.append(", ");
            }
            query.append(name + " = ");
            if(name.equalsIgnoreCase("modificationdate")) {
                query.append("CURRENT_TIMESTAMP");
            } else {
                query.append("?");
            }
        }
        query.append(" WHERE ");
        for(int i = 0; i < keys.length; i++) {
            String lKey = keys[i];
            if(i>0) {
                query.append(" AND ");
            }
            query.append(lKey + " = ?");
        }

        // And now the code itself.
        StringBuffer lsb = new StringBuffer(
                "\t/**\n\t * This method allows the caller to update the data represented by this\n\t * object in a persistent store.\n");
        lsb.append("\t *\n\t * @param   aConn Connection to the persitent store.\n\t */\n");
        lsb.append("\tpublic int update(Connection aConn) throws SQLException {\n");
        lsb.append("\t\tif(!this.iUpdated) {\n");
        lsb.append("\t\t\treturn 0;\n");
        lsb.append("\t\t}\n");
        lsb.append("\t\tPreparedStatement lStat = aConn.prepareStatement(\"" + query.toString() + "\");\n");
        int paramCount = 0;
        for(int i = 0; i < names.length; i++) {
            String lCol = names[i];
            String type = types[i];
            String name = "i" + lCol.substring(0,1).toUpperCase() + lCol.substring(1).toLowerCase();
            // See if we have an Object type, or something else.
            if(lCol.equalsIgnoreCase("modificationdate")) {
                // Do nothing as the 'CURRENT_TIMESTAMP' has been defined above.
                continue;
            } else {
                paramCount++;
                if(Character.isUpperCase(type.charAt(0)) || type.indexOf(".") >= 0) {
                    lsb.append("\t\tlStat.setObject(" + paramCount + ", " + name + ");\n");
                } else {
                    if(type.equals("byte[]")) {
                        // A Byte[] is set using an inputstream.
                        lsb.append("\t\tByteArrayInputStream bais" + i + " = new ByteArrayInputStream(" + name + ");\n");
                        lsb.append("\t\tlStat.setBinaryStream(" + paramCount + ", bais" + i + ", " + name + ".length);\n");
                    } else if(type.equals("int")) {
                        lsb.append("\t\tlStat.setInt(" + paramCount + ", " + name + ");\n");
                    } else if(type.equals("long")) {
                        lsb.append("\t\tlStat.setLong(" + paramCount + ", " + name + ");\n");
                    } else if(type.equals("double")) {
                        lsb.append("\t\tlStat.setDouble(" + paramCount + ", " + name + ");\n");
                    } else if(type.equals("char")) {
                        lsb.append("\t\tlStat.setObject(" + paramCount + ", new Character(" + name + "));\n");
                    } else if(type.equals("boolean")) {
                        lsb.append("\t\tlStat.setBoolean(" + paramCount + ", " + name + ");\n");
                    }
                }
            }
        }
        // Variable for the 'where' clause.
        for(int i = paramCount; i < paramCount + keys.length; i++) {
            String lCol = keys[i-paramCount];
            String name = "i" + lCol.substring(0,1).toUpperCase() + lCol.substring(1).toLowerCase();
            String type = aMeta.getConvertedColumnType(lCol);
            // See if we have an Object type, or something else.
            if(Character.isUpperCase(type.charAt(0)) || type.indexOf(".") >= 0) {
                lsb.append("\t\tlStat.setObject(" + (i+1) + ", " + name + ");\n");
            } else {
                if(type.equals("byte[]")) {
                    // A Byte[] is set using an inputstream.
                    lsb.append("\t\tByteArrayInputStream bais" + i + " = new ByteArrayInputStream(" + name + ");\n");
                    lsb.append("\t\tlStat.setBinaryStream(" + (i+1) + ", bais" + i + ", " + name + ".length);\n");
                } else if(type.equals("int")) {
                    lsb.append("\t\tlStat.setInt(" + (i+1) + ", " + name + ");\n");
                } else if(type.equals("long")) {
                    lsb.append("\t\tlStat.setLong(" + (i+1) + ", " + name + ");\n");
                } else if(type.equals("double")) {
                    lsb.append("\t\tlStat.setDouble(" + (i+1) + ", " + name + ");\n");
                } else if(type.equals("char")) {
                    lsb.append("\t\tlStat.setObject(" + (i+1) + ", new Character(" + name + "));\n");
                } else if(type.equals("boolean")) {
                    lsb.append("\t\tlStat.setBoolean(" + (i+1) + ", " + name + ");\n");
                }
            }
        }
        lsb.append("\t\tint result = lStat.executeUpdate();\n");
        lsb.append("\t\tlStat.close();\n");
        lsb.append("\t\tthis.iUpdated = false;\n");
        lsb.append("\t\treturn result;\n");
        lsb.append("\t}\n");
        this.iCode = lsb.toString();
    }

    /**
     * This method will report on the generated code.
     *
     * @return  String  with the generated code.
     */
    public String toString() {
        return "\n" + this.iCode + "\n";
    }
}
