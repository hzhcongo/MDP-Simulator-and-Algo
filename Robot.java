package robot;

import map.Map;
import map.MapConstants;
import robot.RobotConstants.DIRECTION;
import robot.RobotConstants.MOVEMENT;
import utils.Communicator;
import utils.MapDescriptor;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.TimeUnit;
import map.Cell;

/**
 * Represents the robot with the setup:
 *
 *        ^  ^  ^
 *        SR SR SR
 *   < SR R  R  R  SR >
 *        R  R  R
 *        R  R  R
 *
 * SR = Short Range Sensor
 * LR = Long Range Sensor
 */

public class Robot {
    private int posRow; // center cell
    private int posCol; // center cell
    private Queue<String> DirectionMoved = new LinkedList<String>();
    private DIRECTION robotDir;
    private int speed;
//    private final Sensor SRBack;
    private final Sensor SRFrontLeft;       // north-facing front-left SR
    private final Sensor SRFrontCenter;     // north-facing front-center SR
    private final Sensor SRFrontRight;      // north-facing front-right SR
    private final Sensor SRLeft;            // west-facing left SR
    private final Sensor SRRight;           // east-facing right SR
//    private final Sensor LRLeft;            // west-facing left LR
    private boolean touchedGoal;
    private final boolean realBot;
    public Cell robotPos;

    public Robot(int row, int col, boolean realBot) {
        posRow = row;
        posCol = col;
        robotDir = RobotConstants.START_DIR;
        speed = RobotConstants.SPEED;
        this.realBot = realBot;
        
//        SRBack = new Sensor(RobotConstants.SENSOR_SHORT_RANGE_L, RobotConstants.SENSOR_LONG_RANGE_H, this.posRow - 1, this.posCol, this.robotDir, "SRB");
        SRFrontLeft = new Sensor(RobotConstants.SENSOR_SHORT_RANGE_L, RobotConstants.SENSOR_SHORT_RANGE_H, this.posRow + 1, this.posCol - 1, this.robotDir, "SRFL");
        SRFrontCenter = new Sensor(RobotConstants.SENSOR_SHORT_RANGE_L, RobotConstants.SENSOR_SHORT_RANGE_H, this.posRow + 1, this.posCol, this.robotDir, "SRFC");
        SRFrontRight = new Sensor(RobotConstants.SENSOR_SHORT_RANGE_L, RobotConstants.SENSOR_SHORT_RANGE_H, this.posRow + 1, this.posCol + 1, this.robotDir, "SRFR");
        SRLeft = new Sensor(RobotConstants.SENSOR_SHORT_RANGE_L, RobotConstants.SENSOR_SHORT_RANGE_H, this.posRow, this.posCol - 1, findNewDirection(MOVEMENT.LEFT), "SRL");
        SRRight = new Sensor(RobotConstants.SENSOR_SHORT_RANGE_L, RobotConstants.SENSOR_SHORT_RANGE_H, this.posRow, this.posCol + 1, findNewDirection(MOVEMENT.RIGHT), "SRR");
//        LRLeft = new Sensor(RobotConstants.SENSOR_LONG_RANGE_L, RobotConstants.SENSOR_LONG_RANGE_H, this.posRow, this.posCol - 1, findNewDirection(MOVEMENT.LEFT), "LRL");
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

    /**
     * Rotates / moves robot according to MOVEMENT by changing its position and direction
     * Sends the movement if this.realBot is set to true
     */
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
            case CALIBRATE:
                break;
            default:
                System.out.println("Error in Robot.move()");
                break;
        }

        if (realBot) sendMovement(m, sendMoveToAndroid);
        else System.out.println("Movement done: " + MOVEMENT.print(m));

