package com.newind.ftp;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Logger;

import com.newind.ApplicationConfig;
import com.newind.base.LogManager;

public abstract class FtpTrasportation extends Thread{
	private static final int CONN_TIMEOUT = 10 * 1000;
	public static final int EVENT_OK_CONN = 1;
	public static final int EVENT_ERR_CONN = -1;
	public static final int EVENT_OK_FILE = 2;
	public static final int EVENT_ERR_FILE = -2;
	public static final int EVENT_EXIT = 0;
	protected Logger logger = LogManager.getLogger();
	protected ApplicationConfig config = ApplicationConfig.instacne();
	protected Socket socket = null;
	protected Callback callback = null;
	protected Thread taskThread = null;
	protected File target = null;
	protected boolean isTransing = true;
	protected MODE workMode;
	
	public enum MODE{
		UPLOAD,
		DOWNLOAD,
		LIST,
	}
	
	public interface Callback{
		void onResult(int result);
	}
	
	protected FtpTrasportation(){
		
	}
	
	public abstract void cancel();
	
	public void startTrans(MODE mode, File target,Callback callback) {
		if (callback == null || target == null) {
			return;
		}
		this.callback = callback;
		this.target = target;
	}
	
	@Override
	public void run() {
		while(true){
			synchronized (this) {
				if(target != null){
					break;
				}				
			}
			//wait for task.
			try {
				sleep(16);
			} catch (InterruptedException e) {
				e.printStackTrace();
				return;
			}
		}
		
		if (target.isDirectory() && workMode != MODE.LIST) {
			callback.onResult(EVENT_ERR_FILE);
			callback.onResult(EVENT_EXIT);
			return;
		}
		try{
		switch (workMode) {
			case LIST:
				listDir();
				break;
			case UPLOAD:
				recvFile();
				break;
			case DOWNLOAD:
				sendFile();
				break;
			}
			socket.close();
		}catch (Exception e) {
			e.printStackTrace();
			callback.onResult(EVENT_ERR_FILE);
			callback.onResult(EVENT_EXIT);
			return;
		}
		if (isTransing) {
			callback.onResult(EVENT_OK_FILE);
		} else {
			callback.onResult(EVENT_ERR_FILE);
		}
		callback.onResult(EVENT_EXIT);
	}
	
	private void listDir() throws Exception{
		File files[] = target.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return !name.startsWith(".");
			}
		});
		OutputStream outputStream = socket.getOutputStream();
		for(int i = 0;files != null && i < files.length; i++){
			File file = files[i];
			outputStream.write(String.format("%s\r\n", file.getName()).getBytes());
		}
		outputStream.close();
	}
	
	private void recvFile() throws Exception{
		OutputStream outputStream = new FileOutputStream(target);
		InputStream inputStream = socket.getInputStream();
		byte buffer[] = new byte[config.getRecvBufferSize()];
		int len = -1;
		while((len = inputStream.read(buffer)) > 0){
			outputStream.write(buffer, 0, len);
		}
		outputStream.close();
		inputStream.close();
	}
	
	private void sendFile() throws Exception{
		OutputStream outputStream = socket.getOutputStream();
		InputStream inputStream = new FileInputStream(target);
		byte buffer[] = new byte[config.getRecvBufferSize()];
		int len = -1;
		while((len = inputStream.read(buffer)) > 0){
			outputStream.write(buffer, 0, len);
		}
		outputStream.close();
		inputStream.close();
	}
	
	public static class PASV extends FtpTrasportation{
		private ServerSocket serverSocket;
		public PASV(ServerSocket socket) {
			serverSocket = socket;
		}
		
		@Override
		public void cancel() {
			isTransing = false;
			Socket s = new Socket();
			try{
				if (!serverSocket.isClosed()) {					
					s.connect(serverSocket.getLocalSocketAddress());
					s.close();
				}
			}catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		@Override
		public void run() {
			try{
				socket = serverSocket.accept();
				serverSocket.close();
				if (!isTransing) {
					throw new Exception("manual exit.");
				}
			}catch(Exception e){
				try{
					socket.close();
					serverSocket.close();
				}catch (Exception ex) {
					
				}
				callback.onResult(EVENT_ERR_CONN);
				callback.onResult(EVENT_EXIT);
				return;
			}
			callback.onResult(EVENT_OK_CONN);
			super.run();
		}
	}
	
	public static class PORT extends FtpTrasportation{
		private String ip;
		private int port;
		public PORT(String ip,int port){
			this.ip = ip;
			this.port = port;
		}
		
		@Override
		public void cancel() {
			isTransing = false;
		}
		
		@Override
		public void run() {
			socket = new Socket();
			try{
				socket.connect(new InetSocketAddress(ip, port), CONN_TIMEOUT);
			}catch (Exception e) {
				try{
					socket.close();
				}catch (Exception ex) {
					socket = null;
				}
				callback.onResult(EVENT_ERR_CONN);
				callback.onResult(EVENT_EXIT);
				return;
			}
			callback.onResult(EVENT_OK_CONN);
			super.run();
		}
	}
	
	public synchronized void setTransport(File target,MODE mode){
		this.workMode = mode;
		this.target = target;
	}
}
