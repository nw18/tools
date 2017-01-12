package com.newind.net;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

public abstract class TcpServer extends Thread{
	private ServerSocket socket;
	public TcpServer(String ip,int port) throws IOException {
		socket = new ServerSocket();
		socket.bind(new InetSocketAddress(ip, port));
	}
	
	public void run() {
		try {
			while(true){
				handSocket(socket.accept());
			}
		} catch (IOException e) {
			e.printStackTrace();
		}finally{
			try{
			socket.close();
			}catch(Exception e){
				
			}
		}
	}
	
	protected abstract void release();
	
	protected abstract void setup();
	
	protected abstract void handSocket(Socket peer);
}
