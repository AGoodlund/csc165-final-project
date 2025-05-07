package a2;

import tage.*;
import tage.Light.LightType;
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

	private int health = 10;

//-------------HUD elements--------------
	private float[] hud1Color = spot.red;
	private float[] hud2Color = spot.yellow;
	private String dispStr1 = " ", dispStr2;
	private int HUDscore, HUDCoords;

//-------------Visuals--------------
	private GameObject avatar, x, y, z, terr, puffer, diver, enemy;//, cube, sphere, torus, crystal;

	private AnimatedShape diverS;
	private ObjShape dolS, xAxis, yAxis, zAxis, terrS, pufferS, pufferCalmS;//, sphereS, torusS,  crystalS, cubeS;
	private TextureImage doltx, hills, grass, pufferX, pufferAltX;

	private Light light1;//, spotlightR, spotlightG, spotlightB;
	private int skybox, seabox;

	private GameObject raft;
	private ObjShape raftS;
	private TextureImage raftx;

	private GameObject laser;
	private ObjShape laserS;

	private GameObject jellyR, jellyG, jellyY;
	private ObjShape jellyfish;
	private Light red, green, yellow;

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
/* 	private Robot robot;
	private float curMouseX, curMouseY, centerX, centerY;
	private float prevMouseX, prevMouseY; // loc of mouse prior to move
	private boolean isRecentering; //indicates the Robot is in action
	private float tilt;

	private float sensitivity;
*/


//-------------Height Map----------------
	private float height;
	private ArrayList<GameObject> mappable = new ArrayList<GameObject>(); //objects that follow height map
	private ArrayList<PhysicsObject> mappableP = new ArrayList<PhysicsObject>(); //Physics objects that follow height map

	//private ArrayList<PhysicsObject> mappableP = new ArrayList<PhysicsObject>(); //Physics objects that follow height map


//-------------Helpers----------------
	private Vector3f v = new Vector3f();
	private Matrix4f m = new Matrix4f();
	
//-------------Physics----------------
	private PhysicsEngine physicsEngine;
	private PhysicsObject dolP, ghostP, raftP, pufferP, groundPlaneP, avatarP, groundingP;
	private float[] gravity = {0f, 0f,0f};//-20f, 0f};//-6f, 0f};
	private float vals[] = new float[16]; 
	//mappableP.add(pufferP);
	
//Networking
	public ObjShape getEnemyShape() { return pufferS; }
	public TextureImage getEnemyTexture() { return pufferAltX; }
	
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

		terrS = new Plane();//TerrainPlane(1000); //pixels per axis is 1000 X 1000

		raftS = new Cube();
