package map;

import robot.Robot;
import robot.RobotConstants;

import javax.swing.*;

import algorithms.ExplorationAlgo;

import java.awt.*;

/**
 * Mapping of arena
 */

public class Map extends JPanel {
    private final Cell[][] grid;
    private final Robot bot;

    /**
     * Initializes Map with a grid of Cells
     */
    public Map(Robot bot) {
        this.bot = bot;

        grid = new Cell[MapConstants.MAP_ROWS][MapConstants.MAP_COLS];
        for (int row = 0; row < grid.length; row++) {
            for (int col = 0; col < grid[0].length; col++) {
                grid[row][col] = new Cell(row, col);

                // Set arena walls
                if (row == 0 || col == 0 || row == MapConstants.MAP_ROWS - 1 || col == MapConstants.MAP_COLS - 1) {
                    grid[row][col].setWall(true);
                }
            }
        }
    }

    /**
     * Returns specified cell
     */
    public Cell getCell(int row, int col) {
        return grid[row][col];
    }

    /**
     * Returns true if row and col values in start zone
     */
    private boolean inStartZone(int row, int col) {
        return row >= 0 && row <= 2 && col >= 0 && col <= 2;
    }

    /**
     * Returns true if row and col values in goal zone
     */
    private boolean inGoalZone(int row, int col) {
        return (row <= MapConstants.GOAL_ROW + 1 && row >= MapConstants.GOAL_ROW - 1 && col <= MapConstants.GOAL_COL + 1 && col >= MapConstants.GOAL_COL - 1);
    }

    /**
     * Returns true if row and col values are valid
     */
    public boolean checkValidCoordinates(int row, int col) {
        return row >= 0 && col >= 0 && row < MapConstants.MAP_ROWS && col < MapConstants.MAP_COLS;
    }

    /**
     * Returns true if a 3x3 Grid of specified center row and col has no obstacles and is walkable
     */
    public boolean checkIfWalkable(int row, int col) {
//    	for(int i = -1; i < 1; i++) {
//        	for(int j = -1; j < 1; j++) {
//                if(row + i>= 0 && col + j>= 0 && row < MapConstants.MAP_ROWS && col < MapConstants.MAP_COLS) {
//                	return false;
//                }
//        	}
//    	}
    	
    	boolean a = grid[row][col].getIsExplored() && grid[row+1][col].getIsExplored() && grid[row-1][col].getIsExplored() &&
    				grid[row+1][col+1].getIsExplored() && grid[row+1][col-1].getIsExplored() && grid [row][col-1].getIsExplored() &&
					grid[row-1][col-1].getIsExplored()  && grid [row][col+1].getIsExplored() && grid [row-1][col+1].getIsExplored();
    	
    	boolean b = !grid[row][col].getIsObstacle() && !grid[row+1][col].getIsObstacle() && !grid[row-1][col].getIsObstacle() &&
    				!grid[row+1][col+1].getIsObstacle() && !grid[row+1][col-1].getIsObstacle() && !grid [row][col-1].getIsObstacle() &&
					!grid[row-1][col-1].getIsObstacle()  && !grid [row][col+1].getIsObstacle() && !grid [row-1][col+1].getIsObstacle();
	    	
    	boolean c = (checkValidCoordinates(row, col + 2) && !grid[row][col + 2].getIsExplored()) || 
    				(checkValidCoordinates(row + 1, col + 2) && !grid[row + 1][col + 2].getIsExplored()) || 
					(checkValidCoordinates(row - 1, col + 2) && !grid[row - 1][col + 2].getIsExplored()) ||
					(checkValidCoordinates(row, col - 2) && !grid[row][col - 2].getIsExplored()) || 
					(checkValidCoordinates(row + 1, col - 2) && !grid[row + 1][col - 2].getIsExplored()) || 
					(checkValidCoordinates(row - 1, col - 2) && !grid [row - 1][col - 2].getIsExplored()) ||
					(checkValidCoordinates(row + 2, col) && !grid[row + 2][col].getIsExplored()) || 
					(checkValidCoordinates(row + 2, col + 1) && !grid [row + 2][col + 1].getIsExplored()) || 
					(checkValidCoordinates(row + 2, col - 1) && !grid [row + 2][col - 1].getIsExplored()) ||
					(checkValidCoordinates(row - 2, col) && !grid[row - 2][col].getIsExplored()) || 
					(checkValidCoordinates(row - 2, col + 1) && !grid [row - 2][col + 1].getIsExplored()) || 
					(checkValidCoordinates(row - 2, col - 1) && !grid [row - 2][col - 1].getIsExplored());
    	
//    	if (a) {
//    		return false;
//    	}
//    	if (b) {
//    		return false;
//    	}
    	if (a && b && c) {
    		return true;
    	}
    	return false;
    }
    
