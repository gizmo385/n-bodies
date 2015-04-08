import java.util.List;

public class Universe {

    private List<Particle> bodies;

    public Universe(List<Particle> bodies) {
        this.bodies = bodies;
    }

    public void moveParticles(double seconds) {
        // Calculate the force between the particles
        for ( int i = 0; i < bodies.size() - 1; i++ ) {
            for ( int j = i+1; j < bodies.size(); j++ ) {
                // Calculate forces between the particles
                Utils.calculateForce(bodies.get(i), bodies.get(j));

            }
        }

        // Move the particles
        for ( Particle body : bodies ) {
            body.move(seconds);
        }
    }

    public void handleCollisions(double seconds, int currentStep) {
        // Determine if collisions have occured between any particles
        boolean print = true;
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
                    System.out.printf("Reporting collision between %d and %d @ %f seconds\n", i, j,
                            currentStep * seconds );


                    // Print out particle information before throwing the particles off the screen
                    System.out.printf("[Before] Body %d: x = %f, y = %f, vx = %f units/sec, vy = %f units/sec\n",
                            i, bodies.get(i).posX, bodies.get(i).posY, bodies.get(i).velocityX,
                            bodies.get(i).velocityY);
                    System.out.printf("[Before] Body %d: x = %f, y = %f, vx = %f units/sec, vy = %f units/sec\n",
                            j, bodies.get(j).posX, bodies.get(j).posY, bodies.get(j).velocityX,
                            bodies.get(j).velocityY);

                    double overlap = (p1.size + p2.size) - Utils.calculateDistance(p1, p2);

                    System.out.printf("overlap = %f\n", overlap);

                    /*
                     * TODO: We make the observation that shifting solely based upon the overlap
                     * value does not produce an entirely accurate simulation. Ideally, a closed
                     * form exists that will allow us to shift based upon individual particles
                     * contribution to that overlap, however for the moment we will shift based only
                     * on the overlap amount.
                     */
                    double dirX = p2.posX - p1.posX;
                    double dirY = p2.posY - p1.posY;

                    p1.posX -= Math.signum(dirX) * (overlap / 2);
                    p1.posY -= Math.signum(dirY) * (overlap / 2);
                    p2.posX += Math.signum(dirX) * (overlap / 2);
                    p2.posY += Math.signum(dirY) * (overlap / 2);

                    // Break the universe
                    Utils.collide(p1, p2);

                    // Print out particle information before throwing the particles off the screen
                    System.out.printf("[After] Body %d: x = %f, y = %f, vx = %f units/sec, vy = %f units/sec\n",
                            i, bodies.get(i).posX, bodies.get(i).posY, bodies.get(i).velocityX,
                            bodies.get(i).velocityY);
                    System.out.printf("[After] Body %d: x = %f, y = %f, vx = %f units/sec, vy = %f units/sec\n",
                            j, bodies.get(j).posX, bodies.get(j).posY, bodies.get(j).velocityX,
                            bodies.get(j).velocityY);
                    System.out.println();

                    //if( print ) {
                        //print = false;
                        //printBodies();
                        //System.out.println();
                    //}
                }
            }
        }
    }

    public void step(double seconds, int currentStep) {
        moveParticles(seconds);
        handleCollisions(seconds, currentStep);
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
