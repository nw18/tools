package com.newind;

import java.io.File;
import java.util.logging.Logger;

import com.newind.base.LogManager;
import com.newind.ftp.FtpServer;
import com.newind.http.HttpServer;
import com.newind.util.TextUtil;

public class Application {
	private static Logger logger = LogManager.getLogger();
	private static HttpServer httpServer;
	private static FtpServer ftpServer;

	public void startServer(String[] args) throws Exception{
		System.out.println("hello");
		ApplicationConfig config = ApplicationConfig.instance();
		config.setShuttingDown(false);
		config.load(args);
		ApplicationPooling.setup(config.getThreadCount());
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
	
	public String getServerAddresses(){
		String addr = "";
		if (null != httpServer) {
			addr = "http://" + httpServer.getAddress();
		}
		if (null != ftpServer) {
			if (!TextUtil.isEmpty(addr)) {
				addr += "\n";
			}
			addr += "ftp://" + ftpServer.getAddress();
		}
		return addr;
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
			logger.info("bye.");
		} catch (Exception e) {
			e.printStackTrace();
			logger.info("bye with exception.");
		}
	}
	

	public static void print(File file,String myPrevPath,String chPrePath){
		System.out.println(myPrevPath + file.getName());
		if(file.isDirectory()){
			File[] files =  file.listFiles();
			for(int i = 0; files != null && i < files.length; i++){
				File subFile =  files[i];
				String nextPrePath = chPrePath;
				String subPrevPath = chPrePath;
				if(i == files.length - 1){
					nextPrePath += "└─";
					subPrevPath += "  ";
				}else{
					nextPrePath += "├─";
					subPrevPath += "│ ";
				}
				print(subFile,nextPrePath,subPrevPath);
			}
		}
	}
}
