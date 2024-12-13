package com.example.asafebet;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.*;

public class App {

    // Directions : UP=0 LEFT=1 DOWN=2 RIGHT=3
    // Mirrors shaped like this \ are 1 and / are 2, because they signify the direction change of the reflection mathematically
    private static byte mirrorReflection(byte initialDirection, byte mirrorType){
        return (byte) ((-1 * initialDirection + 3 + mirrorType * 2) % 4);
    }

    static long rows, cols, twoMirrorCount, oneMirrorCount;
    static HashMap<Long, Byte> mirrors;
    static Map<Long, TreeSet<Long>> mirrorsByRow;
    static Map<Long, TreeSet<Long>> mirrorsByColumn;
    static long smallestMirrorInsertedLexicoPosition;
    static long numberOfInsertedMirrors;
    static boolean canBeOpenedWithoutMirror;

    public static void sweepLine(List<FlatLine<Long, Long, Long>> horizontalLines, List<FlatLine<Long, Long, Long>> verticalLines){
        List<Event> events = new ArrayList<>();
        for(var horizontalLine : horizontalLines){
            events.add(new Event(horizontalLine.start(), horizontalLine.main(), horizontalLine.main(), (byte) 1));
            events.add(new Event(horizontalLine.end()+1, horizontalLine.main(), horizontalLine.main(), (byte) -1));
        }
        for(var verticalLine : verticalLines){
            events.add(new Event(verticalLine.main(), verticalLine.start(), verticalLine.end(), (byte) 0));
        }

        events.sort((a, b) -> {
            if (a.col() == b.col()) {
                return b.type() - a.type();
            }
            return (int) (a.col() - b.col());
        });

        var activeRows = new TreeSet<Long>();

        // Process all events
        for (Event event : events) {
            switch (event.type()) {
                case 1 -> activeRows.add(event.row1());
                case -1 -> activeRows.remove(event.row1());
                case 0 -> {
                    var intersectingRows = activeRows.subSet(event.row1(), event.row2() + 1);
                    numberOfInsertedMirrors += intersectingRows.size();
                    try{
                        var minimumIntersectingRow = intersectingRows.first();
                        smallestMirrorInsertedLexicoPosition = Math.min(smallestMirrorInsertedLexicoPosition, minimumIntersectingRow * cols + event.col());
                    } catch (NoSuchElementException ignored){}
                }
            }
        }
    }

    public static boolean generateLines(List<FlatLine<Long, Long, Long>> horizontalLines, List<FlatLine<Long, Long, Long>> verticalLines, long startRow, long startCol, byte startDirection){
        long currRow = startRow, currCol = startCol;
        byte currDirection = startDirection;

        while(true){
            switch (currDirection){
                case 0 -> { // up
                    Long nextRow = mirrorsByColumn.get(currCol).lower(currRow);
                    if(nextRow == null){
                        verticalLines.add(new FlatLine<>(currCol, 0L, currRow - 1));
                        return false;
                    } else {
                        verticalLines.add(new FlatLine<>(currCol, nextRow + 1, currRow - 1));
                        currRow = nextRow;
                    }
                }
                case 1 -> { // left
                    Long nextCol = mirrorsByRow.get(currRow).lower(currCol);
                    if(nextCol == null){
                        horizontalLines.add(new FlatLine<>(currRow, 0L, currCol - 1));
                        return false;
                    } else {
                        horizontalLines.add(new FlatLine<>(currRow, nextCol + 1, currCol - 1));
                        currCol = nextCol;
                    }
                }
                case 2 -> { // down
                    Long nextRow = mirrorsByColumn.get(currCol).higher(currRow);
                    if(nextRow == null){
                        verticalLines.add(new FlatLine<>(currCol, currRow + 1, rows - 1));
                        return false;
                    } else {
                        verticalLines.add(new FlatLine<>(currCol, currRow + 1, nextRow - 1));
                        currRow = nextRow;
                    }
                }
                default -> { // 3 - right
                    Long nextCol = mirrorsByRow.get(currRow).higher(currCol);
                    if(nextCol == null){
                        horizontalLines.add(new FlatLine<>(currRow, currCol + 1, cols - 1));
                        return false;
                    } else {
                        if(nextCol == cols){ // we reached the end without any mirrors. no other processing is needed.
                            return true;
                        }
                        horizontalLines.add(new FlatLine<>(currRow, currCol + 1, nextCol - 1));
                        currCol = nextCol;
                    }
                }
            }

            currDirection = mirrorReflection(currDirection, mirrors.get(currRow * cols + currCol));
        }
    }

