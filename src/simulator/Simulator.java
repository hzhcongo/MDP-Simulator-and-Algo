package simulator;

import algorithms.ExplorationAlgo;
import algorithms.FastestPathAlgo;
import map.Map;
import map.MapConstants;
import robot.Robot;
import robot.RobotConstants;
import robot.RobotConstants.DIRECTION;
import utils.Communicator;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import static utils.MDFGenerator.generateMapDescriptor;
import static utils.MDFGenerator.loadMap;

/**
 * Simulator + initiator of actual bot traversing through arena
 * @author Heng Ze Hao
 */
public class Simulator {
    public static final Communicator communicator = Communicator.getCommunicator();
    
    private static JFrame mainJFrame = null;         // application JFrame
    private static JPanel mapCardsJPanel = null;	// JPanel for map views
    private static JPanel buttonsJPanel = null;		// JPanel for buttons

    private static Map actualMap = null;            // actual map
    private static Map exploredMap = null;          // exploration map
    
    private static int timeLimit = 240000;          // time limit in ms (4mins). Should take less time to goHome()
    private static int coverageLimit = 300;         // coverage limit

    private static Robot robot;						// robot for simulation

    public static final boolean actualRun = true;
    private static String msg = "";   

    public static int wrow = -1;   
    public static int wcol = -1;   
    
    // Initializes maps and displays simulator
    public static void main(String[] args) {

        robot = new Robot(RobotConstants.START_ROW, RobotConstants.START_COL, actualRun);
        robot.setRobotDir(DIRECTION.EAST);
        
        if(actualRun) 
        	robot.setSpeed(0); // Set robot speed to 0 if it's an actual run
        
        actualMap = new Map(robot);
        actualMap.setAllUnexplored();
        exploredMap = new Map(robot);
        exploredMap.setAllUnexplored();

        displaySimulator();

        if (actualRun) {	// If it's actual run and not a simulation, setup connection and await for instructions
        	communicator.openConnection();

        	while (true) {

//              COMMENT OUT BELOW LINE FOR NON-ANDROID DRY RUN
        		msg = communicator.recvMsg();
//        		msg = "0";
        		
	            switch (msg) {
	    		case "0":
	    			// Start exploration
	    			System.out.println("Android started exploration");
	    			CardLayout cl = ((CardLayout) mapCardsJPanel.getLayout());
	                cl.show(mapCardsJPanel, "EXPLORATION");
	
	                int row = RobotConstants.START_ROW;
	                int col = RobotConstants.START_COL;
	
	                robot.setRobotPos(row, col);
	
	                ExplorationAlgo exploration;
	                exploration = new ExplorationAlgo(exploredMap, actualMap, robot, coverageLimit, timeLimit);
	                exploration.runExploration();
	    			
	                break;
	    		case "1":
	    			// Start Fastest Path
	    			System.out.println("Start Fastest Path");
	    			robot.setRobotPos(RobotConstants.START_ROW, RobotConstants.START_COL);
	                exploredMap.repaint();
	
	                FastestPathAlgo fastestPath;
	                fastestPath = new FastestPathAlgo(exploredMap, robot, actualMap, false);
	                
	                if(wrow != -1 || wcol != -1)
	                    fastestPath.findFastestPath(wrow, wcol, false);
	                fastestPath.findFastestPath(RobotConstants.GOAL_ROW, RobotConstants.GOAL_COL, false);
	
	    			break;
	    		default:
	    			//Set Waypoint by splitting a-x-y
	    			System.out.println("Start waypoint mark");
	    			try {
		                String[] waypoints = msg.split("-");
		                wrow = Integer.parseInt(waypoints[1]);
		                wcol = Integer.parseInt(waypoints[2]);
		    			System.out.println(wrow + " " + wcol);
		    			break;
	    			}
	    			catch(NullPointerException e) {
	        			System.out.println("NullPointerException");
	    			}
	    			catch(IndexOutOfBoundsException e) {
	        			System.out.println("IndexOutOfBoundsException");
	    			}
	    			catch(Exception e) {
	        			System.out.println(e.toString());
	    			}
	    		}
        	}
        }
    }

