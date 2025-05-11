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
import java.net.InetAddress;

import java.net.UnknownHostException;

import org.joml.*; 

import net.java.games.input.*;
import net.java.games.input.Component.Identifier.*;

import javax.swing.*;

import tage.physics.PhysicsEngine;
import tage.physics.PhysicsObject;
import tage.physics.JBullet.*;
import com.bulletphysics.dynamics.RigidBody;
import com.bulletphysics.collision.dispatch.CollisionObject;

public class MyGame extends VariableFrameRateGame
{
	private static Engine engine;
	private Camera cam;
	private InputManager im;
	private IAudioManager am;

	private ArrayList<GameObject> hideableShapes = new ArrayList<GameObject>();
	
	private double lastFrameTime, currFrameTime, elapsTime;

	private int health = 10;

//-------------HUD elements--------------
//	private float[] hud1Color = spot.red;
	private float[] hud2Color = spot.red;
	private String dispStr1 = " ", dispStr2;
	private int HUDscore, HUDCoords;

//-------------Visuals--------------
	private GameObject avatar, x, y, z, terr, diver, enemy, dol;//, puffer;

	private AnimatedShape diverS;
	public boolean isAnimating = false, hasLooped = false;

	private ObjShape dolS, xAxis, yAxis, zAxis, terrS, pufferS, pufferCalmS;
	private TextureImage dolX, hills, grass, pufferX, pufferAltX;

	private Light light1;
	private int skybox, seabox;

//weapon objects
	private GameObject 	laser, gun, 
						harpoon1, harpoon2, harpoon3, harpoon4, harpoon5; 

	private PhysicsObject bullet1, bullet2, bullet3, bullet4, bullet5;
	private Matrix4f bulletStorage = new Matrix4f();

	private ArrayList<GameObject> harpoons = new ArrayList<GameObject>();

	private ObjShape laserS, gunS, crystalS, harpoonS, orbS;

	private GameObject jellyR, jellyG, jellyY;
	private ObjShape jellyfish;
	private Light red, green, yellow, diverVision;

	private float height;

//-------------Sounds--------------
	private Sound bubbles, bow;
	private Vector3f up = new Vector3f(spot.y);

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
	//private float sensitivity = 1.0f;

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
	private PhysicsObject dolP, ghostP, raftP, groundPlaneP, avatarP, groundingP;//, pufferP;
	private float[] gravity = {0f, 0f,0f};//-20f, 0f};//-6f, 0f};
	private float vals[] = new float[16]; 
	
//Networking
	public ObjShape getEnemyShape() { return enemy.getShape(); }
	public TextureImage getEnemyTexture() { return enemy.getTextureImage(); }
	public Matrix4f getEnemySize(Matrix4f dest){ enemy.getWorldScale(dest); return dest; }

	public ObjShape getDiverShape(){ return diverS; }
	public TextureImage getDiverTexture(){ return diver.getTextureImage(); }
	public Matrix4f getDiverSize(Matrix4f dest){ diver.getWorldScale(dest); return dest; }

	public ObjShape getDolShape(){ return dol.getShape(); }
	public TextureImage getDolTexture(){ return dol.getTextureImage(); }
	public Matrix4f getDolSize(Matrix4f dest){ dol.getWorldScale(dest); return dest; }

	
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
		pufferS = new ImportedModel("PufferFish_Angry_flipped.obj");
		pufferCalmS = new ImportedModel("PufferFish_Calm.obj");
		jellyfish = new ImportedModel("Jellyfish.obj");

		terrS = new TerrainPlane(1000); //pixels per axis is 1000 X 1000


		diverS = new AnimatedShape("Diver.rkm", "Diver.rks");
			diverS.loadAnimation("WALK", "Diver_walk.rka");
		gunS = new ImportedModel("crossbow_loaded.obj");
		harpoonS = new ImportedModel("spear.obj");
			harpoonS.setMatAmb(Utils.goldAmbient());
			harpoonS.setMatDif(Utils.goldDiffuse());
			harpoonS.setMatSpe(Utils.goldSpecular());
			harpoonS.setMatShi(Utils.goldShininess());			

