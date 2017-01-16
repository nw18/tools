package com.newind.ftp;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.logging.Logger;

import com.newind.AppConfig;
import com.newind.base.LogManager;
import com.newind.base.PoolingWorker;

public class FtpConnection implements PoolingWorker<Socket> {
	public static final String TAG = FtpConnection.class.getSimpleName();
	private Logger logger = LogManager.getLogger();
	private AppConfig config = AppConfig.instacne();
	private byte[] buffer = new byte[config.getRecvBufferSize()]; 
	private File rootFile = new File(config.getRoot());
	private InputStream inputStream;
	private OutputStream outputStream;
	@Override
	public void handle(Socket param) {
		try {
			attachTo(param);
			long last_recv_time = System.currentTimeMillis();
			while (true) {
				try {
					int len = inputStream.read(buffer, 0, buffer.length);
					if (len <= 0) {
						logger.info(TAG + " socket closed.");
						param.close();
						break;
					}
					if(len >= buffer.length){
						//TODO bad command too long command.
						break;
					}
					String cmdString = new String(buffer,0,len);
					FtpCommand ftpCmd = FtpCommand.parse(cmdString);
					handCmd(ftpCmd.getCmdName(), ftpCmd.getCmdParaList());
				} catch (SocketTimeoutException e) {
					if (config.isShuttingDown()) {
						break;
					}
					if (System.currentTimeMillis() - last_recv_time > config
							.getConnectionTimeout()) {
						logger.info(TAG + " " + param.getRemoteSocketAddress()
								+ " connect timeout.");
						break;
					}
				}
			}
			param.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void attachTo(Socket sock) throws IOException{
		sock.setSoTimeout(config.getRecvTimeout());
		inputStream = sock.getInputStream();
		outputStream = sock.getOutputStream();
		
	}
	
	private void handCmd(String cmd,String...paras){
		switch (cmd) {
		case "PWD":
			break;
		case "CWD":
			break;
		default:
			break;
		}
	}
	
	private void sendResponse(String responseString) throws IOException {
		outputStream.write(responseString.getBytes("UTF-8"));
		outputStream.flush();
	}
}
