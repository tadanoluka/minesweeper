package minesweeper

fun main() {
    val rows = 9
    val columns = 9

    val mineField = mutableListOf<MutableList<Char>>()

    val allPossibleLocations = mutableListOf<List<Int>>()
    for (row in 0 until rows) {
        for (column in 0 until columns) {
            allPossibleLocations.add(listOf(row, column))
        }
    }
    allPossibleLocations.shuffle()

    repeat(rows) {
        val row = mutableListOf<Char>()
        repeat(columns) {
            row.add('.')
        }
        mineField.add(row)
    }

    print("How many mines do you want on the field? ")
    val numberOfMines = readln().toInt()

    for (i in 0 until numberOfMines) {
        val row = allPossibleLocations[i][0]
        val column = allPossibleLocations[i][1]
        mineField[row][column] = 'X'
    }

    // Display grid
    for (i in 0 until rows) {
        println(mineField[i].joinToString(""))
    }
}
