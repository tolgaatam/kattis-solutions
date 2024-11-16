package com.example.asafebet;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.util.StringTokenizer;

public class FastConsoleReader {
    private final BufferedReader reader;
    private StringTokenizer tokenizer;

    public FastConsoleReader() {
        reader = new BufferedReader(new InputStreamReader(System.in), 4096*4);
    }

    String next() throws IOException {
        while (tokenizer == null || !tokenizer.hasMoreTokens()) {
            tokenizer = new StringTokenizer(reader.readLine());
        }
        return tokenizer.nextToken();
    }

    int nextInt() throws IOException {
        return Integer.parseInt(next());
    }

    long nextLong() throws IOException {
        return Long.parseLong(next());
    }

    String nextLine() throws IOException {
        return reader.readLine();
    }
}
