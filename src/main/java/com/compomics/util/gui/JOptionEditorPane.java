package com.compomics.util.gui;

import com.compomics.util.Util;
import com.compomics.util.examples.BareBonesBrowserLaunch;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

/**
 * A dialog to show HTML content with clickable links inside a JOptionsPane. 
 * 
 * @author Harald Barsnes
 */
public class JOptionEditorPane {

    /**
     * Returns a JEditorPane with HTML support to be used in a JOptionsPane.
     *
     * @param htmlMessage the message to show, can include HTML, e.g., links,
     * but not start and end HTML tags
     * @return a JEditorPane with HTML support
     */
    public static JEditorPane getJOptionEditorPane(String htmlMessage) {

        // create an empty label to put the message in
        JLabel label = new JLabel();

        // html content 
        JEditorPane ep = new JEditorPane(
                "text/html", "<html><body bgcolor=\"#" + Util.color2Hex(label.getBackground()) + "\">" + htmlMessage + "</body></html>");

        // handle link events 
        ep.addHyperlinkListener(new HyperlinkListener() {
            @Override
            public void hyperlinkUpdate(HyperlinkEvent e) {
                if (e.getEventType().equals(HyperlinkEvent.EventType.ACTIVATED)) {
                    BareBonesBrowserLaunch.openURL(e.getURL().toString());
                }
            }
        });

        ep.setBorder(null);
        ep.setEditable(false);

        return ep;
    }
}
