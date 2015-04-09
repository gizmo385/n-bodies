import java.util.List;

/**
 * A step listener is something that will listen for the completition of individual time steps from
 * the Universe and publish those results in some way.
 */
public interface StepListener {

    /**
     * This is called when the calculation of a step has finished. Upon completion, the GUI will be
     * free to draw to new positions for the particles in the Universe.
     *
     * @param particlesAfterStep The list of particles in the Universe after the step has been
     * completed
     */
    public void finishStep(List<Particle> particlesAfterStep);
}
