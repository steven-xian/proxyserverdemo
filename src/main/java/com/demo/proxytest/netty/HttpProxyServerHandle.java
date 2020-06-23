package com.demo.proxytest.netty;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.handler.codec.http.*;

public class HttpProxyServerHandle extends ChannelInboundHandlerAdapter {
    private ChannelFuture cf;
    private String host;
    private int port;

    @Override
    public void channelRead(final ChannelHandlerContext ctx, final Object msg) throws Exception {
        if (msg instanceof FullHttpRequest) {
            FullHttpRequest request = (FullHttpRequest) msg;
            String host = request.headers().get("host");
            String[] temp = host.split(":");
            int port = 80;
            if (temp.length > 1) {
                port = Integer.parseInt(temp[1]);
            } else {
                if (request.uri().indexOf("https") == 0) {
                    port = 443;
                }
            }

            this.host = temp[0];
            this.port = port;
            if ("CONNECT".equalsIgnoreCase(request.method().name())) {
                HttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpProxyServer.SUCCESS);
                ctx.writeAndFlush(response);
                ctx.pipeline().remove("httpCodec");
                ctx.pipeline().remove("httpObject");
                return;
            }

            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(ctx.channel().eventLoop())
                    .channel(ctx.channel().getClass())
                    .handler(new HttpProxyInitializer(ctx.channel()));

            ChannelFuture cf = bootstrap.connect(temp[0], port);
            cf.addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture channelFuture) throws Exception {
                    if (channelFuture.isSuccess()) {
                        channelFuture.channel().writeAndFlush(msg);
                    } else {
                        ctx.channel().close();
                    }
                }
            });
        } else {
            if (cf == null) {
                Bootstrap bootstrap = new Bootstrap();
                bootstrap.group(ctx.channel().eventLoop())
                        .channel(ctx.channel().getClass())
                        .handler(new ChannelInitializer() {
                            @Override
                            protected void initChannel(Channel channel) throws Exception {
                                channel.pipeline().addLast(new ChannelInboundHandlerAdapter() {
                                    @Override
                                    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                                        ctx.channel().writeAndFlush(msg);
                                    }
                                });
                            }
                        });
                cf = bootstrap.connect(host, port);
                cf.addListener(new ChannelFutureListener() {
                    @Override
                    public void operationComplete(ChannelFuture channelFuture) throws Exception {
                        if (channelFuture.isSuccess()) {
                            channelFuture.channel().writeAndFlush(msg);
                        } else {
                            ctx.channel().close();
                        }
                    }
                });
            } else {
                cf.channel().writeAndFlush(msg);
            }
        }
    }
}
