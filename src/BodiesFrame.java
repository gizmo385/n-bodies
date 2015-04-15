import javax.swing.*;
import javax.swing.border.Border;

import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.FlowLayout;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;

import java.util.Random;
import java.util.List;
import java.util.ArrayList;
import java.util.Vector;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Arrays;
import java.util.stream.Collectors;

public class BodiesFrame extends JFrame implements StepListener {

    // Graphics constants
    protected static final int FRAME_WIDTH = 900, FRAME_HEIGHT = 500;
    protected static final int DRAWING_WIDTH = 500, DRAWING_HEIGHT = 500;
    protected static final int CONTROLS_WIDTH = 400, CONTROLS_HEIGHT = 500;

    // Graphical values
    private int zoomFactor = 5;

    // The particle being highlighted in the GUI
    protected int highlightedParticle = -1;

    // Universe Constants
    protected static int TIME_STEPS = 3500000;
    protected static int NUM_WORKERS = 8;
    protected static double DT = 1;
    protected static double BODY_SIZE = 1;
    protected static boolean GUI = true;
    protected static final double MASS = 1;
    private static final int PRINT_COUNT = 15;

    // Simulation
    protected Universe universe;

    // Graphical components
    protected DrawingPanel drawingCanvas;
    protected ControlsPanel controlPanel;

    public BodiesFrame(Universe universe) {
        this.universe = universe;
        this.universe.addStepListener(this);
        initComponents();
        initFrame();
    }

    private void initComponents() {
        this.drawingCanvas = new DrawingPanel(DRAWING_WIDTH, DRAWING_HEIGHT);
        this.drawingCanvas.setVisible(true);

        this.controlPanel = new ControlsPanel(CONTROLS_WIDTH, CONTROLS_HEIGHT);
        this.controlPanel.setVisible(true);

        updatePanel(this.universe.getBodies());
    }

    private void initFrame() {
        super.setSize(FRAME_WIDTH, FRAME_HEIGHT);
        JPanel panel = new JPanel();
        panel.setLayout( new BoxLayout(panel, BoxLayout.X_AXIS) );

        super.setDefaultCloseOperation( EXIT_ON_CLOSE );
        super.setLocationRelativeTo( null );
        super.setTitle("N-Bodies Simulation");
        super.setResizable( false );

        panel.add( this.drawingCanvas );
        panel.add( this.controlPanel );
        super.add(panel);
    }

    @Override
    public void finishStep(int step, List<Particle> particlesAfterStep) {
        if( step % 100 == 0 ) {
            updatePanel(particlesAfterStep);
        }
    }

    public void updatePanel(List<Particle> particles) {
        SwingUtilities.invokeLater(() -> this.drawingCanvas.postParticles(particles));
    }

    public static void main(String[] args) {
        if( args.length < 4 ) {
            System.err.println("Usage: java BodiesFrame [numWorkers] [numBodies] [bodySize] [timeSteps]");
            System.exit(1);
        }

        NUM_WORKERS = Integer.parseInt(args[0]);
        int numBodies = Integer.parseInt(args[1]);
        BODY_SIZE = Double.parseDouble(args[2]);
        TIME_STEPS = Integer.parseInt(args[3]);

        if( args.length > 4 ) {
            if( "--no-gui".equals(args[4]) ) {
                GUI = false;
            }
        }

        // Create particles
        Particle[] particles = new Particle[numBodies];
        Random r = new Random();
        for( int i = 0; i < numBodies; i++ ) {
            // Generate the starting locations
            double startX, startY;
            if( i % 2 == 0 ) {
                startX = (Math.random() < .5 ? i : -i) * BODY_SIZE * 2;
                startY = Math.random() * i * (Math.random() < .5 ? 1 : -1);
            } else {
                startY = (Math.random() < .5 ? i : -i) * BODY_SIZE * 2;
                startX = Math.random() * i * (Math.random() < .5 ? 1 : -1);
            }

            Particle p = new Particle(startX, startY, 0, 0, BODY_SIZE, MASS);
            particles[i] = p;
        }

        Universe universe = new Universe(DT, TIME_STEPS, NUM_WORKERS, particles);

        if( GUI ) {
            BodiesFrame bf = new BodiesFrame(universe);
            bf.setVisible(true);
        }

        long startTime = System.nanoTime();
        universe.start(NUM_WORKERS);
        long totalTime = System.nanoTime() - startTime;
        System.out.printf("Finished\nRunning for %d time steps with %d workers took %d.%d seconds",
                TIME_STEPS, NUM_WORKERS, (long)(totalTime / 1e9), (long)(totalTime % 1e9));
    }

    private class DrawingPanel extends JPanel {

        private Queue<List<Particle>> particleDrawingQueue = new LinkedList<>();

        private final int WIDTH, HEIGHT;
        private JLabel pausedLabel;

        public DrawingPanel(int width, int height) {
            this.WIDTH = width;
            this.HEIGHT = height;

            this.setSize(this.WIDTH, this.HEIGHT);
            this.addMouseWheelListener(wheelEvent -> {
                zoomFactor = Math.max(wheelEvent.getUnitsToScroll() + zoomFactor, 1);
                controlPanel.setZoomSlider(zoomFactor);
            });

            // Indicates that the simulation is paused
            pausedLabel = new JLabel("The simulation is paused!");
            pausedLabel.setFont(new Font("Arial", 1, 14));
            pausedLabel.setForeground(Color.RED);
            pausedLabel.setVisible(false);
            add(pausedLabel);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D)g;

