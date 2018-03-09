package algorithms;

import map.Cell;
import map.Map;
import map.MapConstants;
import robot.Robot;
import robot.RobotConstants;
import robot.RobotConstants.DIRECTION;
import robot.RobotConstants.MOVEMENT;
import simulator.Simulator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Stack;

/**
 * Fastest path algorithm using A* algorithm
 * g(n) = Real Cost from START to n
 * h(n) = Heuristic Cost from n to GOAL
 */

public class FastestPathAlgo {
    private ArrayList<Cell> toVisit;        // array of Cells to be visited
    private ArrayList<Cell> visited;        // array of visited Cells
    private HashMap<Cell, Cell> parents;    // HashMap of Child --> Parent
    private Cell current;                   // current Cell
    private Cell[] neighbors;               // array of neighbors of current Cell
    private DIRECTION curDir;               // current direction of robot
    private double[][] gCosts;              // array of real cost from START to [row][col] i.e. g(n)
    private Robot bot;
    private Map exploredMap;
    private final Map realMap;

    public FastestPathAlgo(Map exploredMap, Robot bot, Map realMap, boolean exploreMode) {
        this.realMap = realMap;
        initAlgo(exploredMap, bot);
    }

    /**
     * Initialize FastestPathAlgo
     */
    private void initAlgo(Map map, Robot bot) {
        this.bot = bot;
        this.exploredMap = map;
        this.toVisit = new ArrayList<>();
        this.visited = new ArrayList<>();
        this.parents = new HashMap<>();
        this.neighbors = new Cell[4];
        this.current = map.getCell(bot.getRobotPosRow(), bot.getRobotPosCol());
        this.curDir = bot.getRobotCurDir();
        this.gCosts = new double[MapConstants.MAP_ROWS][MapConstants.MAP_COLS];

        // Initialize gCosts array
        for (int i = 0; i < MapConstants.MAP_ROWS; i++) {
            for (int j = 0; j < MapConstants.MAP_COLS; j++) {
                Cell cell = map.getCell(i, j);
                if (!canBeVisited(cell)) {
                    gCosts[i][j] = RobotConstants.INFINITE_COST;
                } else {
                    gCosts[i][j] = 0;
                }
            } 
        }
        toVisit.add(current);

        // Initialize starting point
        gCosts[bot.getRobotPosRow()][bot.getRobotPosCol()] = 0;
    }

    /**
     * Returns true if cell can be visited
     */
    private boolean canBeVisited(Cell c) {
        return c.getIsExplored() && !c.getIsObstacle() && !c.getIsWall();
    }

    /**
     * Returns cell inside toVisit with the minimum g(n) + h(n)
     */
    public Cell minimumCostCell(int goalRow, int getCol) {
        int size = toVisit.size();
        double minCost = RobotConstants.INFINITE_COST;
        Cell result = null;

        for (int i = size - 1; i >= 0; i--) {
            double gCost = gCosts[(toVisit.get(i).getRow())][(toVisit.get(i).getCol())];
            double cost = gCost + costH(toVisit.get(i), goalRow, getCol);
            if (cost < minCost) {
                minCost = cost;
                result = toVisit.get(i);
            }
        }

        return result;
    }

    /**
     * Returns heuristic cost (h(n)) from cell to [goalRow, goalCol]
     */
    private double costH(Cell b, int goalRow, int goalCol) {
        // Heuristic: No. of moves equal to difference in the row and column values
        double movementCost = (Math.abs(goalCol - b.getCol()) + Math.abs(goalRow - b.getRow())) * RobotConstants.MOVE_COST;

        if (movementCost == 0) return 0;

        // Heuristic: If b not in the same row or column, 1 turn needed
        double turnCost = 0;
        if (goalCol - b.getCol() != 0 || goalRow - b.getRow() != 0) {
            turnCost = RobotConstants.TURN_COST;
        }

        return movementCost + turnCost;
    }

    /**
     * Returns target direction from robot to cell
     */
    private DIRECTION getTargetDir(int botR, int botC, DIRECTION botDir, Cell target) {
        if (botC - target.getCol() > 0) {
            return DIRECTION.WEST;
        } else if (target.getCol() - botC > 0) {
            return DIRECTION.EAST;
        } else {
            if (botR - target.getRow() > 0) {
                return DIRECTION.SOUTH;
            } else if (target.getRow() - botR > 0) {
                return DIRECTION.NORTH;
            } else {
                return botDir;
            }
        }
    }

