package com.newind.mycamera2;

import android.util.Log;

import java.text.SimpleDateFormat;

/**
 * Utility log tool.
 */
public class MyDebug {
    static final SimpleDateFormat SDF = new SimpleDateFormat("HH:mm:ss-SSS");
    private static final boolean LOG_OUT = true;
    private final static String LOG_TAG = "XXX";

    private static final int MAX_LOG_LEN = 3000;
    private static LogFile logFile = new LogFile();

    public static void forTest() {
        String strRes = "";
        StackTraceElement[] stackElements = new Throwable().getStackTrace();
        if (stackElements != null) {
            strRes = "" + stackElements[1];
        }
        d("" + strRes + " Only For Test");
    }

    public static void footPrint() {
        String strRes = "";
        StackTraceElement[] stackElements = new Throwable().getStackTrace();
        if (stackElements != null) {
            strRes = "" + stackElements[1];
        }
        strRes += " FootPrint: " + SDF.format(System.currentTimeMillis());
        i(strRes);
    }

    public static void footPrint(String msg) {
        i(String.format("FootPrint:%s at:%s", msg,
                SDF.format(System.currentTimeMillis())));
    }

    public static void ntImp() {
        String strRes = "";
        StackTraceElement[] stackElements = new Throwable().getStackTrace();
        if (stackElements != null) {
            strRes = "" + stackElements[1];
        }
        ntImp(strRes);
    }

    public static void ntImp(String funName) {
        d("" + funName + " Not Implemented");
    }

    public static void invoked() {
        String strRes = "";
        StackTraceElement[] stackElements = new Throwable().getStackTrace();
        if (stackElements != null) {
            strRes = "" + stackElements[1];
        }
        invoked(strRes);
    }

    public static void invoked(String funName) {
        e("" + funName + " Invoked");
    }

    public static void v(String log) {
        if (LOG_OUT) {
            int index = 0;
            String sub;
            int seg = 0;
            while (index < log.length()) {
                // java的字符不允许指定超过总的长度end
                if (log.length() <= index + MAX_LOG_LEN) {
                    sub = log.substring(index);
                } else {
                    sub = log.substring(index, index + MAX_LOG_LEN);
                }

                if (index != 0) {
                    Log.v(LOG_TAG, String.format("{%s}^^%d^^:%s", Thread.currentThread().getName(), seg, sub));
                } else {
                    Log.v(LOG_TAG, String.format("{%s}:%s", Thread.currentThread().getName(), sub));
                }
                index += MAX_LOG_LEN;
                seg++;
            }
            logFile.write("/V",LOG_TAG,log);
        }
    }

    public static void d(String log) {
        if (LOG_OUT) {
            int index = 0;
            String sub;
            int seg = 0;
            while (index < log.length()) {
                // java的字符不允许指定超过总的长度end
                if (log.length() <= index + MAX_LOG_LEN) {
                    sub = log.substring(index);
                } else {
                    sub = log.substring(index, index + MAX_LOG_LEN);
                }

                if (index != 0) {
                    Log.d(LOG_TAG, String.format("{%s}^^%d^^:%s", Thread.currentThread().getName(), seg, sub));
                } else {
                    Log.d(LOG_TAG, String.format("{%s}:%s", Thread.currentThread().getName(), sub));
                }
                index += MAX_LOG_LEN;
                seg++;
            }
            logFile.write("/D",LOG_TAG,log);
        }
    }

    public static void i(String log) {
        if (LOG_OUT) {
            int index = 0;
            String sub;
            int seg = 0;
            while (index < log.length()) {
                // java的字符不允许指定超过总的长度end
                if (log.length() <= index + MAX_LOG_LEN) {
                    sub = log.substring(index);
                } else {
                    sub = log.substring(index, index + MAX_LOG_LEN);
                }

                if (index != 0) {
                    Log.i(LOG_TAG, String.format("{%s}^^%d^^:%s", Thread.currentThread().getName(), seg, sub));
                } else {
                    Log.i(LOG_TAG, String.format("{%s}:%s", Thread.currentThread().getName(), sub));
                }
                index += MAX_LOG_LEN;
                seg++;
            }
            logFile.write("/I",LOG_TAG,log);
        }
    }

    public static void w(String log) {
        if (LOG_OUT) {
            int index = 0;
            String sub;
            int seg = 0;
            while (index < log.length()) {
                // java的字符不允许指定超过总的长度end
                if (log.length() <= index + MAX_LOG_LEN) {
                    sub = log.substring(index);
                } else {
                    sub = log.substring(index, index + MAX_LOG_LEN);
                }

                if (index != 0) {
                    Log.w(LOG_TAG, String.format("{%s}^^%d^^:%s", Thread.currentThread().getName(), seg, sub));
                } else {
                    Log.w(LOG_TAG, String.format("{%s}:%s", Thread.currentThread().getName(), sub));
                }
                index += MAX_LOG_LEN;
                seg++;
            }
            logFile.write("/W",LOG_TAG,log);
        }
    }

    public static void e(String log) {
        if (LOG_OUT) {
            Log.e(LOG_TAG, "{Thread:" + Thread.currentThread().getName() + "}:"
                    + log);
            logFile.write("/E",LOG_TAG,log);
        }
    }
}
