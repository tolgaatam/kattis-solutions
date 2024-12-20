import java.util.LinkedList
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import kotlin.collections.HashSet

open class Ending(open val id: Int) {
    data class Cycle(
        override val id: Int,
        val indicesList: List<Int>
    ): Ending(id)

    data class Void(
        override val id: Int,
        val prevIndex: Int
    ): Ending(id)
}

data class Pole(
    val ending: Ending
)

fun main() {
    val reader = System.`in`.bufferedReader()
    val n = reader.readLine().toInt()
    val board = reader.readLine().split(" ").map { it.toInt() }

    var numberOfWinningOutcomes: Long = 0

    val reverseGraph = Array<MutableList<Int>>(n){ ArrayList() } // shows incoming edges
    @OptIn(ExperimentalStdlibApi::class)
    for (s in 0 ..< n){
        val sNext = s + board[s]
        @OptIn(ExperimentalStdlibApi::class)
        if(sNext in 0..< n){
            reverseGraph[sNext].add(s)
        }
    }

    val indexToCycleArray = Array<Ending.Cycle?>(n){null}
    val indexToPoleArray = Array<Pole?>(n){null}

    val endingList = ArrayList<Ending>()

    @OptIn(ExperimentalStdlibApi::class)
    for(s in 0 ..< n){
        if(indexToPoleArray[s] != null || indexToCycleArray[s] != null){
            continue
        }
        var currIndex = s
        val indicesList = ArrayList<Int>()
        val indicesOrderMap = HashMap<Int, Int>()

        indicesOrderMap[s] = 0
        indicesList.add(s)

        while(true){
            currIndex += board[currIndex]
            @OptIn(ExperimentalStdlibApi::class)
            if(currIndex !in 0 ..< n){ // Dead cycle (dead-end)
                val voidEnding = Ending.Void(endingList.size, indicesList.last())
                endingList.add(voidEnding)

                val newPole = Pole(voidEnding)
                for(elt in indicesList){
                    indexToPoleArray[elt] = newPole
                }

                break
            } else if(indexToCycleArray[currIndex] != null){ // currIndex already in a cycle
                val newPole = Pole(indexToCycleArray[currIndex]!!)
                for(elt in indicesList){
                    indexToPoleArray[elt] = newPole
                }

                break
            } else if(indexToPoleArray[currIndex] != null){ // currIndex already in a pole
                val existingPole = indexToPoleArray[currIndex]!!

                val newPole = Pole(existingPole.ending)
                for(elt in indicesList){
                    indexToPoleArray[elt] = newPole
                }

                break
            } else if (indicesOrderMap.containsKey(currIndex)){ // new cycle forming
                val cycleFormingStartsAt = indicesOrderMap[currIndex]!!
                val cycleIndicesList = indicesList.subList(cycleFormingStartsAt, indicesList.size)
                val newCycle = Ending.Cycle(
                    endingList.size,
                    cycleIndicesList,
                )
                endingList.add(newCycle)

                for(elt in cycleIndicesList){
                    indexToCycleArray[elt] = newCycle
                }

                if(cycleFormingStartsAt > 0){ // there is also a pole to generate
                    val poleIndicesList = indicesList.subList(0, cycleFormingStartsAt)
                    val newPole = Pole(
                        newCycle
                    )
                    for(elt in poleIndicesList){
                        indexToPoleArray[elt] = newPole
                    }
                }
                break
            } else { // nothing special, just move on
                indicesOrderMap[currIndex] = indicesList.size
                indicesList.add(currIndex)
            }
        }
    }

    for(ending in endingList){

        fun dfs(s: Int, magicNumbersSet: MutableSet<Int>){
            val isNewMagicNumberAdded = magicNumbersSet.add(board[s])
            numberOfWinningOutcomes += magicNumbersSet.size.toLong()

            for(prevS in reverseGraph[s]){
                dfs(prevS, magicNumbersSet)
            }

            if(isNewMagicNumberAdded){
                magicNumbersSet.remove(board[s])
            }
        }

        when(ending){
            is Ending.Cycle -> {
                val cycleMagicNumberSet = HashSet(ending.indicesList.map { board[it] })
                val indicesSet = HashSet(ending.indicesList)

                numberOfWinningOutcomes += ending.indicesList.size.toLong() * cycleMagicNumberSet.size.toLong()

                for(s in ending.indicesList){
                    for(prevS in reverseGraph[s].filter{it !in indicesSet}){
                        dfs(prevS, cycleMagicNumberSet)
                    }
                }
            }
            is Ending.Void -> { // Void
                dfs(ending.prevIndex, HashSet())
            }
            else -> {
                throw RuntimeException("Endings cannot be initialized without its derived types")
            }
        }
    }

    println(numberOfWinningOutcomes)
}