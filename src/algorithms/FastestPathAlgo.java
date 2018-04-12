package algorithms;

import map.Cell;
import map.Map;
import map.MapConstants;
import robot.Robot;
import robot.RobotConstants;
import robot.RobotConstants.DIRECTION;
import robot.RobotConstants.MOVEMENT;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Stack;

/**
 * Fastest path algorithm using A* algorithm
 * @author Heng Ze Hao
 */
public class FastestPathAlgo {
    private ArrayList<Cell> toVisit;        //Array of Cells to be visited
    private ArrayList<Cell> visited;        //Array of visited Cells
    private HashMap<Cell, Cell> parents;    //HashMap of Child --> Parent
    private Cell current;                   //Current Cell
    private Cell[] neighbours;               //Array of neighbors of current Cell
    private DIRECTION curDir;               //Current direction of robot
    private double[][] gCosts;              //Array of real cost from START to [row][col] i.e. g(n)
    private Robot bot;
    private Map exploredMap;
    private final Map realMap;

    public FastestPathAlgo(Map exploredMap, Robot bot, Map realMap, boolean exploreMode) {
        this.realMap = realMap;
        initAlgo(exploredMap, bot);
    }

    // Initialize FastestPathAlgo
    private void initAlgo(Map map, Robot bot) {
        this.bot = bot;
        this.exploredMap = map;
        this.toVisit = new ArrayList<>();
        this.visited = new ArrayList<>();
        this.parents = new HashMap<>();
        this.neighbours = new Cell[4];
        this.current = map.getCell(bot.getRobotPosRow(), bot.getRobotPosCol());
        this.curDir = bot.getRobotCurDir();
        this.gCosts = new double[MapConstants.MAP_ROWS][MapConstants.MAP_COLS];

        //Initialize gCosts array
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

        //Initialize starting point
        gCosts[bot.getRobotPosRow()][bot.getRobotPosCol()] = 0;
    }

    // Returns true if cell can be visited
    private boolean canBeVisited(Cell c) {
        return c.getIsExplored() && !c.getIsObstacle() && !c.getIsWall();
    }

    // Returns cell inside toVisit with the minimum g(n) + h(n)
    public Cell getMinCostingCell(int goalRow, int getCol) {
        int size = toVisit.size();
        double minCost = RobotConstants.INFINITE_COST;
        Cell result = null;

        for (int i = size - 1; i >= 0; i--) {
            double gCost = gCosts[(toVisit.get(i).getRow())][(toVisit.get(i).getCol())];
            double cost = gCost + costOfH(toVisit.get(i), goalRow, getCol);
            if (cost < minCost) {
                minCost = cost;
                result = toVisit.get(i);
            }
        }

        return result;
    }

    // Returns heuristic cost (h(n)) from cell to [goalRow, goalCol]
    private double costOfH(Cell b, int goalRow, int goalCol) {
        //Heuristic: No. of moves equal to difference in the row and column values
        double movementCost = (Math.abs(goalCol - b.getCol()) + Math.abs(goalRow - b.getRow())) * RobotConstants.MOVE_COST;

        if (movementCost == 0) return 0;

        //Heuristic: If b not in the same row or column, 1 turn needed
        double turnCost = 0;
        if (goalCol - b.getCol() != 0 || goalRow - b.getRow() != 0) {
            turnCost = RobotConstants.TURN_COST;
        }

        return movementCost + turnCost;
    }

    // Returns target direction from bot to cell
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

    // Get actual turning cost from 1 DIRECTION to another
    private double costOfTurning(DIRECTION a, DIRECTION b) {
        int numOfTurn = Math.abs(a.ordinal() - b.ordinal());
        if (numOfTurn > 2) {
            numOfTurn = numOfTurn % 2;
        }
        return (numOfTurn * RobotConstants.TURN_COST);
    }

