package com.example.asafebet;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;

public class App {

    // Directions : UP=0 LEFT=1 DOWN=2 RIGHT=3
    // Orientations : VERTICAL=0 HORIZONTAL=1
    // Mirrors shaped like this \ are 1 and / are 2, because they signify the direction change of the reflection mathematically
    private static byte mirrorReflection(byte initialDirection, byte mirrorType){
        return (byte) ((-1 * initialDirection + 3 + mirrorType * 2) % 4);
    }

    private static long move(long pos, byte direction){
        if(pos % cols == 0 && direction == 1){ // left
            return -1;
        }
        if(pos % cols == cols - 1 && direction == 3){ // right
            return -1;
        }
        return switch (direction) {
            case 0 -> pos - cols;
            case 1 -> pos - 1;
            case 2 -> pos + cols;
            case 3 -> pos + 1;
            default -> -1;
        };
    }

    private static byte directionToOrientation(byte direction){
        return (direction % 2 == 0) ? (byte) 0 : (byte) 1;
    }

    static long rows, cols, twoMirrorCount, oneMirrorCount;
    static HashMap<Long, Byte> mirrors;
    static HashSet<Pair<Long, Byte>> visited;
    static HashSet<Long> mirrorInsertedPositions;

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
            Byte mirrorOnCell = mirrors.get(pos);
            if(direction == 3 && mirrorOnCell == null){ // we go straight to success and there is no mirror to block us
                if(insertedMirror == null){
                    canBeOpenedWithoutMirror = true;
                } else {
                    mirrorInsertedPositions.add(insertedMirror.first());
                    if(smallestMirrorInsertedPos == null || insertedMirror.first() < smallestMirrorInsertedPos){
                        smallestMirrorInsertedPos = insertedMirror.first();
                    }
                }
            } else if(direction == 2 && ((mirrorOnCell == null && insertedMirror == null) || (mirrorOnCell != null && mirrorOnCell == 1))){
                if(mirrorOnCell == null && insertedMirror == null) {
                    mirrorInsertedPositions.add(pos);
                    if(smallestMirrorInsertedPos == null || pos < smallestMirrorInsertedPos){
                        smallestMirrorInsertedPos = pos;
                    }
                } else { // mirrorOnCell is 1, we do not need to do anything for successful path
                    if(insertedMirror == null){
                        canBeOpenedWithoutMirror = true;
                    } else {
                        mirrorInsertedPositions.add(insertedMirror.first());
                        if(smallestMirrorInsertedPos == null || insertedMirror.first() < smallestMirrorInsertedPos){
                            smallestMirrorInsertedPos = insertedMirror.first();
                        }
                    }
                }
            }

            return;
        }

        byte orientation = directionToOrientation(direction);
        if(visited.contains(new Pair<>(pos, orientation))){
            // we have already visited this cell with this direction, we are returning back on the same path at the moment. no need to proceed.
            // no, we cannot be in a loop, because loops are not possible in this problem due to the nature of mirrors
            return;
        }

        Byte mirrorOnCell = mirrors.get(pos);
        if(mirrorOnCell == null && insertedMirror != null && insertedMirror.first() == pos){
            mirrorOnCell = insertedMirror.second();
        }

        if(mirrorOnCell != null) { // there is mirror on the cell. we should just apply the mirror and continue
            byte newDirection = mirrorReflection(direction, mirrorOnCell);
            dfs(move(pos, newDirection), newDirection, insertedMirror);
        } else {
            visited.add(new Pair<>(pos, orientation));
            dfs(move(pos, direction), direction, insertedMirror);
            visited.remove(new Pair<>(pos, orientation));
            if(canBeOpenedWithoutMirror){
                return;
            }
            if(insertedMirror == null){
                dfs(move(pos, mirrorReflection(direction, (byte) 1)), mirrorReflection(direction, (byte) 1), new Pair<>(pos, (byte) 1));
                dfs(move(pos, mirrorReflection(direction, (byte) 2)), mirrorReflection(direction, (byte) 2), new Pair<>(pos, (byte) 2));
            }
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
                mirrorInsertedPositions = new HashSet<>();
                smallestMirrorInsertedPos = null;
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

            // START: logic part

            dfs(0, (byte) 3, null);
            if(canBeOpenedWithoutMirror){
                System.out.printf("Case %d: 0\n", caseCount);
            } else if(smallestMirrorInsertedPos != null){
                System.out.printf("Case %d: %d %d %d\n", caseCount, mirrorInsertedPositions.size(), smallestMirrorInsertedPos / cols + 1, smallestMirrorInsertedPos % cols + 1);
            } else {
                System.out.printf("Case %d: impossible\n", caseCount);
            }

            // END: logic part
            caseCount++;

        }
    }
}
