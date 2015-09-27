package util;

import android.os.Environment;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Milenko on 16/07/2015.
 */
public class myLog {
    private static String fileName;
    private static String currentDateandTime;

    public static void initialize(String filePath) {
        int file_size;

        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
        currentDateandTime = sdf.format(new Date());

        fileName = currentDateandTime + "_mhp.txt";
        File logFile = new File(Environment.getExternalStorageDirectory() + filePath);
        file_size = Integer.parseInt(String.valueOf(logFile.length() / 1024));
        if (file_size > parameters.LogFileSize) logFile.delete();
        add("++++++++++++++++++++++++Session: " + currentDateandTime + "+++++++++++++++++++++++");

    }

    public static void add(String text) {
        add(text, "mhp");
    }

    /***
     * Add the text to a file which has TAG in the name. It also prints in this tag.
     *
     * @param text
     * @param TAG
     */
    public static void add(String text, String TAG) {
        Log.d(TAG, text);

        File logFile = new File(Environment.getExternalStorageDirectory(), "/WCLOG/" + currentDateandTime + "_" + TAG + ".txt");
        if (!logFile.exists()) {
            try {
                logFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss (dd)| ");
            String currentDateandTime = sdf.format(new Date());

            //BufferedWriter for performance, true to set append to file flag
            BufferedWriter buf = new BufferedWriter(new FileWriter(logFile, true));
            buf.append(currentDateandTime + text);
            buf.newLine();
            buf.flush();
            buf.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /***
     * Send unhandled errors to a text file in the phone
     *
     * @param activated
     */
    public static void WriteUnhandledErrors(boolean activated) {
        if (activated) {
            Thread.currentThread().setUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
                @Override
                public void uncaughtException(Thread thread, Throwable ex) {
                    PrintWriter pw;
                    try {
                        pw = new PrintWriter(
                                new FileWriter(Environment.getExternalStorageDirectory() + "/WCLOG//rt.txt", true));
                        ex.printStackTrace(pw);
                        pw.flush();
                        pw.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }


    public static void addError(Class<?> clase, Exception e) {
        add("-----Error en " + clase.getSimpleName() + ": " + e.getLocalizedMessage());
    }
}