package com.compomics.util.io;

import com.compomics.util.waiting.WaitingHandler;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import org.apache.commons.codec.binary.Base64;

/**
 * Utils for I/O and file handling.
 *
 * @author Marc Vaudel
 * @author Harald Barsnes
 */
public class IoUtil {

    /**
     * Default encoding, cf the second rule.
     */
    public static final String ENCODING = "UTF-8";
    /**
     * Default separator for tabular files.
     */
    public static final String DEFAULT_SEPARATOR = "\t";

    /**
     * Deletes all files and subdirectories under dir. Returns true if all
     * deletions were successful. If a deletion fails, the method stops
     * attempting to delete and returns false.
     *
     * @param dir the directory to delete
     *
     * @return rue if all deletions were successful
     */
    public static boolean emptyDir(
            File dir
    ) {

        if (dir.isDirectory()) {

            for (File child : dir.listFiles()) {

                boolean success = deleteDir(child);

                if (!success) {

                    return false;

                }
            }
        }

        return true;

    }

    /**
     * Deletes all files and subdirectories under dir and dir itself. Returns
     * true if all deletions were successful. If a deletion fails, the method
     * stops attempting to delete and returns false.
     *
     * @param dir the directory to delete
     *
     * @return rue if all deletions were successful
     */
    public static boolean deleteDir(
            File dir
    ) {

        boolean empty = emptyDir(dir);

        if (!empty) {

            return false;

        }

        return dir.delete();

    }

    /**
     * Copy the content of a file to another.
     *
     * @param in the file to copy from
     * @param out the file to copy to
     *
     * @throws IOException if a problem occurs when writing to the file
     */
    public static void copyFile(
            File in,
            File out
    ) throws IOException {

        copyFile(in, out, true);

    }

    /**
     * Appends the content of a file to another.
     *
     * @param in the file to copy from
     * @param out the file to copy to
     *
     * @throws IOException if a problem occurs when writing to the file
     */
    public static void append(
            File in,
            File out
    ) throws IOException {

        copyFile(in, out, false);

    }

    /**
     * Copy the content of one file to another.
     *
     * @param in the file to copy from
     * @param out the file to copy to
     * @param overwrite boolean indicating whether out should be overwritten
     *
     * @throws IOException if an error occurred while reading or writing a file
     */
    public static void copyFile(
            File in,
            File out,
            boolean overwrite
    ) throws IOException {

        long start = 0;

        if (out.exists() && out.length() > 0) {

            if (overwrite) {

                out.delete();

            } else {

                start = out.length();

            }
        }

        try (FileChannel inChannel = new FileInputStream(in).getChannel()) {
            try (FileChannel outChannel = new FileOutputStream(out).getChannel()) {

                long bytesCopied = 0;

                while (bytesCopied < in.length()) {

                    bytesCopied += inChannel.transferTo(start + bytesCopied, inChannel.size(), outChannel);

                }

            }
        }
    }

    /**
     * An OS independent getName alternative. Useful if the path is provided as
     * a hardcoded string and opened in a different OS.
     *
     * @param filePath the file path as a string
     * @return the file name, or the complete path of no file name is detected
     */
    public static String getFileName(
            String filePath
    ) {

        String tempFileName = filePath;

        int slash1 = tempFileName.lastIndexOf("/");
        int slash2 = tempFileName.lastIndexOf("\\");

        int lastSlashIndex = Math.max(slash1, slash2);

        if (lastSlashIndex != -1) {
            tempFileName = tempFileName.substring(lastSlashIndex + 1);
        }

        return tempFileName;
    }

    /**
     * An OS independent getName alternative. Useful if the path is provided as
     * a hardcoded string and opened in a different OS.
     *
     * @param file the file
     * @return the file name, or the complete path of no file name is detected
     */
    public static String getFileName(
            File file
    ) {
        return getFileName(file.getAbsolutePath());
    }

    /**
     * Returns the extensions of a file.
     *
     * @param file the file
     * @return the extension of a file
     */
    public static String getExtension(
            File file
    ) {

        String fileName = getFileName(file.getAbsolutePath());

        return getExtension(fileName);

    }