    /**
     * Get actual turning cost from 1 DIRECTION to another
     */
    private double getTurnCost(DIRECTION a, DIRECTION b) {
        int numOfTurn = Math.abs(a.ordinal() - b.ordinal());
        if (numOfTurn > 2) {
            numOfTurn = numOfTurn % 2;
        }
        return (numOfTurn * RobotConstants.TURN_COST);
    }

    /**
     * Calculate the actual cost of moving from Cell a to Cell b
     */
    private double costG(Cell a, Cell b, DIRECTION aDir) {
        double moveCost = RobotConstants.MOVE_COST;

        double turnCost;
        DIRECTION targetDir = getTargetDir(a.getRow(), a.getCol(), aDir, b);
        turnCost = getTurnCost(aDir, targetDir);

        return moveCost + turnCost;
    }

    /**
     * Find fastest path from the robot's current pos to [goalRow, goalCol]
     */
    public String findFastestPath(int goalRow, int goalCol) {
    	initAlgo(exploredMap, bot);
    	
    	System.out.print("\nCalculating fastest path from (" + current.getRow() + ", " + current.getCol() + ") to goal (" + goalRow + ", " + goalCol + ") ");
        Stack<Cell> path;
        do {

            // Get cell with minimum cost from toVisit and assign it to current.
            current = minimumCostCell(goalRow, goalCol);

            // Point the robot in the direction of current from the previous cell.
            if (parents.containsKey(current)) {
                curDir = getTargetDir(parents.get(current).getRow(), parents.get(current).getCol(), curDir, current);
            }

            visited.add(current);       // add current to visited
            toVisit.remove(current);    // remove current from toVisit

            if (visited.contains(exploredMap.getCell(goalRow, goalCol))) {
                System.out.println("found: ");
                path = getPath(goalRow, goalCol);
                printFastestPath(path);
                return executePath(path, goalRow, goalCol);
            }
            // Setup neighbors of current cell. [Top, Bottom, Left, Right].
            if (exploredMap.checkValidCoordinates(current.getRow() + 1, current.getCol())) {
                neighbors[0] = exploredMap.getCell(current.getRow() + 1, current.getCol());
                if (!canBeVisited(neighbors[0])) {
                    neighbors[0] = null;
                }
            }
            if (exploredMap.checkValidCoordinates(current.getRow() - 1, current.getCol())) {
                neighbors[1] = exploredMap.getCell(current.getRow() - 1, current.getCol());
                if (!canBeVisited(neighbors[1])) {
                    neighbors[1] = null;
                }
            }
            if (exploredMap.checkValidCoordinates(current.getRow(), current.getCol() - 1)) {
                neighbors[2] = exploredMap.getCell(current.getRow(), current.getCol() - 1);
                if (!canBeVisited(neighbors[2])) {
                    neighbors[2] = null;
                }
            }
            if (exploredMap.checkValidCoordinates(current.getRow(), current.getCol() + 1)) {
                neighbors[3] = exploredMap.getCell(current.getRow(), current.getCol() + 1);
                if (!canBeVisited(neighbors[3])) {
                    neighbors[3] = null;
                }
            }

            // Iterate through neighbors and update their g(n) values
            for (int i = 0; i < 4; i++) {
                if (neighbors[i] != null) {
                    if (visited.contains(neighbors[i])) {
                        continue;
                    }
                    if (!(toVisit.contains(neighbors[i]))) {
                        parents.put(neighbors[i], current);
                        gCosts[neighbors[i].getRow()][neighbors[i].getCol()] = gCosts[current.getRow()][current.getCol()] + costG(current, neighbors[i], curDir);
                        toVisit.add(neighbors[i]);
                    } else {
                        double currentGScore = gCosts[neighbors[i].getRow()][neighbors[i].getCol()];
                        double newGScore = gCosts[current.getRow()][current.getCol()] + costG(current, neighbors[i], curDir);
                        if (newGScore < currentGScore) {
                            gCosts[neighbors[i].getRow()][neighbors[i].getCol()] = newGScore;
                            parents.put(neighbors[i], current);
                        }
                    }
                }
            }
        } while (!toVisit.isEmpty());

        System.out.println("not found.");
        return null;
    }

    /**
     * Generates path in reverse using the parents HashMap
     */
    private Stack<Cell> getPath(int goalRow, int goalCol) {
        Stack<Cell> actualPath = new Stack<>();
        Cell temp = exploredMap.getCell(goalRow, goalCol);

        while (true) {
            actualPath.push(temp);
            temp = parents.get(temp);
            if (temp == null) {
                break;
            }
        }

        return actualPath;
    }

