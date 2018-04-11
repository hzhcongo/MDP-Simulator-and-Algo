package map;

/**
 * Represents each grid in Map
 * @author Heng Ze Hao
 */
public class Cell {
    private final int row;
    private final int col;
    private boolean isObstacle;
    private boolean isWall;
    private boolean isExplored;
    private boolean isWalked = false;

    public Cell(int row, int col) {
        this.row = row;
        this.col = col;
    }

    public int getRow() {
        return this.row;
    }

    public int getCol() {
        return this.col;
    }

    public void setIsObstacle(boolean val) {
        this.isObstacle = val;
    }

    public boolean getIsObstacle() {
        return this.isObstacle;
    }

    public void setWall(boolean val) {
        if (val) {
            this.isWall = true;
        } else {
            if (row != 0 && row != MapConstants.MAP_ROWS - 1 && col != 0 && col != MapConstants.MAP_COLS - 1) {
                this.isWall = false;
            }
        }
    }

    public boolean getIsWall() {
        return this.isWall;
    }
    
    public void setIsWalked(boolean val) {
    	this.isWalked = true;
    }
    
    public boolean getIsWalked() {
    	return this.isWalked;
    }
    
    public void setIsExplored(boolean val) {
        this.isExplored = val;
    }

    public boolean getIsExplored() {
        return this.isExplored;
    }
}