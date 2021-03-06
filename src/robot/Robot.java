package robot;

import map.Map;
import map.MapConstants;
import robot.RobotConstants.DIRECTION;
import robot.RobotConstants.MOVEMENT;
import simulator.Simulator;
import utils.MDFGenerator;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.TimeUnit;
import map.Cell;

/**
 * Sensor class for a sensor object
 *        ^   ^   ^
 *        SS  SS  SS
 *   < LS --------- SS >
 *        |  bot  |
 *        --------- SS >
 */
public class Robot {
    private int posRow; // center cell of bot
    private int posCol; // center cell of bot
    private Queue<String> DirectionMoved = new LinkedList<String>();
    private DIRECTION robotDir;
    private int speed;
    private final Sensor SRFrontLeft;       // north-facing front-left SR
    private final Sensor SRFrontCenter;     // north-facing front-center SR
    private final Sensor SRFrontRight;      // north-facing front-right SR
    private final Sensor LRLeft;            // west-facing left SR
    private final Sensor SRRight;           // east-facing right SR at top right corner
    private final Sensor SRRight2;          // east-facing right SR at bottom right corner
    private boolean touchedGoal;
    private final boolean realBot;
    public Cell robotPos;

    //Initialize robot and sensors
    public Robot(int row, int col, boolean realBot) {
        posRow = row;
        posCol = col;
        robotDir = RobotConstants.START_DIR;
        speed = RobotConstants.SPEED;
        this.realBot = realBot;
        
        SRFrontLeft = new Sensor(RobotConstants.SENSOR_SHORT_RANGE_L, RobotConstants.SENSOR_SHORT_RANGE_H, this.posRow + 1, this.posCol - 1, this.robotDir, "SRFL");
        SRFrontCenter = new Sensor(RobotConstants.SENSOR_SHORT_RANGE_L, RobotConstants.SENSOR_SHORT_RANGE_H, this.posRow + 1, this.posCol, this.robotDir, "SRFC");
        SRFrontRight = new Sensor(RobotConstants.SENSOR_SHORT_RANGE_L, RobotConstants.SENSOR_SHORT_RANGE_H, this.posRow + 1, this.posCol + 1, this.robotDir, "SRFR");
        LRLeft = new Sensor(RobotConstants.SENSOR_LONG_RANGE_L, RobotConstants.SENSOR_LONG_RANGE_H, this.posRow, this.posCol - 1, findNewDirection(MOVEMENT.LEFT), "LRL");
        SRRight = new Sensor(RobotConstants.SENSOR_SHORT_RANGE_L, RobotConstants.SENSOR_SHORT_RANGE_H, this.posRow + 1, this.posCol + 1, findNewDirection(MOVEMENT.RIGHT), "SRR");
        SRRight2 = new Sensor(RobotConstants.SENSOR_SHORT_RANGE_L, RobotConstants.SENSOR_SHORT_RANGE_H, this.posRow - 1, this.posCol + 1, findNewDirection(MOVEMENT.RIGHT), "SRR2");
    }

    public void setRobotPos(int row, int col) {
        posRow = row;
        posCol = col;
    }

    public int getRobotPosRow() {
        return posRow;
    }

    public int getRobotPosCol() {
        return posCol;
    }

    public void setRobotDir(DIRECTION dir) {
        robotDir = dir;
    }

    public void setSpeed(int speed) {
        this.speed = speed;
    }

    public DIRECTION getRobotCurDir() {
        return robotDir;
    }

    public boolean getRealBot() {
        return realBot;
    }

    private void updateTouchedGoal() {
        if (this.getRobotPosRow() == MapConstants.GOAL_ROW && this.getRobotPosCol() == MapConstants.GOAL_COL)
            this.touchedGoal = true;
    }

    public boolean getTouchedGoal() {
        return this.touchedGoal;
    }

