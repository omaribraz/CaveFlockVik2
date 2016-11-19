/**
 * Created by Vikram on 11/12/2016.
 */

import toxi.geom.PointOctree;
import toxi.geom.Vec3D;


public class Octree extends PointOctree {
    private CaveFlockVik p;

    Octree(CaveFlockVik _p, Vec3D o, float d) {
        super(o, d);
        p = _p;
    }

    void addBoid(Boid b) {
        addPoint(b);
    }

    void addPts(meshvertices a){addPoint(a);}

    void run() {
        updateTree();
    }

    void updateTree() {
        empty();
        for (Boid b : p.flock.boids) {
            addBoid(b);
        }
    }

    void draw() {
        drawNode(this);
        System.out.println("p = " + this.getNumChildren());
    }

    void drawNode(PointOctree n) {
        if (n.getNumChildren() > 0) {
            p.noFill();
            p.stroke(255);
            p.strokeWeight(1);
            p.pushMatrix();
            p.translate(n.x, n.y, n.z);
            p.popMatrix();
            PointOctree[] childNodes = n.getChildren();
            for (int i = 0; i < 8; i++) {
                if (childNodes[i] != null) drawNode(childNodes[i]);
            }
        }
    }


}
