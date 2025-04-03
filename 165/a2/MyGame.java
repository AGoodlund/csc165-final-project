package a2;

import tage.*;
import tage.input.*;
import tage.input.action.*;
import net.java.games.input.*;
import net.java.games.input.Component.Identifier.*;
import tage.shapes.*;
import java.lang.Math;
import java.util.ArrayList;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import javax.swing.*;
import org.joml.*; 
import tage.nodeControllers.*;

public class MyGame extends VariableFrameRateGame
{
	private static Engine engine;
	private Camera cam;
	private CameraOrbit3D orb;

	private ArrayList<GameObject> hideableShapes = new ArrayList<GameObject>();
	
	private double lastFrameTime, currFrameTime, elapsTime;

//-------------HUD elements--------------
	private Vector3f hud1Color = new Vector3f(spot.red);
	private Vector3f hud2Color = new Vector3f(spot.yellow);
	private String dispStr1 = " ", dispStr2;
	private int HUDscore, HUDCoords;

//-------------game visuals--------------
	private GameObject avatar, x, y, z;
	private ObjShape dolS, xAxis, yAxis, zAxis;
	private TextureImage doltx;
	private Light light1;

	private InputManager im;

	public MyGame() { super(); }
	public GameObject getAvatar(){ return avatar; }
	

	public static void main(String[] args)
	{	MyGame game = new MyGame();
		engine = new Engine(game);
		game.initializeSystem();
		game.game_loop();
	}

	@Override
	public void loadShapes()
	{	
		dolS = new ImportedModel("ULPD.obj");

		xAxis = new Line(new Vector3f(0f,0f,0f), new Vector3f(3f,0f,0f));
		yAxis = new Line(new Vector3f(0f,0f,0f), new Vector3f(0f,3f,0f));
		zAxis = new Line(new Vector3f(0f,0f,0f), new Vector3f(0f,0f,-3f));
	}

	@Override
	public void loadTextures()
	{	
		doltx = new TextureImage("ULPDuv.png");
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
		HideObjectAction hideAxes = new HideObjectAction(hideableShapes);
		
		// ------------- inputs section ------------------
 		
//NOTE: associateActionWithAllKeyboards means you're using Identifier.Key to get a keyboard key
//		associateActionWithAllGamepads means you're using Identifier.Axis to get a joystick and .Button for the 
//			controller's buttons
//avatar movement
//		if(gamepad == null){	//if no gamepad is plugged in
//https://www.javadoc.io/doc/net.java.jinput/jinput/2.0.7/net/java/games/input/Component.Identifier.Key.html
			ForBAction forward = new ForBAction(this, 1);			//move actions
			ForBAction back = new ForBAction(this, -1);
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
	
		if(gamepad != null){	//if a gamepad is plugged in
			LorRTurnAction rc = new LorRTurnAction(this, -1);
			ForBAction fc = new ForBAction(this, -1);
			im.associateAction(gamepad,net.java.games.input.Component.Identifier.Axis.X, rc,		//Axis.X/Y are the left joystick
				InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
			im.associateAction(gamepad,net.java.games.input.Component.Identifier.Axis.Y, fc, 
				InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
//REMEMBER: buttons start at 0, but are shown starting at 1

			im.associateAction(gamepad,net.java.games.input.Component.Identifier.Button._6, hideAxes,	//TODO:write out controlls for the readme
				InputManager.INPUT_ACTION_TYPE.ON_PRESS_ONLY);
//all three of these need to be sent at the same time or else only the first item assigned to the key is hidden
		}
//https://javadoc.io/doc/net.java.jinput/jinput/2.0.7/net/java/games/input/Component.Identifier.html


		// ------------- HUD section ------------------
//hud testing section
		HUDscore = engine.getHUDmanager().addHUDElement(dispStr1, hud1Color, 15, 15);
		HUDCoords = engine.getHUDmanager().addHUDElement(dispStr2, hud2Color, 15,15);//findViewportMiddleX("MAP", dispStr2), 15);
//		engine.getHUDmanager().addHUDElement("Third HUD Test",white, engine.getRenderSystem().getWindowX(),engine.getRenderSystem().getWindowY());
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
	}
}