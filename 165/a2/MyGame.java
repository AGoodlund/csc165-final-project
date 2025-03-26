package a2;

import tage.*;
import tage.shapes.*;
import tage.input.*;
import tage.input.action.*;

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
import tage.networking.IGameConnection.ProtocolType;

import java.util.ArrayList;
import javax.swing.*;
import tage.nodeControllers.*;

public class MyGame extends VariableFrameRateGame
{
	private static Engine engine;
	private Camera cam;
	private CameraOrbit3D orb;
	private InputManager im;

	private ArrayList<GameObject> hideableShapes = new ArrayList<GameObject>();
	private ArrayList<GameObject> disarmables = new ArrayList<GameObject>();

	public boolean paused=false, mounted = true;
	public static int counter=0;

	private boolean disSphere=false,disCube=false,disTorus=false;
	private double lastFrameTime, currFrameTime, elapsTime;
	private float spDist, cuDist, toDist;

//-------------HUD elements--------------
	private Vector3f red = new Vector3f(1f,0f,0f), green = new Vector3f(0f,1f,0f), blue = new Vector3f(0f,0f,1f), white = new Vector3f(1f,1f,1f);
	private Vector3f purple = new Vector3f(1f,1f,0f), yellow = new Vector3f(1f,0f,1f), teal = new Vector3f(0f,1f,1f), black = new Vector3f(0f,0f,0f);
		//same colors are in public float arrays within HUDmanager.java. TODO: Should be moved here for ease of use later 
	
	private Vector3f hud1Color = red;
	private Vector3f hud2Color = yellow;
	private String dispStr1 = " ", dispStr2;//, counterStr;
	private int HUDscore, HUDCoords;

//-------------game visuals--------------
	private GameObject avatar, cube, x, y, z, sphere, torus,  crystal, ground;
	private ObjShape dolS, cubeS, xAxis, yAxis, zAxis, sphereS, torusS,  crystalS, groundS;
	private TextureImage doltx, cubeX,sphereX,torusX,brokeX,cubeClose, sphereClose,torusClose, groundX, sphereSafe, cubeSafe, torusSafe;
	private Light light1, spotlightR, spotlightG, spotlightB;

	private Matrix4f initialRotation;
	private RotationController rc;
	private RollController roll;

//-------------Networking----------------

	private GhostManager gm;
	private String serverAddress;
	private int serverPort;
	private ProtocolType serverProtocol;
	private ProtocolClient protClient;
	private boolean isClientConnected = false;
	private ObjShape ghostS;
	private TextureImage ghostT;


//	public MyGame() { super(); System.out.println("Single Player boot up"); }
	public MyGame(String serverAddress, int serverPort, String protocol)
	{	super();
		gm = new GhostManager(this);
		this.serverAddress = serverAddress;
		this.serverPort = serverPort;
		if (protocol.toUpperCase().compareTo("TCP") == 0)
			this.serverProtocol = ProtocolType.TCP;
		else
			this.serverProtocol = ProtocolType.UDP;
		System.out.println("Networking booting up");
	}

	public GameObject getAvatar(){ return avatar; }
	

/* 	public static void main(String[] args)
	{	MyGame game = new MyGame();
		engine = new Engine(game);
		game.initializeSystem();
		game.game_loop();
	}*/
	public static void main(String[] args){	
		MyGame game;
//		if(args.length == 0)
	//		game = new MyGame();
		//else
		game = new MyGame(args[0], Integer.parseInt(args[1]), args[2]);
		engine = new Engine(game);
		game.initializeSystem();
		game.game_loop();
	}

	@Override
	public void loadShapes()
	{	dolS = new ImportedModel("dolphinHighPoly.obj");
		ghostS = new ImportedModel("dolphinHighPoly.obj");
//		dolS = new ImportedModel("dolphinLowPoly.obj");

		cubeS = new Cube();
 		sphereS = new Sphere();
		torusS = new Torus(0.5f, 0.2f, 48);
		crystalS = new ManualCrystal();
		
		groundS = new Plane();

		xAxis = new Line(new Vector3f(0f,0f,0f), new Vector3f(3f,0f,0f));
		yAxis = new Line(new Vector3f(0f,0f,0f), new Vector3f(0f,3f,0f));
		zAxis = new Line(new Vector3f(0f,0f,0f), new Vector3f(0f,0f,-3f));
	}

