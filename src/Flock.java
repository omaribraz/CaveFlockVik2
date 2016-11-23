import java.util.ArrayList;
import java.util.List;

/**
 * Created by Vikram on 11/12/2016.
 */
public class Flock {
    private CaveFlockVik p;
    List<Boid> boids;
    List<trail> trailPop;

    Flock(CaveFlockVik _p) {
        p = _p;
        boids = new ArrayList<Boid>();
        trailPop = new ArrayList<trail>();
    }

    void run() {
        for (Boid b : boids) {
            b.run();
            b.draw();
        }
    }

    void addBoid( Boid b) {
        boids.add(b);
    }

    void removeBoid( Boid b) {
        boids.remove(b);
    }

    void addTrail( trail t) {
        trailPop.add(t);
    }

    void removeTrail( trail t) {
        trailPop.remove(t);
    }
}
