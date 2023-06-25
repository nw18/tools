package com.newind.util;

public class myLog {
    public static void debug(String msg) {
        System.out.println(msg);
    }

    public static void error(String msg) {
        System.out.println(msg);
    }

    public static void error(Exception e) {
        e.printStackTrace();
    }

    public static void info(String msg) {
        System.out.println(msg);
    }

    public static void invoke() {
        StackTraceElement[] stackInfo = Thread.currentThread().getStackTrace();
        if(stackInfo.length > 1) {
            debug(stackInfo[1].getClassName() + "\t\t\t\t" + stackInfo[1].getMethodName() + "\t\t\t\t" + stackInfo[1].getFileName());
        }
    }
}
