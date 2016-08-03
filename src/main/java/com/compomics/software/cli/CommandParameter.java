package com.compomics.software.cli;

import com.compomics.util.preferences.SequenceMatchingPreferences;
import java.util.ArrayList;
import java.util.List;

/**
 * Convenience methods for the validation of command line parameters.
 *
 * @author Marc Vaudel
 * @author Harald Barsnes
 */
public class CommandParameter {

    /**
     * Returns true if the input is an integer value inside the given range.
     *
     * @param argType the name of the argument
     * @param arg the content of the argument
     * @param minValue the minimum value allowed
     * @param maxValue the maximum value allowed
     * @return true if the input is an integer value inside the given range
     */
    public static boolean inIntegerRange(String argType, String arg, int minValue, int maxValue) {
        boolean valid = true;
        try {
            int value = new Integer(arg);
            if (value < minValue || value > maxValue) {
                System.out.println(System.getProperty("line.separator") + "Error parsing the " + argType + " option: Not in the range [" + minValue + " - " + maxValue + "]." + System.getProperty("line.separator"));
                valid = false;
            }
        } catch (NumberFormatException e) {
            System.out.println(System.getProperty("line.separator") + "Error parsing the " + argType + " option: Not an integer value!" + System.getProperty("line.separator"));
            valid = false;
        } catch (Exception e) {
            System.out.println(System.getProperty("line.separator") + "Error parsing the " + argType + " option: " + e.getLocalizedMessage() + System.getProperty("line.separator"));
            valid = false;
        }
        return valid;
    }

    /**
     * Returns true if the input is a double value inside the given range.
     *
     * @param argType the name of the argument
     * @param arg the content of the argument
     * @param minValue the minimum value allowed
     * @param maxValue the maximum value allowed
     * @return true if the input is a double value inside the given range
     */
    public static boolean inDoubleRange(String argType, String arg, double minValue, double maxValue) {
        boolean valid = true;
        try {
            double value = new Double(arg);
            if (value < minValue || value > maxValue) {
                System.out.println(System.getProperty("line.separator") + "Error parsing the " + argType + " option: Not in the range [" + minValue + " - " + maxValue + "]." + System.getProperty("line.separator"));
                valid = false;
            }
        } catch (NumberFormatException e) {
            System.out.println(System.getProperty("line.separator") + "Error parsing the " + argType + " option: Not a floating value!" + System.getProperty("line.separator"));
            valid = false;
        } catch (Exception e) {
            System.out.println(System.getProperty("line.separator") + "Error parsing the " + argType + " option: " + e.getLocalizedMessage() + System.getProperty("line.separator"));
            valid = false;
        }
        return valid;
    }

    /**
     * Returns true of the input is in the provided list.
     *
     * @param argType the name of the argument
     * @param arg the content of the argument
     * @param supportedInput the list of supported input
     * @return true of the input is in the list
     */
    public static boolean isInList(String argType, String arg, List<String> supportedInput) {
        boolean valid = true;
        if (!supportedInput.contains(arg)) {
            valid = false;
            String errorMessage = System.getProperty("line.separator") + "Error parsing the " + argType + " option: Found " + arg + ". Supported input: [";
            for (int i = 0; i < supportedInput.size(); i++) {
                if (i > 0) {
                    errorMessage += ", ";
                }
                errorMessage += supportedInput.get(i);
            }
            errorMessage += "]." + System.getProperty("line.separator");
            System.out.println(errorMessage);
        }
        return valid;
    }

    /**
     * Returns true of the input is 0 or 1.
     *
     * @param argType the name of the argument
     * @param arg the content of the argument
     * @return true of the input is 0 or 1
     */
    public static boolean isBooleanInput(String argType, String arg) {
        boolean valid = true;
        try {
            int value = new Integer(arg);
            if (value != 0 && value != 1) {
                System.out.println(System.getProperty("line.separator") + "Error parsing the " + argType + " option: Found " + value + " where 0 or 1 was expected." + System.getProperty("line.separator"));
                valid = false;
            }
        } catch (NumberFormatException e) {
            System.out.println(System.getProperty("line.separator") + "Error parsing the " + argType + " option: Found " + arg + " where 0 or 1 was expected." + System.getProperty("line.separator"));
            valid = false;
        } catch (Exception e) {
            System.out.println(System.getProperty("line.separator") + "Error parsing the " + argType + " option: " + e.getLocalizedMessage() + System.getProperty("line.separator"));
            valid = false;
        }
        return valid;
    }

