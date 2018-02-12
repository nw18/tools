package com.newind.net;

public interface IServer<T> {
    void start();
    void close();
    void join();
    void handleConnection(T connection);
}