//		waterS = new Plane();
		diverS = new AnimatedShape("Diver.rkm", "Diver.rks");
			diverS.loadAnimation("WALK", "Diver_walk.rka");
		jellyfish = new ImportedModel("Jellyfish.obj");

		xAxis = new Line(new Vector3f(0f,0f,0f), new Vector3f(3f,0f,0f));
		yAxis = new Line(new Vector3f(0f,0f,0f), new Vector3f(0f,3f,0f));
		zAxis = new Line(new Vector3f(0f,0f,0f), new Vector3f(0f,0f,-3f));
		laserS = new Line(new Vector3f(0f,1f,0f), new Vector3f(0f,1f,-30f));
	}

	@Override
	public void loadTextures(){	
		doltx = new TextureImage("ULPDuv.png");
		ghostT = new TextureImage("oiter.png");
		pufferX = new TextureImage("Pufferfish_Angry_Spiney.png");
		pufferAltX = new TextureImage("Pufferfish_Angry_SpineyAlt.png");

//		hills = new TextureImage("heightmap map.png");
		grass = new TextureImage("sand.png");
//		hills = new TextureImage("hills.jpg");
//		grass = new TextureImage("grass.jpg");
	}

	@Override
	public void buildObjects()
	{	Matrix4f initialTranslation, initialScale, initialRotation;

		// build dolphin in the center of the window
		avatar = new GameObject(GameObject.root(), diverS);//new Cube());//dolS, doltx);
		initialTranslation = (new Matrix4f()).translation(0f,2f,0f);

		avatar.setLocalTranslation(initialTranslation);
//		initialScale = new Matrix4f().scale(1f,2f,1f);
		mappable.add(avatar);
//		avatar.getRenderStates().disableRendering();
		avatar.getRenderStates().setColor(new Vector3f(spot.black));
		avatar.getRenderStates().setHasSolidColor(true);

		jellyR = new GameObject(GameObject.root(), jellyfish);
			initialTranslation = (new Matrix4f()).translation(50f,15f,0f);
			initialScale = new Matrix4f().scaling(0.5f);
			jellyR.setLocalTranslation(initialTranslation);
			jellyR.setLocalScale(initialScale);

		jellyG = new GameObject(GameObject.root(), jellyfish);
			initialTranslation = (new Matrix4f()).translation(0f,15f,0f);
			initialScale = new Matrix4f().scaling(0.5f);
			jellyG.setLocalTranslation(initialTranslation);
			jellyG.setLocalScale(initialScale);

		jellyY = new GameObject(GameObject.root(), jellyfish);
			initialTranslation = (new Matrix4f()).translation(-50f,15f,0f);
			initialScale = new Matrix4f().scaling(0.5f);
			jellyY.setLocalTranslation(initialTranslation);
			jellyY.setLocalScale(initialScale);

		jellyR.getRenderStates().setColor(new Vector3f(spot.red));
		jellyG.getRenderStates().setColor(new Vector3f(spot.green));
		jellyY.getRenderStates().setColor(new Vector3f(spot.yellow));
		jellyR.getRenderStates().setHasSolidColor(true);
		jellyG.getRenderStates().setHasSolidColor(true);
		jellyY.getRenderStates().setHasSolidColor(true);//or set them to hasPositionalColor(true);
		jellyR.getRenderStates().setRenderHiddenFaces(true);
		jellyG.getRenderStates().setRenderHiddenFaces(true);
		jellyY.getRenderStates().setRenderHiddenFaces(true);

		laser = new GameObject(GameObject.root(), laserS);
		laser.getRenderStates().setColor(new Vector3f(spot.red));
		laser.getRenderStates().setHasSolidColor(true);
		laser.setParent(avatar);
		laser.propagateRotation(true);
		laser.propagateTranslation(true);

		// build Enemy Pufferfish
		enemy = new GameObject(GameObject.root(), pufferS, pufferAltX);
		initialTranslation = (new Matrix4f()).translation(8f,0f,-3f);

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
		initialTranslation = (new Matrix4f()).translation(0f,0f,0f); 

		terr.setLocalTranslation(initialTranslation);
		initialScale = new Matrix4f().scaling(120f,1f,30f);
		terr.setLocalScale(initialScale);
//		terr.setHeightMap(hills);
		// set tiling for terrain texture
		terr.getRenderStates().setTiling(1);
		terr.getRenderStates().setTileFactor(10);

/* 
		terr.translate(0f,-10f,0f); //Removed so I could use terrain again

		raft = new GameObject(GameObject.root(),raftS);
		initialTranslation=(new Matrix4f()).translate(0f,-.5f+20f,0f);
		raft.setLocalTranslation(initialTranslation);
		initialScale = (new Matrix4f()).scaling(3f,.5f,5f);
		raft.setLocalScale(initialScale);
		raft.getRenderStates().setHasSolidColor(true); //TODO:make a texture for this to be square logs
		raft.getRenderStates().setColor(new Vector3f(0.725f, 0.478f, 0.341f));
*/
/* 		water = new GameObject(GameObject.root(), waterS);
		water.getRenderStates().setColor(new Vector3f(spot.blue));
		water.getRenderStates().setHasSolidColor(true);
		initialTranslation = new Matrix4f().translation(0f,-.25f+20f,0f);
		water.setLocalTranslation(initialTranslation);
		initialScale = new Matrix4f().scaling(200.0f, 1.0f, 200.0f);
		water.setLocalScale(initialScale);
*/
		diver = new GameObject(GameObject.root(), diverS);
		initialTranslation = new Matrix4f().translation(0f,1.5f,-1f);
		initialScale = new Matrix4f().scaling(.5f);
		diver.setLocalTranslation(initialTranslation);
		diver.setLocalScale(initialScale);
		diver.yaw(180f);
		diver.getRenderStates().setPositionalColor(true);
		mappable.add(diver);
	}
