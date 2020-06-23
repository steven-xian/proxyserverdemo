package com.demo.proxytest.socket;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class SocketHandle extends Thread {
    private Socket socket;

    public SocketHandle(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        OutputStream clientOutput = null;
        InputStream clientInput = null;
        Socket proxySocket = null;
        InputStream proxyInput = null;
        OutputStream proxyOutput = null;

        try {
            clientInput = socket.getInputStream();
            clientOutput = socket.getOutputStream();

            String line;
            String host = "";

            LineBuffer lineBuffer = new LineBuffer(1024);
            StringBuilder headStr = new StringBuilder();

            while(null != (line = lineBuffer.readLine(clientInput))) {
                System.out.println(line);

                headStr.append(line + "\r\n");
                if (line.length() == 0) {
                    break;
                } else {
                    String[] temp = line.split(" ");
                    if (temp[0].contains("Host")) {
                        host = temp[1];
                    }
                }
            }

            String type = headStr.substring(0, headStr.indexOf(" "));
            String[] hostTemp = host.split(":");
            host = hostTemp[0];
            int port = 80;
            if (hostTemp.length > 1) {
                port = Integer.valueOf(hostTemp[1]);
            }

            proxySocket = new Socket(host, port);
            proxyInput = proxySocket.getInputStream();
            proxyOutput = proxySocket.getOutputStream();

            if ("CONNECT".equalsIgnoreCase(type)) {
                clientOutput.write("HTTP/1.1 200 Connection Established\r\n\r\n".getBytes());
                clientOutput.flush();
            } else {
                proxyOutput.write(headStr.toString().getBytes());
            }

            new ProxyHandleThread(clientInput, proxyOutput).start();

            while (true) {
                clientOutput.write(proxyInput.read());
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (proxyInput != null) {
                try {
                    proxyInput.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if (proxyOutput != null) {
                try {
                    proxyOutput.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if (clientInput != null) {
                try {
                    clientInput.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if (clientOutput != null) {
                try {
                    clientOutput.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
