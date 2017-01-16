package com.newind.http;

import java.io.IOException;
import java.net.Socket;
import java.net.SocketAddress;

import com.newind.AppPooling;
import com.newind.base.LogManager;
import com.newind.base.Pooling;
import com.newind.base.PoolingWorker;
import com.newind.net.TcpServer;

import java.util.logging.Logger;

public class HttpServer extends TcpServer{
	public static final String TAG = TcpServer.class.getSimpleName();
	private Logger logger = LogManager.getLogger();
	private Pooling<Socket, PoolingWorker<Socket>> pooling;
	public HttpServer(String ip, int port) throws IOException {
		super(ip, port);
		pooling = AppPooling.instance();
	}

	@Override
	protected void handSocket(Socket peer) {
		SocketAddress address = peer.getRemoteSocketAddress();
		logger.info(TAG + " from: " + address.toString());
		pooling.putTask(peer);
	}

	@Override
	public void setup() {
		logger.info(TAG + " setup");
		pooling.setup();
		start();
		logger.info(TAG + " setup finish");
	}

}
