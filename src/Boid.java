import processing.core.PApplet;
import toxi.geom.Vec3D;
import toxi.geom.mesh.WETriangleMesh;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Omar on 11/12/2016.
 */
public class Boid extends Vec3D {
    private CaveFlockVik p;
    private Vec3D vel;
    private Vec3D acc;
    private Vec3D nod = null;
    private Vec3D nod2 = null;
    private Vec3D sep = null;
    private Vec3D ali = null;
    private Vec3D coh = null;
    private Vec3D stig = null;
    private Vec3D alitr = null;
    private Vec3D vert = null;
    private Vec3D noise;
    private Vec3D cra = null;

    private Boid stigboid = null;

    private meshvertices go = null;
    private meshvertices go1 = null;

    private List<List<trail>> trailpop;

    private float maxforce;
    private float maxspeed;
    private boolean reflect = true;
    private boolean print = false;
    private boolean node = false;
    private boolean node2 = false;
    private boolean stigfollow = false;
    private boolean craw = false;
    private int cracnt = 0;
    private boolean pnoise = false;

    private int printcnt;
    public int type;
    private int trno = -1;
    private int thinkTimer = 0;

    private List friends;

    Boid(CaveFlockVik _p, Vec3D pos, int _type) {
        super(pos);
        p = _p;
        vel = new Vec3D(p.random(-p.TWO_PI, p.TWO_PI), p.random(-p.TWO_PI, p.TWO_PI), p.random(-p.TWO_PI, p.TWO_PI));
        type = _type;
        acc = new Vec3D(0, 0, 0);
        maxspeed = 4;
        if((type==7)&&(type==8))maxspeed = 1;
        maxforce = 0.77f;
        if((type==7)&&(type==8))maxforce = 0.44f;
        trailpop = new ArrayList<>();
        friends = new ArrayList<>();
        thinkTimer = (int) (p.random(10));
    }

    public void run() {
        increment();
        rules(this);
        if (thinkTimer == 0) {
            if (!p.boidoctre) getFriends(this.type, 100);
            if (p.boidoctre) friends = p.boidoctree.getPointsWithinSphere(this.copy(), 120);
        }
        flock();
        if ((p.frameCount % 3 == 0) && (p.frameCount > 20) && (print)) trail();
        update();
        borders();
    }

    private void increment() {
        thinkTimer = (thinkTimer + 1) % 5;
    }

    private void getFriends(int atype, int var1) {
        List<Boid> nearby = new ArrayList<>();
        for (int i = 0; i < p.flock.boids.size(); i++) {
            Boid test = p.flock.boids.get(i);
            if (test == this) continue;
            if (test.type == atype) {
                if (p.abs(test.x - this.x) < var1 &&
                        p.abs(test.y - this.y) < var1 &&
                        p.abs(test.z - this.z) < var1) {
                    nearby.add(test);
                }
            }
        }
        friends = nearby;
    }

    private void rules(Boid a) {

        if ((a.type == 6) && (p.frameCount < 12)) {
            trailpop.add(new ArrayList<trail>());
        }

        if (a.type == 1) {
            a.node = true;
            a.print = false;
            a.go1 = null;
            if (a.go == null) {
                a.go = seekclosestptless(0, 80);
            }
        }

        if (a.type == 2) {
            a.node2 = true;
            a.print = false;
            if (a.go == null) {
                a.go = seekclosestpt(1);
            }
        }

        if (a.type == 3) {
            a.print = true;
            a.go = null;
            if (a.go1 == null) {
                a.go1 = seekclosestptmorerange(0, 110, 700);
            }
        }

        if (a.type == 4) {
            a.print = true;
            a.stigfollow = true;
            a.go = null;
        }

        if ((a.printcnt > 40) && (a.type == 3)) {
            a.type = 1;
            a.print = false;
            a.printcnt = 0;
            if(a.cracnt>30){
                a.craw = false;
                a.cracnt = 0;
            }

        }

        if ((a.printcnt > 30) && (a.type == 4)) {
            a.type = 2;
            a.print = false;
            a.printcnt = 0;

        }

    }

