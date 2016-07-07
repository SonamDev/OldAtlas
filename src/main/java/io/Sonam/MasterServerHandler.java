package io.Sonam;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.GlobalEventExecutor;
import org.json.simple.JSONObject;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;

@SuppressWarnings("ALL")
public class MasterServerHandler extends SimpleChannelInboundHandler<Object> {

    private static final ChannelGroup channels = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
    private static final ChannelGroup bungees = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
    private static final ChannelGroup instances = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
    private static final HashMap<String, Channel> bungee_getter = new HashMap<String, Channel>();
    private static final HashMap<String, Channel> instance_getter = new HashMap<String, Channel>();

    @Override
    public void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
        System.out.println("Got Data.");
        JSONObject object = (JSONObject) msg;
        Channel channel = ctx.channel();
        String command = object.get("command").toString();
        final JSONObject finalData = (JSONObject) object.get("data");
        System.out.println("COMMAND > " + command + " > DATA > " + finalData);
        if(command.equalsIgnoreCase("PROXY_REG")) {
            bungee_getter.put(finalData.get("instance").toString(), channel);
            bungees.add(channel);
            System.out.println("Registered Bungee " + finalData + " : Bungees Connected = " + bungee_getter.size());
            return;
        }
        if(command.equalsIgnoreCase("INS_REG")) {
            instance_getter.put(finalData.get("instance").toString(), channel);
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
            final Object copy = msg;
            System.out.println(instance_getter);
            ctx.channel().eventLoop().schedule(new Runnable() {
                public void run() {
                    instance_getter.get(finalData.get("instance").toString()).writeAndFlush(copy);
                }
            }, 24, TimeUnit.MILLISECONDS);
            return;
        }
        String asdasd = "SC " + finalData;
        channels.writeAndFlush(asdasd.getBytes());
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        System.out.println("Got Data (read1).");
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
