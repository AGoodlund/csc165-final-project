package a2;

import tage.*;
import tage.shapes.*;
import tage.input.*;
import tage.input.action.*;
import tage.networking.IGameConnection.ProtocolType;
import tage.nodeControllers.*;
import tage.audio.*;

import java.lang.Math;
import java.awt.*;

import java.awt.event.*;

import java.io.*;
import java.util.*;
import java.util.UUID;
import java.net.InetAddress;

import java.net.UnknownHostException;

import org.joml.*; 

import net.java.games.input.*;
import net.java.games.input.Component.Identifier.*;

import java.util.ArrayList;
import javax.swing.*;

import com.jogamp.opengl.awt.GLCanvas;//this is for mouse movement

import tage.physics.PhysicsEngine;
import tage.physics.PhysicsObject;
import tage.physics.JBullet.*;
import com.bulletphysics.dynamics.RigidBody;
import com.bulletphysics.collision.dispatch.CollisionObject;

public class MyGame extends VariableFrameRateGame
{
	private static Engine engine;
	private Camera cam;
//	private CameraOrbit3D orb;
	private InputManager im;
	private IAudioManager am;

	private ArrayList<GameObject> hideableShapes = new ArrayList<GameObject>();
	
	private double lastFrameTime, currFrameTime, elapsTime;

//-------------HUD elements--------------
	private float[] hud1Color = spot.red;
	private float[] hud2Color = spot.yellow;
	private String dispStr1 = " ", dispStr2;
	private int HUDscore, HUDCoords;

//-------------Visuals--------------
	private GameObject avatar, x, y, z, terr, puffer, enemy, diver;//, cube, sphere, torus, crystal;
	private AnimatedShape diverS;
	private ObjShape dolS, xAxis, yAxis, zAxis, terrS, pufferS, pufferCalmS;//, sphereS, torusS,  crystalS, cubeS;
	private TextureImage doltx, hills, grass, pufferX, pufferAltX;

	private Light light1;//, spotlightR, spotlightG, spotlightB;
	private int skybox, seabox;

	private GameObject raft;
	private ObjShape raftS;
	private TextureImage raftx;

	private GameObject water;
	private ObjShape waterS;

//-------------Sounds--------------
	private Sound bubbles;
	private Vector3f up = new Vector3f(spot.y);

//-------------Node Controllers-------------
//	private RotationController rc;
//	private RollController roll;

//-------------Networking----------------
	private GhostManager gm;
	private String serverAddress;
	private int serverPort = -1;
	private ProtocolType serverProtocol;
	private ProtocolClient protClient;
	private boolean isClientConnected = false;
	private ObjShape ghostS;
	private TextureImage ghostT;


//-------------Mouse Controls----------------
	private Robot robot;
	private float curMouseX, curMouseY, centerX, centerY;
	private float prevMouseX, prevMouseY; // loc of mouse prior to move
	private boolean isRecentering; //indicates the Robot is in action
	private float tilt;
	private float sensitivity = 1.0f;

//-------------Height Map----------------
	private float height;
	private ArrayList<GameObject> mappable = new ArrayList<GameObject>(); //objects that follow height map
	//private ArrayList<PhysicsObject> mappableP = new ArrayList<PhysicsObject>(); //Physics objects that follow height map
	private Vector3f loc = new Vector3f();

//-------------Helpers----------------
	private Vector3f v = new Vector3f();
	private Matrix4f m = new Matrix4f();
	
//-------------Physics----------------
	private PhysicsEngine physicsEngine;
	private PhysicsObject dolP, ghostP, raftP, pufferP, groundPlaneP, diverP;
	public float[] gravity = {0f, -9.8f, 0f}; //Making this public in case we want to change it anywhere
	private float vals[] = new float[16]; 
	//mappableP.add(pufferP);

//-------------My Game----------------
	public MyGame() { super(); }
	public MyGame(String serverAddress, int serverPort, String protocol)
	{	super();
		gm = new GhostManager(this);
		this.serverAddress = serverAddress;
		this.serverPort = serverPort;
		if (protocol.toUpperCase().compareTo("TCP") == 0)
			this.serverProtocol = ProtocolType.TCP;
		else
			this.serverProtocol = ProtocolType.UDP;
	}

	
		public static void main(String[] args){	
		MyGame game;
		if(args.length == 0)
			game = new MyGame();
		else
			game = new MyGame(args[0], Integer.parseInt(args[1]), args[2]);
		engine = new Engine(game);
		game.initializeSystem();
		game.game_loop();
	}
	
	
	public GameObject getAvatar(){ return avatar; }

