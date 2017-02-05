package com.newind;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import com.newind.util.ResourceUtil;

public class ApplicationConfig {
	private static ApplicationConfig _config_ = null;
	private String root = ".";
	private int httpPort = 8080;
	private int ftpPort = 2121;
	private String ip = "0.0.0.0";
	private int threadCount = 64;
	private int recvBufferSize = 8 * 1024 + 1;
	private boolean isShuttingDown = false;
	private int recvTimeout = 5 * 1000;
	private int connectionTimeout = 5 * 60 * 1000;
	private String userName = "admin";
	private String passWord = "123456";
	private boolean writable = false;
	private boolean jsonMode = true;
	private String codeType = "UTF-8";
	private boolean ftpOn = true;
	private boolean httpOn = true;
	private Map<String, byte[]> resourceMap = new HashMap<>();
	
	public static ApplicationConfig instance() {
		if(null == _config_){
			_config_ = new ApplicationConfig();
		}
		return _config_;
	}
	
	public void load(String argv[]) throws Exception{
		if (argv == null) {
			return;
		}
		for(int i = 0; i < argv.length; i+=2){
			String key = argv[i];
			String value = argv[i+1];
			switch (key) {
			case "root":
				File file = new File(value);
				if (!(file.exists() && file.isDirectory())) {
					throw new Exception(value + " not exists or unaccessable.");
				}
				root = value;
				break;
			case "ip":
				ip = value;
				break;
			case "http_port":
				httpPort = Integer.parseInt(value);
				break;
			case "ftp_port":
				ftpPort = Integer.parseInt(value);
				break;
			case "thread_count":
				threadCount = Integer.parseInt(value);
				break;
			case "json_mode":
				jsonMode = Boolean.parseBoolean(value);
				break;
			case "writable":
				writable = Boolean.parseBoolean(value);
				break;
			case "code_type":
				codeType = value;
				break;
			case "user_name":
				userName = value;
				break;
			case "pass_word":
				passWord = value;
				break;
			case "ftp_on":
				ftpOn = Boolean.parseBoolean(value);
				break;
			case "http_on":
				httpOn = Boolean.parseBoolean(value);
				break;
			default:
				break;
			}
		}
		//remove the end /. or \. any better way?
		if(root.startsWith(".")){
			root = new File(root).getAbsolutePath();
		}
		if (root.endsWith("/.") || root.endsWith("\\.")) {
			root = root.substring(0, root.length() - 2);
		}
	}
	
	public String getRoot() {
		return root;
	}

	public void setRoot(String root) {
		this.root = root;
	}

	public String getIp() {
		return ip;
	}
	
	public int getHttpPort() {
		return httpPort;
	}
	
	public int getFtpPort() {
		return ftpPort;
	}
	
	public int getThreadCount() {
		return threadCount;
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
	
	public String getUserName() {
		return userName;
	}
	
	public void setUserName(String userName) {
		this.userName = userName;
	}
	
	public String getPassWord() {
		return passWord;
	}
	
	public void setPassWord(String passWord) {
		this.passWord = passWord;
	}

	public boolean isWritable() {
		return writable;
	}

	public void setWritable(boolean writable) {
		this.writable = writable;
	}
	
	public boolean isJsonMode() {
		return jsonMode;
	}
	
	public void setJsonMode(boolean jsonMode) {
		this.jsonMode = jsonMode;
	}
	
	public String getCodeType() {
		return codeType;
	}
	
	public void setCodeType(String codeType) {
		this.codeType = codeType;
	}
	
	public boolean isFtpOn() {
		return ftpOn;
	}
	
	public void setFtpOn(boolean ftpOn) {
		this.ftpOn = ftpOn;
	}
	
	public boolean isHttpOn() {
		return httpOn;
	}
	
	public void setHttpOn(boolean httpOn) {
		this.httpOn = httpOn;
	}
	
	public byte[] getResource(String name){
		synchronized (resourceMap) {
			if (resourceMap.size() == 0) {
				//TODO the inner resource per-load here
				addResource("favicon.ico");
				addResource("application.html");
				addResource("jquery-3.1.1.min.js");				
			}
		}
		return resourceMap.get(name);
	}
	
	private void addResource(String name){
		InputStream stream;
		try {
			stream = ResourceUtil.getResource(name);
			if (null == stream) {
				throw new IOException("Can't find " + name);
			}
			int length = 0;
			while(stream.read() >= 0){
				length++;
			}
			byte[] buffer = new byte[length];
			stream = ResourceUtil.getResource(name);
			for(int i = 0; i < buffer.length; i++){
				buffer[i] = (byte) stream.read();
			}
			resourceMap.put(name, buffer);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}

