package com.newind.http;

public class HttpConfig {
	private static HttpConfig _config_ = null;
	private HttpConfig(){
		
	}
	
	public static HttpConfig instacne() {
		if(null == _config_){
			_config_ = new HttpConfig();
		}
		return _config_;
	}
	
	
}
