package io.Sonam;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.CharsetUtil;
import io.netty.util.concurrent.GlobalEventExecutor;
import org.json.JSONObject;

import java.util.HashMap;

@SuppressWarnings("all")
public class MasterServerHandler extends ChannelInboundHandlerAdapter {

    private static final ChannelGroup channels = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
    private static final ChannelGroup bungees = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
    private static final ChannelGroup instances = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
    private static final HashMap<String, Channel> bungee_getter = new HashMap<String, Channel>();
    private static final HashMap<String, Channel> instance_getter = new HashMap<String, Channel>();

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf buf = (ByteBuf) msg;
        String jsonPayload = buf.toString(CharsetUtil.UTF_8);
        System.out.println("[ByteBuf] " + jsonPayload);
        JSONObject object = new JSONObject(jsonPayload);
        Channel channel = ctx.channel();
        String command = object.getString("command");
        final JSONObject finalData = object.getJSONObject("data");
        System.out.println("COMMAND > " + command + " > DATA > " + finalData);
        if(command.equalsIgnoreCase("PROXY_REG")) {
            bungee_getter.put(finalData.getString("instance"), channel);
            bungees.add(channel);
            System.out.println("[ByteBuf] Registered Bungee " + finalData.getString("instance") + " : Bungees Connected = " + bungee_getter.size());
            return;
        }
        if(command.equalsIgnoreCase("INS_REG")) {
            instance_getter.put(finalData.getString("instance"), channel);
            instances.add(channel);
            System.out.println("[ByteBuf] Registered Instance " + finalData.get("instance") + " : Instances Connected = " + instance_getter.size());
            return;
        }
        if(command.equalsIgnoreCase("PROXY_UNREG")) {
            System.out.println("[ByteBuf] Unregistered Bungee " + bungee_getter.get(ctx.channel()));
            bungee_getter.values().remove(ctx.channel());
            return;
        }
        if(command.equalsIgnoreCase("INS_UNREG")) {
            System.out.println("[ByteBuf] Unregistered Instance " + instance_getter.get(ctx.channel()));
            bungee_getter.values().remove(ctx.channel());
            return;
        }
        if(command.equalsIgnoreCase("PROFILE")) {
            final ByteBuf unpooledBuf = Unpooled.copiedBuffer(buf);
            System.out.println(instance_getter);
            instance_getter.get(finalData.getString("instance")).writeAndFlush(unpooledBuf);
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

    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
        System.out.println("[ByteBuf] Channel Removed");
        channels.remove(ctx.channel());
    }
}
