package com.newind.http;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.URLDecoder;
import java.util.logging.Logger;

import com.newind.ApplicationConfig;
import com.newind.base.LogManager;
import com.newind.base.Mime;
import com.newind.base.PoolingWorker;
import com.newind.util.TextUtil;

public class HttpConnection implements PoolingWorker<Socket>{
	public static final String TAG = HttpConnection.class.getSimpleName();
	private Logger logger = LogManager.getLogger();
	private ApplicationConfig config = ApplicationConfig.instance();
	private byte[] buffer = new byte[config.getRecvBufferSize()];
	private int MAX_TRUNK_SIZE = Math.min(config.getRecvBufferSize() / 2,1440);
	private int trunkOffset = 0;
	private File rootFile = new File(config.getRoot());
	private InputStream inputStream;
	private OutputStream outputStream;
	@Override
	public void handle(Socket param) {
		SocketAddress address = param.getRemoteSocketAddress();
		logger.info("handle connection from:" + address.toString());
		try{
			attachTo(param);
			long last_recv_time = System.currentTimeMillis();
			while(true){
				try{
					int reqLen = inputStream.read(buffer);
					if (reqLen <= 0) {
						logger.info("socket closed.");
						param.close();
						break;
					}
					if (reqLen >= buffer.length) {
						logger.info("receive too long reuqest.");
						sendResponse(HttpResponse.BadRequest());
						inputStream.close();
						outputStream.close();
						param.close();
						break;
					}
					last_recv_time = System.currentTimeMillis();
					String reqString = new String(buffer, 0, reqLen);
					handleRequest(reqString);
				}catch(SocketTimeoutException e){
					if (config.isShuttingDown()) {
						break;
					}
					if(System.currentTimeMillis() - last_recv_time > config.getConnectionTimeout()){
						logger.info(param.getRemoteSocketAddress() + " connect timeout.");
						break;
					}
				}
			}
		}catch(SocketException e){
			logger.info("SocketException " + e.getMessage());
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
	
	private void sendResponse(String str) throws Exception{
		outputStream.write(str.getBytes(config.getCodeType()));
		outputStream.flush();
	}
	
	private void sendResponse(byte[] data) throws Exception{
		outputStream.write(data);
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
	
	private void handleRequest(String reqString) throws Exception{
		String fields[] = reqString.split("\r\n");
		if(fields.length < 1){
			sendResponse(HttpResponse.BadRequest());
			throw new RuntimeException("bad http request");
		}
		logger.info("receive request:" + fields[0]);
		String method[] = fields[0].split(" ");
		boolean isGet = TextUtil.equal(method[0].toUpperCase(),"GET");
		boolean isHead = TextUtil.equal(method[0].toUpperCase(), "HEAD");
		if (method.length != 3 || (!isGet && !isHead)) {
			sendResponse(HttpResponse.BadRequest());
			throw new RuntimeException("bad http request:" + fields[0]);
		}
		String filePath = URLDecoder.decode(method[1],config.getCodeType());
		if (filePath.startsWith("/")) {
			filePath = filePath.substring(1);
		}
		//hard code for FAVICON
		if (filePath.equals(HttpResponse.FAVICON)) {
			byte[] data = config.getResource(HttpResponse.FAVICON);
			if (data == null) {
				sendResponse(HttpResponse.FileNotFound());
			}else {
				sendResponse(HttpResponse.OkayFile(data.length, Mime.toContentType("png")));
				if(!isHead){
					sendResponse(data);
				}
			}
			return;
		}
		File fileObject = null;
		//is is a inner resource,this use a query string.
		if (filePath.startsWith("?")) {
			sendInnerResource(filePath.substring(1),isHead);
			return;
		}else {
			fileObject = TextUtil.isEmpty(filePath) ? rootFile : new File(rootFile,filePath);
			if (!fileObject.exists() 
					|| filePath.startsWith(".")
					|| !fileObject.getAbsolutePath().startsWith(config.getRoot())) {
				logger.warning("not found:\"" + fileObject.getAbsolutePath() + "\" in \"" + config.getRoot() + "\"");
				sendResponse(HttpResponse.FileNotFound());
				return;
			}
		}
		if (fileObject.isDirectory()) {
			byte[] data;
			String listString;
			if (config.isJsonMode()) {
				boolean isJsonRequest = true;
				if (fileObject == rootFile) {
					String json1 = "application/json", json2 = "text/javascript";
					for(int i = 0; i < fields.length; i++){
						String field = fields[i].toLowerCase();
						if (field.startsWith("accept:")) {
							isJsonRequest = field.indexOf(json1) > 0 || field.indexOf(json2) > 0;
							break;
						}
					}
				}
				if (!isJsonRequest) {
					sendInnerResource("application.html",isHead);
					return;
				}else {
//					listString = HttpResponse.listDirectoryJSON(fileObject, rootFile);
//					data = listString.getBytes(config.getCodeType());
//					sendResponse(HttpResponse.OkayFile(data.length, "application/json"));
					sendResponse(HttpResponse.OkayFileTrunked("application/json"));
					HttpResponse.listDirectoryJSONByTrunk(fileObject,rootFile,this);
					return;
				}
			}else {
				listString = HttpResponse.listDirectoryHTML(fileObject, rootFile);
				data = listString.getBytes(config.getCodeType());
				sendResponse(HttpResponse.OkayHtml(data.length));
			}
			if(!isHead){
				sendResponse(data);
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
			if (!isHead) {
				FileInputStream fileStream = new FileInputStream(fileObject);
				sendResponse(fileStream);
			}
		}
		logger.info("transfer complete:" + fileObject.getAbsolutePath());
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
