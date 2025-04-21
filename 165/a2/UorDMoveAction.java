package a2;
import tage.GameObject;
import tage.Camera;

import tage.input.action.AbstractInputAction;
import net.java.games.input.Event;
import org.joml.*;

public class UorDMoveAction extends AbstractInputAction {    //move camera+avatar forward
    private MyGame game;
    private GameObject obj;
    private Camera cam;
    private int direction;
    private boolean keyboard;
    private float keyValue;

    private Vector3f oldPos, newPos, upDir;
    
/** Constructor for camera and avatar movign in sync without keyboard */
    public UorDMoveAction(MyGame g, Camera c){ game = g; cam = c; obj = g.getAvatar(); keyboard = false; }
/** Constructor for avatar moving independently without keyboard */
    public UorDMoveAction(MyGame g){ game = g; cam = null; obj = g.getAvatar(); keyboard = false; }
/** Constructor for Camera moving independently without keyboard */
    public UorDMoveAction(Camera c){ cam = c; game = null; obj = null; keyboard = false; } 

/** Constructor for camera and avatar movign in sync with keyboard */
    public UorDMoveAction(MyGame g, Camera c, int dir){ game = g; cam = c; obj = g.getAvatar(); direction = dir; keyboard = true; }
/** Constructor for avatar moving independently with keyboard */
    public UorDMoveAction(MyGame g, int dir){ game = g; cam = null; obj = g.getAvatar(); direction = dir; keyboard = true; }
/** Constructor for Camera moving independently with keyboard */
    public UorDMoveAction(Camera c, int dir){ cam = c; game = null; obj = null; direction = dir; keyboard = true; } 

@Override
    public void performAction(float time, Event e){
        keyValue = e.getValue();
        if(keyValue > -0.2f && keyValue < 0.2f) return; //deadzone

        if(cam != null){
            cam.getLocation(oldPos);//oldPos = cam.getLocation();
            cam.getV(upDir);//upDir = cam.getV(); //V is the camera's up vector

            if(keyboard)
                keyValue *= direction;
            upDir.mul(time*spot.runSpeed * keyValue);
            newPos = oldPos.add(upDir.x(),upDir.y(),upDir.z());
            cam.setLocation(newPos);
        }

        if(game != null){
            obj.getLocalUpVector(upDir);//upDir = obj.getLocalUpVector();

            if(keyboard)
                keyValue *= direction;
            upDir.mul(time*spot.runSpeed*keyValue);
            obj.getWorldLocation(newPos); obj.setLocalLocation(newPos.add(upDir.x(),upDir.y(),upDir.z()));
        }
    }    
}
