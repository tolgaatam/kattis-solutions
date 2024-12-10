package com.example.asafebet;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

public class App {

    // Directions : UP=0 LEFT=1 DOWN=2 RIGHT=3
    // Mirrors shaped like this \ are 1 and / are 2, because they signify the direction change of the reflection mathematically
    private static byte mirrorReflection(byte initialDirection, byte mirrorType){
        return (byte) ((-1 * initialDirection + 3 + mirrorType * 2) % 4);
    }

    private static int moveRow(int row, byte direction){
        return switch (direction) {
            case 1, 3 -> row;
            case 0 -> row - 1;
            default -> row + 1; // 2 (DOWN)
        };
    }

    private static int moveColumn(int column, byte direction){
        return switch (direction) {
            case 0, 2 -> column;
            case 1 -> column - 1;
            default -> column + 1; // 3 (RIGHT)
        };
    }

    static long rows, cols, twoMirrorCount, oneMirrorCount;
    static HashMap<Long, Byte> mirrors;
    static long smallestMirrorInsertedLexicoPosition;
    static long numberOfInsertedMirrors;
    static boolean canBeOpenedWithoutMirror;

    public static void solve(){
        int baseRow = 0, baseCol = 0;
        byte baseDirection = 3; // start with right direction

        // first, let's see if we can reach the end without any mirrors
        while(true){
            if(baseRow == rows - 1 && baseCol == cols){ // success
                canBeOpenedWithoutMirror = true;
                break;
            }
            if(baseRow < 0 || baseRow >= rows || baseCol < 0 || baseCol >= cols){ // out of bounds
                break;
            }
            long posLexico = baseRow * cols + baseCol;
            Byte mirrorOnCell = mirrors.get(posLexico);
            if(mirrorOnCell != null) { // there is mirror on the base cell. we should apply the base mirror's reflection
                baseDirection = mirrorReflection(baseDirection, mirrorOnCell);
            }
            baseRow = moveRow(baseRow, baseDirection);
            baseCol = moveColumn(baseCol, baseDirection);
        }

        if(canBeOpenedWithoutMirror){
            return;
        }

        // we can't reach the end without any mirrors, let's try inserting mirrors all along the way
        baseRow = 0;
        baseCol = 0;
        baseDirection = 3; // start with right direction
        Set<Long> posLexicoSoFar = new HashSet<>();
        while(true){
            if(baseRow < 0 || baseRow >= rows || baseCol < 0 || baseCol >= cols){ // out of bounds
                break;
            }
            long basePosLexico = baseRow * cols + baseCol;

            Byte mirrorOnBaseCell = mirrors.get(basePosLexico);
            if(mirrorOnBaseCell != null) { // there is mirror on the base cell. we should apply the mirror and continue (we cannot apply any mirror here)
                baseDirection = mirrorReflection(baseDirection, mirrorOnBaseCell);
                baseRow = moveRow(baseRow, baseDirection);
                baseCol = moveColumn(baseCol, baseDirection);
                posLexicoSoFar.add(basePosLexico);
                continue;
            }

            if(!posLexicoSoFar.contains(basePosLexico)){ // this cell was not already visited in the base path, so we can insert mirrors here (otherwise, we would change the past...)
                // valid cell, without mirror. let's try inserting mirrors
                boolean anySuccess = false;
                for(byte insertedMirror = 1; insertedMirror <= 2 && !anySuccess; insertedMirror++){
                    byte currDirection = mirrorReflection(baseDirection, insertedMirror);
                    int currRow = moveRow(baseRow, currDirection);
                    int currCol = moveColumn(baseCol, currDirection);
                    mirrors.put(basePosLexico, insertedMirror);
                    while(true){
                        if(currRow == rows - 1 && currCol == cols){ // success
                            numberOfInsertedMirrors ++;
                            if(basePosLexico < smallestMirrorInsertedLexicoPosition){
                                smallestMirrorInsertedLexicoPosition = basePosLexico;
                            }
                            anySuccess = true;
                            break;
                        }
                        if(currRow < 0 || currRow >= rows || currCol < 0 || currCol >= cols){ // out of bounds
                            break;
                        }
                        long currPosLexico = currRow * cols + currCol;
                        Byte mirrorOnCurrCell = mirrors.get(currPosLexico);
                        if(mirrorOnCurrCell != null) { // there is mirror on the curr cell. we should apply the curr mirror's reflection
                            currDirection = mirrorReflection(currDirection, mirrorOnCurrCell);
                        }
                        currRow = moveRow(currRow, currDirection);
                        currCol = moveColumn(currCol, currDirection);
                    }
                }
                mirrors.remove(basePosLexico);
            }

            // keep moving normally as if no mirror is inserted
            baseRow = moveRow(baseRow, baseDirection);
            baseCol = moveColumn(baseCol, baseDirection);
            posLexicoSoFar.add(basePosLexico);
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
                smallestMirrorInsertedLexicoPosition = Long.MAX_VALUE;
                numberOfInsertedMirrors = 0;
                canBeOpenedWithoutMirror = false;

                for(int i = 0; i < twoMirrorCount; i++){
                    mirrors.put((stdin.nextLong()-1) * cols + (stdin.nextLong()-1), (byte) 2);
                }
                for(int i = 0; i < oneMirrorCount; i++){
                    mirrors.put((stdin.nextLong()-1) * cols + (stdin.nextLong()-1), (byte) 1);
                }
            } catch (NullPointerException e){ // means that the input stream is closed
                break;
            }

            solve();

            caseCount++;

            if(canBeOpenedWithoutMirror){
                System.out.printf("Case %d: 0\n", caseCount);
            } else if(numberOfInsertedMirrors > 0){
                System.out.printf("Case %d: %d %d %d\n", caseCount, numberOfInsertedMirrors, smallestMirrorInsertedLexicoPosition / cols + 1, smallestMirrorInsertedLexicoPosition % cols + 1);
            } else {
                System.out.printf("Case %d: impossible\n", caseCount);
            }

        }
    }
}
