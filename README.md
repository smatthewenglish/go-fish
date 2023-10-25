# Skeleton Key Game

A word-based game where players exchange words from their hand with an objective to create pairs.
Words are secured with a hashing mechanism for added security.

## How to Play?

The game initializes by assigning a random set of words to each player.
On their turn, a player chooses a word and asks another player for the hashed version of a word.
If the other player has the word, they hand it over; otherwise, the player draws a word from the pile and loses a skeleton key.
The objective is to make pairs and earn skeleton keys.
Successfully making a pair earns a player a skeleton key.
A player wins if they collect 3 skeleton keys and loses if they run out of skeleton keys.

## How to Run?

```
kotlinc Game.kt -include-runtime -d Game.jar
java -jar Game.jar
```