import javax.swing.*;
import javax.swing.border.Border;

import java.awt.Color;
import java.awt.GridLayout;
import java.awt.FlowLayout;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;

import java.util.List;
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
    private int zoomFactor = 10;

    // The particle being highlighted in the GUI
    protected int highlightedParticle = -1;

    // Universe Constants
    protected static final double BODY_SIZE = 1;
    protected static final double MASS = 1;
    protected static final double DT = 1;
    protected static final int TIME_STEPS = 3500000;
    protected static final int NUM_WORKERS = 1;
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
        super.setResizable( false );

        panel.add( this.drawingCanvas );
        panel.add( this.controlPanel );
        super.add(panel);
    }

    @Override
    public void finishStep(int step, List<Particle> particlesAfterStep) {
        updatePanel(particlesAfterStep);
    }

    public void updatePanel(List<Particle> particles) {
        SwingUtilities.invokeLater(() -> this.drawingCanvas.postParticles(particles));
    }

    public static void main(String[] args) {
        Particle p1 = new Particle(-5, 5, 0, 0, BODY_SIZE, MASS);
        Particle p2 = new Particle(-5, -5, 0, 0, BODY_SIZE, MASS);
        Particle p3 = new Particle(5, -5, 0, 0, BODY_SIZE, MASS);
        Particle p4 = new Particle(5, 5, 0, 0, BODY_SIZE, MASS);
        Particle p5 = new Particle(-3, 3, 0, 0, BODY_SIZE, MASS);
        Particle p6 = new Particle(-3, -3, 0, 0, BODY_SIZE, MASS);
        Particle p7 = new Particle(3, -3, 0, 0, BODY_SIZE, MASS);
        Particle p8 = new Particle(3, 3, 0, 0, BODY_SIZE, MASS);
        Particle p9 = new Particle(7, 7, 0, 0, BODY_SIZE, MASS);
        Particle p10 = new Particle(-7, 7, 0, 0, BODY_SIZE, MASS);
        Particle p11 = new Particle(-7, -7, 0, 0, BODY_SIZE, MASS);
        Particle p12 = new Particle(7, -7, 0, 0, BODY_SIZE, MASS);

        Universe universe = new Universe(DT, TIME_STEPS, NUM_WORKERS, p1, p2, p3, p4, p5, p6, p7,
                p8, p9, p10, p11, p12);

        BodiesFrame bf = new BodiesFrame(universe);
        bf.setVisible(true);

        long startTime = System.nanoTime();
        universe.start(NUM_WORKERS);
        long totalTime = System.nanoTime() - startTime;
        System.out.printf("Finished\nRunning for %d time steps with %d workers took %d.%d seconds",
                TIME_STEPS, NUM_WORKERS, (long)(totalTime / 1e9), (long)(totalTime % 1e9));
    }

    private class DrawingPanel extends JPanel {

        private Queue<List<Particle>> particleDrawingQueue = new LinkedList<>();

        private final int WIDTH, HEIGHT;

        public DrawingPanel(int width, int height) {
            this.WIDTH = width;
            this.HEIGHT = height;

            this.setSize(this.WIDTH, this.HEIGHT);
            this.addMouseWheelListener(wheelEvent -> {
                zoomFactor = Math.max(wheelEvent.getUnitsToScroll() + zoomFactor, 1);
                controlPanel.setZoomSlider(zoomFactor);
            });
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

                    } else {
                        g2.fillOval(x, y, size, size);
                    }
                }
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

            zoomSlider = new JSlider(JSlider.HORIZONTAL, 0, 100, zoomFactor);
            zoomSlider.setValue(zoomFactor);
            zoomSlider.setMajorTickSpacing(20);
            zoomSlider.setMinorTickSpacing(5);
            zoomSlider.setPaintTicks(true);
            zoomSlider.setPaintLabels(true);
            zoomSlider.addChangeListener(event -> zoomFactor = zoomSlider.getValue());
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
        }

        @Override
        public Dimension getPreferredSize() {
            return new Dimension(this.WIDTH, this.HEIGHT);
        }
    }
}
