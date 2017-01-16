package com.newind;

import java.util.logging.Logger;

import com.newind.base.LogManager;
import com.newind.http.HttpServer;

public class AppEntrance {
	private static Logger logger = LogManager.getLogger();
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			System.out.println("hello");
			AppConfig config = AppConfig.instacne();
			AppPooling.setup(config.getThread());
			config.load(args);
			final HttpServer httpServer = new HttpServer(config.getIp(), config.getHttpPort());
			httpServer.setup();
			Runtime.getRuntime().addShutdownHook(new Thread(){
				@Override
				public void run() {
					System.out.println("abort exit");
					httpServer.close();
					AppConfig.instacne().setShuttingDown(true);
					System.out.println("rlease worker");
					AppPooling.instance().release();
					System.out.println("rlease worker <<");
					System.out.println("abort exit <<");
				}
			});
			System.out.println("listen");
			httpServer.join();
			System.out.println("listen <<");
			System.out.println("main bye.");
		} catch (Exception e) {
			e.printStackTrace();
			logger.info("bye on exception.");
		} 
	}

}
