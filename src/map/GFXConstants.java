package map;

import java.awt.*;

/**
 * Color Constants for Cells, Map, Robot etc
 * @author Heng Ze Hao
 */
class GFXConstants {
    public static final int CELL_LINE_WEIGHT = 2;

    public static final Color COLOR_START = Color.GREEN;
    public static final Color COLOR_GOAL = Color.RED;
    public static final Color COLOR_UNEXPLORED = Color.LIGHT_GRAY;
    public static final Color COLOR_FREE = Color.WHITE;
    public static final Color COLOR_OBSTACLE = Color.BLACK;
    public static final Color COLOR_WAYPOINT = Color.PINK;

    public static final Color COLOR_ROBOT = Color.BLUE;
    public static final Color COLOR_ROBOT_DIR = Color.GREEN;

    public static final int ROBOT_WIDTH = 70;
    public static final int ROBOT_HEIGHT = 70;

    public static final int ROBOT_X_OFFSET = 10;
    public static final int ROBOT_Y_OFFSET = 20;

    public static final int ROBOT_DIR_W = 10;
    public static final int ROBOT_DIR_H = 10;

    public static final int CELL_SIZE = 30;

    public static final int MAP_H = 610;
}