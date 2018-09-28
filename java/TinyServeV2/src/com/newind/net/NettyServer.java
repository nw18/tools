package com.newind.net;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelOption;
import io.netty.channel.MultithreadEventLoopGroup;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollChannelOption;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class NettyServer implements IServer<SocketChannel> {
    private int port = 80;
    private int bossCount = 2;
    private int workerCount = 16;

    private MultithreadEventLoopGroup bossGroup;
    private MultithreadEventLoopGroup workerGroup;

    private ExecutorService bossExecutor;
    private ExecutorService workerExecutor;

    public void setPort(int port)
    {
        this.port = port;
    }

    @Override
    public void start()
    {
        ServerBootstrap bootstrap = new ServerBootstrap();
        bootstrap.bind(port);
        bootstrap.option(ChannelOption.SO_BACKLOG,8);
        bootstrap.option(ChannelOption.SO_REUSEADDR, true);
        bootstrap.childOption(ChannelOption.SO_REUSEADDR, true);
        this.bossExecutor = Executors.newCachedThreadPool();
        this.workerExecutor = Executors.newCachedThreadPool();
        if (Epoll.isAvailable()) {
            this.bossGroup = new EpollEventLoopGroup(this.bossCount, this.bossExecutor);
            this.workerGroup = new EpollEventLoopGroup(this.workerCount, this.workerExecutor);
            bootstrap.option(EpollChannelOption.SO_REUSEPORT, true)
                    .group(this.bossGroup, this.workerGroup)
                    .channel(EpollServerSocketChannel.class);
        } else {
            this.bossGroup = new NioEventLoopGroup(this.bossCount, this.bossExecutor);
            this.workerGroup = new NioEventLoopGroup(this.workerCount, this.workerExecutor);
            bootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class);
        }
    }

    @Override
    public void close() {

    }

    @Override
    public void join() {

    }

    @Override
    public void handleConnection(SocketChannel connection) {

    }
}
