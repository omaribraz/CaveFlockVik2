/**
 * Created by Vikram on 11/14/2016.
 */
import toxi.geom.Vec3D;
public class meshvertices extends Vec3D {
    private CaveFlockVik p;
    public int slope;
    public Vec3D Normal;
    public int taken;
    public int takencnt;
    public Boid boid;


    meshvertices( CaveFlockVik _p, Vec3D v, int s, Vec3D n) {
        super(v);
        p = _p;
        slope = s;
        Normal = n.copy();
        taken = 0;
        takencnt = 0;
    }

    public void update(){
        if(takencnt>4){
            p.vertexpop.remove(this);
        }
//        render();

    }



    private void render() {
        if (taken == 1) {
            p.stroke(255, 255);
            p.strokeWeight(20);
            p.point(this.x, this.y, this.z);
        }
    }



}
