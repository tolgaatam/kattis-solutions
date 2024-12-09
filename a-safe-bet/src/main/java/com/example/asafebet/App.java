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

    public static void solve(){
        long currRow = 0, currCol = -1L;
        byte currDirection = 3; // start with `right` direction
        List<FlatLine<Long, Long, Long>> originalHorizontalLines = new ArrayList<>();
        List<FlatLine<Long, Long, Long>> originalVerticalLines = new ArrayList<>();

        // first, let's see if we can reach the end without any mirrors
        while(true){
            if(currDirection == 0){ // up
                Long nextRow = mirrorsByColumn.get(currCol).lower(currRow);
                if(nextRow == null){
                    originalVerticalLines.add(new FlatLine<>(currCol, 0L, currRow - 1));
                    break;
                } else {
                    originalVerticalLines.add(new FlatLine<>(currCol, nextRow + 1, currRow - 1));
                    currRow = nextRow;
                }
            } else if (currDirection == 1) { // left
                Long nextCol = mirrorsByRow.get(currRow).lower(currCol);
                if(nextCol == null){
                    originalHorizontalLines.add(new FlatLine<>(currRow, 0L, currCol - 1));
                    break;
                } else {
                    originalHorizontalLines.add(new FlatLine<>(currRow, nextCol + 1, currCol - 1));
                    currCol = nextCol;
                }
            } else if (currDirection == 2) { // down
                Long nextRow = mirrorsByColumn.get(currCol).higher(currRow);
                if(nextRow == null){
                    originalVerticalLines.add(new FlatLine<>(currCol, currRow + 1, rows - 1));
                    break;
                } else {
                    originalVerticalLines.add(new FlatLine<>(currCol, currRow + 1, nextRow - 1));
                    currRow = nextRow;
                }
            } else { // right
                Long nextCol = mirrorsByRow.get(currRow).higher(currCol);
                if(nextCol == null){
                    originalHorizontalLines.add(new FlatLine<>(currRow, currCol + 1, cols - 1));
                    break;
                } else {
                    if(nextCol == cols){ // we reached the end without any mirrors. no other processing is needed.
                        canBeOpenedWithoutMirror = true;
                        return;
                    }
                    originalHorizontalLines.add(new FlatLine<>(currRow, currCol + 1, nextCol - 1));
                    currCol = nextCol;
                }
            }
            currDirection = mirrorReflection(currDirection, mirrors.get(currRow * cols + currCol));
        }

        // we can't reach the end without any mirrors (we would have returned from the function if we did), let's try with mirrors
        // we will traverse from the end to the start, and collect line information
        currRow = rows - 1;
        currCol = cols;
        currDirection = 1; // start with `left` direction
        List<FlatLine<Long, Long, Long>> reverseHorizontalLines = new ArrayList<>();
        List<FlatLine<Long, Long, Long>> reverseVerticalLines = new ArrayList<>();

        while(true){
            if(currDirection == 0){ // up
                Long nextRow = mirrorsByColumn.get(currCol).lower(currRow);
                if(nextRow == null){
                    reverseVerticalLines.add(new FlatLine<>(currCol, 0L, currRow - 1));
                    break;
                } else {
                    reverseVerticalLines.add(new FlatLine<>(currCol, nextRow + 1, currRow - 1));
                    currRow = nextRow;
                }
            } else if (currDirection == 1) { // left
                Long nextCol = mirrorsByRow.get(currRow).lower(currCol);
                if(nextCol == null){
                    reverseHorizontalLines.add(new FlatLine<>(currRow, 0L, currCol - 1));
                    break;
                } else {
                    reverseHorizontalLines.add(new FlatLine<>(currRow, nextCol + 1, currCol - 1));
                    currCol = nextCol;
                }
            } else if (currDirection == 2) { // down
                Long nextRow = mirrorsByColumn.get(currCol).higher(currRow);
                if(nextRow == null){
                    reverseVerticalLines.add(new FlatLine<>(currCol, currRow + 1, rows - 1));
                    break;
                } else {
                    reverseVerticalLines.add(new FlatLine<>(currCol, currRow + 1, nextRow - 1));
                    currRow = nextRow;
                }
            } else { // right
                Long nextCol = mirrorsByRow.get(currRow).higher(currCol);
                if(nextCol == null){
                    reverseHorizontalLines.add(new FlatLine<>(currRow, currCol + 1, cols - 1));
                    break;
                } else {
                    reverseHorizontalLines.add(new FlatLine<>(currRow, currCol + 1, nextCol - 1));
                    currCol = nextCol;
                }
            }
            currDirection = mirrorReflection(currDirection, mirrors.get(currRow * cols + currCol));
        }

        sweepLine(originalHorizontalLines, reverseVerticalLines);
        sweepLine(reverseHorizontalLines, originalVerticalLines);
    }

    public static void main(String[] args) throws IOException {
        var stdin = new FastConsoleReader();

        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(System.out), 1024* 128);

        int caseCount = 0;
        while(true){
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
                    mirrors.put(row * cols + col, (byte) 2);
                    mirrorsByRow.computeIfAbsent(row, k -> new TreeSet<>()).add(col);
                    mirrorsByColumn.computeIfAbsent(col, k -> new TreeSet<>()).add(row);
                }
                for(int i = 0; i < oneMirrorCount; i++){
                    row = stdin.nextLong()-1;
                    col = stdin.nextLong()-1;
                    mirrors.put(row * cols + col, (byte) 1);
                    mirrorsByRow.computeIfAbsent(row, k -> new TreeSet<>()).add(col);
                    mirrorsByColumn.computeIfAbsent(col, k -> new TreeSet<>()).add(row);
                }
            } catch (NullPointerException e){ // means that the input stream is closed
                break;
            }

            // add imaginary mirrors to the start and success points, to be able to stop properly
            mirrorsByRow.computeIfAbsent(rows - 1, k -> new TreeSet<>()).add(cols);
            mirrorsByRow.computeIfAbsent(0L, k -> new TreeSet<>()).add(-1L);

            solve();

            caseCount++;

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
