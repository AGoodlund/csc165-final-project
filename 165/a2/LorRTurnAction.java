package a2;
import tage.GameObject;
import tage.Camera;

import tage.input.action.AbstractInputAction;
import net.java.games.input.Event;
import org.joml.*;

public class LorRTurnAction extends AbstractInputAction {    //move camera+avatar forward
    private GameObject obj;
    private Camera cam;
    private int direction;
    private boolean keyboard;
    private float keyValue;
    
/** Constructor for camera and avatar movign in sync without keyboard */
    public LorRTurnAction(MyGame g, Camera c){ cam = c; obj = g.getAvatar(); keyboard = false; }
/** Constructor for avatar moving independently without keyboard */
    public LorRTurnAction(MyGame g){ cam = null; obj = g.getAvatar(); keyboard = false; }
/** Constructor for Camera moving independently without keyboard */
    public LorRTurnAction(Camera c){ cam = c; obj = null; keyboard = false; } 

/** Constructor for camera and avatar movign in sync with keyboard */
    public LorRTurnAction(MyGame g, Camera c, int dir){ cam = c; obj = g.getAvatar(); direction = dir; keyboard = true; }
/** Constructor for avatar moving independently with keyboard */
    public LorRTurnAction(MyGame g, int dir){ cam = null; obj = g.getAvatar(); direction = dir; keyboard = true; }
/** Constructor for Camera moving independently with keyboard */
    public LorRTurnAction(Camera c, int dir){ cam = c; obj = null; direction = dir; keyboard = true; } 

@Override
    public void performAction(float time, Event e){

        keyValue = e.getValue();
        if(keyValue > -spot.deadzoneBounds && keyValue < spot.deadzoneBounds) return; //deadzone
        if(keyboard)
            keyValue *= direction;
        if(cam != null)
            cam.yaw(time * spot.turnSpeed * keyValue);
        if(obj != null)
            obj.yaw(time * spot.turnSpeed * keyValue);
    }    
}
