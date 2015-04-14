import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import java.awt.Graphics;
import java.awt.Graphics2D;

import java.util.List;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Arrays;

public class BodiesFrame extends JFrame implements StepListener {

    // Graphics constants
    protected static final int WIDTH = 500, HEIGHT = 500;

    // Universe Constants
    protected static final double BODY_SIZE = 1;
    protected static final double MASS = 1;
    protected static final double DT = .1;
    protected static final int TIME_STEPS = 3500000;
    protected static final int NUM_WORKERS = 8;
    private static final int PRINT_COUNT = 15;

    // Simulation
    protected Universe universe;

    // Graphical components
    protected DrawingPanel drawingCanvas;

    public BodiesFrame(Universe universe) {
        this.universe = universe;
        this.universe.addStepListener(this);
        initComponents();
        initFrame();
    }

    private void initComponents() {
        this.drawingCanvas = new DrawingPanel();
        this.drawingCanvas.setSize(WIDTH, HEIGHT);
        this.drawingCanvas.setVisible(true);

        updatePanel(this.universe.getBodies());
    }

    private void initFrame() {
        super.setSize(WIDTH, HEIGHT);
        super.setLayout( null );
        super.setDefaultCloseOperation( EXIT_ON_CLOSE );
        super.setLocationRelativeTo( null );
        super.setResizable( false );

        super.add( this.drawingCanvas );
    }

    @Override
    public void finishStep(int step, List<Particle> particlesAfterStep) {
        if( step % 700 == 0 ) {
            updatePanel(particlesAfterStep);
        }
    }

    public void updatePanel(List<Particle> particles) {
        SwingUtilities.invokeLater(() -> this.drawingCanvas.postParticles(particles));
    }

    public static void main(String[] args) {
        Particle p1 = new Particle(-15, 15, .00001, -.00001, BODY_SIZE, MASS);
        Particle p2 = new Particle(-2, 7, 0, 0, BODY_SIZE, MASS);
        Particle p3 = new Particle(3, -9, 0, 0, BODY_SIZE, MASS);
        Particle p4 = new Particle(-4, 9, 0, 0, BODY_SIZE, MASS);
        Particle p5 = new Particle(5, -8, 0, 0, BODY_SIZE, MASS);
        Particle p6 = new Particle(-6, 7, 0, 0, BODY_SIZE, MASS);
        Particle p7 = new Particle(7, -6, 0, 0, BODY_SIZE, MASS);
        Particle p8 = new Particle(-9, 5, 0, 0, BODY_SIZE, MASS);
        Particle p9 = new Particle(9, -4, 0, 0, BODY_SIZE, MASS);
        Particle p10 = new Particle(-8, 3, 0, 0, BODY_SIZE, MASS);
        Particle p11 = new Particle(7, -2, 0, 0, BODY_SIZE, MASS);
        Particle p12 = new Particle(15, -15, -.00001, .00001, BODY_SIZE, MASS);
        Universe universe = new Universe(DT, TIME_STEPS, NUM_WORKERS, p1, p2, p3, p4, p5, p6, p7, p8, p9, p10, p11, p12);
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
        private final static int ZOOM_FACTOR = 2;

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D)g;

            List<Particle> particles = particleDrawingQueue.poll();

            if( particles != null ) {
                for( Particle p : particles ) {
                    int x = (int)Math.round(p.posX * ZOOM_FACTOR) + (BodiesFrame.WIDTH / 2) - (int)Math.round(p.radius * (ZOOM_FACTOR));
                    int y = - (int)Math.round(p.posY * ZOOM_FACTOR) + (BodiesFrame.HEIGHT / 2) - (int)Math.round(p.radius * (ZOOM_FACTOR));
                    int size = (int)Math.round(p.radius * ZOOM_FACTOR * 2);

                    g2.fillOval(x, y, size, size);
                }
            }
        }

        public void postParticles(List<Particle> particles) {
            this.particleDrawingQueue.add(particles);
            this.repaint();
        }
    }
}
