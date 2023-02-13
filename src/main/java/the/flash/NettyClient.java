package the.flash;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.AttributeKey;

import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * @author 闪电侠
 */
public class NettyClient {
    private static final int MAX_RETRY = 5;


    public static void main(String[] args) {

        // 线程模型
        NioEventLoopGroup workerGroup = new NioEventLoopGroup();

        Bootstrap bootstrap = new Bootstrap();
        AttributeKey<Object> clientKey = AttributeKey.newInstance("clientName");

        bootstrap
                // 1.指定线程模型
                .group(workerGroup)
                // 2.指定 IO 类型为 NIO - IO 模型
                .channel(NioSocketChannel.class)

                // 绑定自定义属性到 channel
                .attr(clientKey, "nettyClient")

                // 设置TCP底层属性
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)// 连接的超时时间
                .option(ChannelOption.SO_KEEPALIVE, true)
                .option(ChannelOption.TCP_NODELAY, true)

                // 3.IO 处理逻辑
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    public void initChannel(SocketChannel ch) {
                        System.out.println("Client key value: "+ch.attr(clientKey).get());
                    }
                });

        // 4.建立连接
        connect(bootstrap, "juejin.im", 80, MAX_RETRY);
    }

    private static void connect(Bootstrap bootstrap, String host, int port, int retry) {

        bootstrap.connect(host, port).addListener(future -> {
            if (future.isSuccess()) {
                System.out.println("连接成功!");
            } else if (retry == 0) {
                System.err.println("重试次数已用完，放弃连接！");
            } else {// 指数退避重连逻辑

                // 第几次重连
                int order = (MAX_RETRY - retry) + 1;
                // 本次重连的间隔
                int delay = 1 << order;
                System.out.println("delay times: "+delay);
                System.err.println(new Date() + ": 连接失败，第" + order + "次重连……");

                // 定时器
                bootstrap.config().group()
                        .schedule(() -> connect(bootstrap, host, port, retry - 1),
                                delay, TimeUnit.SECONDS);
            }
        });

    }
}
