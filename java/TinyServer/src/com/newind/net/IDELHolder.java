package com.newind.net;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

import com.newind.ApplicationConfig;
import com.newind.ApplicationPooling;

public class IDELHolder {
	private ApplicationConfig config = ApplicationConfig.instance();
	private Selector selector;
	private Thread selectorThread;
	private long lastCheckTime;

	protected IDELHolder(){
	}
	
	private static IDELHolder instance = null;
	public static IDELHolder getInstance(){
		if(null == instance){
			instance = new IDELHolder();
		}
		return instance;
	}
	
	public void start() throws IOException {
		selector = Selector.open();
		selectorThread  = new Thread(){
			@Override
			public void run() {
				lastCheckTime = System.currentTimeMillis();
				while(true){
					try {
						int res = selector.select(1000);
						if(res > 0){
							Iterator<SelectionKey> it = selector.selectedKeys().iterator();
							while(it.hasNext()){
								SelectionKey key = it.next();
								if (key.isReadable()) {
									key.cancel(); //put to task pooling.
									SocketChannel socketChannel = (SocketChannel) key.channel();
									socketChannel.configureBlocking(true);
									ApplicationPooling.instance().putTask(socketChannel.socket());
									System.out.println("back to busy.");
								}
								
								if (!key.isValid()) {
									key.cancel();
								}
							}
						}
						long currectTime = System.currentTimeMillis();
						if (currectTime - lastCheckTime > config.getRecvTimeout()) {
							lastCheckTime = currectTime;
							Iterator<SelectionKey> iterator = selector.keys().iterator();
							while(iterator.hasNext()){
								SelectionKey key = iterator.next();
								Long addTime = (Long) key.attachment();
								if(addTime == null || currectTime - addTime > config.getConnectionTimeout()){
									key.cancel(); //remove timeout key.
								}
							}
						}
					}catch (Exception e) {
						e.printStackTrace();
						break;
					}
				}
			};
		};
		selectorThread.start();
	}
	
	public boolean addIDEL(SocketChannel socketChannel){
		try {
			socketChannel.configureBlocking(false);
			socketChannel.register(selector, 
					SelectionKey.OP_READ,
					Long.valueOf(System.currentTimeMillis()));
			System.out.println("addIDEL");
		} catch (IOException e) {
			return false;
		}
		return true;
	}
}
