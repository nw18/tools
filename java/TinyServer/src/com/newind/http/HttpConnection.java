package com.newind.http;

import java.io.IOException;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.logging.Logger;

import com.newind.base.LogManager;
import com.newind.base.PoolingWorker;

public class HttpConnection implements PoolingWorker<Socket>{
	public static final String TAG = HttpConnection.class.getSimpleName();
	private Logger logger = LogManager.getLogger();
	@Override
	public void handle(Socket param) {
		SocketAddress address = param.getRemoteSocketAddress();
		logger.info(TAG + " from:" + address.toString());
		try {
			param.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
