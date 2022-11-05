package minesweeper

const val MINE_CELL_SYMBOL = 'X'
const val EMPTY_CELL_SYMBOL = ' '
const val UNEXPLORED_CELL_SYMBOL = '.'
const val FLAG_CELL_SYMBOL = '*'

enum class CellType {
    MINE,
    EMPTY,
    DIGIT,
}

class Cell(location: List<Int>) {
    val y: Int
    val x: Int
    val neighbors = mutableListOf<Cell>()
    var cellType = CellType.EMPTY
        set(value) {
            when (value) {
                CellType.EMPTY -> {
                    field = value
                    displayableSymbol = EMPTY_CELL_SYMBOL
                }
                CellType.DIGIT -> {
                    if (field == CellType.MINE) {
                        return
                    }
                    if (numberValue != 0) {
                        field = value
                        displayableSymbol = numberValue.digitToChar()
                    } else {
                        field = CellType.EMPTY
                    }
                }
                CellType.MINE -> {
                    field = value
                    displayableSymbol = MINE_CELL_SYMBOL
                }
            }
        }

    var displayableSymbol = EMPTY_CELL_SYMBOL
    var numberValue = 0
    var explored = false
    var marked = false

    init {
        y = location[0]
        x = location[1]
    }

    fun getSymbol(): Char {
        return if (marked) {
            FLAG_CELL_SYMBOL
        } else if (explored) {
            displayableSymbol
        } else {
            UNEXPLORED_CELL_SYMBOL
        }
    }

    fun addNeighbor(cell: Cell){
        if (y != cell.y || x != cell.x){
            neighbors.add(cell)
        }
    }

}

class Minefield(private val sizeX: Int, private val sizeY: Int, private val numOfMines: Int) {
    val field = mutableListOf<MutableList<Cell>>()
    val availableLocations = mutableListOf<List<Int>>()
    val minesLocations = mutableListOf<List<Int>>()
    val marksLocations = mutableListOf<List<Int>>()

    init {
        createField()
    }

    fun setupMinefield(safeY: Int, safeX: Int) {
        tellCellsAboutNeighbors()
        placeMines(safeY, safeX)
        tellCellsAboutNumOfMinesAround()
    }

    private fun createField() {
        for (y in 0 until sizeY) {
            val row = mutableListOf<Cell>()
            for (x in 0 until sizeX) {
                row.add(Cell(listOf(y, x)))
                availableLocations.add(listOf(y, x))
            }
            field.add(row)
        }
    }

    private fun placeMines(safeY: Int, safeX: Int) {
        // Setup list with location of the minefield in random order without area around first player move
        val allPossibleLocations = availableLocations.toMutableList()
        val safeCell = getCellByYX(listOf(safeY, safeX))
        allPossibleLocations.remove(listOf(safeCell.y, safeCell.x))
        for (neighbor in safeCell.neighbors) {
            allPossibleLocations.remove(listOf(neighbor.y, neighbor.x))
        }
        allPossibleLocations.shuffle()

        // Placing the required number of mines using a list with a random minefield location
        repeat(numOfMines) {
            val y = allPossibleLocations[0][0]
            val x = allPossibleLocations[0][1]
            field[y][x].cellType = CellType.MINE
            minesLocations.add(listOf(y, x))
            allPossibleLocations.removeFirst()
            availableLocations.remove(listOf(y, x))
        }
        minesLocations.sortWith(compareBy({it[0]}, {it[1]}))

    }

    private fun tellCellsAboutNeighbors() {
        for (row in field) {
            for (cell in row) {
                for (y in -1..1) {
                    for (x in -1..1) {
                        val neighborY = cell.y + y
                        val neighborX = cell.x + x
                        if (neighborY in 0 until sizeY && neighborX in 0 until sizeX) {
                            cell.addNeighbor(field[neighborY][neighborX])
                        }
                    }
                }
            }
        }
    }

    private fun tellCellsAboutNumOfMinesAround() {
        for (row in field) {
            for (cell in row) {
                var numOfMinesAroundCell = 0
                for (neighbor in cell.neighbors) {
                    if (neighbor.cellType == CellType.MINE) {
                        numOfMinesAroundCell += 1
                    }
                }
                if (numOfMinesAroundCell != 0) {
                    cell.numberValue = numOfMinesAroundCell
                    cell.cellType = CellType.DIGIT
                }
            }
        }
    }

    private fun getCellByYX(listYX: List<Int>): Cell {
        val cellY = listYX[0]
        val cellX = listYX[1]
        return field[cellY][cellX]
    }

    fun getTypeOfCellByYX(listYX: List<Int>): CellType {
        val cell = getCellByYX(listYX)
        return cell.cellType
    }

