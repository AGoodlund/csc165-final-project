package tage;
import tage.GameObject;
import tage.input.InputManager;
import tage.input.action.AbstractInputAction;
import net.java.games.input.Event;
import a2.spot;
import org.joml.*;
import java.lang.Math;

/**
* A CameraOrbit3D specifies the rendering viewpoint for a Viewport.
* A CameraOrbit3D instance includes all public functions from Camera.
* <p>
* Coordinates are in polar coordinates that can be translated to cartesian coordinates
* for a Camera instance by modifying location, U, V, and N based on user input.
* The default camera position is directly behind and slightly above its attached object.
*/
public class CameraOrbit3D extends Camera{ //code from https://csus.instructure.com/courses/130924/files/22876802?module_item_id=6834555
    private Engine engine;
    private Camera cam;
    private GameObject focus;
    private float a, e, r; 
//(a)zimuth is the pitch, (e)levation is the yaw, (r)adis is distance from focus
//azimuth: counted in degrees away from object's facing
//elevation: counted in degrees above object's facing

    private Vector3f avatarRot;
    private double avatarAngle, phi, theta;
    private float totalAz, x, y, z;
    
    public CameraOrbit3D(Engine e, Camera c, GameObject obj, String gamepadName){
        engine = e;
        cam = c;
        focus = obj;
        setDefaults();
        setInput(gamepadName); //gamepad is null if nothing is plugged in
        updateCameraPosition();
	}
    private void setDefaults(){ a=spot.defaultA; e=spot.defaultE; r=spot.defaultR; }
/** sets a new object the camera orbits */
    public void setNewTarget(GameObject obj){ focus = obj; /*setDefaults();*/ }

