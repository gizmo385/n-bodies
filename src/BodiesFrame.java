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
    //protected static final double DT = 0.1;
    protected static final double DT = 5;
    protected static final int TIME_STEPS = 3500000;
    protected static final int NUM_WORKERS = 1;
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
        Particle p1 = new Particle(5, -5, 0, 0, BODY_SIZE, MASS);
        Particle p2 = new Particle(-5, 5, 0, 0, BODY_SIZE, MASS);
        Universe universe = new Universe(DT, TIME_STEPS, NUM_WORKERS, p1, p2);
        BodiesFrame bf = new BodiesFrame(universe);
        bf.setVisible(true);

        universe.start(NUM_WORKERS);
    }

    private class DrawingPanel extends JPanel {

        private Queue<List<Particle>> particleDrawingQueue = new LinkedList<>();
        private final static int ZOOM_FACTOR = 20;

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
