/**
 * Created by Omar on 11/12/2016.
 */

import processing.core.PShape;
import processing.opengl.*;
import processing.core.PApplet;


import toxi.geom.*;
import toxi.geom.mesh.*;
import toxi.math.waves.AbstractWave;
import toxi.math.waves.SineWave;
import toxi.volume.*;
import toxi.processing.*;

import wblut.processing.*;
import wblut.hemesh.*;
import wblut.geom.*;

import java.util.*;
import java.util.Map;
import java.util.Iterator;

import pathfinder.*;

import peasy.*;

import com.hamoid.*;

import java.util.ArrayList;


public class CaveFlockVik extends PApplet {

    public PeasyCam cam;
    public PShape obj;

    float xmaxint;
    float ymaxint;
    float xminint;
    float yminint;
    float zminint;
    float zmaxint;

    boolean start2 = false;

    boolean boidoctre = false;
    boolean makecorridor = true;
    boolean makepath = false;

    public WETriangleMesh cave;
    public HE_Mesh mesh;


    public List<Vec3D> cavepts;
    public List<meshvertices> vertexpop = new ArrayList<>();
    public List<Vec3D> pts = new ArrayList<>();

    public HashMap<Vec3D, Integer> Slope = new HashMap();
    public HashMap<Vec3D, Vec3D> Normal = new HashMap();
    public HashMap<Vec3D, Integer> ptscheck = new HashMap<>();

    float DIM = 1500;


    public Octree meshoctree;
    public Octree boidoctree;

    public Flock flock;

    float ballvel = 0f;
    boolean ballmove = true;
    boolean buildmesh = false;
    boolean buildmesh1 = false;
    boolean buildvolume = false;

    public Graph gs = new Graph();
    public GraphNode[] gNodes, rNodes;
    public GraphEdge[] gEdges, exploredEdges;
    IGraphSearch pathFinder;


    public WETriangleMesh corridor;

    public Vec3D SCALE = new Vec3D(1, 1, 1).scaleSelf(100);

    int DIMX = 64;
    int DIMY = 64;
    int DIMZ = 64;

    float density = 0.5f;

    VolumetricSpace volume;
    VolumetricBrush brush;
    IsoSurface surface;

    AbstractWave brushSize;

    WB_Render render;
    ToxiclibsSupport gfx;

    VideoExport videoExport;

    Vec3D meshcentre;

    public static void main(String[] args) {
        PApplet.main("CaveFlockVik", args);
    }

    public void settings() {
        size(1400, 800, P3D);
        smooth();
    }

    public void setup() {

        flock = new Flock(this);

        obj = loadShape("data/" + "drone.obj");
        obj.scale(3);

        meshsetup();

        cam = new PeasyCam(this, meshcentre.x, meshcentre.y, meshcentre.z, 2200);

        meshoctree = new Octree(this, new Vec3D(-1, -1, -1).scaleSelf(meshcentre), DIM * 2);
        if (boidoctre) boidoctree = new Octree(this, new Vec3D(-1, -1, -1).scaleSelf(meshcentre), DIM * 2);

        meshrun();

        if ((makecorridor)||(makepath)) {
            volume = new VolumetricSpaceArray(SCALE.scaleSelf(20), DIMX, DIMY, DIMZ);
            brush = new RoundBrush(volume, 1000);
            surface = new ArrayIsoSurface(volume);
            if (makecorridor) corridor = new WETriangleMesh();
        }

        setpathfind();

        for (int i = 0; i < 15; i++) {
            Vec3D a = randomitem(pts);
            flock.addBoid(new Boid(this, a, 1));
        }

        for (int i = 0; i < 100; i++) {
            Vec3D a = randomitem(pts);
            flock.addBoid(new Boid(this, a, 2));
        }

        for (int i = 0; i < 10; i++) {
            Vec3D a = randomitem(pts);
            flock.addBoid(new Boid(this, a, 7));
        }

        for (int i = 0; i < 0; i++) {
            Vec3D a = randomitem(pts);
            flock.addBoid(new Boid(this, a, 8));
        }

        videoExport = new VideoExport(this, "basic.mp4");

    }

