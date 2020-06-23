package com.demo.proxytest.socket;

import java.io.IOException;
import java.io.InputStream;

public class LineBuffer {
    private int size;

    public LineBuffer(int size) {
        this.size = size;
    }

    public String readLine(InputStream inputStream) throws IOException {
        int flag = 0;
        int index = 0;

        byte[] bytes = new byte[this.size];
        int b;

        while (flag != 2 && (b = inputStream.read()) != -1) {
            bytes[index++] = (byte) b;
            if (b == '\r' && flag % 2 ==0) {
                flag++;
            } else if (b =='\n' && flag % 2 ==1) {
                flag ++;
                if (flag ==2) {
                    return new String(bytes, 0, index - 2);
                }
            } else {
                flag = 0;
            }

            if (index == bytes.length) {
                byte[] newBytes = new byte[bytes.length * 2];
                System.arraycopy(bytes, 0, newBytes, 0, bytes.length);
                bytes = newBytes;
            }
        }

        return null;
    }
}
