package com.newind.http;

import java.beans.Encoder;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.logging.Logger;

import com.newind.base.LogManager;
import com.newind.base.PoolingWorker;

public class HttpConnection implements PoolingWorker<Socket>{
	public static final String TAG = HttpConnection.class.getSimpleName();
	private Logger logger = LogManager.getLogger();
	private HttpConfig config = HttpConfig.instacne();
	private byte[] buffer = new byte[config.getRecvBufferSize()]; 
	private InputStream inputStream;
	private OutputStream outputStream;
	@Override
	public void handle(Socket param) {
		SocketAddress address = param.getRemoteSocketAddress();
		logger.info(TAG + " from:" + address.toString());
		try{
			attachTo(param);
			while(true){
				int reqLen = inputStream.read(buffer);
				if (reqLen <= 0) {
					logger.info(TAG + " socket closed.");
					param.close();
					break;
				}
				if (reqLen >= buffer.length) {
					logger.info(TAG + " too long reuqest.");
					sendResponse(HttpResponse.BadRequest());
					inputStream.close();
					outputStream.close();
					param.close();
					break;
				}
				String reqString = new String(buffer, 0, reqLen);
				logger.info(TAG + " receive:\n" + reqString);
				sendResponse(HttpResponse.OkayText("Hello TinySever."));
			}
		}catch (Exception e) {
			e.printStackTrace();
		}finally{
			outputStream = null;
			inputStream = null;
			try{
				param.close();
			}catch (Exception e) {
				logger.warning(TAG + " last close error.");
			}
		}
	}
	
	private void attachTo(Socket param) throws Exception{
		 inputStream = param.getInputStream();
		 outputStream = param.getOutputStream();
	}
	
	private void sendResponse(String str) throws Exception{
		outputStream.write(str.getBytes());
		outputStream.flush();
	}
}