    // Calculate the actual cost of moving from Cell a to Cell b
    private double costOfG(Cell a, Cell b, DIRECTION aDir) {
        double moveCost = RobotConstants.MOVE_COST;

        double turnCost;
        DIRECTION targetDir = getTargetDir(a.getRow(), a.getCol(), aDir, b);
        turnCost = costOfTurning(aDir, targetDir);

        return moveCost + turnCost;
    }

    // Find fastest path from the bot's current pos to [goalRow, goalCol]
    public boolean findFastestPath(int goalRow, int goalCol, boolean exploring) {
    	initAlgo(exploredMap, bot);
    	
    	System.out.print("\nCalculating fastest path from (" + current.getRow() + ", " + current.getCol() + ") to goal (" + goalRow + ", " + goalCol + ") ");
        Stack<Cell> path;
        do {

            //Get cell with minimum cost from toVisit and assign it to current
            current = getMinCostingCell(goalRow, goalCol);

            //Point the robot in the direction of current from the previous cell
            if (parents.containsKey(current)) {
                curDir = getTargetDir(parents.get(current).getRow(), parents.get(current).getCol(), curDir, current);
            }

            visited.add(current);       // add current to visited
            toVisit.remove(current);    // remove current from toVisit

            if (visited.contains(exploredMap.getCell(goalRow, goalCol))) {
                System.out.println("found: ");
                path = getPath(goalRow, goalCol);
                printFastestPath(path);
                return executePath(path, goalRow, goalCol, exploring);
            }
            
            //Setup neighbors of current cell. [Top, Bottom, Left, Right].
            if (exploredMap.checkIfCoordinatesValid(current.getRow() + 1, current.getCol())) {
                neighbours[0] = exploredMap.getCell(current.getRow() + 1, current.getCol());
                if (!canBeVisited(neighbours[0])) {
                    neighbours[0] = null;
                }
            }
            if (exploredMap.checkIfCoordinatesValid(current.getRow() - 1, current.getCol())) {
                neighbours[1] = exploredMap.getCell(current.getRow() - 1, current.getCol());
                if (!canBeVisited(neighbours[1])) {
                    neighbours[1] = null;
                }
            }
            if (exploredMap.checkIfCoordinatesValid(current.getRow(), current.getCol() - 1)) {
                neighbours[2] = exploredMap.getCell(current.getRow(), current.getCol() - 1);
                if (!canBeVisited(neighbours[2])) {
                    neighbours[2] = null;
                }
            }
            if (exploredMap.checkIfCoordinatesValid(current.getRow(), current.getCol() + 1)) {
                neighbours[3] = exploredMap.getCell(current.getRow(), current.getCol() + 1);
                if (!canBeVisited(neighbours[3])) {
                    neighbours[3] = null;
                }
            }

            //Iterate through neighbors and update their g(n) values
            for (int i = 0; i < 4; i++) {
                if (neighbours[i] != null) {
                    if (visited.contains(neighbours[i])) {
                        continue;
                    }
                    if (!(toVisit.contains(neighbours[i]))) {
                        parents.put(neighbours[i], current);
                        gCosts[neighbours[i].getRow()][neighbours[i].getCol()] = gCosts[current.getRow()][current.getCol()] + costOfG(current, neighbours[i], curDir);
                        toVisit.add(neighbours[i]);
                    } else {
                        double currentGScore = gCosts[neighbours[i].getRow()][neighbours[i].getCol()];
                        double newGScore = gCosts[current.getRow()][current.getCol()] + costOfG(current, neighbours[i], curDir);
                        if (newGScore < currentGScore) {
                            gCosts[neighbours[i].getRow()][neighbours[i].getCol()] = newGScore;
                            parents.put(neighbours[i], current);
                        }
                    }
                }
            }
        } while (!toVisit.isEmpty());

        System.out.println("not found.");
        return false;
    }

    // Generates path in reverse using parents' HashMap
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

