package com.newind.ftp;

import java.io.File;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Logger;

import com.newind.base.LogManager;

public abstract class FtpTrasportation extends Thread{
	private static final int CONN_TIMEOUT = 10 * 1000;
	public static final int EVENT_OK_CONN = 1;
	public static final int EVENT_ERR_CONN = -1;
	public static final int EVENT_OK_FILE = 2;
	public static final int EVENT_ERR_FILE = -2;
	public static final int EVENT_EXIT = 0;
	protected Logger logger = LogManager.getLogger();
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
		
	}
	
	private void recvFile() throws Exception{
		
	}
	
	private void sendFile() throws Exception{
		
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
}