//TODO:remove. not using heightmapping for top-down
	public float getTerrainHeight(float x, float z)
	{
		float height = terr.getHeight(x,z);
		return height;
	}

	@Override
	public void initializeLights()
	{	Light.setGlobalAmbient(.3f,.3f,.3f);//0.5f, 0.5f, 0.5f);
		light1 = new Light();
		light1.setLocation(new Vector3f(5.0f, 4.0f, 2.0f));
		(engine.getSceneGraph()).addLight(light1);

		red = new Light();
		red.setType(LightType.SPOTLIGHT);
		red.setDirection(new Vector3f(0f,-1f,0f));
		red.setColor(spot.dimRed);
		jellyR.getWorldLocation(v);
		red.setLocation(v);
//		setType() defaults to positional

		green = new Light();
		green.setType(LightType.SPOTLIGHT);
		green.setDirection(new Vector3f(0f,-1f,0f));
		green.setColor(spot.dimGreen);
		jellyG.getWorldLocation(v);
		green.setLocation(v);

		yellow = new Light();
		yellow.setType(LightType.SPOTLIGHT);
		yellow.setDirection(new Vector3f(0f,-1f,0f));
		yellow.setColor(spot.dimYellow);
		jellyY.getWorldLocation(v);
		yellow.setLocation(v);

		engine.getSceneGraph().addLight(red);
		engine.getSceneGraph().addLight(green);
		engine.getSceneGraph().addLight(yellow);
	}

	@Override
	public void createViewports(){
		engine.getRenderSystem().addViewport("MAIN",0f,0f,1f,1f);

		Viewport main = engine.getRenderSystem().getViewport("MAIN");
		Camera mainCam = main.getCamera();
		avatar.getLocalLocation(v);
		mainCam.setLocation(v.add(0f, spot.cameraOffset, 0f));
	}

	@Override
	public void loadSkyBoxes(){
//		skybox = engine.getSceneGraph().loadCubeMap("lakeIslands");
		seabox = engine.getSceneGraph().loadCubeMap("unda da sea");
		engine.getSceneGraph().setActiveSkyBoxTexture(seabox);
		engine.getSceneGraph().setSkyBoxEnabled(true);
	}

	@Override
	public void loadSounds(){
		AudioResource bubbling;
		am = engine.getAudioManager();
		bubbling = am.createAudioResource("sound_ahead__bubbles_low_4.wav", AudioResourceType.AUDIO_SAMPLE);//sound_ahead is the name of the sound's creator
		bubbles = new Sound(bubbling, SoundType.SOUND_EFFECT, spot.bubbleVolume, true);
		bubbles.initialize(am);
		bubbles.setMaxDistance(20f); //This is the distance at which you hear the quiet version of the sound. Anything past this is imperceptable.
		bubbles.setMinDistance(2f); //This is the distance at which you hear the loud version of the sound.
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
		avatar.getWorldLocation(v); 
		cam.setLocation(v);
		cam.heightAdjust(spot.cameraOffset);
		cam.lookAt(avatar);

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
		float tempUp[ ] = {0,1,0};
		float pufferRadius = 1.5f;
		float dolRadius = 1.0f;
		float tempHeight = 2.0f;
		boolean physicsDebug = true;

		
		double[ ] tempTransform;
		Matrix4f physicsTranslation = new Matrix4f();
		
		//Doesn't take movement into account
		//Add force in a direction to a physics object
		//Every second add a random force to avatar
/* 		only the player and harpoons will need physics because everything else will be controlled by the Server

		//Puffer Fish
		//Gravity
		puffer.getLocalTranslation(physicsTranslation);
		tempTransform = toDoubleArray(physicsTranslation.get(vals));
		pufferP = (engine.getSceneGraph()).addPhysicsSphere(tempMass, tempTransform, pufferRadius);

		//pufferP.isDynamic() = true;
		pufferP.setSleepThresholds(5.0f,5.0f);
		pufferP.setBounciness(0.8f);
		puffer.setPhysicsObject(pufferP);
		
		raft.getLocalTranslation(physicsTranslation);
		tempTransform = toDoubleArray(physicsTranslation.get(vals));
		groundPlaneP = (engine.getSceneGraph()).addPhysicsBox(0f, tempTransform, new float[]{6f,1f,10f});
		groundPlaneP.setBounciness(0.0f);
		raft.setPhysicsObject(groundPlaneP);
*/
 		//Terrain
		terr.getLocalTranslation(physicsTranslation);
		tempTransform = toDoubleArray(physicsTranslation.get(vals));
		groundPlaneP = (engine.getSceneGraph()).addPhysicsStaticPlane(tempTransform, spot.y, 0.5f); //Decided that 0.5f is the best of both worlds when it comes to height for the terrain
		groundPlaneP.setBounciness(0.02f);
		terr.setPhysicsObject(groundPlaneP);

//		groundingP = engine.getSceneGraph().addPhysicsBox(0f, tempTransform, new float[]{4f,2f,4f});
//		groundingP.setBounciness(0.02f);

		//avatar
		avatar.getLocalTranslation(physicsTranslation);
		physicsTranslation.m31(physicsTranslation.m31()+0.5f);
		tempTransform = toDoubleArray(physicsTranslation.get(vals));
//		avatarP = engine.getSceneGraph().addPhysicsBox(3f, tempTransform, new float[]{2f,4f,2f});
//		avatarP = engine.getSceneGraph().addPhysicsSphere(300f, tempTransform, 1f)
		avatarP = engine.getSceneGraph().addPhysicsCapsule(3f, tempTransform, 1f,2f);
		avatarP.setSleepThresholds(5.0f,5.0f);
		avatarP.setBounciness(0f);
		avatarP.setFriction(1f);
		avatar.setPhysicsObject(avatarP);

		
		if (physicsDebug)
		{
			engine.enablePhysicsWorldRender();
//			engine.disableGraphicsWorldRender();
		}
		
		// ------------- inputs section ------------------
		//NOTE: associateActionWithAllKeyboards means you're using Identifier.Key to get a keyboard key
		//		associateActionWithAllGamepads means you're using Identifier.Axis to get a joystick and .Button for the 
		//			controller's buttons
		HideObjectAction hideAxes = new HideObjectAction(hideableShapes);
		//------------- avatar movement section -------------
//Keyboard TODO:add camera movement to ForBAction and mouse control to take over the turn/tiltActions
		ForBAction forward = new ForBAction(this, cam, -1, protClient);				//move actions
		ForBAction back = new ForBAction(this, cam, 1, protClient);
		LorRStrafeAction right = new LorRStrafeAction(this, cam, 1, protClient);
		LorRStrafeAction left = new LorRStrafeAction(this, cam, -1, protClient);
		LorRTurnAction Tleft = new LorRTurnAction(this, 1, protClient); 			//yaw left and right
		LorRTurnAction Tright = new LorRTurnAction(this, -1, protClient);
		im.associateActionWithAllKeyboards(net.java.games.input.Component.Identifier.Key.W, forward, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);	
		im.associateActionWithAllKeyboards(net.java.games.input.Component.Identifier.Key.S, back, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
		im.associateActionWithAllKeyboards(net.java.games.input.Component.Identifier.Key.A, left, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
		im.associateActionWithAllKeyboards(net.java.games.input.Component.Identifier.Key.D, right, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
		im.associateActionWithAllKeyboards(net.java.games.input.Component.Identifier.Key.LEFT, Tleft, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
		im.associateActionWithAllKeyboards(net.java.games.input.Component.Identifier.Key.RIGHT, Tright, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
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

//------------------mouse control----------------------TODO:remove
//		initMouseMode();

//--------------Animation section--------------
		diverS.playAnimation("WALK", 1f, AnimatedShape.EndType.LOOP, 0);
	}

	public void updateEar(){
		puffer.getWorldLocation(v);
		bubbles.setLocation(v);
		avatar.getLocalLocation(v); //changing from cam to avatar since the camera is sitting above 
		am.getEar().setLocation(v);
		cam.getN(v);
//		cam.getV(up);
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
//TODO:deleting mouse movement
/* 	private void initMouseMode()
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
*/

//-------------Terrain---------------- 
/*	public void applyHeightMap(){
		for(GameObject obj: mappable){ 
//TODO: refactor so it just changes the height of the floor collider instead of obj location
			obj.getWorldLocation(v);
			height = getTerrainHeight(v.x(), v.z()) + terr.getHeight(); //height map + y position of the plane
			if(obj.getHeight() < height)
				obj.heightAdjust(height);
		}
		avatar.getWorldLocation(v);
		cam.setLocation(v);
		cam.heightAdjust(spot.cameraOffset);
	}
*/
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
	
//-------------Physics----------------TODO:so much needs fixing
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
//TODO:don't initialize in a function run in update()
		
		force.mul((-1 * strength)); 
		
		//System.out.println("Distance: " + distance + "Force: " + force);
		
		
		if (distance.equals(avatarLocalLocation, radiusOfEffect))
		{
//			obj.getPhysicsObject().applyForce(force.x(), force.y(), force.z(),0.0f,0.0f,0.0f); //TODO: fix still
		}
	}
	
	private Vector3f distanceFromAvatar (GameObject obj) 
	{
		Vector3f avatarLoc = new Vector3f();
		Vector3f objLoc = new Vector3f();
		Vector3f distanceBetween = new Vector3f();
//TODO:don't initialize in a function run in update()
		
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
//		applyHeightMap();
		
		//--------------Altitude--------------	
/* 		applyHeightMap();
		cam.getLocation(v);
		if(swap && v.y() < 0f){
			light1.disable();
			engine.getSceneGraph().setActiveSkyBoxTexture(seabox);
			swap = false;
		}*/
		//--------------Sound--------------
		updateEar();

		//--------------Animation--------------
		diverS.updateAnimation();
		//--------------Physics--------------	
		AxisAngle4f aa = new AxisAngle4f();
		Matrix4f mat = new Matrix4f();
		Matrix4f mat2 = new Matrix4f().identity();
		Matrix4f mat3 = new Matrix4f().identity();
			//TODO:don't initialize in update()

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
//			mat.getRotation(aa); rotation doesn't need to be enforced
//			mat3.rotation(aa);
//			go.setLocalRotation(mat3);
			} 
		} 

//		calculateAvatarCollision(puffer);//TODO:do not hardcode specific enemies. just find special interactions in the loop

		//make avatar's grounding box follow the avatar at the groundplane's height
//		cam.getLocation(v);
//		m.identity().setTranslation(v.x(), -1f, v.z()); //TODO:this only lines up with the terrain at the edges and center. Only the highest and lowest spots
//		groundingP.setTransform(toDoubleArray(m.get(vals)));
//TODO: Decide if heightmapping is worth it or if it should be a top down twin-stick shooter. Current movement would work for that and just need to be able to turn and shoot
		//physics would be specifically for if something has been hit. If projectile hits an enemy it deals damage. If enemy hits a player they take damage
		//movement would go back to the old move camera and avatar in a direction by an amount, but with the camera y above the avatar and looking straight down

		
		//--------------HUD drawing----------------
		cam.getLocation(v);
		dispStr2 = "(" + v.x() + ", " + v.y() + ", " + v.z() + ")";
		engine.getHUDmanager().setHUDValue(HUDscore, dispStr1);
		engine.getHUDmanager().setHUDValue(HUDCoords, dispStr2);
		engine.getHUDmanager().setHUDPosition(HUDscore, findViewportMiddleX("MAIN", dispStr1), 15);
		
		//--------------Game Loop----------------
		im.update((float)elapsTime);


		//--------------Networking Update----------------
		processNetworking((float)elapsTime);
	}
	

//-------Mouse control------TODO:remove
/* 	@Override
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
		cam.yaw(tilt);
		avatar.yaw(tilt);
	}
	public void pitch(float mouseDeltaY){
		if (mouseDeltaY < 0.0) tilt = -spot.mouseSensitivity * sensitivity;
		else if (mouseDeltaY > 0.0) tilt = spot.mouseSensitivity * sensitivity;
		else tilt = 0.0f;
		cam.limitedPitch(tilt);//pitch(tilt);
	}
*/

// ---------- NETWORKING SECTION ----------------

	public ObjShape getGhostShape() { return ghostS; }
	public TextureImage getGhostTexture() { return ghostT; }
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
/* 			case KeyEvent.VK_UP:
				sensitivity+=0.001f;
				if(sensitivity > 1f)
					sensitivity = 1f;
				break;
			case KeyEvent.VK_DOWN:
				if (sensitivity > 0.1f)
					sensitivity = sensitivity - 0.1f;
				break;
				
			case KeyEvent.VK_UP:
				if (sensitivity < 1.9f)
					sensitivity = sensitivity + 0.1f;
				break;
*/		}
	}
}