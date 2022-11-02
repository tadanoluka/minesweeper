package minesweeper

fun main() {
    print("How many mines do you want on the field? ")
    val numberOfMines = readln().toInt()
    Minefield(numberOfMines)
}

class Minefield(private val numberOfMines: Int) {
    private val mineSymbol = 'X'
    private val safeSymbol = '.'

    private val fieldXSize = 9
    private val fieldYSize = 9

    private val mineField = MutableList(fieldYSize) { MutableList(fieldXSize) { '0' } }
    private val mineLocations = mutableListOf<List<Int>>()

    init {
        placeMines()
        placeDigits()
        displayMineField()
    }

    private fun placeMines() {
        // Setup list with location of the minefield in random order
        val allPossibleLocations = mutableListOf<List<Int>>()
        for (y in 0 until fieldYSize) {
            for (x in 0 until fieldXSize) {
                allPossibleLocations.add(listOf(y, x))
            }
        }
        allPossibleLocations.shuffle()

        // Placing the required number of mines using a list with a random minefield location
        for (i in 0 until numberOfMines) {
            val row = allPossibleLocations[i][0]
            val column = allPossibleLocations[i][1]
            mineField[row][column] = mineSymbol
            mineLocations.add(listOf(row, column))
        }
    }

    private fun placeDigits() {
        for(i in 0 until  numberOfMines) {
            val mineY = mineLocations[i][0]
            val mineX = mineLocations[i][1]
            for (y in -1..1) {
                for (x in -1 .. 1) {
                    val targetY = mineY + y
                    val targetX = mineX + x
                    if (targetY in 0 until fieldYSize && targetX in 0 until fieldXSize) {
                        if (mineField[targetY][targetX].isDigit()) {
                            mineField[targetY][targetX] = mineField[targetY][targetX] + 1
                        }
                    }
                }
            }
        }
    }

    private fun displayMineField() {
        for (i in 0 until fieldYSize) {
            println(mineField[i].joinToString("").replace('0', safeSymbol))
        }
    }
}