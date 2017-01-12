package com.newind;

import java.io.IOException;
import java.util.logging.Logger;

import com.newind.base.LogManager;
import com.newind.http.HttpConfig;
import com.newind.http.HttpServer;

public class Application {
	public static final int MAX_THREAD_COUNT = 256;
	private static Logger logger = LogManager.getLogger();
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			HttpConfig config = HttpConfig.instacne();
			config.load(args);
			final HttpServer httpServer = new HttpServer(config.getIp(), config.getPort());
			httpServer.setup();
			Runtime.getRuntime().addShutdownHook(new Thread(){
				@Override
				public void run() {
					logger.info("about to exit.");
					httpServer.release();
					logger.info("bye.");
					System.exit(0);
				}
			});
			while(true){
				try{
					Thread.sleep(1000);
				}catch (Exception e){
					e.printStackTrace();
					break;
				}
			}
			logger.info("bye from main.");
		} catch (Exception e) {
			e.printStackTrace();
		} 
	}

}
