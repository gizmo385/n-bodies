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
    protected static final double TIME_STEPS = 15000000;
    protected static final double MASS = 1;
    protected static final double DT = 1;
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

    public void startUniverse() {
        // Run the universe in a separate thread
        for ( int i = 0; ; i++ ) {
            if ( i % (TIME_STEPS / PRINT_COUNT) == 0 ) {
                System.out.printf("Time = %f:\n", i * DT);
                universe.printBodies();
            }
            universe.step(DT, i);
        }
    }

    public static void main(String[] args) {
        Particle p1 = new Particle(-5, 5, BODY_SIZE, MASS);
        Particle p2 = new Particle(-5, -5, BODY_SIZE, MASS);
        Particle p3 = new Particle(5, -5, BODY_SIZE, MASS);
        Particle p4 = new Particle(5, 5, BODY_SIZE, MASS);
        Particle p5 = new Particle(-3, 3, BODY_SIZE, MASS);
        Particle p6 = new Particle(-3, -3, BODY_SIZE, MASS);
        Particle p7 = new Particle(3, -3, BODY_SIZE, MASS);
        Particle p8 = new Particle(3, 3, BODY_SIZE, MASS);
        Universe universe = new Universe(Arrays.asList(p1, p2, p3, p4, p5, p6, p7, p8));
        BodiesFrame bf = new BodiesFrame(universe);

        bf.setVisible(true);

        bf.startUniverse();
    }

    private class DrawingPanel extends JPanel {

        private Queue<List<Particle>> particleDrawingQueue = new LinkedList<>();
        private final static int SIZE_UPSCALE_AMOUNT = 20;
        private final static int POS_UPSCALE_AMOUNT = 7;

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D)g;

            List<Particle> particles = particleDrawingQueue.poll();

            if( particles != null ) {
                for( Particle p : particles ) {
                    int x = (int)Math.round(p.posX * POS_UPSCALE_AMOUNT) + (BodiesFrame.WIDTH / 2);
                    int y = (int)Math.round(p.posY * POS_UPSCALE_AMOUNT) + (BodiesFrame.HEIGHT / 2);
                    int size = (int)Math.round(p.size * SIZE_UPSCALE_AMOUNT);

                    g2.fillOval(x, y, size, size);
                }
            }

            //this.repaint();
        }

        public void postParticles(List<Particle> particles) {
            this.particleDrawingQueue.add(particles);
            this.repaint();
        }
    }
}
