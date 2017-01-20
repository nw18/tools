package com.newind.ftp;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.HashMap;
import java.util.Map;
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
	private static Map<String, Method> actionMap = new HashMap<>();
	private WorkStatus workStatus = WorkStatus.Init;
	private FtpTrasportation ftpTrasportation;
	private String userName;
	
	static {
		//load command method map.
		Method[] methods = FtpConnection.class.getDeclaredMethods();
		for(int i = 0; i < methods.length; i++){
			Method method = methods[i];
			Class<?>[] params = method.getParameterTypes();
			if (params.length == 1 && params[0].equals(FtpCommand.class)) {
				String name = method.getName();
				if (name.startsWith("on")) {					
					actionMap.put(name.substring(2).toUpperCase(), method);
				}
			}
		}
	}
	
	@Override
	public void handle(Socket param) {
		try {
			attachTo(param);
			long last_recv_time = System.currentTimeMillis();
			sendResponse(FtpResponse.OK_SERVER_READY);
			while (true) {
				try {
					int len = inputStream.read(buffer, 0, buffer.length);
					if (len <= 0) {
						logger.info(TAG + " socket closed.");
						param.close();
						break;
					}
					if(len >= buffer.length){
						logger.info("too long command!!!");
						break;
					}
					String cmdString = new String(buffer,0,len);
					FtpCommand ftpCmd = new FtpCommand();
					ftpCmd.parse(cmdString);
					if (ftpCmd.getResult() != 0) {
						sendResponse(ftpCmd.getResponse());
						continue;
					}
					execFtpCommand(ftpCmd);
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
				} catch (CloseConnection e) {
					logger.info("CloseConnection");
					break;
				} catch (Exception e){
					e.printStackTrace();
					break;
				}
			}
			param.close();
		} catch (IOException e) {
			e.printStackTrace();
		}catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void attachTo(Socket sock) throws IOException{
		sock.setSoTimeout(config.getRecvTimeout());
		inputStream = sock.getInputStream();
		outputStream = sock.getOutputStream();
		
	}
	
	void sendResponse(String responseString) throws IOException {
		outputStream.write(responseString.getBytes("UTF-8"));
		outputStream.flush();
	}
	
	void execFtpCommand(FtpCommand ftpCommand) throws CloseConnection , Exception{
		Method method = actionMap.get(ftpCommand.getCmdName());
		if (method == null) {
			ftpCommand.setResult(-1);
			ftpCommand.setResponse(FtpResponse.ERR_COMMAND_NOT_IMPLEMENT);
			return;
		}
		method.invoke(this, ftpCommand);
	}
	
	enum WorkStatus{
		Init,
		WaitPass,
		LogOn,
		RenameFrom
	}

	void onUser(FtpCommand cmd) throws CloseConnection, IOException {
		this.userName = cmd.getParam(0);
		System.out.println("onUser " + userName);
		workStatus = WorkStatus.WaitPass;
		sendResponse(FtpResponse.PEND_PASS_WORD);
	}

	void onPass(FtpCommand cmd) throws CloseConnection, IOException{
		if (workStatus != WorkStatus.WaitPass) {
			sendResponse(FtpResponse.ERR_NOT_LOGIN);
			return;
		}
		String passWord = cmd.getParam(0);
		System.out.println("onPass " + passWord);
		workStatus = WorkStatus.LogOn;
		sendResponse(FtpResponse.OK_USER_LOGON);
	}
	
	private static class CloseConnection extends RuntimeException{
		private static final long serialVersionUID = 1L;
	}
}
