package algorithms;

import map.Cell;
import map.Map;
import map.MapConstants;
import robot.Robot;
import robot.RobotConstants;
import robot.RobotConstants.DIRECTION;
import robot.RobotConstants.MOVEMENT;
import utils.CommMgr;
import java.util.LinkedList;
import java.util.Stack;

//Queue<String> qe= new LinkedList<String>();

/**
 * Exploration algorithm for the robot.
 *
 * @author Priyanshu Singh
 * @author Suyash Lakhotia
 */

public class ExplorationAlgo {
    private final Map exploredMap;
    private final Map realMap;
    private final Robot bot;
    private final int coverageLimit;
    private final int timeLimit;
    private int areaExplored;
    private long startTime;
    private long endTime;
    private int lastCalibrate;
    private boolean calibrationMode;
    private int x;
    private int y;
    private Stack<String> directionMoved = new Stack();
    private String a;
    
    private int movableSteps;

    public ExplorationAlgo(Map exploredMap, Map realMap, Robot bot, int coverageLimit, int timeLimit) {
        this.exploredMap = exploredMap;
        this.realMap = realMap;
        this.bot = bot;
        this.coverageLimit = coverageLimit;
        this.timeLimit = timeLimit;
    }

    /**
     * Main method that is called to start the exploration.
     */
    public void runExploration() {
        if (bot.getRealBot()) {
            System.out.println("Starting calibration...");
//            System.out.println("Starting calibration...");

            CommMgr.getCommMgr().recvMsg();
            if (bot.getRealBot()) {
                bot.move(MOVEMENT.LEFT, false);
                CommMgr.getCommMgr().recvMsg();
                bot.move(MOVEMENT.CALIBRATE, false);
                CommMgr.getCommMgr().recvMsg();
                bot.move(MOVEMENT.LEFT, false);
                CommMgr.getCommMgr().recvMsg();
                bot.move(MOVEMENT.CALIBRATE, false);
                CommMgr.getCommMgr().recvMsg();
                bot.move(MOVEMENT.RIGHT, false);
                CommMgr.getCommMgr().recvMsg();
                bot.move(MOVEMENT.CALIBRATE, false);
                CommMgr.getCommMgr().recvMsg();
                bot.move(MOVEMENT.RIGHT, false);
            }

            while (true) {
                System.out.println("Waiting for EX_START...");
                String msg = CommMgr.getCommMgr().recvMsg();
                String[] msgArr = msg.split(";");
                if (msgArr[0].equals(CommMgr.EX_START)) break;
            }
        }
        exploredMap.getCell(1,1).setIsWalked(true);
        
        System.out.println("Starting exploration...");
        System.out.println();

        startTime = System.currentTimeMillis();
        endTime = startTime + (timeLimit * 1000);
        
        //WORKING! Used to show checklist requirements: breaking when area explored / touched goal / exceed time
//        endTime = startTime + (11 * 1000);

        if (bot.getRealBot()) {
            CommMgr.getCommMgr().sendMsg(null, CommMgr.BOT_START);
        }
        senseAndRepaint();

        areaExplored = calculateAreaExplored();
        System.out.println("Explored Area: " + areaExplored);

        explorationLoop(bot.getRobotPosRow(), bot.getRobotPosCol());
    }

    /**
     * Loops through robot movements until one (or more) of the following conditions is met:
     * 1. Robot is back at (r, c)
     * 2. areaExplored > coverageLimit
     * 3. System.currentTimeMillis() > endTime
     */
    private void explorationLoop(int r, int c) {

        do {
            nextMove();

            areaExplored = calculateAreaExplored();
            System.out.println("Area explored: " + areaExplored);

            if (bot.getRobotPosRow() == r && bot.getRobotPosCol() == c) {
                if (areaExplored >= 100) {
                    break;
                }
            }

        //WORKING! Used to show checklist requirements: breaking when area explored / touched goal / exceed time
//        } while (areaExplored <= 222);
//        } while (!bot.getTouchedGoal(); 
//        } while (System.currentTimeMillis() <= endTime);
        } while ((!bot.getTouchedGoal() && System.currentTimeMillis() <= endTime) || (areaExplored < coverageLimit && System.currentTimeMillis() <= endTime));

        goHome();
    }

    
    private int movableSteps(String m) {
    	switch(m) {
    	case "N": 
    	}
    	
    	return movableSteps;
    }
    
