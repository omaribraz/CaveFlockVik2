import toxi.geom.Vec3D;

public class trail extends Vec3D {
    private CaveFlockVik p1;
    Vec3D orientation;
    public int  trailNo = 80;

    trail( CaveFlockVik _p, Vec3D p, Vec3D o) {
        super(p);
        p1 =_p;
        orientation = o.copy();
        orientation = orientation.normalize();
    }
}