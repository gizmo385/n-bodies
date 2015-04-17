import java.util.ArrayList;

/**
 * So, like I said, this is how the book does it (not this class, but particularly the methods Universe#step(),
 * Util.calculateForce() and Particle#move()). I've noted some changes we can make for readability, but I wanted to
 * start with precisely what the book has so we can look at it and think about it.
 */
public class Bodies {

    private static final double DT = 1; // how long each time step is
    private static final int PRINT_COUNT = 15; // divisions of TIME_STEPS to print

    public static void main( String[] args ) {

        // Read command-line arguments
        final int TIME_STEPS = args.length > 0 ? Integer.parseInt(args[0]) : 15000000;
        final double BODY_SIZE  = args.length > 1 ? Double.parseDouble(args[1]) : 0.01;
        final double MASS       = args.length > 2 ? Double.parseDouble(args[2]) : 1;

        Particle p1 = new Particle(-2, 0, 0, 0, BODY_SIZE, MASS);
        Particle p2 = new Particle(2, 0, 0, 0, BODY_SIZE, MASS);
        //Particle p3 = new Particle(-0.5, 0.5, BODY_SIZE, 1);
        //Particle p4 = new Particle(0.5, 0.5, BODY_SIZE, 1);
        //Particle p1 = new Particle(randomInRange(-10, 10), randomInRange(-10, 10), BODY_SIZE, 1);
        //Particle p2 = new Particle(randomInRange(-10, 10), randomInRange(-10, 10), BODY_SIZE, 1);
        //Particle p3 = new Particle(randomInRange(-10, 10), randomInRange(-10, 10), BODY_SIZE, 1);
        //Particle p4 = new Particle(randomInRange(-10, 10), randomInRange(-10, 10), BODY_SIZE, 1);

        //particles.add(p3);
        //particles.add(p4);

        Universe u = new Universe(DT, TIME_STEPS, 1, p1, p2);

        for ( int i = 0; i < TIME_STEPS; i++ ) {
            if ( i % (TIME_STEPS / PRINT_COUNT) == 0 ) {
                System.out.printf("Time = %f:\n", i * DT);
                u.printBodies();
            }
//            u.step(DT, i);
        }
        System.out.printf("Time (seconds) = %f:\n", TIME_STEPS);
        u.printBodies();

        System.out.println("Done");
    }

    /**
     * Returns a double in the range of [min, max]
     *
     * @param min The smallest possible double that can be returned
     * @param max The largest possible number that can be returned
     */
    private static double randomInRange(double min, double max) {
        return min + (Math.random() * ((max - min) + 1));
    }
}
