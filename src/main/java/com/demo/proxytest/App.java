package com.demo.proxytest;

import com.demo.proxytest.netty.HttpProxyServer;
import com.demo.proxytest.socket.SocketHandle;

import java.net.ServerSocket;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args )
    {
//        try {
//            ServerSocket serverSocket = new ServerSocket(1212);
//            for (;;) {
//                new SocketHandle(serverSocket.accept()).start();
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
        new HttpProxyServer().start(1212);
    }
}
