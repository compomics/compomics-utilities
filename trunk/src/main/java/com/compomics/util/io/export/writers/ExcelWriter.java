package com.compomics.util.io.export.writers;

import com.compomics.util.io.export.ExportFormat;
import com.compomics.util.io.export.ExportWriter;
import com.compomics.util.io.export.WorkbookStyle;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;

/**
 * ExportWriter for the export to excel files.
 *
 * @author Marc Vaudel
 */
public class ExcelWriter extends ExportWriter {

    /**
     * The workbook for excel exports.
     */
    private HSSFWorkbook workbook;
    /**
     * The workbook style to use for an excel export.
     */
    private WorkbookStyle workbookStyle = null;
    /**
     * The destination file.
     */
    private final File destinationFile;
    /**
     * The sheet number of the current sheet.
     */
    private int sheetNumber;
    /**
     * The current sheet.
     */
    private HSSFSheet currentSheet;
    /**
     * The current hierarchical depth.
     */
    private int hierarchicalDepth = 0;
    /**
     * Map of the rows for depth change: depth -> starting row.
     */
    private final HashMap<Integer, Integer> collapsedRow = new HashMap<Integer, Integer>();
    /**
     * The current row number.
     */
    private int rowNumber = 0;
    /**
     * The current cell number.
     */
    private int cellNumber = 0;
    /**
     * The current row.
     */
    private HSSFRow currentRow = null;
    /**
     * The current cell content.
     */
    private StringBuilder currentCellContent = new StringBuilder();
    /**
     * The current cell style.
     */
    private CellStyle currentCellStyle = null;

    /**
     * Constructor.
     *
     * @param destinationFile the file where to write the data
     */
    public ExcelWriter(File destinationFile) {
        this.destinationFile = destinationFile;
        workbook = new HSSFWorkbook();
        exportFormat = ExportFormat.excel;
    }

    /**
     * Returns the workbook style.
     *
     * @return the workbook style
     */
    public WorkbookStyle getWorkbookStyle() {
        return workbookStyle;
    }

    /**
     * Sets the workbook style.
     *
     * @param workbookStyle the workbook style
     */
    public void setWorkbookStyle(WorkbookStyle workbookStyle) {
        this.workbookStyle = workbookStyle;
    }

    /**
     * Return the workBook.
     *
     * @return the workBook
     */
    public HSSFWorkbook getWorkbook() {
        return workbook;
    }

    @Override
    public void write(String text, WorkbookStyle textStyle) throws IOException {
        if (currentRow == null) {
            if (currentSheet == null) {
                throw new IllegalArgumentException("No section started to write in.");
            }
            currentRow = currentSheet.createRow(rowNumber);
            rowNumber++;
            if (textStyle != null) {
                currentRow.setHeightInPoints(textStyle.getStandardHeight());
            } else if (workbookStyle != null) {
                currentRow.setHeightInPoints(workbookStyle.getStandardHeight());
            } else {
                currentRow.setHeightInPoints(12.75f);
            }
        }
        if (textStyle != null) {
            currentCellStyle = textStyle.getStandardStyle(hierarchicalDepth);
        } else if (workbookStyle != null) {
            currentCellStyle = workbookStyle.getStandardStyle(hierarchicalDepth);
        }
        currentCellContent.append(text);
    }

    @Override
    public void writeMainTitle(String text, WorkbookStyle textStyle) throws IOException {

        if (text != null) {

            HSSFSheet sheet = workbook.createSheet(" ");
            sheet.setRowSumsBelow(false);
            HSSFRow row = sheet.createRow(0);

            Cell cell = row.createCell(0);
            cell.setCellValue(text);
            if (textStyle != null) {
                row.setHeightInPoints(textStyle.getMainTitleRowHeight());
                CellStyle cellStyle = textStyle.getStandardStyle(hierarchicalDepth);
                cell.setCellStyle(cellStyle);
            } else if (workbookStyle != null) {
                row.setHeightInPoints(workbookStyle.getMainTitleRowHeight());
                CellStyle cellStyle = workbookStyle.getStandardStyle(hierarchicalDepth);
                cell.setCellStyle(cellStyle);
            } else {
                row.setHeightInPoints(12.75f);
            }

            sheetNumber++;
        }
    }

    @Override
    public void startNewSection(String sectionTitle, WorkbookStyle textStyle) throws IOException {
        if (currentCellContent.length() > 0) {
            addSeparator();
            rowNumber = 0;
            cellNumber = 0;
        }
        String sheetName = sectionTitle;
        if (sheetName == null) {
            sheetName = sheetNumber++ + "";
        }
        currentSheet = workbook.createSheet(sheetName);
    }

    @Override
    public void writeHeaderText(String text, WorkbookStyle textStyle) throws IOException {
        if (currentRow == null) {
            if (currentSheet == null) {
                throw new IllegalArgumentException("No section started to write in.");
            }
            currentRow = currentSheet.createRow(rowNumber);
            rowNumber++;
            if (textStyle != null) {
                currentRow.setHeightInPoints(textStyle.getHeaderHeight());
            } else if (workbookStyle != null) {
                currentRow.setHeightInPoints(workbookStyle.getHeaderHeight());
            } else {
                currentRow.setHeightInPoints(12.75f);
            }
        }
        if (textStyle != null) {
            currentCellStyle = textStyle.getHeaderStyle(hierarchicalDepth);
        } else if (workbookStyle != null) {
            currentCellStyle = workbookStyle.getHeaderStyle(hierarchicalDepth);
        }
        currentCellContent.append(text);
    }

    @Override
    public void addSeparator() throws IOException {

        Cell cell = currentRow.createCell(cellNumber);
        cellNumber++;
        cell.setCellValue(currentCellContent.toString());
        currentCellContent = new StringBuilder();
        if (currentCellStyle != null) {
            cell.setCellStyle(currentCellStyle);
        }
    }

    @Override
    public void newLine() throws IOException {
        if (currentRow == null) {
            if (currentSheet == null) {
                throw new IllegalArgumentException("No section to write in.");
            }
            currentRow = currentSheet.createRow(rowNumber);
            rowNumber++;
        } else {
            if (currentCellContent.length() > 0) {
                addSeparator();
            }
            currentRow = null;
        }
        cellNumber = 0;
    }

    @Override
    public void close() throws IOException, FileNotFoundException {
        FileOutputStream fileOut = new FileOutputStream(destinationFile);
        try {
            workbook.write(fileOut);
        } finally {
            fileOut.close();
        }
    }

    @Override
    public void increaseDepth() {
        collapsedRow.put(++hierarchicalDepth, rowNumber);
    }

    @Override
    public void decreseDepth() {
        Integer originalRow = collapsedRow.get(hierarchicalDepth);
        if (originalRow == null) {
            throw new IllegalArgumentException("No original row found for hierarchical depth " + originalRow + ".");
        }
        if (hierarchicalDepth > 1) {
            currentSheet.groupRow(originalRow, rowNumber);
            currentSheet.setRowGroupCollapsed(originalRow, true);
        }
        hierarchicalDepth--;
    }
}
