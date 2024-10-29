import java.util.Arrays.fill

fun main() {
    val n = readln().toInt()
    val board = readln().split(" ").map { it.toInt() }

    val possibleMagicNumbers = board.toSet() // creates a LinkedHashSet, which is faster for readonly sets like this
    val memo = arrayOfNulls<Boolean?>(n)

    var numberOfWinningOutcomes = 0

    for (magicNumber in possibleMagicNumbers){
        fill(memo, null)

        // Get over with the obvious wins
        @OptIn(ExperimentalStdlibApi::class)
        for(s in 0 ..< n){
            if(board[s] == magicNumber){
                memo[s] = true
                numberOfWinningOutcomes++
            }
        }

        // test each starting index
        // Note: the OptIn annotation may seem redundant, but kattis compiler is old and needs this
        @OptIn(ExperimentalStdlibApi::class)
        for(s in 0 ..< n){
            if(memo[s] != null){ // we have noted this index already
                continue
            }

            var currIndex = s
            val visitedIndices = hashSetOf<Int>()
            while(true){
                if (currIndex < 0 || currIndex >= n) { // out of bounds issue
                    for(visitedIndex in visitedIndices){
                        memo[visitedIndex] = false
                    }
                    break
                } else if (memo[currIndex] == true){ // known-to-succeed or currently discovered success
                    numberOfWinningOutcomes += visitedIndices.size
                    for(visitedIndex in visitedIndices){
                        memo[visitedIndex] = true
                    }
                    break
                } else if (memo[currIndex] == false || currIndex in visitedIndices) { // known-to-fail or cycle issue
                    for(visitedIndex in visitedIndices){
                        memo[visitedIndex] = false
                    }
                    memo[currIndex] = false
                    break
                } else { // keep looking
                    visitedIndices.add(currIndex)
                    currIndex += board[currIndex]
                }
            }
        }
    }

    println(numberOfWinningOutcomes)
}