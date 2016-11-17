import processing.core.PApplet;
import toxi.geom.ReadonlyVec3D;
import toxi.geom.Vec3D;
import toxi.geom.mesh.Vertex;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by Vikram on 11/12/2016.
 */
public class Boid extends Vec3D {
    private CaveFlockVik p;
    Vec3D vel;
    Vec3D acc;
    Vec3D nod = null;
    Vec3D nod2 = null;
    Vec3D sep = null;
    Vec3D ali = null;
    Vec3D coh = null;
    Vec3D stig = null;

    public ArrayList<trail> trailPop;

    float maxforce;
    float maxspeed;
    boolean reflect = true;
    boolean print = false;
    boolean node = false;
    boolean node2 = false;

    int popsize;
    int printcnt;
    int type;
    int bouncecnt;

    Boid(CaveFlockVik _p, Vec3D pos, Vec3D _vel, int _type) {
        super(pos);
        p = _p;
        vel = _vel;
        type = _type;
        acc = new Vec3D(0, 0, 0);
        maxspeed = 2;
        maxforce = 0.07f;
        trailPop = new ArrayList<>();
    }

    void rules(Boid a) {
        if (a.type == 1) {
            a.node = true;
        }

        if (a.type == 2) {
            a.node2 = true;
        }

        if (a.type == 3) {
            a.print = true;
        }

        if (a.type == 4) {
            a.print = true;
        }

        if ((a.printcnt > 80)&&(type==3)) {
            a.type = 1;
            a.print = false;
            a.printcnt = 0;
        }

        if ((a.printcnt > 70)&&(type==4)) {
            a.type = 2;
            a.print = false;
            a.printcnt = 0;
        }

    }

    void flockrules(Boid a) {
        if (a.type == 2) {
            if ((a.node2) && (p.start2)) a.nod2.scaleSelf(7.1f);
            a.sep.scaleSelf(6.0f);
            a.ali.scaleSelf(0.6f);
            a.coh.scaleSelf(0.1f);
        }

        if (a.type == 4) {
            a.sep.scaleSelf(6.0f);
            a.ali.scaleSelf(0.4f);
            a.coh.scaleSelf(0.1f);
            a.stig.scaleSelf(6.0f);
        }

        if (a.type == 3) {
            a.sep.scaleSelf(6.0f);
            a.ali.scaleSelf(0.6f);
            a.coh.scaleSelf(0.1f);
            a.nod.scaleSelf(0f);
        }

        if (a.type == 1) {
            if (a.node) a.nod.scaleSelf(7.1f);
            a.sep.scaleSelf(6.0f);
            a.ali.scaleSelf(0.5f);
            a.coh.scaleSelf(0.2f);
        }

    }


    void run() {
        rules(this);
        flock();
        if ((p.frameCount % 3 == 0) && (p.frameCount > 20) && (print)) trail();
        update();
        borders();
        if (p.frameCount > 25) render();
    }

    void flock() {

        List boidpos = null;
        if ((type == 1) || (type == 3)) boidpos = p.boidoctree.getPointsWithinSphere(this.copy(), 900);
        if ((type == 2) || (type == 4)) boidpos = p.boidoctree.getPointsWithinSphere(this.copy(), 120);
        popsize = 0;



        if (boidpos != null) {

            sep = separate(boidpos);
            ali = align(boidpos);
            coh = cohesion(boidpos);
            if ((node) && (type == 1)) nod = vertexseek();
            if ((node2) && (type == 2) && (p.start2)) nod2 = vertexseek1();
            stig = seektrail(p.flock.trailPop);

            flockrules(this);


            applyForce(sep);
            applyForce(ali);
            applyForce(coh);
            if ((node) && (type == 1)) applyForce(nod);
            if ((node2) && (type == 2) && (p.start2)) applyForce(nod2);
            applyForce(stig);
        }
    }

