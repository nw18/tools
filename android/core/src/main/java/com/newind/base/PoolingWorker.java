package com.newind.base;

public interface PoolingWorker<T> {
	void handle(T param);
}
