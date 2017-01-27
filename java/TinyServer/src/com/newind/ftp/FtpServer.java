package com.newind.ftp;

import java.io.IOException;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.logging.Logger;

import com.newind.ApplicationPooling;
import com.newind.base.LogManager;
import com.newind.base.Pooling;
import com.newind.base.PoolingWorker;
import com.newind.net.TcpServer;

public class FtpServer extends TcpServer {

	private Logger logger = LogManager.getLogger();
	private Pooling<Socket, PoolingWorker<Socket>> pooling = ApplicationPooling.instance();
	public FtpServer(String ip, int port) throws IOException {
		super(ip, port);
	}

	@Override
	protected void handSocket(Socket peer) {
		SocketAddress address = peer.getRemoteSocketAddress();
		logger.info("connection from: " + address.toString());
		pooling.putTask(peer);
	}

}
