package com.newind.net;

public interface IServer<T> {
    void start() throws Exception;
    void close();
    void join();
    void handleConnection(T connection);
}
