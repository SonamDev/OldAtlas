package io.Sonam;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.GlobalEventExecutor;

public class MasterServerHandler extends SimpleChannelInboundHandler<byte[]> {

    final ChannelGroup channels = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

    @Override
    public void channelRead0(ChannelHandlerContext ctx, byte[] bytes) throws Exception {
        System.out.println(new String(bytes));
        String MCP_COMMAND = new String(bytes); String[] CMD = MCP_COMMAND.split(" ");
        String command = CMD[1];
        StringBuilder dataBuilder = new StringBuilder();
        for(int i = 2; i < CMD.length; i++) {
            dataBuilder.append(CMD[i]).append(" ");
        }
        String finalData = dataBuilder.toString().trim();
        System.out.println("COMMAND > " + command + " > DATA > " + finalData);
        for(Channel ch : channels) {
            String asdasd = "SC " + finalData;
            ch.writeAndFlush(asdasd.getBytes());
        }
    }

    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        channels.add(ctx.channel());
    }

    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
        channels.remove(ctx.channel());
    }
}