	@Override
	public void loadTextures()
	{	doltx = new TextureImage("Dolphin_HighPolyUV.png");
		ghostT = new TextureImage("oiter.png");
 
		cubeX = new TextureImage("MUSHROOMS.png");
		cubeClose = new TextureImage("flower.png");
 		sphereX = new TextureImage("planet.png");
		sphereClose = new TextureImage("ice.jpg");					
		torusX = new TextureImage("space station.png");
		torusClose = new TextureImage("starfield2048.jpg");			
		brokeX = new TextureImage("black hole.png");
		groundX = new TextureImage("oiter.png");

		sphereSafe = new TextureImage("moon.jpg");
		cubeSafe = new TextureImage("squareMoonMap.jpg");
		torusSafe = new TextureImage("ice.jpg");
	}

	@Override
	public void buildObjects()
	{	Matrix4f initialTranslation, initialScale;

		// build dolphin in the center of the window
		avatar = new GameObject(GameObject.root(), dolS, doltx);
		initialTranslation = (new Matrix4f()).translation(0f,0f,0f);
		initialScale = (new Matrix4f()).scaling(3.0f);
		avatar.setLocalTranslation(initialTranslation);
		avatar.setLocalScale(initialScale);
		initialRotation = (new Matrix4f()).rotationY((float)java.lang.Math.toRadians(180f));//135.0f));
		avatar.setLocalRotation(initialRotation);
		avatar.getRenderStates().setPositionalColor(true);
//		avatar.getRenderStates().hasLighting(false);
 
		//build cube
		cube = new GameObject(GameObject.root(), cubeS, cubeX);
		initialTranslation = (new Matrix4f()).translation(13.0f,2.0f,-15.0f);
		initialScale = (new Matrix4f()).scaling(2.5f);
		cube.setLocalTranslation(initialTranslation);
		cube.setLocalScale(initialScale);
		disarmables.add(cube);
//		cube.getRenderStates().setPositionalColor(true);
//		cube.getRenderStates().hasLighting(false);
 
		//build torus
		torus = new GameObject(GameObject.root(), torusS, torusX);
		initialTranslation = (new Matrix4f()).translation(25.0f,1.0f,35.0f);
		initialScale = (new Matrix4f()).scaling(2.0f);
		torus.setLocalTranslation(initialTranslation);
		torus.setLocalScale(initialScale);
		torus.getRenderStates().setTiling(1);	//this looks weird. Check setTiling function to figure out what's wrong
		disarmables.add(torus);
//		torus.getRenderStates().disableRendering();
		torus.getRenderStates().setPositionalColor(true);
//		torus.getRenderStates().hasLighting(false);

		//build sphere
		sphere = new GameObject(GameObject.root(), sphereS, sphereX);
		initialTranslation = (new Matrix4f()).translation(-15.0f,1.0f,-30.0f);
		initialScale = (new Matrix4f()).scaling(1.2f);
		sphere.setLocalTranslation(initialTranslation);
		sphere.setLocalScale(initialScale);
		disarmables.add(sphere);
//		sphere.getRenderStates().setPositionalColor(true);
//		sphere.getRenderStates().hasLighting(false);

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

		//build lines
		x = new GameObject(GameObject.root(), xAxis);
		y = new GameObject(GameObject.root(), yAxis);
		z = new GameObject(GameObject.root(), zAxis);
		(x.getRenderStates()).setColor(new Vector3f(red));
		(y.getRenderStates()).setColor(new Vector3f(green));
		(z.getRenderStates()).setColor(new Vector3f(blue));
		hideableShapes.add(x); hideableShapes.add(y); hideableShapes.add(z);

		//build ground
		ground = new GameObject(GameObject.root(), groundS, groundX);
//			ground.getRenderStates().setColor(new Vector3f(.1f,.3f,.7f));
		initialTranslation = new Matrix4f().translation(0f,-1f,0f);
		initialScale = new Matrix4f().scaling(300f);
		ground.setLocalTranslation(initialTranslation);
		ground.setLocalScale(initialScale);
		ground.getRenderStates().setTiling(2);
		ground.getRenderStates().setTileFactor(100);
		ground.getRenderStates().disableRendering();
//ground.getRenderStates().setPositionalColor(true);
//ground.getRenderStates().hasLighting(false);
	}

