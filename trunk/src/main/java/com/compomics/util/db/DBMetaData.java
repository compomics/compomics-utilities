/*
 * Copyright (C) Lennart Martens
 * 
 * Contact: lennart.martens AT UGent.be (' AT ' to be replaced with '@')
 */

/*
 * Created by IntelliJ IDEA.
 * User: Lennart
 * Date: 17-dec-02
 * Time: 15:06:19
 */
package com.compomics.util.db;

import java.util.Vector;

/*
 * CVS information:
 *
 * $Revision: 1.3 $
 * $Date: 2007/07/06 09:41:53 $
 */

/**
 * This class wraps the metadata retrieved from a table on the DB.
 *
 * @author Lennart Martens
 */
public class DBMetaData {

    /**
     * The table name for which the metadata has been gathered.
     */
    private String iTable = null;

    /**
     * The column names.
     */
    private String[] iColumns = null;

    /**
     * The coded column types.
     */
    private int[] iColTypes = null;

    /**
     * The converted column types.
     */
    private String[] iConColTypes = null;

    /**
     * The column sizes.
     */
    private int[] iColSizes = null;

    /**
     * The primary key columns.
     */
    private String[] iPKColumns = null;

    /**
     * Constructor that allows the specification of table name,
     * column names and column types.
     *
     * @param   aTable  String with the table name
     * @param   aColumns    String[] with the column names
     * @param   aColTypes   int[] with the coded column types
     * @param   aColSizes   int[] with the column sizes
     */
    public DBMetaData(String aTable, String[] aColumns, int[] aColTypes, int[] aColSizes) {
        this(aTable, aColumns, aColTypes, aColSizes, new String[]{});
    }

    /**
     * Constructor that allows the specification of table name,
     * column names and column types and the primary key columns..
     *
     * @param   aTable  String with the table name
     * @param   aColumns    String[] with the column names
     * @param   aColTypes   int[] with the coded column types
     * @param   aColSizes   int[] with the column sizes
     * @param   aPKColumns  String[] with the names for the primary key columns.
     */
    public DBMetaData(String aTable, String[] aColumns, int[] aColTypes, int[] aColSizes, String[] aPKColumns) {
        if(aTable == null) {
            throw new IllegalArgumentException("Tablename was 'null'!");
        }

        if(aColumns.length != aColTypes.length) {
            throw new IllegalArgumentException("Columnnames count does not equal column types count: names (" + aColumns.length + ") <> types (" + aColTypes.length + ")!");
        }

        // Okay, we survived all the nasty checks.
        this.iTable = aTable;
        this.iColumns = aColumns;
        this.iColTypes = aColTypes;
        this.iColSizes = aColSizes;
        this.iConColTypes = ColumnTypeConverter.convertTypes(iColTypes, iColSizes);
        this.iPKColumns = aPKColumns;
    }

    /**
     * Constructor that allows the specification of table name,
     * column names and column types.
     *
     * @param   aTable  String with the table name
     * @param   aColumns    Vector with the column names (should contain only Strings)
     * @param   aColTypes   Vector with the coded column types (should contain only Integers)
     * @param   aColSizes   Vector with the column sizes (should contain only integers).
     */
    public DBMetaData(String aTable, Vector aColumns, Vector aColTypes, Vector aColSizes) {
        this(aTable, aColumns, aColTypes, aColSizes, new Vector());
    }

    /**
     * Constructor that allows the specification of table name,
     * column names, column types, column sizes and the primary key columns.
     *
     * @param   aTable  String with the table name
     * @param   aColumns    Vector with the column names (should contain only Strings)
     * @param   aColTypes   Vector with the coded column types (should contain only Integers)
     * @param   aColSizes   Vector with the column sizes (should contain only integers).
     * @param   aPKColumns  Vector with the names for the primary key columns.
     */
    public DBMetaData(String aTable, Vector aColumns, Vector aColTypes, Vector aColSizes, Vector aPKColumns) {
        if(aTable == null) {
            throw new IllegalArgumentException("Tablename was 'null'!");
        }

        if(aColumns.size() != aColTypes.size() || aColTypes.size() != aColSizes.size()) {
            throw new IllegalArgumentException("Columnnames count does not equal column types count and/or column size count: names (" + aColumns.size() + ") <> types (" + aColTypes.size() + ") <> sizes (" + aColSizes.size() + ")!");
        }

        String[] names = new String[aColumns.size()];
        aColumns.toArray(names);

        int liSize = aColTypes.size();
        int[] types = new int[liSize];
        int[] sizes = new int[liSize];
        for(int i = 0; i < liSize; i++) {
            Integer lInteger = (Integer)aColTypes.elementAt(i);
            types[i] = lInteger.intValue();
            sizes[i] = ((Integer)aColSizes.elementAt(i)).intValue();
        }

        // Primary key columns.
        liSize = aPKColumns.size();
        this.iPKColumns = new String[liSize];
        for(int i = 0; i < liSize; i++) {
            this.iPKColumns[i] = (String)aPKColumns.elementAt(i);
        }

        // Okay, we survived all the nasty checks.
        this.iTable = aTable;
        this.iColumns = names;
        this.iColTypes = types;
        this.iColSizes = sizes;
        this.iConColTypes = ColumnTypeConverter.convertTypes(iColTypes, iColSizes);
    }

