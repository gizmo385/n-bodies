import java.util.List;

public class Universe {

    private List<Particle> bodies;

    public Universe(List<Particle> bodies) {
        this.bodies = bodies;
    }

    public void step(double seconds) {
        for ( int i = 0; i < bodies.size() - 1; i++ ) {
            for ( int j = i+1; j < bodies.size(); j++ ) {
                Utils.calculateForce(bodies.get(i), bodies.get(j));
            }
        }
        for ( Particle body : bodies ) {
            body.move(seconds);
        }
    }

    public void printBodies() {
        for ( int i = 0; i < bodies.size(); i++ ) {
            System.out.printf("Body %d: x = %f, y = %f, vx = %f units/sec, vy = %f units/sec\n",
                    i, bodies.get(i).posX, bodies.get(i).posY, bodies.get(i).velocityX, bodies.get(i).velocityY);
        }
        System.out.println();
    }

}