    /**
     * Returns the extensions of a file name.
     *
     * @param fileName The file name.
     *
     * @return The extension of the file name.
     */
    public static String getExtension(
            String fileName
    ) {

        int index = fileName.lastIndexOf(".");

        return index > 0 ? fileName.substring(index) : "";

    }

    /**
     * Returns the given file name with lower-case extension.
     *
     * @param fileName The name of the file.
     *
     * @return The name of the file with lower-case extension.
     */
    public static String getFilenameExtensionLowerCase(
            String fileName
    ) {

        return removeExtension(fileName) + getExtension(fileName).toLowerCase();

    }

    /**
     * Checks if the given file exists with the extension in another case and
     * returns it. Returns the given file otherwise.
     *
     * @param file The file to check.
     *
     * @return The existing file with another extension if it exists, the given
     * file otherwise.
     */
    public static File existsExtensionNotCaseSensitive(File file) {

        if (file.exists()) {

            return file;

        }

        File folder = file.getParentFile();
        String fileName = file.getName();
        String nameExtensionLowerCase = getFilenameExtensionLowerCase(fileName);

        for (File tempFile : folder.listFiles()) {

            String tempName = tempFile.getName();
            String tempNameExtensionLowerCase = getFilenameExtensionLowerCase(tempName);

            if (tempNameExtensionLowerCase.equals(nameExtensionLowerCase)) {

                return tempFile;

            }
        }

        return file;

    }

    /**
     * Appends a suffix to a file name before the file extension.
     *
     * @param fileName the file name
     * @param suffix the suffix to add
     * @return the file name with suffix
     */
    public static String appendSuffix(
            String fileName,
            String suffix
    ) {

        String tempName;
        String extension;
        int extensionIndex = fileName.lastIndexOf(".");
        if (extensionIndex > -1) {
            tempName = fileName.substring(0, fileName.lastIndexOf("."));
            extension = fileName.substring(fileName.lastIndexOf("."));
        } else {
            tempName = fileName;
            extension = "";
        }
        return tempName + suffix + extension;
    }

    /**
     * Removes the extension from a file name or path.
     *
     * @param fileName the file name
     * @return the file name without extension
     */
    public static String removeExtension(
            String fileName
    ) {

        if (fileName.endsWith(".gz")) {

            fileName = fileName.substring(0, fileName.length() - 3);

        }

        int pointIndex = fileName.lastIndexOf(".");
        return pointIndex > 0 ? fileName.substring(0, pointIndex) : fileName;

    }

    /**
     * Save a file from a URL.
     *
     * @param saveFile the file to save to
     * @param targetUrlAsString the target URL as a string
     * @param fileSizeInBytes the file size in bytes
     * @param userName the user name
     * @param password the password
     * @param waitingHandler the waiting handler
     * @return the saved file
     *
     * @throws MalformedURLException thrown if an MalformedURLException occurs
     * @throws IOException thrown if an IOException occurs
     * @throws FileNotFoundException thrown if a FileNotFoundException occurs
     */
    public static File saveUrl(
            File saveFile,
            String targetUrlAsString,
            int fileSizeInBytes,
            String userName,
            String password,
            WaitingHandler waitingHandler
    )
            throws MalformedURLException, IOException, FileNotFoundException {

        BufferedInputStream in = null;
        FileOutputStream fout = null;

        try {
            boolean urlExists = checkIfURLExists(targetUrlAsString, userName, password);

            if (!urlExists) {
                if (targetUrlAsString.endsWith(".gz")) {
                    targetUrlAsString = targetUrlAsString.substring(0, targetUrlAsString.length() - 3);
                    saveFile = new File(saveFile.getAbsolutePath().substring(0, saveFile.getAbsolutePath().length() - 3));
                }
            }

            URL targetUrl = new URL(targetUrlAsString);
            URLConnection urlConnection = targetUrl.openConnection();

            if (password != null) {
                String userpass = userName + ":" + password;
                String basicAuth = "Basic " + new String(new Base64().encode(userpass.getBytes()));
                urlConnection.setRequestProperty("Authorization", basicAuth);
            }

            int contentLength = urlConnection.getContentLength();

            if (contentLength != -1) {
                waitingHandler.resetPrimaryProgressCounter();
                waitingHandler.setMaxPrimaryProgressCounter(contentLength);
            } else if (fileSizeInBytes != -1) {
                waitingHandler.resetPrimaryProgressCounter();
                contentLength = fileSizeInBytes;
                waitingHandler.setMaxPrimaryProgressCounter(contentLength);
            } else {
                waitingHandler.setPrimaryProgressCounterIndeterminate(true);
            }

            in = new BufferedInputStream(urlConnection.getInputStream());
            fout = new FileOutputStream(saveFile);
            long start = System.currentTimeMillis();

            final byte data[] = new byte[1024];
            int count;
            while ((count = in.read(data, 0, 1024)) != -1 && !waitingHandler.isRunCanceled()) {
                fout.write(data, 0, count);

                if (contentLength != -1) {
                    long now = System.currentTimeMillis();
                    if ((now - start) > 100) {
                        waitingHandler.setPrimaryProgressCounter((int) saveFile.length());
                        start = System.currentTimeMillis();
                    }
                }
            }
        } finally {
            if (in != null) {
                in.close();
            }
            if (fout != null) {
                fout.close();
            }
        }

        return saveFile;
    }

