package com.compomics.util.waiting;

import com.compomics.util.Util;

/**
 * Class used to measure duration of a process.
 *
 * @author Marc Vaudel
 */
public class Duration {

    /**
     * The system current time in milliseconds when the process was started.
     */
    private Long start = null;
    /**
     * The system current time in milliseconds when the process was ended.
     */
    private Long end = null;

    /**
     * Constructor for an empty instance. Start and end time can be set via the
     * start() and end() methods.
     */
    public Duration() {

    }

    /**
     * Constructor.
     *
     * @param start the process start time in milliseconds
     * @param end the process end time in milliseconds
     */
    public Duration(long start, long end) {
        this.start = start;
        this.end = end;
    }

    /**
     * Sets the start time to the system current time in milliseconds.
     */
    public void start() {
        start = System.currentTimeMillis();
    }

    /**
     * Sets the end time to the system current time in milliseconds.
     */
    public void end() {
        end = System.currentTimeMillis();
    }

    /**
     * Returns the duration in milliseconds.
     *
     * @return the duration in milliseconds
     */
    public long getDuration() {
        return end - start;
    }

    @Override
    public String toString() {

        if (start == null) {
            throw new IllegalArgumentException("Start time not set.");
        }

        if (end == null) {
            throw new IllegalArgumentException("End time not set.");
        }

        long processingTimeMilliseconds = end - start;
        double processingTimeSeconds = ((double) processingTimeMilliseconds) / 1000;
        double processingTimeMinutes = processingTimeSeconds / 60;
        int nMinutes = (int) processingTimeMinutes;
        double restSeconds = processingTimeSeconds - (60 * nMinutes);
        double processingTimeHours = ((double) nMinutes) / 60;
        int nHours = (int) processingTimeHours;
        int restMinutes = nMinutes - (60 * nHours);
        double processingTimeDays = ((double) nHours) / 24;
        int nDays = (int) processingTimeDays;
        int restHours = nHours - (24 * nDays);

        StringBuilder result = new StringBuilder();

        if (nDays > 0) {
            result.append(nDays);
            if (nDays == 1) {
                result.append(" day ");
            } else {
                result.append(" days ");
            }
        }

        if (restHours > 0) {
            result.append(restHours);
            if (restHours == 1) {
                result.append(" hour ");
            } else {
                result.append(" hours ");
            }
        }

        if (restMinutes > 0) {
            result.append(restMinutes);
            if (restMinutes == 1) {
                result.append(" minute ");
            } else {
                result.append(" minutes ");
            }
        }

        if (restSeconds < 1) {
            result.append(processingTimeMilliseconds).append(" milliseconds");
        } else if (restMinutes > 1) {
            int nSeconds = (int) restSeconds;
            result.append(nSeconds).append(" seconds");
        } else {
            result.append(Util.roundDouble(restSeconds, 3)).append(" seconds");
        }

        return result.toString();
    }
}
