package com.newind.http;

import java.io.File;

public class HttpConfig {
	private static HttpConfig _config_ = null;
	private String root = ".";
	private int port = 8080;
	private String ip = "0.0.0.0";
	private int maxThread = 16;
	private int recvBufferSize = 8 * 1024 + 1;
	private boolean isShuttingDown = false;
	private int recvTimeout = 5 * 1000;
	private int connectionTimeout = 5 * 60 * 1000;
	
	private HttpConfig(){
		
	}
	
	public static HttpConfig instacne() {
		if(null == _config_){
			_config_ = new HttpConfig();
		}
		return _config_;
	}
	
	public void load(String argv[]) throws Exception{
		for(String arg : argv){
			int pos = arg.indexOf(':');
			if (pos <= 0) {
				System.out.print("bad parameter :\n" + arg);
				break;
			}
			String value = arg.substring(pos + 1);
			switch (arg.substring(0, pos)) {
			case "root":
				while (value.endsWith("/") || value.endsWith("\\")) {
					value = value.substring(0, value.length() - 2);
				}
				File file = new File(value);
				if (!(file.exists() && file.isDirectory())) {
					throw new Exception(value + " not exists or unaccessable.");
				}
				root = value;
				break;
			case "port":
				port = Integer.parseInt(value);
				break;
			case "max_thread":
				maxThread = Integer.parseInt(value);
				break;
			default:
				break;
			}
		}
		//这块有问题,路径处理需要统一搞搞.
		if(root.startsWith(".")){
			root = new File(root).getAbsolutePath();
		}
		if (root.endsWith("/.")) {
			root = root.substring(0, root.length() - 2);
		}
	}
	
	public String getRoot() {
		return root;
	}
	
	public String getIp() {
		return ip;
	}
	
	public int getPort() {
		return port;
	}
	
	public int getMaxThread() {
		return maxThread;
	}
	
	public int getRecvBufferSize() {
		return recvBufferSize;
	}
	
	public boolean isShuttingDown() {
		return isShuttingDown;
	}
	
	public void setShuttingDown(boolean isShuttingDown) {
		this.isShuttingDown = isShuttingDown;
	}
	
	public int getRecvTimeout() {
		return recvTimeout;
	}
	
	public int getConnectionTimeout() {
		return connectionTimeout;
	}
}
