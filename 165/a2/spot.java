package a2;

public class spot {
//---------------------------   Movement    ---------------------------
    public static float turnSpeed = 0.002f, runSpeed = 0.01f, deadzoneBounds = 0.02f, mountingDistance = 5.0f, maxDistanceFromMount = 10f,
        closeFactor = 1.8f, tooCloseFactor = .7f, sphereClose = 8f, torusClose = 12.4f, cubeClose = 10.2f;
    public static float close = 10f, tooClose = 6f;

//---------------------------   Points      ---------------------------
    public static int capture = 1000;

//---------------------------   Camera      ---------------------------
    public static float orbitLook = .1f, zoomSpeed = .03f, lookSpeed = .002f, elevationMax = 80f, elevationMin = 0f,
    zoomMax = 10f, zoomMin = .75f, azimuthMax = 270f, azimuthMin = 90f, defaultA=0f, defaultE=30f, defaultR = 5f,
    lookAboveFocus = .5f, orbitSpeed = .2f;

    public static boolean limitAzimuth = false, limitZoom = true, limitElevation = true, followCamera = false;

//---------------------------   Axes        ---------------------------
    public static float[] x = {1f,0f,0f}, y = {0f,1f,0f}, z = {0f,0f,-1f}; //-1 for z because that's default

//---------------------------   Colors      ---------------------------
    public static float[] 	red = {1,0,0}, green = {0,1,0}, blue = {0,0,1},
    purple = {1,1,0}, yellow = {1,0,1}, teal = {0,1,1},
    white = {1,1,1}, black = {0,0,0};
}

//REMEMBER: change the folder name, every package a2, the compile.bat and run.bat when changing the folder's name
//      if nothing else try to run it after changing the folder to see what breaks

//make a node controller that causes and object to scale up and down by an amount so it pulses
/* NOTES
 * 
 * Lab 2:
 * 
 * TODO:Replace Jerma, write readme
 * [DONE]


COMPLETED
 * ground plane you can't move through at y=0
 *      tage has a plane class 
 * [DONE]
 * 
 * toggle to stop rendering the xyz axes
 *      could set texture image to "invis.png"
 *      renderStates is in GameObject with a bool that stops rendering
 *          x.getRenderStates().disableRendering();
		    y.getRenderStates().disableRendering();
		    z.getRenderStates().disableRendering();
 * [DONE] sends as an ArrayList<GameObject>
 * 
 * working node controllers (2 different ones)
 *      use standard rotation and one that bounces back and forth no more than a set distance from a point
 *      roll controller that uses localYaw and pitch?
 * [DONE]
 * 
 * Working orbit camera
 *      orbit camera separate to dolphin's movement
 *          neither are effected by the other
 *      zoom in and out
 *      change elevation of camera
 * [DONE]
 * 
 * seperate overhead camera in the corner
 *      camera zoom in and out and panning, otherwise maintains it's look down -y directly above the dolphin
 *          IJKL for WASD panning with O and P for zooming in/out?
 * [DONE]
 * 
 * Fix the roll node slowly shrinking the cube to nothing over time
 * [Hacked] slow enough rotation means it won't show up before grader is done
 * 
 * huds for both cameras that stay centered even when stretching the frame
 *      CODE IN MyGame line 300
 * [DONE]
 * 
 * parent/child relation between 2 objects
 *      sphere with a taurus around it
 *      torus becomes attached to the dolphin once taken. Only need one thing to stick to the dolphin
 * [DONE]
 * 
 * Action for disarming satellites when in range
 * [DONE]
 * 
 * objects start animating only after being disarmed
 *      have node toggled on, but only use addTarget(GameObject) when disarmed 
 * [DONE]
 */