    /**
     * Determines the next move for the robot and executes it accordingly.
     */
    private void nextMove() {
    	x = bot.getRobotPosRow();
    	y = bot.getRobotPosCol();
//    	System.out.println(x);
//    	System.out.println(y);
    	
        if  (southFree() && !exploredMap.getCell(x-1,y).getIsWalked()) {
        	
        	if (bot.getRobotCurDir() == DIRECTION.WEST) {
    			moveBot(MOVEMENT.LEFT);
    		}
        	
        	else while (bot.getRobotCurDir() != DIRECTION.SOUTH) {
        		moveBot(MOVEMENT.RIGHT);
        	}

        	moveBot(MOVEMENT.FORWARD);
        	directionMoved.push("S");
        	
        	exploredMap.getCell(x-1, y).setIsWalked(true);

        }
        
        else if (eastFree() && !exploredMap.getCell(x,y+1).getIsWalked()) {
        	if (bot.getRobotCurDir() == DIRECTION.SOUTH) {
   			 moveBot(MOVEMENT.LEFT);
   		 }
   		  
   		 else while (bot.getRobotCurDir() != DIRECTION.EAST) {
         		moveBot(MOVEMENT.RIGHT);
         	}
         	moveBot(MOVEMENT.FORWARD);
         	directionMoved.push("E");
         	exploredMap.getCell(x, y+1).setIsWalked(true);
         }
        
        else if (westFree() && !exploredMap.getCell(x,y-1).getIsWalked()) {
        	if (bot.getRobotCurDir() == DIRECTION.NORTH) {
        		moveBot(MOVEMENT.LEFT);
        	}
        	
        	else while (bot.getRobotCurDir() != DIRECTION.WEST) {
        		moveBot(MOVEMENT.RIGHT);
        	}

        	moveBot(MOVEMENT.FORWARD);
        	directionMoved.push("W");
        	exploredMap.getCell(x, y-1).setIsWalked(true);
        }
        
        else if (northFree() && !exploredMap.getCell(x+1,y).getIsWalked()) {
        	if (bot.getRobotCurDir() == DIRECTION.EAST) {
        		moveBot(MOVEMENT.LEFT);
        	}
        	else while (bot.getRobotCurDir() != DIRECTION.NORTH) {
        		moveBot(MOVEMENT.RIGHT);
        	}
        	
        	moveBot(MOVEMENT.FORWARD);
        	directionMoved.push("N");
        	exploredMap.getCell(x+1, y).setIsWalked(true);
        }
        
        else{
        	if (fastestPath()) {
        		
        	}
        
        	else 
        {
        	a = directionMoved.pop();
        	switch(a) {
        	case "N": while (bot.getRobotCurDir() != DIRECTION.SOUTH) {
        			moveBot(MOVEMENT.RIGHT);
        				}
        			moveBot(MOVEMENT.FORWARD);
        			break;
        			
        	case "S":  while (bot.getRobotCurDir() != DIRECTION.NORTH) {
        		moveBot(MOVEMENT.RIGHT);
        	}
        	moveBot(MOVEMENT.FORWARD);
        	break;
        	
        	case "E": while (bot.getRobotCurDir() != DIRECTION.WEST) {
        		moveBot(MOVEMENT.RIGHT);
        	}
        	moveBot(MOVEMENT.FORWARD);
        	break;
        	
        	case "W": while (bot.getRobotCurDir() != DIRECTION.EAST) {
        		moveBot(MOVEMENT.RIGHT);
        	}
        	moveBot(MOVEMENT.FORWARD);
        	break;
        	}
        }
        
    }

    
    
    private boolean fastestPath() {
    	//System.out.println(bot.getRobotPosRow());
    	//System.out.println(bot.getRobotPosCol());
    	FastestPathAlgo goToCell = new FastestPathAlgo (exploredMap,bot,realMap);
    	
    	
    	int z = 0; //z is to store the array counter
    	int num = 999; //storing the minimum cost
    	
    	//iterating through the cells of all the map to get the cells that have been explored but not walked and not obstacle
    	for (int i = 1; i < 15;i++) {
    		for (int j = 1; j < 15;j++) {
    			if (!(exploredMap.getCell(i, j).getIsWalked()) && (exploredMap.getCell(i, j).getIsExplored()) && !(exploredMap.getCell(i, j).getIsObstacle())) {  				
    				array[0][z] = i; //storing the row values 
    				array[1][z] = j; //storing the col values
    				z++;
    			}
    		}
    	}
    		//iterate thru the above array
    		for (int i = 0; i<z; i++) {
    			int x = (bot.getRobotPosRow() - array[0][i] + bot.getRobotPosCol() - array[1][i]); //finding the nearest cell by comparing row and col
    			int minCost = 999;
    			if ((x < minCost)) {
    				minCost = x;
    				num = i;
    			}
    		}	   
    		//if num == 999 means no cell in the array
    	if (num == 999) {
    		return false;
    	}
    	
    	goToCell.runFastestPath(array[0][num], array[1][num]);
    	return true;
    }
    /**
     * Returns true if the right side of the robot is free to move into.
     */
    private boolean lookRight() {
        switch (bot.getRobotCurDir()) {
            case NORTH:
                return eastFree();
            case EAST:
                return southFree();
            case SOUTH:
                return westFree();
            case WEST:
                return northFree();
        }
        return false;
    }

    /**
     * Returns true if the robot is free to move forward.
     */
    private boolean lookForward() {
        switch (bot.getRobotCurDir()) {
            case NORTH:
                return northFree();
            case EAST:
                return eastFree();
            case SOUTH:
                return southFree();
            case WEST:
                return westFree();
        }
        return false;
    }

    /**
     * * Returns true if the left side of the robot is free to move into.
     */
    private boolean lookLeft() {
        switch (bot.getRobotCurDir()) {
            case NORTH:
                return westFree();
            case EAST:
                return northFree();
            case SOUTH:
                return eastFree();
            case WEST:
                return southFree();
        }
        return false;
    }

    /**
     * Returns true if the robot can move to the north cell.
     */
    private boolean northFree() {
        int botRow = bot.getRobotPosRow();
        int botCol = bot.getRobotPosCol();
        return (isExploredNotObstacle(botRow + 1, botCol - 1) && isExploredAndFree(botRow + 1, botCol) && isExploredNotObstacle(botRow + 1, botCol + 1));
    }

    /**
     * Returns true if the robot can move to the east cell.
     */
    private boolean eastFree() {
        int botRow = bot.getRobotPosRow();
        int botCol = bot.getRobotPosCol();
        return (isExploredNotObstacle(botRow - 1, botCol + 1) && isExploredAndFree(botRow, botCol + 1) && isExploredNotObstacle(botRow + 1, botCol + 1));
    }

    /**
     * Returns true if the robot can move to the south cell.
     */
    private boolean southFree() {
        int botRow = bot.getRobotPosRow();
        int botCol = bot.getRobotPosCol();
        return (isExploredNotObstacle(botRow - 1, botCol - 1) && isExploredAndFree(botRow - 1, botCol) && isExploredNotObstacle(botRow - 1, botCol + 1));
    }

    /**
     * Returns true if the robot can move to the west cell.
     */
    private boolean westFree() {
        int botRow = bot.getRobotPosRow();
        int botCol = bot.getRobotPosCol();
        return (isExploredNotObstacle(botRow - 1, botCol - 1) && isExploredAndFree(botRow, botCol - 1) && isExploredNotObstacle(botRow + 1, botCol - 1));
    }

    /**
     * Returns the robot to START after exploration and points the bot northwards.
     */
    private void goHome() {
    	//If haven't touched goal, rush to goal then return to start
        if (!bot.getTouchedGoal() && coverageLimit == 300 && timeLimit == 3600) {
            FastestPathAlgo goToGoal = new FastestPathAlgo(exploredMap, bot, realMap);
            goToGoal.runFastestPath(RobotConstants.GOAL_ROW, RobotConstants.GOAL_COL);
        }

        //Return to start
        FastestPathAlgo returnToStart = new FastestPathAlgo(exploredMap, bot, realMap);
        returnToStart.runFastestPath(RobotConstants.START_ROW, RobotConstants.START_COL);

        System.out.println("Exploration complete!");
        areaExplored = calculateAreaExplored();
        System.out.printf("%.2f%% Coverage", (areaExplored / 300.0) * 100.0);
        System.out.println(", " + areaExplored + " Cells");
        System.out.println((System.currentTimeMillis() - startTime) / 1000 + " Seconds");

        if (bot.getRealBot()) {
            turnBotDirection(DIRECTION.WEST);
            moveBot(MOVEMENT.CALIBRATE);
            turnBotDirection(DIRECTION.SOUTH);
            moveBot(MOVEMENT.CALIBRATE);
            turnBotDirection(DIRECTION.WEST);
            moveBot(MOVEMENT.CALIBRATE);
        }
        turnBotDirection(DIRECTION.NORTH);
    }

    /**
     * Returns true for cells that are explored and not obstacles.
     */
    private boolean isExploredNotObstacle(int r, int c) {
        if (exploredMap.checkValidCoordinates(r, c)) {
            Cell tmp = exploredMap.getCell(r, c);
            return (tmp.getIsExplored() && !tmp.getIsObstacle());
        }
        return false;
    }

    /**
     * Returns true for cells that are explored, not virtual walls and not obstacles.
     */
    private boolean isExploredAndFree(int r, int c) {
        if (exploredMap.checkValidCoordinates(r, c)) {
            Cell b = exploredMap.getCell(r, c);
            return (b.getIsExplored() && !b.getIsVirtualWall() && !b.getIsObstacle());
        }
        return false;
    }

    /**
     * Returns the number of cells explored in the grid.
     */
    private int calculateAreaExplored() {
        int result = 0;
        for (int r = 0; r < MapConstants.MAP_ROWS; r++) {
            for (int c = 0; c < MapConstants.MAP_COLS; c++) {
                if (exploredMap.getCell(r, c).getIsExplored()) {
                    result++;
                }
            }
        }
        return result;
    }

    /**
     * Moves the bot, repaints the map and calls senseAndRepaint().
     */
    private void moveBot(MOVEMENT m) {
        bot.move(m);
        exploredMap.repaint();
        if (m != MOVEMENT.CALIBRATE) {
            senseAndRepaint();
        } else {
            CommMgr commMgr = CommMgr.getCommMgr();
            commMgr.recvMsg();
        }

        if (bot.getRealBot() && !calibrationMode) {
            calibrationMode = true;

            if (canCalibrateOnTheSpot(bot.getRobotCurDir())) {
                lastCalibrate = 0;
                moveBot(MOVEMENT.CALIBRATE);
            } else {
                lastCalibrate++;
                if (lastCalibrate >= 5) {
                    DIRECTION targetDir = getCalibrationDirection();
                    if (targetDir != null) {
                        lastCalibrate = 0;
                        calibrateBot(targetDir);
                    }
                }
            }

            calibrationMode = false;
        }
    }

    /**
     * Sets the bot's sensors, processes the sensor data and repaints the map.
     */
    private void senseAndRepaint() {
        bot.setSensors();
        bot.sense(exploredMap, realMap);
        exploredMap.repaint();
    }

    /**
     * Checks if the robot can calibrate at its current position given a direction.
     */
    private boolean canCalibrateOnTheSpot(DIRECTION botDir) {
        int row = bot.getRobotPosRow();
        int col = bot.getRobotPosCol();

        switch (botDir) {
            case NORTH:
                return exploredMap.getIsObstacleOrWall(row + 2, col - 1) && exploredMap.getIsObstacleOrWall(row + 2, col) && exploredMap.getIsObstacleOrWall(row + 2, col + 1);
            case EAST:
                return exploredMap.getIsObstacleOrWall(row + 1, col + 2) && exploredMap.getIsObstacleOrWall(row, col + 2) && exploredMap.getIsObstacleOrWall(row - 1, col + 2);
            case SOUTH:
                return exploredMap.getIsObstacleOrWall(row - 2, col - 1) && exploredMap.getIsObstacleOrWall(row - 2, col) && exploredMap.getIsObstacleOrWall(row - 2, col + 1);
            case WEST:
                return exploredMap.getIsObstacleOrWall(row + 1, col - 2) && exploredMap.getIsObstacleOrWall(row, col - 2) && exploredMap.getIsObstacleOrWall(row - 1, col - 2);
        }

        return false;
    }

    /**
     * Returns a possible direction for robot calibration or null, otherwise.
     */
    private DIRECTION getCalibrationDirection() {
        DIRECTION origDir = bot.getRobotCurDir();
        DIRECTION dirToCheck;

        dirToCheck = DIRECTION.getNext(origDir);                    // right turn
        if (canCalibrateOnTheSpot(dirToCheck)) return dirToCheck;

        dirToCheck = DIRECTION.getPrevious(origDir);                // left turn
        if (canCalibrateOnTheSpot(dirToCheck)) return dirToCheck;

        dirToCheck = DIRECTION.getPrevious(dirToCheck);             // u turn
        if (canCalibrateOnTheSpot(dirToCheck)) return dirToCheck;

        return null;
    }

    /**
     * Turns the bot in the needed direction and sends the CALIBRATE movement. Once calibrated, the bot is turned back
     * to its original direction.
     */
    private void calibrateBot(DIRECTION targetDir) {
        DIRECTION origDir = bot.getRobotCurDir();

        turnBotDirection(targetDir);
        moveBot(MOVEMENT.CALIBRATE);
        turnBotDirection(origDir);
    }

    /**
     * Turns the robot to the required direction.
     */
    private void turnBotDirection(DIRECTION targetDir) {
        int numOfTurn = Math.abs(bot.getRobotCurDir().ordinal() - targetDir.ordinal());
        if (numOfTurn > 2) numOfTurn = numOfTurn % 2;

        if (numOfTurn == 1) {
            if (DIRECTION.getNext(bot.getRobotCurDir()) == targetDir) {
                moveBot(MOVEMENT.RIGHT);
            } else {
                moveBot(MOVEMENT.LEFT);
            }
        } else if (numOfTurn == 2) {
            moveBot(MOVEMENT.RIGHT);
            moveBot(MOVEMENT.RIGHT);
        }
    }
}