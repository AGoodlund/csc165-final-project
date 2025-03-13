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
    private int direction;
    private boolean keyboard;
    private float keyValue;

    private Vector3f oldPos, newPos, strafeDir;
    
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

@Override
    public void performAction(float time, Event e){
        keyValue = e.getValue();
        if(keyValue > -0.2f && keyValue < 0.2f) return; //deadzone

        if(cam != null){
            oldPos = cam.getLocation();
            strafeDir = cam.getU(); //U is the camera's right vector

            if(keyboard)
                keyValue *= direction;
            strafeDir.mul(time*spot.runSpeed * keyValue);
            newPos = oldPos.add(strafeDir.x(),strafeDir.y(),strafeDir.z());
            cam.setLocation(newPos);
        }

        if(game != null){
            strafeDir = obj.getLocalRightVector();

            if(keyboard)
                keyValue *= direction;
            strafeDir.mul(time*spot.runSpeed*keyValue);
            obj.setLocalLocation(obj.getWorldLocation().add(strafeDir.x(),strafeDir.y(),strafeDir.z()));
        }
    }    
}
