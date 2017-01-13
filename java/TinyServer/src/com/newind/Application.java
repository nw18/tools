package com.newind;

import java.util.logging.Logger;

import com.newind.base.LogManager;
import com.newind.http.HttpConfig;
import com.newind.http.HttpServer;

public class Application {
	private static Logger logger = LogManager.getLogger();
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			System.out.println("hello");
			HttpConfig config = HttpConfig.instacne();
			config.load(args);
			final HttpServer httpServer = new HttpServer(config.getIp(), config.getPort());
			httpServer.setup();
			Runtime.getRuntime().addShutdownHook(new Thread(){
				@Override
				public void run() {
					logger.info("about to exit.");
					HttpConfig.instacne().setShuttingDown(true);
					System.exit(0);
				}
			});
			//listen thread end.
			httpServer.join();
			//release the pool.
			httpServer.release();
			logger.info("bye.");
		} catch (Exception e) {
			e.printStackTrace();
			logger.info("bye on exception.");
		} 
	}

}
