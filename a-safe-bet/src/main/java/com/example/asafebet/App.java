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

    public static void sweepLine(List<FlatLine> horizontalLines, List<FlatLine> verticalLines){
        // Build events: horizontal add(+1)/remove(-1) and vertical queries(0)
        List<Event> events = new ArrayList<>();
        for(var horizontalLine : horizontalLines){
            events.add(new Event(horizontalLine.start(), horizontalLine.main(), horizontalLine.main(), (byte) 1));
            events.add(new Event(horizontalLine.end(), horizontalLine.main(), horizontalLine.main(), (byte) -1));
        }
        for(var verticalLine : verticalLines){
            events.add(new Event(verticalLine.main(), verticalLine.start(), verticalLine.end(), (byte) 0));
        }

        events.sort((a, b) -> {
            if (a.col() == b.col()) {
                return b.type() - a.type();
            }
            return Long.compare(a.col(), b.col());
        });

        // Coordinate compress the rows that can be active (horizontal line mains)
        var rowsSet = new TreeSet<Long>();
        for (var hl : horizontalLines) rowsSet.add(hl.main());

        if (rowsSet.isEmpty()) return; // nothing to do

        var rowsList = new ArrayList<Long>(rowsSet);
        var indexMap = new HashMap<Long, Integer>();
        for (int i = 0; i < rowsList.size(); i++) indexMap.put(rowsList.get(i), i + 1); // 1-based for BIT

        Fenwick bit = new Fenwick(rowsList.size());

        // Process events in sweep-line order, using BIT to maintain active rows
        for (Event event : events) {
            switch (event.type()) {
                case 1 -> { // add row
                    Integer idx = indexMap.get(event.row1());
                    if (idx != null) bit.add(idx, 1);
                }
                case -1 -> { // remove row
                    Integer idx = indexMap.get(event.row1());
                    if (idx != null) bit.add(idx, -1);
                }
                case 0 -> { // vertical query over [row1, row2]
                    // find compressed index range [l, r] covering rows in [row1, row2]
                    int l = lowerBound(rowsList, event.row1());
                    int r = upperBound(rowsList, event.row2()) - 1;
                    if (l <= r) {
                        long sum = bit.sum(r + 1) - bit.sum(l);
                        numberOfInsertedMirrors += sum;
                        if (sum > 0) {
                            long prefBefore = bit.sum(l);
                            int pos = bit.findFirstGreater(prefBefore);
                            if (pos >= 1 && pos <= r + 1) {
                                long rowVal = rowsList.get(pos - 1);
                                smallestMirrorInsertedLexicoPosition = Math.min(smallestMirrorInsertedLexicoPosition, rowVal * cols + event.col());
                            }
                        }
                    }
                }
            }
        }
    }

    // lowerBound: first index in sorted list >= value (0-based)
    private static int lowerBound(List<Long> list, long value){
        int l = 0, r = list.size();
        while(l < r){
            int m = (l + r) >>> 1;
            if(list.get(m) >= value) r = m; else l = m + 1;
        }
        return l;
    }

    // upperBound: first index in sorted list > value (0-based)
    private static int upperBound(List<Long> list, long value){
        int l = 0, r = list.size();
        while(l < r){
            int m = (l + r) >>> 1;
            if(list.get(m) > value) r = m; else l = m + 1;
        }
        return l;
    }

    // Fenwick tree (1-based)
    static class Fenwick{
        private final long[] bit;
        private final int n;
        Fenwick(int n){ this.n = n; bit = new long[n+1]; }
        void add(int idx, long delta){
            for(int i = idx; i <= n; i += i & -i) bit[i] += delta;
        }
        long sum(int idx){ // prefix sum up to idx (1-based). if idx==0 returns 0
            long res = 0;
            for(int i = idx; i > 0; i -= i & -i) res += bit[i];
            return res;
        }
        // find smallest index such that prefix sum > target. Returns 1-based index, or n+1 if none
        int findFirstGreater(long target){
            int idx = 0;
            int bitMask = Integer.highestOneBit(n);
            long sum = 0;
            for(int k = bitMask; k != 0; k >>= 1){
                int next = idx + k;
                if(next <= n && sum + bit[next] <= target){
                    idx = next;
                    sum += bit[next];
                }
            }
            return idx + 1;
        }
    }

    public static boolean generateLines(List<FlatLine> horizontalLines, List<FlatLine> verticalLines, long startRow, long startCol, byte startDirection){
        long currRow = startRow, currCol = startCol;
        byte currDirection = startDirection;

        while(true){
            switch (currDirection){
                case 0 -> { // up
                    Long nextRow = mirrorsByColumn.get(currCol).lower(currRow);
                    if(nextRow == null){
                        verticalLines.add(new FlatLine(currCol, 0L, currRow - 1));
                        return false;
                    } else {
                        if(nextRow + 1 <= currRow - 1) {
                            verticalLines.add(new FlatLine(currCol, nextRow + 1, currRow - 1));
                        }
                        currRow = nextRow;
                    }
                }
                case 1 -> { // left
                    Long nextCol = mirrorsByRow.get(currRow).lower(currCol);
                    if(nextCol == null){
                        horizontalLines.add(new FlatLine(currRow, 0L, currCol - 1));
                        return false;
                    } else {
                        if(nextCol + 1 <= currCol - 1){
                            horizontalLines.add(new FlatLine(currRow, nextCol + 1, currCol - 1));
                        }
                        currCol = nextCol;
                    }
                }
                case 2 -> { // down
                    Long nextRow = mirrorsByColumn.get(currCol).higher(currRow);
                    if(nextRow == null){
                        verticalLines.add(new FlatLine(currCol, currRow + 1, rows - 1));
                        return false;
                    } else {
                        if(currRow + 1 <= nextRow - 1) {
                            verticalLines.add(new FlatLine(currCol, currRow + 1, nextRow - 1));
                        }
                        currRow = nextRow;
                    }
                }
                default -> { // 3 - right
                    Long nextCol = mirrorsByRow.get(currRow).higher(currCol);
                    if(nextCol == null){
                        horizontalLines.add(new FlatLine(currRow, currCol + 1, cols - 1));
                        return false;
                    } else {
                        if(nextCol == cols){ // we reached the end without any mirrors. no other processing is needed.
                            return true;
                        }
                        if(currCol + 1 <= nextCol - 1) {
                            horizontalLines.add(new FlatLine(currRow, currCol + 1, nextCol - 1));
                        }
                        currCol = nextCol;
                    }
                }
            }

            currDirection = mirrorReflection(currDirection, mirrors.get(currRow * cols + currCol));
        }
    }

    public static void solve(){
        List<FlatLine> originalHorizontalLines = new ArrayList<>();
        List<FlatLine> originalVerticalLines = new ArrayList<>();
        // start from top left corner to the `right` direction
        canBeOpenedWithoutMirror = generateLines(originalHorizontalLines, originalVerticalLines, 0L, -1L, (byte) 3);
        if(canBeOpenedWithoutMirror){
            return;
        }

        // we can't reach the end without any mirrors (we would have returned from the function if we did), let's try with mirrors
        // we will traverse from the end to the start, and collect line information
        List<FlatLine> reverseHorizontalLines = new ArrayList<>();
        List<FlatLine> reverseVerticalLines = new ArrayList<>();
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
            } catch (IOException e){ // input stream closed or EOF
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
