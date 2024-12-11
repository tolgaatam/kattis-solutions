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

    private static long moveRow(long row, byte direction){
        return (direction % 2 == 1) ? row : (row - 1 + direction);
    }

    private static long moveColumn(long column, byte direction){
        return (direction % 2 == 0) ? column : (column - 2 + direction);
    }

    static long rows, cols, twoMirrorCount, oneMirrorCount;
    static HashMap<Long, Byte> mirrors;
    static long smallestMirrorInsertedLexicoPosition;
    static long numberOfInsertedMirrors;
    static boolean canBeOpenedWithoutMirror;

    public static void solve(){
        long currRow = 0, currCol = 0;
        byte currDirection = 3; // start with `right` direction
        Set<Long> originalHorizontalVisits = new TreeSet<>();
        Set<Long> originalVerticalVisits = new TreeSet<>();

        // first, let's see if we can reach the end without any mirrors
        while(true){
            if(currRow == rows - 1 && currCol == cols){ // success
                canBeOpenedWithoutMirror = true;
                break;
            }
            if(currRow < 0 || currRow >= rows || currCol < 0 || currCol >= cols){ // out of bounds
                break;
            }
            long posLexico = currRow * cols + currCol;
            Byte mirrorOnCell = mirrors.get(posLexico);
            if(mirrorOnCell != null) { // there is mirror on the base cell. we should apply the base mirror's reflection
                currDirection = mirrorReflection(currDirection, mirrorOnCell);
            } else {
                if(currDirection % 2 == 0){ // we are vertical
                    originalVerticalVisits.add(posLexico);
                } else { // we are horizontal
                    originalHorizontalVisits.add(posLexico);
                }
            }
            currRow = moveRow(currRow, currDirection);
            currCol = moveColumn(currCol, currDirection);
        }

        if(canBeOpenedWithoutMirror){
            return;
        }

        // we can't reach the end without any mirrors, let's try with mirrors
        // we will traverse starting from the end to the start, and try to see if we can intersect with the original path perpendicularly
        currRow = rows - 1;
        currCol = cols - 1;
        currDirection = 1; // start with `left` direction
        while(true){
            if(currRow < 0 || currRow >= rows || currCol < 0 || currCol >= cols){ // out of bounds
                break;
            }
            long posLexico = currRow * cols + currCol;
            Byte mirrorOnCell = mirrors.get(posLexico);
            if(mirrorOnCell != null) { // there is mirror on the base cell. we should apply the base mirror's reflection
                currDirection = mirrorReflection(currDirection, mirrorOnCell);
            } else {
                // check perpendicular intersection with original path
                if(currDirection % 2 == 0){ // we are vertical
                    if(originalHorizontalVisits.contains(posLexico)){
                        if(posLexico < smallestMirrorInsertedLexicoPosition){
                            smallestMirrorInsertedLexicoPosition = posLexico;
                        }
                        numberOfInsertedMirrors++;
                    }
                } else { // we are horizontal
                    if(originalVerticalVisits.contains(posLexico)){
                        if(posLexico < smallestMirrorInsertedLexicoPosition){
                            smallestMirrorInsertedLexicoPosition = posLexico;
                        }
                        numberOfInsertedMirrors++;
                    }
                }
            }
            currRow = moveRow(currRow, currDirection);
            currCol = moveColumn(currCol, currDirection);
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
