package com.newind.base;

import java.util.LinkedList;
import java.util.concurrent.Semaphore;

public abstract class Pooling<P,T extends PoolingWorker<P>>{
	private int maxCount;
	private Semaphore writeObject ,readObject;
	private LinkedList<P> pendList;
	private LinkedList<Thread> threadList;
	protected Pooling(int maxCount) {
		this.maxCount = maxCount;
		writeObject = new Semaphore(maxCount);
		readObject = new Semaphore(0);
		pendList = new LinkedList<>();
		threadList = new LinkedList<>();
	}
	
	public int getMaxCount() {
		return maxCount;
	}
	
	private int putTaskI(P param){
		try {
			writeObject.acquire();
			synchronized (pendList) {
				pendList.push(param);
			}
			readObject.release();
			return 0;
		} catch (InterruptedException e) {
			e.printStackTrace();
			return -1;
		}
	}
	
	public int putTask(P param){
		if(null == param){
			return -1;
		}
		return putTaskI(param);
	}
	
	public int setup(){
		for(int i = 0; i < maxCount;i++){
			Thread t = new Thread(){
				PoolingWorker<P> worker = makeWorker();
				@Override
				public void run() {
					handProc(worker);
				}
			};
			threadList.add(t);
			t.start();
		}
		return 0;
	}
	
	public void release(){
		for(int i = 0;i < maxCount;i++){
			putTaskI(null);
		}
		for(Thread thread : threadList){
			try {
				thread.join();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		threadList.clear();
	}
	
	protected void handProc(PoolingWorker<P> handler){
		try {
			P p;
			readObject.acquire();
			synchronized (pendList) {				
				p = pendList.pop();
			}
			writeObject.release();
			handler.handle(p);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	protected abstract PoolingWorker<P> makeWorker(); 
}