	@Override
	public void initializeLights()
	{	/*Light.setGlobalAmbient(0.5f, 0.5f, 0.5f);
		light1 = new Light();
		light1.setLocation(new Vector3f(5.0f, 4.0f, 2.0f));
		(engine.getSceneGraph()).addLight(light1);
		spotlightR = new Light(); spotlightR.setType(Light.LightType.POSITIONAL); spotlightR.setSpecular(cube.getLocalLocation().x(), cube.getLocalLocation().y()-3f, cube.getLocalLocation().z());	//cube underlight
			spotlightR.setLocation(new Vector3f(13f,-13f,-15f)); engine.getSceneGraph().addLight(spotlightR);
*/		spotlightG = new Light(); spotlightG.setType(Light.LightType.POSITIONAL); spotlightG.setSpecular(yellow.x(), yellow.y(), yellow.z());	//torus inner light
			spotlightG.setLocation(new Vector3f(25f,10f,-22f)); engine.getSceneGraph().addLight(spotlightG);
//		spotlightB = new Light(); spotlightB.setType(Light.LightType.POSITIONAL); spotlightB.setSpecular(sphere.getLocalLocation().x(), sphere.getLocalLocation().y()+2f, sphere.getLocalLocation().z());	//sphere halo
//			spotlightB.setLocation(new Vector3f(0,7f,-30f)); engine.getSceneGraph().addLight(spotlightB);
	}

