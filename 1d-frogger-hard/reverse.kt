import java.util.Arrays.fill

fun main() {
    val n = readln().toInt()
    val board = readln().split(" ").map { it.toInt() }

    val possibleMagicNumbers = board.toSet() // creates a LinkedHashSet, which is faster for readonly sets like this
    val memo = arrayOfNulls<Boolean>(n)

    var numberOfWinningOutcomes = 0

    for (magicNumber in possibleMagicNumbers){
        fill(memo, null)

        var discoveredThisRound = 0
        // Detect direct wins
        @OptIn(ExperimentalStdlibApi::class)
        for(s in 0 ..< n){
            if(board[s] == magicNumber){
                memo[s] = true
                discoveredThisRound++
            }
        }
        numberOfWinningOutcomes += discoveredThisRound

        while (discoveredThisRound > 0){
            discoveredThisRound = 0
            @OptIn(ExperimentalStdlibApi::class)
            for(currIndex in 0 ..< n){
                if(memo[currIndex] != null) {
                    continue
                }

                val nextIndex = currIndex + board[currIndex]
                if(nextIndex < 0 || nextIndex >= n || memo[nextIndex] == false){
                    memo[currIndex] = false
                } else if(memo[nextIndex] == true){
                    memo[currIndex] = true
                    discoveredThisRound++
                }
            }

            numberOfWinningOutcomes += discoveredThisRound
        }
    }

    println(numberOfWinningOutcomes)
}