    // Rotates / moves bot according to MOVEMENT by changing its position and direction
    // Sends movement if this.realBot true
    public void move(MOVEMENT m, boolean sendMoveToAndroid) {
        if (!realBot) {
            // Simulate actual movement sleeping
            try {
                TimeUnit.MILLISECONDS.sleep(speed);
            } catch (InterruptedException e) {
                System.out.println("Error in Robot.move()");
            }
        }

        switch (m) {
            case FORWARD:
                switch (robotDir) {
                    case NORTH:
                        posRow++;
                        break;
                    case EAST:
                        posCol++;
                        break;
                    case SOUTH:
                        posRow--;
                        break;
                    case WEST:
                        posCol--;
                        break;
                }
                break;
            case BACKWARD:
                switch (robotDir) {
                    case NORTH:
                        posRow--;
                        break;
                    case EAST:
                        posCol--;
                        break;
                    case SOUTH:
                        posRow++;
                        break;
                    case WEST:
                        posCol++;
                        break;
                }
                break;
            case RIGHT:
            case LEFT:
                robotDir = findNewDirection(m);
                break;
            default:
                System.out.println("Error in Robot.move()");
                break;
        }
        updateTouchedGoal();
    }

    // Move method for exploration execution
    // Moves robot, prints output of movement/s, and send message to RPi
    public void move(MOVEMENT m, Robot bot, Map exploredMap, boolean exploring) {
    	switch(m) {
    	case FORWARD:  DirectionMoved.add("F");
    					break;
    	case BACKWARD:  DirectionMoved.add("B");
						break;
    	case LEFT:		DirectionMoved.add("L");
    					break;
    	case RIGHT: 	DirectionMoved.add("R");
    					break;
    	case ERROR: 	System.out.println("MOVEMENT.ERROR provided to move()");
						break;
    	}
        this.move(m, true);
        
        String[] mapStrings = MDFGenerator.generateMapDescriptor(exploredMap);
        String output;
        
//        if(exploring) {
////            output = "@" + MOVEMENT.print(m) + "-" + bot.getRobotPosCol() + "-"
////            		+ bot.getRobotPosRow() + "-" + RobotConstants.DIRECTION.print(bot.getRobotCurDir()) + "-" 
////            		+ mapStrings[0] + "-" + mapStrings[1] + "-" ;
//            output = MOVEMENT.print(m) + "-" + bot.getRobotPosCol() + "-"
//            		+ bot.getRobotPosRow() + "-" + RobotConstants.DIRECTION.print(bot.getRobotCurDir()) + "-" 
//            		+ mapStrings[0] + "-" + mapStrings[1] + "-" ;
//        }
//        else {
//            output = Character.toString(MOVEMENT.print(m));
//        }

        output = MOVEMENT.print(m) + "-" + bot.getRobotPosCol() + "-"
        		+ bot.getRobotPosRow() + "-" + RobotConstants.DIRECTION.print(bot.getRobotCurDir()) + "-" 
        		+ mapStrings[0] + "-" + mapStrings[1] + "-" ;
        
    	if(bot.getRealBot()) {
	    	Simulator.communicator.sendMsg(output, null);
    	}
    }
   
    // Move method for fastest path execution
    // Moves robot, prints output of movement/s, and send message to RPi
    public void move(MOVEMENT m) {
    	switch(m) {
    	case FORWARD:  DirectionMoved.add("F");
    					break;
    	case BACKWARD:  DirectionMoved.add("B");
						break;
    	case LEFT:		DirectionMoved.add("L");
    					break;
    	case RIGHT: 	DirectionMoved.add("R");
    					break;
    	case ERROR: 	System.out.println("MOVEMENT.ERROR provided to move()");
						break;
    	}
        this.move(m, true);
    }

