package com.newind;

import java.net.Socket;
import com.newind.base.LogManager;
import com.newind.base.Pooling;
import com.newind.base.PoolingWorker;
import com.newind.ftp.FtpConnection;
import com.newind.http.HttpConnection;

public class ApplicationPooling extends Pooling<Socket, PoolingWorker<Socket>> {

	protected ApplicationPooling(int maxCount) {
		super(maxCount);
	}

	@Override
	protected PoolingWorker<Socket> makeWorker() {
		return new AppWorker();
	}

	static class AppWorker implements PoolingWorker<Socket>{
		private byte[] buffer = new byte[ApplicationConfig.instance().getRecvBufferSize()];
		private HttpConnection httpConnection;
		private FtpConnection ftpConnection;
		@Override
		public void handle(Socket param) {
			if(null == param){
				return;
			}
			if(param.getLocalPort() == ApplicationConfig.instance().getHttpPort()){
				if(httpConnection == null){
					httpConnection = new HttpConnection(buffer);
				}
				httpConnection.handle(param);
			}else if (param.getLocalPort() == ApplicationConfig.instance().getFtpPort()) {
				if(ftpConnection == null){
					ftpConnection = new FtpConnection(buffer);
				}
				ftpConnection.handle(param);
			}else {
				try{
					LogManager.getLogger().warning("unhandle " + param.getLocalAddress());
					param.close();
				}catch(Exception e){
					e.printStackTrace();
				}
			}
		}
	}
	
	private static ApplicationPooling _instacne_ = null;
	public static ApplicationPooling instance(){
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
		_instacne_ = new ApplicationPooling(threadCount);
		_instacne_.setup();
	}
	
	@Override
	public void release() {
		super.release();
		_instacne_ = null;
	}
}
