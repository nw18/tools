package com.newind.ftp;

import java.io.IOException;
import java.net.Socket;

import com.newind.net.TcpServer;

public class FtpServer extends TcpServer {

	public FtpServer(String ip, int port) throws IOException {
		super(ip, port);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void setup() {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void handSocket(Socket peer) {
		// TODO Auto-generated method stub
		
	}

}
