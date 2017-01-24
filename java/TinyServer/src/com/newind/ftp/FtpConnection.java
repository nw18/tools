package com.newind.ftp;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import com.newind.Config;
import com.newind.base.LogManager;
import com.newind.base.PoolingWorker;

public class FtpConnection implements PoolingWorker<Socket> {
	public static final String TAG = FtpConnection.class.getSimpleName();
	private Logger logger = LogManager.getLogger();
	private Config config = Config.instacne();
	private byte[] buffer = new byte[config.getRecvBufferSize()];
	private File currentDirectory = new File(config.getRoot());
	private InputStream inputStream;
	private OutputStream outputStream;
	private static Map<String, Method> actionMap = new HashMap<>();
	private WorkStatus workStatus = WorkStatus.Init;
	private FtpTrasportation ftpTrasportation;
	private String userName;
	private Socket socket;
	
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
					//TODO debug info
					System.out.println("recv:\n" + cmdString);
					ftpCmd.parse(cmdString);
					if (ftpCmd.getResult() != 0) {
						sendResponse(ftpCmd.getResponse());
						continue;
					}
					execFtpCommand(ftpCmd);
					if (ftpCmd.getResult() != 0) {
						sendResponse(ftpCmd.getResponse());
					}
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
		}finally {
			socket = null;
		}
	}
	
	private void attachTo(Socket sock) throws IOException{
		this.socket = sock;
		sock.setSoTimeout(config.getRecvTimeout());
		inputStream = sock.getInputStream();
		outputStream = sock.getOutputStream();
		
	}
	
	void sendResponse(String responseString) throws IOException {
		//TODO debug info
		System.out.println("sendResponse:\n" + responseString);
		outputStream.write(responseString.getBytes("UTF-8"));
		outputStream.flush();
	}
	
	void execFtpCommand(FtpCommand ftpCommand) throws CloseConnection , Exception{
		Method method = actionMap.get(ftpCommand.getCmdName());
		if (method == null) {
			ftpCommand.setResult(FtpCommand.ERR);
			ftpCommand.setResponse(FtpResponse.ERR_COMMAND_NOT_IMPLEMENT);
			return;
		}
		ftpCommand.setResult(0);
		method.invoke(this, ftpCommand);
	}
	
	enum WorkStatus{
		Init,
		WaitPass,
		LogOn,
		RenameFrom
	}

	void onUser(FtpCommand cmd) throws IOException {
		this.userName = cmd.getParam(0);
		System.out.println("onUser " + userName);
		workStatus = WorkStatus.WaitPass;
		sendResponse(FtpResponse.PEND_PASS_WORD);
	}

	void onPass(FtpCommand cmd) throws IOException{
		if (workStatus != WorkStatus.WaitPass) {
			sendResponse(FtpResponse.ERR_NOT_LOGIN);
			return;
		}
		String passWord = cmd.getParam(0);
		System.out.println("onPass " + passWord);
		workStatus = WorkStatus.LogOn;
		sendResponse(FtpResponse.OK_USER_LOGON);
	}
	
	void onSyst(FtpCommand cmd) throws IOException{
		sendResponse(FtpResponse.OK_SYSTEM_INFO);
	}
	
	void onType(FtpCommand cmd) throws IOException{
		if (cmd.getParam(0) == "I" || cmd.getParam(0) == "A") {
			sendResponse(FtpResponse.OK_COMMAND);
		}else {
			sendResponse(FtpResponse.ERR_COMMAND_NOT_IMPLEMENT_PARAMETER);
		}
	}
	
	void onStru(FtpCommand cmd) throws IOException{
		if (cmd.getParam(0) == "F") {
			sendResponse(FtpResponse.OK_COMMAND);
		}else {
			sendResponse(FtpResponse.ERR_COMMAND_NOT_IMPLEMENT_PARAMETER);
		}
	}
	
	void onMode(FtpCommand cmd) throws IOException{
		if (cmd.getParam(0) == "S") {
			sendResponse(FtpResponse.OK_COMMAND);
		}else {
			sendResponse(FtpResponse.ERR_COMMAND_NOT_IMPLEMENT_PARAMETER);
		}
	}
	
	void onPwd(FtpCommand cmd) throws IOException{
		if (workStatus != WorkStatus.LogOn) {
			sendResponse(FtpResponse.ERR_NOT_LOGIN);
			return;
		}
		String curPath = currentDirectory.getAbsolutePath();
		curPath = curPath.substring(config.getRoot().length());
		if (curPath.length() == 0) {
			curPath = "/";
		}else {			
			curPath = curPath.replace('\\', '/');
		}
		sendResponse(String.format(FtpResponse.OK_FILE_PATH, curPath));
	}
	
	void onPasv(FtpCommand cmd) throws IOException{
		ServerSocket socket = new ServerSocket(0);
		try {
			ftpTrasportation = new FtpTrasportation.PASV(socket);
			int port = socket.getLocalPort();
			String ipAddrss = this.socket.getLocalAddress().getHostAddress();
			ftpTrasportation.start();
			sendResponse(String.format(FtpResponse.OK_PASV_MODE, 
					ipAddrss.replaceAll(".", ","),
					port >> 8,
					port & 0xFF));
		} catch (Exception e) {
			e.printStackTrace();
			ftpTrasportation = null;
			socket.close();
			sendResponse(FtpResponse.ERR_NOT_TAKEN_ACTION);
		}
	}
	
	void onPort(FtpCommand cmd) throws IOException,Exception{
		String ip = cmd.getParam(0);
		ip += "," + cmd.getParam(1);
		ip += "," + cmd.getParam(2);
		ip += "," + cmd.getParam(3);
		int port = (Integer.parseInt(cmd.getParam(4)) << 8) + Integer.parseInt(cmd.getParam(5));
		ftpTrasportation = new FtpTrasportation.PORT(ip, port);
		ftpTrasportation.start();
		sendResponse(FtpResponse.OK_COMMAND);
	}
	
	void onList(FtpCommand cmd) throws IOException{
		File tarDir = null;
		if (cmd.getParamCount() == 0) {
			tarDir = currentDirectory;
		}else {
			String path = cmd.getParam(0);
			path = URLDecoder.decode(path,"UTF-8");
			if (path.startsWith("/")) {
				//absolute path
				
			}else {
				//relative path
				
			}
		}
	}
	
	void onNoop(FtpCommand cmd) throws IOException{
		sendResponse(FtpResponse.OK_COMMAND);
	}
	
	private static class CloseConnection extends RuntimeException{
		private static final long serialVersionUID = 1L;
	}
}