        updateTouchedGoal();
    }

    /**
     * Overloaded method
     * Calls this.move(MOVEMENT m, boolean sendMoveToAndroid = true)
     */
    public void move(MOVEMENT m) {
    	switch(m) {
    	case FORWARD:  DirectionMoved.add("F");
    					break;
    	case LEFT:		DirectionMoved.add("L");
    					break;
    	case RIGHT: 	DirectionMoved.add("R");
    					break;
    	}
        this.move(m, true);
    }

    /**
     * Sends a number instead of 'F' for multiple continuous forward movements
     */
    public void moveForwardMultiple(int count) {
        if (count == 1) {
            move(MOVEMENT.FORWARD);
        } else {
            Communicator comm = Communicator.getCommMgr();
            if (count == 10) {
                comm.sendMsg("0", Communicator.INSTRUCTIONS);
            } else if (count < 10) {
                comm.sendMsg(Integer.toString(count), Communicator.INSTRUCTIONS);
            }

            switch (robotDir) {
                case NORTH:
                    posRow += count;
                    break;
                case EAST:
                    posCol += count;
                    break;
                case SOUTH:
                    posRow += count;
                    break;
                case WEST:
                    posCol += count;
                    break;
            }

            comm.sendMsg(this.getRobotPosRow() + "," + this.getRobotPosCol() + "," + DIRECTION.print(this.getRobotCurDir()), Communicator.BOT_POS);
        }
    }

    /**
     * Uses Communicator to send next movement to robot
     */
    private void sendMovement(MOVEMENT m, boolean sendMoveToAndroid) {
        Communicator comm = Communicator.getCommMgr();
        comm.sendMsg(MOVEMENT.print(m) + "", Communicator.INSTRUCTIONS);
        if (m != MOVEMENT.CALIBRATE && sendMoveToAndroid) {
            comm.sendMsg(this.getRobotPosRow() + "," + this.getRobotPosCol() + "," + DIRECTION.print(this.getRobotCurDir()), Communicator.BOT_POS);
        }
    }

    /**
     * Sets the sensors' position and direction according to the robot's current position and direction
     */
    public void setSensors() {
        switch (robotDir) {
            case NORTH:
                SRFrontLeft.setSensor(this.posRow + 1, this.posCol - 1, this.robotDir);
                SRFrontCenter.setSensor(this.posRow + 1, this.posCol, this.robotDir);
                SRFrontRight.setSensor(this.posRow + 1, this.posCol + 1, this.robotDir);
                SRLeft.setSensor(this.posRow + 1, this.posCol - 1, findNewDirection(MOVEMENT.LEFT));
//                LRLeft.setSensor(this.posRow, this.posCol - 1, findNewDirection(MOVEMENT.LEFT));
                SRRight.setSensor(this.posRow + 1, this.posCol + 1, findNewDirection(MOVEMENT.RIGHT));
                break;
            case EAST:
                SRFrontLeft.setSensor(this.posRow + 1, this.posCol + 1, this.robotDir);
                SRFrontCenter.setSensor(this.posRow, this.posCol + 1, this.robotDir);
                SRFrontRight.setSensor(this.posRow - 1, this.posCol + 1, this.robotDir);
                SRLeft.setSensor(this.posRow + 1, this.posCol + 1, findNewDirection(MOVEMENT.LEFT));
//                LRLeft.setSensor(this.posRow + 1, this.posCol, findNewDirection(MOVEMENT.LEFT));
                SRRight.setSensor(this.posRow - 1, this.posCol + 1, findNewDirection(MOVEMENT.RIGHT));
                break;
            case SOUTH:
                SRFrontLeft.setSensor(this.posRow - 1, this.posCol + 1, this.robotDir);
                SRFrontCenter.setSensor(this.posRow - 1, this.posCol, this.robotDir);
                SRFrontRight.setSensor(this.posRow - 1, this.posCol - 1, this.robotDir);
                SRLeft.setSensor(this.posRow - 1, this.posCol + 1, findNewDirection(MOVEMENT.LEFT));
//                LRLeft.setSensor(this.posRow, this.posCol + 1, findNewDirection(MOVEMENT.LEFT));
                SRRight.setSensor(this.posRow - 1, this.posCol - 1, findNewDirection(MOVEMENT.RIGHT));
                break;
            case WEST:
                SRFrontLeft.setSensor(this.posRow - 1, this.posCol - 1, this.robotDir);
                SRFrontCenter.setSensor(this.posRow, this.posCol - 1, this.robotDir);
                SRFrontRight.setSensor(this.posRow + 1, this.posCol - 1, this.robotDir);
                SRLeft.setSensor(this.posRow - 1, this.posCol - 1, findNewDirection(MOVEMENT.LEFT));
//                LRLeft.setSensor(this.posRow - 1, this.posCol, findNewDirection(MOVEMENT.LEFT));
                SRRight.setSensor(this.posRow + 1, this.posCol - 1, findNewDirection(MOVEMENT.RIGHT));
                break;
        }
    }

    /**
     * Find new direction of the robot via current direction of robot and the given movement
     */
    private DIRECTION findNewDirection(MOVEMENT m) {
        if (m == MOVEMENT.RIGHT) {
            return DIRECTION.getNext(robotDir);
        } else {
            return DIRECTION.getPrevious(robotDir);
        }
    }

    /**
     * Sense surroundings and stores received values in integer array
     */
    public int[] sense(Map explorationMap, Map realMap) {
        int[] result = new int[6];

        if (!realBot) {
            result[0] = SRFrontLeft.sense(explorationMap, realMap);
            result[1] = SRFrontCenter.sense(explorationMap, realMap);
            result[2] = SRFrontRight.sense(explorationMap, realMap);
            result[3] = SRLeft.sense(explorationMap, realMap);
            result[4] = SRRight.sense(explorationMap, realMap);
//            result[5] = LRLeft.sense(explorationMap, realMap);
        } else {
            Communicator comm = Communicator.getCommMgr();
            String msg = comm.recvMsg();
            String[] msgArr = msg.split(";");

            if (msgArr[0].equals(Communicator.SENSOR_DATA)) {
                result[0] = Integer.parseInt(msgArr[1].split("_")[1]);
                result[1] = Integer.parseInt(msgArr[2].split("_")[1]);
                result[2] = Integer.parseInt(msgArr[3].split("_")[1]);
                result[3] = Integer.parseInt(msgArr[4].split("_")[1]);
                result[4] = Integer.parseInt(msgArr[5].split("_")[1]);
//                result[5] = Integer.parseInt(msgArr[6].split("_")[1]);
            }

            SRFrontLeft.senseReal(explorationMap, result[0]);
            SRFrontCenter.senseReal(explorationMap, result[1]);
            SRFrontRight.senseReal(explorationMap, result[2]);
            SRLeft.senseReal(explorationMap, result[3]);
            SRRight.senseReal(explorationMap, result[4]);
//            LRLeft.senseReal(explorationMap, result[5]);

            String[] mapStrings = MapDescriptor.generateMapDescriptor(explorationMap);
            comm.sendMsg(mapStrings[0] + " " + mapStrings[1], Communicator.MAP_STRINGS);
        }

        return result;
    }
}