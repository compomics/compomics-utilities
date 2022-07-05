package com.compomics.util.examples;

/////////////////////////////////////////////////////////
//  Bare Bones Browser Launch                          //
//  Version 3.1 (June 6, 2010)                         //
//  By Dem Pilafian                                    //
//  Supports:                                          //
//     Mac OS X, GNU/Linux, Unix, Windows XP/Vista/7   //
//  Example Usage:                                     //
//     String url = "http://www.centerkey.com/";       //
//     BareBonesBrowserLaunch.openURL(url);            //
//  Public Domain Software -- Free to Use as You Like  //
/////////////////////////////////////////////////////////

import java.util.Arrays;
import javax.swing.JOptionPane;

/**
 * Class that makes it possible to open a URL in the default browser.
 */
public class BareBonesBrowserLaunch {

    // list of browsers
    private static final String[] browsers = {"google-chrome", "firefox",
        "opera", "epiphany", "konqueror", "conkeror", "midori", "kazehakase",
        "mozilla"};

    /**
     * Tries to open the given URL in the default browser.
     *
     * @param url the URL to open
     */
    public static void openURL(String url) {

        try {
            // attempt to use Desktop library from JDK 1.6+
            // code mimicks: java.awt.Desktop.getDesktop().browse()
            Class<?> d = Class.forName("java.awt.Desktop");
            d.getDeclaredMethod("browse", new Class[]{java.net.URI.class})
                    .invoke(d.getDeclaredMethod("getDesktop").invoke(null),
                            new Object[]{java.net.URI.create(url)});

        } catch (Exception ignore) { // library not available or failed

            String osName = System.getProperty("os.name");

            try {
                if (osName.startsWith("Mac OS")) {

                    Class.forName("com.apple.eio.FileManager")
                            .getDeclaredMethod("openURL", new Class[]{String.class})
                            .invoke(null, new Object[]{url});

                } else if (osName.startsWith("Windows")) {

                    Runtime.getRuntime().exec(
                            "rundll32 url.dll,FileProtocolHandler " + url);

                } else { // assume Unix or Linux

                    String browser = null;

                    for (String b : browsers) {

                        if (browser == null
                                && Runtime.getRuntime()
                                        .exec(new String[]{"which", b})
                                        .getInputStream().read() != -1) {

                            Runtime.getRuntime().exec(
                                    new String[]{browser = b, url});

                        }

                    }

                    if (browser == null) {
                        throw new Exception(Arrays.toString(browsers));
                    }
                }

            } catch (Exception e) {

                JOptionPane.showMessageDialog(null,
                        "Error attempting to launch web browser:\n" + e.getLocalizedMessage(),
                        "Could Not Open Web Page",
                        JOptionPane.ERROR_MESSAGE
                );

                e.printStackTrace();
            }
        }
    }
}