    public void draw() {
        background(0);

        if (frameCount < 10) {
            for (int i = 0; i < flock.boids.size(); i++) {
                Boid b = flock.boids.get(i);
                b.checkMesh();
            }
        }

        //        if ((frameCount%20 == 0)&&(flock.boids.size()<25)) {
//            for (int i = 0; i < 5; i++) {
//                flock.addBoid(new Boid(this, new Vec3D(random(xminint + 300, xmaxint - 300), random(yminint + 300, ymaxint - 300), random(zminint + 300, zmaxint - 300)), new Vec3D(random(-TWO_PI, TWO_PI), random(-TWO_PI, TWO_PI), random(-TWO_PI, TWO_PI)), 2));
//            }
//        }

        if (frameCount > 10) {
            if((makepath)||(makecorridor)) {
                List<Boid> pathboid = new ArrayList<>();
                List<Boid> pathroom = new ArrayList<>();

                for (Boid a : flock.boids) {
                    if (a.type == 7) {
                        pathboid.add(a);
                    }
                    if (a.type == 8) {
                        pathroom.add(a);
                    }
                }

                if (makepath) {
                    List<String> pathptsfile = new ArrayList<>();
                    runpathfind2(pathptsfile, pathboid);
                }

                if (makecorridor) {
                    List<String> pathptsfile = new ArrayList<>();
                    List<Pathagent> pathagtpts = new ArrayList<>();
                    List<Vec3D> circpts = new ArrayList<>();

                    runpathfind2(pathptsfile, pathboid);
                    readpath(circpts);
                    for (Vec3D a : circpts) {
                        Pathagent b = new Pathagent(this, a, 1);
                        pathagtpts.add(b);
                    }
                    for (Boid a : pathboid) {
                        Pathagent c = new Pathagent(this, a, 2);
                        pathagtpts.add(c);
                    }

                    for (Boid a : pathroom) {
                        Pathagent c = new Pathagent(this, a, 2);
                        pathagtpts.add(c);
                    }

                    drawcorridor(pathagtpts);
                }
            }
            flock.run();
            if (boidoctre) boidoctree.run();
        }
        //if octree.draw();

        pushMatrix();
        fill(40, 120);
        noStroke();
        lights();
        gfx.mesh(cave, false, 0);
        popMatrix();

        for (int i = 0; i < vertexpop.size(); i++) {
            meshvertices a = vertexpop.get(i);
            a.update();
        }

//       videoExport.saveFrame();
    }

    private void meshsetup() {
        cave = (WETriangleMesh) new STLReader().loadBinary(sketchPath("data/" + "cave2.stl"), STLReader.WEMESH);
        mesh = new HEC_FromBinarySTLFile(sketchPath("data/" + "cave2.stl")).create();


        ArrayList xMax = new ArrayList();
        ArrayList yMax = new ArrayList();
        ArrayList zMax = new ArrayList();

        int novert1 = cave.getNumVertices();

        cavepts = (new ArrayList<Vec3D>(cave.getVertices()));

        for (int i = 0; i < novert1; i++) {
            Vec3D a = cavepts.get(i);
            xMax.add(a.x);
            yMax.add(a.y);
            zMax.add(a.z);
        }

        xmaxint = (float) Collections.max(xMax);
        ymaxint = (float) Collections.max(yMax);
        zmaxint = (float) Collections.max(zMax);
        xminint = (float) Collections.min(xMax);
        yminint = (float) Collections.min(yMax);
        zminint = (float) Collections.min(zMax);

        xMax.clear();
        yMax.clear();
        zMax.clear();

        meshcentre = cave.computeCentroid();

    }

    private void readpath(List<Vec3D> cpts) {

        String linept[] = loadStrings("data/" + "path.txt");
        int stringcount = 0;

        for (int i = 0; i < linept.length; i++) {
            if (linept[i].equals("!")) {
                stringcount++;
            }
        }

        Integer[] splitnumbers = new Integer[stringcount];
        int stringcount2 = 0;

        for (int i = 0; i < linept.length; i++) {
            if (linept[i].equals("!")) {
                splitnumbers[stringcount2] = i;
                stringcount2++;
            }
        }

        String[][] ptslpit = new String[stringcount][];
        for (int i = 0; i < splitnumbers.length; i++) {
            if (i == 0) {
                ptslpit[i] = (Arrays.copyOfRange(linept, 0, splitnumbers[i] - 1));
            }
            if ((i > 0) && (i < splitnumbers.length)) {
                ptslpit[i] = (Arrays.copyOfRange(linept, (splitnumbers[i - 1] + 1), (splitnumbers[i] - 1)));
            }
        }

        for (int i = 0; i < ptslpit.length; i++) {
            for (int j = 0; j < ptslpit[i].length; j++) {
                String[] vec = split(ptslpit[i][j], ",");
                Vec3D a = new Vec3D(Float.parseFloat(vec[0]), Float.parseFloat(vec[1]), Float.parseFloat(vec[2]));
                cpts.add(a);
            }
        }
    }

