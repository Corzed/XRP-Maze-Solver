package frc.robot;

import edu.wpi.first.wpilibj.TimedRobot;
import edu.wpi.first.wpilibj.xrp.XRPMotor;
import edu.wpi.first.wpilibj.AnalogInput;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import java.util.ArrayList;
import java.util.List;

public class Robot extends TimedRobot {
    private final XRPMotor m_leftMotor = new XRPMotor(0);
    private final XRPMotor m_rightMotor = new XRPMotor(1);
    private AnalogInput m_ultraSonic = new AnalogInput(2);
    private AnalogInput m_leftSensor = new AnalogInput(0);
    private AnalogInput m_rightSensor = new AnalogInput(1);

    private ArrayList<String> movements = new ArrayList<>();
    private List<String> savedMovements = new ArrayList<>();
    private boolean mazeComplete = false;

    // For replaying movements
    private int replayIndex = 0;

    // Autonomous mode selector
    private SendableChooser<String> autonChooser = new SendableChooser<>();
    private static final String AUTON_MODE_SOLVE = "Solve Maze";
    private static final String AUTON_MODE_REPLAY = "Replay Last Maze";

    @Override
    public void robotInit() {
        // Setup autonomous chooser
        autonChooser.setDefaultOption("Solve Maze", AUTON_MODE_SOLVE);
        autonChooser.addOption("Replay Last Maze", AUTON_MODE_REPLAY);
        SmartDashboard.putData("Autonomous Mode", autonChooser);

        // Load saved movements if available
        loadMovements();
    }

    @Override
    public void autonomousInit() {
        String selectedAuton = autonChooser.getSelected();
        if (AUTON_MODE_REPLAY.equals(selectedAuton) && !savedMovements.isEmpty()) {
            // Prepare to replay
            replayIndex = 0;
            System.out.println("Starting Replay Mode");
        } else {
            // Prepare to solve the maze
            movements.clear();
            mazeComplete = false;
            System.out.println("Starting Solve Mode");
        }
    }

    @Override
    public void autonomousPeriodic() {
        String selectedAuton = autonChooser.getSelected();
        if (AUTON_MODE_REPLAY.equals(selectedAuton) && !savedMovements.isEmpty()) {
            // Replay mode
            if (replayIndex < savedMovements.size()) {
                String command = savedMovements.get(replayIndex);
                executeCommand(command);
                replayIndex++;
                // Implement timing control if necessary
            } else {
                stopMotors();
                System.out.println("Replay Complete!");
            }
        } else {
            // Solve mode
            if (!mazeComplete) {
                double distanceMillimeters = m_ultraSonic.getVoltage() * (4000.0 / 5.0);
                double leftReflectance = m_leftSensor.getVoltage();
                double rightReflectance = m_rightSensor.getVoltage();

                // Check for white tape to end the maze
                if (leftReflectance <= 1.0 || rightReflectance <= 1.0) {
                    mazeComplete = true;
                    System.out.println("Maze Complete!");
                    stopMotors();
                    saveMovements();
                    return;
                }

                // Navigation logic
                if (distanceMillimeters >= 200) {
                    moveForward();
                    movements.add("FORWARD");
                } else if (distanceMillimeters <= 50) {
                    backup();
                    movements.add("BACKUP");
                } else {
                    turnRight();
                    movements.add("RIGHT");
                }
            }
        }
    }

    private void moveForward() {
        m_leftMotor.set(1);
        m_rightMotor.set(-1);
    }

    private void backup() {
        m_leftMotor.set(-1);
        m_rightMotor.set(1);
    }

    private void turnRight() {
        m_leftMotor.set(-1);
        m_rightMotor.set(-1);
    }

    private void stopMotors() {
        m_leftMotor.set(0);
        m_rightMotor.set(0);
    }

    private void executeCommand(String command) {
        switch (command) {
            case "FORWARD":
                moveForward();
                break;
            case "BACKUP":
                backup();
                break;
            case "RIGHT":
                turnRight();
                break;
            default:
                stopMotors();
                break;
        }
    }

    @Override
    public void disabledInit() {
        String selectedAuton = autonChooser.getSelected();
        if (AUTON_MODE_SOLVE.equals(selectedAuton) && mazeComplete) {
            saveMovements();
        }
    }

    private void saveMovements() {
        // Optimize movements before saving
        savedMovements = optimizeMovements(new ArrayList<>(movements));
        System.out.println("Optimized Movements saved: " + savedMovements);
        // To persist, implement file saving or other storage mechanisms
    }

    private List<String> optimizeMovements(List<String> originalMovements) {
        List<String> optimized = new ArrayList<>();
        for (String move : originalMovements) {
            if (move.equals("BACKUP")) {
                // Combine or skip redundant moves
                continue;
            }
            optimized.add(move);
        }
        return optimized;
    }

    private void loadMovements() {
        // For demonstration, assume no prior saved movements
        // Implement file loading or other storage retrieval
        savedMovements = new ArrayList<>();
    }
}