    /**
     * Executes fastest path and returns StringBuilder with the path steps
     */
    private String executePath(Stack<Cell> path, int goalRow, int goalCol) {
        StringBuilder outputString = new StringBuilder();
        StringBuilder shortOutputString = new StringBuilder();
        Cell temp = path.pop();
        DIRECTION targetDir;
        int bin = 0;
        char prev = '0';
        Robot tempB = new Robot(bot.getRobotPosRow(), bot.getRobotPosCol(), false);
        tempB.setRobotDir(bot.getRobotCurDir());
        tempB.setSpeed(0);

        ArrayList<MOVEMENT> movements = new ArrayList<>();
        
        while ((tempB.getRobotPosRow() != goalRow) || (tempB.getRobotPosCol() != goalCol)) {
            if (tempB.getRobotPosRow() == temp.getRow() && tempB.getRobotPosCol() == temp.getCol()) {
                temp = path.pop();
            }

            targetDir = getTargetDir(tempB.getRobotPosRow(), tempB.getRobotPosCol(), tempB.getRobotCurDir(), temp);

            //If bot has to move backwards (To save 1 rotation)
            if(targetDir == DIRECTION.SOUTH && tempB.getRobotCurDir() == DIRECTION.NORTH ||
            		targetDir == DIRECTION.NORTH && tempB.getRobotCurDir() == DIRECTION.SOUTH ||
            		targetDir == DIRECTION.WEST && tempB.getRobotCurDir() == DIRECTION.EAST ||
            		targetDir == DIRECTION.EAST && tempB.getRobotCurDir() == DIRECTION.WEST) {
                movements.add(MOVEMENT.BACKWARD);
                outputString.append(MOVEMENT.print(MOVEMENT.BACKWARD));

                tempB.move(MOVEMENT.BACKWARD);
            }
            //Else rotate to the right direction before moving forward
            else {
            	
	            while (tempB.getRobotCurDir() != targetDir) {
	                movements.add(getTargetMove(tempB.getRobotCurDir(), targetDir));
	                outputString.append(MOVEMENT.print(getTargetMove(tempB.getRobotCurDir(), targetDir)));
	                tempB.move(getTargetMove(tempB.getRobotCurDir(), targetDir));
	            }
                movements.add(MOVEMENT.FORWARD);
                outputString.append(MOVEMENT.print(MOVEMENT.FORWARD));
                tempB.move(MOVEMENT.FORWARD);
            }
            
//            ExplorationAlgo explalgo = new ExplorationAlgo(exploredMap, realMap, bot, 300, 3000);
//            explalgo.senseAndRepaint();
            System.out.println("Direction " + DIRECTION.print(targetDir)+ " to (" + tempB.getRobotPosRow() + ", " + tempB.getRobotPosCol() + ")");
            
//            movements.add(MOVEMENT.FORWARD);
//            outputString.append(MOVEMENT.print(MOVEMENT.FORWARD));
//            tempB.move(MOVEMENT.FORWARD);
        }
        
        System.out.println("Instruction string:" + outputString.toString());
        
        shortOutputString.append("#");
        prev = outputString.charAt(0);
        shortOutputString.append(prev);
        bin++;
	        
        for(int i = 1; i < outputString.length(); i++) {
        	
        	switch (outputString.charAt(i)) {
			case 'F':
				if(prev == 'F') {
					if(bin == 9) {
						shortOutputString.append(bin);
						shortOutputString.append('F');
						bin = 1;
					}
					else{
						bin++;
					}
				}
				else {
					shortOutputString.append(bin);
					shortOutputString.append('F');
					bin = 1;
					prev = 'F';
				}
				break;
			case 'L':
				if(prev == 'L') {
					if(bin == 9) {
						shortOutputString.append(bin);
						shortOutputString.append('L');
						bin = 1;
					}
					else{
						bin++;
					}
				}
				else {
					shortOutputString.append(bin);
					shortOutputString.append('L');
					bin = 1;
					prev = 'L';
				}
				break;
			case 'B':
				if(prev == 'B') {
					if(bin == 9) {
						shortOutputString.append(bin);
						shortOutputString.append('B');
						bin = 1;
					}
					else{
						bin++;
					}
				}
				else {
					shortOutputString.append(bin);
					shortOutputString.append('B');
					bin = 1;
					prev = 'B';
				}
				break;
			case 'R':
				if(prev == 'R') {
					if(bin == 9) {
						shortOutputString.append(bin);
						shortOutputString.append('R');
						bin = 1;
					}
					else{
						bin++;
					}
				}
				else {
					shortOutputString.append(bin);
					shortOutputString.append('R');
					bin = 1;
					prev = 'R';
				}
				break;
			default:
				findFastestPath(MapConstants.GOAL_ROW, MapConstants.GOAL_COL);
			}
        }
        
        shortOutputString.append(bin);
        shortOutputString.append("!");
        
        System.out.println("Short string:" + shortOutputString);

        if(bot.getRealBot()) {
	    	Simulator.communicator.sendMsg(shortOutputString.toString(), null);
    	}
    	
    	executePathMovements(outputString.toString(), false);
    	
        return outputString.toString();
    }
    
