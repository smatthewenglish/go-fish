import java.io.File
import kotlin.random.Random

/**
 * 
 */
class Player {

    var name: String
    // Start with two, if you get the three you win, if you get to zero you lose.
    var skelletonKeys: Int = 2

    lateinit var wordList: List<String>

    constructor(name: String) {
        this.name = name
    }

    fun dealHand(wordList: List<String>) {
        this.wordList = wordList
    }

    fun hasWord(word: String): Boolean {
        return wordList.contains(word)
    }

}

class Game {

    companion object {
        const val WORDS_PER_PLAYER: Int = 5
    }

    var players: List<Player>
    var allWords: List<String>
    lateinit var seedWord: String

    constructor(players: List<Player>, allWords: List<String>) {
        this.players = players
        this.allWords = allWords
    }

    // Start the game
    //fun initializeRound(seedWord: String) {
    fun initializeRound() {
        //this.seedWord = seedWord
        assignWordsToPlayers()
    }

    private fun assignWordsToPlayers() {
        val shuffledWords = allWords.shuffled()
    
        players.forEachIndexed { playerIndex, player ->
            val startIndex = playerIndex * WORDS_PER_PLAYER
            val endIndex = startIndex + WORDS_PER_PLAYER
            val wordsForPlayer = shuffledWords.subList(startIndex, endIndex)
            player.dealHand(wordsForPlayer)
        }
    }
}

fun main() {

    val player1 = Player("Sean")
    val player2 = Player("Greta")

    val words = File("wordlist.csv").readLines()

    val game = Game(listOf(player1, player2), words)

    
    game.initializeRound()

}