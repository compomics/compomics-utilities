package com.compomics.util.io.export;

/**
 * Enum of the different formats available for the exports.
 *
 * @author Marc Vaudel
 */
public enum ExportFormat {

    /**
     * Text.
     */
    text(0, "Text", "txt", "Tab separated text format"),
    /**
     * Excel file.
     */
    excel(1, "Excel", "xls", "Microsoft Excel format");

    /**
     * The index of the format.
     */
    public final int index;
    /**
     * The name of the format.
     */
    public final String name;
    /**
     * The extention to use for this format.
     */
    public final String extention;
    /**
     * Description of the format.
     */
    public final String description;

    /**
     * Constructor.
     *
     * @param index The index of the format
     * @param name The name of the format
     * @param extention The extention to use for this format
     * @param description Description of the format
     */
    private ExportFormat(int index, String name, String extention, String description) {
        this.index = index;
        this.name = name;
        this.extention = extention;
        this.description = description;
    }
    /**
     * The default export format to use for command line exports.
     */
    public static final ExportFormat commandLineDefaultOption = text;
    /**
     * The default export format to use for GUI exports.
     */
    public static final ExportFormat guiDefaultOption = excel;

    /**
     * Returns the command line description when the format is used as command
     * line argument.
     *
     * @return the command line description when the format is used as command
     * line argument
     */
    public static String getCommandLineOption() {
        StringBuilder options = new StringBuilder();
        for (ExportFormat exportFormat : values()) {
            if (options.length() == 0) {
                options.append("The format to use for the export: ");
            } else {
                options.append(", ");
            }
            options.append(exportFormat.extention).append(": ").append(exportFormat.description);
            if (exportFormat == commandLineDefaultOption) {
                options.append(" (default)");
            }
        }
        return options.toString();
    }

    /**
     * Returns the export format designed by the given command line option. Null
     * if not found.
     *
     * @param commandLineOption the command line option
     *
     * @return the export format designed by the given command line option
     */
    public static ExportFormat getFormatFromCommandLineOption(String commandLineOption) {
        for (ExportFormat exportFormat : values()) {
            if (commandLineOption.equals(exportFormat.extention)) {
                return exportFormat;
            }
        }
        return null;
    }
}