    private void meshrun() {


        int novert = mesh.getNumberOfVertices();

        for (int i = 0; i < novert; i++) {

            WB_Coord mnorm = mesh.getVertexNormal(i);
            Vec3D vertex = cave.getVertexForID(i);

            float xnPos = mnorm.xf();
            float ynPos = mnorm.yf();
            float znPos = mnorm.zf();

            Vec3D mnormv = new Vec3D(xnPos, ynPos, znPos);
            Vec3D mvert = new Vec3D(0, 0, 1);

            float slope = mnormv.angleBetween(mvert);

            slope = degrees(slope);

            slope = 180 - slope;

            int slopeint = (int) slope;
            Slope.put(vertex, slopeint);
            Normal.put(vertex, mnormv);

            meshvertices a = new meshvertices(this, vertex, slopeint, mnormv);
            if(i%5==0)vertexpop.add(a);
            meshoctree.addPts(a);


        }


        gfx = new ToxiclibsSupport(this);
        // render = new WB_Render(this);
    }

    private void setpathfind() {

        readText();

        gs = new Graph();

        Collections.reverse(pts);

        float minValue;
        float maxValue;

        ArrayList<Float> variable = new ArrayList<>();

        for (int i = 0; i < pts.size(); i++) {
            Vec3D f = pts.get(i);
            ptscheck.put(f, i);
            Vec3D ptmesh = cave.getClosestVertexToPoint(f);
            float meshrad = f.distanceTo(ptmesh);
            int slppt = Slope.get(ptmesh);
            float meshvariable = slppt / meshrad * meshrad;
            variable.add(meshvariable);
        }

        maxValue = Collections.max(variable);
        minValue = Collections.min(variable);


        HashMap<Vec3D, Float> ptsvar = new HashMap<>();


        for (int i = 0; i < pts.size(); i++) {
            Vec3D f = pts.get(i);
            float var1 = variable.get(i);
            float mVal = 0.00f;
            float MVal = 0.4f;
            float var2 = map(var1, minValue, maxValue, mVal, MVal);
            float var3 = map(var2, 0.00f, 0.2f, 0.00f, 100.0f);
            ptsvar.put(f, var3);
        }

        for (int i = 0; i < pts.size(); i++) {
            Vec3D f = pts.get(i);
            gs.addNode(new GraphNode(i, f.x, f.y, f.z));
            for (int j = 0; j < pts.size(); j++) {
                Vec3D b = pts.get(j);
                if (b != f) {
                    if (b.distanceToSquared(f) < 80 * 80) {
                        float varline = ptsvar.get(f);
                        gs.addEdge(i, j, varline);
                    }
                }
            }
        }

        gNodes = gs.getNodeArray();
        gEdges = gs.getAllEdgeArray();
        gs.compact();

    }

    private void drawcorridor(List<Pathagent> pathapts) {
        if ((makecorridor)) {

            ballvel = 0;

            if (ballmove) {
                for (Pathagent a : pathapts) {
                    a.run();
                }

            }

            if (!buildmesh) {

                corridor.clear();
                surface.reset();
                volume.clear();

                brush.setSize(new SineWave(0, 0.1f, 140f, 35f).update());
                for (Pathagent a : pathapts) {
                    if (a.type == 1)
                        brush.drawAtAbsolutePos(new Vec3D(a.x - meshcentre.x, a.y - meshcentre.y, a.z - meshcentre.z), density);
                }
                brush.setSize(new SineWave(0, 0.1f, 140f, 120f).update());
                for (Pathagent a : pathapts) {
                    if (a.type == 2)
                        brush.drawAtAbsolutePos(new Vec3D(a.x - meshcentre.x, a.y - meshcentre.y, a.z - meshcentre.z), density);
                }
                volume.closeSides();
                surface.reset();
                surface.computeSurfaceMesh(corridor, 0.1f);
                for (int i = 0; i < 1; i++) {
                    new LaplacianSmooth().filter(corridor, 1);
                }

                corridor = corridor.getTranslated(new Vec3D(meshcentre.x, meshcentre.y, meshcentre.z));



//                translate(meshcentre.x, meshcentre.y, meshcentre.z);
                //                   buildmesh = true;


//                corridor.computeFaceNormals();
//                corridor.computeVertexNormals();


            }

//            if (!buildmesh) {
//                corridor.clear();
//
//                for (Pathagent a : pathagtpts) {
//                    corridor.addMesh(a.b);
//                }
//
//                corridcntr = corridor.computeCentroid();
//                MeshVoxelizer voxelizer = new MeshVoxelizer(RES);
//                voxelizer.setWallThickness(0);
//                VolumetricSpace vol = voxelizer.voxelizeMesh(corridor);
//                vol.closeSides();
//                IsoSurface surface = new HashIsoSurface(vol);
//                corridor = new WETriangleMesh();
//                surface.computeSurfaceMesh(corridor, 0.2f);
//                corridor.computeVertexNormals();
//
//                for (int i = 0; i < 1; i++) {
//                    new LaplacianSmooth().filter(corridor, 1);
//                }
////                    buildmesh = true;
//            }

            //           corridor.saveAsOBJ(sketchPath("data/" + "corridor.obj"));

//            pushMatrix();
//            strokeWeight(1f);
//            stroke(255, 0, 0);
//            noFill();
//            gfx.mesh(corridor);
//            popMatrix();

//            }
        }
    }