	@Override
	public void loadShapes(){	
		dolS = new ImportedModel("ULPD.obj");
		ghostS = new ImportedModel("dolphinLowPoly.obj");
		pufferS = new ImportedModel("PufferFish_Angry.obj");
		pufferCalmS = new ImportedModel("PufferFish_Calm.obj");
		terrS = new TerrainPlane(1000); //pixels per axis is 1000 X 1000
		raftS = new Cube();
		waterS = new Plane();
		diverS = new AnimatedShape("Diver.rkm", "Diver.rks");
			diverS.loadAnimation("WALK", "Diver_walk.rka");
//    	cubeS = new Cube();
// 		sphereS = new Sphere();
//		torusS = new Torus(0.5f, 0.2f, 48);
//		crystalS = new ManualCrystal();

		xAxis = new Line(new Vector3f(0f,0f,0f), new Vector3f(3f,0f,0f));
		yAxis = new Line(new Vector3f(0f,0f,0f), new Vector3f(0f,3f,0f));
		zAxis = new Line(new Vector3f(0f,0f,0f), new Vector3f(0f,0f,-3f));
	}

	@Override
	public void loadTextures(){	
		//doltx = new TextureImage("ULPDuv.png");
		ghostT = new TextureImage("oiter.png");
		pufferX = new TextureImage("Pufferfish_Angry_Spiney.png");
		pufferAltX = new TextureImage("Pufferfish_Angry_SpineyAlt.png");

		hills = new TextureImage("hills.jpg");
		grass = new TextureImage("grass.jpg");
	}

	@Override
	public void buildObjects()
	{	Matrix4f initialTranslation, initialScale, initialRotation;

		// build dolphin in the center of the window
		avatar = new GameObject(GameObject.root(), new Cube());//dolS, doltx);
		initialTranslation = (new Matrix4f()).translation(0f,0f,10.0f);
//		initialScale = (new Matrix4f()).scaling(0.75f);
		avatar.setLocalTranslation(initialTranslation);
//		avatar.setLocalScale(initialScale);
		mappable.add(avatar);
		avatar.getRenderStates().setColor(new Vector3f(spot.black));
		avatar.getRenderStates().setHasSolidColor(true);


		// build Enemy Pufferfish
		enemy = new GameObject(GameObject.root(), pufferS, pufferAltX);
		initialTranslation = (new Matrix4f()).translation(5f,2f,-1f); 
		initialScale = (new Matrix4f()).scaling(5f);
		enemy.setLocalTranslation(initialTranslation);
		enemy.setLocalScale(initialScale);
		mappable.add(enemy);
/*
		//build crystal
		crystal = new GameObject(GameObject.root(),crystalS);
//			crystal.getRenderStates().setColor(teal).setHasSolidColor(true);
//			crystal.getRenderStates().setHasSolidColor(true);
		initialTranslation = (new Matrix4f()).translation(0f,3f,-2f);
		initialScale = (new Matrix4f()).scaling(2f);
		crystal.setLocalTranslation(initialTranslation);
		crystal.setLocalScale(initialScale);
		crystal.getRenderStates().setPositionalColor(true);

//		crystal.setParent(torus);
//		crystal.propagateScale(false);
//		crystal.propagateTranslation(true);
*/
		//build lines
		initialRotation = (new Matrix4f()).rotationY((float)java.lang.Math.toRadians(180f));
		avatar.setLocalRotation(initialRotation);

		//build Pufferfish
		
		puffer = new GameObject(GameObject.root(), pufferS, pufferX);
		initialTranslation = (new Matrix4f()).translation(0f,-.5f,0f);
		initialScale = (new Matrix4f()).scaling(10f);
		puffer.setLocalTranslation(initialTranslation);
		puffer.setLocalScale(initialScale);
		
		puffer.translate(0f,10f,0f);
		mappable.add(puffer);

		//build lines
		x = new GameObject(GameObject.root(), xAxis);
		y = new GameObject(GameObject.root(), yAxis);
		z = new GameObject(GameObject.root(), zAxis);
		(x.getRenderStates()).setColor(new Vector3f(spot.red));
		(y.getRenderStates()).setColor(new Vector3f(spot.green));
		(z.getRenderStates()).setColor(new Vector3f(spot.blue));
		hideableShapes.add(x); hideableShapes.add(y); hideableShapes.add(z);
		x.getRenderStates().disableRendering();
		y.getRenderStates().disableRendering();
		z.getRenderStates().disableRendering();
		
		//Terrain
		terr = new GameObject(GameObject.root(),terrS,grass);
		initialTranslation = (new Matrix4f()).translation(0f,-0.25f,0f); 
		terr.setLocalTranslation(initialTranslation);
		initialScale = (new Matrix4f()).scaling(20.0f, 1.0f, 20.0f);
		terr.setLocalScale(initialScale);
		terr.setHeightMap(hills);
		// set tiling for terrain texture
		terr.getRenderStates().setTiling(1);
		terr.getRenderStates().setTileFactor(10);
		terr.translate(0f,-10f,0f); //Removed so I could use terrain again

		raft = new GameObject(GameObject.root(),raftS);
		initialTranslation=(new Matrix4f()).translate(0f,-.5f,0f);
		raft.setLocalTranslation(initialTranslation);
		initialScale = (new Matrix4f()).scaling(3f,.5f,5f);
		raft.setLocalScale(initialScale);
		raft.getRenderStates().setPositionalColor(true);

		water = new GameObject(GameObject.root(), waterS);
		water.getRenderStates().setColor(new Vector3f(spot.blue));
		water.getRenderStates().setHasSolidColor(true);
		initialTranslation = new Matrix4f().translation(0f,-.25f,0f);
		water.setLocalTranslation(initialTranslation);
		initialScale = new Matrix4f().scaling(20.0f, 1.0f, 20.0f);
		water.setLocalScale(initialScale);

		diver = new GameObject(GameObject.root(), diverS);
		initialTranslation = new Matrix4f().translation(0f,1.5f,-1f);
		initialScale = new Matrix4f().scaling(.5f);
		diver.setLocalTranslation(initialTranslation);
		diver.setLocalScale(initialScale);
		diver.yaw(180f);
		diver.getRenderStates().setPositionalColor(true);
		mappable.add(diver);
	}
	
