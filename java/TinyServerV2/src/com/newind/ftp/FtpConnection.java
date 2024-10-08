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
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import com.newind.app.ApplicationConfig;
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
	private byte[] buffer;
	private File currentDirectory = new File(config.getRoot());
	private InputStream inputStream;
	private OutputStream outputStream;
	private static Map<String, Method> actionMap = new HashMap<>();
	private WorkStatus workStatus = WorkStatus.Init;
	private File renamingFile = null;
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
	
	public FtpConnection(byte[] buffer) {
		this.buffer = buffer;
	}


	private String readLine() throws IOException{
		ByteBuffer byteBuffer = ByteBuffer.wrap(buffer);
		while (byteBuffer.limit() > 1) {
			int ch = inputStream.read();
			if (ch < 0){
				return  null;
			}
			if(ch == '\r'){
				byteBuffer.put((byte)ch);
				if(inputStream.read() == '\n'){
					byteBuffer.put((byte)'\n');
					break;
				}else{
					throw new IOException("bad line end.");
				}
			}
			byteBuffer.put((byte)ch);
		}
		if (byteBuffer.limit() <= 1){
			throw new IOException("to long a line.");
		}
		byteBuffer.flip();
		return new String(buffer,0,byteBuffer.limit(),config.getCodeType());
	}

	@Override
	public void handle(Socket param) {
		try {
			attachTo(param);
			long last_recv_time = System.currentTimeMillis();
			sendResponse(FtpResponse.OK_SERVER_READY);
			while (true) {
				try {
					String cmdString = readLine();
					if(cmdString == null) break; //end of stream return null
					FtpCommand ftpCmd = new FtpCommand();
					logger.info(cmdString);
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
					if (System.currentTimeMillis() - last_recv_time > config.getConnectionTimeout()) {
						logger.info("connection timeout:" + param.getRemoteSocketAddress());
						break;
					}
				} catch (CloseConnection e) {
					logger.info("closing ftp connection.");
					break;
				} catch (Throwable e){
					e.printStackTrace(); //Unhandily exception
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
			//recycle the data transport thread.
			if (ftpTrasportation != null) {
				ftpTrasportation.cancel();
				ftpTrasportation = null;
			}
		}
	}
	
	private void attachTo(Socket sock) throws IOException{
		this.socket = sock;
		sock.setSoTimeout(config.getRecvTimeout());
		inputStream = sock.getInputStream();
		outputStream = sock.getOutputStream();
		
	}
	
	void sendResponse(String responseString) throws IOException {
		logger.info(responseString);
		outputStream.write(responseString.getBytes(config.getCodeType()));
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
		path = URLDecoder.decode(path, config.getCodeType());
		if (TextUtil.equal(path, "/")) {
			return new File(config.getRoot());
		}
		String absPath;
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
		return URLEncoder.encode(absPath.replace('\\', '/'),config.getCodeType());
	}
	
	void onUser(FtpCommand cmd) throws IOException {
		this.userName = cmd.getParam(0);
		if (TextUtil.equal(userName,config.getUserName())) {
			if (TextUtil.isEmpty(config.getPassWord())){
				sendResponse(FtpResponse.OK_USER_LOGON);
			}else {
				workStatus = WorkStatus.WaitPass;
				sendResponse(FtpResponse.PEND_PASS_WORD);
			}
		}else {
			sendResponse(FtpResponse.ERR_NOT_LOGIN);
		}
	}

	void onPass(FtpCommand cmd) throws IOException{
		if (workStatus != WorkStatus.WaitPass) {
			sendResponse(FtpResponse.ERR_NOT_LOGIN);
			return;
		}
		String passWord = cmd.getParam(0);
		if (TextUtil.equal(passWord,config.getPassWord())) {
			workStatus = WorkStatus.LogOn;
			sendResponse(FtpResponse.OK_USER_LOGON);
		}else {
			sendResponse(FtpResponse.ERR_NOT_LOGIN);
		}
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

	void onXPwd(FtpCommand cmd) throws IOException{
		this.onPwd(cmd);
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
			// in d:\ case, will lose the / seprator.
			if (!curPath.startsWith("/")) {
				curPath = "/" + curPath;
			}
		}
		sendResponse(String.format(FtpResponse.OK_FILE_CREATED, curPath));
	}
	
	void onCDUP(FtpCommand command) throws IOException{
		if (workStatus != WorkStatus.LogOn) {
			sendResponse(FtpResponse.ERR_NOT_LOGIN);
			return;
		}
		try{
			currentDirectory = checkAndGet("..");
		}catch (FileCheckFail e) {
			sendResponse(FtpResponse.ERR_NOT_TAKEN_ACTION);
			return;
		}
		sendResponse(FtpResponse.OK_COMMAND);
	}
	
	void onXCWD(FtpCommand command) throws IOException{
		onCWD(command);
	}
	
	void onCWD(FtpCommand command) throws IOException{
		if (workStatus != WorkStatus.LogOn) {
			sendResponse(FtpResponse.ERR_NOT_LOGIN);
			return;
		}
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
		if (workStatus != WorkStatus.LogOn) {
			sendResponse(FtpResponse.ERR_NOT_LOGIN);
			return;
		}
		ServerSocket socket = new ServerSocket(0);
		try {
			//quit the origin data connection
			if (ftpTrasportation != null) {
				ftpTrasportation.cancel();
				ftpTrasportation = null;
			}
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
		if (workStatus != WorkStatus.LogOn) {
			sendResponse(FtpResponse.ERR_NOT_LOGIN);
			return;
		}
		//quit the origin data connection
		if (ftpTrasportation != null) {
			ftpTrasportation.cancel();
			ftpTrasportation = null;
		}
		String ip = cmd.getParam(0);
		ip += "." + cmd.getParam(1);
		ip += "." + cmd.getParam(2);
		ip += "." + cmd.getParam(3);
		int port = (Integer.parseInt(cmd.getParam(4)) << 8) + Integer.parseInt(cmd.getParam(5));
		ftpTrasportation = new FtpTrasportation.PORT(ip, port);
		ftpTrasportation.start(this);
		sendResponse(FtpResponse.OK_COMMAND);
	}
	
	void onNLst(FtpCommand cmd) throws IOException{
		if (workStatus != WorkStatus.LogOn) {
			sendResponse(FtpResponse.ERR_NOT_LOGIN);
			return;
		}
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
		if (workStatus != WorkStatus.LogOn) {
			sendResponse(FtpResponse.ERR_NOT_LOGIN);
			return;
		}
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
		if (workStatus != WorkStatus.LogOn) {
			sendResponse(FtpResponse.ERR_NOT_LOGIN);
			return;
		}
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
	
	//write able commands begin
	void onStor(FtpCommand command) throws IOException{
		if (workStatus != WorkStatus.LogOn) {
			sendResponse(FtpResponse.ERR_NOT_LOGIN);
			return;
		}
		if (!config.isWritable()){
			sendResponse(FtpResponse.ERR_NOT_TAKEN_ACTION);
			return;
		}
		if (ftpTrasportation == null) {
			sendResponse(FtpResponse.ERR_NOT_TAKEN_ACTION);
			return;
		}
		try{
			File target = checkAndGet(command.getParam(0));
			ftpTrasportation.setTransport(target, MODE.UPLOAD);
		}catch (FileCheckFail e) {
			sendResponse(FtpResponse.ERR_NOT_TAKEN_ACTION);
		}
	}
	
	void onMKD(FtpCommand command) throws IOException {
		if (workStatus != WorkStatus.LogOn) {
			sendResponse(FtpResponse.ERR_NOT_LOGIN);
			return;
		}
		if (!config.isWritable()){
			sendResponse(FtpResponse.ERR_NOT_TAKEN_ACTION);
			return;
		}
		try {
			File target = checkAndGet(command.getParam(0));
			if(target.mkdirs()){
				sendResponse(String.format(FtpResponse.OK_FILE_CREATED, command.getParam(0)));
			}else {
				sendResponse(FtpResponse.FAIL_NOT_TAKEN_ACTION);
			}
		} catch (FileCheckFail e) {
			sendResponse(FtpResponse.ERR_NOT_TAKEN_ACTION);
		} catch (SecurityException e) {
			sendResponse(FtpResponse.ERR_NOT_TAKEN_ACTION);
		}
	}
	
	void onRNFR(FtpCommand command) throws IOException{
		if (workStatus != WorkStatus.LogOn) {
			sendResponse(FtpResponse.ERR_NOT_LOGIN);
			return;
		}
		if (!config.isWritable()){
			sendResponse(FtpResponse.ERR_NOT_TAKEN_ACTION);
			return;
		}
		try{
			File target = checkAndGet(command.getParam(0));
			if (!target.exists()) {
				sendResponse(FtpResponse.FAIL_NOT_TAKEN_ACTION);
			}else {
				workStatus = WorkStatus.RenameFrom;
				renamingFile = target;
				sendResponse(FtpResponse.PEND_NEXT_FILE_ACTION);
			}
		}catch (FileCheckFail e) {
			sendResponse(FtpResponse.FAIL_NOT_TAKEN_ACTION);
		}
	}
	
	void onRNTO(FtpCommand command) throws IOException{
		if (workStatus != WorkStatus.RenameFrom) {
			sendResponse(FtpResponse.ERR_NOT_LOGIN);
			return;
		}
		if (renamingFile == null) {
			sendResponse(FtpResponse.ERR_NOT_TAKEN_ACTION);
			return;
		}
		workStatus = WorkStatus.LogOn;
		try{
			File target = checkAndGet(command.getParam(0));
			if(renamingFile.renameTo(target)){
				sendResponse(String.format(FtpResponse.OK_FILE_OPERATION_EX,command.getParam(0),"renamed"));
			}else {
				sendResponse(FtpResponse.FAIL_NOT_TAKEN_ACTION);
			}
		}catch (FileCheckFail e) {
			sendResponse(FtpResponse.ERR_NOT_TAKEN_ACTION);
		}
		renamingFile = null;
	}
	
	void onRMD(FtpCommand command) throws IOException{
		if (workStatus != WorkStatus.LogOn) {
			sendResponse(FtpResponse.ERR_NOT_LOGIN);
			return;
		}
		if (!config.isWritable()){
			sendResponse(FtpResponse.ERR_NOT_TAKEN_ACTION);
			return;
		}
		try {
			File target = checkAndGet(command.getParam(0));
			if (target.exists() && target.isDirectory() && target.delete()) {
				sendResponse(String.format(FtpResponse.OK_FILE_OPERATION_EX,command.getParam(0),"removed"));
			}else {
				sendResponse(FtpResponse.ERR_NOT_TAKEN_ACTION);
			}
		} catch (FileCheckFail e) {
			sendResponse(FtpResponse.ERR_NOT_TAKEN_ACTION);
		}
	}
	
	void onDele(FtpCommand command) throws IOException{
		if (workStatus != WorkStatus.LogOn) {
			sendResponse(FtpResponse.ERR_NOT_LOGIN);
			return;
		}
		if (!config.isWritable()){
			sendResponse(FtpResponse.ERR_NOT_TAKEN_ACTION);
			return;
		}
		try {
			File target = checkAndGet(command.getParam(0));
			if (target.exists() && target.isFile() && target.delete()) {
				sendResponse(String.format(FtpResponse.OK_FILE_OPERATION_EX,command.getParam(0),"deleted"));
			}else {
				sendResponse(FtpResponse.FAIL_NOT_TAKEN_ACTION);
			}
		} catch (FileCheckFail e) {
			sendResponse(FtpResponse.ERR_NOT_TAKEN_ACTION);
		}
		
	}
	
	//write able commands end
	void onSize(FtpCommand command) throws IOException{
		if (workStatus != WorkStatus.LogOn) {
			sendResponse(FtpResponse.ERR_NOT_LOGIN);
			return;
		}
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
