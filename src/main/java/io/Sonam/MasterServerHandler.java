package io.Sonam;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.CharsetUtil;
import io.netty.util.concurrent.GlobalEventExecutor;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;

@SuppressWarnings("all")
public class MasterServerHandler extends ChannelHandlerAdapter {

    private static final ChannelGroup channels = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
    private static final ChannelGroup bungees = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
    private static final ChannelGroup instances = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
    private static final HashMap<String, Channel> bungee_getter = new HashMap<String, Channel>();
    private static final HashMap<String, Channel> instance_getter = new HashMap<String, Channel>();

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf buf = (ByteBuf) msg;
        String jsonPayload = buf.toString(CharsetUtil.UTF_8);
        JSONObject object = new JSONObject(jsonPayload);
        Channel channel = ctx.channel();
        channel.flush();
        String command = object.getString("command");
        final JSONObject finalData = object.getJSONObject("data");
        if(command.equalsIgnoreCase("PROXY_REG")) {
            bungee_getter.put(finalData.getString("instance"), channel);
            bungees.add(channel);
            System.out.println("[ByteBuf] Registered Bungee " + finalData.getString("instance") + " : Bungees Connected = " + bungee_getter.size());
            MasterServer.getSocketIO().getSocket().emit("registeredProxy", 1);
            MasterServer.bungees++;
            return;
        }
        if(command.equalsIgnoreCase("INS_REG")) {
            instance_getter.put(finalData.getString("instance"), channel);
            instances.add(channel);
            System.out.println("[ByteBuf] Registered Instance " + finalData.get("instance") + " : Instances Connected = " + instance_getter.size());
            MasterServer.getSocketIO().getSocket().emit("registeredIns", 1);
            MasterServer.instances++;
            return;
        }
        if(command.equalsIgnoreCase("PROXY_UNREG")) {
            System.out.println("[ByteBuf] Unregistered Bungee " + finalData.getString("instance"));
            bungee_getter.values().remove(ctx.channel());
            MasterServer.bungees--;
            MasterServer.getSocketIO().getSocket().emit("setBungees", MasterServer.bungees);
            return;
        }
        if(command.equalsIgnoreCase("INS_UNREG")) {
            System.out.println("[ByteBuf] Unregistered Instance " + finalData.getString("instance"));
            bungee_getter.values().remove(ctx.channel());
            MasterServer.instances--;
            MasterServer.getSocketIO().getSocket().emit("setInstances", MasterServer.instances);
            return;
        }
        if(command.equalsIgnoreCase("PROFILE")) {
            System.out.println(instance_getter);
            final ByteBuf byteBuf = Unpooled.copiedBuffer(jsonPayload, CharsetUtil.UTF_8);
            byteBuf.capacity(1024);
            ctx.channel().eventLoop().schedule(new Runnable() {
                public void run() {
                    instance_getter.get(finalData.getString("instance")).writeAndFlush(byteBuf);
                }
            }, 100L, TimeUnit.MILLISECONDS);
            return;
        }
        if(command.equalsIgnoreCase("INS_STATUS")) {
            System.out.println("STATUS : " + finalData.getString("instance") + " : " + finalData.getString("status"));
            MasterServer.getSocketIO().getSocket().emit("status", finalData.toString());
            return;
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("[ByteBuf] Channel Added");
        channels.add(ctx.channel());
    }

    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
        System.out.println("[ByteBuf] Channel Removed");
        channels.remove(ctx.channel());
    }
}
