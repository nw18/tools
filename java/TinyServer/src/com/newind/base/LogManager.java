package com.newind.base;

import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

public class LogManager {
	/**
	 * 初始化logger
	 */
	private static class MemoryHandler extends Handler {
		
		@Override
		public void publish(LogRecord arg0) {
			// TODO Auto-generated method stub
		}
		
		@Override
		public void flush() {
			// TODO Auto-generated method stub
		}
		
		@Override
		public void close() throws SecurityException {
			// TODO Auto-generated method stub
		}
	};
	
	private static MemoryHandler memoryHandler = new MemoryHandler();
	
	static{
		getLogger().addHandler(memoryHandler);
	}
	/**
	 * 返回默认logger
	 * @return
	 */
	public static Logger getLogger() {
		return Logger.getLogger("default");
	}
}
