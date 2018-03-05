import Constants
# NOT NEEDED
class Cell(object):
    def __init__(self, row, col):
        self._isWalked = False
        self._row = row
        self._col = col
        self._isWall = False
        self._isExplored = False
        self._isObstacle = False

    def getRow(self):
        return self._row

    def getCol(self):
        return self._col

    def setIsObstacle(self, val):
        self._isObstacle = val

    def getIsObstacle(self):
        return self._isObstacle

    def setWall(self, val):
        if val:
            self._isWall = True
        elif self._row != 0 and self._row != Constants.MAP_ROWS - 1 and self._col != 0 and self._col != Constants.MAP_COLS - 1:
                self._isWall = False

    def getIsWall(self):
        return self._isWall

    def setIsWalked(self, val):
        self._isWalked = val

    def getIsWalked(self):
        return self._isWalked

    def setIsExplored(self, val):
        self._isExplored = val

    def getIsExplored(self):
        return self._isExplored