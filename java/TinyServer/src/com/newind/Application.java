package com.newind;

import java.util.logging.Logger;

import com.newind.base.LogManager;
import com.newind.ftp.FtpServer;
import com.newind.http.HttpServer;

public class Application {
	private static Logger logger = LogManager.getLogger();
	private static HttpServer httpServer;
	private static FtpServer ftpServer;

	public void startServer(String[] args) throws Exception{
		System.out.println("hello");
		ApplicationConfig config = ApplicationConfig.instance();
		ApplicationPooling.setup(config.getThreadCount());
		config.load(args);
		if (config.isHttpOn()) {			
			httpServer = new HttpServer(config.getIp(), config.getHttpPort());
			httpServer.start();
		}
		if (config.isFtpOn()) {			
			ftpServer = new FtpServer(config.getIp(), config.getFtpPort());
			ftpServer.start();
		}
	}
	
	public void waitServer() throws InterruptedException{
		System.out.println("waitServer.");
		if (null != httpServer) {
			httpServer.join();
			httpServer = null;
		}
		if (null != ftpServer) {
			ftpServer.join();
			ftpServer = null;
		}
		System.out.println("bye.");
	}

	public void closeServer(){
		System.out.println("close server start.");
		if (null != httpServer) {						
			httpServer.close();
		}
		if (null != ftpServer) {
			ftpServer.close();
		}
		ApplicationConfig.instance().setShuttingDown(true);
		System.out.println("rlease workers start.");
		ApplicationPooling.instance().release();
		System.out.println("rlease workers end.");
		System.out.println("close server end.");
	}

	public boolean isRunning(){
		return ftpServer != null || httpServer != null;
	}
	
	public static void main(String[] args) {
		try {
			ApplicationConfig config = ApplicationConfig.instance();
			config.setRoot("X:\\code_back"); //no end separator
			//config.setRoot("D:\\");//with end separator.
			config.setWritable(true);
			config.setJsonMode(true);
			final Application application = new Application();
			application.startServer(args);
			Runtime.getRuntime().addShutdownHook(new Thread(){
				@Override
				public void run() {
					application.closeServer();
				}
			});
			application.waitServer();
		} catch (Exception e) {
			e.printStackTrace();
			logger.info("bye on exception.");
		}
	}
}
