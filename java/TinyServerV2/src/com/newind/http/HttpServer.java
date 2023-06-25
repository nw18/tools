package com.newind.http;

import java.io.IOException;
import java.net.Socket;
import java.net.SocketAddress;

import com.newind.app.ApplicationPooling;
import com.newind.base.LogManager;
import com.newind.base.Pooling;
import com.newind.base.PoolingWorker;
import com.newind.net.TcpServer;

import java.util.logging.Logger;

public class HttpServer extends TcpServer{
	private Logger logger = LogManager.getLogger();
	private Pooling<Socket, PoolingWorker<Socket>> pooling = ApplicationPooling.instance();
	public HttpServer(String ip, int port) throws IOException {
		super(ip, port);
	}

	@Override
	public void handleConnection(Socket peer) {
		SocketAddress address = peer.getRemoteSocketAddress();
		logger.info("connection from: " + address.toString());
		pooling.putTask(peer);
	}
}