		xAxis = new Line(new Vector3f(0f,0f,0f), new Vector3f(3f,0f,0f));
		yAxis = new Line(new Vector3f(0f,0f,0f), new Vector3f(0f,3f,0f));
		zAxis = new Line(new Vector3f(0f,0f,0f), new Vector3f(0f,0f,-3f));
		laserS = new Line(new Vector3f(0f,.1f,0f), new Vector3f(0f,.1f,-45f));
	}

	@Override
	public void loadTextures(){	
		dolX = new TextureImage("ULPDuv.png");
		ghostT = new TextureImage("oiter.png");
		pufferX = new TextureImage("Pufferfish_Angry_Spiney.png");
		pufferAltX = new TextureImage("Pufferfish_Angry_SpineyAlt.png");

		grass = new TextureImage("sand_watery.png");
		hills = new TextureImage("heightmap map.png");
	}
private void createBullet(GameObject g, ArrayList<GameObject> goal, float scale, float[] color){
	Matrix4f initialScale;
	initialScale = new Matrix4f().scaling(scale);
	g.setLocalScale(initialScale);
	g.setLocalTranslation(bulletStorage);
	g.getRenderStates().setColor(new Vector3f(color));
	g.getRenderStates().setHasSolidColor(true);
	goal.add(g);
}
	@Override
	public void buildObjects()
	{	Matrix4f initialTranslation, initialScale, initialRotation;

		// build dolphin in the center of the window
		avatar = new GameObject(GameObject.root(), diverS);
		initialTranslation = (new Matrix4f()).translation(0f,2f,0f);
		avatar.setLocalTranslation(initialTranslation);
		initialRotation = new Matrix4f().rotationY((float)Math.toRadians(-90f));
		avatar.setLocalRotation(initialRotation);
		avatar.getRenderStates().setPositionalColor(true);
		avatar.takesDamage = true;

//weapons and ammo section
		gun = new GameObject(GameObject.root(), gunS);
		initialTranslation = new Matrix4f().translation(0,1f,0);
		gun.setLocalTranslation(initialTranslation);
		initialScale = new Matrix4f().scaling(.5f);
		gun.setLocalScale(initialScale);
		initialRotation = new Matrix4f().rotationY((float)Math.toRadians(-90f));
		gun.setLocalRotation(initialRotation);
		gun.getRenderStates().isEnvironmentMapped(true);
		gun.setParent(avatar);
		gun.propagateRotation(true);
		gun.propagateTranslation(true);
		gun.propagateScale(false);
   		
		harpoon1 = new GameObject(GameObject.root(), harpoonS);
		harpoon2 = new GameObject(GameObject.root(), harpoonS);
		harpoon3 = new GameObject(GameObject.root(), harpoonS);
		harpoon4 = new GameObject(GameObject.root(), harpoonS);
		harpoon5 = new GameObject(GameObject.root(), harpoonS);
		float bulletScale = 1f;
		float[] color = spot.yellow;
		createBullet(harpoon1, harpoons, bulletScale, color);
		createBullet(harpoon2, harpoons, bulletScale, color);
		createBullet(harpoon3, harpoons, bulletScale, color);
		createBullet(harpoon4, harpoons, bulletScale, color);
		createBullet(harpoon5, harpoons, bulletScale, color);

		harpoon1.getRenderStates().setPositionalColor(true);
		harpoon2.getRenderStates().setPositionalColor(true);
		harpoon3.getRenderStates().setPositionalColor(true);
		harpoon4.getRenderStates().setPositionalColor(true);
		harpoon5.getRenderStates().setPositionalColor(true);
//W&E section end

		//build illuminated jellyfish
		jellyR = new GameObject(GameObject.root(), jellyfish);
			initialTranslation = (new Matrix4f()).translation(50f,10f,-20f);
			initialScale = new Matrix4f().scaling(0.5f);
			jellyR.setLocalTranslation(initialTranslation);
			jellyR.setLocalScale(initialScale);

		jellyG = new GameObject(GameObject.root(), jellyfish);
			initialTranslation = (new Matrix4f()).translation(0f,15f,10f);
			initialScale = new Matrix4f().scaling(0.5f);
			jellyG.setLocalTranslation(initialTranslation);
			jellyG.setLocalScale(initialScale);

		jellyY = new GameObject(GameObject.root(), jellyfish);
			initialTranslation = (new Matrix4f()).translation(-50f,5f,15f);
			initialScale = new Matrix4f().scaling(0.5f);
			jellyY.setLocalTranslation(initialTranslation);
			jellyY.setLocalScale(initialScale);

		jellyR.getRenderStates().setColor(new Vector3f(spot.red));
		jellyG.getRenderStates().setColor(new Vector3f(spot.green));
		jellyY.getRenderStates().setColor(new Vector3f(spot.yellow));
		jellyY.getRenderStates().setHasSolidColor(true);
		jellyR.getRenderStates().setHasSolidColor(true);
		jellyG.getRenderStates().setHasSolidColor(true);
		jellyR.getRenderStates().setRenderHiddenFaces(true);
		jellyG.getRenderStates().setRenderHiddenFaces(true);
		jellyY.getRenderStates().setRenderHiddenFaces(true);

 		// build Enemy Pufferfish
		enemy = new GameObject(GameObject.root(), pufferS, pufferAltX);
		initialTranslation = (new Matrix4f()).translation(8f,3f,-3f);
		initialScale = (new Matrix4f()).scaling(10f);
		enemy.setLocalTranslation(initialTranslation);
		enemy.setLocalScale(initialScale);

		dol = new GameObject(GameObject.root(), dolS, dolX);
		initialTranslation = new Matrix4f().translation(0,2,0);
		initialScale = new Matrix4f().scaling(1f);
		dol.setLocalTranslation(initialTranslation);
		dol.setLocalScale(initialScale);
		dol.getRenderStates().disableRendering();

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

		laser = new GameObject(GameObject.root(), laserS);
		laser.getRenderStates().setColor(new Vector3f(spot.dimRed));
		laser.getRenderStates().setHasSolidColor(true);
		laser.setParent(avatar);
		laser.propagateRotation(true);
		laser.propagateTranslation(true);
		
		//Terrain
		terr = new GameObject(GameObject.root(),terrS,grass);
		initialTranslation = (new Matrix4f()).translation(0f,0f,0f); 
		terr.setLocalTranslation(initialTranslation);
		initialScale = new Matrix4f().scaling(spot.mapSize[0], spot.mapSize[1], spot.mapSize[2]);
		terr.setLocalScale(initialScale);
		terr.setHeightMap(hills);
		// set tiling for terrain texture
		terr.getRenderStates().setTiling(2); //1 for regular sand.png, 2 for sand_watery
		terr.getRenderStates().setTileFactor(spot.mapTiling);
		
		diver = new GameObject(GameObject.root(), diverS);
		avatar.getWorldTranslation(initialTranslation);
		avatar.getWorldScale(initialScale);
		avatar.getWorldRotation(initialRotation);
		diver.setLocalTranslation(initialTranslation);
		diver.setLocalScale(initialScale);
		diver.setLocalRotation(initialRotation);
		diver.getRenderStates().disableRendering();
	}
	
		
	@Override
	public void initializeLights()
	{	Light.setGlobalAmbient(.3f,.3f,.3f);//0.5f, 0.5f, 0.5f);

		red = new Light();
		red.setType(LightType.SPOTLIGHT); //Light defaults to positional
		red.setDirection(new Vector3f(0f,-1f,0f));
		red.setColor(spot.dimRed);
		jellyR.getWorldLocation(v);
		red.setLocation(v);

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

		diverVision = new Light();
		diverVision.setType(LightType.SPOTLIGHT);
		avatar.getWorldForwardVector(v);
		diverVision.setDirection(v);
		avatar.getWorldLocation(v);
		diverVision.setLocation(v);
		diverVision.setDiffuse(.55f,.8f,.4f);

		avatar.getWorldLocation(v);
		v.add(0f,2f,0f);
		diverVision.setLocation(v);
		avatar.getWorldForwardVector(v);
		v.add(0f,.5f,0f);
		diverVision.setDirection(v);

		
		engine.getSceneGraph().addLight(diverVision);
	}

	@Override
	public void createViewports(){
		engine.getRenderSystem().addViewport("MAIN",0f,0f,1f,1f);
	}

	@Override
	public void loadSkyBoxes(){
		seabox = engine.getSceneGraph().loadCubeMap("unda da sea");
		engine.getSceneGraph().setActiveSkyBoxTexture(seabox);
		engine.getSceneGraph().setSkyBoxEnabled(true);
	}

	@Override
	public void loadSounds(){
		AudioResource bubbling, shooting;
		am = engine.getAudioManager();
		bubbling = am.createAudioResource("sound_ahead__bubbles_low_4.wav", AudioResourceType.AUDIO_SAMPLE);//sound_ahead is the name of the sound's creator
		bubbles = new Sound(bubbling, SoundType.SOUND_EFFECT, spot.bubbleVolume, true);
		bubbles.initialize(am);
		bubbles.setMaxDistance(100f); //This is the distance at which you hear the quiet version of the sound. Anything past this is imperceptable.
		bubbles.setMinDistance(10f); //This is the distance at which you hear the loud version of the sound.
		bubbles.setRollOff(5f);

		shooting = am.createAudioResource("752207__dude_x-soundlab__crossbow-fire-vii.wav", AudioResourceType.AUDIO_SAMPLE);
		bow = new Sound(shooting, SoundType.SOUND_EFFECT, spot.bowVolume, false);
		bow.initialize(am);
		bow.setMaxDistance(50f);
		bow.setMinDistance(5f);
		bubbles.setRollOff(1f);
	}

