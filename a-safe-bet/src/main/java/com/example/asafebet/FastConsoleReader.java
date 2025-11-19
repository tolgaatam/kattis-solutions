package com.example.asafebet;

import java.io.IOException;
import java.io.InputStream;

public class FastConsoleReader {
    private final InputStream in;
    private final byte[] buffer = new byte[1 << 16];
    private int ptr = 0, len = 0;

    public FastConsoleReader() {
        this.in = System.in;
    }

    private int read() throws IOException {
        if (ptr >= len) {
            len = in.read(buffer);
            ptr = 0;
            if (len <= 0) return -1;
        }
        return buffer[ptr++];
    }

    public long nextLong() throws IOException {
        int c = read();
        while (c <= ' ') {
            if (c == -1) throw new IOException("EOF");
            c = read();
        }
        boolean neg = false;
        if (c == '-') { neg = true; c = read(); }
        long val = 0;
        while (c > ' ') {
            val = val * 10 + (c - '0');
            c = read();
        }
        return neg ? -val : val;
    }

    public int nextInt() throws IOException {
        return (int) nextLong();
    }

    public String nextLine() throws IOException {
        StringBuilder sb = new StringBuilder();
        int c = read();
        while (c != -1 && c != '\n') {
            sb.append((char) c);
            c = read();
        }
        if (c == -1 && sb.length() == 0) return null;
        return sb.toString();
    }
}
