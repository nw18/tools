package com.newind;

import java.net.Socket;

import com.newind.base.Pooling;
import com.newind.base.PoolingWorker;
import com.newind.http.HttpConnection;

public class AppPooling extends Pooling<Socket, PoolingWorker<Socket>> {

	protected AppPooling(int maxCount) {
		super(maxCount);
	}

	@Override
	protected PoolingWorker<Socket> makeWorker() {
		return new AppWorker();
	}

	static class AppWorker implements PoolingWorker<Socket>{
		@Override
		public void handle(Socket param) {
			if(null == param){
				return;
			}
			if(param.getLocalPort() == AppConfig.instacne().getHttpPort()){
				HttpConnection connection = new HttpConnection();
				connection.handle(param);
			}else if (param.getLocalPort() == AppConfig.instacne().getFtpPort()) {
				
			}else {
				
			}
		}
	}
	
	private static AppPooling _instacne_ = null;
	public static AppPooling instance(){
		if(null == _instacne_){
			System.err.println("AppPooling instance not init!!!");
		}
		return _instacne_;
	}
	
	public static void setup(int threadCount){
		if (null != _instacne_) {
			System.err.println("AppPooling instance already inited!!!");
			return;
		}
		_instacne_ = new AppPooling(threadCount);
	}
}
