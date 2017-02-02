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
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.logging.Logger;

import com.newind.ApplicationConfig;
import com.newind.base.LogManager;

public abstract class FtpTrasportation extends Thread{
	private static final int CONN_TIMEOUT = 10 * 1000;
	public static final int EVENT_OK_CONN = 1;
	public static final int EVENT_ERR_CONN = -1;
	public static final int EVENT_OK_FILE_STATUS = 2;
	public static final int EVENT_OK_FILE = 3;
	public static final int EVENT_ERR_FILE = -2;
	public static final int EVENT_ABOR_FILE = -3;
	public static final int EVENT_EXIT = 0;
	protected Logger logger = LogManager.getLogger();
	protected ApplicationConfig config = ApplicationConfig.instance();
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
		NLST,
	}
	
	public interface Callback{
		void onResult(int result);
	}
	
	protected FtpTrasportation(){
		
	}
	
	public abstract void cancel();
	
	public void startTrans(MODE mode, File target) {
		if (callback == null || target == null) {
			return;
		}
		this.target = target;
	}
	
	@Override
	public void run() {
		while(true){
			synchronized (this) {
				if(!isTransing || target != null){
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
		if (!isTransing) {
			callback.onResult(EVENT_EXIT);
			return;
		}
		if ((workMode == MODE.UPLOAD && target.exists() && (!target.isFile() || !target.canWrite()))|| //upload but can't write
				(workMode != MODE.UPLOAD && !target.canRead())|| //download but can't read
				(target.isDirectory() && workMode != MODE.LIST && workMode != MODE.NLST)) {//download or upload a non dir.
			callback.onResult(EVENT_ERR_FILE);
			callback.onResult(EVENT_EXIT);
			return;
		}
		callback.onResult(EVENT_OK_FILE_STATUS);
		try{
		switch (workMode) {
			case LIST:
				listDir(true);
				break;
			case NLST:
				listDir(false);
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
			callback.onResult(EVENT_ABOR_FILE);
		}
		callback.onResult(EVENT_EXIT);
	}
	
	private void listDir(boolean showDetail) throws Exception{
		File files[] = target.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return !name.startsWith(".") || name.equals(".") || name.equals("..");
			}
		});
		OutputStream outputStream = socket.getOutputStream();
		Date date = new Date(0);
		SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd yyyy",Locale.US);
		SimpleDateFormat dateFormat2 = new SimpleDateFormat("MMM dd HH:mm",Locale.US);
		long current = System.currentTimeMillis();
		for(int i = 0;files != null && i < files.length; i++){
			File file = files[i];
			if (showDetail) {
				date.setTime(file.lastModified());
				String dateStr = 
						(current - file.lastModified()) < 365L * 24L * 3600L * 1000L ? 
						dateFormat2.format(date) : dateFormat.format(date);;
				String line = String.format("%c%c%cxr--r-- 1 tiny tiny %d %s %s\r\n", 
						file.isDirectory() ? 'd' : '-',file.canRead() ? 'r' : '-' , file.canWrite() ? 'w' : '-',
						file.length() , dateStr , file.getName());
				outputStream.write(line.getBytes(config.getCodeType()));
				System.out.print(line);
			}else {
				outputStream.write(String.format("%s\r\n", file.getName()).getBytes(config.getCodeType()));
			}
		}
		outputStream.flush();
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
	
	public void start(Callback callback){
		this.callback = callback;
		super.start();
	}
	
	@Override
	public synchronized void start() {
		System.out.println(1 / 0);
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
			try {
				join();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		@Override
		public void run() {
			try{
				socket = serverSocket.accept();
				serverSocket.close();
				if (!isTransing) {
					socket.close();
					callback.onResult(EVENT_EXIT);
					return;
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
			try {				
				join();
			} catch (Exception e) {
				e.printStackTrace();
			}
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
