package com.newind.http;

import java.io.File;
import java.io.FilenameFilter;
import java.net.URLEncoder;
import java.sql.Date;
import java.text.SimpleDateFormat;

public class HttpResponse {
	public static final String HTML_HEAD = "<html>" +
    "<head>" + 
    "<meta name=\"viewport\" content=\"width=1024px, initial-scale=1\" />" +
    "<meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\"/>" +
    "<title>TinyServer</title>" + 
    "<style>a{{font-size:100%}}</style></head>" +
    "<body><table><tr><td>";
	public static final String HTML_TAIL = "</td></tr></table></body></html>";
	public static final String HTML_COL_SPAN = "&nbsp;&nbsp;</td><td>";
	public static final String HTML_ROW_SPAN = "</td></tr><tr><td>";
	
	public static String OkayHtml(int length){
		return String.format("HTTP/1.1 200 OK\r\nContent-Length: %d\r\nContent-type: text/html\r\n\r\n", length);
	}
	
	public static String OkayFile(long contentLength,String contentType){
		return String.format("HTTP/1.1 200 OK\r\nContent-Length: %d\r\nContent-type: %s\r\n\r\n",contentLength,contentType);
	}
	
	public static String BadRequest() {
		return "HTTP/1.1 500 Bad Requect\r\n\r\n";
	}
	
	public static String FileNotFound(){
		return "HTTP/1.1 404 Not Found\r\nContent-type: text/plain\r\nContent-Length: 0\r\n\r\n";
	}
	
	public static String OkayText(String text){
		return String.format("HTTP/1.1 200 OK\r\nContent-type:text/plain\r\nContent-Length: %d\r\n\r\n%s",text.length(),text);
	}
	
	public static String listDirectory(File dir,File root){
		StringBuilder stringBuilder = new StringBuilder();
		File fileList[] = dir.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File file, String name) {
				return !name.startsWith(".");
			}
		});
		stringBuilder.append(HTML_HEAD);
		String rootPath = root.getAbsolutePath();
		try{
			if(fileList != null){
				SimpleDateFormat timeFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
				Date date = new Date(0);
				for(int i = 0; i < fileList.length; i++){
					File file = fileList[i];
					String url = URLEncoder.encode(dir.getAbsolutePath().substring(rootPath.length()),"UTF-8");
					stringBuilder.append("<a href=\"" + url + "\">" + file.getName() + "</a>");
					stringBuilder.append(HTML_COL_SPAN);
					stringBuilder.append(file.length());
					stringBuilder.append(HTML_COL_SPAN);
					date.setTime(file.lastModified());
					stringBuilder.append(timeFormat.format(date));
					if (i != fileList.length - 1) {
						stringBuilder.append(HTML_ROW_SPAN);
					}
				}
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		stringBuilder.append(HTML_TAIL);
		return stringBuilder.toString();
	}
}
