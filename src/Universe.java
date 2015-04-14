import java.util.List;
import java.util.Arrays;
import java.util.ArrayList;

import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;

public class Universe {

    private static final int NUM_PRINTS = 15;

    private List<Particle> bodies;
//    private CyclicBarrier barrier;

    private final int timeSteps;
    private double DT;
    private AtomicBoolean running = new AtomicBoolean(false);
    private int numWorkers;
    protected static Semaphore[][] threadBarriers;

    private List<StepListener> registeredStepListeners;
    private boolean[][] collisionMatrix;

    public Universe(double DT, int timeSteps, int numWorkers, Particle... bodies) {
        this.registeredStepListeners = new ArrayList<>();
        this.bodies = Arrays.asList(bodies);

        this.DT = DT;
        this.timeSteps = timeSteps;

        this.collisionMatrix = new boolean[bodies.length][bodies.length];
        for( int i = 0; i < collisionMatrix.length; i++ ) {
            for( int j = 0; j < collisionMatrix.length; j++ ) {
                collisionMatrix[i][j] = false;
            }
        }
    }

    public void setDeltaTime(double DT) {
        this.DT = DT;
    }

    public void start(int numWorkers) {
        this.numWorkers = numWorkers;
        Worker[] workers = new Worker[numWorkers];
        Thread[] threads = new Thread[numWorkers];
        threadBarriers = new Semaphore[4][numWorkers];
        for ( int i = 0; i < numWorkers; i++ ) {
            for ( int j = 0; j < 4; j++ ) {
                threadBarriers[j][i] = new Semaphore(0);
            }
            workers[i] = new Worker(i);
        }
        for( int i = 0; i < bodies.size(); ) {
            for ( int j = 0; j < numWorkers && i < bodies.size(); j++, i++ ) {
                workers[j].addParticle(i);
                System.out.println("Adding particle " + i + " to worker " + j);
            }
            for ( int j = numWorkers - 1; j >= 0 && i < bodies.size(); j--, i++ ) {
                workers[j].addParticle(i);
                System.out.println("Adding particle " + i + " to worker " + j);
            }
        }
        for ( int i = 0; i < numWorkers; i++ ) {
            threads[i] = new Thread(workers[i]);
            threads[i].start();
        }

        running.set(true);

        for ( int i = 0; i < numWorkers; i++ ) {
            try {
                threads[i].join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void pause() {
        running.set(false);
    }

    public boolean isPaused() {
        return ! running.get();
    }

    public void unpause() {
        running.set(true);
    }

    private void calculateForces(int particleId) {
        // Calculate the force between the particles
        for ( int i = particleId + 1; i < bodies.size(); i++ ) {
            // Calculate forces between the particles
            if( ! collisionMatrix[particleId][i] ) {
                Utils.calculateForce(bodies.get(particleId), bodies.get(i));
            } else {
                System.out.printf("Not calculating forces between %d and %d due to recent collision\n", particleId, i);
            }
            collisionMatrix[particleId][i] = false;
        }
    }

    private void moveParticles(Particle body, double seconds) {
        // Move the particles
        body.move(seconds);
    }

    private void handleCollisions(int particleId, double seconds, int currentStep) {
        for( int j = particleId + 1; j < bodies.size(); j++ ) {
            Particle p1 = bodies.get(particleId);
            Particle p2 = bodies.get(j);

            /*
             * If the distance between the centers of the particles is less than the sum of the
             * radiuses, then the particles are "within" one another. This means that collision
             * has occured.
             */
            if( Utils.colliding(p1, p2) ) {
                collisionMatrix[particleId][j] = true;
                collisionMatrix[j][particleId] = true;

                System.out.printf("Reporting collision between %d and %d @ %f seconds\n", particleId, j,
                        currentStep * seconds );

                // Print out particle information before throwing the particles off the screen
                System.out.printf("[Before] Body %d: x = %f, y = %f, vx = %.10f units/sec, vy = %.10f units/sec\n",
                        particleId, bodies.get(particleId).posX, bodies.get(particleId).posY, bodies.get(particleId).velocityX,
                        bodies.get(particleId).velocityY);
                System.out.printf("[Before] Body %d: x = %f, y = %f, vx = %.10f units/sec, vy = %.10f units/sec\n",
                        j, bodies.get(j).posX, bodies.get(j).posY, bodies.get(j).velocityX,
                        bodies.get(j).velocityY);

                // Break the universe
                Utils.collide(p1, p2);

                // Print out particle information before throwing the particles off the screen
                System.out.printf("[After] Body %d: x = %f, y = %f, vx = %.10f units/sec, vy = %.10f units/sec\n",
                        particleId, bodies.get(particleId).posX, bodies.get(particleId).posY, bodies.get(particleId).velocityX,
                        bodies.get(particleId).velocityY);
                System.out.printf("[After] Body %d: x = %f, y = %f, vx = %.10f units/sec, vy = %.10f units/sec\n",
                        j, bodies.get(j).posX, bodies.get(j).posY, bodies.get(j).velocityX,
                        bodies.get(j).velocityY);
                System.out.println();

            }
        }
    }

    public void printBodies() {
        for ( int i = 0; i < bodies.size(); i++ ) {
            System.out.printf("Body %d: x = %f, y = %f, vx = %f units/sec, vy = %f units/sec\n",
                    i, bodies.get(i).posX, bodies.get(i).posY, bodies.get(i).velocityX,
                    bodies.get(i).velocityY);
        }
        System.out.println();
    }

    private class Worker implements Runnable {

        ArrayList<Integer> myParticles = new ArrayList<>();
        int threadNum;

        public Worker(int threadNum) {
            this.threadNum = threadNum;
        }

        public void addParticle(int p) {
            myParticles.add(p);
        }

        public void run() {
            int currentStep = 0;
            while( currentStep < timeSteps ) {
                if( running.get() ) {
                    for ( int i : myParticles ) {
                        calculateForces(i);
                    }
                    barrier(this.threadNum);
                    for ( int i : myParticles ) {
                        moveParticles(bodies.get(i), DT);
                    }

                    for ( int i : myParticles ) {
                        handleCollisions(i, DT, currentStep);
                    }
                    barrier(this.threadNum);
                    if ( this.threadNum == 0 ) {
                        notifyListeners(currentStep);

                        if ( currentStep % (timeSteps / NUM_PRINTS) == 0 ) {
                            printBodies();
                        }

                    }
                    currentStep++;
                } else {
                    try {
                        Thread.sleep(100);
                    } catch( InterruptedException ie ) {
                        ie.printStackTrace();
                    }
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

    private void barrier(int threadNum) {
        for ( int i = 1, j = 0; i < numWorkers; i = i * 2, j++ ) {
            threadBarriers[j][threadNum].release();
            try {
                threadBarriers[j][(threadNum + i) % numWorkers].acquire();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

}