    // Executes fastest path and returns StringBuilder with the path steps
    // Commented codes are for sending of multiple instructions in 1 message to RPi 
    private boolean executePath(Stack<Cell> path, int goalRow, int goalCol, boolean exploring) {
        StringBuilder outputString = new StringBuilder();
//        StringBuilder shortOutputString = new StringBuilder();
        Cell temp = path.pop();
        DIRECTION targetDir;
//        int bin = 0;
//        char prev = '0';

        ArrayList<MOVEMENT> movements = new ArrayList<>();

        System.out.print("Directions");
        
        while ((bot.getRobotPosRow() != goalRow) || (bot.getRobotPosCol() != goalCol)) {
            if (bot.getRobotPosRow() == temp.getRow() && bot.getRobotPosCol() == temp.getCol()) {
                temp = path.pop();
            }

            targetDir = getTargetDir(bot.getRobotPosRow(), bot.getRobotPosCol(), bot.getRobotCurDir(), temp);

            //If bot has to move backwards (To save 1 rotation)
            if(targetDir == DIRECTION.SOUTH && bot.getRobotCurDir() == DIRECTION.NORTH ||
            		targetDir == DIRECTION.NORTH && bot.getRobotCurDir() == DIRECTION.SOUTH ||
            		targetDir == DIRECTION.WEST && bot.getRobotCurDir() == DIRECTION.EAST ||
            		targetDir == DIRECTION.EAST && bot.getRobotCurDir() == DIRECTION.WEST) {
                movements.add(MOVEMENT.BACKWARD);
                outputString.append(MOVEMENT.print(MOVEMENT.BACKWARD));

//                bot.move(MOVEMENT.BACKWARD);
        		bot.move(MOVEMENT.BACKWARD, bot, exploredMap, exploring);
        		// CAN REMOVE NEXT 2 LINES OF CODES TO 'OFF' SENSORS, BUT AFFECTS EXPLORATION ALGORITHM AS IT RELIES ON FASTEST PATH
                bot.setSensors();
                bot.sense(exploredMap, realMap, bot);
                exploredMap.repaint();
            }
            //Else rotate to the right direction before moving forward
            else {
            	
	            while (bot.getRobotCurDir() != targetDir) {
	                movements.add(getTargetMove(bot.getRobotCurDir(), targetDir));
	                outputString.append(MOVEMENT.print(getTargetMove(bot.getRobotCurDir(), targetDir)));
	        		bot.move(getTargetMove(bot.getRobotCurDir(), targetDir), bot, exploredMap, exploring);
	        		// CAN REMOVE NEXT 2 LINES OF CODES TO 'OFF' SENSORS, BUT AFFECTS EXPLORATION ALGORITHM AS IT RELIES ON FASTEST PATH
	                bot.setSensors();
	                bot.sense(exploredMap, realMap, bot);
	                exploredMap.repaint();
	            }
                movements.add(MOVEMENT.FORWARD);
                outputString.append(MOVEMENT.print(MOVEMENT.FORWARD));
        		bot.move(MOVEMENT.FORWARD, bot, exploredMap, exploring);
        		// CAN REMOVE NEXT 2 LINES OF CODES TO 'OFF' SENSORS, BUT AFFECTS EXPLORATION ALGORITHM AS IT RELIES ON FASTEST PATH
                bot.setSensors();
                bot.sense(exploredMap, realMap, bot);
                exploredMap.repaint();
            }
            
//            ExplorationAlgo explalgo = new ExplorationAlgo(exploredMap, realMap, bot, 300, 3000);
//            explalgo.senseAndRepaint();
            System.out.print(" -> " + DIRECTION.print(targetDir)+ "(" + bot.getRobotPosRow() + ", " + bot.getRobotPosCol() + ")");
            
//            movements.add(MOVEMENT.FORWARD);
//            outputString.append(MOVEMENT.print(MOVEMENT.FORWARD));
//            tempB.move(MOVEMENT.FORWARD);
        }
        System.out.println("");

        // CODES THAT GENERATE 1 WHOLE STRING TO SEND, FOR ARDUINO AND ANDROID TO EXECUTE ALL MOVEMENTS AT ONCE INSTEAD OF 1 BY 1
//        System.out.println("\nInstruction string:" + outputString.toString());
//        
//        shortOutputString.append("#");
//        prev = outputString.charAt(0);
//        shortOutputString.append(prev);
//        bin++;
//	        
//        for(int i = 1; i < outputString.length(); i++) {
//        	
//        	switch (outputString.charAt(i)) {
//			case 'F':
//				if(prev == 'F') {
//					if(bin == 9) {
//						shortOutputString.append(bin);
//						shortOutputString.append('F');
//						bin = 1;
//					}
//					else{
//						bin++;
//					}
//				}
//				else {
//					shortOutputString.append(bin);
//					shortOutputString.append('F');
//					bin = 1;
//					prev = 'F';
//				}
//				break;
//			case 'L':
//				if(prev == 'L') {
//					if(bin == 9) {
//						shortOutputString.append(bin);
//						shortOutputString.append('L');
//						bin = 1;
//					}
//					else{
//						bin++;
//					}
//				}
//				else {
//					shortOutputString.append(bin);
//					shortOutputString.append('L');
//					bin = 1;
//					prev = 'L';
//				}
//				break;
//			case 'B':
//				if(prev == 'B') {
//					if(bin == 9) {
//						shortOutputString.append(bin);
//						shortOutputString.append('B');
//						bin = 1;
//					}
//					else{
//						bin++;
//					}
//				}
//				else {
//					shortOutputString.append(bin);
//					shortOutputString.append('B');
//					bin = 1;
//					prev = 'B';
//				}
//				break;
//			case 'R':
//				if(prev == 'R') {
//					if(bin == 9) {
//						shortOutputString.append(bin);
//						shortOutputString.append('R');
//						bin = 1;
//					}
//					else{
//						bin++;
//					}
//				}
//				else {
//					shortOutputString.append(bin);
//					shortOutputString.append('R');
//					bin = 1;
//					prev = 'R';
//				}
//				break;
//			default:
//				findFastestPath(MapConstants.GOAL_ROW, MapConstants.GOAL_COL);
//			}
//        }
//        
//        shortOutputString.append(bin);
//        shortOutputString.append("!");
//        
//        System.out.println("Short string:" + shortOutputString);
//
//        if(bot.getRealBot()) {
//	    	Simulator.communicator.sendMsg(shortOutputString.toString(), null);
//    	}
//    	
//    	executePathMovements(outputString.toString(), false);
    	
        return true;
    }
    
//    private boolean executePathMovements(String outputString, boolean actualFastestPath, boolean exploring){
//    	try {
//
//	        if(Simulator.actualRun) bot.setSpeed(500);
//	        
//	    	for(int i = 0; i < outputString.length(); i++) {
//		    	switch (outputString.charAt(i)) {
//				case 'F':
//					bot.move(MOVEMENT.FORWARD, bot, exploredMap, false);
//					break;
//				case 'L':
//					bot.move(MOVEMENT.LEFT, bot, exploredMap, false);
//					break;
//				case 'B':
//					bot.move(MOVEMENT.BACKWARD, bot, exploredMap, false);
//					break;
//				case 'R':
//					bot.move(MOVEMENT.RIGHT, bot, exploredMap, false);
//					break;
//				default:
//					findFastestPath(MapConstants.GOAL_ROW, MapConstants.GOAL_COL, exploring);
//					return false;
//				}
//		    	
//		    	if(!actualFastestPath) {
//		            ExplorationAlgo explalgo = new ExplorationAlgo(exploredMap, realMap, bot, 300, 3000);
//		            explalgo.senseAndRepaint();
//		    	}
//	    	}
//		} catch (Exception e) {
//			System.out.println(e.toString());
//			return false;
//		}
//
//		System.out.println("Fastest Path executed completely");
//    	return true;
//    }

    // Returns movement to execute to get from 1 direction to another
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

    // Prints fastest path from Stack
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

    // Prints all g(n) values for the cells
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