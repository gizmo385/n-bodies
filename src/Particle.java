public class Particle {

    protected double posX;
    protected double posY;
    protected double mass;
    protected double forceX = 0.0;
    protected double forceY = 0.0;
    protected double velocityX = 0.0;
    protected double velocityY = 0.0;

    public Particle(double x, double y, double mass) {
        this.posX = x;
        this.posY = y;
        this.mass = mass;
    }

    /**
     * Moves the particle for a certain amount of time. Recalculating velocity seems to logically be another function,
     * so we can extract that.
     * @param seconds
     */
    public void move(double seconds) {
        double dvX, dvY; // delta velocity x and y
        double dpX, dpY; // delta position x and y
        dvX = this.forceX/this.mass * seconds;
        dvY = this.forceY/this.mass * seconds;
        dpX = (velocityX + dvX/2) * seconds;
        dpY = (velocityY + dvY/2) * seconds;
        this.velocityX = this.velocityX + dvX;
        this.velocityY = this.velocityY + dvY;
        this.posX = this.posX + dpX;
        this.posY = this.posY + dpY;
        this.forceX = 0.0;
        this.forceY = 0.0;
    }
}
