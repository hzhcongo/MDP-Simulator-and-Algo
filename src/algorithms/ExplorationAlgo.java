package algorithms;

import map.Cell;
import map.Map;
import map.MapConstants;
import robot.Robot;
import robot.RobotConstants;
import robot.RobotConstants.DIRECTION;
import robot.RobotConstants.MOVEMENT;
import simulator.Simulator;
import utils.MDFGenerator;
//import java.util.Stack;

/**
 * Exploration algorithm
 * @author Heng Ze Hao
 */
public class ExplorationAlgo {
    public final Map exploredMap;
    private final Map realMap;
    private final Robot bot;
    private final int coverageLimit;
    private final int timeLimit;
    private int areaExplored;
    private long startTime;
    public MOVEMENT prevWalledTurn;
    public int stepsTaken = 0;
//    private Stack<String> directionMoved = new Stack<String>();
    
    public ExplorationAlgo(Map exploredMap, Map realMap, Robot bot, int coverageLimit, int timeLimit) {
        this.exploredMap = exploredMap;
        this.realMap = realMap;
        this.bot = bot;
        this.coverageLimit = coverageLimit;
        this.timeLimit = timeLimit;
    }

    // Main method that is called to start the exploration
    public void runExploration() {
        exploredMap.getCell(1,1).setIsWalked(true);
        startTime = System.currentTimeMillis();
        explore(bot.getRobotPosRow(), bot.getRobotPosCol());
    }

    // Loops through exploration algorithm until:
    // - bot reaches goal and returns back to start zone, 
    // OR 
    // - areaExplored > coverageLimit || System.currentTimeMillis() > endTime
    //
    // FastestPath called to goal when all cells are explored but have not reached goal
    private void explore(int r, int c) {
        if (bot.getRealBot()) {
        	Simulator.communicator.sendMsg("E", null);
        }            
        
    	senseAndRepaint();
    	
    	do {
			rightWallHug();

			areaExplored = calculateAreaExplored();
            System.out.println("Area explored: " + areaExplored);
            System.out.println("Time passed: " + (System.currentTimeMillis() - startTime) + " ms");
            
            //areaExplored >= 150 ensures map must be at least be half-explored before breaking out of loop
            if (bot.getRobotPosRow() == r && bot.getRobotPosCol() == c && areaExplored >= 150) 
            	break;
            
        } while ((!bot.getTouchedGoal() && System.currentTimeMillis() - startTime <= timeLimit) || (areaExplored < coverageLimit && System.currentTimeMillis() - startTime <= timeLimit));
        
    	int x = 0;
    	do {            
            if (areaExplored == 300) {
                break;
            }
            else {
	    		System.out.println("Map not fully explored");
            	if(!fastestPath()) {
	            	if(x < 2) {	
	    	    		System.out.println("Re-sensing surroundings");
	    	    		moveBot(MOVEMENT.RIGHT);
	    	    		moveBot(MOVEMENT.RIGHT);
	    	    		moveBot(MOVEMENT.RIGHT);
	    	    		moveBot(MOVEMENT.RIGHT);
	    	    		x++;
	            	}
	            	else {
	    	    		System.out.println("Surrounding is all blocked");
	            		break;
	            	}
    	    	}
            }

			areaExplored = calculateAreaExplored();
            System.out.println("Area explored: " + areaExplored);
            System.out.println("Time passed: " + (System.currentTimeMillis() - startTime) + " ms");
            
        } while ((!bot.getTouchedGoal() && System.currentTimeMillis() - startTime <= timeLimit) || (areaExplored < coverageLimit && System.currentTimeMillis() - startTime <= timeLimit));
        
        goHome();
    }

