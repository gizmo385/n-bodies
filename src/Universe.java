import java.util.List;
import java.util.ArrayList;

public class Universe {

    private List<Particle> bodies;
    private List<StepListener> registeredStepListeners;
    private static boolean hasCollided = false;

    public Universe(List<Particle> bodies) {
        this.registeredStepListeners = new ArrayList<>();
        this.bodies = bodies;
    }

    public void moveParticles(double seconds) {
        // Calculate the force between the particles
        for ( int i = 0; i < bodies.size() - 1; i++ ) {
            for ( int j = i+1; j < bodies.size(); j++ ) {
                // Calculate forces between the particles
                if( ! hasCollided ) {
                    Utils.calculateForce(bodies.get(i), bodies.get(j));
                }
            }
        }

        hasCollided = false;
        // Move the particles
        for ( Particle body : bodies ) {
            body.move(seconds);
        }
    }

    public void handleCollisions(double seconds, int currentStep) {
        for( int i = 0; i < bodies.size() - 1; i++ ) {
            for( int j = i + 1; j < bodies.size(); j++ ) {
                Particle p1 = bodies.get(i);
                Particle p2 = bodies.get(j);

                /*
                 * If the distance between the centers of the particles is less than the sum of the
                 * radiuses, then the particles are "within" one another. This means that collision
                 * has occured.
                 */
                if( Utils.colliding(p1, p2) ) {
                    hasCollided = true;
                    System.out.printf("Reporting collision between %d and %d @ %f seconds\n", i, j,
                            currentStep * seconds );

                    // Print out particle information before throwing the particles off the screen
                    System.out.printf("[Before] Body %d: x = %f, y = %f, vx = %.10f units/sec, vy = %.10f units/sec\n",
                            i, bodies.get(i).posX, bodies.get(i).posY, bodies.get(i).velocityX,
                            bodies.get(i).velocityY);
                    System.out.printf("[Before] Body %d: x = %f, y = %f, vx = %.10f units/sec, vy = %.10f units/sec\n",
                            j, bodies.get(j).posX, bodies.get(j).posY, bodies.get(j).velocityX,
                            bodies.get(j).velocityY);

                    // Break the universe
                    Utils.collide(p1, p2);

                    // Print out particle information before throwing the particles off the screen
                    System.out.printf("[After] Body %d: x = %f, y = %f, vx = %.10f units/sec, vy = %.10f units/sec\n",
                            i, bodies.get(i).posX, bodies.get(i).posY, bodies.get(i).velocityX,
                            bodies.get(i).velocityY);
                    System.out.printf("[After] Body %d: x = %f, y = %f, vx = %.10f units/sec, vy = %.10f units/sec\n",
                            j, bodies.get(j).posX, bodies.get(j).posY, bodies.get(j).velocityX,
                            bodies.get(j).velocityY);
                    System.out.println();

                }
            }
        }
    }

    public List<Particle> getBodies() {
        return bodies;
    }

    public void addStepListener( StepListener listener ) {
        registeredStepListeners.add(listener);
    }

    public void notifyListeners(int step) {
        registeredStepListeners.stream().forEach(l -> l.finishStep(step, bodies));
    }

    public void step(double seconds, int currentStep) {
        moveParticles(seconds);
        handleCollisions(seconds, currentStep);
        notifyListeners(currentStep);
    }

    public void printBodies() {
        for ( int i = 0; i < bodies.size(); i++ ) {
            System.out.printf("Body %d: x = %f, y = %f, vx = %f units/sec, vy = %f units/sec\n",
                    i, bodies.get(i).posX, bodies.get(i).posY, bodies.get(i).velocityX,
                    bodies.get(i).velocityY);
        }
        System.out.println();
    }
}
