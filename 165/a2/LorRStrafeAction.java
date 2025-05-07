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

    private Vector3f strafeDir = new Vector3f(), v = new Vector3f();//oldPos, newPos, strafeDir;
    private Matrix4f loc = new Matrix4f();
    private float[] vals = new float[16];
//    private float[] f = new float[3];
    
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

        if(obj != null){
//            strafeDir = obj.getLocalRightVector();
            obj.getLocalRightVector(strafeDir);

//            if(keyboard)
                keyValue *= direction;
            strafeDir.mul(time*spot.runSpeed*keyValue);
            obj.getWorldLocation(v);
            v.add(strafeDir);
            obj.setLocalLocation(v);
//            obj.setLocalLocation(obj.getWorldLocation().add(strafeDir.x(),strafeDir.y(),strafeDir.z()));
            
//physics object moving alingside object
            obj.getWorldTranslation(loc);
            obj.getPhysicsObject().setTransform(obj.toDoubleArray(loc.get(vals))); 
/*             cam.getU(v);
            f[0]=v.x*keyValue*time; f[1]=0f; f[2]=v.z*keyValue*time;
            obj.getPhysicsObject().setLinearVelocity(f);
*/        }

        if(cam != null){
//            cam.setLocation(v);
//            cam.heightAdjust(spot.cameraOffset);
        }

        if(protClient != null){
			obj.getWorldLocation(v); protClient.sendMoveMessage(v);}//obj.getWorldLocation());
    }    
}
