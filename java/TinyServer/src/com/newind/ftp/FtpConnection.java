package com.newind.ftp;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import com.newind.ApplicationConfig;
import com.newind.base.LogManager;
import com.newind.base.PoolingWorker;
import com.newind.ftp.FtpTrasportation.Callback;
import com.newind.ftp.FtpTrasportation.MODE;
import com.newind.util.TextUtil;

public class FtpConnection implements PoolingWorker<Socket>,Callback {
	enum WorkStatus{
		Init,
		WaitPass,
		LogOn,
		RenameFrom
	}

	private static class CloseConnection extends RuntimeException{
		private static final long serialVersionUID = 1L;
	}
	
	private static class FileCheckFail extends IOException {
		private static final long serialVersionUID = 1L;
		public FileCheckFail(String msg) {
			super(msg);
		}
	}

	public static final String TAG = FtpConnection.class.getSimpleName();
	private Logger logger = LogManager.getLogger();
	private ApplicationConfig config = ApplicationConfig.instance();
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
				} catch (Throwable e){
					e.printStackTrace(); //unhandle exception
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
	
	void execFtpCommand(FtpCommand ftpCommand) throws Throwable{
		Method method = actionMap.get(ftpCommand.getCmdName());
		if (method == null) {
			ftpCommand.setResult(FtpCommand.ERR);
			ftpCommand.setResponse(FtpResponse.ERR_COMMAND_NOT_IMPLEMENT);
			return;
		}
		ftpCommand.setResult(0);
		try{
			method.invoke(this, ftpCommand);
		}catch (InvocationTargetException e) {
			throw e.getCause();
		}
	}
	
	File checkAndGet(String path) throws IOException{
		String absPath;
		path = URLDecoder.decode(path, "UTF-8");
		if (path.startsWith("/") || path.startsWith("\\")) {
			absPath = config.getRoot() + path;
		}else{
			absPath = currentDirectory.getAbsolutePath() + File.separator + path;
		}
		absPath = absPath.replace("\\", "/");
		absPath = absPath.replace("//", "/");
		absPath = absPath.replace("/./", "/");
		String dirList[] = absPath.split("/");
		absPath = "";
		int upTimes = 0;
		for(int i = 0; i < dirList.length; i++){
			if (TextUtil.equal(dirList[i], "..")) {
				dirList[i] = "";
				upTimes++;
			}
		}
		for(int i = dirList.length - 1; i >= 0; i--){
			if (TextUtil.equal(dirList[i], "")) {
				continue;
			}
			if (upTimes > 0) {
				upTimes -= 1;
				continue;
			}
			absPath = "/" + dirList[i] + absPath;
		}
		if (!config.getRoot().startsWith("/") && absPath.startsWith("/")) {
			absPath = absPath.substring(1);
		}
		if (!absPath.startsWith(config.getRoot().replace('\\', '/'))) {
			throw new FileCheckFail("file out of root :" + absPath + "|" + config.getRoot());
		}
		return new File(absPath);
	}
	
	String getFilePath(File target) throws UnsupportedEncodingException{
		String absPath = target.getAbsolutePath().substring(config.getRoot().length());
		return URLEncoder.encode(absPath.replace('\\', '/'),"UTF-8");
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
		if ("I".equals(cmd.getParam(0)) || "A".equals(cmd.getParam(0))) {
			sendResponse(FtpResponse.OK_COMMAND);
		}else {
			sendResponse(FtpResponse.ERR_COMMAND_NOT_IMPLEMENT_PARAMETER);
		}
	}
	
	void onStru(FtpCommand cmd) throws IOException{
		if ("F".equals(cmd.getParam(0))) {
			sendResponse(FtpResponse.OK_COMMAND);
		}else {
			sendResponse(FtpResponse.ERR_COMMAND_NOT_IMPLEMENT_PARAMETER);
		}
	}
	
	void onMode(FtpCommand cmd) throws IOException{
		if ("S".equals(cmd.getParam(0))) {
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
	
	void onCDUP(FtpCommand command) throws IOException{
		try{
			currentDirectory = checkAndGet("..");
		}catch (FileCheckFail e) {
			sendResponse(FtpResponse.ERR_NOT_TAKEN_ACTION);
			return;
		}
		System.out.println("CDUP:" + currentDirectory.getAbsolutePath());
		sendResponse(FtpResponse.OK_COMMAND);
	}
	
	void onXCWD(FtpCommand command) throws IOException{
		onCWD(command);
	}
	
	void onCWD(FtpCommand command) throws IOException{
		File target = null;
		try{
			target = checkAndGet(command.getParam(0));
		}catch (FileCheckFail e) {
			sendResponse(FtpResponse.ERR_NOT_TAKEN_ACTION);
			return;
		}
		if (!target.exists() 
				|| !target.isDirectory()
				|| !target.canRead()) {
			sendResponse(FtpResponse.ERR_NOT_TAKEN_ACTION);
			return;
		}
		currentDirectory = target;
		sendResponse(String.format(FtpResponse.OK_FILE_OPERATION_EX, getFilePath(currentDirectory),"changed"));
	}
	
	void onPasv(FtpCommand cmd) throws IOException{
		ServerSocket socket = new ServerSocket(0);
		try {
			ftpTrasportation = new FtpTrasportation.PASV(socket);
			int port = socket.getLocalPort();
			String ipAddrss = this.socket.getLocalAddress().getHostAddress();
			ftpTrasportation.start(this);
			sendResponse(String.format(FtpResponse.OK_PASV_MODE, 
					ipAddrss.replace('.', ','),
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
		ip += "." + cmd.getParam(1);
		ip += "." + cmd.getParam(2);
		ip += "." + cmd.getParam(3);
		int port = (Integer.parseInt(cmd.getParam(4)) << 8) + Integer.parseInt(cmd.getParam(5));
		System.out.println("porting to :" + ip + ":" + port);
		ftpTrasportation = new FtpTrasportation.PORT(ip, port);
		ftpTrasportation.start(this);
		sendResponse(FtpResponse.OK_COMMAND);
	}
	
	void onNLst(FtpCommand cmd) throws IOException{
		if (ftpTrasportation == null) {
			sendResponse(FtpResponse.FAIL_NOT_TAKEN_ACTION);
			return;
		}
		try{
			File target = checkAndGet(cmd.getParam(0));
			ftpTrasportation.setTransport(target,MODE.NLST);
		}catch (FileCheckFail e) {
			sendResponse(FtpResponse.FAIL_NOT_TAKEN_ACTION);
		}
	}
	
	void onList(FtpCommand cmd) throws IOException{
		if (ftpTrasportation == null) {
			sendResponse(FtpResponse.FAIL_NOT_TAKEN_ACTION);
			return;
		}
		try{
			String path = cmd.getParam(0);
			if (path.startsWith("-")) {
				ftpTrasportation.setTransport(currentDirectory, MODE.LIST);
			}else {;
				File target = checkAndGet(cmd.getParam(0));
				ftpTrasportation.setTransport(target,MODE.LIST);
			}
		}catch (FileCheckFail e) {
			sendResponse(FtpResponse.FAIL_NOT_TAKEN_ACTION);
		}
	}
	
	void onRetr(FtpCommand cmd) throws IOException{
		if (ftpTrasportation == null) {
			sendResponse(FtpResponse.ERR_NOT_TAKEN_ACTION);
			return;
		}
		try{
			File target = checkAndGet(cmd.getParam(0));
			ftpTrasportation.setTransport(target,MODE.DOWNLOAD);
		}catch (FileCheckFail e) {
			sendResponse(FtpResponse.ERR_NOT_TAKEN_ACTION);
		}
	}
	
	void onStor(FtpCommand command) throws IOException{
		if (ftpTrasportation == null) {
			sendResponse(FtpResponse.FAIL_NOT_TAKEN_ACTION);
			return;
		}
		try{
			File target = checkAndGet(command.getParam(0));
			ftpTrasportation.setTransport(target, MODE.UPLOAD);
		}catch (FileCheckFail e) {
			sendResponse(FtpResponse.FAIL_NOT_TAKEN_ACTION);
		}
	}
	
	void onSize(FtpCommand command) throws IOException{
		try{
			File target = checkAndGet(command.getParam(0));
			if (target.exists() && target.isFile()) {
				sendResponse(String.format(FtpResponse.OK_FILE_SIZE, target.length()));
				return;
			}
		}catch (FileCheckFail e) {
			// TODO: handle exception
		}
		sendResponse(FtpResponse.FAIL_NOT_TAKEN_ACTION);
	}
	
	void onQuit(FtpCommand cmd) throws IOException,CloseConnection{
		sendResponse(FtpResponse.OK_COMMAND);
		throw new CloseConnection();
	}
	
	void onNoop(FtpCommand cmd) throws IOException{
		sendResponse(FtpResponse.OK_COMMAND);
	}
	
	@Override
	public void onResult(int result) {
		try{
		switch (result) {
			case FtpTrasportation.EVENT_OK_CONN:
				break;
			case FtpTrasportation.EVENT_OK_FILE:
				sendResponse(FtpResponse.OK_FILE_OPERATION);
				break;
			case FtpTrasportation.EVENT_OK_FILE_STATUS:
				sendResponse(FtpResponse.RD_FILE_AND_CONNECTION);
				break;
			case FtpTrasportation.EVENT_EXIT:
				ftpTrasportation = null;
				break;
			case FtpTrasportation.EVENT_ERR_CONN:
				sendResponse(FtpResponse.FAIL_OPEN_DATA_CONNECTION);
				break;
			case FtpTrasportation.EVENT_ERR_FILE:
				sendResponse(FtpResponse.FAIL_ON_IO_EXCEPTION);
				break;
			case FtpTrasportation.EVENT_ABOR_FILE:
				sendResponse(FtpResponse.FAIL_ABOR_DATA_TRANSPORT);
				break;
			default:
				break;
			}
		}catch (Exception e) {
			// TODO: handle exception
		}
	}
}