    private void flockrules(Boid a) {

        if (a.type == 2) {
            if ((a.node2) && (p.start2)) a.nod2.scaleSelf(0.7f);
            a.sep.scaleSelf(0.3f);
            a.ali.scaleSelf(0.1f);
            a.coh.scaleSelf(0.07f);
        }

        if (a.type == 4) {
            a.sep.scaleSelf(0.4f);
            a.ali.scaleSelf(0.05f);
            a.coh.scaleSelf(0.007f);
            if (stigfollow) {
                a.stig.scaleSelf(0.7f);
                a.alitr.scaleSelf(0.05f);
            }
        }

        if (a.type == 3) {
            a.sep.scaleSelf(0.15f);
            a.ali.scaleSelf(0.03f);
            a.coh.scaleSelf(0.005f);
            a.nod.scaleSelf(0f);
            a.vert.scaleSelf(0.202f);
            if ((p.makecorridor) && (craw)) a.cra.scaleSelf(0.4f);
            if (pnoise) a.noise.scaleSelf(0.01f);
        }

        if (a.type == 1) {
            if (a.node) a.nod.scaleSelf(0.71f);
            a.sep.scaleSelf(0.3f);
            a.ali.scaleSelf(0.05f);
            a.coh.scaleSelf(0.02f);
        }

        if ((a.type == 7) && (a.type == 8)) {
            a.sep.scaleSelf(0.7f);
            a.ali.scaleSelf(0.5f);
            a.coh.scaleSelf(0.2f);
        }

    }

    private void flock() {


        if ((type == 1) || (type == 3)) {
            sep = separate(friends, 90.0f);
            ali = align(friends, 40.0f);
            coh = cohesion(friends, 40.0f);
        }

        if ((type == 2) || (type == 4)) {
            sep = separate(friends, 30.0f);
            ali = align(friends, 40.0f);
            coh = cohesion(friends, 30.0f);
        }

        if ((node) && (type == 1)) {
            nod = vertexseek();

        }

        if (type == 3) {
            vert = edgeseek();
            if ((p.makecorridor) && (craw)) {
                cra = crawl(p.corridor, 80, 2);
                cracnt++;
            }
            if (pnoise) noise = new Vec3D(p.random(2) - 1, p.random(2) - 1, p.random(2) - 1);
        }

        if ((node2) && (type == 2) && (p.start2)) {
            nod2 = vertexseek1();
        }

        if ((type == 4)) {
            stig = seektrail(p.flock.trailPop, 50.0f);
            alitr = aligntrail(p.flock.trailPop, 50.0f);
        }

        if ((type == 7) || (type == 8)) {
            sep = separate(friends, 90.0f);
            ali = align(friends, 40.0f);
            coh = cohesion(friends, 40.0f);
        }

        flockrules(this);

        applyForce(sep);
        applyForce(ali);
        applyForce(coh);

        if ((node) && (type == 1)) {
            applyForce(nod);
        }

        if ((node2) && (type == 2) && (p.start2)) {
            applyForce(nod2);
        }

        if ((type == 4)) {
            applyForce(stig);
            applyForce(alitr);
        }

        if (type == 3) {
            applyForce(vert);
            if ((p.makecorridor) && (craw)) applyForce(cra);
            if (pnoise) applyForce(noise);
        }
    }

    private void update() {
        vel.addSelf(acc);
        vel.limit(maxspeed);
        this.addSelf(vel);
        acc.scaleSelf(0);
    }

    private void applyForce(Vec3D force) {
        acc.addSelf(force);
    }

    private Vec3D seek(Vec3D target) {
        Vec3D desired = target.subSelf(this.copy());
        desired.normalize();
        desired.scaleSelf(maxspeed);
        Vec3D steer = desired.subSelf(vel);
        steer.limit(maxforce);
        return steer;
    }

    private void trail() {
        Vec3D tpos = new Vec3D((int) this.copy().x, (int) this.copy().y, (int) this.copy().z);
        Vec3D tvel = new Vec3D((int) this.vel.copy().x, (int) this.vel.copy().y, (int) this.vel.copy().z);
        trail tr = new trail(p, tpos, tvel);
        trailpop.get(trno).add(tr);
        if (type == 3) {
            p.flock.addTrail(tr);
        }
        printcnt++;
    }

