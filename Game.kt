/**
 * 
 */
class Player() {
    // Each round starts by providing the players with a seed word. 
    lateinit var seedWord: String

    // Start with two, if you get the three you win, if you get to zero you lose.
    var skelletonKeys: Int = 2


    fun startRound(seedWord: String) {
        this.seedWord = seedWord
    }

    fun guessWord(word: String): Boolean {
        if (word == seedWord) {
            return true
        } else {
            return false
        }
    }

}

class Game(val players: List<Player>) {
    // Game's properties, e.g., status, level, etc.
    var status: String = "Not Started"
    var level: Int = 1

    // Start the game
    fun start() {
        status = "In Progress"
        println("Game started!")
    }

    // End the game
    fun end() {
        status = "Finished"
        println("Game ended!")
    }

    // Other methods related to the game...
}

fun main() {
    val player1 = Player("Alice")
    val player2 = Player("Bob")
    
    val game = Game(listOf(player1, player2))

    game.start()

    player1.move()
    player2.jump()

    game.end()
}