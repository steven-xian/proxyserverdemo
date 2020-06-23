package com.demo.proxytest.socket;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class ProxyHandleThread extends Thread {
    private InputStream inputStream;
    private OutputStream outputStream;

    public ProxyHandleThread(InputStream inputStream, OutputStream outputStream) {
        this.inputStream = inputStream;
        this.outputStream = outputStream;
    }

    @Override
    public void run() {
        try {
            while (true) {
                outputStream.write(inputStream.read());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
