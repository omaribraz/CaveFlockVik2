/**
 * Created by omar on 10/31/2016.
 */

import toxi.geom.ReadonlyVec3D;
import toxi.geom.Shape3D;
import toxi.geom.Sphere;
import toxi.geom.Vec3D;
import toxi.geom.mesh.Mesh3D;

import java.util.List;

import static processing.core.PApplet.degrees;

public class Pathagent extends Vec3D {
    private CaveFlockVik p;
    private Vec3D vel;
    public Sphere a;
    public Mesh3D b;
    private float dia = 60;
    private float dia2 = 120;
    int type;


    Pathagent(CaveFlockVik _p, Vec3D pos, int _type) {
        super(pos);
        p = _p;
        vel = new Vec3D(0, 0, 0);
        type = _type;
        this.z += 10;
    }

    public void run() {
        if (type == 1) move();
        update();
        render();
    }

    private void update() {
        vel.limit(80.7f);
        this.addSelf(vel);
        vel.scaleSelf(0);
    }

    private void move() {
        List<Vec3D> cavepts2 = null;

        cavepts2 = p.meshoctree.getPointsWithinSphere(this.copy(), dia);
        if (cavepts2 != null) {
            if (cavepts2.size() > 0) {
                Vec3D var1 = new Vec3D();
                float var3 = 3.4028235E38F;
                for (int i = 0; i < cavepts2.size(); i++) {
                    Vec3D vara = cavepts2.get(i);
                    float dista = vara.distanceToSquared(this);
                    if (dista < var3) {
                        var1 = vara;
                        var3 = dista;
                    }
                }
                Vec3D a = this.copy().subSelf(var1);

                float rad = var1.distanceTo(this);

                Vec3D a1 = var1.copy().subSelf(this);
                Vec3D a2 = p.Normal.get(var1);

                float ang = a2.angleBetween(a1, true);
                float ang2 = degrees(ang);
                if (ang2 > 90) {
                    a = a.copy().scaleSelf(-1);
                }

                if (rad < (dia + 5)) {
//                    a.normalize();
                    a = a.copy().scaleSelf(1 / rad);
                    vel.addSelf(a);
                } else {
                    vel.scaleSelf(0);
                }
            }
        }
    }

    private void render() {
//        p.stroke(255, 0, 0);
//        p.pushMatrix();
//        p.translate(x, y, z);
//        p.sphere(dia);
//        p.popMatrix();
        if (type == 1) a = new Sphere(this, dia);
        if (type == 2) a = new Sphere(this, dia2);
        // b = a.toMesh(12);
//        p.noFill();
//        p.stroke(255, 0, 0);
//        p.gfx.sphere(a, 8);
    }


}
