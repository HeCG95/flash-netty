package the.flash;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.AttributeKey;

public class NettyServer {

    private static final int BEGIN_PORT = 8000;

    public static void main(String[] args) {

        // 1.线程模型
        NioEventLoopGroup boosGroup = new NioEventLoopGroup();// 监听端口 - 来 accept 新连接的线程组
        NioEventLoopGroup workerGroup = new NioEventLoopGroup();// 处理每一条连接的数据读写的线程组

        final ServerBootstrap serverBootstrap = new ServerBootstrap();
        final AttributeKey<Object> clientKey = AttributeKey.newInstance("clientKey");
        final AttributeKey<Object> serverKey = AttributeKey.newInstance("serverName");

        serverBootstrap
                .group(boosGroup, workerGroup)
                // 2. IO 模型
                .channel(NioServerSocketChannel.class)

                .attr(serverKey, "nettyServer")
                .childAttr(clientKey, "clientValue")

                // 初始化服务端可连接队列
                .option(ChannelOption.SO_BACKLOG, 1024)

                // 给每条连接设置一些TCP底层相关的属性
                .childOption(ChannelOption.SO_KEEPALIVE, true)// TCP底层心跳机制
                .childOption(ChannelOption.TCP_NODELAY, true)// 减少发送次数

                .handler(new ChannelInitializer<NioServerSocketChannel>() {// 在服务端启动过程中的一些逻辑
                    @Override
                    protected void initChannel(NioServerSocketChannel ch) throws Exception {
                        System.out.println("服务端启动中 - attr[serverName] "+ch.attr(serverKey).get());
                    }
                })


                // 3.连接读写处理逻辑
                .childHandler(new ChannelInitializer<NioSocketChannel>() {// 处理新连接数据的读写处理逻辑
                    protected void initChannel(NioSocketChannel ch) {
                        System.out.println(ch.attr(clientKey).get());
                    }
                });


        bind(serverBootstrap, BEGIN_PORT);
        System.out.println(">>>>>>>>>>>... ");
    }

    private static void bind(final ServerBootstrap serverBootstrap, final int port) {
        serverBootstrap.bind(port).addListener(future -> {
            if (future.isSuccess()) {
                System.out.println("端口[" + port + "]绑定成功!");
            } else {
                System.err.println("端口[" + port + "]绑定失败!");
                bind(serverBootstrap, port + 1);
            }
        });
    }
}
