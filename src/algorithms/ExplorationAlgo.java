package algorithms;

import map.Cell;
import map.Map;
import map.MapConstants;
import robot.Robot;
import robot.RobotConstants;
import robot.RobotConstants.DIRECTION;
import robot.RobotConstants.MOVEMENT;
import simulator.Simulator;
import utils.Communicator;
import utils.MapDescriptor;

import java.util.Stack;

/**
 * Exploration algorithm for the robot
 */

public class ExplorationAlgo {
    public final Map exploredMap;
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
    private Stack<String> directionMoved = new Stack<String>();
    private int array[][] = new int [99][99];
    
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
//        	FOR CALIBRATION
//            System.out.println("Starting calibration...");

//            Simulator.communicator.recvMsg();
//            if (bot.getRealBot()) {
//                bot.move(MOVEMENT.LEFT, false);
//                Communicator.getCommMgr().recvMsg();
//                bot.move(MOVEMENT.CALIBRATE, false);
//                Communicator.getCommMgr().recvMsg();
//                bot.move(MOVEMENT.LEFT, false);
//                Communicator.getCommMgr().recvMsg();
//                bot.move(MOVEMENT.CALIBRATE, false);
//                Communicator.getCommMgr().recvMsg();
//                bot.move(MOVEMENT.RIGHT, false);
//                Communicator.getCommMgr().recvMsg();
//                bot.move(MOVEMENT.CALIBRATE, false);
//                Communicator.getCommMgr().recvMsg();
//                bot.move(MOVEMENT.RIGHT, false);
//            }

//            while (true) {
//                System.out.println("Waiting for Arduino to send data...");
//                String msg = Simulator.communicator.recvMsg();
//                String[] msgArr = msg.split(";");
//                if (msgArr[0].equals(Communicator.EX_START)) break;
//            }
        }
        exploredMap.getCell(1,1).setIsWalked(true);

        startTime = System.currentTimeMillis();
        endTime = startTime + (timeLimit * 1000);

        explorationLoop(bot.getRobotPosRow(), bot.getRobotPosCol());
    }

    /**
     * Loops through exploration algorithm until:
     * - bot reaches goal and returns back to start zone, 
     * OR 
     * - areaExplored > coverageLimit || System.currentTimeMillis() > endTime
     */
    private void explorationLoop(int r, int c) {

        if (bot.getRealBot()) {
        	Simulator.communicator.sendMsg("@", null);
        	Simulator.communicator.sendMsg("E", null);
        }            
    	senseAndRepaint();

        do {
            System.out.println("");
            System.out.println("Entered explore do-while");
            
            if (bot.getTouchedGoal() && areaExplored >= coverageLimit) {
                System.out.println("Touched goal and full coverage achieved.");
            	break;
            }

            nextMove();

            areaExplored = calculateAreaExplored();
//            System.out.println("Area explored: " + areaExplored);
        } while ((areaExplored <= coverageLimit && System.currentTimeMillis() <= endTime));

        goHome();
    }
    
    /**
     * Determines the next move for the robot and executes it accordingly.
     */
    private void nextMove() {
    	x = bot.getRobotPosRow();
    	y = bot.getRobotPosCol();
    	System.out.println("Bot current pos: " + x + ", " + y);
    	   	
	    if(southFree() && !exploredMap.getCell(x-1,y).getIsWalked()) {
	    		
	    	if(bot.getRobotCurDir() == DIRECTION.WEST) {
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
	    	//Map2 eastfree returns true as it never detect obstacle at top right side. 
	    	//SOLVE BY PUTTING 1 MORE SENSOR, 
	    	//OR REMOVE BACKWARDS, 
	    	//OR IF DIRECTION NOT ALL 3 CELLS EXPLORED, TURN TO FACE THERE TO EXPLORE BEFORE. xFree() need check
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
	    	fastestPath();
	    	senseAndRepaint();
    	}
    }
    
    private boolean fastestPath() {
		System.out.println("Doing fastest path for Exploration");
    	int currentcellrow = bot.getRobotPosRow();
    	int currentcellcol = bot.getRobotPosCol();
    	exploredMap.getCell(currentcellrow,currentcellcol).setIsWalked(true);
    	FastestPathAlgo goToCell = new FastestPathAlgo(exploredMap,bot, realMap, true);
    	
    	int z = 0; //z is to store the array counter
    	int num = 999; //storing the minimum cost
    	
    	for (int i = 18; i > 0;i--) {
    		for (int j = 13; j > 0;j--) {
   			if (!(exploredMap.getCell(i, j).getIsWalked()) && (exploredMap.getCell(i, j).getIsExplored()) && !(exploredMap.getCell(i, j).getIsObstacle())) {  				
    				if (exploredMap.checkIfWalkable(i,j)) {
    				array[0][z] = i; //storing the row values 
    				array[1][z] = j; //storing the col values
    				z++;
    				}
    			}	
    		}
    	}
    	int minCost = 999;
    		//iterate thru the above array
		for (int i = 0; i<z; i++) {
			int x = (Math.abs(bot.getRobotPosRow() - array[0][i]) + Math.abs(bot.getRobotPosCol() - array[1][i])); //finding the nearest cell by comparing row and col
//    			int x = (Math.abs(MapConstants.GOAL_ROW - array[0][i]) + Math.abs(MapConstants.GOAL_COL - array[1][i])); //finding the nearest cell to goal by comparing row and col

			if ((x < minCost)) {
				minCost = x;
//    				System.out.println(minCost);
				num = i;
			}
		}	   

		int x = array[0][num];
		int y = array[1][num];

		System.out.println("Executing fastest path from Exploration");
    	goToCell.findFastestPath(x, y);
    	senseAndRepaint();
    	return false;
    }

    /**
     * Returns true if the robot can move to the north cell.
     */
    private boolean northFree() {
        int botRow = bot.getRobotPosRow();
        int botCol = bot.getRobotPosCol();
        if(isNotExploredandValid(botRow + 2, botCol - 1) || isNotExploredandValid(botRow + 2, botCol) || isNotExploredandValid(botRow + 2, botCol + 1)) {
        	while (bot.getRobotCurDir() != DIRECTION.NORTH) {
        		moveBot(MOVEMENT.RIGHT);
//        		senseAndRepaint();
        	}
        }
        return (isExploredNotObstacle(botRow + 1, botCol - 1) && isExploredAndFree(botRow + 1, botCol) && isExploredNotObstacle(botRow + 1, botCol + 1));
    }
    /**
     * Returns true if the robot can move to the east cell.
     */
    private boolean eastFree() {
        int botRow = bot.getRobotPosRow();
        int botCol = bot.getRobotPosCol();
        if(isNotExploredandValid(botRow - 1, botCol + 2) || isNotExploredandValid(botRow, botCol + 2) || isNotExploredandValid(botRow + 1, botCol + 2)) {
        	while (bot.getRobotCurDir() != DIRECTION.EAST) {
        		moveBot(MOVEMENT.RIGHT);
//        		senseAndRepaint();
        	}
        }
        return (isExploredNotObstacle(botRow - 1, botCol + 1) && isExploredAndFree(botRow, botCol + 1) && isExploredNotObstacle(botRow + 1, botCol + 1));
    }
   
    /**
     * Returns true if the robot can move to the south cell.
     */
    private boolean southFree() {
        int botRow = bot.getRobotPosRow();
        int botCol = bot.getRobotPosCol();
        if(isNotExploredandValid(botRow - 2, botCol - 1) || isNotExploredandValid(botRow - 2, botCol) || isNotExploredandValid(botRow - 2, botCol + 1)) {
        	while (bot.getRobotCurDir() != DIRECTION.SOUTH) {
        		moveBot(MOVEMENT.RIGHT);
//        		senseAndRepaint();
        	}
        }
        return (isExploredNotObstacle(botRow - 1, botCol - 1) && isExploredAndFree(botRow - 1, botCol) && isExploredNotObstacle(botRow - 1, botCol + 1));
    }
    
    /**
     * Returns true if the robot can move to the west cell.
     */
    private boolean westFree() {
        int botRow = bot.getRobotPosRow();
        int botCol = bot.getRobotPosCol();
        if(isNotExploredandValid(botRow - 1, botCol - 2) || isNotExploredandValid(botRow, botCol - 2) || isNotExploredandValid(botRow + 1, botCol - 2)) {
        	while (bot.getRobotCurDir() != DIRECTION.WEST) {
        		moveBot(MOVEMENT.RIGHT);
//        		senseAndRepaint();
        	}
        }
        return (isExploredNotObstacle(botRow - 1, botCol - 1) && isExploredAndFree(botRow, botCol - 1) && isExploredNotObstacle(botRow + 1, botCol - 1));
    }
    
    /**
     * Returns the robot to START after exploration and points the bot northwards.
     */
    private void goHome() {
    	
		System.out.println("Going home");
		
        if (!bot.getTouchedGoal() && coverageLimit == 300 && timeLimit == 3600) {
            FastestPathAlgo goToGoal = new FastestPathAlgo(exploredMap, bot, realMap, true);
            goToGoal.findFastestPath(RobotConstants.GOAL_ROW, RobotConstants.GOAL_COL);
        }

        FastestPathAlgo returnToStart = new FastestPathAlgo(exploredMap, bot, realMap, true);
        returnToStart.findFastestPath(RobotConstants.START_ROW, RobotConstants.START_COL);

        System.out.println("\nExploration complete!");
        String[] mapStrings = MapDescriptor.generateMapDescriptor(exploredMap);
        System.out.println("Part 1 MDF: " + mapStrings[0]);
        System.out.println("Part 2 MDF: " + mapStrings[1]);
        areaExplored = calculateAreaExplored();
        System.out.printf("%.2f%% Coverage", (areaExplored / 300.0) * 100.0);
        System.out.print(", " + areaExplored + " Cells, ");
        System.out.println((System.currentTimeMillis() - startTime) + " miliseconds");

        System.out.println("\nResetting bot direction");
//        if (bot.getRealBot()) {
//            turnBotDirection(DIRECTION.WEST);
//            moveBot(MOVEMENT.CALIBRATE);
//            turnBotDirection(DIRECTION.SOUTH);
//            moveBot(MOVEMENT.CALIBRATE);
//            turnBotDirection(DIRECTION.WEST);
//            moveBot(MOVEMENT.CALIBRATE);
//        }
        turnBotDirection(DIRECTION.NORTH);
    }

    /**
     * Returns true for cells that are explored and not obstacles.
     */
    private boolean isNotExploredandValid(int r, int c) {
        if (exploredMap.checkValidCoordinates(r, c)) {
            Cell tmp = exploredMap.getCell(r, c);
            return !tmp.getIsExplored();
        }
        return false;
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
            return (b.getIsExplored() && !b.getIsWall() && !b.getIsObstacle());
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
		System.out.println("moveBot(): " + MOVEMENT.print(m));
        
		bot.move(m);
        String[] mapStrings = MapDescriptor.generateMapDescriptor(exploredMap);
        String output = "@" + MOVEMENT.print(m) + "-" + bot.getRobotPosCol() + "-"
        		+ bot.getRobotPosRow() + "-" + RobotConstants.DIRECTION.print(bot.getRobotCurDir()) + "-" 
        		+ mapStrings[0] + "-" + mapStrings[1] + "-" ;

    	if(bot.getRealBot()) {
	    	Simulator.communicator.sendMsg(output, null);
    	}
//        if (!bot.getRealBot()) {
//            senseAndRepaint();
//        } else {
//            Simulator.communicator.recvMsg();
//        }

//        if (bot.getRealBot() && !calibrationMode) {
//            calibrationMode = true;
//
//            if (canCalibrateOnTheSpot(bot.getRobotCurDir())) {
//                lastCalibrate = 0;
//                moveBot(MOVEMENT.CALIBRATE);
//            } else {
//                lastCalibrate++;
//                if (lastCalibrate >= 5) {
//                    DIRECTION targetDir = getCalibrationDirection();
//                    if (targetDir != null) {
//                        lastCalibrate = 0;
//                        calibrateBot(targetDir);
//                    }
//                }
//            }
//
//            calibrationMode = false;
//        }
//		System.out.println("sent mdf");
    	senseAndRepaint();
    	
//        String[] mapStrings = MapDescriptor.generateMapDescriptor(explorationMap);
//        Simulator.communicator.sendMsg(mapStrings[0] + " " + mapStrings[1], Communicator.MAP_STRINGS);
    }

    /**
     * Sets the bot's sensors, processes the sensor data and repaints the map.
     */
    public void senseAndRepaint() {
        bot.setSensors();
        bot.sense(exploredMap, realMap, bot);
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
    	System.out.println("turnBotDirection(): " + targetDir);
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