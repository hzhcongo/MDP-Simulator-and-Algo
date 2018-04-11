package utils;

import map.Map;
import map.MapConstants;
import java.io.*;

/**
 * Generates MDF Part 1 and Part 2 strings
 * @author Heng Ze Hao
 */

public class MDFGenerator {
    
    public static void loadMap(Map map, String filename) {
    	//Reads filename.txt from disk
        try {
            InputStream input = new FileInputStream("maps/" + filename + ".txt"); 
            BufferedReader buffer = new BufferedReader(new InputStreamReader(input));

            String line = buffer.readLine();
            StringBuilder sb = new StringBuilder();
            while (line != null) {
                sb.append(line);
                line = buffer.readLine();
            }

            // Load it into a Map object
            String bin = sb.toString();
            int binPtr = 0;
            for (int row = MapConstants.MAP_ROWS - 1; row >= 0; row--) {
                for (int col = 0; col < MapConstants.MAP_COLS; col++) {
                    if (bin.charAt(binPtr) == '1') 
                    	map.setObstacleCell(row, col, true);
                    else
                    	map.setObstacleCell(row, col, false);
            		map.getCell(row, col).setIsWalked(false);
                    binPtr++;
                }
            }

            map.setAllExplored();
            buffer.close();
            
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String binToHex(String bin) {
        return Integer.toHexString(Integer.parseInt(bin, 2));
    }

    // Generate MDF strings from Map
    public static String[] generateMapDescriptor(Map map) {
        String[] ret = new String[2];
        StringBuilder Part1 = new StringBuilder();
        StringBuilder Part1_bin = new StringBuilder();
        Part1_bin.append("11");
        
        for (int r = 0; r < MapConstants.MAP_ROWS; r++) {
            for (int c = 0; c < MapConstants.MAP_COLS; c++) {
                if (map.getCell(r, c).getIsExplored())
                    Part1_bin.append("1");
                else
                    Part1_bin.append("0");

                if (Part1_bin.length() == 4) {
                    Part1.append(binToHex(Part1_bin.toString()));
                    Part1_bin.setLength(0);
                }
            }
        }
        
        Part1_bin.append("11");
        Part1.append(binToHex(Part1_bin.toString()));
        ret[0] = Part1.toString();

        StringBuilder Part2 = new StringBuilder();
        StringBuilder Part2_bin = new StringBuilder();
        for (int r = 0; r < MapConstants.MAP_ROWS; r++) {
            for (int c = 0; c < MapConstants.MAP_COLS; c++) {
                if (map.getCell(r, c).getIsExplored()) {
                    if (map.getCell(r, c).getIsObstacle())
                        Part2_bin.append("1");
                    else
                        Part2_bin.append("0");

                    if (Part2_bin.length() == 4) {
                        Part2.append(binToHex(Part2_bin.toString()));
                        Part2_bin.setLength(0);
                    }
                }
            }
        }
        
        if (Part2_bin.length() > 0) 
        	Part2.append(binToHex(Part2_bin.toString()));
        while (Part2.length() % 8 != 0) 
        	Part2.append(0); 		// Pad till same length as MDF[1]
        ret[1] = Part2.toString();

        return ret;
    }
}