private void setAmmoPhysics(GameObject g, PhysicsObject p){
	double[ ] tempTransform;
	tempTransform = toDoubleArray(bulletStorage.get(vals));//physicsTranslation.get(vals));
	p = engine.getSceneGraph().addPhysicsSphere(1f, tempTransform, .25f);
	g.setPhysicsObject(p);
	bulletStorage.m31(bulletStorage.m31()+1f); //move y up by 1
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
		float diverMass = 10.0f;
//		float tempUp[ ] = {0,1,0};
		float raftSize[ ] = {1,1,1,1};
		float tempUp[ ] = {0,1,0};
		float pufferRadius = 1.5f;
		float dolRadius = 1.0f;
		float tempHeight = 2.0f;
		boolean physicsDebug = false;

		
		double[ ] tempTransform;
		Matrix4f physicsTranslation = new Matrix4f();

		bulletStorage.translate(0f,spot.cameraOffset+10,0f);
		
		//Doesn't take movement into account
		//Add force in a direction to a physics object
		//Every second add a random force to avatar

 		//Terrain
		terr.getLocalTranslation(physicsTranslation); 
		tempTransform = toDoubleArray(physicsTranslation.get(vals));
		groundPlaneP = (engine.getSceneGraph()).addPhysicsStaticPlane(tempTransform, spot.y, 0.5f); //Decided that 0.5f is the best of both worlds when it comes to height for the terrain
		groundPlaneP.setBounciness(0f);
		terr.setPhysicsObject(groundPlaneP);

//		groundingP = engine.getSceneGraph().addPhysicsBox(0f, tempTransform, new float[]{4f,2f,4f});
//		groundingP.setBounciness(0.02f);

		//avatar
		avatar.getLocalTranslation(physicsTranslation);
		physicsTranslation.m31(physicsTranslation.m31()+0.5f);
		tempTransform = toDoubleArray(physicsTranslation.get(vals));
//		avatarP = engine.getSceneGraph().addPhysicsBox(3f, tempTransform, new float[]{2f,4f,2f});
//		avatarP = engine.getSceneGraph().addPhysicsSphere(300f, tempTransform, 1f)
		avatarP = engine.getSceneGraph().addPhysicsCapsule(0f, tempTransform, 1f,2f);
		avatarP.setSleepThresholds(5.0f,5.0f);
		avatarP.setBounciness(0f);
		avatarP.setFriction(1f);
		avatar.setPhysicsObject(avatarP);

		setAmmoPhysics(harpoon1, bullet1);
		setAmmoPhysics(harpoon2, bullet2);
		setAmmoPhysics(harpoon3, bullet3);
		setAmmoPhysics(harpoon4, bullet4);
		setAmmoPhysics(harpoon5, bullet5);
		
		if (physicsDebug)
		{
			engine.enablePhysicsWorldRender();
//			engine.disableGraphicsWorldRender();
		}
		
		
		// ------------- NPCs/AI section ------------------
		
		protClient.createGhostNPC(new Vector3f()); //TODO and change to instantiate a regular npc. ghostNPC must be made private again
		
		// ------------- inputs section ------------------
		//NOTE: associateActionWithAllKeyboards means you're using Identifier.Key to get a keyboard key
		//		associateActionWithAllGamepads means you're using Identifier.Axis to get a joystick and .Button for the 
		//			controller's buttons
		HideObjectAction hideAxes = new HideObjectAction(hideableShapes);
		//------------- avatar movement section -------------
		ForBAction forward = new ForBAction(this, cam, -1, protClient);				//move actions
		ForBAction back = new ForBAction(this, cam, 1, protClient);
		LorRStrafeAction right = new LorRStrafeAction(this, cam, 1, protClient);
		LorRStrafeAction left = new LorRStrafeAction(this, cam, -1, protClient);
		LorRTurnAction Tleft = new LorRTurnAction(this, 1, protClient); 			//yaw left and right
		LorRTurnAction Tright = new LorRTurnAction(this, -1, protClient);
		forward.addLight(diverVision);
		back.addLight(diverVision);
		right.addLight(diverVision);
		left.addLight(diverVision);
		Tleft.addLight(diverVision);
		Tright.addLight(diverVision);
		im.associateActionWithAllKeyboards(net.java.games.input.Component.Identifier.Key.W, forward, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);	
		im.associateActionWithAllKeyboards(net.java.games.input.Component.Identifier.Key.S, back, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
		im.associateActionWithAllKeyboards(net.java.games.input.Component.Identifier.Key.A, left, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
		im.associateActionWithAllKeyboards(net.java.games.input.Component.Identifier.Key.D, right, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
		im.associateActionWithAllKeyboards(net.java.games.input.Component.Identifier.Key.LEFT, Tleft, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
		im.associateActionWithAllKeyboards(net.java.games.input.Component.Identifier.Key.RIGHT, Tright, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
		im.associateActionWithAllKeyboards(net.java.games.input.Component.Identifier.Key.H, hideAxes, InputManager.INPUT_ACTION_TYPE.ON_PRESS_ONLY);

		ToggleFlashLightAction toggleLight = new ToggleFlashLightAction(diverVision);
		im.associateActionWithAllKeyboards(net.java.games.input.Component.Identifier.Key.F, toggleLight, InputManager.INPUT_ACTION_TYPE.ON_PRESS_ONLY);

		PanCameraAction pan = new PanCameraAction(cam, this);
		im.associateActionWithAllKeyboards(net.java.games.input.Component.Identifier.Key.V, pan, InputManager.INPUT_ACTION_TYPE.ON_PRESS_ONLY);

		ShootAction shoot = new ShootAction(harpoons, avatar, protClient);
		shoot.addSound(bow);
		im.associateActionWithAllKeyboards(net.java.games.input.Component.Identifier.Key.SPACE, shoot, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);

		ChangeCharacterAction change = new ChangeCharacterAction(avatar, protClient);
			change.addShapes(avatar.getShape(), dol.getShape(), enemy.getShape());
			change.addTextures(avatar.getTextureImage(), dol.getTextureImage(), enemy.getTextureImage());
			Matrix4f m2 = new Matrix4f(), m3 = new Matrix4f();
			avatar.getWorldScale(m);
			dol.getWorldScale(m2);
			enemy.getWorldScale(m3);
			change.addSizes(m, m2, m3);
		im.associateActionWithAllKeyboards(net.java.games.input.Component.Identifier.Key.C, change, InputManager.INPUT_ACTION_TYPE.ON_PRESS_ONLY);

//all three axes need to be sent at the same time or else only the first item assigned to the key is hidden
			//controller
		if(gamepad != null){	//if a gamepad is plugged in
//TODO: update controls to work with controller to fit requirements
			LorRTurnAction rc = new LorRTurnAction(this, protClient);
			ForBAction fc = new ForBAction(this, cam, -1, protClient);
			rc.addLight(diverVision);
			fc.addLight(diverVision);
			im.associateAction(gamepad,net.java.games.input.Component.Identifier.Axis.X, rc, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN); 	//Axis.X/Y are the left joystick
			im.associateAction(gamepad,net.java.games.input.Component.Identifier.Axis.Y, fc, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
			//REMEMBER: buttons start at 0, but are shown starting at 1

			LorRStrafeAction lr = new LorRStrafeAction(this, cam, protClient);

//			im.associateAction(gamepad, net.java.games.input.Component.Identifier.Button. , toggleLight, InputManager.INPUT_ACTION_TYPE.ON_PRESS_ONLY);
//			im.associateAction(gamepad, net.java.games.input.Component.Identifier.Button. , shoot, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
//			im.associateAction(gamepad, net.java.games.input.Component.Identifier.Button. , change, InputManager.INPUT_ACTION_TYPE.ON_PRESS_ONLY);
//			im.associateAction(gamepad, net.java.games.input.Component.Identifier.Button. , pan, InputManager.INPUT_ACTION_TYPE.ON_PRESS_ONLY);
			
			im.associateAction(gamepad,net.java.games.input.Component.Identifier.Button._6, hideAxes, InputManager.INPUT_ACTION_TYPE.ON_PRESS_ONLY);
		}
//https://javadoc.io/doc/net.java.jinput/jinput/2.0.7/net/java/games/input/Component.Identifier.html


		// ------------- HUD section ------------------
		HUDCoords = engine.getHUDmanager().addHudElement(dispStr2, hud2Color, 15,15);

//------------------sound section----------------------
		updateEar();
		bubbles.play();

//--------------Animation section--------------
//		diverS.playAnimation("WALK", 1f, AnimatedShape.EndType.LOOP, 0);
	}

	public void updateEar(){
		enemy.getWorldLocation(v);
		bubbles.setLocation(v);
		avatar.getLocalLocation(v);
		am.getEar().setLocation(v);
		cam.getLocation(v);
		if(v.y() > 5)
			cam.getV(v);
		else
			cam.getN(v);
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
	public void startAnimation(){
		diverS.playAnimation("WALK", 1f, AnimatedShape.EndType.LOOP, 0);
	}
	
//-------------Physics----------------
//TODO:
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
		
		
		if (distance.equals(avatarLocalLocation, radiusOfEffect))//TODO:turn this into regular collision monitoring
		{
//			obj.getPhysicsObject().applyForce(force.x(), force.y(), force.z(),0.0f,0.0f,0.0f);
		}
	}
	
	//TODO: YEEEEEEEET
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
	
	private void checkForCollisions() //TODO: when a collision is detected run resolveCollision
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
/*
	private void resolveCollision(GameObject a, GameObject b){ //determine what special collision needs to happen
		if(a.dealsDamage && b.takesDamage){
			if(b == avatar)
				health--;
			else return;
				protClient.dealDamage(b); //protClient tells the server which ghostNPC needs to take damage
		}
	} 
*/	
//-------------Terrain---------------- 
/*	public void applyHeightMap(){
		for(GameObject obj: mappable){ 
			obj.getWorldLocation(v);
			height = terr.getHeight(); //height map + y position of the plane
			if(obj.getHeight() < height)
				obj.heightAdjust(height);
		}
	}
*/
	@Override
	public void update()
	{
		//--------------Time Keeping--------------
		lastFrameTime = currFrameTime;
		currFrameTime = System.currentTimeMillis();
		elapsTime = (currFrameTime - lastFrameTime);// / 1000.0; //the /1000 turns it into seconds. used more like a FrameTime variable than an Elapsed time variable. That would be "+= curr-last"
		
		//--------------Sound--------------
		updateEar();

		//--------------Animation--------------
		if(isAnimating){
//System.out.println("isAnimating within if statement = " + isAnimating);
			isAnimating = false;
		}
		else{
			diverS.stopAnimation();
			hasLooped = false;
//System.out.println("isAnimating within else statement = " + isAnimating);
		}
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
//			to make enemies turn just have them run lookAt(avatar) whenever Think updates their pathing
			} 
		} 

//		calculateAvatarCollision(puffer);//TODO:just find special interactions in the loop
		//when something in the bullets arraylist hits something send a hit message and move it to bulletStorage's location
		//when the player gets hit then lower the private int health from MyGame and put the bullet in storage
			//	if(bulletStorate.m31() > spot.cameraOffset+20f); bulletStorage.m31(spot.cameraOffset+10f);
			//statement to put somewhere so the storage is reusable instead of slowly building toward an overflow
		

		//----------------Height Map-----------------
		avatar.getWorldLocation(v);
		height = terr.getHeight(v.x(), v.z())+3.5f;
		avatar.heightAdjust(height);//avatar is the only one that needs to follow the heightmap
		avatar.getWorldTranslation(m);
        avatar.getPhysicsObject().setTransform(toDoubleArray(m.get(vals))); 
		
		//--------------HUD drawing----------------
		dispStr2 = "HEALTH: " + health;
		engine.getHUDmanager().setHUDValue(HUDCoords, dispStr2);
		
		//--------------Game Loop----------------
		im.update((float)elapsTime);


		//--------------Networking Update----------------
		processNetworking((float)elapsTime);
	}

// ---------- NETWORKING SECTION ----------------

	/*public AnimatedShape getAnimatedGhostShape(int ghostShapeID) 
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
			//case 3:
			//ghostShape = sphereS;
			//break;
			//case 4:
			//ghostShape = torusS;
			//break;
			//case 5:
			//ghostShape = crystalS;
			//break;
			//case 6:
			//ghostShape = cubeS;
			//break;
			
			default:
			ghostShape = pufferS;
			break;
		}
		return ghostShape;
	}
	public TextureImage getGhostTexture(int ghostTexID) 
	{
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
			
			//case 4:
			//ghostTex = pufferCalmX;
			//break; //add if time
			
			default:
			ghostTex = pufferX;
			break;
		}
		return ghostTex;
	}*/
	
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
		}
	}
}