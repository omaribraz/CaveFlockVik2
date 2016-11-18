import toxi.geom.Vec3D;

public class trail extends Vec3D {
    private CaveFlockVik p1;
    Vec3D orientation;
    public int  trailNo = 80;
    public int strength = trailNo;
    Boid b;

    trail( CaveFlockVik _p, Vec3D p, Vec3D o) {
        super(p);
        p1 =_p;
        orientation = o.copy();
        orientation = orientation.normalize();
    }

    void update() {
//        render();
    }

    void render() {
        p1.stroke(255);
        p1.strokeWeight(2);
        p1.point(x, y, z);
    }
}