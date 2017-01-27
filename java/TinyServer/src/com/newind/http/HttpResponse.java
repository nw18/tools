package com.newind.http;

import java.io.File;
import java.io.FilenameFilter;
import java.sql.Date;
import java.text.SimpleDateFormat;

public class HttpResponse {
	public static final String _INNER_LOGO_ = "favicon.ico";
	public static final String HTML_HEAD = "<html>" +
    "<head>" + 
	"<link href=\"/" + _INNER_LOGO_ + "\" type=\"image/x-icon\" rel=icon>" +
    "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1\" />" +
    "<meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\"/>" +
    "<title>TinyServer</title>" + 
    "<style>a{{font-size:100%}}</style></head>" +
    "<body><table><tr><td>";
	public static final String HTML_JS_HEAD = 
			"<script type=\"text/javascript\">\n"
			+ "var sp = \"&nbsp;&nbsp;</td><td>\";\n";
	public static final String HTML_JS_TAIL =
			"for(var i = 0; i < data.length;i++){ data[i][0] =  \"<a href='\" + cd + \"/\" + data[i][0] + \"'>\" + data[i][0] + \"</a>\"; }\n"
			+ "for(var i = 0; i < data.length;i++){ data[i] = data[i].join(sp); }\n"
			+ "document.write(data.join(\"</td></tr><tr><td>\"));\n"
			+ "</script>\n";
	public static final String HTML_TAIL = "</td></tr></table></body></html>";
	public static final String HTML_COL_SPAN = "&nbsp;&nbsp;</td><td>";
	public static final String HTML_ROW_SPAN = "</td></tr><tr><td>";
	public static final String HTML_ROW_SPAN_EMPTY = "</td></tr><tr><td colspan=3>";
	
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
		return "HTTP/1.1 404 Not Found\r\nContent-type: text/plain\r\nContent-Length: 15\r\n\r\nFile Not Found.";
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
		}catch(Exception e){
			e.printStackTrace();
		}
		stringBuilder.append(HTML_TAIL);
		return stringBuilder.toString();
	}
	
	//make the directory list as a java script format resutl.
	public static String listDirectoryInJS(File dir,File root){
		StringBuilder stringBuilder = new StringBuilder();
		File fileList[] = dir.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File file, String name) {
				return !name.startsWith(".");
			}
		});
		String rootPath = root.getAbsolutePath();
		try{
			String current = dir.getAbsolutePath().substring(rootPath.length()).replace('\\', '/');
			stringBuilder.append(HTML_HEAD);
			stringBuilder.append("<a href=\""); 
			String parent = current + "/..";
			stringBuilder.append(parent);
			stringBuilder.append("\">[Parent Directory]</a>");
			stringBuilder.append(HTML_COL_SPAN);
			stringBuilder.append("[Size]");
			stringBuilder.append(HTML_COL_SPAN);
			stringBuilder.append("[Modify]");
			if(fileList != null && fileList.length != 0){
				stringBuilder.append(HTML_ROW_SPAN);
				stringBuilder.append(HTML_JS_HEAD);
				stringBuilder.append(String.format("var cd=\"%s\"\n",current));
				SimpleDateFormat timeFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
				Date date = new Date(0);
				stringBuilder.append("var data=[");
				for(int i = 0; i < fileList.length; i++){
					File file = fileList[i];
					date.setTime(file.lastModified());
					stringBuilder.append(String.format("['%s','%s','%s']",
							file.getName(),
							file.isFile() ? String.valueOf(file.length()) : "",
							timeFormat.format(date)
							));
					if (i != fileList.length - 1) {
						stringBuilder.append(",\n");
					}else {
						stringBuilder.append("];\n");
					}
				}
				stringBuilder.append(HTML_JS_TAIL);
			}else {
				stringBuilder.append(HTML_ROW_SPAN_EMPTY);
				stringBuilder.append("nothing");
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		stringBuilder.append(HTML_TAIL);
		return stringBuilder.toString();
	}
}