    /**
     * Returns true if the argument can be parsed as an integer value.
     *
     * @param argType the name of the argument
     * @param arg the content of the argument
     * @return true if the argument can be parsed as an integer value
     */
    public static boolean isInteger(String argType, String arg) {
        boolean valid = true;
        try {
            new Integer(arg);
        } catch (NumberFormatException e) {
            System.out.println(System.getProperty("line.separator") + "Error parsing the " + argType + " option: Not an integer value!" + System.getProperty("line.separator"));
            valid = false;
        } catch (Exception e) {
            System.out.println(System.getProperty("line.separator") + "Error parsing the " + argType + " option: " + e.getLocalizedMessage() + System.getProperty("line.separator"));
            valid = false;
        }
        return valid;
    }

    /**
     * Returns true of the input is in the provided list.
     *
     * @param argType the name of the argument
     * @param arg the content of the argument
     * @return true of the input is in the list
     */
    public static boolean isSequenceMatchingType(String argType, String arg) {
        List<String> supportedInput = new ArrayList<String>(SequenceMatchingPreferences.MatchingType.values().length);
        for (SequenceMatchingPreferences.MatchingType tempMatchType : SequenceMatchingPreferences.MatchingType.values()) {
            supportedInput.add("" + tempMatchType.index);
        }
        return isInList(argType, arg, supportedInput);
    }

    /**
     * Returns true if the argument can be parsed as a double value.
     *
     * @param argType the name of the argument
     * @param arg the content of the argument
     * @return true if the argument can be parsed as a double value
     */
    public static boolean isDouble(String argType, String arg) {
        boolean valid = true;
        try {
            new Double(arg);
        } catch (NumberFormatException e) {
            System.out.println(System.getProperty("line.separator") + "Error parsing the " + argType + " option: Not a floating value!" + System.getProperty("line.separator"));
            valid = false;
        } catch (Exception e) {
            System.out.println(System.getProperty("line.separator") + "Error parsing the " + argType + " option: " + e.getLocalizedMessage() + System.getProperty("line.separator"));
            valid = false;
        }
        return valid;
    }

    /**
     * Returns true if the argument can be parsed as a positive double value.
     *
     * @param argType the name of the argument
     * @param arg the content of the argument
     * @param allowZero if true, zero values are allowed
     * @return true if the argument can be parsed as a positive double value
     */
    public static boolean isPositiveDouble(String argType, String arg, boolean allowZero) {
        boolean valid = true;
        try {
            double value = new Double(arg);
            if (allowZero) {
                if (value < 0) {
                    System.out.println(System.getProperty("line.separator") + "Error parsing the " + argType + " option: Negative value found." + System.getProperty("line.separator"));
                    valid = false;
                }
            } else if (value <= 0) {
                System.out.println(System.getProperty("line.separator") + "Error parsing the " + argType + " option: Negative or zero value found." + System.getProperty("line.separator"));
                valid = false;
            }
        } catch (NumberFormatException e) {
            System.out.println(System.getProperty("line.separator") + "Error parsing the " + argType + " option: Not a floating value!" + System.getProperty("line.separator"));
            valid = false;
        } catch (Exception e) {
            System.out.println(System.getProperty("line.separator") + "Error parsing the " + argType + " option: " + e.getLocalizedMessage() + System.getProperty("line.separator"));
            valid = false;
        }
        return valid;
    }

    /**
     * Returns true if the argument can be parsed as a positive integer value.
     *
     * @param argType the name of the argument
     * @param arg the content of the argument
     * @param allowZero if true, zero values are allowed
     * @return true if the argument can be parsed as a positive integer value
     */
    public static boolean isPositiveInteger(String argType, String arg, boolean allowZero) {
        boolean valid = true;
        try {
            int value = new Integer(arg);
            if (allowZero) {
                if (value < 0) {
                    System.out.println(System.getProperty("line.separator") + "Error parsing the " + argType + " option: Negative value found." + System.getProperty("line.separator"));
                    valid = false;
                }
            } else if (value <= 0) {
                System.out.println(System.getProperty("line.separator") + "Error parsing the " + argType + " option: Negative or zero value found." + System.getProperty("line.separator"));
                valid = false;
            }
        } catch (NumberFormatException e) {
            System.out.println(System.getProperty("line.separator") + "Error parsing the " + argType + " option: Not an integer value!" + System.getProperty("line.separator"));
            valid = false;
        } catch (Exception e) {
            System.out.println(System.getProperty("line.separator") + "Error parsing the " + argType + " option: " + e.getLocalizedMessage() + System.getProperty("line.separator"));
            valid = false;
        }
        return valid;
    }

}
