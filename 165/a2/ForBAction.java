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

    private Vector3f fwdDir = new Vector3f(), v = new Vector3f();
    private Matrix4f loc = new Matrix4f();

    private float[] vals = new float[16];
    
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
//TODO:start animation and add StopAnimatingAction action to the key's 'on release' setting
//        if(keyboard)  if controller has wacky movement this being commented out is why
            keyValue *= direction;

        if(obj != null){
            obj.getLocalLocation(v);
            fwdDir.set(0f,0f,keyValue);
            fwdDir.mul(time*spot.runSpeed);//*keyValue);
            v.add(fwdDir);
            obj.setLocalLocation(v);

//physics object moving alongside object
            obj.getWorldTranslation(loc);
            obj.getPhysicsObject().setTransform(obj.toDoubleArray(loc.get(vals))); 
        }

        if(cam != null){
            cam.setLocation(v);
            cam.heightAdjust(spot.cameraOffset);
        }
        if(protClient != null)
		{   obj.getWorldLocation(v);
			protClient.sendMoveMessage(v);
//System.out.println("ForBAction moved to " + obj.getWorldLocation());
		}
    }    
}