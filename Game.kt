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

    /**
     * Allows a player to draw a word from the pile.
     */
    fun drawFromPile(): String? {
        return if (drawPile.isNotEmpty()) drawPile.removeAt(0) else null
    }

    /**
     * Executes the main game loop where players take turns until someone wins.
     * Eliminated players are removed from the game, and the game continues with the remaining players.
     */
    fun playGame(seedWord: String) {
        initialize(seedWord)
        
        while (players.size > 1) {
            for (player in players.toList()) {
                if (players.size <= 1) {
                    break // Exit the loop if there's only one player remaining
                }
                
                takeTurnFor(player)
                
                if (player.skeletonKeys >= 3) {
                    return
                }
            }
        }
        
        if (players.isNotEmpty()) {
            println("It's a draw!")
            println("Game Over!")
        }
    }

    /**
     * Generates a hash using the input word and the game's seed word.
     */
    fun hashWithSeed(input: String): String {
        val combined = "$input:$this.seedWord"  // Combines input and seed with a separator
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(combined.toByteArray())
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

    /**
     * Defines a turn for a player: they select a word, ask another player for it, and handle the outcome.
     */
    fun takeTurnFor(player: Player) {
        val word = player.getRandomWordFromHand()
        val hashedWord = hashWithSeed(word)
    
        val otherPlayers = players.filter { it != player }
        val targetPlayer = otherPlayers.shuffled().first()
    
        println("${player.name} asks ${targetPlayer.name} for: $hashedWord")
        val targetPlayerHasMatchingWord = askForHashedWord(targetPlayer, hashedWord)
    
        if (targetPlayerHasMatchingWord) {
            exchangeWords(player, targetPlayer, word, hashedWord)
        } else {
            goFish(player)
        }
    
        checkWinOrLose(player)
    }

    private fun checkWinOrLose(player: Player) {
        if (player.skeletonKeys <= 0) {
            println("${player.name} has lost the game.")
            players.remove(player)
        } else if (player.skeletonKeys >= 3) {
            println("${player.name} has won the game!")
            println("Game Over!")
            players.clear() // Remove all remaining players from the game
        }
    }
    
    private fun exchangeWords(player: Player, targetPlayer: Player, word: String, hashedWord: String) {
        println("${targetPlayer.name} hands over the word '$hashedWord' to ${player.name}.")
        targetPlayer.removeWord(word)
        player.addWordToHand(word)
    
        if (player.checkForPair(word)) {
            player.increaseSkeletonKeys()
            println("${player.name} now has ${player.skeletonKeys} skeleton keys.")
            player.removeAllInstancesOfWord(word)
        }
    }
    
    private fun goFish(player: Player) {
        val drawnWord = drawFromPile()
        if (drawnWord == null) {
            println("The draw pile is empty.")
        } else {
            val drawnWordHashed = hashWithSeed(drawnWord) // Fix the reference here
            println("${player.name} drew '$drawnWordHashed'.")
            player.addWordToHand(drawnWord)
        }
    
        player.decreaseSkeletonKeys()
        println("${player.name} loses a skeleton key. Now has ${player.skeletonKeys} skeleton keys.")
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
        return wordList.count { it == word } >= 2
    }

    fun getRandomWordFromHand(): String {
        return wordList.shuffled().first()
    }

    fun increaseSkeletonKeys() {
        skeletonKeys++
    }

    fun decreaseSkeletonKeys() {
        skeletonKeys--
    }

    fun removeAllInstancesOfWord(word: String) {
        wordList.removeAll { it == word }
    }

}

/**
 * Main game execution starts here. Players are created, word lists are loaded, 
 * and the game is initialized and started.
 */
fun main() {

    val player1 = Player("Sean")
    val player2 = Player("Jeff")
    val player3 = Player("Darsh")

    // Load words for the game from a CSV file
    val words = File("wordlist.csv").readText().split(",").map { it.trim() }

    val game = Game(listOf(player1, player2, player3), words)

    // Load seed words and select a random one for hashing purposes
    val seedWords = File("seedlist.csv").readText().split(",").map { it.trim() }
    val randomSeedWord = seedWords.shuffled().first()
    game.playGame(randomSeedWord)

}