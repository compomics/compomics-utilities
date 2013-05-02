package com.compomics.util.messages;

import java.util.ArrayList;

/**
 * This class is used to report a message to the user.
 *
 *
 * @author Marc Vaudel
 */
public class FeedBack {

    /**
     * The supported report types.
     */
    public enum FeedBackType {

        /**
         * A warning
         */
        WARNING,
        /**
         * A tip
         */
        TIP,
        /**
         * Feed from a stream like twitter
         */
        FEED
    }
    /**
     * The type of report.
     */
    private FeedBackType type;
    /**
     * The title of the report.
     */
    private String title;
    /**
     * The keywords related to this report. Keywords are used to contextualize
     * the feedback. Can be "iTRAQ", "label free".
     */
    private ArrayList<String> keywords;
    /**
     * The actual message.
     */
    private String message;

    /**
     * Creates a new report for the user.
     *
     * @param type the type of report
     * @param title the title of the report
     * @param keyWords the key words for this report
     * @param message the message of the report
     */
    public FeedBack(FeedBackType type, String title, ArrayList<String> keyWords, String message) {
        this.type = type;
        this.title = title;
        this.keywords = keyWords;
        this.message = message;
    }

    /**
     * Creates a warning object.
     *
     * @param title the title of the warning
     * @param message the message of the warning
     * @return the corresponding warning
     */
    public static FeedBack getWarning(String title, String message) {
        return new FeedBack(FeedBackType.WARNING, title, new ArrayList<String>(), message);
    }

    /**
     * Creates a tip object.
     *
     * @param title the title of the tip
     * @param message the message of the tip
     * @param keywords the keywords
     * @return the corresponding tip
     */
    public static FeedBack getTip(String title, String message, ArrayList<String> keywords) {
        return new FeedBack(FeedBackType.TIP, title, keywords, message);
    }

    /**
     * Creates a general tip with no key word.
     *
     * @param title the title of the tip
     * @param message the message
     * @return the corresponding tip
     */
    public static FeedBack getTip(String title, String message) {
        return getTip(title, message, new ArrayList<String>());
    }

    /**
     * Creates a feed object.
     *
     * @param title the title of the feed
     * @param message the message of the feed
     * @param keywords the keywords
     * @return the corresponding feed
     */
    public static FeedBack getFeed(String title, String message, ArrayList<String> keywords) {
        return new FeedBack(FeedBackType.FEED, title, keywords, message);
    }

    /**
     * Creates a general feed with no key word.
     * 
     * @param title the title of the feed
     * @param message the message
     * @return the corresponding feed
     */
    public static FeedBack getFeed(String title, String message) {
        return getFeed(title, message, new ArrayList<String>());
    }

    /**
     * Returns the key words for this report.
     *
     * @return the key words for this report
     */
    public ArrayList<String> getKeyWords() {
        return keywords;
    }

    /**
     * Sets the the key words for this report.
     *
     * @param keyWords the key words for this report
     */
    public void setKeyWords(ArrayList<String> keyWords) {
        this.keywords = keyWords;
    }

    /**
     * Returns the message.
     *
     * @return the message
     */
    public String getMessage() {
        return message;
    }

    /**
     * Sets the message.
     *
     * @param message the message
     */
    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * Returns the title.
     *
     * @return the title
     */
    public String getTitle() {
        return title;
    }

    /**
     * Sets the title.
     *
     * @param title the title
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Returns the feedback type.
     *
     * @return the feedback type
     */
    public FeedBackType getType() {
        return type;
    }

    /**
     * Sets the feedback type.
     *
     * @param type the feedback type
     */
    public void setType(FeedBackType type) {
        this.type = type;
    }
}