    /**
     * This method reports on the tablename.
     *
     * @return  String  with the tablename.
     */
    public String getTableName() {
        return iTable;
    }

    /**
     * This method reports on all the column names.
     *
     * @return  String[]    with all the column names.
     */
    public String[] getColumnNames() {
        return iColumns;
    }

    /**
     * This method reports on all the coded column types.
     *
     * @return  int[]    with all the coded column types.
     */
    public int[] getCodedColumnTypes() {
        return iColTypes;
    }

    /**
     * This method reports on all the column sizes.
     *
     * @return  int[]    with all the column sizes.
     */
    public int[] getColumnSizes() {
        return iColSizes;
    }

    /**
     * This method reports on all the converted column types.
     *
     * @return  String[]    with all the converted column types.
     */
    public String[] getConvertedColumnTypes() {
        return iConColTypes;
    }

    /**
     * This method returns the column count for this table.
     *
     * @return  int with the column count.
     */
    public int getColumnCount() {
        return iColumns.length;
    }

    /**
     * This method returns the name of the specified column.
     *
     * @param   aColumnIndex    int with the index of the column
     *                          for which to retrieve the name.
     * @return  String  with the columnname, or 'null' if the
     *                  specified index was out of bounds.
     */
    public String getColumnName(int aColumnIndex) {
        String result = null;

        try {
            result = iColumns[aColumnIndex];
        } catch(ArrayIndexOutOfBoundsException aibe) {
            // Do nothing.
        }

        return result;
    }

    /**
     * This method returns the coded column type for the specified column.
     *
     * @param   aColumnName String with the column name.
     * @return  int with the coded column type for the specified column, or
     *              '-1' if the specified column was not found.
     */
    public int getCodedColumnType(String aColumnName) {
        int result = -1;

        for(int i = 0; i < iColumns.length; i++) {
            String lColumn = iColumns[i].trim();
            if(aColumnName.trim().equalsIgnoreCase(lColumn)) {
                result = iColTypes[i];
                break;
            }
        }

        return result;
    }

    /**
     * This method returns the column size for the specified column.
     *
     * @param   aColumnName String with the column name.
     * @return  int with the column size for the specified column, or
     *              '-1' if the specified column was not found.
     */
    public int getColumnSize(String aColumnName) {
        int result = -1;

        for(int i = 0; i < iColumns.length; i++) {
            String lColumn = iColumns[i].trim();
            if(aColumnName.trim().equalsIgnoreCase(lColumn)) {
                result = iColSizes[i];
                break;
            }
        }

        return result;
    }

    /**
     * This method returns the converted column type for the specified column.
     *
     * @param   aColumnName String with the column name.
     * @return  String  with the converted column type for the specified column, or
     *                  'null' if the specified column was not found.
     */
    public String getConvertedColumnType(String aColumnName) {
        String result = null;

        for(int i = 0; i < iColumns.length; i++) {
            String lColumn = iColumns[i].trim();
            if(aColumnName.trim().equalsIgnoreCase(lColumn)) {
                result = iConColTypes[i];
                break;
            }
        }

        return result;
    }


    /**
     * This method returns the coded columntype for the specified column index.
     *
     * @param   aColumnIndex    int with the column index
     * @return  int with the coded column type for the specified column, or '-1' if the
     *              specified index went out of range.
     */
    public int getCodedColumnType(int aColumnIndex) {
        int result = -1;

        try {
            result = iColTypes[aColumnIndex];
        } catch(ArrayIndexOutOfBoundsException aibe) {
            // Do nothing.
        }

        return result;
    }

    /**
     * This method returns the column size for the specified column index.
     *
     * @param   aColumnIndex    int with the column index
     * @return  int with the column size for the specified column, or '-1' if the
     *              specified index went out of range.
     */
    public int getColumnSize(int aColumnIndex) {
        int result = -1;

        try {
            result = iColSizes[aColumnIndex];
        } catch(ArrayIndexOutOfBoundsException aibe) {
            // Do nothing.
        }

        return result;
    }

    /**
     * This method returns the converted columntype for the specified column index.
     *
     * @param   aColumnIndex    int with the column index
     * @return  String  with the converted column type for the specified column, or
     *                  'null' if the specified index went out of range.
     */
    public String getConvertedColumnType(int aColumnIndex) {
        String result = null;

        try {
            result = iConColTypes[aColumnIndex];
        } catch(ArrayIndexOutOfBoundsException aibe) {
            // Do nothing.
        }

        return result;
    }

    /**
     * This method reports on the primary key columns for the table.
     *
     * @return  String[]    with the names of the primary key columns.
     */
    public String[] getPrimaryKeyColumns() {
        return this.iPKColumns;
    }

    /**
     * This method presents a String representation for this object.
     *
     * @return  String  with the String representation for this object.
     */
    public String toString() {
        StringBuffer lsb = new StringBuffer("---------------------------------------------------\n  This is the metadata for the table '" + iTable + "':\n\n");
        for(int i = 0; i < iColumns.length; i++) {
            lsb.append("\t- " + iColumns[i] + " : ");
            lsb.append(iColTypes[i] + " (" + iConColTypes[i] + ")\n");
        }
        lsb.append("\n\n  Primary keys:\n\n");
        for(int i = 0; i < iPKColumns.length; i++) {
            lsb.append("\t- " + iPKColumns[i] + "\n");
        }
        lsb.append("---------------------------------------------------\n");
        return lsb.toString();
    }
}
