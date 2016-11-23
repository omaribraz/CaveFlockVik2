import java.util.ArrayList;
import java.util.List;

/**
 * Created by Omar on 11/12/2016.
 */
public class Flock {
    private CaveFlockVik p;
    public List<Boid> boids;
    public List<trail> trailPop;

    Flock(CaveFlockVik _p) {
        p = _p;
        boids = new ArrayList<Boid>();
        trailPop = new ArrayList<trail>();
    }

    public void run() {
        for (Boid b : boids) {
            b.run();
            b.draw();
        }
    }

    public void addBoid( Boid b) {
        boids.add(b);
    }

    public void removeBoid( Boid b) {
        boids.remove(b);
    }

    public void addTrail( trail t) {
        trailPop.add(t);
    }

    public void removeTrail( trail t) {
        trailPop.remove(t);
    }
}
