package com.newind.mycamera2;

import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by newind on 17-4-19.
 * 用于从MyDebug中截取log,命名为包名+时间 >> YJHomework目录
 * 文件大小超过一兆从新allocate文件.目前仅测试版输出此文件,正式版关闭.
 */

public class LogFile {
    private static final String LOG_DIR = "YJHomework";
    private static final int MAX_SIZE = 1024 * 1024;
    private FileOutputStream fileOutputStream;
    private byte[] CRLF = "\r\n".getBytes();
    private int sumLogLength = 0;
    LogFile() {
        resetLogFile();
    }

    private void resetLogFile() {

    }

    void write(String level, String tag, String log) {
        if (null == fileOutputStream) {
            return;
        }

        try {
            synchronized (this) {
                byte[] data = String.format("%s|%s|%s|%d|",level,MyDebug.SDF.format(System.currentTimeMillis()),tag, Thread.currentThread().getId()).getBytes();
                fileOutputStream.write(data);
                sumLogLength += data.length;
                data = log.getBytes();
                fileOutputStream.write(data);
                sumLogLength += data.length;
                fileOutputStream.write(CRLF);
                sumLogLength += CRLF.length;
            }
            fileOutputStream.flush();
            if (sumLogLength > MAX_SIZE) {
                synchronized (this) {
                    if (sumLogLength > MAX_SIZE) {
                        resetLogFile();
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
