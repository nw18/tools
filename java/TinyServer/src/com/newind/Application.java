package com.newind;

import java.util.logging.Logger;

import com.newind.base.LogManager;
import com.newind.ftp.FtpServer;
import com.newind.http.HttpServer;

public class Application {
	private static Logger logger = LogManager.getLogger();
	private static HttpServer httpServer;
	private static FtpServer ftpServer;
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			System.out.println("hello");
			ApplicationConfig config = ApplicationConfig.instacne();
			ApplicationPooling.setup(config.getThreadCount());
			config.load(args);
			//TODO remove HTTP server
//			httpServer = new HttpServer(config.getIp(), config.getHttpPort());
//			httpServer.start();
			ftpServer = new FtpServer(config.getIp(), config.getFtpPort());
			ftpServer.start();
			Runtime.getRuntime().addShutdownHook(new Thread(){
				@Override
				public void run() {
					System.out.println("receive quit signal.");
					if (null != httpServer) {						
						httpServer.close();
					}
					if (null != ftpServer) {
						ftpServer.close();
					}
					ApplicationConfig.instacne().setShuttingDown(true);
					System.out.println("rlease worker.");
					ApplicationPooling.instance().release();
					System.out.println("rlease worker <<");
					System.out.println("rlease exit <<");
				}
			});
			System.out.println("running.");
			if (null != httpServer) {
				httpServer.join();
			}
			if (null != ftpServer) {
				ftpServer.join();
			}
			System.out.println("bye.");
		} catch (Exception e) {
			e.printStackTrace();
			logger.info("bye on exception.");
		} 
	}

}