	@Override
	public void createViewports(){	//code directly from https://csus.instructure.com/courses/130924/files/22876803?module_item_id=6834558 
		engine.getRenderSystem().addViewport("MAIN",0f,0f,1f,1f);
//		engine.getRenderSystem().addViewport("MAP", .75f, 0f, .25f, .25f);

		Viewport main = engine.getRenderSystem().getViewport("MAIN");
		Camera mainCam = main.getCamera();
		mainCam.setLocation(new Vector3f(-2,0,2));
		mainCam.setU(new Vector3f(spot.x));
		mainCam.setV(new Vector3f(spot.y));
		mainCam.setN(new Vector3f(spot.z));

/* 		Viewport map = engine.getRenderSystem().getViewport("MAP");
		Camera mapCam = map.getCamera();
		map.setHasBorder(true);
		map.setBorderWidth(4);
		map.setBorderColor(red.x(), red.y(), red.z());
		mapCam.setLocation(new Vector3f(0,2,0));
		mapCam.setU(new Vector3f(spot.x));
		mapCam.setV(new Vector3f(spot.z));
		mapCam.setN(new Vector3f(0,-1,0));
*/	}
	@Override
	public void initializeGame()
	{	lastFrameTime = System.currentTimeMillis();
		currFrameTime = System.currentTimeMillis();
		elapsTime = 0.0;
		(engine.getRenderSystem()).setWindowDimensions(1900,1000);

		im = engine.getInputManager();
		String gamepad = im.getFirstGamepadName();
//System.out.println("Gamepad = " + gamepad);
		// ------------- positioning the camera -------------
		cam = engine.getRenderSystem().getViewport("MAIN").getCamera();
		orb = new CameraOrbit3D(engine, cam, avatar, gamepad);
		
		// ------------- inputs section ------------------
 		

//NOTE: associateActionWithAllKeyboards means you're using Identifier.Key to get a keyboard key
//		associateActionWithAllGamepads means you're using Identifier.Axis to get a joystick and .Button for the 
//			controller's buttons

		// ------------- Node section ------------------
		rc = new RotationController(engine, new Vector3f(0,1,0), .001f);
		engine.getSceneGraph().addNodeController(rc);
		rc.addTarget(torus);
		rc.addTarget(crystal);
		rc.addTarget(sphere);
		rc.addTarget(cube);
		rc.toggle();
//		rc.setSpeed(1f);

		roll = new RollController(.001f);
		engine.getSceneGraph().addNodeController(roll);
		roll.setPitchSpeed(.001f);
		roll.toggle();

		//All things are supposed to default to no motion and only start moving once disarmed
/* 			Camera mapCam = engine.getRenderSystem().getViewport("MAP").getCamera();
			ForBAction mapZoomIn = new ForBAction(mapCam, 1, .25f);
			ForBAction mapZoomOut = new ForBAction(mapCam, -1);
			UorDMoveAction mapMoveUp = new UorDMoveAction(mapCam, 1);
			UorDMoveAction mapMoveBack = new UorDMoveAction(mapCam, -1);
			LorRStrafeAction mapMoveLeft = new LorRStrafeAction(mapCam, -1);
			LorRStrafeAction mapMoveRight = new LorRStrafeAction(mapCam, 1);
*/			DisarmAction disarm = new DisarmAction(avatar, disarmables, roll, rc);
			HideObjectAction hideAxes = new HideObjectAction(hideableShapes);

//avatar movement
//		if(gamepad == null){	//if no gamepad is plugged in
//https://www.javadoc.io/doc/net.java.jinput/jinput/2.0.7/net/java/games/input/Component.Identifier.Key.html
			ForBAction forward = new ForBAction(this, 1, protClient);			//move actions
			ForBAction back = new ForBAction(this, -1, protClient);
			LorRTurnAction left = new LorRTurnAction(this, 1); 			//yaw left and right
			LorRTurnAction right = new LorRTurnAction(this, -1);
			im.associateActionWithAllKeyboards(net.java.games.input.Component.Identifier.Key.W, forward, 
				InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);	
			im.associateActionWithAllKeyboards(net.java.games.input.Component.Identifier.Key.S, back,
				InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
			im.associateActionWithAllKeyboards(net.java.games.input.Component.Identifier.Key.A, left,
				InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
			im.associateActionWithAllKeyboards(net.java.games.input.Component.Identifier.Key.D, right,
				InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);

			im.associateActionWithAllKeyboards(net.java.games.input.Component.Identifier.Key.H, hideAxes,
				InputManager.INPUT_ACTION_TYPE.ON_PRESS_ONLY);
//all three of these need to be sent at the same time or else only the first item assigned to the key is hidden

			im.associateActionWithAllKeyboards(net.java.games.input.Component.Identifier.Key.SPACE, disarm, 
				InputManager.INPUT_ACTION_TYPE.ON_PRESS_ONLY);

/* 			im.associateActionWithAllKeyboards(net.java.games.input.Component.Identifier.Key.I, mapMoveUp, 
				InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);	
			im.associateActionWithAllKeyboards(net.java.games.input.Component.Identifier.Key.K, mapMoveBack,
				InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
			im.associateActionWithAllKeyboards(net.java.games.input.Component.Identifier.Key.J, mapMoveLeft,
				InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
			im.associateActionWithAllKeyboards(net.java.games.input.Component.Identifier.Key.L, mapMoveRight,
				InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);

			im.associateActionWithAllKeyboards(net.java.games.input.Component.Identifier.Key.U, mapZoomIn,
				InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
			im.associateActionWithAllKeyboards(net.java.games.input.Component.Identifier.Key.O, mapZoomOut,
				InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
//		}
*/		if(gamepad != null){	//if a gamepad is plugged in
			LorRTurnAction rc = new LorRTurnAction(this, -1);
			ForBAction fc = new ForBAction(this, -1);
			im.associateAction(gamepad,net.java.games.input.Component.Identifier.Axis.X, rc,		//Axis.X/Y are the left joystick
				InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
			im.associateAction(gamepad,net.java.games.input.Component.Identifier.Axis.Y, fc, 
				InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
//REMEMBER: buttons start at 0, but are shown at 1

			im.associateAction(gamepad,net.java.games.input.Component.Identifier.Button._6, hideAxes,	//TODO:write out controlls for the readme
				InputManager.INPUT_ACTION_TYPE.ON_PRESS_ONLY);
//all three of these need to be sent at the same time or else only the first item assigned to the key is hidden
			
			im.associateAction(gamepad,net.java.games.input.Component.Identifier.Button._7, disarm, 
				InputManager.INPUT_ACTION_TYPE.ON_PRESS_ONLY);
  
/* 			im.associateAction(gamepad,net.java.games.input.Component.Identifier.Button._3, mapMoveUp, 
				InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);	
			im.associateAction(gamepad,net.java.games.input.Component.Identifier.Button._0, mapMoveBack, 
				InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
			im.associateAction(gamepad,net.java.games.input.Component.Identifier.Button._2, mapMoveLeft, 
				InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
			im.associateAction(gamepad,net.java.games.input.Component.Identifier.Button._1, mapMoveRight, 
				InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);

			im.associateAction(gamepad,net.java.games.input.Component.Identifier.Button._4, mapZoomIn, 
				InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
			im.associateAction(gamepad,net.java.games.input.Component.Identifier.Button._5, mapZoomOut, 
				InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
*/			
         
//         protClient.sendMoveMessage(avatar.getWorldLocation());
		}
         setupNetworking();



/* 		im.associateActionWithAllKeyboards(net.java.games.input.Component.Identifier.Key.UP, up, 
			InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
		im.associateActionWithAllKeyboards(net.java.games.input.Component.Identifier.Key.DOWN, down, 
			InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);

//		im.associateActionWithAllKeyboards(net.java.games.input.Component.Identifier.Key.C, count, 
//			InputManager.INPUT_ACTION_TYPE.ON_PRESS_ONLY);

//		im.associateActionWithAllKeyboards(net.java.games.input.Component.Identifier.Key.SPACE, mount, 	
//			InputManager.INPUT_ACTION_TYPE.ON_PRESS_ONLY);
*/
//https://javadoc.io/doc/net.java.jinput/jinput/2.0.7/net/java/games/input/Component.Identifier.html


		// ------------- HUD section ------------------
//hud testing section
		HUDscore = engine.getHUDmanager().addHUDElement(dispStr1, hud1Color, 15, 15);
		HUDCoords = engine.getHUDmanager().addHUDElement(dispStr2, hud2Color, 15,15);//findViewportMiddleX("MAP", dispStr2), 15);
//		engine.getHUDmanager().addHUDElement("Third HUD Test",white, engine.getRenderSystem().getWindowX(),engine.getRenderSystem().getWindowY());
//puts hud at the very bottom left corner for some reason
//		System.out.println("actualWidth() = " + (int)engine.getRenderSystem().getViewport("MAP").getActualWidth());

//for(int i = 1; i <= 5; i++)
//		engine.getHUDmanager().addHUDElement("HUD stack test", new Vector3f(.2f*i, 1f-.2f*(i-1), .5f), 15,45*i);	//test if deleting a middle one causes it to delete properly or crash the program
	}

	private int findViewportMiddleX(String name, String text){ //middle of viewport's width compared to x from MAIN
		float size = engine.getRenderSystem().getViewport(name).getActualWidth();
//		float ratio = engine.getRenderSystem().getViewport(name).getRelativeWidth();
		float middle = size/2;

		float drawAt = engine.getRenderSystem().getViewport("MAIN").getActualWidth() - middle - textMidpoint(text);
		return (int)drawAt; 
	}
	private int textMidpoint(String text){ 
		if(text.isEmpty())
			return 0;
		return (int)(text.length()*10)/2; } //assumes default of GLUT.BITMAP_TIMES_ROMAN_24

	@Override
	public void update()
	{	// rotate dolphin if not paused
		lastFrameTime = currFrameTime;
		currFrameTime = System.currentTimeMillis();
		elapsTime = (currFrameTime - lastFrameTime);// / 1000.0; //the /1000 turns it into seconds. used more like a FrameTime variable than an Elapsed time variable. That would be "+= curr-last"

		
//		System.out.println("actualWidth() = " + (int)engine.getRenderSystem().getViewport("MAIN").getActualWidth());

//--------------HUD drawing----------------
//		counterStr = Integer.toString(counter);		
		victoryCondition();
		dispStr2 = "(" + cam.getLocation().x() + ", " + cam.getLocation().y() + ", " + cam.getLocation().z() + ")";

		engine.getHUDmanager().setHUDValue(HUDscore, dispStr1);
		engine.getHUDmanager().setHUDValue(HUDCoords, dispStr2);
//		engine.getHUDmanager().setHUDPosition(HUDCoords, findViewportMiddleX("MAP", dispStr2), 15);
		engine.getHUDmanager().setHUDPosition(HUDscore, findViewportMiddleX("MAIN", dispStr1), 15);
//--------------Game----------------
		spaceCheck();
		changeCheck();
		orb.updateCameraPosition();
		im.update((float)elapsTime);
      if(isClientConnected)
         protClient.sendMoveMessage(avatar.getWorldLocation());
		processNetworking((float)elapsTime);
	}
/*
   	@Override
	public void keyPressed(KeyEvent e)
	{	switch (e.getKeyCode())
		{	case KeyEvent.VK_W:
			{	Vector3f oldPosition = avatar.getWorldLocation();
				Vector4f fwdDirection = new Vector4f(0f,0f,1f,1f);
				fwdDirection.mul(avatar.getWorldRotation());
				fwdDirection.mul(0.05f);
				Vector3f newPosition = oldPosition.add(fwdDirection.x(), fwdDirection.y(), fwdDirection.z());
				avatar.setLocalLocation(newPosition);
				protClient.sendMoveMessage(avatar.getWorldLocation());
				break;
			}
			case KeyEvent.VK_D:
			{	Matrix4f oldRotation = new Matrix4f(avatar.getWorldRotation());
				Vector4f oldUp = new Vector4f(0f,1f,0f,1f).mul(oldRotation);
				Matrix4f rotAroundAvatarUp = new Matrix4f().rotation(-.01f, new Vector3f(oldUp.x(), oldUp.y(), oldUp.z()));
				Matrix4f newRotation = oldRotation;
				newRotation.mul(rotAroundAvatarUp);
				avatar.setLocalRotation(newRotation);
				break;
			}
		}
		super.keyPressed(e);
	}

	@Override
	public void keyPressed(KeyEvent e) //DO NOT COPY/PASTE SAMPLE CODE FROM PDF! IT BREAKS *EVERYTHNG*
	{	
		Vector3f loc, fwd, up, right, newLocation;
		Camera cam;

		switch (e.getKeyCode())
		{	case KeyEvent.VK_C:
				counter++;
				break;
 			case KeyEvent.VK_1:
				paused = !paused;
				break;
			case KeyEvent.VK_2://move forward
				fwd = dol.getWorldForwardVector();
				loc = dol.getWorldLocation();
				newLocation = loc.add(fwd.mul(0.02f));
				dol.setLocalLocation(newLocation);
				break;
			case KeyEvent.VK_3://move back
				fwd = dol.getWorldForwardVector();
				loc = dol.getWorldLocation();
				newLocation = loc.add(fwd.mul(-0.02f));
				dol.setLocalLocation(newLocation);
				break;
			case KeyEvent.VK_4://move camera on top of dolphin. Put in main to have it follow constantly
//best way to move this into display() is to make it its own function that gets called every frame
				/*get camera from engine, set vec3 items to dolphin's coordinates, update camera's values * /
//				(engine.getRenderSystem().getViewport("MAIN").getCamera()).setLocation(new Vector3f(0,0,0));
				break;
			case KeyEvent.VK_5://move camera back to start point
				cam = engine.getRenderSystem().getViewport("MAIN").getCamera();
				cam.setLocation(new Vector3f(0.0f,0.0f,5.0f));
				//also needs to reset orientation
				//setUV and N to 0,0,0?
				break;
		}
		super.keyPressed(e);
	}*/
 	public void jumpToDol(){ 	//move camera to dol 
/* 			Camera cam = engine.getRenderSystem().getViewport("MAIN").getCamera();
			cam.setU(avatar.getWorldRightVector());
			cam.setV(avatar.getWorldUpVector());
			cam.setN(avatar.getWorldForwardVector());
			cam.setLocation(avatar.getWorldLocation().add(avatar.getWorldUpVector().mul(3f)).add(
				avatar.getWorldForwardVector().mul(-5f)));
*/	}
	public void dismountDol(){	//move to spot slightly off dol
/*		Camera cam = engine.getRenderSystem().getViewport("MAIN").getCamera();
		cam.setU(avatar.getWorldRightVector());
		cam.setV(avatar.getWorldUpVector());
		cam.setN(avatar.getWorldForwardVector());
		cam.setLocation(avatar.getWorldLocation().add(avatar.getWorldRightVector().mul(1.5f)).add(
			avatar.getWorldForwardVector().mul(-1.5f)).add(avatar.getWorldUpVector().mul(1.2f)));
*/	}
	private void birdView(float dist, Camera c){
		c.setLocation(avatar.getWorldLocation().add(new Vector3f( 0f, dist, 0f)));
		c.lookAt(avatar);
	}
	private void victoryCondition(){ //moving it out of update() looks nicer
		if(sphere.destroyed || cube.destroyed || torus.destroyed)
			dispStr1 = "GAME OVER";
		else if(sphere.disarmed && cube.disarmed && torus.disarmed)
			dispStr1 = "VICTORY";
		else if((spDist < spot.close && !sphere.disarmed) || (cuDist < spot.close && !cube.disarmed) || (toDist < spot.close && !torus.disarmed))
			dispStr1 = "Close Enough";
		else if((cuDist < spot.tooClose && cube.disarmed) || 
			(spDist < spot.tooClose && sphere.disarmed) ||
			(toDist < spot.tooClose && torus.disarmed))
				dispStr1 = "Disarmed";
		else
			dispStr1 = "Score = " + counter*spot.capture;//elapsTimeStr;
	}

	private void spaceCheck(){
		spDist = (float)avatar.getWorldLocation().distance(sphere.getWorldLocation());
		cuDist = (float)avatar.getWorldLocation().distance(cube.getWorldLocation());
		toDist = (float)avatar.getWorldLocation().distance(torus.getWorldLocation());

		if(spDist < spot.close && !sphere.destroyed){
			sphere.setTextureImage(sphereClose);
			if(spDist < spot.tooClose && !sphere.disarmed){
				sphere.setTextureImage(brokeX);
				sphere.destroyed = true;
			}
		}
		if(cuDist < spot.close && !cube.destroyed){
			cube.setTextureImage(cubeClose);
			if(cuDist < spot.tooClose && !cube.disarmed){
				cube.setTextureImage(brokeX);
				cube.destroyed = true;
			}
		}
		if(toDist < spot.close && !torus.destroyed){
			torus.setTextureImage(torusClose);
			if(toDist < spot.tooClose && !torus.disarmed){
				torus.setTextureImage(brokeX);
				torus.destroyed = true;
			}
		}	}
	private void changeCheck(){
		if(!cube.destroyed && cuDist > spot.close && cube.getTextureImage().getTexture() != cubeX.getTexture() && !cube.disarmed)
			cube.setTextureImage(cubeX);
		if(!sphere.destroyed && spDist > spot.close && sphere.getTextureImage().getTexture() != sphereX.getTexture() && !sphere.disarmed)
			sphere.setTextureImage(sphereX);
		if(!torus.destroyed && toDist > spot.close && torus.getTextureImage().getTexture() != torusX.getTexture() && !torus.disarmed)
			torus.setTextureImage(torusX);

		if(sphere.disarmed && sphere.getTextureImage().getTexture() != sphereSafe.getTexture())
			sphere.setTextureImage(sphereSafe);
		if(cube.disarmed && cube.getTextureImage().getTexture() != cubeSafe.getTexture())
			cube.setTextureImage(cubeSafe);
		if(torus.disarmed && torus.getTextureImage().getTexture() != torusSafe.getTexture())
			torus.setTextureImage(torusSafe);
	}

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

	public Vector3f getPlayerPosition() { return avatar.getWorldLocation(); }

	public void setIsConnected(boolean value) { this.isClientConnected = value; }
	
	private class SendCloseConnectionPacketAction extends AbstractInputAction
	{	@Override
		public void performAction(float time, net.java.games.input.Event evt) 
		{	if(protClient != null && isClientConnected == true)
			{	protClient.sendByeMessage();
			}
		}
	}
}