/**
 * Not sure if we need a separate class for utils, the calculateForce() would make sense in the Universe class, but I
 * put it here in case we needed more things.
 */
public class Utils {

    public static final double GRAVITY_CONSTANT = 6.67e-11;

    /**
     * The book does it this way, in its weird c-ish pseudocode. It calculates the force exerted on both particles by
     * the other. This could be split up, but we'd need to recalculate distance and magnitude for each method call,
     * which is less than optimal.
     *
     * Also to consider is whether we want to keep forceX and forceY as instance variables, and if we can just
     * cumulatively recalculate velocity between each particle. The "test" I have set up in the main method exhibits
     * the desired behavior (until they collide), so we can mess around with it and compare results.
     */
    public static void calculateForce(Particle p1, Particle p2) {
        double distance, magnitude, dirX, dirY;
        distance = Math.sqrt(Math.pow(p1.posX - p2.posX, 2) +
                             Math.pow(p1.posY - p2.posY, 2) );
        magnitude = (GRAVITY_CONSTANT * p1.mass * p2.mass) / (Math.pow(distance, 2));
        dirX = p2.posX - p1.posX;
        dirY = p2.posY - p1.posY;
        p1.forceX = p1.forceX + magnitude * dirX / distance;
        p2.forceX = p2.forceX - magnitude * dirX / distance;
        p1.forceY = p1.forceY + magnitude * dirY / distance;
        p2.forceY = p2.forceY - magnitude * dirY / distance;
    }

    public static double calculateDistance( Particle p1, Particle p2 ) {
        return Math.sqrt( Math.pow(p2.posY - p1.posY, 2) + Math.pow(p2.posX - p1.posX, 2) );
    }

    public static boolean colliding(Particle p1, Particle p2) {
        return calculateDistance(p1, p2) <= (p1.size + p2.size);
    }

    public static void collide(Particle p1, Particle p2) {
        // Modify the x and y velocities for p1. Equations taken from 2D collisions PDF
        double p1NewDx = (p2.velocityX * Math.pow((p2.posX - p1.posX), 2) + p2.velocityY * (p2.posX
                    - p1.posX) * (p2.posY - p1.posY) + p1.velocityX * Math.pow(p2.posY - p1.posY, 2)
                - p1.velocityY * (p2.posX - p1.posX) * (p2.posY - p1.posY)) / (Math.pow(p2.posX
                    - p1.posX, 2) + Math.pow(p2.posY - p1.posY, 2));

        double p1NewDy = (p2.velocityX * (p2.posX - p1.posX) * (p2.posY - p1.posY) + p2.velocityY
                * Math.pow(p2.posY - p1.posY, 2) - p1.velocityX * (p2.posY - p1.posY) * (p2.posX
                    * - p1.posX) + p1.velocityY * Math.pow(p2.posX - p1.posX, 2)) / (Math.pow(p2.posX
                        - p1.posX, 2) + Math.pow(p2.posY - p1.posY, 2));

        // Modify the x and y velocities for p2. Equations taken from 2D collisions PDF
        double p2NewDx = (p1.velocityX * Math.pow((p2.posX - p1.posX), 2) + p2.velocityY * (p2.posX
                    - p1.posX) * (p2.posY - p1.posY) + p2.velocityX * Math.pow(p2.posY - p1.posY, 2)
                - p2.velocityY * (p2.posX - p1.posX) * (p2.posY - p1.posY)) / (Math.pow(p2.posX
                    - p1.posX, 2) + Math.pow(p2.posY - p1.posY, 2));

        double p2NewDy = (p1.velocityX * (p2.posX - p1.posX) * (p2.posY - p1.posY) + p1.velocityY
                * Math.pow(p2.posY - p1.posY, 2) - p2.velocityX * (p2.posY - p1.posY) * (p2.posX
                    * - p1.posX) + p2.velocityY * Math.pow(p2.posX - p1.posX, 2)) / (Math.pow(p2.posX
                        - p1.posX, 2) + Math.pow(p2.posY - p1.posY, 2));

        // Actually modify the god damn velocities
        p2.velocityY = p2NewDy;
        p2.velocityX = p2NewDx;
        p1.velocityY = p1NewDy;
        p1.velocityX = p1NewDx;

    }
}