    public static void solve(){
        List<FlatLine<Long, Long, Long>> originalHorizontalLines = new ArrayList<>();
        List<FlatLine<Long, Long, Long>> originalVerticalLines = new ArrayList<>();
        // start from top left corner to the `right` direction
        canBeOpenedWithoutMirror = generateLines(originalHorizontalLines, originalVerticalLines, 0L, -1L, (byte) 3);
        if(canBeOpenedWithoutMirror){
            return;
        }

        // we can't reach the end without any mirrors (we would have returned from the function if we did), let's try with mirrors
        // we will traverse from the end to the start, and collect line information
        List<FlatLine<Long, Long, Long>> reverseHorizontalLines = new ArrayList<>();
        List<FlatLine<Long, Long, Long>> reverseVerticalLines = new ArrayList<>();
        generateLines(reverseHorizontalLines, reverseVerticalLines, rows - 1, cols, (byte) 1);

        sweepLine(originalHorizontalLines, reverseVerticalLines);
        sweepLine(reverseHorizontalLines, originalVerticalLines);
    }

    private static void addMirror(long row, long col, byte mirrorType){
        mirrors.put(row * cols + col, mirrorType);
        TreeSet<Long> ms;

        if((ms = mirrorsByRow.get(row)) == null){
            ms = new TreeSet<>();
            mirrorsByRow.put(row, ms);
        }
        ms.add(col);

        if((ms = mirrorsByColumn.get(col)) == null){
            ms = new TreeSet<>();
            mirrorsByColumn.put(col, ms);
        }
        ms.add(row);
    }

    public static void main(String[] args) throws IOException {
        var stdin = new FastConsoleReader();

        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(System.out), 1024* 128);

        int caseCount = 0;
        while(true){
            caseCount++;
            try{
                rows = stdin.nextInt();
                cols = stdin.nextInt();
                twoMirrorCount = stdin.nextInt();
                oneMirrorCount = stdin.nextInt();

                mirrors = new HashMap<>();
                mirrorsByRow = new HashMap<>();
                mirrorsByColumn = new HashMap<>();
                smallestMirrorInsertedLexicoPosition = Long.MAX_VALUE;
                numberOfInsertedMirrors = 0;
                canBeOpenedWithoutMirror = false;

                long row, col;
                for(int i = 0; i < twoMirrorCount; i++){
                    row = stdin.nextLong()-1;
                    col = stdin.nextLong()-1;
                    addMirror(row, col, (byte) 2);
                }
                for(int i = 0; i < oneMirrorCount; i++){
                    row = stdin.nextLong()-1;
                    col = stdin.nextLong()-1;
                    addMirror(row, col, (byte) 1);
                }
            } catch (NullPointerException e){ // means that the input stream is closed
                break;
            }

            // add imaginary mirrors to the start and success points, to be able to stop properly
            addMirror(rows - 1, cols, (byte) 2);
            addMirror(0L, -1L, (byte) 2);

            Byte m1, m2;
            // checking if entry and exit point does not have a mirror that blocks the path.
            // if so, we could directly say that the safe is impossible to open
            if(!((m1 = mirrors.get(0L)) != null && m1 == 2) && !((m2 = mirrors.get(rows * cols - 1)) != null && m2 == 2)) {
                solve();
            }

            if(canBeOpenedWithoutMirror){
                writer.write("Case " + caseCount + ": 0\n");
            } else if(numberOfInsertedMirrors > 0){
                String sb = "Case " + caseCount + ": " + numberOfInsertedMirrors + " " +
                        (smallestMirrorInsertedLexicoPosition / cols + 1) + " " +
                        (smallestMirrorInsertedLexicoPosition % cols + 1) + '\n';

                writer.write(sb);
            } else {
                writer.write("Case " + caseCount + ": impossible\n");
            }

        }
        writer.flush();
    }
}