    private void setInput(String gamepad){

        if(gamepad != null){
            OrbitAzimuthAction orbit = new OrbitAzimuthAction();
            OrbitElevationAction ele = new OrbitElevationAction();
            OrbitZoomAction zoom = new OrbitZoomAction();

            engine.getInputManager().associateAction(gamepad,net.java.games.input.Component.Identifier.Axis.RX, orbit,
                InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
            engine.getInputManager().associateAction(gamepad,net.java.games.input.Component.Identifier.Axis.RY, ele,
                InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
//TODO: find the shoulder buttons on a controller
            engine.getInputManager().associateAction(gamepad,net.java.games.input.Component.Identifier.Axis.Z, zoom,
                InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
        }       //might need to do associateActionWithAllGamepads rather than with the string
        else{
            OrbitAzimuthAction LOrbit = new OrbitAzimuthAction(-1);
            OrbitAzimuthAction ROrbit = new OrbitAzimuthAction(1);
            OrbitElevationAction Uele = new OrbitElevationAction(1);
            OrbitElevationAction Dele = new OrbitElevationAction(-1);
            OrbitZoomAction zoomIn = new OrbitZoomAction(-1);
            OrbitZoomAction zoomOut = new OrbitZoomAction(1);
            engine.getInputManager().associateActionWithAllKeyboards(net.java.games.input.Component.Identifier.Key.LEFT, LOrbit, 
                InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
            engine.getInputManager().associateActionWithAllKeyboards(net.java.games.input.Component.Identifier.Key.RIGHT, ROrbit, 
                InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
            engine.getInputManager().associateActionWithAllKeyboards(net.java.games.input.Component.Identifier.Key.UP, Uele, 
                InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
            engine.getInputManager().associateActionWithAllKeyboards(net.java.games.input.Component.Identifier.Key.DOWN, Dele, 
                InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
            engine.getInputManager().associateActionWithAllKeyboards(net.java.games.input.Component.Identifier.Key.Q, zoomIn,
                InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
            engine.getInputManager().associateActionWithAllKeyboards(net.java.games.input.Component.Identifier.Key.E, zoomOut,
                InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
        }
    } 

/** Update the x,y,z coordinates of the orbit camera. Needs to be called every frame its position changes */
    public void updateCameraPosition(){
/* This chunk of code causes the camera to move in a sin wave for some reason
Vector3f forWorld = focus.getWorldForwardVector();
double theta = Math.toRadians(a - (float)Math.toDegrees((double)forWorld.angleSigned(new Vector3f(0f,0f,-1f), new Vector3f(0f,1f,0f))));
double phi = Math.toRadians(e);
cam.setLocation(new Vector3f(getX(phi, theta), getY(theta), getZ(phi, theta)).add(focus.getWorldLocation()));
cam.lookAt(focus);*/

            avatarRot = focus.getWorldForwardVector();
            avatarAngle = Math.toDegrees((double)
            avatarRot.angleSigned(new Vector3f(0,0,-1), new Vector3f(0,1,0)));
            totalAz = a - (float)avatarAngle;
            theta = Math.toRadians(totalAz);
            phi = Math.toRadians(e);
            x = r * (float)(Math.cos(phi) * Math.sin(theta));
            y = r * (float)(Math.sin(phi));
            z = r * (float)(Math.cos(phi) * Math.cos(theta));
            cam.setLocation(new
            Vector3f(x,y,z).add(focus.getWorldLocation()));
            cam.lookAt(focus);

    }
    private float getX(double phi, double theta){ return r * (float)(Math.cos(phi) * Math.sin(theta)); }
    private float getY(double phi){ return r * (float)(Math.sin(phi)); }
    private float getZ(double phi, double theta){ return r * (float)(Math.cos(phi) * Math.cos(theta)); }

private class OrbitAzimuthAction extends AbstractInputAction{//TODO: moves in sin wave
        private int direction;
        private boolean keyboard;
        private float rotAmount;
        public OrbitAzimuthAction(int dir){ direction = dir; keyboard = true; }  //positive is right negative is left
        public OrbitAzimuthAction(){ direction = 1; keyboard = false; }

@Override
    public void performAction(float time, Event e){
        if(e.getValue() < -spot.deadzoneBounds || e.getValue() > spot.deadzoneBounds)
            rotAmount = spot.orbitSpeed * time * e.getValue();
        else
            rotAmount = 0f;
        if(keyboard)      //e.getValue() == 1.0 on keyboard for turns
            rotAmount *= direction;

        a += rotAmount;
        a = a % 360;
        if(spot.limitAzimuth){
            if(a > spot.azimuthMax)
                a = spot.azimuthMax;
            else if(a < spot.azimuthMin)
                a = spot.azimuthMin;
        }
        updateCameraPosition();
    }
}
private class OrbitElevationAction extends AbstractInputAction{         //TODO: elevation only moves along the forward axis, not up/down the y axis
    private int direction;
    private boolean keyboard;
    private float eleAmount;
    public OrbitElevationAction(int dir){ direction = dir; keyboard = true; }  //positive is right negative is left
    public OrbitElevationAction(){ direction = 1; keyboard = false; }

    @Override
    public void performAction(float time, Event ev){

        if(ev.getValue() < -spot.deadzoneBounds || ev.getValue() > spot.deadzoneBounds)
            eleAmount = spot.orbitSpeed * time * ev.getValue();
        else
            eleAmount = 0f;

        if(keyboard)      //e.getValue() == 1.0 on keyboard for turns
            eleAmount *= direction;
        
        e += eleAmount;

        if(spot.limitElevation){
            if(e > spot.elevationMax)
                e = spot.elevationMax;
            else if(e < spot.elevationMin)
                e = spot.elevationMin;
        }

        updateCameraPosition();        
    }
}
private class OrbitZoomAction extends AbstractInputAction{
    private int direction;
    private float zoomAmount;
    public OrbitZoomAction(int dir){ direction = dir; }
    public OrbitZoomAction(){ direction = 0; }

@Override
    public void performAction(float time, Event e){
        if(e.getValue() < -spot.deadzoneBounds || e.getValue() > spot.deadzoneBounds)
            zoomAmount = e.getValue();
        if(direction == 0)
            r += spot.zoomSpeed*time*zoomAmount;
        else
            r += spot.zoomSpeed*time*direction;

        if(r < spot.zoomMin)
            r = spot.zoomMin;
        else if(r > spot.zoomMax)
            r = spot.zoomMax;

        updateCameraPosition();
    }
}
}