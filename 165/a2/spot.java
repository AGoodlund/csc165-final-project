package a2;

public class spot {
//---------------------------   Movement    ---------------------------
    public static float turnSpeed = 0.002f, runSpeed = .03f, deadzoneBounds = 0.02f, mountingDistance = 5.0f, maxDistanceFromMount = 10f,
        closeFactor = 1.8f, tooCloseFactor = .7f, sphereClose = 8f, torusClose = 12.4f, cubeClose = 10.2f;
    public static float close = 10f, tooClose = 6f;

//---------------------------   Points      ---------------------------
    public static int capture = 1000;

//---------------------------   Camera      ---------------------------
    public static float orbitLook = .1f, zoomSpeed = .03f, lookSpeed = .002f, elevationMax = 80f, elevationMin = 0f,
    zoomMax = 10f, zoomMin = .75f, azimuthMax = 270f, azimuthMin = 90f, defaultA=0f, defaultE=30f, defaultR = 5f,
    lookAboveFocus = .5f, orbitSpeed = .2f;

    public static float cameraOffset = 2f;

//---------------------------   Orbit Camera---------------------------
    public static boolean limitAzimuth = false, limitZoom = true, limitElevation = true, followCamera = false;

//---------------------------   Mouse Camera---------------------------
    public static float mouseSensitivity = 0.09f, pitchMaxUp = 0f, pitchMatchDown = 180f;

//---------------------------   Axes        ---------------------------
    public static float[] x = {1f,0f,0f}, y = {0f,1f,0f}, z = {0f,0f,-1f}; //-1 for z because that's default

//---------------------------   Colors      ---------------------------
    public static float[] 	red = {1,0,0}, green = {0,1,0}, blue = {0,0,1},
                            yellow = {1,1,0}, purple = {1,0,1}, teal = {0,1,1},
                            white = {1,1,1}, black = {0,0,0};
    public static float[]   dimRed = {.5f,0,0}, dimGreen = {0,.5f,0}, dimBlue = {0,0,.5f},
                            dimYellow = {.5f,.5f,0}, dimPurple = {.5f,0,.5f}, dimTeal = {0,.5f,.5f},
                            grey = {.5f,.5f,.5f};

//---------------------------   Ghosts      ---------------------------
    public static float startingScale = 3f;

//---------------------------   Sound       ---------------------------    
    public static int bubbleVolume = 0;//25;
}

//REMEMBER: change the folder name, every package a2, the compile.bat and run.bat when changing the folder's name
//      if nothing else try to run it after changing the folder to see what breaks

/* TODO:
 * character selection upon joining
 *      model or texture choosing
 *      doesn't *have* to be both
 * character selection effects the texture and/or shape of ghosts
 * Add 3 lights to the scene
 *      jellyfish positional lights that are the same color as the solidColor for the fish
 *      if one gets shot the light toggles off for 5 seconds
 * HUD elements 
 *      Health
 * Hierarchical SceneGraph between at least 2 objects
 *      harpoon becoming child object of whatever it hits
 * stop physics based movement from being so jittery
 * physics object that shares XZ with character box and Y with heightmap
 * shooting harpoon
 *      NPC takes damage from the shot
 *      NPC dies and respawns in the level
 *      Player has health
 *      Player resets to the raft if they die
 * harpoon sending back a hit message to put "HIT!" above cursor for HUD element
 * physics plane walls to prevent player from going past the bounds of the ground
 * UorDLookAction for controllers
 * 
 * 

COMPLETED
 * 
 * skybox not from tage or 155
 * animated model added to game
 */