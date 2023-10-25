import java.io.File
import kotlin.random.Random
import java.security.MessageDigest

/**
 * 
 */
class Game {

    companion object {
        const val WORDS_PER_PLAYER: Int = 5
    }

    var players: List<Player>
    var allWords: List<String>
    var drawPile: MutableList<String>

    lateinit var seedWord: String

    constructor(players: List<Player>, allWords: List<String>) {
        this.players = players
        this.allWords = allWords
        drawPile = mutableListOf()
    }

    // Start the game
    fun initialize(seedWord: String) {
        this.seedWord = seedWord
        assignWordsToPlayers()
    }

    private fun assignWordsToPlayers() {
        val shuffledWords = allWords.shuffled().toMutableList()

        players.forEach { player -> 
            val wordsForPlayer = mutableListOf<String>()

            repeat(WORDS_PER_PLAYER) {
                if (shuffledWords.isNotEmpty()) {
                    wordsForPlayer.add(shuffledWords.removeAt(0))
                }
            }
            
            player.dealHand(wordsForPlayer)
        }
        drawPile = shuffledWords 
    }

    fun drawFromPile(): String? {
        return if (drawPile.isNotEmpty()) drawPile.removeAt(0) else null
    }

    fun checkWinOrLose(player: Player): Boolean {
        when {
            player.skelletonKeys <= 0 -> {
                println("${player.name} lost the game!")
                return true
            }
            player.skelletonKeys >= 3 -> {
                println("${player.name} won the game!")
                return true
            }
            else -> return false
        }
    }

    fun playGame(seedWord: String) {
        initialize(seedWord)
        var gameFinished = false
        while (!gameFinished) {
            for (player in players) {
                takeTurnFor(player)
                if (checkWinOrLose(player)) {
                    gameFinished = true
                    break
                }
            }
        }
        println("Game Over!")
    }

    fun hashWithSeed(input: String): String {
        val combined = "$input:$this.seed"  // Combines input and seed with a separator
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(combined.toByteArray())
        return digest.joinToString("") {
            "%02x".format(it)
        }
    }

    fun askForHashedWord(targetPlayer: Player, hashedWord: String): Boolean {
        return targetPlayer.wordList.any { hashWithSeed(it) == hashedWord }
    }

    fun takeTurnFor(player: Player) {
        val word = player.getRandomWordFromHand()
        val hashedWord = hashWithSeed(word) 
    
        val otherPlayers = players.filter { it != player }
        val targetPlayer = otherPlayers.shuffled().first()
    
        println("${player.name} asks ${targetPlayer.name} for: $hashedWord")
        val targetPlayerHasMatchingWord: Boolean = askForHashedWord(targetPlayer, hashedWord)
    
        if (targetPlayerHasMatchingWord) {
            println("${targetPlayer.name} hands over the word '$hashedWord' to ${player.name}.")
            targetPlayer.removeWord(word)
            player.addWordToHand(word)
    
            if (player.checkForPair(word)) {
                player.increaseSkeletonKeys()
                println("${player.name} now has ${player.skelletonKeys} skeleton keys.")
                player.removeAllInstancesOfWord(word)
            }
    
        } else {
            println("${targetPlayer.name} says: Go fish, ${player.name}!")
            val drawnWord = drawFromPile()
            if (drawnWord == null) {
                println("The draw pile is empty.")
            } else {
                val drawnWordHashed = hashWithSeed(word) 
                println("${player.name} drew '$drawnWordHashed'.")
                player.addWordToHand(drawnWord)
            }
    
            player.decreaseSkeletonKeys()
            println("${player.name} loses a skeleton key. Now has ${player.skelletonKeys} skeleton keys.")
        }
    }
}

class Player(val name: String) {

    // Start with two, if you get the three you win, if you get to zero you lose.
    var skelletonKeys: Int = 2
    lateinit var wordList: MutableList<String>

    fun dealHand(wordList: List<String>) {
        this.wordList = wordList.toMutableList()
    }

    fun addWordToHand(word: String) {
        wordList.add(word)
    }

    fun hasWord(word: String): Boolean {
        return wordList.contains(word)
    }

    fun removeWord(word: String): Boolean {
        return wordList.remove(word)
    }

    fun checkForPair(word: String): Boolean {
        return wordList.count { it == word } >= 2
    }

    fun getRandomWordFromHand(): String {
        return wordList.shuffled().first()
    }

    fun increaseSkeletonKeys() {
        skelletonKeys++
    }

    fun decreaseSkeletonKeys() {
        skelletonKeys--
    }

    fun removeAllInstancesOfWord(word: String) {
        wordList.removeAll { it == word }
    }

}

fun main() {

    val player1 = Player("Sean")
    val player2 = Player("Jeff")
    val player3 = Player("Darsh")

    val words = File("wordlist.csv").readText().split(",").map { it.trim() }

    val game = Game(listOf(player1, player2, player3), words)

    val seedWords = File("seedlist.csv").readText().split(",").map { it.trim() }
    val randomSeedWord = seedWords.shuffled().first()
    game.playGame(randomSeedWord)

}