package a2;

import tage.*;
import tage.shapes.*;

public class ManualD4 extends ManualObject{
    private float[] vertices = {
        -0.707f,0.0f,0.707f,    0.707f,0.0f,0.707f,     0.0f,1.0f,0.0f,     //font
        0.0f,0.0f,-1.0f,        -0.707f,0.0f,0.707f,    0.0f,1.0f,0.0f,     //left
        0.707f,0.0f,0.707f,     0.0f,0.0f,-1.0f,        0.0f,1.0f,0.0f,     //right
        -0.707f,0.0f,0.707f,    0.707f,0.0f,0.707f,     0.0f,0.0f,-1.0f     //bottom
    };
    private float[] texcoords = {
        0.0f,0.0f,    1.0f,0.0f,    0.5f,1.0f,      //side
        0.0f,0.0f,    1.0f,0.0f,    0.5f,1.0f,      //side
        0.0f,0.0f,    1.0f,0.0f,    0.5f,1.0f,      //side
        0.0f,0.95f,   0.1f,0.95f,   0.1f,1.0f      //bottom
    };
    private float[] normals = { //from one point in a triangle to both others crossed together (v2 - v1) x (v3 - v1) = P 
        0.0f,0.999698f,1.414f,  0.0f,0.999698f,1.414f,  0.0f,0.999698f,1.414f,
        //front: 1.414, 0, 0 x 0.707, 1, -0.707
        -0.57735f,0.57735f,-0.57735f,   -0.57735f,0.57735f,-0.57735f,   -0.57735f,0.57735f,-0.57735f,
        //left: -0.707, 1, 1.707 x 0, 1, 1
        0.208116f,-0.462274f,-0.861969f,    0.208116f,-0.462274f,-0.861969f,    0.208116f,-0.462274f,-0.861969f,
        //right: -7.07,0,-1.707 x -0.707, 1, -0.707
        0.0f,1.0f,0.0f, 0.0f,1.0f,0.0f, 0.0f,1.0f,0.0f
        //bottom: 1.414, 0, 0 x 0.707, 0, -1.707
    }; //each P should be same for whole face so it's 3 numbers in groups of 3
    //https://www.emathhelp.net/calculators/linear-algebra/vector-subtraction-calculator/
    //https://www.wolframalpha.com/input?i=cross+product+calculator

    public ManualD4(){
        super();
        setNumVertices(12); //should be 4 sides with 3 vertices
        setVertices(vertices);
        setTexCoords(texcoords);
        setNormals(normals);

        setMatAmb(Utils.goldAmbient());
        setMatDif(Utils.goldDiffuse());
        setMatSpe(Utils.goldSpecular());
        setMatShi(Utils.goldShininess());
    }
}