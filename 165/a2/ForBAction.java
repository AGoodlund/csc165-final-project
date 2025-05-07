package a2;
import tage.GameObject;
import tage.Camera;

import tage.input.action.AbstractInputAction;
import net.java.games.input.Event;
import org.joml.*;

public class ForBAction extends AbstractInputAction {    //move camera+avatar forward
    private GameObject obj;
    private MyGame game;
    private Camera cam;
    private int direction = 1;
    private boolean keyboard = false;
    private float keyValue;
    private ProtocolClient protClient;

    private Vector3f fwdDir = new Vector3f(), v = new Vector3f();//oldPos, newPos, fwdDir;
    private Matrix4f loc = new Matrix4f();

    private float[] f = new float[3], vals = new float[16];
    
/** Constructor for camera and avatar movign in sync without keyboard */
    public ForBAction(MyGame g, Camera c){ cam = c; obj = g.getAvatar(); }
/** Constructor for avatar moving independently without keyboard */
    public ForBAction(MyGame g){ obj = g.getAvatar(); }
/** Constructor for Camera moving independently without keyboard */
    public ForBAction(Camera c){ cam = c; } 

/** Constructor for camera and avatar movign in sync with keyboard */
    public ForBAction(MyGame g, Camera c, int dir){ cam = c; obj = g.getAvatar(); direction = dir; keyboard = true; }
/** Constructor for avatar moving independently with keyboard */
    public ForBAction(MyGame g, int dir){ obj = g.getAvatar(); direction = dir; keyboard = true; }
/** Constructor for Camera moving independently with keyboard */
    public ForBAction(Camera c, int dir){ cam = c; direction = dir; keyboard = true; }
    
    public ForBAction(MyGame g, int dir, ProtocolClient p){ obj = g.getAvatar(); direction = dir; keyboard = true; protClient = p; }
    public ForBAction(MyGame g, Camera c, int dir, ProtocolClient p){ obj = g.getAvatar(); cam = c; direction = dir; keyboard = true; protClient = p; }

@Override
    public void performAction(float time, Event e){
        keyValue = e.getValue();
        if(keyValue > -0.2f && keyValue < 0.2f) return; //deadzone
        keyValue *= direction;

        //TODO:change to basic movement along xyz axes with the camera having it's xz updated to be the same as the obj
        if(obj != null){
            obj.getLocalLocation(v);
            fwdDir.set(0f,0f,keyValue);
            fwdDir.mul(time*spot.runSpeed);//*keyValue);
            v.add(fwdDir);
            obj.setLocalLocation(v);
//            obj.getLocalForwardVector(fwdDir);

//            if(keyboard) //TODO:if controller has wacky movement this is why
//                keyValue *= direction;
//            fwdDir.mul(time*spot.runSpeed*keyValue);
//            obj.getWorldLocation(v);
//            v.add(fwdDir);
//            obj.setLocalLocation(v); 

//physics object moving alongside object
            obj.getWorldTranslation(loc);
            obj.getPhysicsObject().setTransform(obj.toDoubleArray(loc.get(vals))); 
//            obj.getPhysicsObject().applyForce(0f,0f,spot.runSpeed*keyValue, 0f, 0f, 0f);
            
//this should just move to where the object is, but doesn't. ASK GORDON
                //goes specifically along the Z axis, irrelevant of facing         
        }

        if(cam != null){    //specifically for moving along floor with avatar. update moved to MyGame to keep up with the avatar physics object
//            cam.translate(0f, 0f, keyValue);
            cam.setLocation(v);
            cam.heightAdjust(spot.cameraOffset);
            //            obj.getWorldLocation(v);
//            cam.setLocation(v);
//            cam.heightAdjust(spot.cameraOffset);
//            cam.translate(spot.cameraOffset);

/* for free movement 
            oldPos = cam.getLocation();
            fwdDir = cam.getN(); //N is the camera's forward vector

//            if(keyboard)
                keyValue *= direction;
            fwdDir.mul(time*spot.runSpeed * keyValue);
            newPos = oldPos.add(fwdDir.x(),fwdDir.y(),fwdDir.z());

            cam.setLocation(newPos);
*/        }
        if(protClient != null)
		{   obj.getWorldLocation(v);
			protClient.sendMoveMessage(v);//obj.getWorldLocation());
//System.out.println("ForBAction moved to " + obj.getWorldLocation());
		}
    }    
}