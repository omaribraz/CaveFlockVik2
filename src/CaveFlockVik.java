/**
 * Created by Vikram on 11/12/2016.
 */

import processing.core.PShape;
import processing.opengl.*;
import processing.core.PApplet;


import toxi.geom.*;
import toxi.geom.mesh.*;
import toxi.volume.*;
import toxi.processing.*;

import wblut.processing.*;
import wblut.hemesh.*;
import wblut.geom.*;

import java.util.*;
import java.util.Map;
import java.util.Iterator;

import peasy.*;


import java.util.ArrayList;

import peasy.*;

public class CaveFlockVik extends PApplet {

    PeasyCam cam;
    PShape obj;

    float xmaxint;
    float ymaxint;
    float xminint;
    float yminint;
    float zminint;
    float zmaxint;
    boolean start2 = false;

    WETriangleMesh cave2;
    HE_Mesh mesh;

    ArrayList xMax = new ArrayList();
    ArrayList yMax = new ArrayList();
    ArrayList zMax = new ArrayList();
    ArrayList meshpts= new ArrayList();
    ArrayList<Vec3D> cavepts;
    ArrayList <Vec3D> Boidpos = new ArrayList();
    ArrayList <Vec3D> Boidpop = new ArrayList();
    ArrayList <meshvertices> vertexpop = new ArrayList<>();

    HashMap<Vec3D, Integer> Slope = new HashMap();
    HashMap<Vec3D, Vec3D> Normal = new HashMap();
    HashMap<Vec3D, meshvertices>vertexhash =new HashMap();

    float DIM = 1500;
    boolean showOctree = true;
    boolean useSphere = true;

    float RADIUS = 20;




    Octree meshoctree;
    Octree boidoctree;

    Flock flock;

    WB_Render render;
    ToxiclibsSupport gfx;

    public static void main(String[] args) {
        PApplet.main("CaveFlockVik", args);
    }

    public void settings() {
        size(1400, 800, P3D);
        smooth();
    }

    public void setup() {

        flock = new Flock(this);
        cam = new PeasyCam(this, 750, 750, 0, 2200);
        obj = loadShape("data/"+"drone.obj");
        obj.scale(3);

        meshrun();

        Vec3D a =cave2.computeCentroid() ;

        meshoctree=new Octree(this,new Vec3D(-1, -1, -1).scaleSelf(a), DIM*2);
        boidoctree =new Octree(this,new Vec3D(-1, -1, -1).scaleSelf(a), DIM*2);

        for (int i = 0; i <100; i++) {
            flock.addBoid(new Boid(this,new Vec3D(random(xminint+200, xmaxint-200), random(yminint+200, ymaxint-200), random(zminint+200, zmaxint-200)), new Vec3D(random(-TWO_PI, TWO_PI), random(-TWO_PI, TWO_PI), random(-TWO_PI, TWO_PI)),1));
        }

        for (int i = 0; i <200; i++) {
            flock.addBoid(new Boid(this,new Vec3D(random(xminint+200, xmaxint-200), random(yminint+200, ymaxint-200), random(zminint+200, zmaxint-200)), new Vec3D(random(-TWO_PI, TWO_PI), random(-TWO_PI, TWO_PI), random(-TWO_PI, TWO_PI)),2));
        }

        meshoctree.addAll(cavepts);
    }

    public void draw() {
        background(0);

        boidoctree.run();

        flock.run();

        if (frameCount<30) {
            for (int i = 0; i <flock.boids.size(); i++) {
                Boid b = flock.boids.get(i);
                b.checkMesh();
            }
        }

        //if (showOctree) octree.draw();
        stroke(255, 0, 0);
        noFill();


        pushMatrix();
        fill(40, 120);
        noStroke();
        strokeWeight(.1f);
        stroke(10);
        lights();
        gfx.mesh(cave2, false, 0);
        popMatrix();


        for(int i = 0; i<vertexpop.size(); i++){
            meshvertices a = vertexpop.get(i);
            a.update();
        }
    }

    private void meshrun() {
        cave2 = (WETriangleMesh) new STLReader().loadBinary(sketchPath("data/"+"cave.stl"), STLReader.WEMESH);
        mesh = new HEC_FromBinarySTLFile(sketchPath("data/" + "cave.stl")).create();


        int novert1 = cave2.getNumVertices();

        cavepts = (new ArrayList<Vec3D>(cave2.getVertices()));

        for (int i=0; i< novert1; i++) {
            Vec3D a = cavepts.get(i);
            xMax.add(a.x);
            yMax.add(a.y);
            zMax.add(a.z);
        }

        xmaxint = (float)Collections.max(xMax);
        ymaxint = (float)Collections.max(yMax);
        zmaxint = (float)Collections.max(zMax);
        xminint = (float)Collections.min(xMax);
        yminint = (float)Collections.min(yMax);
        zminint = (float)Collections.min(zMax);

        xMax.clear();
        yMax.clear();
        zMax.clear();

        int novert = mesh.getNumberOfVertices();

        for (int i = 0; i < novert; i++) {

            WB_Coord mnorm = mesh.getVertexNormal(i);
            WB_Coord vertex1 = mesh.getVertex(i);
            Vec3D vertex = cave2.getVertexForID(i);

            float xnPos = mnorm.xf();
            float ynPos = mnorm.yf();
            float znPos = mnorm.zf();

            Vec3D mnormv = new Vec3D(xnPos, ynPos, znPos);
            Vec3D mvert = new Vec3D(0, 0, 1);

            float slope = mnormv.angleBetween(mvert);

            slope = degrees(slope);

            slope = 180-slope;

            int slopeint = (int) slope;
            Slope.put(vertex, slopeint);
            Normal.put(vertex, mnormv);

            if((slopeint<80)){
                meshvertices a = new meshvertices(this,vertex,slopeint,mnormv);
                vertexpop.add(a);
                vertexhash.put(vertex,a);
            }



        }

        gfx=new ToxiclibsSupport(this);
        // render = new WB_Render(this);
    }

}