    // Inner logic of Right Wall Hug algorithm
    private void rightWallHug() {
    	System.out.println("\nBot current pos: " + bot.getRobotPosRow() + ", " + bot.getRobotPosCol());
    	
    	//Failsafe to break out of constant right turning
//    	if(lookRight() && lookForward() && lookLeft()) {
//	        moveBot(MOVEMENT.FORWARD);
//    	}
    	
    	// If right no wall, turn right to hug wall
    	if (lookRight()) {
    		if(stepsTaken >= 4) {
	    		moveBot(MOVEMENT.LEFT);
	    		stepsTaken = 0;
	    	}
	    	else
	    	{
    			moveBot(MOVEMENT.RIGHT);
	    	}
	        // If front no wall, move forward and break
	        if (lookForward()) {
	        	moveBot(MOVEMENT.FORWARD);
	        	stepsTaken++;
	        }
        // Else, right already has wall, so just check if can move forward
	    } else if (lookForward()) {
	        moveBot(MOVEMENT.FORWARD);
    		stepsTaken = 0;
	    // Else, right already has wall but front has wall, left has no wall
	    } else if (lookLeft()) {
	        moveBot(MOVEMENT.LEFT);
	        // If front no wall, move forward and break
	        if (lookForward())
	        	moveBot(MOVEMENT.FORWARD);
        // Else, check backwards
    		stepsTaken = 0;
	    } else {
	        moveBot(MOVEMENT.RIGHT);
	        moveBot(MOVEMENT.RIGHT);
    		stepsTaken = 0;
	    }
    	
//    	NEW ALGORITHM
//    	- xFree/lookRight HAS TO SET AS 2 CELLS INSTEAD OF 1
//        if (right adj is obs){
//	        moveBot(MOVEMENT.LEFT);
//	        moveBot(MOVEMENT.FORWARD);
//	        moveBot(MOVEMENT.RIGHT);
//        }
//        else {
//    		if (right adj adj is obs){
//    			if(front adj adj is obs) {
//    		        moveBot(MOVEMENT.FORWARD);
//    			}
//				else {
//					
//				}
//			}
//    		else{
//    		
//    		}
//		}
    }
    
    // lookX() methods executes the appropiate XFree() based on current robot direction
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
    
    // XFree() methods checks if adjacent cells are free to move to
    private boolean northFree() {
        int botRow = bot.getRobotPosRow();
        int botCol = bot.getRobotPosCol();
        return (isExploredNotObstacle(botRow + 1, botCol - 1) && isExploredAndFree(botRow + 1, botCol) && isExploredNotObstacle(botRow + 1, botCol + 1));
    }

    private boolean eastFree() {
        int botRow = bot.getRobotPosRow();
        int botCol = bot.getRobotPosCol();
        return (isExploredNotObstacle(botRow - 1, botCol + 1) && isExploredAndFree(botRow, botCol + 1) && isExploredNotObstacle(botRow + 1, botCol + 1));
    }

    private boolean southFree() {
        int botRow = bot.getRobotPosRow();
        int botCol = bot.getRobotPosCol();
        return (isExploredNotObstacle(botRow - 1, botCol - 1) && isExploredAndFree(botRow - 1, botCol) && isExploredNotObstacle(botRow - 1, botCol + 1));
    }

    private boolean westFree() {
        int botRow = bot.getRobotPosRow();
        int botCol = bot.getRobotPosCol();
        return (isExploredNotObstacle(botRow - 1, botCol - 1) && isExploredAndFree(botRow, botCol - 1) && isExploredNotObstacle(botRow + 1, botCol - 1));
    }
    
