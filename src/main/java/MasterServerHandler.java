import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public class MasterServerHandler extends SimpleChannelInboundHandler<byte[]> {

    public void channelRead0(ChannelHandlerContext ctx, byte[] bytes) throws Exception {
        System.out.println("Got Packets");
        System.out.println(new String(bytes));
    }

    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }

}