    /**
     * Returns true if a cell is obstacle
     */
    public boolean isObstacleCell(int row, int col) {
        return grid[row][col].getIsObstacle();
    }

    /**
     * Returns true if a cell is wall
     */
    public boolean isVirtualWallCell(int row, int col) {
        return grid[row][col].getIsWall();
    }

    /**
     * Sets all cells in the grid to an unexplored state except for the START & GOAL zone.
     */
    public void setAllUnexplored() {
        for (int row = 0; row < grid.length; row++) {
            for (int col = 0; col < grid[0].length; col++) {
                if (inStartZone(row, col)) {
//                    if (inStartZone(row, col) || inGoalZone(row, col)) {
                    grid[row][col].setIsExplored(true);
                } else {
                    grid[row][col].setIsExplored(false);
                }
            }
        }
    }

    /**
     * Sets all cells in grid to an explored state
     */
    public void setAllExplored() {
        for (int row = 0; row < grid.length; row++) {
            for (int col = 0; col < grid[0].length; col++) {
                grid[row][col].setIsExplored(true);
            }
        }
    }

    /**
     * Sets a cell as obstacle or wall or walkable
     */
    public void setObstacleCell(int row, int col, boolean obstacle) {
        if (obstacle && (inStartZone(row, col) || inGoalZone(row, col)))
            return;

        grid[row][col].setIsObstacle(obstacle);

        if (row >= 1) {
            grid[row - 1][col].setWall(obstacle);            // bottom cell

            if (col < MapConstants.MAP_COLS - 1) {
                grid[row - 1][col + 1].setWall(obstacle);    // bottom-right cell
            }

            if (col >= 1) {
                grid[row - 1][col - 1].setWall(obstacle);    // bottom-left cell
            }
        }

        if (row < MapConstants.MAP_ROWS - 1) {
            grid[row + 1][col].setWall(obstacle);            // top cell

            if (col < MapConstants.MAP_COLS - 1) {
                grid[row + 1][col + 1].setWall(obstacle);    // top-right cell
            }

            if (col >= 1) {
                grid[row + 1][col - 1].setWall(obstacle);    // top-left cell
            }
        }

        if (col >= 1) {
            grid[row][col - 1].setWall(obstacle);            // left cell
        }

        if (col < MapConstants.MAP_COLS - 1) {
            grid[row][col + 1].setWall(obstacle);            // right cell
        }
    }

    /**
     * Returns true if the specified cell out of bounds or an obstacle
     */
    public boolean getIsObstacleOrWall(int row, int col) {
        return !checkValidCoordinates(row, col) || getCell(row, col).getIsObstacle();
    }
    
