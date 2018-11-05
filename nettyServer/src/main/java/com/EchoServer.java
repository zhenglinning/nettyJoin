package com;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import java.net.InetSocketAddress;

public class EchoServer {
    private int port;

    public EchoServer(int port) {
        this.port = port;
    }

    public static void main(String[] args) throws InterruptedException {
        if (args.length != 1) {
            System.err.println("Usages: " + EchoServer.class.getSimpleName() + "<port>");
        }

        int port = Integer.parseInt(args[0]);
        new EchoServer(port).start();
    }

    /**
     * 启动方法
     *
     * @throws InterruptedException
     */
    public void start() throws InterruptedException {
        final EchoServerHandler serverHandler = new EchoServerHandler();

        //1创建EventLoopGroup 用来接收和处理新的连接
        EventLoopGroup group = new NioEventLoopGroup();

        try {
            //2 创建ServerBootstrap
            ServerBootstrap b = new ServerBootstrap();
            b.group(group)
                    //3 制定所使用的NIO传输channel
                    .channel(NioServerSocketChannel.class)
                    //4 使用制定的端口设置套接字地址
                    .localAddress(new InetSocketAddress((port)))
                    //5 添加一个EchoServerHandler到子channel的ChannelPipeLine
                    .childHandler(new ChannelInitializer<SocketChannel>() {

                        @Override
                        protected void initChannel(SocketChannel socketChannel)
                                throws Exception {
                            //EchoServerHandler被标注为@Shareable，所以我们可以总是使用同样的实例
                            socketChannel.pipeline().addLast(serverHandler);
                        }
                    });

            //6 异地绑定服务器；调用sync方法阻塞等待直到绑定完成
            ChannelFuture f = b.bind().sync();
            //7 获取channel的closeFuture，并且阻塞当前线程直到它完成
            f.channel().closeFuture().sync();
        } finally {
            //8 关闭EventLoopGroup释放所有的资源
            group.shutdownGracefully().sync();
        }
    }

}
