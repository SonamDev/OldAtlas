package io.Sonam;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.GlobalEventExecutor;

import java.util.HashMap;

@SuppressWarnings("ALL")
public class MasterServerHandler extends SimpleChannelInboundHandler<byte[]> {

    private static final ChannelGroup channels = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
    private static final ChannelGroup bungees = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
    private static final ChannelGroup instances = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
    private static final HashMap<String, Channel> bungee_getter = new HashMap<String, Channel>();
    private static final HashMap<String, Channel> instance_getter = new HashMap<String, Channel>();

    @Override
    public void channelRead0(ChannelHandlerContext ctx, byte[] bytes) throws Exception {
        System.out.println(new String(bytes));
        Channel channel = ctx.channel();
        String MCP_COMMAND = new String(bytes); String[] CMD = MCP_COMMAND.split(" ");
        String command = CMD[1];
        StringBuilder dataBuilder = new StringBuilder();
        for(int i = 2; i < CMD.length; i++) {
            dataBuilder.append(CMD[i]).append(" ");
        }
        String finalData = dataBuilder.toString().trim();
        System.out.println("COMMAND > " + command + " > DATA > " + finalData);
        if(command.equalsIgnoreCase("PROXY_REG")) {
            bungee_getter.put(finalData, channel);
            bungees.add(channel);
            System.out.println("Registered Bungee " + finalData + " : Bungees Connected = " + bungee_getter.size());
            return;
        }
        if(command.equalsIgnoreCase("INS_REG")) {
            instance_getter.put(finalData, channel);
            instances.add(channel);
            System.out.println("Registered Instance " + finalData + " : Instances Connected = " + instance_getter.size());
            return;
        }
        if(command.equalsIgnoreCase("PROXY_UNREG")) {
            System.out.println("Unregistered Bungee " + bungee_getter.get(ctx.channel()));
            bungee_getter.values().remove(ctx.channel());
            return;
        }
        if(command.equalsIgnoreCase("INS_UNREG")) {
            System.out.println("Unregistered Instance " + instance_getter.get(ctx.channel()));
            bungee_getter.values().remove(ctx.channel());
            return;
        }
        if(command.equalsIgnoreCase("PROFILE")) {
            System.out.println(instance_getter);
            instance_getter.get(CMD[2]).writeAndFlush(MCP_COMMAND.getBytes());
            return;
        }
        String asdasd = "SC " + finalData;
        channels.writeAndFlush(asdasd.getBytes());
    }

    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        channels.add(ctx.channel());
        System.out.println("Added Channel > " + channels.size());
    }

    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
        channels.remove(ctx.channel());
        System.out.println("Added Channel > " + channels.size());
    }
}
