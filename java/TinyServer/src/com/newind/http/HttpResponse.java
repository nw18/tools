package com.newind.http;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.sql.Date;
import java.text.SimpleDateFormat;

public class HttpResponse {
	public static final String FAVICON = "favicon.ico";
	public static final String HTML_HEAD = "<!DOCTYPE html><html><head>"
			+ "<link href=\"/" + FAVICON + "\" type=\"image/x-icon\" rel=icon>"
			+ "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1\" />"
			+ "<meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\"/>" + "<title>TinyServer</title>"
			+ "<style>a{{font-size:100%}}</style></head>" + "<body><table><tr><td>";
	public static final String HTML_TAIL = "</td></tr></table></body></html>";
	public static final String HTML_COL_SPAN = "&nbsp;&nbsp;</td><td>";
	public static final String HTML_ROW_SPAN = "</td></tr><tr><td>";
	public static final String HTML_ROW_SPAN_EMPTY = "</td></tr><tr><td colspan=3>";
	public static final byte[] CRLF = new byte[] {13,10};
	
	public static String OkayHtml(int length){
		return String.format("HTTP/1.1 200 OK\r\nContent-Length: %d\r\nContent-type: text/html\r\n\r\n", length);
	}
	
	public static String OkayFile(long contentLength,String contentType){
		return String.format("HTTP/1.1 200 OK\r\nContent-Length: %d\r\nContent-type: %s\r\n\r\n",contentLength,contentType);
	}
	
	public static String OkayFileTrunked(String contentType){
		return String.format("HTTP/1.1 200 OK\r\nContent-type: %s\r\nTransfer-Encoding: chunked\r\n\r\n",contentType);
	}
	
	public static String BadRequest() {
		return "HTTP/1.1 500 Bad Requect\r\n\r\n";
	}
	
	public static String FileNotFound(){
		return "HTTP/1.1 404 Not Found\r\nContent-type: text/plain\r\nContent-Length: 15\r\n\r\nFile Not Found.";
	}
	
	public static String OkayText(String text){
		return String.format("HTTP/1.1 200 OK\r\nContent-type:text/plain\r\nContent-Length: %d\r\n\r\n%s",text.length(),text);
	}
	
	public static String listDirectoryHTML(File dir,File root){
		StringBuilder stringBuilder = new StringBuilder();
		File fileList[] = dir.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File file, String name) {
				return !name.startsWith(".");
			}
		});
		stringBuilder.append(HTML_HEAD);
		String rootPath = root.getAbsolutePath();
		stringBuilder.append("<a href=\""); 
		String parent = dir.getAbsolutePath().substring(rootPath.length()) + "/..";
		stringBuilder.append(parent.replace('\\', '/'));
		stringBuilder.append("\">[Parent Directory]</a>");
		stringBuilder.append(HTML_COL_SPAN);
		stringBuilder.append("[Size]");
		stringBuilder.append(HTML_COL_SPAN);
		stringBuilder.append("[Modify]");
		if(fileList != null && fileList.length != 0){
			stringBuilder.append(HTML_ROW_SPAN);
			SimpleDateFormat timeFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
			Date date = new Date(0);
			for(int i = 0; i < fileList.length; i++){
				File file = fileList[i];
				String absWebPath = file.getAbsolutePath().substring(rootPath.length()).replace('\\', '/');
				stringBuilder.append("<a href=\"" + absWebPath + "\">" + file.getName() + "</a>");
				stringBuilder.append(HTML_COL_SPAN);
				if (file.isFile()) {						
					stringBuilder.append(file.length());
				}
				stringBuilder.append(HTML_COL_SPAN);
				date.setTime(file.lastModified());
				stringBuilder.append(timeFormat.format(date));
				if (i != fileList.length - 1) {
					stringBuilder.append(HTML_ROW_SPAN);
				}
			}
		}else {
			stringBuilder.append(HTML_ROW_SPAN_EMPTY);
			stringBuilder.append("nothing");
		}
		stringBuilder.append(HTML_TAIL);
		return stringBuilder.toString();
	}
	
	//make the directory list as a JSON format result.
	public static String listDirectoryJSON(File dir,File root){
		StringBuilder stringBuilder = new StringBuilder();
		File fileList[] = dir.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File file, String name) {
				return !name.startsWith(".");
			}
		});
		String rootPath = root.getAbsolutePath();
		String current = dir.getAbsolutePath().substring(rootPath.length()).replace('\\', '/'); 
		String parent = current + "/..";
		stringBuilder.append(String.format("{\"parent\":\"%s\",\n",parent));
		stringBuilder.append("\"data\":[");
		if(fileList != null && fileList.length != 0){
			for(int i = 0; i < fileList.length; i++){
				File file = fileList[i];
				stringBuilder.append(String.format("[%d,\"%s\",%d,%d]",
						file.isDirectory() ? 1 : 0,
						file.getName(),
						file.length(),
						file.lastModified()
						));
				if (i != fileList.length - 1) {
					stringBuilder.append(",\n");
				}
			}
		}
		stringBuilder.append("]}");
		return stringBuilder.toString();
	}

	public static void listDirectoryJSONByTrunk(File dir,File root,HttpConnection connection) throws IOException{
		connection.sendTrunkBegin();
		File fileList[] = dir.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File file, String name) {
				return !name.startsWith(".");
			}
		});
		String rootPath = root.getAbsolutePath();
		String current = dir.getAbsolutePath().substring(rootPath.length()).replace('\\', '/');
		String parent = current + "/..";
		connection.sendTrunk(String.format("{\"parent\":\"%s\",\n",parent));
		connection.sendTrunk("\"data\":[");
		if(fileList != null && fileList.length != 0){
			for(int i = 0; i < fileList.length; i++){
				File file = fileList[i];
				connection.sendTrunk(String.format("[%d,\"%s\",%d,%d]",
						file.isDirectory() ? 1 : 0,
						file.getName(),
						file.length(),
						file.lastModified()
				));
				if (i != fileList.length - 1) {
					connection.sendTrunk(",\n");
				}
			}
		}
		connection.sendTrunk("]}");
		connection.sendTrunkEnd();
	}
}
