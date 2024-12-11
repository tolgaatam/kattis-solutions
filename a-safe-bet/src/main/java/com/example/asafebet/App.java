package com.example.asafebet;

import java.io.IOException;
import java.util.*;

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

    // TODO: Remove this and replace the list by a TreeSet
    private static Long findSmallestGreaterThan(ArrayList<Long> list, long element) { // assumes the list is sorted
        // Use binary search to find the index of the smallest element greater than `element`
        int index = Collections.binarySearch(list, element);

        // If the exact element is found, move to the next element
        if (index >= 0) {
            index++;
        } else {
            // If not found, binarySearch returns (-(insertion point) - 1)
            index = -index - 1;
        }

        // Check if the index is within bounds
        if (index < list.size()) {
            return list.get(index);
        } else {
            // Return null if no such element exists
            return null;
        }
    }

    // TODO: Remove this and replace the list by a TreeSet
    public static Long findGreatestSmallerThan(ArrayList<Long> list, long element) { // assumes the list is sorted
        // Use binary search to find the index of the element
        int index = Collections.binarySearch(list, element);

        // If the exact element is found, move to the previous element
        if (index >= 0) {
            index--;
        } else {
            // If not found, binarySearch returns (-(insertion point) - 1)
            index = -(index + 1) - 1;
        }

        // Check if the index is within bounds
        if (index >= 0) {
            return list.get(index);
        } else {
            // Return null if no such element exists
            return null;
        }
    }

    static long rows, cols, twoMirrorCount, oneMirrorCount;
    static HashMap<Long, Byte> mirrors;
    static Map<Long, ArrayList<Long>> mirrorsByRow;
    static Map<Long, ArrayList<Long>> mirrorsByColumn;
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
                Long nextRow = findGreatestSmallerThan(mirrorsByColumn.get(currCol), currRow);
                if(nextRow == null){
                    originalVerticalLines.add(new FlatLine<>(currCol, 0L, currRow - 1));
                    break;
                } else {
                    originalVerticalLines.add(new FlatLine<>(currCol, nextRow + 1, currRow - 1));
                    currRow = nextRow;
                }
            } else if (currDirection == 1) { // left
                Long nextCol = findGreatestSmallerThan(mirrorsByRow.get(currRow), currCol);
                if(nextCol == null){
                    originalHorizontalLines.add(new FlatLine<>(currRow, 0L, currCol - 1));
                    break;
                } else {
                    originalHorizontalLines.add(new FlatLine<>(currRow, nextCol + 1, currCol - 1));
                    currCol = nextCol;
                }
            } else if (currDirection == 2) { // down
                Long nextRow = findSmallestGreaterThan(mirrorsByColumn.get(currCol), currRow);
                if(nextRow == null){
                    originalVerticalLines.add(new FlatLine<>(currCol, currRow + 1, rows - 1));
                    break;
                } else {
                    originalVerticalLines.add(new FlatLine<>(currCol, currRow + 1, nextRow - 1));
                    currRow = nextRow;
                }
            } else { // right
                Long nextCol = findSmallestGreaterThan(mirrorsByRow.get(currRow), currCol);
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
                Long nextRow = findGreatestSmallerThan(mirrorsByColumn.get(currCol), currRow);
                if(nextRow == null){
                    reverseVerticalLines.add(new FlatLine<>(currCol, 0L, currRow - 1));
                    break;
                } else {
                    reverseVerticalLines.add(new FlatLine<>(currCol, nextRow + 1, currRow - 1));
                    currRow = nextRow;
                }
            } else if (currDirection == 1) { // left
                Long nextCol = findGreatestSmallerThan(mirrorsByRow.get(currRow), currCol);
                if(nextCol == null){
                    reverseHorizontalLines.add(new FlatLine<>(currRow, 0L, currCol - 1));
                    break;
                } else {
                    reverseHorizontalLines.add(new FlatLine<>(currRow, nextCol + 1, currCol - 1));
                    currCol = nextCol;
                }
            } else if (currDirection == 2) { // down
                Long nextRow = findSmallestGreaterThan(mirrorsByColumn.get(currCol), currRow);
                if(nextRow == null){
                    reverseVerticalLines.add(new FlatLine<>(currCol, currRow + 1, rows - 1));
                    break;
                } else {
                    reverseVerticalLines.add(new FlatLine<>(currCol, currRow + 1, nextRow - 1));
                    currRow = nextRow;
                }
            } else { // right
                Long nextCol = findSmallestGreaterThan(mirrorsByRow.get(currRow), currCol);
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
                    mirrorsByRow.computeIfAbsent(row, k -> new ArrayList<>()).add(col);
                    mirrorsByColumn.computeIfAbsent(col, k -> new ArrayList<>()).add(row);
                }
                for(int i = 0; i < oneMirrorCount; i++){
                    row = stdin.nextLong()-1;
                    col = stdin.nextLong()-1;
                    mirrors.put(row * cols + col, (byte) 1);
                    mirrorsByRow.computeIfAbsent(row, k -> new ArrayList<>()).add(col);
                    mirrorsByColumn.computeIfAbsent(col, k -> new ArrayList<>()).add(row);
                }
            } catch (NullPointerException e){ // means that the input stream is closed
                break;
            }

            // add imaginary mirrors to the start and success points, to be able to stop properly
            mirrorsByRow.computeIfAbsent(rows - 1, k -> new ArrayList<>()).add(cols);
            mirrorsByRow.computeIfAbsent(0L, k -> new ArrayList<>()).add(-1L);

            // sort every arraylist in mirrorsByRow and mirrorsByColumn
            mirrorsByRow.values().forEach(Collections::sort);
            mirrorsByColumn.values().forEach(Collections::sort);

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
