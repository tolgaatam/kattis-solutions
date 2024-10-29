import java.util.LinkedList

fun main() {
    val n = readln().toInt()
    val board = readln().split(" ").map { it.toInt() }

    val possibleMagicNumbers = HashMap<Int, MutableList<Int>>()
    val reverseGraph = Array<MutableList<Int>>(n){LinkedList()} // shows incoming edges

    @OptIn(ExperimentalStdlibApi::class)
    for (s in 0 ..< n){
        val sVal = board[s]
        if(!possibleMagicNumbers.containsKey(sVal)){
            possibleMagicNumbers[sVal] = ArrayList()
        }
        possibleMagicNumbers[sVal]!!.add(s)

        val sNext = s + board[s]
        @OptIn(ExperimentalStdlibApi::class)
        if(sNext in 0..<n){
            reverseGraph[sNext].add(s)
        }
    }

    var numberOfWinningOutcomes = 0

    for (magicNumber in possibleMagicNumbers.keys){
        val winningIndices = HashSet<Int>()

        val discoveredIndicesThisRound = HashSet<Int>()
        val discoveredIndicesLastRound = HashSet<Int>()

        // Detect starting indices which yield the magic number directly
        @OptIn(ExperimentalStdlibApi::class)
        for(s in possibleMagicNumbers[magicNumber]!!){
            winningIndices.add(s)
            discoveredIndicesThisRound.add(s)
        }

        while (discoveredIndicesThisRound.size > 0){
            discoveredIndicesLastRound.clear()
            discoveredIndicesLastRound.addAll(discoveredIndicesThisRound)
            discoveredIndicesThisRound.clear()

            @OptIn(ExperimentalStdlibApi::class)
            for(s in discoveredIndicesLastRound){
                for(incomingS in reverseGraph[s]){
                    if(incomingS !in winningIndices){
                        winningIndices.add(incomingS)
                        discoveredIndicesThisRound.add(incomingS)
                    }
                }
            }
        }

        numberOfWinningOutcomes += winningIndices.size
    }

    println(numberOfWinningOutcomes)
}