    public void draw() {
        float theta = vel.headingXY() + p.radians(90);
        p.stroke(255);
        p.pushMatrix();
        p.translate(x, y, z);
        p.rotate(theta);
        if ((type == 1) || (type == 3)) p.obj.setFill(p.color(0, 0, 255));
        if ((type == 2) || (type == 4)) p.obj.setFill(p.color(255, 255, 255));
        if (type == 7) p.obj.setFill(p.color(255, 0, 255));
        if (type == 8) p.obj.setFill(p.color(255, 0, 0));
        if (type == 6) p.obj.setFill(p.color(0, 255, 0));
        p.obj.setStroke(100);
        p.obj.scale(1);
        p.shape(p.obj);
        p.popMatrix();

        p.noFill();
        p.strokeWeight(2);

        for (int i = 0; i < trailpop.size(); i++) {
            List<trail> a = trailpop.get(i);
            p.beginShape();
            for (int j = 0; j < a.size(); j++) {
                trail t = a.get(j);

                if ((type == 4) || (type == 2)) {
                    float lerp1 = PApplet.map(j, 0, t.trailNo, 0, 1);
                    int c1 = p.color(125, 60, 100, 255);
                    int c2 = p.color(200, 255, 50, 255);
                    int c = p.lerpColor(c1, c2, lerp1);
                    p.stroke(c);
                }

                if ((type == 3) || (type == 1)) {
                    float lerp1 = PApplet.map(j, 0, t.trailNo, 0, 1);
                    int c1 = p.color(255, 255, 255, 255);
                    int c2 = p.color(255, 255, 255, 255);
                    int c = p.lerpColor(c1, c2, lerp1);
                    p.stroke(c);
                }

                if ((type == 6)) {
                    float lerp1 = PApplet.map(j, 0, t.trailNo, 0, 1);
                    int c1 = p.color(255, 0, 0, 255);
                    int c2 = p.color(255, 10, 10, 255);
                    int c = p.lerpColor(c1, c2, lerp1);
                    p.stroke(c);
                }
                p.curveVertex(t.x, t.y, t.z);
            }
            p.endShape();
        }

    }

    private meshvertices seekclosestpt(int var1) {
        meshvertices var2 = null;
        float var3 = 3.4028235E38F;
        for (int i = 0; i < p.vertexpop.size(); i++) {
            meshvertices b = p.vertexpop.get(i);
            if (b.taken == var1) {
                float var6 = b.distanceTo(this.copy());
                if (var6 < var3) {
                    var2 = b;
                    var3 = var6;
                }
            }
        }
        return var2;
    }

    private meshvertices seekclosestptless(int var1, int slope1) {
        meshvertices var2 = null;
        float var3 = 3.4028235E38F;
        for (int i = 0; i < p.vertexpop.size(); i++) {
            meshvertices b = p.vertexpop.get(i);
            if (b.taken == var1) {
                if (b.slope < slope1) {
                    float var6 = b.distanceTo(this.copy());
                    if (var6 < var3) {
                        var2 = b;
                        var3 = var6;
                    }
                }
            }
        }
        return var2;
    }

    private meshvertices seekclosestptmore(int var1, int slope1) {
        meshvertices var2 = null;
        float var3 = 3.4028235E38F;
        for (int i = 0; i < p.vertexpop.size(); i++) {
            meshvertices b = p.vertexpop.get(i);
            if (b.taken == var1) {
                if (b.slope > slope1) {
                    float var6 = b.distanceTo(this.copy());
                    if (var6 < var3) {
                        var2 = b;
                        var3 = var6;
                    }
                }
            }
        }
        return var2;
    }

    private meshvertices seekclosestptmorerange(int var1, int slope1, int range) {
        meshvertices var2 = null;
        float var3 = 3.4028235E38F;
        for (int i = 0; i < p.vertexpop.size(); i++) {
            meshvertices b = p.vertexpop.get(i);
            if (b.taken == var1) {
                if (b.slope > slope1) {
                    float var6 = b.distanceTo(this.copy());
                    if ((var6 < var3) && (var6 > range)) {
                        var2 = b;
                        var3 = var6;
                    }
                }
            }
        }
        return var2;
    }

    private Vec3D vertexseek() {
        if (go == null) {
            go = seekclosestptless(0, 50);
        }
        float dist = go.distanceToSquared(this);
        if (dist < 64 * 64) {
            type = 3;
            craw = true;
            p.start2 = true;
            node = false;
            trailpop.add(new ArrayList<trail>());
            trno++;
            go.taken = 1;
        }
        return seek(go.copy());
    }

    private Vec3D crawl(WETriangleMesh mesh, int var1, int var2) {
        Vec3D predict = vel.copy();
        predict.normalize();
        predict.scaleSelf(var1);
        Vec3D nextPosPrev = this.copy().addSelf(predict);
        Vec3D var3 = mesh.getClosestVertexToPoint(nextPosPrev);
        Vec3D delta = var3.sub(nextPosPrev);
        if (delta.magSquared() < var2) {
            Vec3D zero = new Vec3D(0, 0, 0);
            zero.scaleSelf(3.0f);
            return zero;
        } else {
            delta.normalize();
            delta.scaleSelf(3.0f);
            return delta;
        }
    }

