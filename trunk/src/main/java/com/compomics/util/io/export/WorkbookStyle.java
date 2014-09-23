package com.compomics.util.io.export;

import org.apache.poi.ss.usermodel.CellStyle;

/**
 * This interface sets the style of a workbook export.
 *
 * @author Marc
 */
public interface WorkbookStyle {

    /**
     * Returns the cell style for the main title.
     * 
     * @return the cell style for the main title
     */
    public CellStyle getMainTitleStyle();
    
    /**
     * Returns the row height for the main title.
     * 
     * @return the row height for the main title
     */
    public float getMainTitleRowHeight();

    /**
     * Returns the standard cell style.
     * 
     * @return the standard cell style
     */
    public CellStyle getStandardStyle();

    /**
     * Returns the standard cell style for the given hierarchical depth. The depth is defined by the sublevel of the current cell.
     * 
     * @param hierarchicalDepth the hierarchical depth
     * 
     * @return the standard cell style for the given hierarchical depth
     */
    public CellStyle getStandardStyle(int hierarchicalDepth);
    
    /**
     * Returns the height of a standard line.
     * 
     * @return the height of a standard line
     */
    public float getStandardHeight();

    /**
     * Returns the standard cell style of a header.
     * 
     * @return the standard cell style of a header
     */
    public CellStyle getHeaderStyle();

    /**
     * Returns the standard cell style of a header at the given hierarchical depth. The depth is defined by the sublevel of the current cell.
     * 
     * @param hierarchicalDepth the hierarchical depth
     * 
     * @return the standard cell style for the given hierarchical depth
     */
    public CellStyle getHeaderStyle(int hierarchicalDepth);
    
    /**
     * Returns the height of a header row.
     * 
     * @return the height of a header row
     */
    public float getHeaderHeight();
    
    
}
