/**
 * Created by Vikram on 11/14/2016.
 */
import toxi.geom.Vec3D;
public class meshvertices extends Vec3D {
    private CaveFlockVik p;
    int slope;
    Vec3D Normal;
    int taken;
    int takencnt;


    meshvertices( CaveFlockVik _p, Vec3D v, int s, Vec3D n) {
        super(v);
        p = _p;
        slope = s;
        Normal = n.copy();
        taken = 0;
        takencnt = 0;
    }



    void render(){
        p.stroke(200);
        p.strokeWeight(2);
        p.point(this.x,this.y,this.z);
    }




}
