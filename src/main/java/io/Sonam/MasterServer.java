package io.Sonam;

import io.Sonam.sio.Socket_IO;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import redis.clients.jedis.Jedis;


public class MasterServer {

    static final int PORT = 35566;
    private static Socket_IO socketIO;
    public static int bungees;
    public static int instances;
    private static Jedis jedis;


    public static void main(String[] args) throws Exception {
        socketIO = new Socket_IO("http://localhost:3000");
        final Socket socket = socketIO.getSocket();
        socket.connect();
        socket.on(Socket.EVENT_CONNECT, new Emitter.Listener() {
            public void call(Object... objects) {
                System.out.println("Successfully Connected to WebSocket");
                socket.emit("reset", "dummy");
                socket.emit("setInstances", instances);
                socket.emit("setBungees", bungees);
            }
        });
        jedis = new Jedis();
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .option(ChannelOption.SO_KEEPALIVE, true)
                    .option(ChannelOption.AUTO_READ, true)
                    .option(ChannelOption.TCP_NODELAY, true)
                    .option(ChannelOption.SO_SNDBUF, 1048576)
                    .option(ChannelOption.SO_RCVBUF, 1048576)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        public void initChannel(SocketChannel ch) {
                            ChannelPipeline p = ch.pipeline();
                            p.addLast(new MasterServerHandler());
                        }
                    });

            ChannelFuture f = b.bind(PORT).sync();

            f.channel().closeFuture().sync();
        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }

    static Socket_IO getSocketIO() {
        return socketIO;
    }

    static Jedis getJedis() { return jedis; }
}