    /**
     * Check if a given URL exists.
     *
     * @param targetUrlAsString the URL to check
     * @param userName the user name
     * @param password the password
     *
     * @return true of it exists
     */
    public static boolean checkIfURLExists(
            String targetUrlAsString,
            String userName,
            String password
    ) {

        try {
            URL targetUrl = new URL(targetUrlAsString);
            URLConnection urlConnection = targetUrl.openConnection();

            if (password != null) {
                String userpass = userName + ":" + password;
                String basicAuth = "Basic " + new String(new Base64().encode(userpass.getBytes()));
                urlConnection.setRequestProperty("Authorization", basicAuth);
            }

            InputStream inputStream = urlConnection.getInputStream();
            inputStream.close();
            return true;
        } catch (FileNotFoundException e) {
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Returns the size of the file located at the given URL.
     *
     * @param url the url of the file
     *
     * @return the size of the file
     */
    public static int getFileSize(
            URL url
    ) {
        HttpURLConnection conn = null;
        try {
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("HEAD");
            conn.getInputStream();
            return conn.getContentLength();
        } catch (IOException e) {
            return -1;
        } finally {
            conn.disconnect();
        }
    }

    /**
     * Attempts at closing a buffer. Taken from
     * https://stackoverflow.com/questions/2972986/how-to-unmap-a-file-from-memory-mapped-using-filechannel-in-java.
     *
     * @param buffer the buffer to close
     */
    public static void closeBuffer(MappedByteBuffer buffer) {

        if (buffer == null || !buffer.isDirect()) {
            return;
        }

        boolean isOldJDK = System.getProperty("java.specification.version", "99").startsWith("1.");

        try {

            if (isOldJDK) {

                Method cleaner = buffer.getClass().getMethod("cleaner");
                cleaner.setAccessible(true);
                Method clean = Class.forName("sun.misc.Cleaner").getMethod("clean");
                clean.setAccessible(true);
                clean.invoke(cleaner.invoke(buffer));

            } else {

                Class unsafeClass;

                try {
                    unsafeClass = Class.forName("sun.misc.Unsafe");
                } catch (Exception ex) {
                    // jdk.internal.misc.Unsafe doesn't yet have an invokeCleaner() method,
                    // but that method should be added if sun.misc.Unsafe is removed.
                    unsafeClass = Class.forName("jdk.internal.misc.Unsafe");
                }

                Method clean = unsafeClass.getMethod("invokeCleaner", ByteBuffer.class);
                clean.setAccessible(true);
                Field theUnsafeField = unsafeClass.getDeclaredField("theUnsafe");
                theUnsafeField.setAccessible(true);
                Object theUnsafe = theUnsafeField.get(null);
                clean.invoke(theUnsafe, buffer);

            }

        } catch (Exception ex) {

        }
    }

}
