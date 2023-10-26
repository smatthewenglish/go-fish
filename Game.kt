import java.io.File
import kotlin.random.Random
import java.security.MessageDigest

/**
 * The Game class represents a word-based game where players exchange words from 
 * their hand trying to create pairs
 *
 * Author: Sean Matt English
 * Date: 2023-10-25
 */
class Game {

    companion object {
        // Each player is dealt this number of words at the beginning of the game
        const val WORDS_PER_PLAYER: Int = 5
    }

    var players: MutableList<Player> // List of all players participating in the game
    var allWords: List<String> // The entire wordlist used for the game
    var drawPile: MutableList<String> // Pile from which players can draw words

    lateinit var seedWord: String // Seed word used for hashing purposes

    constructor(players: List<Player>, allWords: List<String>) {
        this.players = players.toMutableList()
        this.allWords = allWords
        drawPile = mutableListOf()
    }

    /**
     * Initializes the game with a seed word and assigns words to players.
     */
    fun initialize(seedWord: String) {
        this.seedWord = seedWord
        assignWordsToPlayers()
    }

    /**
     * Assign words to players randomly from a shuffled wordlist.
     */
    private fun assignWordsToPlayers() {
        val shuffledWords: MutableList<String> = allWords.shuffled().toMutableList()

        players.forEach { player -> 
            val wordsForPlayer: MutableList<String> = mutableListOf<String>()

            repeat(WORDS_PER_PLAYER) {
                wordsForPlayer.add(shuffledWords.removeAt(0))
            }
            player.dealHand(wordsForPlayer)
        }
        drawPile = shuffledWords 
    }

    /**
     * Allows a player to draw a word from the pile.
     */
    fun drawFromPile(): String? {
        return if (drawPile.isNotEmpty()) drawPile.removeAt(0) else null
    }

    /**
     * Defines a turn for a player: they select a word, ask another player for it, and handle the outcome.
     */
    fun takeTurnFor(player: Player) {
        val word: String = player.getRandomWordFromHand()
        val wordHashed: String = hashWithSeed(word)
    
        val opponentList: List<Player> = players.filter { listPlayer -> listPlayer != player && listPlayer.skeletonKeys > 0 }
        val opponent: Player = opponentList.shuffled().first()
    
        println("${player.name} asks ${opponent.name} for $wordHashed.")
        val targetPlayerHasMatchingWord: Boolean = askForHashedWord(opponent, wordHashed)
    
        if (targetPlayerHasMatchingWord) {
            exchangeWords(player, opponent, word, wordHashed)
        } else {
            if (player.skeletonKeys == 0) {
                println("${player.name} has no skeleton keys and cannot draw from the pile.")
                return
            }
            goFish(player)
        }
    }
    
    /**
     * Executes the main game loop where players take turns until someone wins.
     * Eliminated players are removed from the game, and the game continues with the remaining players.
     */
    fun playGame(currentPlayers: MutableList<Player>, currentPlayerIndex: Int = 0) {
        // Check for winner
        if (currentPlayers.size == 1) {
            printWinner(currentPlayers[0])
            return
        }
        // Get current player
        val currentPlayer: Player = currentPlayers[currentPlayerIndex]
        takeTurnFor(currentPlayer)
    
        if (currentPlayer.skeletonKeys == 0) {
            currentPlayers.remove(currentPlayer)
            println("${currentPlayer.name} has lost the game, ${currentPlayers.size} ${if (currentPlayers.size == 1) "player remains" else "players remain"}.")

            val nextIndex: Int = currentPlayerIndex % currentPlayers.size
            playGame(currentPlayers, nextIndex)
        } else if (currentPlayer.skeletonKeys == 3) {
            printWinner(currentPlayer)
        } else {
            // Move to next player or wrap around
            val nextIndex: Int = (currentPlayerIndex + 1) % currentPlayers.size
            playGame(currentPlayers, nextIndex)
        }
    }

    /**
     * Utility function to output the winner of the game
     */
    fun printWinner(player: Player) {
        println("${player.name} is the winner!")
        println("Game Over!")
    }

    /**
     * Generates a hash using the input word and the game's seed word.
     */
    fun hashWithSeed(input: String): String {
        val combined: String = "$input:$this.seedWord"  // Combines input and seed with a separator
        val md: MessageDigest = MessageDigest.getInstance("SHA-256")
        val digest: ByteArray = md.digest(combined.toByteArray())
        return digest.joinToString("") {
            "%02x".format(it)
        }
    }

    /**
     * Asks a player if they have a word in their hand matching a given hashed word.
     */
    fun askForHashedWord(targetPlayer: Player, hashedWord: String): Boolean {
        return targetPlayer.wordList.any { hashWithSeed(it) == hashedWord }
    }
    
    private fun exchangeWords(player: Player, targetPlayer: Player, word: String, hashedWord: String) {
        println("${targetPlayer.name} hands over $hashedWord to ${player.name}.")
        targetPlayer.removeWord(word)
        player.addWordToHand(word)
    
        if (player.checkForPair(word)) {
            player.increaseSkeletonKeys(word)
        }
    }
    
    private fun goFish(player: Player) {
        val drawnWord: String? = drawFromPile()
        if (drawnWord == null) {
            println("The draw pile is empty.")
        } else {
            val drawnWordHashed: String = hashWithSeed(drawnWord)
            println("${player.name} drew $drawnWordHashed.")
            player.addWordToHand(drawnWord)
        }
        player.decreaseSkeletonKeys()
    }
    
}

/**
 * Represents a player in the game with their name, hand of words, and skeleton key count.
 */
class Player(val name: String) {

    // Start with two, if you get the three you win, if you get to zero you lose.
    var skeletonKeys: Int = 2
    lateinit var wordList: MutableList<String>

    /**
     * Deals an initial hand of words to the player.
     */
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
        return wordList.count { listWord -> listWord == word } >= 2
    }

    fun getRandomWordFromHand(): String {
        return wordList.shuffled().first()
    }

    fun increaseSkeletonKeys(word: String) {
        skeletonKeys++
        println("${name} now has ${skeletonKeys} skeleton ${if (skeletonKeys == 1) "key" else "keys"}.")
        wordList.removeAll { listWord -> listWord == word }
    }

    fun decreaseSkeletonKeys() {
        skeletonKeys--
        println("${name} loses a skeleton key. Now has ${skeletonKeys} skeleton ${if (skeletonKeys == 1) "key" else "keys"}.")
    }

}

/**
 * Main game execution starts here. Players are created, word lists are loaded, 
 * and the game is initialized and started.
 */
fun main() {

    val player1: Player = Player("A")
    val player2: Player = Player("B")
    //val players: MutableList<Player> = listOf(player1, player2).toMutableList()
    val player3: Player = Player("C")
    val player4: Player = Player("D")
    //val players: MutableList<Player> = listOf(player1, player2, player3, player4).toMutableList()
    val player5: Player = Player("E")
    val player6: Player = Player("F")
    val players: MutableList<Player> = listOf(player1, player2, player3, player4, player5, player6).toMutableList()

    // Load words for the game from a CSV file
    val words: List<String> = File("wordlist.csv").readText().split(",").map { it.trim() }

    val game: Game = Game(players, words)

    // Load seed words and select a random one for hashing purposes
    val seedWords: List<String> = File("seedlist.csv").readText().split(",").map { it.trim() }
    val randomSeedWord: String = seedWords.shuffled().first()
    game.initialize(randomSeedWord)
    game.playGame(players)

}