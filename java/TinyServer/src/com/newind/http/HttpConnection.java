package com.newind.http;

import java.io.*;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;
import java.util.logging.Logger;

import com.newind.ApplicationConfig;
import com.newind.base.LogManager;
import com.newind.base.Mime;
import com.newind.base.PoolingWorker;
import com.newind.util.CycleTest;
import com.newind.util.TextUtil;

public class HttpConnection implements PoolingWorker<Socket>{
	public static final String TAG = HttpConnection.class.getSimpleName();
	private Logger logger = LogManager.getLogger();
	private ApplicationConfig config = ApplicationConfig.instance();
	private byte[] buffer;
	private int MAX_TRUNK_SIZE = Math.min(config.getRecvBufferSize() / 2,1440);
	private int trunkOffset = 0;
	private File rootFile = new File(config.getRoot());
	private InputStream inputStream;
	private OutputStream outputStream;
	
	public HttpConnection(byte[] buffer) {
		this.buffer = buffer;
	}
	
	@Override
	public void handle(Socket param) {
		SocketAddress address = param.getRemoteSocketAddress();
		logger.info("handle connection from:" + address.toString());
		try{
			attachTo(param);
			long lastRequestTime = System.currentTimeMillis();
			while(true){
				try{
					String line = readLine();
					if (TextUtil.isEmpty(line)){
						break;
					}
					param.setSoTimeout(config.getConnectionTimeout());
					HttpHead header = HttpHead.init(line,config.getCodeType());
					while(!TextUtil.isEmpty((line = readLine()))){
						System.out.println(line);
						header.addHead(line);
					}
					lastRequestTime = System.currentTimeMillis();
					handleRequest(header);
					param.setSoTimeout(config.getRecvTimeout());
				}catch(SocketTimeoutException e){
					if (config.isShuttingDown()) {
						break;
					}
					if(System.currentTimeMillis() - lastRequestTime > config.getConnectionTimeout()){
						logger.info("connect timeout:" + param.getRemoteSocketAddress());
						break;
					}
				}
			}
		}catch(SocketException e){
			logger.info("SocketException " + e.getMessage());
		}catch (IOException | RuntimeException e){
			logger.warning("RuntimeException " + e.getMessage());
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
		param.setSoTimeout(config.getRecvTimeout());
		inputStream = param.getInputStream();
		outputStream = param.getOutputStream();
	}
	
	private String readLine() throws IOException{
		ByteBuffer byteBuffer = ByteBuffer.wrap(buffer);
		while (byteBuffer.limit() > 0) {
			int ch = inputStream.read();
			if (ch < 0){
				throw new IOException("stream end.");
			}
			if(ch == '\r'){
				if(inputStream.read() == '\n'){
					break;
				}else{
					throw new IOException("bad line end.");
				}
			}
			byteBuffer.put((byte)ch);
		}
		if (byteBuffer.limit() <= 0){
			throw new IOException("to long a line.");
		}
		byteBuffer.flip();
		return new String(buffer,0,byteBuffer.limit(),config.getCodeType());
	}

	private void readBoundary(byte[] boundary) throws IOException{
		int i = 0;
		while(i < boundary.length){
			if (boundary[i++] != inputStream.read()){
				throw new IOException("bad boundary.");
			}
		}
	}

	private int readToBoundary(CycleTest cycleTest, byte[] buffer) throws IOException{
		int i = 0;
		System.out.println();
		while (i < buffer.length){
			int ch = inputStream.read();
			System.out.print((char)ch);
			if (ch < 0){
				throw new IOException("unexpected stream end.");
			}
			if (cycleTest.isFull()){
				buffer[i++] = cycleTest.get(0);
			}
			cycleTest.put((byte) ch);
			if (cycleTest.isEqual()){
				break;
			}
		}
		return i;
	}
	
	private void sendResponse(String str) throws Exception{
		outputStream.write(str.getBytes(config.getCodeType()));
		outputStream.flush();
	}

	private void sendTrunk(byte[] data,int length) throws IOException{
		outputStream.write(String.format("%x\r\n", length).getBytes(config.getCodeType()));
		outputStream.write(data,0,length);
		outputStream.write(HttpResponse.CRLF);
		outputStream.flush();
	}

	void sendTrunkBegin(){
		trunkOffset = 0;
	}
	
	void sendTrunk(String response) throws IOException{
		byte[] data = response.getBytes(config.getCodeType());
		if (trunkOffset + data.length > MAX_TRUNK_SIZE ){
			sendTrunk(buffer,trunkOffset);
			trunkOffset = 0;
		}
		if(trunkOffset + data.length < MAX_TRUNK_SIZE)
		{
			System.arraycopy(data,0,buffer,trunkOffset,data.length);
			trunkOffset += data.length;
		}else {
			if (trunkOffset > 0){
				sendTrunk(buffer,trunkOffset);
				trunkOffset = 0;
			}
			sendTrunk(data,data.length);
		}
	}
	
	void sendTrunkEnd() throws IOException{
		if (trunkOffset > 0){
			sendTrunk(buffer,trunkOffset);
			trunkOffset = 0;
		}
		outputStream.write("0\r\n\r\n".getBytes(config.getCodeType()));
		outputStream.flush();
	}

	private File url2file(String url){
		return  TextUtil.equal(url, "/") ? rootFile : new File(rootFile,url.substring(1));
	}
	
	private void handleRequest(HttpHead header) throws Exception{
		logger.info("receive request:" + header);
		if (header.isGet() || header.isHead()) {
			handleRequestGet(header);
		}else if (header.isPost()){
			handleRequestPost(header);
		}
	}

	private void handleRequestGet(HttpHead header) throws Exception{
		//is is a inner resource,this use a query string.
		if (config.isResource(header.getUrl().substring(1))) {
			sendInnerResource(header.getUrl().substring(1),header.isHead());
			logger.info("transfer complete:" + header.getRawUrl());
			return;
		}

		File fileObject = url2file(header.getUrl());
		if (!fileObject.exists()
				|| !fileObject.getAbsolutePath().startsWith(config.getRoot())) {
			logger.warning("not found:\"" + fileObject.getAbsolutePath() + "\" in \"" + config.getRoot() + "\"");
			sendResponse(HttpResponse.FileNotFound());
			logger.info("file not found:" + header.getRawUrl());
			return;
		}

		if (fileObject.isDirectory()) {
			if (config.isJsonMode()) {
				boolean isJsonRequest = true;
				if (fileObject == rootFile) {
					String accept = header.getHeadString("accept");
					String json1 = "application/json", json2 = "text/javascript";
					isJsonRequest = accept.indexOf(json1) > 0 || accept.indexOf(json2) > 0;
				}
				if (!isJsonRequest) {
					sendInnerResource("application.html",header.isHead());
				}else {
					sendResponse(HttpResponse.OkayFileTrunked("application/json"));
					HttpResponse.listDirectoryJSONByTrunk(fileObject,rootFile,this);
				}
			}else {
				sendResponse(HttpResponse.OkayFileTrunked("text/html"));
				HttpResponse.listDirectoryHTML(fileObject, rootFile,this);
			}
		}else {
			String extString = fileObject.getName();
			int pos = extString.lastIndexOf('.');
			if (pos < 0) {
				extString = Mime.toContentType("");
			}else {
				extString = Mime.toContentType(extString.substring(pos + 1));
			}
			sendResponse(HttpResponse.OkayFile(fileObject.length(), extString));
			if (!header.isHead()) {
				FileInputStream fileStream = new FileInputStream(fileObject);
				sendResponse(fileStream);
			}
		}
		logger.info("transfer complete:" + fileObject.getAbsolutePath());
	}

	private void handleRequestPost(HttpHead header) throws Exception{
		String contentType = header.getHeadString("content-type");
		if (TextUtil.isEmpty(contentType)){
			throw new RuntimeException("empty content type.");
		}
		String[] contentTypeFields = contentType.replace(" ","").split(";");
		if (contentTypeFields == null
				|| contentTypeFields.length != 2
				|| !TextUtil.equal(contentTypeFields[0],"multipart/form-data")
				|| !contentTypeFields[1].startsWith("boundary=")){

			sendResponse(HttpResponse.OkayText("only multipart/form-data supported."));
			throw new RuntimeException("bad content type.");
		}
		byte[] boundary = ("--" + contentTypeFields[1].substring("boundary=".length()) + "\r\n").getBytes();
		File tarDir = url2file(header.getUrl());
		if (!tarDir.exists() || !tarDir.isDirectory()){
			sendResponse("directory not exists.");
			throw new RuntimeException("directory not exists.");
		}
		readBoundary(boundary);
		while (true) {
			String contentDisposition = readLine();
			if (TextUtil.isEmpty(contentDisposition)){
				break;
			}
			contentType = readLine();
			if (!contentDisposition.startsWith("Content-Disposition:")
					|| !contentType.startsWith("Content-Type:")){
				throw new IOException("bad content disposition / type.");
			}
			String emptyLine = readLine();
			if (!TextUtil.isEmpty(emptyLine)){
				throw new IOException("acquire empty line but:" + emptyLine);
			}
			String[] contentDispositionFields = contentDisposition.substring("Content-Disposition:".length()).split(";");
			String fileName = null;
			for (String field : contentDispositionFields){
				field = field.trim();
				if (field.startsWith("filename=")){
					fileName = field.substring("filename=".length());
					break;
				}
			}
			if (TextUtil.isEmpty(fileName)){
				throw new IOException("can't find file name.");
			}
			int pos = fileName.replace('\\','/').lastIndexOf('/');
			if (pos > 0){
				fileName = fileName.substring(pos + 1);
				fileName = fileName.replace("\"","");
			}
			FileOutputStream fileOutputStream = new FileOutputStream(new File(tarDir,fileName));
			try {
				CycleTest cycleTest = new CycleTest(boundary);
				while (!cycleTest.isEqual()) {
					int len = readToBoundary(cycleTest, buffer);
					if (len > 0) {
						fileOutputStream.write(buffer, 0, len);
					}
				}
			}catch (IOException e){
				throw e;
			}finally {
				fileOutputStream.close();
			}
			sendResponse(HttpResponse.OkayText("file save ok."));
		}
	}

	private void sendResponse(InputStream stream) throws IOException {
		int len = 0;
		while((len = stream.read(buffer,0,buffer.length)) > 0){
			outputStream.write(buffer, 0, len);
		}
		stream.close();
	}
	
	private void sendInnerResource(String filePath,boolean isHead) throws Exception{
		byte[] data = config.getResource(filePath);
		if (null == data) {
			sendResponse(HttpResponse.FileNotFound());
		}else {
			String extString = filePath;
			int pos = extString.lastIndexOf('.');
			if (pos < 0) {
				extString = Mime.toContentType("");
			}else {				
				extString = Mime.toContentType(extString.substring(pos + 1));
			}
			sendResponse(HttpResponse.OkayFile(data.length, extString));
			if(!isHead){
				outputStream.write(data);
			}
		}
	}
}
