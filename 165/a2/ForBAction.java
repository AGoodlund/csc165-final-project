package a2;
import tage.GameObject;
import tage.Camera;

import tage.input.action.AbstractInputAction;
import net.java.games.input.Event;
import org.joml.*;

public class ForBAction extends AbstractInputAction {    //move camera+avatar forward
    private GameObject obj;
    private Camera cam;
    private int direction;
    private boolean keyboard;
    private float keyValue;
    private float limit;

    private Vector3f oldPos, newPos, fwdDir;
    
/** Constructor for camera and avatar movign in sync without keyboard */
    public ForBAction(MyGame g, Camera c){ cam = c; obj = g.getAvatar(); keyboard = false; }
/** Constructor for avatar moving independently without keyboard */
    public ForBAction(MyGame g){ cam = null; obj = g.getAvatar(); keyboard = false; }
/** Constructor for Camera moving independently without keyboard */
    public ForBAction(Camera c){ cam = c; obj = null; keyboard = false;  } 

/** Constructor for camera and avatar movign in sync with keyboard */
    public ForBAction(MyGame g, Camera c, int dir){ cam = c; obj = g.getAvatar(); direction = dir; keyboard = true; }
/** Constructor for avatar moving independently with keyboard */
    public ForBAction(MyGame g, int dir){ cam = null; obj = g.getAvatar(); direction = dir; keyboard = true; }
/** Constructor for Camera moving independently with keyboard */
    public ForBAction(Camera c, int dir){ cam = c; obj = null; direction = dir; keyboard = true; }

    public ForBAction(Camera c, int dir, float limitPlane){ cam = c; obj = null; direction = dir; keyboard = true; limit = limitPlane; } //TODO: contemplate if this is needed

@Override
    public void performAction(float time, Event e){

        keyValue = e.getValue();
        if(keyValue > -0.2f && keyValue < 0.2f) return; //deadzone

        if(cam != null){
            oldPos = cam.getLocation();
            fwdDir = cam.getN(); //N is the camera's forward vector

            if(keyboard)
                keyValue *= direction;
            fwdDir.mul(time*spot.runSpeed * keyValue);
            newPos = oldPos.add(fwdDir.x(),fwdDir.y(),fwdDir.z());
            if(newPos.y() < limit)
                ;
            else
                cam.setLocation(newPos);
        }

        if(obj != null){
            fwdDir = obj.getLocalForwardVector();

            if(keyboard)
                keyValue *= direction;
            fwdDir.mul(time*spot.runSpeed*keyValue);
            obj.setLocalLocation(obj.getWorldLocation().add(fwdDir.x(),fwdDir.y(),fwdDir.z()));
        }

/*      this moves the dolphin and not the camera so cam has to be tied to dolphin explicitely
        object = game.getAvatar();
        oldPos = object.getWorldLocation();
        fwdDir = new Vector4f(0f,0f,1f,1f);
        fwdDir.mul(object.getWorldRotation());
        fwdDir.mul(0.01f);
        newPos = oldPos.add(fwdDir.x(),fwdDir.y(),fwdDir.z());
        object.setLocalLocation(newPos);
*/    }    
}
/*@Override
public void performAction(float time, Event e)
{ c = (game.getEngine().getRenderSystem())
.getViewport("MAIN").getCamera();
oldPosition = c.getLocation();
fwdDirection = c.getN();
fwdDirection.mul(0.01f);
newPosition = oldPosition.add(fwdDirection.x(),
fwdDirection.y(), fwdDirection.z());
c.setLocation(newPosition);
} */

/*
 * 		Vector3f loc, fwd, up, right, newLocation;
		Camera cam;		
        
        dol is object
        fwd = fwdDir
        loc = oldPos
        newLocation = newPos

 *              fwd = dol.getWorldForwardVector();
				loc = dol.getWorldLocation();
				newLocation = loc.add(fwd.mul(0.02f));
				dol.setLocalLocation(newLocation);

 */