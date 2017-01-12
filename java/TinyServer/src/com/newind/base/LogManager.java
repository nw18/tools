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
		if(!LOG_FILE_PATH.equals("")){
			FileHandler fileHandler;
			try {
				fileHandler = new FileHandler();
				fileHandler.setLevel(Level.ALL);
				getLogger().addHandler(fileHandler);
			} catch (SecurityException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}else{
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
