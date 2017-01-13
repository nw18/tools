package com.newind.net;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

public abstract class TcpServer extends Thread{
	private boolean running = true;
	private ServerSocket socket;
	public TcpServer(String ip,int port) throws IOException {
		socket = new ServerSocket();
		socket.bind(new InetSocketAddress(ip, port));
	}
	
	public void run() {
		try {
			while(running){
				if(running){
					handSocket(socket.accept());
				}else{
					break;
				}
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
	
	public void close() {
		running = false;
		Socket client = new Socket();
		try {
			client.connect(socket.getLocalSocketAddress(), 200);
		} catch (IOException e) {
			e.printStackTrace();
		}finally{
			try {
				client.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	protected abstract void release();
	
	protected abstract void setup();
	
	protected abstract void handSocket(Socket peer);
}
