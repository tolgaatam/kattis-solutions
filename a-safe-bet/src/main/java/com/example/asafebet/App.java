package com.example.asafebet;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.TreeSet;

public class App {

    // Directions : UP=0 LEFT=1 DOWN=2 RIGHT=3
    // Orientations : VERTICAL=0 HORIZONTAL=1
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
    static TreeSet<Long> mirrorInsertedPositions;
    static boolean canBeOpenedWithoutMirror;

    public static void dfs(int row, int col, byte direction, Pair<Long, Byte> insertedMirror){
        if(row < 0 || row >= rows || col < 0 || col >= cols){ // out of bounds
            return;
        }
        long posLexico = row * cols + col;

        if(row == rows - 1 && col == cols - 1){ // success
            Byte mirrorOnCell = mirrors.get(posLexico);
            if(direction == 3 && mirrorOnCell == null){ // we go straight to success and there is no mirror to block us
                if(insertedMirror == null){
                    canBeOpenedWithoutMirror = true;
                } else {
                    mirrorInsertedPositions.add(insertedMirror.first());
                }
            } else if(direction == 2 && ((mirrorOnCell == null && insertedMirror == null) || (mirrorOnCell != null && mirrorOnCell == 1))){
                if(mirrorOnCell == null && insertedMirror == null) {
                    mirrorInsertedPositions.add(posLexico);
                } else { // mirrorOnCell is 1, we do not need to do anything for successful path
                    if(insertedMirror == null){
                        canBeOpenedWithoutMirror = true;
                    } else {
                        mirrorInsertedPositions.add(insertedMirror.first());
                    }
                }
            }

            return;
        }

        Byte mirrorOnCell = mirrors.get(posLexico);

        if(mirrorOnCell != null) { // there is mirror on the cell. we should just apply the mirror and continue
            byte newDirection = mirrorReflection(direction, mirrorOnCell);
            dfs(moveRow(row, newDirection), moveColumn(col, newDirection), newDirection, insertedMirror);
        } else {
            dfs(moveRow(row, direction), moveColumn(col, direction), direction, insertedMirror);
            if(canBeOpenedWithoutMirror){
                return;
            }
            if(insertedMirror == null){
                mirrors.put(posLexico, (byte) 1); // add mirror temporarily
                byte newDirection = mirrorReflection(direction, (byte) 1);
                dfs(moveRow(row, newDirection), moveColumn(col, newDirection), newDirection, new Pair<>(posLexico, (byte) 1));

                mirrors.put(posLexico, (byte) 2); // replace mirror with another mirror
                newDirection = mirrorReflection(direction, (byte) 2);
                dfs(moveRow(row, newDirection), moveColumn(col, newDirection), newDirection, new Pair<>(posLexico, (byte) 2));

                mirrors.remove(posLexico); // remove the mirror after trying both, before reverting back
            }
        }

    }

    public static void main(String[] args) throws IOException {
        var stdin = new FastConsoleReader();

        mirrors = new HashMap<>();
        mirrorInsertedPositions = new TreeSet<>();

        int caseCount = 0;
        while(true){
            try{
                rows = stdin.nextInt();
                cols = stdin.nextInt();
                twoMirrorCount = stdin.nextInt();
                oneMirrorCount = stdin.nextInt();

                mirrors.clear();
                mirrorInsertedPositions.clear();
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

            dfs(0, 0, (byte) 3, null);

            caseCount++;

            if(canBeOpenedWithoutMirror){
                System.out.printf("Case %d: 0\n", caseCount);
            } else if(!mirrorInsertedPositions.isEmpty()){
                long smallestMirrorInsertedPos = mirrorInsertedPositions.first();
                System.out.printf("Case %d: %d %d %d\n", caseCount, mirrorInsertedPositions.size(), smallestMirrorInsertedPos / cols + 1, smallestMirrorInsertedPos % cols + 1);
            } else {
                System.out.printf("Case %d: impossible\n", caseCount);
            }

        }
    }
}
