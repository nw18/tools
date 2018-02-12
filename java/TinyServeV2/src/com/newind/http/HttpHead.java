package com.newind.http;

import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.newind.util.TextUtil;

public class HttpHead {
	private String method;
	private String version;
	private String rawUrl;
	private String url;
	private String query;
	private Map<String, String> queryStringMap = new HashMap<>();
	private Map<String, String> headFieldsMap = new HashMap<>();
	public static HttpHead parse(String string,String encType) throws Exception{
		String lines[] = string.split("\r\n");
		if(lines.length < 1){
			throw new RuntimeException("bad http request");
		}
		String fields[] = lines[0].split(" ");
		if (fields == null || fields.length != 3) {
			throw new RuntimeException("bad http method.");
		}
		HttpHead head = new HttpHead();
		head.method = fields[0].toUpperCase();
		head.rawUrl = URLDecoder.decode(fields[1],encType);
		head.version = fields[2];
		int pos = head.rawUrl.indexOf('?');
		if (pos > 0) {
			head.url = head.rawUrl.substring(0, pos);
			head.query = head.rawUrl.substring(pos + 1);
			String queStrs[] = head.query.split("&");
			for(String queStr : queStrs){
				String ques[] = queStr.split("=");
				if (ques != null && ques.length == 2) {
					head.queryStringMap.put(ques[0], ques[1]);
				}
			}
		}else {
			head.url = head.rawUrl;
		}
		for(int i = 1; i < lines.length; i++){
			String line = lines[i];
			pos = line.indexOf(":");
			if (pos > 0) {
				head.headFieldsMap.put(line.substring(0, pos).toLowerCase(), line.substring(pos + 1));
			}
		}
		return head;
	}

	public static HttpHead init(String line,String encType) throws Exception{
		String fields[] = line.split(" ");
		if (fields == null || fields.length != 3) {
			throw new RuntimeException("bad http method.");
		}
		HttpHead head = new HttpHead();
		head.method = fields[0].toUpperCase();
		head.rawUrl = URLDecoder.decode(fields[1],encType);
		head.version = fields[2];
		int pos = head.rawUrl.indexOf('?');
		if (pos > 0) {
			head.url = head.rawUrl.substring(0, pos);
			head.query = head.rawUrl.substring(pos + 1);
			String queStrs[] = head.query.split("&");
			for(String queStr : queStrs){
				String ques[] = queStr.split("=");
				if (ques != null && ques.length == 2) {
					head.queryStringMap.put(ques[0], ques[1]);
				}
			}
		}else {
			head.url = head.rawUrl;
		}
		return head;
	}

	public void addHead(String line){
		int pos = line.indexOf(":");
		if (pos > 0) {
			headFieldsMap.put(line.substring(0, pos).toLowerCase(), line.substring(pos + 1));
		}
	}
	
	public String getMethod() {
		return method;
	}
	
	public String getVersion() {
		return version;
	}
	
	public String getRawUrl() {
		return rawUrl;
	}
	
	public String getUrl() {
		return url;
	}
	
	public String getQuery() {
		return query;
	}
	
	public Set<String> getQueryKeys(){
		return queryStringMap.keySet();
	}
	
	public String getQueryString(String key){
		return queryStringMap.get(key);
	}
	
	public Set<String> getHeadKeys(){
		return headFieldsMap.keySet();
	}
	
	public String getHeadString(String key) {
		return headFieldsMap.get(key);
	}
	
	public boolean isGet(){
		return TextUtil.equal(method, "GET");
	}
	
	public boolean isPost(){
		return TextUtil.equal(method, "POST");
	}
	
	public boolean isHead(){
		return TextUtil.equal(method, "HEAD");
	}
	
	@Override
	public String toString() {
		return method + " " + rawUrl + " " + version;
	}
}