    private Vec3D edgeseek() {
        if (go1 == null) {
            go1 = seekclosestptmorerange(0, 110, 200);
        }
        float dist = go1.distanceToSquared(this);
        if (dist < 64 * 64) {
            type = 1;
            node = true;
            go1.taken = 3;
        }
        return seek(go1.copy());
    }

    private Vec3D vertexseek1() {

        Vec3D a1 = new Vec3D(0, 0, 0);

        if (go == null) {
            go = seekclosestpt(1);
        }

        if (go != null) {
            float dist = go.distanceToSquared(this);

            if (dist < 500 * 500) {
                a1 = seek(go.copy());
            } else go = null;

            if (dist < 55 * 55) {
                type = 4;
                trailpop.add(new ArrayList<trail>());
                trno++;
                node2 = false;
                go.takencnt++;
                stigboid = go.boid;
                meshvertices c = seekclosestpt(0);
                c.taken = 3;
            }
        }
        return a1;
    }

    private Vec3D separate(List<Boid> boids, float var1) {
        float desiredseparation = var1 * var1;
        Vec3D steer = new Vec3D(0, 0, 0);
        int count = 0;
        for (Boid other : boids) {
            float d = this.copy().distanceToSquared(other);
            if ((d > 0) && (d < desiredseparation)) {
                Vec3D diff = this.copy().subSelf(other);
                diff.normalize();
                diff.scaleSelf(1 / d);
                steer.addSelf(diff);
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

    private Vec3D align(List<Boid> boids, float var1) {
        float neighbordist = var1 * var1;
        Vec3D sum = new Vec3D(0, 0, 0);
        int count = 0;
        for (Boid other : boids) {
            if (other == this) continue;
            float d = this.copy().distanceToSquared(other);
            if ((d < neighbordist)) {
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

    private Vec3D cohesion(List<Boid> boids, float var1) {
        float neighbordist = var1 * var1;
        Vec3D sum = new Vec3D(0, 0, 0);
        int count = 0;
        for (Boid other : boids) {
            if (other == this) continue;
            float d = this.copy().distanceToSquared(other);
            if ((d < neighbordist)) {
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

    private Vec3D seektrail(List<trail> tPop, float var1) {
        float neighbordist = var1;
        Vec3D sum = new Vec3D(0, 0, 0);
        int count = 0;

        for (int i = 0; i < tPop.size(); i++) {
            trail t = tPop.get(i);
            if (p.abs(t.x - this.x) < neighbordist && p.abs(t.y - this.y) < neighbordist && p.abs(t.x - this.x) < neighbordist) {
                sum.addSelf(t.copy());
                count++;
            }
        }

        if (count > 0) {
            sum.scaleSelf(1 / (float) count);
            return seek(sum);
        }

        return sum;
    }

    private Vec3D aligntrail(List<trail> tPop, float var1) {
        Vec3D sum = new Vec3D(0, 0, 0);
        int count = 0;
        float neighbordist = var1 * var1;
        for (int i = 0; i < tPop.size(); i++) {
            trail t = tPop.get(i);
            float dist = this.distanceToSquared(t);
            if ((dist > 0) && (dist < neighbordist)) {
                sum.addSelf(t.orientation);
                count++;
            }
        }
        if (count > 0) {
            sum.scaleSelf(1 / (float) count);
            sum.normalize();
            sum.scaleSelf(maxspeed);
            return sum;
        } else {
            return new Vec3D(0, 0, 0);
        }
    }

    private boolean inView(Vec3D target, float angle) {
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

    private void borders() {
        List<Vec3D> cavepoints = null;
        cavepoints = p.meshoctree.getPointsWithinSphere(this.copy(), 60);

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
                        float dista = vara.distanceToSquared(this.copy());
                        if (dista < var3) {
                            var1 = vara;
                            var3 = dista;
                        }
                    }

                    Vec3D norm = p.Normal.get(var1);
                    norm.scaleSelf(-1);
                    float velnorm = vel.copy().dot(norm.normalize());
                    Vec3D refl1 = norm.normalize().scaleSelf(velnorm);
                    vel = vel.copy().addSelf(norm);

                    if ((type == 7) || (type == 8)) {
                        vel = vel.copy().subSelf(refl1.scaleSelf(6.0f));
                    } else {
                        vel = vel.copy().subSelf(refl1.scaleSelf(3.0f));
                    }


                }
            }
        }
    }

    public void checkMesh() {

        Vec3D cavept = p.cave.getClosestVertexToPoint(this);
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