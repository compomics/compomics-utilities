
package com.compomics.util.protein_sequences_manager;

import com.compomics.util.gui.waiting.waitinghandlers.ProgressDialogX;
import com.compomics.util.protein_sequences_manager.gui.sequences_import.ImportSequencesFromUniprotDialog;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;

/**
 * Helper class for downloading a file from a URL.
 * 
 * @author Kenneth Verheggen
 */
public class DownloadingUtil {

    /**
     * Download a file from a URL.
     * 
     * @param parent the parent
     * @param url the URL to download
     * @param outputFile the output file
     * @param progressDialog the progress dialog
     * @return true if the downloading worked
     * @throws IOException if an exception occurs
     */
    public static boolean downloadFileFromURL(ImportSequencesFromUniprotDialog parent, URL url, File outputFile, ProgressDialogX progressDialog) throws IOException {

        //start the downloading and update the dialog every read?
        BufferedInputStream in = null;
        FileOutputStream fout = null;

        try {
            in = new BufferedInputStream(url.openStream());
            fout = new FileOutputStream(outputFile);
            final byte data[] = new byte[1024];
            int totalCount = 0;
            int count;
            while ((count = in.read(data, 0, 1024)) != -1) {
                if (!parent.isCanceled() & !progressDialog.isRunCanceled()) {
                    fout.write(data, 0, count);
                    totalCount += count;
                    progressDialog.setSecondaryProgressText("Downloaded " + totalCount + " bytes");
                } else {
                    return false;
                }
            }
        } finally {
            if (in != null) {
                in.close();
            }
            if (fout != null) {
                fout.close();
            }
            progressDialog.setSecondaryProgressText("");
            progressDialog.resetSecondaryProgressCounter();
        }

        return true;
    }
}
