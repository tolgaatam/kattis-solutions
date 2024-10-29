data class Cycle(
    val indicesSet: Set<Int>,
    val magicNumbersSet: Set<Int>
)

data class Pole(
    val indicesList: List<Int>,
    val indicesOrderMap: Map<Int, Int>,
    val finalCycle: Cycle?
)

fun main() {
    val n = readln().toInt()
    val board = readln().split(" ").map { it.toInt() }

    var numberOfWinningOutcomes = 0

    val indexToCycleArray = Array<Cycle?>(n){null}
    val indexToPoleArray = Array<Pole?>(n){null}

    @OptIn(ExperimentalStdlibApi::class)
    for(s in 0 ..< n){
        if(indexToCycleArray[s] != null || indexToPoleArray[s] != null){
            continue
        }
        var currIndex = s
        val indicesList = ArrayList<Int>()
        val indicesOrderMap = HashMap<Int, Int>()

        indicesOrderMap[s] = 0
        indicesList.add(s)

        while(true){
            currIndex = currIndex + board[currIndex]
            if(currIndex < 0 || currIndex >= n){ // Dead cycle (dead-end)
                val newPole = Pole(indicesList, indicesOrderMap, null)
                for(elt in indicesList){
                    indexToPoleArray[elt] = newPole
                }
                break
            } else if(indexToCycleArray[currIndex] != null){ // currIndex already in a cycle
                val newPole = Pole(indicesList, indicesOrderMap, indexToCycleArray[currIndex])
                for(elt in indicesList){
                    indexToPoleArray[elt] = newPole
                }
                break
            } else if(indexToPoleArray[currIndex] != null){ // currIndex already in a pole
                val numberOfElementsBeforeMerge = indicesList.size
                val existingPole = indexToPoleArray[currIndex]!!
                val currIndexOrderInExistingPole = existingPole.indicesOrderMap[currIndex]!!
                for(index in existingPole.indicesList.subList(currIndexOrderInExistingPole,existingPole.indicesList.size)){
                    indicesOrderMap[index] = indicesList.size
                    indicesList.add(index)
                }

                val newPole = Pole(indicesList, indicesOrderMap, existingPole.finalCycle)

                if(currIndexOrderInExistingPole == 0){ // replace the whole pole if we found its head
                    for(elt in indicesList){
                        indexToPoleArray[elt] = newPole
                    }
                } else { // create a new long pole without disturbing the existing one
                    for(elt in indicesList.subList(0, numberOfElementsBeforeMerge)){
                        indexToPoleArray[elt] = newPole
                    }
                }
                break
            } else if (indicesOrderMap.containsKey(currIndex)){ // new cycle forming
                val cycleFormingStartsAt = indicesOrderMap[currIndex]!!
                val cycleIndicesList = indicesList.subList(cycleFormingStartsAt, indicesList.size)
                val newCycle = Cycle(
                    HashSet(cycleIndicesList),
                    HashSet(cycleIndicesList.map { board[it] })
                )
                for(elt in cycleIndicesList){
                    indexToCycleArray[elt] = newCycle
                }

                if(cycleFormingStartsAt > 0){ // there is also a pole to generate
                    val poleIndicesList = indicesList.subList(0, cycleFormingStartsAt)
                    val newPole = Pole(
                        poleIndicesList,
                        indicesOrderMap.filterValues { it < cycleFormingStartsAt },
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

    // Collect results now

    @OptIn(ExperimentalStdlibApi::class)
    for(s in 0 ..< n){
        if(indexToCycleArray[s] != null){
            numberOfWinningOutcomes += indexToCycleArray[s]!!.magicNumbersSet.size
        } else { // Pole
            val magicNumbersReachable = HashSet<Int>()
            val pole = indexToPoleArray[s]!!
            val startingOrderInPole = pole.indicesOrderMap[s]!!

            @OptIn(ExperimentalStdlibApi::class)
            for(i in startingOrderInPole ..< pole.indicesList.size){
                magicNumbersReachable.add(board[pole.indicesList[i]])
            }
            if(pole.finalCycle != null){
                magicNumbersReachable.addAll(pole.finalCycle.magicNumbersSet)
            }

            numberOfWinningOutcomes += magicNumbersReachable.size
        }
    }

    println(numberOfWinningOutcomes)
}