    // Initializes and display simulator
    private static void displaySimulator() {
        // Initialize main display frame
        mainJFrame = new JFrame();
        mainJFrame.setTitle("MDP Algorithm Simulator");
        mainJFrame.setSize(new Dimension(468, 785));
        mainJFrame.setResizable(false);

        // Center window
        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        mainJFrame.setLocation(dim.width / 2 - mainJFrame.getSize().width / 2, dim.height / 2 - mainJFrame.getSize().height / 2);

        // Create CardLayout to store the different maps
        mapCardsJPanel = new JPanel(new CardLayout());

        // Create JPanel for buttons
        buttonsJPanel = new JPanel(new GridLayout(0,2));

        // Add mapCards & buttons to main frame
        Container contentPane = mainJFrame.getContentPane();
        contentPane.add(mapCardsJPanel, BorderLayout.CENTER);
        contentPane.add(buttonsJPanel, BorderLayout.PAGE_END);

        // Initialize main map view
        initMainLayout();

        // Add buttons
        addButtons();

        // Display application
        mainJFrame.setVisible(true);
        mainJFrame.setResizable(true);
        mainJFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    }

    // Initializes main map view by adding the different maps using CardLayout cards
    private static void initMainLayout() {
        if (!actualRun) {
            mapCardsJPanel.add(actualMap, "REAL_MAP");
        }
        mapCardsJPanel.add(exploredMap, "EXPLORATION");

        CardLayout mapCardLayout = ((CardLayout) mapCardsJPanel.getLayout());
        if (!actualRun) {
            mapCardLayout.show(mapCardsJPanel, "REAL_MAP");
        } else {
            mapCardLayout.show(mapCardsJPanel, "EXPLORATION");
        }
    }

    // Format JButtons with colors
    private static void formatButton(JButton btn) {
        btn.setFont(new Font("Arial", Font.BOLD, 16));
        btn.setFocusPainted(true);
        btn.setBackground(Color.pink);
        btn.setForeground(Color.black);
    }