    void update() {

        vel.addSelf(acc);
        vel.limit(maxspeed);
        this.addSelf(vel);
        acc.scaleSelf(0);
    }

    void applyForce(Vec3D force) {
        acc.addSelf(force);
    }

    Vec3D seek(Vec3D target) {
        Vec3D desired = target.subSelf(this);
        desired.normalize();
        desired.scaleSelf(maxspeed);
        Vec3D steer = desired.subSelf(vel);
        steer.limit(maxforce);
        return steer;
    }

    void trail() {

        if (type == 3) {
            trail tr = new trail(p, this.copy(), vel.copy());
            p.flock.addTrail(tr);
        }
        if (type == 4) {
            trail tr = new trail(p, this.copy(), vel.copy());
            trailPop.add(tr);
        }
        printcnt++;
    }

    void trailupdate() {
        p.noFill();
        p.strokeWeight(2);
        p.beginShape();
        for (int i = 0; i < trailPop.size(); i++) {
            trail t = trailPop.get(i);
            t.update();
            float lerp1 = PApplet.map(t.strength, 0, t.trailNo, 0, 1);
            int c1 = p.color(60, 120, 255, 20);
            int c2 = p.color(255, 165, 0, 255);
            int c = p.lerpColor(c1, c2, lerp1);
            p.stroke(c);
            p.curveVertex(t.x, t.y, t.z);
        }
        p.endShape();
    }

    void render() {
        float theta = vel.headingXY() + p.radians(90);

        p.stroke(255);
        p.pushMatrix();
        p.translate(x, y, z);
        p.rotate(theta);
        if (type == 1) p.obj.setFill(p.color(0, 0, 255));
        if (type == 3) p.obj.setFill(p.color(0, 255, 255));
        if (type == 2) p.obj.setFill(p.color(255, 255, 255));
        if (type == 4) p.obj.setFill(p.color(255, 0, 255));
        p.obj.setStroke(100);
        p.obj.scale(1);
        p.shape(p.obj);
        p.popMatrix();
    }

    Vec3D seekclosestpt(int var1) {
        Vec3D var2 = null;
        float var3 = 3.4028235E38F;
        for (int i = 0; i < p.vertexpop.size(); i++) {
            meshvertices b = p.vertexpop.get(i);
            if (b.taken == var1) {
                float var6 = b.distanceTo(this);
                if (var6 < var3) {
                    var2 = b.copy();
                    var3 = var6;
                }
            }
        }
        return var2;
    }


    Vec3D vertexseek() {
        Vec3D b = seekclosestpt(0);
        float varb = b.distanceTo(this);
        if ((varb < 52)) {
            type = 3;
            p.start2 = true;
            node = false;
            p.vertexhash.get(b).taken = 1;
        }

        return seek(b);

    }

    Vec3D vertexseek1() {
        Vec3D b = seekclosestpt(1);
        float varb = b.distanceTo(this);
        if (varb < 54) {
            type = 4;
            node2 = false;
            Vec3D c = seekclosestpt(0);
            p.vertexhash.get(c).taken = 1;
        }

        return seek(b);

    }


    // Separation
    Vec3D separate(List<Boid> boids) {
        float desiredseparation = 60.0f * 60.0f;
        Vec3D steer = new Vec3D(0, 0, 0);
        int count = 0;
        for (Boid other : boids) {
            float d = this.distanceToSquared(other);
            if ((d > 0) && (d < desiredseparation)) {
                Vec3D diff = this.sub(other);
                diff.normalize();
                diff.scaleSelf(1 / d);
                steer.add(diff);
                count++;
            }
        }
        if (count > 0) {
            steer.scaleSelf(1 / (float) count);
        }
        if (steer.magnitude() > 0) {
            steer.normalize();
            steer.scaleSelf(maxspeed);
            steer.subSelf(vel);
            steer.limit(maxforce);
        }
        return steer;
    }

