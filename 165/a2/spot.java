package a2;

public class spot {
//---------------------------   Movement    ---------------------------
    public static float turnSpeed = 0.007f, runSpeed = .03f, deadzoneBounds = 0.02f, mountingDistance = 5.0f, maxDistanceFromMount = 10f,
        closeFactor = 1.8f, tooCloseFactor = .7f, sphereClose = 8f, torusClose = 12.4f, cubeClose = 10.2f;
    public static float close = 10f, tooClose = 6f;

//---------------------------   Points      ---------------------------
    public static int capture = 1000;

//---------------------------   Camera      ---------------------------
    public static float orbitLook = .1f, zoomSpeed = .03f, lookSpeed = .002f, elevationMax = 80f, elevationMin = 0f,
    zoomMax = 10f, zoomMin = .75f, azimuthMax = 270f, azimuthMin = 90f, defaultA=0f, defaultE=30f, defaultR = 5f,
    lookAboveFocus = .5f, orbitSpeed = .2f;

    public static float cameraOffset = 35f;

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
    public static int bubbleVolume = 25, bowVolume = 5;

//---------------------------   Scale       ---------------------------   
    public static float[] mapSize = {120f,2f,120f};//30,1,30};
    public static int mapTiling = 40;
}

//REMEMBER: change the folder name, every package a2, the compile.bat and run.bat when changing the folder's name
//      if nothing else try to run it after changing the folder to see what breaks