    private void runpathfind2(List<String> p1,List<Boid> bds) {

        for (Boid b : bds) {
            pathFinder = makePathFinder(3);
            usePathFinder(pathFinder, findclosestnode(b), 0);
            if(!buildmesh)drawRoute(rNodes, color(200, 0, 0), 5.0f);
            for (int i = 0; i < rNodes.length; i++) {
                String a = (Float.toString(rNodes[i].xf()) + "," + Float.toString(rNodes[i].yf()) + "," + Float.toString(rNodes[i].zf()));
                p1.add(a);
            }
            p1.add("!");

        }

//        Vec3D builder1 = new Vec3D();
//        Vec3D builder2 = new Vec3D();
//
//        builder1 = builderpop.get(0);
//        builder2 = builderpop.get(1);
//
//        pathFinder = makePathFinder(3);
//        usePathFinder(pathFinder, findclosestnode(builder1), 0);
//        drawRoute(rNodes, color(200, 0, 0), 5.0f);
//        for (int i = 0; i < rNodes.length; i++) {
//            String a = (Float.toString(rNodes[i].xf()) + "," + Float.toString(rNodes[i].yf()) + "," + Float.toString(rNodes[i].zf()));
//            pathptsfile.add(a);
//        }
//        pathptsfile.add("!");
//
//
//        pathFinder = makePathFinder(3);
//        usePathFinder(pathFinder, findclosestnode(builder2), 0);
//        drawRoute(rNodes, color(200, 0, 0), 5.0f);
//        for (int i = 0; i < rNodes.length; i++) {
//            String a = (Float.toString(rNodes[i].xf()) + "," + Float.toString(rNodes[i].yf()) + "," + Float.toString(rNodes[i].zf()));
//            pathptsfile.add(a);
//        }
//        pathptsfile.add("!");


        String[] pathpts = new String[p1.size()];

        for (int i = 0; i < p1.size(); i++) {
            String a = p1.get(i);
            pathpts[i] = a;
        }

        saveStrings("data/" + "path.txt", pathpts);


    }

    private void readText() {
        String[] attptList = loadStrings("data/" + "points.txt");
        for (int i = attptList.length - 1; i >= 0; i--) {
            String point[] = (split(attptList[i], ','));
            if (point.length == 3) {
                Vec3D Temp_PT = new Vec3D(Float.parseFloat(point[0]), Float.parseFloat(point[1]), Float.parseFloat(point[2]));
                pts.add(Temp_PT);
            }
        }
    }

    private Vec3D randomitem(List <Vec3D> pts){
        int index = (int) random(pts.size());
        Vec3D item = pts.get(index);
        return item;
    }

    IGraphSearch makePathFinder(int pathFinder) {
        IGraphSearch pf = null;
        float f = 1.0f;
        switch (pathFinder) {
            case 0:
                pf = new GraphSearch_DFS(gs);
                break;
            case 1:
                pf = new GraphSearch_BFS(gs);
                break;
            case 2:
                pf = new GraphSearch_Dijkstra(gs);
                break;
            case 3:
                pf = new GraphSearch_Astar(gs, new AshCrowFlight(f));
                break;
            case 4:
                pf = new GraphSearch_Astar(gs, new AshManhattan(f));
                break;
        }
        return pf;
    }

    void drawRoute(GraphNode[] r, int lineCol, float sWeight) {
        if (r.length >= 2) {
            pushStyle();
            stroke(lineCol);
            strokeWeight(sWeight);
            noFill();
            for (int i = 1; i < r.length; i++) {
                line(r[i - 1].xf(), r[i - 1].yf(), r[i - 1].zf(), r[i].xf(), r[i].yf(), r[i].zf());
            }
            popStyle();
        }
    }

    void usePathFinder(IGraphSearch pf, int start1, int end1) {
        pf.search(start1, end1, true);
        rNodes = pf.getRoute();
        exploredEdges = pf.getExaminedEdges();
    }

    private int findclosestnode(ReadonlyVec3D var1) {
        float var3 = 3.4028235E38F;
        int node = 0;

        for (int i = 0; i < pts.size(); i++) {
            Vec3D vara = pts.get(i);
            float dista = vara.distanceToSquared(var1);
            if (dista < var3) {
                var3 = dista;
                node = i;
            }
        }
        return node;
    }

}
