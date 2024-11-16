package com.example.asafebet;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Stack;

public class App {

    // Directions : UP=0 LEFT=1 DOWN=2 RIGHT=3
    // Orientations : VERTICAL=0 HORIZONTAL=1
    // Mirrors shaped like this \ are 1 and / are 2, because they signify the direction change of the reflection mathematically
    private static int mirrorReflection(byte initialDirection, byte mirrorType){
        return (-1 * initialDirection + 3 + mirrorType * 2) % 4;
    }

    private static byte directionToOrientation(byte direction){
        return (direction % 2 == 0) ? (byte) 0 : (byte) 1;
    }

    static long rows, cols, twoMirrorCount, oneMirrorCount;
    static HashMap<Long, Byte> mirrors;
    static HashSet<Pair<Long, Byte>> visited;

    static Long smallestMirrorInsertedPos;
    static boolean canBeOpenedWithoutMirror;

    public static void dfs(long pos, byte direction, Pair<Long, Byte> insertedMirror){
        if(pos < 0 || pos >= rows * cols){ // out of bounds
            return;
        }
        if(canBeOpenedWithoutMirror){
            return;
        }

        if(pos == rows * cols - 1){
            if(insertedMirror == null){
                canBeOpenedWithoutMirror = true;
            }

            return;
        }
    }

    public static void main(String[] args) throws IOException {
        var stdin = new FastConsoleReader();

        int caseCount = 0;
        while(true){
            try{
                rows = stdin.nextInt();
                cols = stdin.nextInt();
                twoMirrorCount = stdin.nextInt();
                oneMirrorCount = stdin.nextInt();

                mirrors = new HashMap<>();
                visited = new HashSet<>();
                smallestMirrorInsertedPos = null;
                canBeOpenedWithoutMirror = false;

                for(int i = 0; i < twoMirrorCount; i++){
                    mirrors.put(stdin.nextLong() * cols + stdin.nextLong(), (byte) 2);
                }
                for(int i = 0; i < oneMirrorCount; i++){
                    mirrors.put(stdin.nextLong() * cols + stdin.nextLong(), (byte) 1);
                }
            } catch (NullPointerException e){ // means that the input stream is closed
                break;
            }

            // START: logic part

            Stack<Triple<Long, Long, Byte>> stack = new Stack<>();
            stack.push(new Triple<>(0L, 0L, (byte) 1));

            // END: logic part
            caseCount++;

            System.out.printf("Case %d: impossible\n", caseCount);
            System.out.println(mirrors);
        }
    }
}
