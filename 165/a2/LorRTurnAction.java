package a2;
import tage.GameObject;
import tage.Camera;

import tage.input.action.AbstractInputAction;
import net.java.games.input.Event;

public class LorRTurnAction extends AbstractInputAction {    //move camera+avatar forward
    private GameObject obj = null;
    private Camera cam = null;
    private int direction = 1;
    private boolean keyboard = false;
    private float keyValue;
    private ProtocolClient protClient = null;
    
/** Constructor for camera and avatar movign in sync without keyboard */
    public LorRTurnAction(MyGame g, Camera c){ cam = c; obj = g.getAvatar(); }
/** Constructor for avatar moving independently without keyboard */
    public LorRTurnAction(MyGame g){ obj = g.getAvatar(); }
/** Constructor for Camera moving independently without keyboard */
    public LorRTurnAction(Camera c){ cam = c; } 

/** Constructor for camera and avatar movign in sync with keyboard */
    public LorRTurnAction(MyGame g, Camera c, int dir){ cam = c; obj = g.getAvatar(); direction = dir; keyboard = true; }
/** Constructor for avatar moving independently with keyboard */
    public LorRTurnAction(MyGame g, int dir){ obj = g.getAvatar(); direction = dir; keyboard = true; }
/** Constructor for Camera moving independently with keyboard */
    public LorRTurnAction(Camera c, int dir){ cam = c; direction = dir; keyboard = true; } 

    public LorRTurnAction(MyGame g, int dir, ProtocolClient p){ obj = g.getAvatar(); direction = dir; keyboard = true; protClient = p; }

@Override
    public void performAction(float time, Event e){

        keyValue = e.getValue();
        if(keyValue > -spot.deadzoneBounds && keyValue < spot.deadzoneBounds) return; //deadzone
//        if(keyboard)
            keyValue *= direction;
        if(cam != null)
            cam.yaw(time * spot.turnSpeed * keyValue);
        if(obj != null)
            obj.yaw(time * spot.turnSpeed * keyValue);
        if(protClient != null)
            protClient.sendTurnMessage(obj.getLocalRotation());
    }    
}
