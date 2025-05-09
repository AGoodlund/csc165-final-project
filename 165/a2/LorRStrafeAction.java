package a2;
import tage.GameObject;
import tage.Camera;

import tage.input.action.AbstractInputAction;
import net.java.games.input.Event;
import org.joml.*;

public class LorRStrafeAction extends AbstractInputAction {    //move camera+avatar forward
    private MyGame game;
    private GameObject obj;
    private Camera cam;
    private int direction = 1;
    private boolean keyboard = false;
    private float keyValue;
    private ProtocolClient protClient;

    private Vector3f strafeDir = new Vector3f(), v = new Vector3f();
    private Matrix4f loc = new Matrix4f();
    private float[] vals = new float[16];
    
/** Constructor for camera and avatar movign in sync without keyboard */
    public LorRStrafeAction(MyGame g, Camera c){ game = g; cam = c; obj = g.getAvatar(); keyboard = false; }
/** Constructor for avatar moving independently without keyboard */
    public LorRStrafeAction(MyGame g){ game = g; cam = null; obj = g.getAvatar(); keyboard = false; }
/** Constructor for Camera moving independently without keyboard */
    public LorRStrafeAction(Camera c){ cam = c; game = null; obj = null; keyboard = false; } 

/** Constructor for camera and avatar movign in sync with keyboard */
    public LorRStrafeAction(MyGame g, Camera c, int dir){ game = g; cam = c; obj = g.getAvatar(); direction = dir; keyboard = true; }
/** Constructor for avatar moving independently with keyboard */
    public LorRStrafeAction(MyGame g, int dir){ game = g; cam = null; obj = g.getAvatar(); direction = dir; keyboard = true; }
/** Constructor for Camera moving independently with keyboard */
    public LorRStrafeAction(Camera c, int dir){ cam = c; game = null; obj = null; direction = dir; keyboard = true; } 

    public LorRStrafeAction(MyGame g, int dir, ProtocolClient p){ obj = g.getAvatar(); direction = dir; keyboard = true; protClient = p; }
    public LorRStrafeAction(MyGame g, Camera c, int dir, ProtocolClient p){ obj = g.getAvatar(); cam = c; direction = dir; keyboard = true; protClient = p; }

@Override
    public void performAction(float time, Event e){

        keyValue = e.getValue();
        if(keyValue > -0.2f && keyValue < 0.2f) return; //deadzone

        keyValue *= direction;
//TODO: perhaps limit camera to being at 0 and only moves far enough so that the ground is always in view. Hallway method
        if(obj != null){
            obj.getLocalLocation(v);
            strafeDir.set(keyValue,0f,0f);
            strafeDir.mul(time*spot.runSpeed);
            v.add(strafeDir);
            obj.setLocalLocation(v);
            
            obj.getWorldTranslation(loc);
            obj.getPhysicsObject().setTransform(obj.toDoubleArray(loc.get(vals))); 
        }

        if(cam != null){
            cam.setLocation(v);
            cam.heightAdjust(spot.cameraOffset);
        }

        if(protClient != null){
			obj.getWorldLocation(v); protClient.sendMoveMessage(v);}//obj.getWorldLocation());
    }    
}
