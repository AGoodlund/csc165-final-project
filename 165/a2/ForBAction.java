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

        if(obj != null){
//            fwdDir = obj.getLocalForwardVector();
            obj.getLocalForwardVector(fwdDir);

//            if(keyboard) //TODO:if controller has wacky movement this is why
                keyValue *= direction;
            fwdDir.mul(time*spot.runSpeed*keyValue);
            obj.getWorldLocation(v);
            v.add(fwdDir);
            obj.setLocalLocation(v);
//            obj.setLocalLocation(obj.getWorldLocation().add(fwdDir.x(),fwdDir.y(),fwdDir.z()));
        }

        if(cam != null){    //specifically for moving along floor with avatar
//            obj.getLocalLocation(v);
            cam.setLocation(v);
            cam.translate(0f,2f,0f);

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
		{
			protClient.sendMoveMessage(obj.getWorldLocation());
//System.out.println("ForBAction moved to " + obj.getWorldLocation());
		}
    }    
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