    private boolean executePathMovements(String outputString, boolean actualFastestPath){
    	try {

	        if(Simulator.actualRun) bot.setSpeed(500);
	        
	    	for(int i = 0; i < outputString.length(); i++) {
		    	switch (outputString.charAt(i)) {
				case 'F':
			        bot.move(MOVEMENT.FORWARD);
					break;
				case 'L':
			        bot.move(MOVEMENT.LEFT);
					break;
				case 'B':
			        bot.move(MOVEMENT.BACKWARD);
					break;
				case 'R':
			        bot.move(MOVEMENT.RIGHT);
					break;
				default:
					findFastestPath(MapConstants.GOAL_ROW, MapConstants.GOAL_COL);
					return false;
				}
		    	
		    	if(!actualFastestPath) {
		            ExplorationAlgo explalgo = new ExplorationAlgo(exploredMap, realMap, bot, 300, 3000);
		            explalgo.senseAndRepaint();
		    	}
	    	}
		} catch (Exception e) {
			System.out.println(e.toString());
			return false;
		}

		System.out.println("Fastest Path executed completely");
    	return true;
    }

    /**
     * Returns movement to execute to get from 1 direction to another
     */
    private MOVEMENT getTargetMove(DIRECTION a, DIRECTION b) {
        switch (a) {
            case NORTH:
                switch (b) {
                    case NORTH:
                        return MOVEMENT.ERROR;
                    case SOUTH:
                        return MOVEMENT.LEFT;
                    case WEST:
                        return MOVEMENT.LEFT;
                    case EAST:
                        return MOVEMENT.RIGHT;
                }
                break;
            case SOUTH:
                switch (b) {
                    case NORTH:
                        return MOVEMENT.LEFT;
                    case SOUTH:
                        return MOVEMENT.ERROR;
                    case WEST:
                        return MOVEMENT.RIGHT;
                    case EAST:
                        return MOVEMENT.LEFT;
                }
                break;
            case WEST:
                switch (b) {
                    case NORTH:
                        return MOVEMENT.RIGHT;
                    case SOUTH:
                        return MOVEMENT.LEFT;
                    case WEST:
                        return MOVEMENT.ERROR;
                    case EAST:
                        return MOVEMENT.LEFT;
                }
                break;
            case EAST:
                switch (b) {
                    case NORTH:
                        return MOVEMENT.LEFT;
                    case SOUTH:
                        return MOVEMENT.RIGHT;
                    case WEST:
                        return MOVEMENT.LEFT;
                    case EAST:
                        return MOVEMENT.ERROR;
                }
        }
        return MOVEMENT.ERROR;
    }

    /**
     * Prints fastest path from Stack
     */
    private void printFastestPath(Stack<Cell> path) {

        Stack<Cell> pathForPrint = (Stack<Cell>) path.clone();
        Cell temp;
        while (!pathForPrint.isEmpty()) {
            temp = pathForPrint.pop();
            if (!pathForPrint.isEmpty()) System.out.print("(" + temp.getRow() + ", " + temp.getCol() + ") -> ");
            else System.out.print("(" + temp.getRow() + ", " + temp.getCol() + ")");
        }

        System.out.println("");
    }

    /**
     * Prints all g(n) values for the cells
     */
    public void printGCosts() {
        for (int i = 0; i < MapConstants.MAP_ROWS; i++) {
            for (int j = 0; j < MapConstants.MAP_COLS; j++) {
                System.out.print(gCosts[MapConstants.MAP_ROWS - 1 - i][j]);
                System.out.print(";");
            }
            System.out.println("\n");
        }
    }
}