	public float getTerrainHeight(float x, float z)
	{
		float height = terr.getHeight(x,z);
		return height;
	}
	
		
	@Override
	public void initializeLights()
	{	Light.setGlobalAmbient(0.5f, 0.5f, 0.5f);
		light1 = new Light();
		light1.setLocation(new Vector3f(5.0f, 4.0f, 2.0f));
		(engine.getSceneGraph()).addLight(light1);

//TODO:add 3 more nonambient lights to fit requirements
		//"glowing" jellyfish/coral models?
	}

	@Override
	public void createViewports(){	//code directly from https://csus.instructure.com/courses/130924/files/22876803?module_item_id=6834558 
		engine.getRenderSystem().addViewport("MAIN",0f,0f,1f,1f);

		Viewport main = engine.getRenderSystem().getViewport("MAIN");
		Camera mainCam = main.getCamera();
		avatar.getLocalLocation(v);
		mainCam.setLocation(v.add(0f, spot.cameraOffset, 0f));//(new Vector3f(-2,2,2)));
//		mainCam.setU(new Vector3f(spot.x)); UVN already default to these values
//		mainCam.setV(new Vector3f(spot.y));
//		mainCam.setN(new Vector3f(spot.z));

/* 		Viewport map = engine.getRenderSystem().getViewport("MAP");
		Camera mapCam = map.getCamera();
		map.setHasBorder(true);
		map.setBorderWidth(4);
		map.setBorderColor(red.x(), red.y(), red.z());
		mapCam.setLocation(new Vector3f(0,2,0));
		mapCam.setU(new Vector3f(spot.x));
		mapCam.setV(new Vector3f(spot.z));
		mapCam.setN(new Vector3f(0,-1,0));
*/	
	}

	@Override
	public void loadSkyBoxes(){
		skybox = engine.getSceneGraph().loadCubeMap("lakeIslands");
		seabox = engine.getSceneGraph().loadCubeMap("unda da sea");
//		engine.getSceneGraph().setActiveSkyBoxTexture(skybox);
		engine.getSceneGraph().setActiveSkyBoxTexture(seabox);
		engine.getSceneGraph().setSkyBoxEnabled(true);

//TODO:underwater skybox that activates when the camera goes below the water line
	//wants to see a skybox that isn't from the base code
	}

	@Override
	public void loadSounds(){
		AudioResource bubbling;
		am = engine.getAudioManager();
//		bubbling = am.createAudioResource("567455__sound_ahead__bubbles_low_4.wav", AudioResourceType.AUDIO_SAMPLE);	//sound_ahead is the name of the sound's creator
		bubbling = am.createAudioResource("sound_ahead__bubbles_low_4.wav", AudioResourceType.AUDIO_SAMPLE);
		bubbles = new Sound(bubbling, SoundType.SOUND_EFFECT, spot.bubbleVolume, true);
		bubbles.initialize(am);
		bubbles.setMaxDistance(100f); //This is the distance at which you hear the quiet version of the sound. Anything past this is imperceptable.
		bubbles.setMinDistance(10f); //This is the distance at which you hear the loud version of the sound.
		bubbles.setRollOff(5f);
	}

