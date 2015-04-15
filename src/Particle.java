public class Particle {

    protected static int particleId = 0;

    protected double posX;
    protected double posY;

    protected double mass;
    protected double radius;

    protected double forceX = 0.0;
    protected double forceY = 0.0;

    protected double velocityX = 0.0;
    protected double velocityY = 0.0;

    protected int id;

    public Particle(double x, double y, double velocityX, double velocityY, double radius, double mass) {
        this.posX = x;
        this.posY = y;
        this.velocityX = velocityX;
        this.velocityY = velocityY;
        this.radius = radius;
        this.mass = mass;
        this.id = particleId;
        particleId++;
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

    public String toString() {
        return String.format("x = %f, y = %f, vx = %f units/sec, vy = %f units/sec", posX, posY,
                velocityX, velocityY);
    }

    public synchronized void collide(Particle p2) {
        // Modify the x and y velocities for this. Equations taken from 2D collisions PDF
        double thisNewDx = (p2.velocityX * Math.pow((p2.posX - this.posX), 2) + p2.velocityY * (p2.posX
                - this.posX) * (p2.posY - this.posY) + this.velocityX * Math.pow(p2.posY - this.posY, 2)
                - this.velocityY * (p2.posX - this.posX) * (p2.posY - this.posY)) / (Math.pow(p2.posX
                - this.posX, 2) + Math.pow(p2.posY - this.posY, 2));

        double thisNewDy = (p2.velocityX * (p2.posX - this.posX) * (p2.posY - this.posY) + p2.velocityY
                * Math.pow(p2.posY - this.posY, 2) - this.velocityX * (p2.posY - this.posY) * (p2.posX
                - this.posX) + this.velocityY * Math.pow(p2.posX - this.posX, 2)) / (Math.pow(p2.posX
                - this.posX, 2) + Math.pow(p2.posY - this.posY, 2));

        // Modify the x and y velocities for p2. Equations taken from 2D collisions PDF
        double p2NewDx = (this.velocityX * Math.pow((p2.posX - this.posX), 2) + this.velocityY * (p2.posX
                - this.posX) * (p2.posY - this.posY) + p2.velocityX * Math.pow(p2.posY - this.posY, 2)
                - p2.velocityY * (p2.posX - this.posX) * (p2.posY - this.posY)) / (Math.pow(p2.posX
                - this.posX, 2) + Math.pow(p2.posY - this.posY, 2));

        double p2NewDy = (this.velocityX * (p2.posX - this.posX) * (p2.posY - this.posY) + this.velocityY
                * Math.pow(p2.posY - this.posY, 2) - p2.velocityX * (p2.posY - this.posY) * (p2.posX
                - this.posX) + p2.velocityY * Math.pow(p2.posX - this.posX, 2)) / (Math.pow(p2.posX
                - this.posX, 2) + Math.pow(p2.posY - this.posY, 2));

        // Actually modify the god damn velocities
        p2.velocityY = p2NewDy;
        p2.velocityX = p2NewDx;
        this.velocityY = thisNewDy;
        this.velocityX = thisNewDx;

    }
}
