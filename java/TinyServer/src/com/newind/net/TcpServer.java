package com.newind.net;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Logger;

import com.newind.base.LogManager;

public abstract class TcpServer extends Thread{
	private boolean running = true;
	private ServerSocket socket;
	private Logger logger = LogManager.getLogger();
	public TcpServer(String ip,int port) throws IOException {
		socket = new ServerSocket();
		socket.bind(new InetSocketAddress(ip, port));
	}
	
	public void run() {
		try {
			logger.info("server setup");
			while(running){
				if(running){
					handSocket(socket.accept());
				}else{
					break;
				}
			}
			logger.info("server quiting");
		} catch (IOException e) {
			e.printStackTrace();
			logger.info("server exception");
		}finally{
			try{
				synchronized (this) {					
					socket.close();
				}
			}catch(Exception e){
				
			}
			logger.info("exit");
		}
	}
	
	public synchronized void close() {
		if (socket.isClosed()) {
			return;
		}
		running = false;
		Socket client = new Socket();
		try {
			logger.info("closing");
			client.connect(socket.getLocalSocketAddress(), 200);
			logger.info("closed");
		} catch (Exception e) {
			logger.info("close exception");
		}finally{
			try {
				client.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	protected abstract void handSocket(Socket peer);
}