    // Alignment
    Vec3D align(List<Boid> boids) {
        float neighbordist = 20.0f * 20.0f;
        Vec3D sum = new Vec3D(0, 0, 0);
        int count = 0;
        for (Boid other : boids) {
            float d = this.distanceToSquared(other);
            if ((d > 0) && (d < neighbordist)) {
                sum.addSelf(other.vel);
                count++;
            }
        }
        if (count > 0) {
            sum.scaleSelf(1 / (float) count);
            sum.normalize();
            sum.scaleSelf(maxspeed);
            Vec3D steer = sum.subSelf(vel);
            steer.limit(maxforce);
            return steer;
        } else {
            return new Vec3D(0, 0, 0);
        }
    }

    // Cohesion
    Vec3D cohesion(List<Boid> boids) {
        float neighbordist = 20.0f * 20.0f;
        Vec3D sum = new Vec3D(0, 0, 0);
        int count = 0;
        for (Boid other : boids) {
            float d = this.distanceToSquared(other);
            if ((d > 0) && (d < neighbordist)) {
                sum.addSelf(other);
                count++;
            }
        }
        if (count > 0) {
            sum.scaleSelf(1 / (float) count);
            return seek(sum);
        } else {
            return new Vec3D(0, 0, 0);
        }
    }

    Vec3D seektrail(ArrayList tPop) {
        float neighbordist = 2000;
        Vec3D sum = new Vec3D(0, 0, 0);
        int count = 0;

        for (int i = 0; i < tPop.size(); i++) {
            trail t = (trail) tPop.get(i);
            float distance = this.distanceTo(t);
            if ((distance < neighbordist)&& (inView(t, 120)) ) {
                sum.addSelf(t);
                count++;
            }
        }
        if (count > 0) {
            sum.scaleSelf(1 / (float) count);
            return seek(sum);
        }
        return sum;
    }

    boolean inView(Vec3D target, float angle) {
        boolean resultBool;
        Vec3D vec = target.copy().subSelf(this.copy());
        float result = vel.copy().angleBetween(vec);
        result = p.degrees(result);
        if (result < angle) {
            resultBool = true;
        } else {
            resultBool = false;
        }
        return resultBool;
    }

    // Wraparound
    void borders() {
        List<Vec3D> cavepoints = null;
        cavepoints = p.meshoctree.getPointsWithinSphere(this.copy(), 50);

        if (cavepoints != null) {

            if (cavepoints.size() > 0) {
                if (!reflect) {
                    vel.scaleSelf(-3);
                }
                if (reflect) {
                    Vec3D var1 = null;
                    float var3 = 3.4028235E38F;
                    for (int i = 0; i < cavepoints.size(); i++) {
                        Vec3D vara = cavepoints.get(i);
                        float dista = vara.distanceToSquared(this);
                        if (dista < var3) {
                            var1 = vara;
                            var3 = dista;
                        }
                    }
                    Vec3D norm = p.Normal.get(var1);
                    norm.scaleSelf(-1);
                    float velnorm = vel.dot(norm.normalize());
                    Vec3D refl1 = norm.normalize().scaleSelf(velnorm);
                    vel = vel.addSelf(norm);
                    vel = vel.subSelf(refl1.scaleSelf(1.0f));
                    vel = vel.subSelf(refl1.scaleSelf(1.5f));

                }
            }
        }
    }

    void checkMesh() {

        Vec3D cavept = p.cave2.getClosestVertexToPoint(this);
        float distpt = cavept.distanceToSquared(this);

        Vec3D a1 = cavept.copy().subSelf(this);
        Vec3D a2 = p.Normal.get(cavept);


        float ang = a2.angleBetween(a1, true);
        float ang2 = p.degrees(ang);
        if (ang2 > 90) {
            p.flock.removeBoid(this);
        }

        if (distpt < 55 * 55) {
            p.flock.removeBoid(this);
        }
    }

}
