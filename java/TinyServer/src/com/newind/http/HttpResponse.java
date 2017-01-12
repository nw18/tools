package com.newind.http;

public class HttpResponse {
	public static String OkayHtml(StringBuilder stringBuilder){
		stringBuilder.insert(0, String.format("HTTP/1.1 200 OK\r\nContent-Length: %d\r\nContent-type: text/html\r\n\r\n", stringBuilder.length()));
		return stringBuilder.toString();
	}
	
	public static String OkayFile(long contentLength,String contentType){
		return String.format("HTTP/1.1 200 OK\r\nContent-Length: %d\r\nContent-type: %s\r\n\r\n",contentLength,contentType);
	}
	
	public static String BadRequest() {
		return "HTTP/1.1 500 Bad Requect\r\n\r\n";
	}
	
	public static String FileNotFound(){
		return "HTTP/1.1 404 Not Found\r\nContent-type: text/html\r\n\r\n";
	}
	
	public static String OkayText(String text){
		return String.format("HTTP/1.1 200 OK\r\nContent-type:text/plain\r\nContent-Length: %d\r\n\r\n%s",text.length(),text);
	}
}
