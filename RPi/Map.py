import Constants
import numpy

class Map(object):

    def __init__(self):
        self._grid = numpy.zeros([Constants.MAP_ROWS, Constants.MAP_COLS])

        # Int representations of a cell
        # 0 = Initialized and unexplored
        # 1 = Explored - walkable
        # 2 = Explored - not walkable due to obstacle
        # 3 = Explored - not walkable due to wall
        # 4 = Explored - walkable and is walked
        # 5 = Explored - not walkable and is walked
        # 6 = UNExplored - not walkable due to wall
        # -1 = Error

        row = 0
        print("Setting map walls")
        while row < Constants.MAP_ROWS - 1:
            col = 0
            while col < Constants.MAP_COLS - 1:
                if row == 0 or col == 0 or row == Constants.MAP_ROWS - 1 or col == Constants.MAP_COLS - 1:
                    self._grid[row][col] = 6
                    col += 1
                    row += 1
        print("Map walls set")

    # Returns true if row and col values in start zone
    def inStartZone(self, row, col):
        return row >= 0 and row <= 2 and col >= 0 and col <= 2

    # Returns true if row and col values in goal zone
    def inGoalZone(self, row, col):
        return row <= Constants.GOAL_ROW + 1 and row >= Constants.GOAL_ROW - 1 and col <= Constants.GOAL_COL + 1 and col >= Constants.GOAL_COL - 1

    # eturns true if row and col values are valid
    def checkValidCoordinates(self, row, col):
        return row >= 0 and col >= 0 and row < Constants.MAP_ROWS and col < Constants.MAP_COLS

    # Returns true if a 3x3 Grid of specified center row and col has no obstacles and is walkable
    def checkIfWalkable(self, row, col):
        if self._grid[row][col] != 1 or self._grid[row + 1][col] != 1 or self._grid[row - 1][col] != 1 or \
            self._grid[row + 1][col + 1] != 1 or self._grid[row + 1][col - 1] != 1 or  \
            self._grid[row][col - 1] != 1 or self._grid[row - 1][col - 1] != 1 or \
            self._grid[row][col + 1] != 1 or self._grid[row - 1][col + 1] != 1:
            return False
        else:
            return True