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
	private File rootFile = new File(config.getRoot());
	private InputStream inputStream;
	private OutputStream outputStream;
	@Override
	public void handle(Socket param) {
		SocketAddress address = param.getRemoteSocketAddress();
		logger.info(TAG + " from:" + address.toString());
		try{
			attachTo(param);
			long last_recv_time = System.currentTimeMillis();
			while(true){
				try{
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
					last_recv_time = System.currentTimeMillis();
					String reqString = new String(buffer, 0, reqLen);
					//logger.info(TAG + " receive:\n" + reqString);
					handleRequest(reqString);
				}catch(SocketTimeoutException e){
					if (config.isShuttingDown()) {
						break;
					}
					if(System.currentTimeMillis() - last_recv_time > config.getConnectionTimeout()){
						logger.info(TAG + " " + param.getRemoteSocketAddress() + " connect timeout.");
						break;
					}
				}
			}
		}catch(SocketException e){
			logger.info("SocketException " + e.getMessage());
		}
		catch (Exception e) {
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
		logger.info(str);
		outputStream.write(str.getBytes(config.getCodeType()));
		outputStream.flush();
	}
	
	private void handleRequest(String reqString) throws Exception{
		String fields[] = reqString.split("\r\n");
		if(fields.length < 1){
			sendResponse(HttpResponse.BadRequest());
			throw new RuntimeException("bad http request");
		}
		logger.info(fields[0]);
		String method[] = fields[0].split(" ");
		if (method.length != 3) {
			sendResponse(HttpResponse.BadRequest());
			throw new RuntimeException("bad http request:" + fields[0]);
		}
		String filePath = URLDecoder.decode(method[1],config.getCodeType());
		if (filePath.startsWith("/")) {
			filePath = filePath.substring(1);
		}
		if (filePath.equals(HttpResponse.FAVICON)) {
			byte[] data = config.getResource(HttpResponse.FAVICON);
			if (data == null) { //TODO fix this code.
				sendResponse(HttpResponse.FileNotFound());
			}else {
				sendResponse(HttpResponse.OkayFile(data.length, Mime.toContentType("png")));
				outputStream.write(data);
				System.out.println("aha tiny logo !!!");
			}
			return;
		}
		File fileObject = null;
		if (filePath.startsWith("?")) {
			byte[] data = config.getResource(filePath.substring(1));
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
				outputStream.write(data);
			}
			return;
		}else {
			fileObject = TextUtil.isEmpty(filePath) ? rootFile : new File(rootFile,filePath);
			if (!fileObject.exists() 
					|| fileObject.isHidden()
					|| !fileObject.getAbsolutePath().startsWith(config.getRoot())) {
				logger.info(config.getRoot() + "\n" + fileObject.getAbsolutePath());
				sendResponse(HttpResponse.FileNotFound());
				return;
			}
		}
		if (fileObject.isDirectory()) {
			byte[] data;
			String listString;
			if (config.isJsonMode()) {
				listString = HttpResponse.listDirectoryJSON(fileObject, rootFile);
				data = listString.getBytes(config.getCodeType());
				sendResponse(HttpResponse.OkayFile(data.length, "application/json"));
			}else {
				listString = HttpResponse.listDirectoryHTML(fileObject, rootFile);
				data = listString.getBytes(config.getCodeType());
				sendResponse(HttpResponse.OkayHtml(data.length));
			}
			//logger.info("sending dir:" + fileObject.getAbsolutePath() + "\n" + listString);
			outputStream.write(data);
		}else {
			String extString = fileObject.getName();
			int pos = extString.lastIndexOf('.');
			if (pos < 0) {
				extString = Mime.toContentType("");
			}else {				
				extString = Mime.toContentType(extString.substring(pos + 1));
			}
			sendResponse(HttpResponse.OkayFile(fileObject.length(), extString));
			FileInputStream fileStream = new FileInputStream(fileObject);
			sendResponse(fileStream);
		}
	}
	
	private void sendResponse(InputStream stream) throws IOException {
		int len = 0;
		//logger.info("sending file:" + fileObject.getAbsolutePath());
		while((len = stream.read(buffer,0,buffer.length)) > 0){
			outputStream.write(buffer, 0, len);
		}
		stream.close();
	}
}
