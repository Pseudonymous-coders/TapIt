package org.pseudonymous.tapit.configs;

import android.util.Log;

/**
 * The Logger class handled all Logging and Debugging for the app.
 * It's a very simple wrapper so that the code doesn't look messy with a ton of String.formats in it
 * The developers prefer the C style of printing.
 */
public class Logger {
    private static final String LOG_TAG = "TapIt";

    /**
     * Debug log (Logcat)
     *
     * @param fmt  The string format
     * @param args Arguments for the string format
     */
    public static void Log(String fmt, Object... args) {
        Log.d(LOG_TAG, String.format(fmt, args));
    }

    /**
     * Warning log (Logcat)
     *
     * @param fmt  The string format
     * @param args Arguments for the string format
     */
    public static void LogWarning(String fmt, Object... args) {
        Log.w(LOG_TAG, String.format(fmt, args));
    }

    /**
     * Error log (Logcat)
     *
     * @param fmt  The string format
     * @param args Arguments for the string format
     */
    public static void LogError(String fmt, Object... args) {
        Log.e(LOG_TAG, String.format(fmt, args));
    }

}
