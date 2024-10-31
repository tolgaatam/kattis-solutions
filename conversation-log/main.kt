import kotlin.collections.HashMap

fun main() {
    val wordTotalCountMap = HashMap<String, Int>()
    val wordUniqueUserCountMap = HashMap<String, Int>()
    val userWordCorpusMap = HashMap<String, String>()

    val reader = System.`in`.bufferedReader()
    val n = reader.readLine().toInt()

    @OptIn(ExperimentalStdlibApi::class)
    for(i in 0 ..< n){
        val splitted = reader.readLine().split(' ', limit = 2)
        val username = splitted[0]
        val rest = splitted[1]

        userWordCorpusMap[username] = "${userWordCorpusMap[username] ?: ""} $rest"
    }

    for((_, wordCorpus) in userWordCorpusMap){
        val wordList = wordCorpus.substring(1).split(' ')
        val wordCounts = wordList.groupingBy { it }.eachCount()
        for((word, count) in wordCounts){
            wordUniqueUserCountMap[word] = (wordUniqueUserCountMap[word]?: 0)  + 1
            wordTotalCountMap[word] = (wordTotalCountMap[word]?: 0) + count
        }
    }

    val userCount = userWordCorpusMap.size
    val winningWords = wordUniqueUserCountMap
        .map { Pair(it.key, it.value) }
        .filter { it.second == userCount }
        .map { it.first }
        .toMutableList()

    winningWords.sortWith {a, b ->
        if ("${(10000000-wordTotalCountMap[a]!!).toString().padStart(7, '0')}${a}"
         < "${(10000000-wordTotalCountMap[b]!!).toString().padStart(7, '0')}${b}")
            -1
        else
            1
    }

    if(winningWords.isEmpty()){
        println("ALL CLEAR")
    } else {
        println(winningWords.joinToString("\n"))
    }
}