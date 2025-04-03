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
	
	private double lastFrameTime, currFrameTime, elapsTime;

//-------------HUD elements--------------
	private float[] hud1Color = spot.red;
	private float[] hud2Color = spot.yellow;
	private String dispStr1 = " ", dispStr2;
	private int HUDscore, HUDCoords;

//-------------Visuals--------------
	private GameObject avatar, x, y, z;
	private ObjShape dolS, xAxis, yAxis, zAxis;
	private TextureImage doltx;
	private int skybox;
	private Light light1;

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


	public MyGame() { super(); System.out.println("Single Player boot up"); }
	public MyGame(String serverAddress, int serverPort, String protocol)
	{	super();
		gm = new GhostManager(this);
		this.serverAddress = serverAddress;
		this.serverPort = serverPort;
		if (protocol.toUpperCase().compareTo("TCP") == 0)
			this.serverProtocol = ProtocolType.TCP;
		else
			this.serverProtocol = ProtocolType.UDP;
		System.out.println("Multiplayer booting up");
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

	@Override
	public void loadShapes(){	
		dolS = new ImportedModel("ULPD.obj");
		ghostS = new ImportedModel("dolphinLowPoly.obj");
    /*
	{	dolS = new ImportedModel("dolphinHighPoly.obj");
		ghostS = new ImportedModel("dolphinHighPoly.obj");
//		dolS = new ImportedModel("dolphinLowPoly.obj");

		cubeS = new Cube();
 		sphereS = new Sphere();
		torusS = new Torus(0.5f, 0.2f, 48);
		crystalS = new ManualCrystal();
		
		groundS = new Plane();
*/

		xAxis = new Line(new Vector3f(0f,0f,0f), new Vector3f(3f,0f,0f));
		yAxis = new Line(new Vector3f(0f,0f,0f), new Vector3f(0f,3f,0f));
		zAxis = new Line(new Vector3f(0f,0f,0f), new Vector3f(0f,0f,-3f));
	}

	@Override
	public void loadTextures(){	
		doltx = new TextureImage("ULPDuv.png");
		ghostT = new TextureImage("oiter.png");
/*

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

*/
	}

	@Override
	public void buildObjects()
	{	Matrix4f initialTranslation, initialScale, initialRotation;

		// build dolphin in the center of the window
		avatar = new GameObject(GameObject.root(), dolS, doltx);
		initialTranslation = (new Matrix4f()).translation(0f,0f,0f);
		initialScale = (new Matrix4f()).scaling(0.75f);
		avatar.setLocalTranslation(initialTranslation);
		avatar.setLocalScale(initialScale);
		initialRotation = (new Matrix4f()).rotationY((float)java.lang.Math.toRadians(180f));
		avatar.setLocalRotation(initialRotation);
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
		x = new GameObject(GameObject.root(), xAxis);
		y = new GameObject(GameObject.root(), yAxis);
		z = new GameObject(GameObject.root(), zAxis);
		(x.getRenderStates()).setColor(new Vector3f(spot.red));
		(y.getRenderStates()).setColor(new Vector3f(spot.green));
		(z.getRenderStates()).setColor(new Vector3f(spot.blue));
		hideableShapes.add(x); hideableShapes.add(y); hideableShapes.add(z);
	}

	@Override
	public void initializeLights()
	{	Light.setGlobalAmbient(0.5f, 0.5f, 0.5f);
		light1 = new Light();
		light1.setLocation(new Vector3f(5.0f, 4.0f, 2.0f));
		(engine.getSceneGraph()).addLight(light1);
	}

	@Override
	public void createViewports(){	//code directly from https://csus.instructure.com/courses/130924/files/22876803?module_item_id=6834558 
		engine.getRenderSystem().addViewport("MAIN",0f,0f,1f,1f);

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
*/	
	}

	@Override
	public void loadSkyBoxes(){
		skybox = engine.getSceneGraph().loadCubeMap(spot.skyboxFile);
		engine.getSceneGraph().setActiveSkyBoxTexture(skybox);
		engine.getSceneGraph().setSkyBoxEnabled(true);
	}

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
    
		// ------------- Node section ------------------
/*		rc = new RotationController(engine, new Vector3f(0,1,0), .001f);
		engine.getSceneGraph().addNodeController(rc);
		rc.addTarget(torus);
		rc.addTarget(crystal);
		rc.addTarget(sphere);
		rc.addTarget(cube);
		rc.toggle();

		roll = new RollController(.001f);
		engine.getSceneGraph().addNodeController(roll);
		roll.setPitchSpeed(.001f);
		roll.toggle();
*/
		// ------------- inputs section ------------------
 		
//NOTE: associateActionWithAllKeyboards means you're using Identifier.Key to get a keyboard key
//		associateActionWithAllGamepads means you're using Identifier.Axis to get a joystick and .Button for the 
//			controller's buttons
		
			HideObjectAction hideAxes = new HideObjectAction(hideableShapes);

//avatar movement
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
//all three axes need to be sent at the same time or else only the first item assigned to the key is hidden

		if(gamepad != null){	//if a gamepad is plugged in

			LorRTurnAction rc = new LorRTurnAction(this, -1);
			ForBAction fc = new ForBAction(this, -1);
			im.associateAction(gamepad,net.java.games.input.Component.Identifier.Axis.X, rc,		//Axis.X/Y are the left joystick
				InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
			im.associateAction(gamepad,net.java.games.input.Component.Identifier.Axis.Y, fc, 
				InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
//REMEMBER: buttons start at 0, but are shown starting at 1

			im.associateAction(gamepad,net.java.games.input.Component.Identifier.Button._6, hideAxes,
				InputManager.INPUT_ACTION_TYPE.ON_PRESS_ONLY);
			
		}

//https://javadoc.io/doc/net.java.jinput/jinput/2.0.7/net/java/games/input/Component.Identifier.html


		// ------------- HUD section ------------------
//hud testing section
		HUDscore = engine.getHUDmanager().addHudElement(dispStr1, hud1Color, 15, 15);
		HUDCoords = engine.getHUDmanager().addHudElement(dispStr2, hud2Color, 15,15);
//		engine.getHUDmanager().addHUDElement("Third HUD Test",white, engine.getRenderSystem().getWindowX(),engine.getRenderSystem().getWindowY());
//for(int i = 1; i <= 5; i++)
//		engine.getHUDmanager().addHUDElement("HUD stack test", new Vector3f(.2f*i, 1f-.2f*(i-1), .5f), 15,45*i);	//test if deleting a middle one causes it to delete properly or crash the program


//------------- Networking Section -------------
		if(serverPort != -1)
        	setupNetworking();
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
		return (int)(text.length()*10)/2; 
	} //assumes default of GLUT.BITMAP_TIMES_ROMAN_24

	public GameObject getAvatar(){ return avatar; }

	@Override
	public void update()
	{	
//--------------Time Keeping--------------
		lastFrameTime = currFrameTime;
		currFrameTime = System.currentTimeMillis();
		elapsTime = (currFrameTime - lastFrameTime);// / 1000.0; //the /1000 turns it into seconds. used more like a FrameTime variable than an Elapsed time variable. That would be "+= curr-last"

//--------------HUD drawing----------------
		dispStr2 = "(" + cam.getLocation().x() + ", " + cam.getLocation().y() + ", " + cam.getLocation().z() + ")";

		engine.getHUDmanager().setHUDValue(HUDscore, dispStr1);
		engine.getHUDmanager().setHUDValue(HUDCoords, dispStr2);
		engine.getHUDmanager().setHUDPosition(HUDscore, findViewportMiddleX("MAIN", dispStr1), 15);

//--------------Game Loop----------------
		orb.updateCameraPosition();
		im.update((float)elapsTime);
    	if(isClientConnected){
        	protClient.sendMoveMessage(avatar.getWorldLocation());
			processNetworking((float)elapsTime);
		}
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