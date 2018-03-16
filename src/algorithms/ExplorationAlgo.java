package algorithms;

import map.Cell;
import map.Map;
import map.MapConstants;
import robot.Robot;
import robot.RobotConstants;
import robot.RobotConstants.DIRECTION;
import robot.RobotConstants.MOVEMENT;
import simulator.Simulator;
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
//    private int lastCalibrate;
//    private boolean calibrationMode;
    private int x;
    private int y;
    private Stack<String> directionMoved = new Stack<String>();
    
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
//        if (bot.getRealBot()) {
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
//        }
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
     * 
     * FastestPath called to goal when all cells are explored but have not reached goal
     */
    private void explorationLoop(int r, int c) {

        if (bot.getRealBot()) {
        	//@E works as of 14/3/2018 2.32pm
        	Simulator.communicator.sendMsg("@E", null);
//        	Simulator.communicator.sendMsg("E", null);
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
            if(areaExplored == 300 && !bot.getTouchedGoal()) {
            	FastestPathAlgo goToCell = new FastestPathAlgo(exploredMap,bot, realMap, true);
            	goToCell.findFastestPath(MapConstants.GOAL_ROW, MapConstants.GOAL_COL, true);
            }
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
    	
    	//Find fastestpath to goal if bot is near goal
    	//Should decrease limit when goal is surrounded by walls, increase limit if not (4/5 optimal)
    	if(!bot.getTouchedGoal() && Math.abs(bot.getRobotPosRow() - MapConstants.GOAL_ROW) + Math.abs(bot.getRobotPosCol() - MapConstants.GOAL_COL) < 4) {
    		System.out.println("Near goal (<4)");
    		FastestPathAlgo goToCell = new FastestPathAlgo(exploredMap,bot, realMap, true);
        	goToCell.findFastestPath(MapConstants.GOAL_ROW, MapConstants.GOAL_COL, true);
    	}
    	
    	if(southUnexplored() && !exploredMap.getCell(x-1,y).getIsWalked()) {
	    		
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
	    else if (westUnexplored() && !exploredMap.getCell(x,y-1).getIsWalked()) {
        	
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
	    else if (eastUnexplored() && !exploredMap.getCell(x,y+1).getIsWalked()) {
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
	    else if (northUnexplored() && !exploredMap.getCell(x+1,y).getIsWalked()) {
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
	    	if(!fastestPath()) {
	    		System.out.println("Re-sensing surroundings");
	    		moveBot(MOVEMENT.RIGHT);
	    		moveBot(MOVEMENT.RIGHT);
	    		moveBot(MOVEMENT.RIGHT);
	    		moveBot(MOVEMENT.RIGHT);
	    	}
//	    	senseAndRepaint();
    	}
    }
    
    private boolean fastestPath() {
		System.out.println("Doing fastest path for Exploration");
    	int currentcellrow = bot.getRobotPosRow();
    	int currentcellcol = bot.getRobotPosCol();
    	exploredMap.getCell(currentcellrow,currentcellcol).setIsWalked(true);
    	FastestPathAlgo goToCell = new FastestPathAlgo(exploredMap,bot, realMap, true);
    	
    	int z = 0; //Array counter
        int array[][] = new int [2][50];
    	int num = 999; //Min cost
    	for (int i = 1; i < 19;i++) {
    		for (int j = 1; j < 14;j++) {
   			if (!exploredMap.getCell(i, j).getIsWalked() && exploredMap.getCell(i, j).getIsExplored()) {  				
    				if (exploredMap.checkIfWalkable(i,j)) {
    					System.out.println(i + " " + j);
	    				array[0][z] = i; //Row values 
	    				array[1][z] = j; //Col values
	    				z++;
    				}
    			}	
    		}
    	}
//    	for (int i = 18; i > 0;i--) {
//    		for (int j = 13; j > 0;j--) {
//   			if (!exploredMap.getCell(i, j).getIsWalked() && exploredMap.getCell(i, j).getIsExplored()) {  				
//    				if (exploredMap.checkIfWalkable(i,j)) { //Check if 3x3 free
//	    				array[0][z] = i; //storing the row values 
//	    				array[1][z] = j; //storing the col values
//	    				z++;
//    				}
//    			}	
//    		}
//    	}
    	int minCost = 999;
		//get minCost for each cell in array, and compare with current minCost
		for (int i = 0; i<z; i++) {
			int x = (Math.abs(bot.getRobotPosRow() - array[0][i]) + Math.abs(bot.getRobotPosCol() - array[1][i])); //finding the nearest cell by comparing row and col
//    			int x = (Math.abs(MapConstants.GOAL_ROW - array[0][i]) + Math.abs(MapConstants.GOAL_COL - array[1][i])); //finding the nearest cell to goal by comparing row and col

			if ((x < minCost)) {
				minCost = x;
				num = i;
			}
		}	   
		
		//The only time there are no valid cells is when map all explored
		//Thus need catch infinite loop (Appears on week10 map), and hard-execute fastestPath to GOAL since all explored
		if(num == 999) {
			System.out.println("Can't find cell valid cells to go to.");
	    	return false;
		}
		else {
			int x = array[0][num];
			int y = array[1][num];
	
			System.out.println("Executing fastest path from Exploration");
			return goToCell.findFastestPath(x, y, true);
		}
    }

    /**
     * Returns true if north has unexplored cells and bot can move to north
     */
    private boolean northUnexplored() {
    	int botRow = bot.getRobotPosRow();
        int botCol = bot.getRobotPosCol();
	    int flag = 0;
	    
        if(isNotExploredandValid(botRow + 2, botCol - 1) || isNotExploredandValid(botRow + 2, botCol) || isNotExploredandValid(botRow + 2, botCol + 1)) {
        	if(bot.getRobotCurDir() == DIRECTION.EAST) moveBot(MOVEMENT.LEFT);
        	else
        		while (bot.getRobotCurDir() != DIRECTION.NORTH) {
        			moveBot(MOVEMENT.RIGHT);
        		}
        }
        for(int i = botRow; i <= 19; i++) {
	    	if(!exploredMap.getCell(i, botCol).getIsExplored() || !exploredMap.getCell(i, botCol + 1).getIsExplored() || !exploredMap.getCell(i, botCol - 1).getIsExplored()) {
	    		flag = 1;
	    		break;
	    	}
	    }
	    if(flag == 1) return (isExploredNotObstacle(botRow + 1, botCol - 1) && isExploredAndFree(botRow + 1, botCol) && isExploredNotObstacle(botRow + 1, botCol + 1));
	    else return false;
//        int botRow = bot.getRobotPosRow();
//        int botCol = bot.getRobotPosCol();
//        if(isNotExploredandValid(botRow + 2, botCol - 1) || isNotExploredandValid(botRow + 2, botCol) || isNotExploredandValid(botRow + 2, botCol + 1)) {
//        	if(bot.getRobotCurDir() == DIRECTION.EAST) moveBot(MOVEMENT.LEFT);
//        	else
//        		while (bot.getRobotCurDir() != DIRECTION.NORTH) {
//        			moveBot(MOVEMENT.RIGHT);
//        		}
//        }
//        return (isExploredNotObstacle(botRow + 1, botCol - 1) && isExploredAndFree(botRow + 1, botCol) && isExploredNotObstacle(botRow + 1, botCol + 1));
    }
    
    /**
     * Returns true if east has unexplored cells and bot can move to east
     */
    private boolean eastUnexplored() {
    	int botRow = bot.getRobotPosRow();
        int botCol = bot.getRobotPosCol();
	    int flag = 0;
	    
        if(isNotExploredandValid(botRow - 1, botCol + 2) || isNotExploredandValid(botRow, botCol + 2) || isNotExploredandValid(botRow + 1, botCol + 2)) {
        	if(bot.getRobotCurDir() == DIRECTION.SOUTH) moveBot(MOVEMENT.LEFT);
        	else
        		while (bot.getRobotCurDir() != DIRECTION.EAST) {
        		moveBot(MOVEMENT.RIGHT);
        	}
        }
        for(int i = botCol; i <= 14; i++) {
	    	if(!exploredMap.getCell(botRow, i).getIsExplored() || !exploredMap.getCell(botRow + 1, i).getIsExplored() || !exploredMap.getCell(botRow - 1, i).getIsExplored()) {
	    		flag = 1;
	    		break;
	    	}
	    }
	    if(flag == 1) return (isExploredNotObstacle(botRow - 1, botCol + 1) && isExploredAndFree(botRow, botCol + 1) && isExploredNotObstacle(botRow + 1, botCol + 1));
	    else return false;
//        int botRow = bot.getRobotPosRow();
//        int botCol = bot.getRobotPosCol();
//        if(isNotExploredandValid(botRow - 1, botCol + 2) || isNotExploredandValid(botRow, botCol + 2) || isNotExploredandValid(botRow + 1, botCol + 2)) {
//        	if(bot.getRobotCurDir() == DIRECTION.SOUTH) moveBot(MOVEMENT.LEFT);
//        	else
//        		while (bot.getRobotCurDir() != DIRECTION.EAST) {
//        		moveBot(MOVEMENT.RIGHT);
//        	}
//        }
//        return (isExploredNotObstacle(botRow - 1, botCol + 1) && isExploredAndFree(botRow, botCol + 1) && isExploredNotObstacle(botRow + 1, botCol + 1));
    }
   
    /**
     * Returns true if south has unexplored cells and bot can move to south
     */
    private boolean southUnexplored() {
//        int botRow = bot.getRobotPosRow();
//        int botCol = bot.getRobotPosCol();
//        if(isNotExploredandValid(botRow - 2, botCol - 1) || isNotExploredandValid(botRow - 2, botCol) || isNotExploredandValid(botRow - 2, botCol + 1)) {
//        	if(bot.getRobotCurDir() == DIRECTION.WEST) moveBot(MOVEMENT.LEFT);
//        	else
//        		while (bot.getRobotCurDir() != DIRECTION.SOUTH) {
//        		moveBot(MOVEMENT.RIGHT);
//        	}
//        }
//        return (isExploredNotObstacle(botRow - 1, botCol - 1) && isExploredAndFree(botRow - 1, botCol) && isExploredNotObstacle(botRow - 1, botCol + 1));
	    int botRow = bot.getRobotPosRow();
	    int botCol = bot.getRobotPosCol();
	    int flag = 0;
	    
	    if(isNotExploredandValid(botRow - 2, botCol - 1) || isNotExploredandValid(botRow - 2, botCol) || isNotExploredandValid(botRow - 2, botCol + 1)) {
	    	if(bot.getRobotCurDir() == DIRECTION.WEST) moveBot(MOVEMENT.LEFT);
	    	else
	    		while (bot.getRobotCurDir() != DIRECTION.SOUTH) {
	    		moveBot(MOVEMENT.RIGHT);
	    	}
	    }
	    for(int i = botRow; i >= 0; i--) {
	    	if(!exploredMap.getCell(i, botCol).getIsExplored() || !exploredMap.getCell(i, botCol + 1).getIsExplored() || !exploredMap.getCell(i, botCol - 1).getIsExplored()) {
	    		flag = 1;
	    		break;
	    	}
	    }
	    if(flag == 1) return (isExploredNotObstacle(botRow - 1, botCol - 1) && isExploredAndFree(botRow - 1, botCol) && isExploredNotObstacle(botRow - 1, botCol + 1));
	    else return false;
    }
    
    /**
     * Returns true if west has unexplored cells and bot can move to west
     */
    private boolean westUnexplored() {
    	int botRow = bot.getRobotPosRow();
        int botCol = bot.getRobotPosCol();
	    int flag = 0;
	    
        if(isNotExploredandValid(botRow - 1, botCol - 2) || isNotExploredandValid(botRow, botCol - 2) || isNotExploredandValid(botRow + 1, botCol - 2)) {
        	if(bot.getRobotCurDir() == DIRECTION.NORTH) moveBot(MOVEMENT.LEFT);
        	else
        		while (bot.getRobotCurDir() != DIRECTION.WEST) {
        		moveBot(MOVEMENT.RIGHT);
        	}
        }
        for(int i = botCol; i >= 0; i--) {
	    	if(!exploredMap.getCell(botRow, i).getIsExplored() || !exploredMap.getCell(botRow + 1, i).getIsExplored() || !exploredMap.getCell(botRow - 1, i).getIsExplored()) {
	    		flag = 1;
	    		break;
	    	}
	    }
        if(flag == 1) return (isExploredNotObstacle(botRow - 1, botCol - 1) && isExploredAndFree(botRow, botCol - 1) && isExploredNotObstacle(botRow + 1, botCol - 1));
        else return false;
//        int botRow = bot.getRobotPosRow();
//        int botCol = bot.getRobotPosCol();
//        if(isNotExploredandValid(botRow - 1, botCol - 2) || isNotExploredandValid(botRow, botCol - 2) || isNotExploredandValid(botRow + 1, botCol - 2)) {
//        	if(bot.getRobotCurDir() == DIRECTION.NORTH) moveBot(MOVEMENT.LEFT);
//        	else
//        		while (bot.getRobotCurDir() != DIRECTION.WEST) {
//        		moveBot(MOVEMENT.RIGHT);
//        	}
//        }
//        return (isExploredNotObstacle(botRow - 1, botCol - 1) && isExploredAndFree(botRow, botCol - 1) && isExploredNotObstacle(botRow + 1, botCol - 1));
    }
    
    /**
     * Returns the robot to START after exploration and points bot north
     */
    private void goHome() {
    	
		System.out.println("Going home");
		
        if (!bot.getTouchedGoal() && coverageLimit == 300 && timeLimit == 3600) {
            FastestPathAlgo goToGoal = new FastestPathAlgo(exploredMap, bot, realMap, true);
            goToGoal.findFastestPath(RobotConstants.GOAL_ROW, RobotConstants.GOAL_COL, false);
        }

        FastestPathAlgo returnToStart = new FastestPathAlgo(exploredMap, bot, realMap, true);
        returnToStart.findFastestPath(RobotConstants.START_ROW, RobotConstants.START_COL, false);

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
        System.out.println("\nExploration complete!");
        String[] mapStrings = MapDescriptor.generateMapDescriptor(exploredMap);
        System.out.println("Part 1 MDF: " + mapStrings[0]);
        System.out.println("Part 2 MDF: " + mapStrings[1]);
        areaExplored = calculateAreaExplored();
        System.out.printf("%.2f%% Coverage", (areaExplored / 300.0) * 100.0);
        System.out.print(", " + areaExplored + " Cells, ");
        System.out.println((System.currentTimeMillis() - startTime) + " miliseconds");
    }

    /**
     * Returns true if valid cell explored
     */
    private boolean isNotExploredandValid(int r, int c) {
        if (exploredMap.checkValidCoordinates(r, c)) {
            Cell tmp = exploredMap.getCell(r, c);
            return !tmp.getIsExplored();
        }
        return false;
    }
    
    /**
     * Returns true if valid cell explored and not obstacle
     */
    private boolean isExploredNotObstacle(int r, int c) {
        if (exploredMap.checkValidCoordinates(r, c)) {
            Cell tmp = exploredMap.getCell(r, c);
            return (tmp.getIsExplored() && !tmp.getIsWalked() && !tmp.getIsObstacle());
        }
        return false;
    }

    /**
     * Returns true if valid cell explored, not virtual wall and not obstacle
     */
    private boolean isExploredAndFree(int r, int c) {
        if (exploredMap.checkValidCoordinates(r, c)) {
            Cell b = exploredMap.getCell(r, c);
            return (b.getIsExplored() && !b.getIsWalked()  && !b.getIsWall() && !b.getIsObstacle());
        }
        return false;
    }
    
    /**
     * Returns true if 1x3 / 3x1 cells walkable, and can fit bot
     */
    public boolean checkIfWalkable(int r, int c) {
    	boolean a = !isNotExploredandValid(r,c) && !isNotExploredandValid(r+1,c) && !isNotExploredandValid(r-1,c) &&
    				!isNotExploredandValid(r,c+1) && !isNotExploredandValid(r+1,c+1) && !isNotExploredandValid(r-1,c+1) &&
    				!isNotExploredandValid(r,c-1)  && !isNotExploredandValid(r+1,c-1) && !isNotExploredandValid(r-1,c-1);
	
		boolean b = isExploredNotObstacle(r,c) && isExploredNotObstacle(r+1,c) && isExploredNotObstacle(r-1,c) &&
					isExploredNotObstacle(r,c+1) && isExploredNotObstacle(r+1,c+1) && isExploredNotObstacle(r-1,c+1) &&
					isExploredNotObstacle(r,c-1)  && isExploredNotObstacle(r+1,c-1) && isExploredNotObstacle(r-1,c-1);
	   	
		boolean d = isNotExploredandValid(r, c+2) || isNotExploredandValid(r+1,c+2) || isNotExploredandValid(r-1,c+2) ||
					isNotExploredandValid(r,c-2) || isNotExploredandValid(r+1,c-2) || isNotExploredandValid(r-1,c-2) ||
					isNotExploredandValid(r+2,c) || isNotExploredandValid(r+2,c+1) || isNotExploredandValid(r+2,c-1) ||
					isNotExploredandValid(r-2,c) || isNotExploredandValid(r-2,c+1) || isNotExploredandValid(r-2,c-1);
	
		if (a) {
			return false;
		}
		if (b) {
			return false;
		}
		if (d) {
			return true;
		}
		
		return false;
    }

    /**
     * Returns number of cells explored
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
     * Moves bot, sense, and repaint map
     */
    public void moveBot(MOVEMENT m) {
		System.out.println("moveBot(): " + MOVEMENT.print(m));
        
		bot.move(m, bot, exploredMap, true);
		
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
    	senseAndRepaint();
    }

    /**
     * Set bot's sensors, process sensor data and repaints the map
     */
    public void senseAndRepaint() {
        bot.setSensors();
        bot.sense(exploredMap, realMap, bot);
        exploredMap.repaint();
    }

    /**
     * Checks if bot can calibrate at its current position given a direction
     */
//    private boolean canCalibrateOnTheSpot(DIRECTION botDir) {
//        int row = bot.getRobotPosRow();
//        int col = bot.getRobotPosCol();
//
//        switch (botDir) {
//            case NORTH:
//                return exploredMap.getIsObstacleOrWall(row + 2, col - 1) && exploredMap.getIsObstacleOrWall(row + 2, col) && exploredMap.getIsObstacleOrWall(row + 2, col + 1);
//            case EAST:
//                return exploredMap.getIsObstacleOrWall(row + 1, col + 2) && exploredMap.getIsObstacleOrWall(row, col + 2) && exploredMap.getIsObstacleOrWall(row - 1, col + 2);
//            case SOUTH:
//                return exploredMap.getIsObstacleOrWall(row - 2, col - 1) && exploredMap.getIsObstacleOrWall(row - 2, col) && exploredMap.getIsObstacleOrWall(row - 2, col + 1);
//            case WEST:
//                return exploredMap.getIsObstacleOrWall(row + 1, col - 2) && exploredMap.getIsObstacleOrWall(row, col - 2) && exploredMap.getIsObstacleOrWall(row - 1, col - 2);
//        }
//
//        return false;
//    }

    /**
     * Returns a direction for bot calibration or null otherwise
     */
//    private DIRECTION getCalibrationDirection() {
//        DIRECTION origDir = bot.getRobotCurDir();
//        DIRECTION dirToCheck;
//
//        dirToCheck = DIRECTION.getNext(origDir);                    // right turn
//        if (canCalibrateOnTheSpot(dirToCheck)) return dirToCheck;
//
//        dirToCheck = DIRECTION.getPrevious(origDir);                // left turn
//        if (canCalibrateOnTheSpot(dirToCheck)) return dirToCheck;
//
//        dirToCheck = DIRECTION.getPrevious(dirToCheck);             // u turn
//        if (canCalibrateOnTheSpot(dirToCheck)) return dirToCheck;
//
//        return null;
//    }

    /**
     * Turns bot in the needed direction and sends CALIBRATE movement. Once calibrated, the bot is turned back
     * to its original direction.
     */
//    private void calibrateBot(DIRECTION targetDir) {
//        DIRECTION origDir = bot.getRobotCurDir();
//
//        turnBotDirection(targetDir);
//        moveBot(MOVEMENT.CALIBRATE);
//        turnBotDirection(origDir);
//    }

    /**
     * Turns the robot to required direction
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