    fun exploreCellsArea(startY: Int, startX: Int) {
        val startCell = field[startY][startX]
        var cellsToExplore = mutableListOf(startCell)
        val areaAroundBlankArea = mutableListOf<Cell>()
        while (true) {
            val tempList = cellsToExplore.toMutableList()
            for (cell in cellsToExplore) {
                for (neighbor in cell.neighbors) {
                    if (neighbor !in tempList) {
                        if (neighbor.displayableSymbol == EMPTY_CELL_SYMBOL) {
                            tempList.add(neighbor)
                        } else {
                            areaAroundBlankArea.add(neighbor)
                        }
                    }
                }
            }
            if (cellsToExplore == tempList) {
                break
            } else {
                cellsToExplore = tempList.toMutableList()
            }
        }
        for (cell in cellsToExplore) {
            availableLocations.remove(listOf(cell.y, cell.x))
            cell.explored = true
            checkIfCellMarked(cell)
        }
        for (cell in areaAroundBlankArea) {
            availableLocations.remove(listOf(cell.y, cell.x))
            cell.explored = true
            checkIfCellMarked(cell)
        }
    }

    private fun checkIfCellMarked(cell: Cell) {
        if (marksLocations.isNotEmpty()) {
            for (mark in marksLocations) {
                if (mark[0] == cell.y && mark[1] == cell.x) {
                    marksLocations.remove(listOf(cell.y, cell.x))
                    getCellByYX(listOf(mark[0], mark[1])).marked = false
                    break
                }
            }
        }
    }

    fun exploreCell(y: Int, x: Int) {
        getCellByYX(listOf(y, x)).explored = true
    }

    fun printCellDebug(y: Int, x: Int) {
        val cell = getCellByYX(listOf(y, x))
        println("Cell X:${cell.x} Y:${cell.y}")
        println("Number: ${cell.numberValue}, Explored: ${cell.explored}, Marked: ${cell.marked}, Cell type: ${cell.cellType}")
        println("Neighbors: ")
        var counter = 1
        for (nei in cell.neighbors) {
            println("Cell$counter X: ${nei.x}, Y: ${nei.y}")
            println("Number: ${nei.numberValue}, Explored: ${nei.explored}, Marked: ${nei.marked}, Cell type: ${nei.cellType}")
            counter += 1
        }
    }

    fun markCell(y: Int, x: Int) {
        val cell = field[y][x]
        if (cell.marked) {
            marksLocations.remove(listOf(y, x))
            availableLocations.add(listOf(y, x))
        } else {
            marksLocations.add(listOf(y, x))
            availableLocations.remove(listOf(y, x))
        }
        cell.marked = !cell.marked
        if (marksLocations.isNotEmpty()) {
            marksLocations.sortWith(compareBy({it[0]}, {it[1]}))
        }
        if (availableLocations.isNotEmpty()) {
            availableLocations.sortWith(compareBy({it[0]}, {it[1]}))
        }
    }
}

class Minesweeper {
    private val fieldXSize: Int = 9
    private val fieldYSize: Int = 9

    private val numOfMines: Int
    private val minefield: Minefield

    private var gameIsEnd = false
    private var win = false

    init {
        numOfMines = askNumOfMines()
        minefield = Minefield(fieldYSize, fieldXSize, numOfMines)
        displayMineField()
        gameloop()
    }

    private fun askNumOfMines(): Int {
        print("How many mines do you want on the field? ")
        return readln().toInt()
    }

    private fun displayMineField() {
        var firstString = ""
        var secondString = ""
        for (i in 1..fieldXSize) {
            firstString += "$i"
            secondString += "—"
        }
        println()
        println(" │$firstString│")
        println("—│$secondString│")
        for (i in 0 until fieldYSize) {
            print("${i+1}│")
            for (cell in minefield.field[i])
            print(cell.getSymbol())
            println("│")
        }
        println("—│$secondString│")
    }

    private fun gameloop() {
        var isFieldReady = false
        while (!gameIsEnd) {
            print("Set/unset mines marks or claim a cell as free: ")
            val inputString = readln().split(" ")
            val x = inputString[0].toInt() - 1
            val y = inputString[1].toInt() - 1
            if (!isFieldReady) {
                minefield.setupMinefield(y, x)
                isFieldReady = true
            }
            when (inputString[2]) {
                "free" -> playerMakesAMove(y, x)
                "mine" -> minefield.markCell(y, x)
                "debug" -> minefield.printCellDebug(y, x)
            }
            checkWin()
            displayMineField()
        }
        if (win) {
            println("Congratulations! You found all the mines!")
        } else {
            println("You stepped on a mine and failed!")
        }
    }

    private fun playerMakesAMove(y: Int, x: Int) {
        when (minefield.getTypeOfCellByYX(listOf(y, x))) {
            CellType.EMPTY-> minefield.exploreCellsArea(y, x)
            CellType.DIGIT -> minefield.exploreCell(y, x)
            CellType.MINE -> playerBlowUp() // kaboom
        }

    }

    private fun playerBlowUp() {
        gameIsEnd = true
        win = false
    }

    private fun checkWin() {
        if (minefield.minesLocations == minefield.marksLocations) {
            gameIsEnd = true
            win = true
        } else if (minefield.availableLocations.isEmpty()) {
            gameIsEnd = true
            win = true
        }
    }
}

fun main() {
    Minesweeper()
}