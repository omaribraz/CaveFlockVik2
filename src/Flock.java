import java.util.ArrayList;

/**
 * Created by Vikram on 11/12/2016.
 */
public class Flock {
    private CaveFlockVik p;
    ArrayList<Boid> boids;
    ArrayList<trail> trailPop;

    Flock(CaveFlockVik _p) {
        p = _p;
        boids = new ArrayList<Boid>();
        trailPop = new ArrayList<trail>();
    }

    void run() {
        for (Boid b : boids) {
            b.run();
            b.trailupdate();
        }
        for (int i = 0; i<trailPop.size(); i++) {
            trail t = trailPop.get(i);
            t.render();
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