    // Set sensors' position and direction according to the robot's current position and direction
    public void setSensors() {
        switch (robotDir) {
            case NORTH:
                SRFrontLeft.setSensor(this.posRow + 1, this.posCol - 1, this.robotDir);
                SRFrontCenter.setSensor(this.posRow + 1, this.posCol, this.robotDir);
                SRFrontRight.setSensor(this.posRow + 1, this.posCol + 1, this.robotDir);
                LRLeft.setSensor(this.posRow + 1, this.posCol - 1, findNewDirection(MOVEMENT.LEFT));
                SRRight.setSensor(this.posRow + 1, this.posCol + 1, findNewDirection(MOVEMENT.RIGHT));
                SRRight2.setSensor(this.posRow - 1, this.posCol + 1, findNewDirection(MOVEMENT.RIGHT));
                break;
            case EAST:
                SRFrontLeft.setSensor(this.posRow + 1, this.posCol + 1, this.robotDir);
                SRFrontCenter.setSensor(this.posRow, this.posCol + 1, this.robotDir);
                SRFrontRight.setSensor(this.posRow - 1, this.posCol + 1, this.robotDir);
                LRLeft.setSensor(this.posRow + 1, this.posCol + 1, findNewDirection(MOVEMENT.LEFT));
                SRRight.setSensor(this.posRow - 1, this.posCol + 1, findNewDirection(MOVEMENT.RIGHT));
                SRRight2.setSensor(this.posRow - 1, this.posCol - 1, findNewDirection(MOVEMENT.RIGHT));
                break;
            case SOUTH:
                SRFrontLeft.setSensor(this.posRow - 1, this.posCol + 1, this.robotDir);
                SRFrontCenter.setSensor(this.posRow - 1, this.posCol, this.robotDir);
                SRFrontRight.setSensor(this.posRow - 1, this.posCol - 1, this.robotDir);
                LRLeft.setSensor(this.posRow - 1, this.posCol + 1, findNewDirection(MOVEMENT.LEFT));
                SRRight.setSensor(this.posRow - 1, this.posCol - 1, findNewDirection(MOVEMENT.RIGHT));
                SRRight2.setSensor(this.posRow + 1, this.posCol - 1, findNewDirection(MOVEMENT.RIGHT));
                break;
            case WEST:
                SRFrontLeft.setSensor(this.posRow - 1, this.posCol - 1, this.robotDir);
                SRFrontCenter.setSensor(this.posRow, this.posCol - 1, this.robotDir);
                SRFrontRight.setSensor(this.posRow + 1, this.posCol - 1, this.robotDir);
                LRLeft.setSensor(this.posRow - 1, this.posCol - 1, findNewDirection(MOVEMENT.LEFT));
                SRRight.setSensor(this.posRow + 1, this.posCol - 1, findNewDirection(MOVEMENT.RIGHT));
                SRRight2.setSensor(this.posRow + 1, this.posCol + 1, findNewDirection(MOVEMENT.RIGHT));
                break;
        }
    }

    // Find new direction of the bot via current direction of robot and the given movement
    private DIRECTION findNewDirection(MOVEMENT m) {
        if (m == MOVEMENT.RIGHT) {
            return DIRECTION.getNext(robotDir);
        } else {
            return DIRECTION.getPrevious(robotDir);
        }
    }

    // Sense surroundings and stores received values in integer array
    public int[] sense(Map explorationMap, Map realMap, Robot bot) {
        int[] result = new int[6];
    	String msg1 = "";
    	
        if (!realBot) {
	        result[0] = SRFrontLeft.sense(explorationMap, realMap);
	        result[1] = SRFrontCenter.sense(explorationMap, realMap);
	        result[2] = SRFrontRight.sense(explorationMap, realMap);
	        result[3] = LRLeft.sense(explorationMap, realMap);
	        result[4] = SRRight.sense(explorationMap, realMap);
            result[5] = SRRight2.sense(explorationMap, realMap);
        } 
        else {

			System.out.println("Getting start flag of sensor data");
        	while(true) {
        		msg1 = Simulator.communicator.recvMsg();
        		if(msg1.hashCode() != 0 && msg1.charAt(0) == '*') 
    			{
        			System.out.println("Start flag recieved from Arduino");
        			break;
    			}
        	}

			System.out.print("Calculating sensor data: ");
			
            for(int i = 1; i < 7; i++) {
                if(msg1.charAt(i) == 'D') result[i-1] = 4;
                else if(msg1.charAt(i) == 'C') result[i-1] = 3;
                else if(msg1.charAt(i) == 'B') result[i-1] = 2;
                else if(msg1.charAt(i) == 'A') result[i-1] = 1;
                else result[i-1] = 0;
    			System.out.print(result[i-1]);
            }
            
            System.out.println();
            
            LRLeft.senseReal(explorationMap, result[0]);
            SRFrontLeft.senseReal(explorationMap, result[1]);
            SRFrontCenter.senseReal(explorationMap, result[2]);
            SRFrontRight.senseReal(explorationMap, result[3]);
            SRRight.senseReal(explorationMap, result[4]);
            SRRight2.senseReal(explorationMap, result[5]);
        }
        
        return result;
    }
}