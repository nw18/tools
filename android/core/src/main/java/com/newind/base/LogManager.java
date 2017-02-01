package com.newind.base;

import java.io.IOException;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

public class LogManager {
	public static String LOG_FILE_PATH = "application.log";
	/**
	 * 初始化logger
	 */
	static{
		FileHandler fileHandler = null;
		if(!LOG_FILE_PATH.equals("")){
			try {
				fileHandler = new FileHandler(LOG_FILE_PATH);
				fileHandler.setLevel(Level.ALL);
				getLogger().addHandler(fileHandler);
			} catch (SecurityException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}catch (Exception e){
				e.printStackTrace();
			}
		}
		if (null == fileHandler){
			ConsoleHandler consoleHandler = new ConsoleHandler();
			consoleHandler.setLevel(Level.INFO);
			getLogger().addHandler(consoleHandler);
		}
	}
	/**
	 * 返回默认logger
	 * @return
	 */
	public static Logger getLogger() {
		return Logger.getLogger("default");
	}
}