    // Runs a variation of fastestPath for during exploration
    // Occurs when robot has reached back home after right wall hugged everything but some cels are still unexplored
    private boolean fastestPath() {
		System.out.println("Doing fastest path for Exploration");
    	int currentcellrow = bot.getRobotPosRow();
    	int currentcellcol = bot.getRobotPosCol();
    	exploredMap.getCell(currentcellrow,currentcellcol).setIsWalked(true);
    	FastestPathAlgo goToCell = new FastestPathAlgo(exploredMap,bot, realMap, true);
    	
    	int z = 0; 		//Array counter
        int array[][] = new int [2][50];
    	int num = 999; 	//Min cost
    	for (int i = 1; i < 19;i++) {
    		for (int j = 1; j < 14;j++) {
   			if (!exploredMap.getCell(i, j).getIsWalked() && exploredMap.getCell(i, j).getIsExplored()) {  				
    				if (exploredMap.checkIfWalkable(i,j)) {
	    				array[0][z] = i; //Row values 
	    				array[1][z] = j; //Col values
	    				z++;
    				}
    			}	
    		}
    	}
    	
		// Get minCost for each cell in array, and compare with current minCost
    	int minCost = 999;
		for (int i = 0; i<z; i++) {
			int x = (Math.abs(bot.getRobotPosRow() - array[0][i]) + Math.abs(bot.getRobotPosCol() - array[1][i])); //finding the nearest cell by comparing row and col
//    			int x = (Math.abs(MapConstants.GOAL_ROW - array[0][i]) + Math.abs(MapConstants.GOAL_COL - array[1][i])); //finding the nearest cell to goal by comparing row and col

			if ((x < minCost)) {
				minCost = x;
				num = i;
			}
		}	   
		
		// The only time there are no valid cells is when map all explored
		// Thus need to catch infinite loop and hard-execute fastestPath to GOAL since all explored
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

//    // XUnexplored returns true if X has unexplored cells and bot can move to X
//    private boolean northUnexplored() {
//    	int botRow = bot.getRobotPosRow();
//        int botCol = bot.getRobotPosCol();
//	    int flag = 0;
//	    
//        if(isNotExploredandValid(botRow + 2, botCol - 1) || isNotExploredandValid(botRow + 2, botCol) || isNotExploredandValid(botRow + 2, botCol + 1)) {
//        	if(bot.getRobotCurDir() == DIRECTION.EAST) moveBot(MOVEMENT.LEFT);
//        	else
//        		while (bot.getRobotCurDir() != DIRECTION.NORTH) {
//        			moveBot(MOVEMENT.RIGHT);
//        		}
//        }
//        for(int i = botRow; i <= 19; i++) {
//	    	if(!exploredMap.getCell(i, botCol).getIsExplored() || !exploredMap.getCell(i, botCol + 1).getIsExplored() || !exploredMap.getCell(i, botCol - 1).getIsExplored()) {
//	    		flag = 1;
//	    		break;
//	    	}
//	    }
//	    if(flag == 1) 
//	    	return (isExploredNotObstacle(botRow + 1, botCol - 1) && isExploredAndFree(botRow + 1, botCol) && isExploredNotObstacle(botRow + 1, botCol + 1));
//	    else 
//	    	return false;
//    }
//    
//    private boolean eastUnexplored() {
//    	int botRow = bot.getRobotPosRow();
//        int botCol = bot.getRobotPosCol();
//	    int flag = 0;
//	    
//        if(isNotExploredandValid(botRow - 1, botCol + 2) || isNotExploredandValid(botRow, botCol + 2) || isNotExploredandValid(botRow + 1, botCol + 2)) {
//        	if(bot.getRobotCurDir() == DIRECTION.SOUTH) moveBot(MOVEMENT.LEFT);
//        	else
//        		while (bot.getRobotCurDir() != DIRECTION.EAST) {
//        		moveBot(MOVEMENT.RIGHT);
//        	}
//        }
//        for(int i = botCol; i <= 14; i++) {
//	    	if(!exploredMap.getCell(botRow, i).getIsExplored() || !exploredMap.getCell(botRow + 1, i).getIsExplored() || !exploredMap.getCell(botRow - 1, i).getIsExplored()) {
//	    		flag = 1;
//	    		break;
//	    	}
//	    }
//	    if(flag == 1) 
//	    	return (isExploredNotObstacle(botRow - 1, botCol + 1) && isExploredAndFree(botRow, botCol + 1) && isExploredNotObstacle(botRow + 1, botCol + 1));
//	    else 
//	    	return false;    
//	    }
//   
//    private boolean southUnexplored() {
//	    int botRow = bot.getRobotPosRow();
//	    int botCol = bot.getRobotPosCol();
//	    int flag = 0;
//	    
//	    if(isNotExploredandValid(botRow - 2, botCol - 1) || isNotExploredandValid(botRow - 2, botCol) || isNotExploredandValid(botRow - 2, botCol + 1)) {
//	    	if(bot.getRobotCurDir() == DIRECTION.WEST) moveBot(MOVEMENT.LEFT);
//	    	else
//	    		while (bot.getRobotCurDir() != DIRECTION.SOUTH) {
//	    		moveBot(MOVEMENT.RIGHT);
//	    	}
//	    }
//	    for(int i = botRow; i >= 0; i--) {
//	    	if(!exploredMap.getCell(i, botCol).getIsExplored() || !exploredMap.getCell(i, botCol + 1).getIsExplored() || !exploredMap.getCell(i, botCol - 1).getIsExplored()) {
//	    		flag = 1;
//	    		break;
//	    	}
//	    }
//	    if(flag == 1) 
//	    	return (isExploredNotObstacle(botRow - 1, botCol - 1) && isExploredAndFree(botRow - 1, botCol) && isExploredNotObstacle(botRow - 1, botCol + 1));
//	    else 
//	    	return false;
//    }
//    
//    private boolean westUnexplored() {
//    	int botRow = bot.getRobotPosRow();
//        int botCol = bot.getRobotPosCol();
//	    int flag = 0;
//	    
//        if(isNotExploredandValid(botRow - 1, botCol - 2) || isNotExploredandValid(botRow, botCol - 2) || isNotExploredandValid(botRow + 1, botCol - 2)) {
//        	if(bot.getRobotCurDir() == DIRECTION.NORTH) moveBot(MOVEMENT.LEFT);
//        	else
//        		while (bot.getRobotCurDir() != DIRECTION.WEST) {
//        		moveBot(MOVEMENT.RIGHT);
//        	}
//        }
//        for(int i = botCol; i >= 0; i--) {
//	    	if(!exploredMap.getCell(botRow, i).getIsExplored() || !exploredMap.getCell(botRow + 1, i).getIsExplored() || !exploredMap.getCell(botRow - 1, i).getIsExplored()) {
//	    		flag = 1;
//	    		break;
//	    	}
//	    }
//        if(flag == 1) 
//        	return (isExploredNotObstacle(botRow - 1, botCol - 1) && isExploredAndFree(botRow, botCol - 1) && isExploredNotObstacle(botRow + 1, botCol - 1));
//        else 
//        	return false;
//    }
    
    // Returns the robot to START and points bot north
    private void goHome() {
    	
		System.out.println("Going home");
		
        if (!bot.getTouchedGoal() && coverageLimit == 300 && timeLimit == 3600) {
            FastestPathAlgo goToGoal = new FastestPathAlgo(exploredMap, bot, realMap, true);
            goToGoal.findFastestPath(RobotConstants.GOAL_ROW, RobotConstants.GOAL_COL, false);
        }

        FastestPathAlgo returnToStart = new FastestPathAlgo(exploredMap, bot, realMap, true);
        returnToStart.findFastestPath(RobotConstants.START_ROW, RobotConstants.START_COL, false);

        System.out.println("\nResetting bot direction");
        turnBotDirection(DIRECTION.NORTH);
        System.out.println("\nExploration complete!");
        String[] mapStrings = MDFGenerator.generateMapDescriptor(exploredMap);
        System.out.println("Part 1 MDF: " + mapStrings[0]);
        System.out.println("Part 2 MDF: " + mapStrings[1]);
        areaExplored = calculateAreaExplored();
        System.out.printf("%.2f%% Coverage", (areaExplored / 300.0) * 100.0);
        System.out.print(", " + areaExplored + " Cells, ");
        System.out.println((System.currentTimeMillis() - startTime) + " miliseconds");
    }

    // Returns true if valid cell explored
    private boolean isNotExploredandValid(int r, int c) {
        if (exploredMap.checkIfCoordinatesValid(r, c)) {
            Cell tmp = exploredMap.getCell(r, c);
            return !tmp.getIsExplored();
        }
        return false;
    }
    
    // Returns true if cell is explored and not an obstacle
    private boolean isExploredNotObstacle(int r, int c) {
        if (exploredMap.checkIfCoordinatesValid(r, c)) {
            Cell tmp = exploredMap.getCell(r, c);
            return (tmp.getIsExplored() && !tmp.getIsObstacle());
        }
        return false;
    }

    // Returns true if cell is explored, not virtual wall and not obstacle
    private boolean isExploredAndFree(int r, int c) {
        if (exploredMap.checkIfCoordinatesValid(r, c)) {
            Cell b = exploredMap.getCell(r, c);
            return (b.getIsExplored() && !b.getIsWall() && !b.getIsObstacle());
        }
        return false;
    }
    
    // Returns true if 1x3 / 3x1 cells walkable, and can fit robot
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

    // Calculates the number of cells explored
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

    // Moves bot, sense, and repaint map
    public void moveBot(MOVEMENT m) {
		System.out.println("moveBot(): " + MOVEMENT.print(m));
		bot.move(m, bot, exploredMap, true);
    	senseAndRepaint();
    }

    // Set bot's sensors, process sensor data and repaints the map
    public void senseAndRepaint() {
        bot.setSensors();
        bot.sense(exploredMap, realMap, bot);
        exploredMap.repaint();
    }
    
    // Turns the robot to required direction
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