	@Override
	public void initializeGame()
	{	
		lastFrameTime = System.currentTimeMillis();
		currFrameTime = System.currentTimeMillis();
		elapsTime = 0.0;
		(engine.getRenderSystem()).setWindowDimensions(1900,1000);

		im = engine.getInputManager();
		String gamepad = im.getFirstGamepadName();
		//System.out.println("Gamepad = " + gamepad);

		// ------------- positioning the camera -------------
		cam = engine.getRenderSystem().getViewport("MAIN").getCamera();
		avatar.getWorldLocation(v); cam.setLocation(v);//avatar.getWorldLocation());
		cam.translate(0f,.5f, 0f);
//		orb = new CameraOrbit3D(engine, cam, avatar, gamepad);

		//------------- Networking Section -------------
		if(serverPort != -1) setupNetworking();
    
		// ------------- Node section ------------------
		//rc = new RotationController(engine, new Vector3f(0,1,0), .001f);
		//engine.getSceneGraph().addNodeController(rc);
		//rc.addTarget(torus);
		//rc.addTarget(crystal);
		//rc.addTarget(sphere);
		//rc.addTarget(cube);
		//rc.toggle();
		//roll = new RollController(.001f);
		//engine.getSceneGraph().addNodeController(roll);
		//roll.setPitchSpeed(.001f);
		//roll.toggle();
		
		// ------------- Physics Section ------------------
		// Initialization
		physicsEngine = (engine.getSceneGraph()).getPhysicsEngine();
		physicsEngine.setGravity(gravity);
		
		//Physics World
		
		

		float tempMass = 1.0f;
		float diverMass = 10.0f;
		float tempUp[ ] = {0,1,0};
		float raftSize[ ] = {1,1,1,1};
		float pufferRadius = 1.5f;
		float dolRadius = 1.0f;
		float tempHeight = 2.0f;
		float diverHeight = 5.0f;
		boolean physicsDebug = false;
		
		double[ ] tempTransform;
		Matrix4f physicsTranslation = new Matrix4f();
		
		//Doesn't take movement into account
		//Add force in a direction to a physics object
		//Every second add a random force to avatar
		
		//Puffer Fish
		//Gravity
		puffer.getLocalTranslation(physicsTranslation);
		tempTransform = toDoubleArray(physicsTranslation.get(vals));
		pufferP = (engine.getSceneGraph()).addPhysicsSphere(tempMass, tempTransform, pufferRadius);
		pufferP.setSleepThresholds(5.0f,5.0f);
		pufferP.setBounciness(0.8f);
		puffer.setPhysicsObject(pufferP);

		//Diver
		//gravity
		/*diver.getLocalTranslation(physicsTranslation);
		tempTransform = toDoubleArray(physicsTranslation.get(vals));
		diverP = (engine.getSceneGraph()).addPhysicsCapsule(diverMass, tempTransform, dolRadius, diverHeight);
		diverP.setSleepThresholds(5.0f,5.0f);
		diverP.setBounciness(0f);
		diver.setPhysicsObject(diverP);*/
		
		
		//Raft
		raft.getLocalTranslation(physicsTranslation);
		tempTransform = toDoubleArray(physicsTranslation.get(vals));
		raftP = (engine.getSceneGraph()).addPhysicsStaticPlane(tempTransform, tempUp, 0f);
		raftP.setBounciness(0.0f);
		raftP.setDynamic(false);
		raft.setPhysicsObject(raftP);

		//Terrain
		terr.getLocalTranslation(physicsTranslation);
		tempTransform = toDoubleArray(physicsTranslation.get(vals));
		groundPlaneP = (engine.getSceneGraph()).addPhysicsStaticPlane(tempTransform, tempUp, 0.5f); //Decided that 0.5f is the best of both worlds when it comes to height for the terrain
		groundPlaneP.setBounciness(1.0f);
		terr.setPhysicsObject(groundPlaneP);
		
		if (physicsDebug)
		{
			engine.enableGraphicsWorldRender();
			engine.enablePhysicsWorldRender();
		}
		
		// ------------- inputs section ------------------
		//NOTE: associateActionWithAllKeyboards means you're using Identifier.Key to get a keyboard key
		//		associateActionWithAllGamepads means you're using Identifier.Axis to get a joystick and .Button for the 
		//			controller's buttons
		HideObjectAction hideAxes = new HideObjectAction(hideableShapes);
		//------------- avatar movement section -------------
//Keyboard TODO:add camera movement to ForBAction and mouse control to take over the turn/tiltActions
		ForBAction forward = new ForBAction(this, cam, 1, protClient);				//move actions
		ForBAction back = new ForBAction(this, cam, -1, protClient);
		LorRStrafeAction right = new LorRStrafeAction(this, cam, 1, protClient);
		LorRStrafeAction left = new LorRStrafeAction(this, cam, -1, protClient);
//		LorRTurnAction left = new LorRTurnAction(this, 1, protClient); 			//yaw left and right
//		LorRTurnAction right = new LorRTurnAction(this, -1, protClient);
		im.associateActionWithAllKeyboards(net.java.games.input.Component.Identifier.Key.W, forward, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);	
		im.associateActionWithAllKeyboards(net.java.games.input.Component.Identifier.Key.S, back, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
		im.associateActionWithAllKeyboards(net.java.games.input.Component.Identifier.Key.A, left, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
		im.associateActionWithAllKeyboards(net.java.games.input.Component.Identifier.Key.D, right, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
		im.associateActionWithAllKeyboards(net.java.games.input.Component.Identifier.Key.H, hideAxes, InputManager.INPUT_ACTION_TYPE.ON_PRESS_ONLY);
//all three axes need to be sent at the same time or else only the first item assigned to the key is hidden
			//controller
		if(gamepad != null){	//if a gamepad is plugged in
//TODO: update controls to work with controller to fit requirements
			LorRTurnAction rc = new LorRTurnAction(this, -1);
			ForBAction fc = new ForBAction(this, -1);
			im.associateAction(gamepad,net.java.games.input.Component.Identifier.Axis.X, rc, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN); 	//Axis.X/Y are the left joystick
			im.associateAction(gamepad,net.java.games.input.Component.Identifier.Axis.Y, fc, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
			//REMEMBER: buttons start at 0, but are shown starting at 1

			im.associateAction(gamepad,net.java.games.input.Component.Identifier.Button._6, hideAxes, InputManager.INPUT_ACTION_TYPE.ON_PRESS_ONLY);
		}
//https://javadoc.io/doc/net.java.jinput/jinput/2.0.7/net/java/games/input/Component.Identifier.html


		// ------------- HUD section ------------------
		//Hud Testing Section
		HUDscore = engine.getHUDmanager().addHudElement(dispStr1, hud1Color, 15, 15);
		HUDCoords = engine.getHUDmanager().addHudElement(dispStr2, hud2Color, 15,15);
		//engine.getHUDmanager().addHUDElement("Third HUD Test",white, engine.getRenderSystem().getWindowX(),engine.getRenderSystem().getWindowY());
		//for(int i = 1; i <= 5; i++)
		//engine.getHUDmanager().addHUDElement("HUD stack test", new Vector3f(.2f*i, 1f-.2f*(i-1), .5f), 15,45*i);	
				//test if deleting a middle one causes it to delete properly or crash the program
//TODO: basic GUI to fit requirements
//------------------sound section----------------------
		updateEar();
		bubbles.play();

//------------------mouse control----------------------
		initMouseMode();

//--------------Animation section--------------
		diverS.playAnimation("WALK", 1f, AnimatedShape.EndType.LOOP, 0);
	}

	public void updateEar(){
		puffer.getWorldLocation(v);
		bubbles.setLocation(v);
		cam.getLocation(v);
		am.getEar().setLocation(v);
		cam.getN(v);
		cam.getV(up);
		am.getEar().setOrientation(v, up);
	}
	
//-------------Utility----------------
	private int findViewportMiddleX(String name, String text)
	{ 	
		return (int)(engine.getRenderSystem().getViewport("MAIN").getActualWidth() - engine.getRenderSystem().getViewport(name).getActualWidth()/2 - textMidpoint(text)); 
	}
	private int findViewportMiddleY(String name, String text){
		return (int)(engine.getRenderSystem().getViewport("MAIN").getActualHeight() - engine.getRenderSystem().getViewport(name).getActualHeight()/2 - textMidpoint(text)); 
	}
	private int textMidpoint(String text)
	{ 
		if(text.isEmpty())
			return 0;
		return (int)(text.length()*10)/2; 
	} //assumes default of GLUT.BITMAP_TIMES_ROMAN_24
	private int screenMiddleY(){
		return (int)(engine.getRenderSystem().getViewport("MAIN").getActualHeight() - engine.getRenderSystem().getViewport("MAIN").getActualHeight()/2);
	}
	private int screenMiddleX(){
		return (int)(engine.getRenderSystem().getViewport("MAIN").getActualWidth() - engine.getRenderSystem().getViewport("MAIN").getActualWidth()/2); 
	}
	private void initMouseMode()
	{ 	RenderSystem rs = engine.getRenderSystem();
		sensitivity = spot.mouseSensitivity;
		centerX = screenMiddleX();//(int) (left + width/2);
		centerY = screenMiddleY();//(int) (bottom - height/2);
		isRecentering = false;
		try
			{ robot = new Robot(); } //some platforms don't support Robot
		catch (AWTException ex)
			{ throw new RuntimeException("Couldn't create Robot!"); }
		recenterMouse();
		prevMouseX = centerX; // 'prevMouse' defines the initial
		prevMouseY = centerY; // mouse position
		// also change the cursor
		Image mouse = new ImageIcon("./assets/textures/mouse reticle.png").getImage();//custom_mouse_test.png").getImage();
		Cursor faceCursor = Toolkit.getDefaultToolkit().createCustomCursor(mouse, new Point(0,0), "FaceCursor");
		GLCanvas canvas = rs.getGLCanvas();
		canvas.setCursor(faceCursor);
	}


//-------------Terrain----------------
	public void applyHeightMap(){
		for(GameObject obj: mappable){ 
//TODO: refactor so it just changes the height of the floor collider instead of obj location
			obj.getWorldLocation(v);
			height = getTerrainHeight(v.x(), v.z()) + terr.getHeight(); //height map + y position of the plane
			if(obj.getHeight() < height)
				obj.heightAdjust(height);
		}
//		cam.heightAdjust(spot.cameraOffset);
	}

//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
//TODO:implement choosing character when loading in for requirements. Example code below
	//needs to send information so the ghosts are the same
public void changeAvatar(GameObject obj, TextureImage img, ObjShape shape){
	obj.setTextureImage(img);
	obj.setShape(shape);
}
public void changeAvatar(GameObject obj, TextureImage img){
	obj.setTextureImage(img);
}
public void changeAvatar(GameObject obj, ObjShape shape){
	obj.setShape(shape);
}
//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

//		loc.set(cam.getLocation());
//		height = getTerrainHeight(loc.x(), loc.z());
//		cam.heightAdjust(height+0.5f);	//has to be done manually because it's not a GameObject
/* 
		//Objects can either be physics based or mappable, but not both
		for(GameObject obj: mappable){
			obj.getWorldLocation(loc);
//			loc.set(obj.getWorldLocation());
			height = getTerrainHeight(loc.x(), loc.z());
			obj.heightAdjust(height);
		}
		
	}*/
	
	
	
//-------------Physics----------------
	private float[] toFloatArray(double[] arr)
	{ 
		if (arr == null) return null;
		int n = arr.length;
		float[] ret = new float[n];
		for (int i = 0; i < n; i++)
		{ 
		  ret[i] = (float)arr[i];
		}
		return ret;
	}
	private double[] toDoubleArray(float[] arr)
	{ 
		if (arr == null) return null;
		
		int n = arr.length;
		
		double[] ret = new double[n];
		
		for (int i = 0; i < n; i++)
		{ 
		  ret[i] = (double)arr[i];
		}
		return ret;
	}
	
	private void calculateAvatarCollision(GameObject obj)
	{
		float strength = 1.0f; //We can make this a parameter
		float radiusOfEffect = 4.0f; //We can make this a parameter
		Vector3f avatarLocalLocation = new Vector3f(0.9f,0.0f,0.0f);  //It mostly feels right with these numbers
		Vector3f distance = distanceFromAvatar(obj);
		
		Vector3f force = distance;
		
		force.mul((-1 * strength)); 
		
		//System.out.println("Distance: " + distance + "Force: " + force);
		
		
		if (distance.equals(avatarLocalLocation, radiusOfEffect))
		{
			obj.getPhysicsObject().applyForce(force.x(), force.y(), force.z(),0.0f,0.0f,0.0f); //TODO: fix still
		}
	}
	
	private Vector3f distanceFromAvatar (GameObject obj) 
	{
		Vector3f avatarLoc = new Vector3f();
		Vector3f objLoc = new Vector3f();
		Vector3f distanceBetween = new Vector3f();
		
		getPlayerPosition(avatarLoc);
		getObjectPosition(obj, objLoc);

		avatarLoc.sub(objLoc,distanceBetween);
		
		return distanceBetween;
	}
	
	private void checkForCollisions()
	{ 
		com.bulletphysics.dynamics.DynamicsWorld dynamicsWorld;
		com.bulletphysics.collision.broadphase.Dispatcher dispatcher;
		com.bulletphysics.collision.narrowphase.PersistentManifold manifold;
		com.bulletphysics.dynamics.RigidBody object1, object2;
		com.bulletphysics.collision.narrowphase.ManifoldPoint contactPoint;
		
		dynamicsWorld =((JBulletPhysicsEngine)physicsEngine).getDynamicsWorld();
		dispatcher = dynamicsWorld.getDispatcher();
		int manifoldCount = dispatcher.getNumManifolds();
		
		for (int i=0; i < manifoldCount; i++)
		{ 
			manifold = dispatcher.getManifoldByIndexInternal(i);
			object1 = (com.bulletphysics.dynamics.RigidBody)manifold.getBody0();
			object2 =(com.bulletphysics.dynamics.RigidBody)manifold.getBody1();
			JBulletPhysicsObject obj1 = JBulletPhysicsObject.getJBulletPhysicsObject(object1);
			JBulletPhysicsObject obj2 = JBulletPhysicsObject.getJBulletPhysicsObject(object2);
			
			for (int j = 0; j < manifold.getNumContacts(); j++)
			{ 
				contactPoint = manifold.getContactPoint(j);

			} 
		} 
	}
	
	
//-------------Misc. Input----------------
	@Override
	public void update()
	{
		//--------------Time Keeping--------------
		lastFrameTime = currFrameTime;
		currFrameTime = System.currentTimeMillis();
		elapsTime = (currFrameTime - lastFrameTime);// / 1000.0; //the /1000 turns it into seconds. used more like a FrameTime variable than an Elapsed time variable. That would be "+= curr-last"
		
		//--------------Altitude--------------	
		applyHeightMap();
		
		//--------------Sound--------------
		updateEar();

		//--------------Animation--------------
		diverS.updateAnimation();
		
		//--------------Physics--------------	
			AxisAngle4f aa = new AxisAngle4f();
			Matrix4f mat = new Matrix4f();
			Matrix4f mat2 = new Matrix4f().identity();
			Matrix4f mat3 = new Matrix4f().identity();
			checkForCollisions();
			physicsEngine.update((float)elapsTime);
			for (GameObject go:engine.getSceneGraph().getGameObjects())
			{ if (go.getPhysicsObject() != null)
			  { // set translation
				mat.set(toFloatArray(go.getPhysicsObject().getTransform())); 
				mat2.set(3,0,mat.m30());
				mat2.set(3,1,mat.m31());
				mat2.set(3,2,mat.m32());
				go.setLocalTranslation(mat2);
				// set rotation
				mat.getRotation(aa);
				mat3.rotation(aa);
				go.setLocalRotation(mat3);
				} 
			} 

			try {
			calculateAvatarCollision(puffer);
			//calculateAvatarCollision(diver);
			}
			catch (Exception e){}
		
	
		
		//--------------HUD drawing----------------
		cam.getLocation(v);
		dispStr2 = "(" + v.x() + ", " + v.y() + ", " + v.z() + ")";
		engine.getHUDmanager().setHUDValue(HUDscore, dispStr1);
		engine.getHUDmanager().setHUDValue(HUDCoords, dispStr2);
		engine.getHUDmanager().setHUDPosition(HUDscore, findViewportMiddleX("MAIN", dispStr1), 15);
		
		//--------------Game Loop----------------
//		orb.updateCameraPosition();
		im.update((float)elapsTime);	
		processNetworking((float)elapsTime);
	}
	
//-------Mouse control------
	@Override
	public void mouseMoved(MouseEvent e)
	{ 	// if robot is recentering and the MouseEvent location is in the center,
		// then this event was generated by the robot
		if (isRecentering && centerX == e.getXOnScreen() && centerY == e.getYOnScreen())
		{ // mouse recentered, recentering complete
			isRecentering = false;
		}
		else
		{ // event was due to a user mouse-move, and must be processed
			curMouseX = e.getXOnScreen();
			curMouseY = e.getYOnScreen();
			yaw(prevMouseX - curMouseX);
			pitch(prevMouseY - curMouseY);
			prevMouseX = curMouseX;
			prevMouseY = curMouseY;
			// tell robot to put the cursor to the center (since user just moved it)
			recenterMouse();
			prevMouseX = centerX; // reset prev to center
			prevMouseY = centerY;
			getPlayerRotation(m);
			protClient.sendTurnMessage(m);
//turn avatar to match direction and send to protClient

		}
	}	
	private void recenterMouse()
	{
		centerX = screenMiddleX();
		centerY = screenMiddleY();
		isRecentering = true;
		robot.mouseMove((int)centerX, (int)centerY);
	}
	public void yaw(float mouseDeltaX){
		if (mouseDeltaX < 0.0) tilt = -spot.mouseSensitivity * sensitivity;
		else if (mouseDeltaX > 0.0) tilt = spot.mouseSensitivity * sensitivity;
		else tilt = 0.0f;
		engine.getRenderSystem().getViewport("MAIN").getCamera().yaw(tilt);
		avatar.yaw(tilt);
	}
	public void pitch(float mouseDeltaY){
		if (mouseDeltaY < 0.0) tilt = -spot.mouseSensitivity * sensitivity;
		else if (mouseDeltaY > 0.0) tilt = spot.mouseSensitivity * sensitivity;
		else tilt = 0.0f;
		engine.getRenderSystem().getViewport("MAIN").getCamera().limitedPitch(tilt);//pitch(tilt);
	}

// ---------- NETWORKING SECTION ----------------

	public AnimatedShape getAnimatedGhostShape(int ghostShapeID) 
	{
		AnimatedShape ghostShape;
		switch(ghostShapeID)
		{
			case 0:
			ghostShape = diverS;
			break;
			
			default:
			ghostShape = diverS;
			break;
			
		}
		return ghostShape;
	}
	
	public ObjShape getGhostShape(int ghostShapeID) 
	{
		ObjShape ghostShape;
		switch(ghostShapeID)
		{
			
			case 0:
			ghostShape = dolS;
			break;
			case 1:
			ghostShape = pufferS;
			break;
			case 2:
			ghostShape = pufferCalmS;
			break;
			/*case 3:
			ghostShape = sphereS;
			break;
			case 4:
			ghostShape = torusS;
			break;
			case 5:
			ghostShape = crystalS;
			break;
			case 6:
			ghostShape = cubeS;
			break;*/
			
			default:
			ghostShape = pufferS;
			break;
		}
		return ghostShape;
	}
	public TextureImage getGhostTexture(int ghostTexID) 
	{
				/*
					private GameObject avatar, x, y, z, terr, puffer, enemy, diver;//, cube, sphere, torus, crystal;
					private AnimatedShape diverS;
					private ObjShape dolS, xAxis, yAxis, zAxis, terrS, pufferS, pufferCalmS;//, sphereS, torusS,  crystalS, cubeS;
					private TextureImage doltx, hills, grass, pufferX, pufferAltX;
			*/
		TextureImage ghostTex;
		switch(ghostTexID)
		{
			case 0:
			ghostTex = ghostT;
			break;
			
			case 1:
			ghostTex = doltx;
			break;
			
			case 2:
			ghostTex = pufferX;
			break;
			
			case 3:
			ghostTex = pufferAltX;
			break;
			
			/*case 4:
			ghostTex = pufferCalmX;
			break;*/ //add if time
			
			default:
			ghostTex = pufferX;
			break;
		}
		return ghostTex;
	}
	
	public GhostManager getGhostManager() { return gm; }
	public Engine getEngine() { return engine; }
	
	private void setupNetworking()
	{	isClientConnected = false;	
		try 
		{	protClient = new ProtocolClient(InetAddress.getByName(serverAddress), serverPort, serverProtocol, this);
		} 	catch (UnknownHostException e) 
		{	e.printStackTrace();
		}	catch (IOException e) 
		{	e.printStackTrace();
		}
		if (protClient == null)
		{	System.out.println("missing protocol host");
		}
		else
		{	// Send the initial join message with a unique identifier for this client
			System.out.println("sending join message to protocol host");
			protClient.sendJoinMessage();
		}
	}
	
	protected void processNetworking(float elapsTime)
	{	// Process packets received by the client from the server
		if (protClient != null)
			protClient.processPackets();
      else
         System.out.println("protClient is null");
	}

	public void getPlayerPosition(Vector3f dest) { avatar.getWorldLocation(v); dest.set(v); }//return avatar.getWorldLocation(); }
	public void getPlayerRotation(Matrix4f dest) { avatar.getWorldRotation(m); dest.set(m); }//return avatar.getWorldRotation(); }
	
	public void getObjectPosition(GameObject obj, Vector3f dest) { obj.getWorldLocation(v); dest.set(v); }
	public void getObjectRotation(GameObject obj, Matrix4f dest) { obj.getWorldRotation(m); dest.set(m); }

	public void setIsConnected(boolean value) { this.isClientConnected = value; }
	
	private class SendCloseConnectionPacketAction extends AbstractInputAction
	{	@Override
		public void performAction(float time, net.java.games.input.Event evt) 
		{	if(protClient != null && isClientConnected == true)
			{	protClient.sendByeMessage();
			}
		}
	}

	public void keyPressed(KeyEvent a)
	{	switch (a.getKeyCode())
		{	case KeyEvent.VK_ESCAPE:
				if(protClient != null)
					protClient.sendByeMessage();
				shutdown();
				System.exit(0);
				break;

			case KeyEvent.VK_DOWN:
				if (sensitivity > 0.2f)
					sensitivity = sensitivity - 0.1f;
				break;
				
			case KeyEvent.VK_UP:
				if (sensitivity < 1.8f)
					sensitivity = sensitivity + 0.1f;
				break;
		}
	}
}