package com.newind.net;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
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
			logger.info("setup");
			while(running){
				if(running){
					handSocket(socket.accept());
				}else{
					break;
				}
			}
			logger.info("quiting");
		} catch (IOException e) {
			e.printStackTrace();
			logger.info("exception");
		}finally{
			try{
			socket.close();
			}catch(Exception e){
				
			}
			logger.info("exit");
		}
	}
	
	public void close() {
		running = false;
		Socket client = new Socket();
		try {
			SocketAddress address = new InetSocketAddress("127.0.0.1", socket.getLocalPort());
			logger.info("connecting " + address);
			client.connect(socket.getLocalSocketAddress(), 200);
			logger.info("connected");
		} catch (IOException e) {
			e.printStackTrace();
		}finally{
			try {
				client.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			logger.info("close finish.");
		}
	}
	
	protected abstract void handSocket(Socket peer);
}