            List<Particle> particles = particleDrawingQueue.poll();

            if( particles != null ) {
                for( Particle p : particles ) {
                    int x = (int)Math.round(p.posX * zoomFactor) + (this.WIDTH / 2) - (int)Math.round(p.radius * (zoomFactor));
                    int y = - (int)Math.round(p.posY * zoomFactor) + (this.HEIGHT / 2) - (int)Math.round(p.radius * (zoomFactor));
                    int size = (int)Math.round(p.radius * zoomFactor * 2);


                    if( p.id == highlightedParticle ) {
                        g2.setColor(Color.YELLOW);
                        g2.fillOval(x, y, size, size);
                        g2.setColor(Color.BLACK);

                        g2.drawString( String.format("Position: (%f, %f)", p.posX, p.posY), 175, 50);
                        g2.drawString( String.format("X-Velocity: %f", p.velocityX), 175, 70);
                        g2.drawString( String.format("Y-Velocity: %f", p.velocityY), 175, 90);
                    } else {
                        g2.fillOval(x, y, size, size);
                    }
                }
            }

            if( universe.isPaused() ) {
                pausedLabel.setVisible(true);
            } else {
                pausedLabel.setVisible(false);
            }
        }

        @Override
        public Dimension getPreferredSize() {
            return new Dimension(this.WIDTH, this.HEIGHT);
        }

        public void postParticles(List<Particle> particles) {
            this.particleDrawingQueue.add(particles);
            this.repaint();
        }
    }

    private class ControlsPanel extends JPanel {

        private final int WIDTH, HEIGHT;

        private JComboBox<String> particleList;
        private JSlider zoomSlider;
        private JTextField deltaTimeAmount;
        private JButton pauseButton, unpauseButton;

        public ControlsPanel(int width, int height) {
            this.WIDTH = width;
            this.HEIGHT = height;

            this.initComponents();
            this.initPanel();
        }

        private void initComponents() {
            // Get the particleIds
            Vector<String> particleIds = universe.getBodies()
                .stream()
                .map(p -> String.valueOf(p.id))
                .collect(Collectors.toCollection(Vector::new));

            this.particleList = new JComboBox<String>(particleIds);
            this.particleList.addActionListener(event -> {
                int selectedId = Integer.parseInt(this.particleList.getSelectedItem().toString());
                highlightedParticle = selectedId;
            });

            // Slider for the zoom amount
            zoomSlider = new JSlider(JSlider.HORIZONTAL, 0, 100, zoomFactor);
            zoomSlider.setValue(zoomFactor);
            zoomSlider.setMajorTickSpacing(20);
            zoomSlider.setMinorTickSpacing(5);
            zoomSlider.setPaintTicks(true);
            zoomSlider.setPaintLabels(true);
            zoomSlider.addChangeListener(event -> zoomFactor = zoomSlider.getValue());

            // Add a text field to change the delta time amount
            deltaTimeAmount = new JTextField(15);
            deltaTimeAmount.setText( String.valueOf(DT) );
            deltaTimeAmount.addActionListener(event -> {
                try {
                    double newDT = Double.parseDouble(deltaTimeAmount.getText());
                    DT = newDT;
                    universe.setDeltaTime(newDT);
                } catch( NumberFormatException nfe ) {
                    deltaTimeAmount.setText(String.valueOf(DT));
                    JOptionPane.showMessageDialog(null, "Enter a valid delta time amount!", "Error!", JOptionPane.ERROR_MESSAGE);
                }
            });

            // Add buttons to pause and unpause the universe
            pauseButton = new JButton("Pause");
            pauseButton.addActionListener(event -> universe.pause());

            unpauseButton = new JButton("Unpause");
            unpauseButton.addActionListener(event -> universe.unpause());
        }

        public void setZoomSlider(int zoomAmount) {
            zoomSlider.setValue(zoomAmount);
        }

        private void initPanel() {
            super.setSize(this.WIDTH, this.HEIGHT);
            super.setVisible(true);

            Border lineBorder = BorderFactory.createLineBorder(Color.black, 5);
            super.setBorder( BorderFactory.createTitledBorder(lineBorder, "Controls"));

            JPanel highlightPanel = new JPanel();
            highlightPanel.add(new JLabel("Highlighted particle: "));
            highlightPanel.add(this.particleList);
            super.add(highlightPanel);

            JPanel zoomPanel = new JPanel();
            zoomPanel.add(new JLabel("Zoom factor: "));
            zoomPanel.add(zoomSlider);
            super.add(zoomPanel);

            JPanel dtPanel = new JPanel();
            dtPanel.add(new JLabel("ΔT: "));
            dtPanel.add(deltaTimeAmount);
            super.add(dtPanel);

            JPanel pausePanel = new JPanel();
            pausePanel.add(pauseButton);
            pausePanel.add(unpauseButton);
            super.add(pausePanel);
        }

        @Override
        public Dimension getPreferredSize() {
            return new Dimension(this.WIDTH, this.HEIGHT);
        }
    }
}