    /**
     * Overrides JComponent's paintComponent() 
     * Creates a 2D array of DisplayCell to store current map state
     * Then paints cells and robot with their designated colors
     */
    public void paintComponent(Graphics g) {
        // Create 2D array of _DisplayCells for rendering
        DisplayCell[][] mapCells = new DisplayCell[MapConstants.MAP_ROWS][MapConstants.MAP_COLS];
        for (int mapRow = 0; mapRow < MapConstants.MAP_ROWS; mapRow++) {
            for (int mapCol = 0; mapCol < MapConstants.MAP_COLS; mapCol++) {
                mapCells[mapRow][mapCol] = new DisplayCell(mapCol * GFXConstants.CELL_SIZE, mapRow * GFXConstants.CELL_SIZE, GFXConstants.CELL_SIZE);
            }
        }

        // Paint cells
        for (int mapRow = 0; mapRow < MapConstants.MAP_ROWS; mapRow++) {
            for (int mapCol = 0; mapCol < MapConstants.MAP_COLS; mapCol++) {
                Color cellColor;

                if (inStartZone(mapRow, mapCol))
                    cellColor = GFXConstants.COLOR_START;
                else if (inGoalZone(mapRow, mapCol))
                    cellColor = GFXConstants.COLOR_GOAL;
                else {
                    if (!grid[mapRow][mapCol].getIsExplored())
                        cellColor = GFXConstants.COLOR_UNEXPLORED;
                    else if (grid[mapRow][mapCol].getIsObstacle())
                        cellColor = GFXConstants.COLOR_OBSTACLE;
                    else
                        cellColor = GFXConstants.COLOR_FREE;
                }

                g.setColor(cellColor);
                g.fillRect(mapCells[mapRow][mapCol].cellX, mapCells[mapRow][mapCol].cellY, mapCells[mapRow][mapCol].cellSize, mapCells[mapRow][mapCol].cellSize);

            }
        }

        // Paint the robot
        g.setColor(GFXConstants.COLOR_ROBOT);
        int r = bot.getRobotPosRow();
        int c = bot.getRobotPosCol();
        g.fillOval((c - 1) * GFXConstants.CELL_SIZE + GFXConstants.ROBOT_X_OFFSET, GFXConstants.MAP_H - (r * GFXConstants.CELL_SIZE + GFXConstants.ROBOT_Y_OFFSET), GFXConstants.ROBOT_WIDTH, GFXConstants.ROBOT_HEIGHT);

        // Paint robot's direction indicator
        g.setColor(GFXConstants.COLOR_ROBOT_DIR);
        RobotConstants.DIRECTION d = bot.getRobotCurDir();
        switch (d) {
            case NORTH:
                g.fillRect(c * GFXConstants.CELL_SIZE + 10, GFXConstants.MAP_H - r * GFXConstants.CELL_SIZE - 15, GFXConstants.ROBOT_DIR_W, GFXConstants.ROBOT_DIR_H);
                break;
            case EAST:
                g.fillRect(c * GFXConstants.CELL_SIZE + 35, GFXConstants.MAP_H - r * GFXConstants.CELL_SIZE + 10, GFXConstants.ROBOT_DIR_W, GFXConstants.ROBOT_DIR_H);
                break;
            case SOUTH:
                g.fillRect(c * GFXConstants.CELL_SIZE + 10, GFXConstants.MAP_H - r * GFXConstants.CELL_SIZE + 35, GFXConstants.ROBOT_DIR_W, GFXConstants.ROBOT_DIR_H);
                break;
            case WEST:
                g.fillRect(c * GFXConstants.CELL_SIZE - 15, GFXConstants.MAP_H - r * GFXConstants.CELL_SIZE + 10, GFXConstants.ROBOT_DIR_W, GFXConstants.ROBOT_DIR_H);
                break;
        }
    }

    private class DisplayCell {
        public final int cellX;
        public final int cellY;
        public final int cellSize;

        public DisplayCell(int borderX, int borderY, int borderSize) {
            this.cellX = borderX + GFXConstants.CELL_LINE_WEIGHT;
            this.cellY = GFXConstants.MAP_H - (borderY - GFXConstants.CELL_LINE_WEIGHT);
            this.cellSize = borderSize - (GFXConstants.CELL_LINE_WEIGHT * 2);
        }
    }
}