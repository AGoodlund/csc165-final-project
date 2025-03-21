package a2;

import tage.*;
import tage.shapes.*;

public class ManualCrystal extends ManualObject{	//0 in the middle of the central square
	public float [] verts = {	//should be 24 vertices for 3 points on 8 faces
		-1f,0f,0f,	0f,1f,0f,	0f,0f,-1f,	//top back left
		0f,0f,-1f,	0f,1f,0f,	1f,0f,0f,	//top back right
		1f,0f,0f,	0f,1f,0f,	0f,0f,1f,	//top front right
		0f,0f,1f,	0f,1f,0f,	-1f,0f,0f,	//top front left
		-1f,0f,0f,	0f,-3f,0f,	0f,0f,-1f,	//bottom back left
		0f,0f,-1f,	0f,-3f,0f,	1f,0f,0f,	//bottom back right
		1f,0f,0f,	0f,-3f,0f,	0f,0f,1f,	//bottom front right
		0f,0f,1f,	0f,-3f,0f,	-1f,0f,0f	//bottom front left
	};
	public float [] texcoords = {
		0f,.75f,	.25f,.75f,	.125f,1f,	//this for all top faces
		0f,.75f,	.25f,.75f,	.125f,1f,
		0f,.75f,	.25f,.75f,	.125f,1f,
		0f,.75f,	.25f,.75f,	.125f,1f,
		0f,.75f,	.25f,.75f,	.125f,0f,	//this for all bottom faces 
		0f,.75f,	.25f,.75f,	.125f,0f,
		0f,.75f,	.25f,.75f,	.125f,0f,
		0f,.75f,	.25f,.75f,	.125f,0f
	};
	public float [] normals = {						//THERE ARE 3 NORMALS PER VERTEX: 1:1 WITH verts.length
		-1f,1f,-1f,	-1f,1f,-1f,	-1f,1f,-1f,
			//1,1,0 x 1,0,-1
		1f,1f,-1f,	1f,1f,-1f,	1f,1f,-1f,
			//0,1,1 x 1,0,1
		0f,1f,0f,	0f,1f,0f,	0f,1f,0f,			//seems to be a problem with this one
			//-1,0,0 x -1,0,1
		-1f,1f,1f,	-1f,1f,1f,	-1f,1f,1f,
			//0,1,-1 x -1,0,-1

		3f,1f,3f,	3f,1f,3f,	3f,1f,3f,			//none of the lower part seems to reflect properly
			//1,-3,0 x 1,0,-1
		-3f,1f,3f,	-3f,1f,3f,	-3f,1f,3f,
			//0,-3,1 x 1,0,1
		-3f,-1f,-3f,-3f,-1f,-3f,-3f,-1f,-3f,
			//-1,-3,0 x -1,0,1
		-3f,1f,3f,	-3f,1f,3f,	-3f,1f,3f
			//0,3,-1 x -1,0,-1
	};

	public ManualCrystal(){
		super();
		setNumVertices(verts.length/3);
		setVertices(verts);
		setTexCoords(texcoords);
		setNormals(normals);

		setMatAmb(Utils.defAmbient()); //figure out a smokey quartz set in future
		setMatDif(Utils.defDiffuse());
		setMatSpe(Utils.defSpecular());
		setMatShi(Utils.defShininess());
	}
}