    // Initializes window and add JButtons, and creates classes for multithreading and mousePressed invokers JDialogs for buttons
    private static void addButtons() {
        if (!actualRun) {
            // Load Map Button
            JButton loadMapBtn = new JButton("Load map");
            formatButton(loadMapBtn);
            
            loadMapBtn.addMouseListener(new MouseAdapter() {
                public void mousePressed(MouseEvent e) {
                    JDialog loadMapDialog = new JDialog(mainJFrame, "Select map from file", true);
                    loadMapDialog.setSize(400, 100);
                    loadMapDialog.setLayout(new FlowLayout());

                    // Center window
                    Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
                    loadMapDialog.setLocation(dim.width / 2 - loadMapDialog.getSize().width / 2, dim.height / 2 - loadMapDialog.getSize().height / 2);

                    final JTextField loadTF = new JTextField(15);
                    JButton loadMapButton = new JButton("Select map");
                    formatButton(loadMapButton);

                    loadMapButton.addMouseListener(new MouseAdapter() {
                        public void mousePressed(MouseEvent e) {
                            loadMapDialog.setVisible(false);
                            loadMap(actualMap, loadTF.getText());
                            CardLayout cl = ((CardLayout) mapCardsJPanel.getLayout());
                            cl.show(mapCardsJPanel, "REAL_MAP");
                            actualMap.repaint();
                        }
                    });

                    loadMapDialog.add(new JLabel("File Name: "));
                    loadMapDialog.add(loadTF);
                    loadMapDialog.add(loadMapButton);
                    loadMapDialog.setVisible(true);
                }
            });
            buttonsJPanel.add(loadMapBtn);
        }

        // FastestPath Class that executes fastest path algorithm
        class FastestPath extends SwingWorker<Integer, String> {
            protected Integer doInBackground() throws Exception {
                robot.setRobotPos(RobotConstants.START_ROW, RobotConstants.START_COL);
                exploredMap.repaint();

                if (actualRun) {
                    while (true) {
                        String msg = communicator.recvMsg();
                    }
                }

                FastestPathAlgo fastestPath;
                fastestPath = new FastestPathAlgo(exploredMap, robot, actualMap, false);
                if(wrow > 0 || wcol > 0 ) fastestPath.findFastestPath(wrow, wcol, false);
                fastestPath.findFastestPath(RobotConstants.GOAL_ROW, RobotConstants.GOAL_COL, false);

                return 222;
            }
        }

        // Exploration Class that executes exploration algorithm
        class Exploration extends SwingWorker<Integer, String> {
            protected Integer doInBackground() throws Exception {
                int row, col;

                row = RobotConstants.START_ROW;
                col = RobotConstants.START_COL;

                robot.setRobotPos(row, col);
                exploredMap.repaint();

                ExplorationAlgo exploration;
                exploration = new ExplorationAlgo(exploredMap, actualMap, robot, coverageLimit, timeLimit);

                exploration.runExploration();
                generateMapDescriptor(exploredMap);

                if (actualRun) {
                    new FastestPath().execute();
                }

                return 111;
            }
        }

        //Exploration Button
        JButton btn_Exploration = new JButton("Explore map");
        formatButton(btn_Exploration);
        btn_Exploration.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                CardLayout cl = ((CardLayout) mapCardsJPanel.getLayout());
                cl.show(mapCardsJPanel, "EXPLORATION");
                new Exploration().execute();
            }
        });
        buttonsJPanel.add(btn_Exploration);

        //Fastest Path Button
        JButton btn_FastestPath = new JButton("Find fastest path");
        formatButton(btn_FastestPath);
        btn_FastestPath.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                CardLayout cl = ((CardLayout) mapCardsJPanel.getLayout());
                cl.show(mapCardsJPanel, "EXPLORATION");
                new FastestPath().execute();
            }
        });
        buttonsJPanel.add(btn_FastestPath);

        //TimeExploration Class that executes exploration algorithm with time limit
        class TimeExploration extends SwingWorker<Integer, String> {
            protected Integer doInBackground() throws Exception {
                robot.setRobotPos(RobotConstants.START_ROW, RobotConstants.START_COL);
                exploredMap.repaint();

                ExplorationAlgo timeExplo = new ExplorationAlgo(exploredMap, actualMap, robot, coverageLimit, timeLimit);
                timeExplo.runExploration();

                generateMapDescriptor(exploredMap);

                return 333;
            }
        }

        //Time-limited Exploration Button
        JButton btn_TimeExploration = new JButton("Explore with time limit");
        formatButton(btn_TimeExploration);
        btn_TimeExploration.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                JDialog timeExploDialog = new JDialog(mainJFrame, "Explore with time limit", true);
                timeExploDialog.setSize(400, 100);
                timeExploDialog.setLayout(new FlowLayout());
                final JTextField timeTF = new JTextField(5);
                JButton timeSaveButton = new JButton("Run");

                // Center window
                   Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
                   timeExploDialog.setLocation(dim.width / 2 - timeExploDialog.getSize().width / 2, dim.height / 2 - timeExploDialog.getSize().height / 2);

                timeSaveButton.addMouseListener(new MouseAdapter() {
                    public void mousePressed(MouseEvent e) {
                        timeExploDialog.setVisible(false);
                        String time = timeTF.getText();
                        String[] timeArr = time.split(":");
                        timeLimit = (Integer.parseInt(timeArr[0]) * 60) + Integer.parseInt(timeArr[1]);
                        CardLayout cl = ((CardLayout) mapCardsJPanel.getLayout());
                        cl.show(mapCardsJPanel, "EXPLORATION");
                        new TimeExploration().execute();
                    }
                });

                timeExploDialog.add(new JLabel("Time Limit (in MM:SS): "));
                timeExploDialog.add(timeTF);
                timeExploDialog.add(timeSaveButton);
                timeExploDialog.setVisible(true);
            }
        });
        buttonsJPanel.add(btn_TimeExploration);

        //CoverageExploration Class that executes exploration algorithm with coverage limit
        class CoverageExploration extends SwingWorker<Integer, String> {
            protected Integer doInBackground() throws Exception {
                robot.setRobotPos(RobotConstants.START_ROW, RobotConstants.START_COL);
                exploredMap.repaint();

                ExplorationAlgo coverageExplo = new ExplorationAlgo(exploredMap, actualMap, robot, coverageLimit, timeLimit);
                coverageExplo.runExploration();

                generateMapDescriptor(exploredMap);

                return 444;
            }
        }

        //Coverage-limited Exploration Button
        JButton btn_CoverageExploration = new JButton("Explore with coverage limit");
        formatButton(btn_CoverageExploration);
       
        btn_CoverageExploration.addMouseListener(new MouseAdapter() {
        	public void mousePressed(MouseEvent e) {
    			JDialog coverageExploDialog = new JDialog(mainJFrame, "Explore with coverage limit", true);
                coverageExploDialog.setSize(400, 100);
                coverageExploDialog.setLayout(new FlowLayout()); 
                final JTextField coverageTF = new JTextField(5);
                JButton coverageSaveButton = new JButton("Run");

                // Center window
                Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
                coverageExploDialog.setLocation(dim.width / 2 - coverageExploDialog.getSize().width / 2, dim.height / 2 - coverageExploDialog.getSize().height / 2);

                coverageSaveButton.addMouseListener(new MouseAdapter() {
                    public void mousePressed(MouseEvent e) {
                        coverageExploDialog.setVisible(false);
                        coverageLimit = (int) ((Integer.parseInt(coverageTF.getText())) * MapConstants.MAP_CELLS / 100.0);
                        new CoverageExploration().execute();
                        CardLayout cl = ((CardLayout) mapCardsJPanel.getLayout());
                        cl.show(mapCardsJPanel, "EXPLORATION");
                    }
                });

                coverageExploDialog.add(new JLabel("Coverage limit (%): "));
                coverageExploDialog.add(coverageTF);
                coverageExploDialog.add(coverageSaveButton);
                coverageExploDialog.setVisible(true);
            }
        });
        buttonsJPanel.add(btn_CoverageExploration);
        
        //Coverage-limited Exploration Button
        JButton btn_changeBotSpeed = new JButton("Change robot speed");
        formatButton(btn_changeBotSpeed);
       
        btn_changeBotSpeed.addMouseListener(new MouseAdapter() {
        	public void mousePressed(MouseEvent e) {
                System.out.println("Initial steps per second: " + 1000 / RobotConstants.SPEED + ". Initial speed in ms: " + RobotConstants.SPEED);
    			JDialog botSpeedDialog = new JDialog(mainJFrame, "Change robot speed", true);
                botSpeedDialog.setSize(400, 100);
                botSpeedDialog.setLayout(new FlowLayout()); 
                final JTextField botSpeedTF = new JTextField(5);
                JButton botSpeedSaveButton = new JButton("Change");

                // Center window
                Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
                botSpeedDialog.setLocation(dim.width / 2 - botSpeedDialog.getSize().width / 2, dim.height / 2 - botSpeedDialog.getSize().height / 2);

                botSpeedSaveButton.addMouseListener(new MouseAdapter() {
                    public void mousePressed(MouseEvent e) {
                        botSpeedDialog.setVisible(false);
                        RobotConstants.SPEED = 1000 / Integer.parseInt(botSpeedTF.getText());
                        robot.setSpeed(RobotConstants.SPEED);
                        System.out.println("Updated steps per second: " + botSpeedTF.getText() + ". Updated speed in ms: " + RobotConstants.SPEED);
                    }
                });

                botSpeedDialog.add(new JLabel("Robot Speed (steps per second): "));
                botSpeedDialog.add(botSpeedTF);
                botSpeedDialog.add(botSpeedSaveButton);
                botSpeedDialog.setVisible(true);
            }
        });
        buttonsJPanel.add(btn_changeBotSpeed);
    }
}