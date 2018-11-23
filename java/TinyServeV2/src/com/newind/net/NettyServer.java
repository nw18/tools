package com.newind.net;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollChannelOption;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import com.newind.util.myLog;

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
    public void start() throws Exception
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
        try {
            bootstrap.childHandler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel socketChannel) throws Exception {
                    NettyServer.this.handleConnection(socketChannel);
                }
            });
            ChannelFuture future = bootstrap.bind(this.port).sync();
            myLog.info(String.format("%s start at %d", NettyServer.class.getSimpleName() ,this.port));
            future.channel().closeFuture().sync();
        }catch (Exception e) {
            myLog.error(e);
        } finally {
            this.bossGroup.shutdownGracefully().sync();
        }
    }

    @Override
    public void close() {
        try {
            this.bossGroup.shutdownGracefully().sync();
            this.workerGroup.shutdownGracefully().sync();
        }catch (Exception e) {
            myLog.error(e);
        }
    }

    @Override
    public void join() {
        myLog.invoke();
    }

    @Override
    public void handleConnection(SocketChannel connection) {

    }
}
