import tage.input.action.AbstractInputAction;
import net.java.games.input.Event;
import a2.spot;
import tage.Camera;
import tage.GameObject;
import org.joml.*;

public class ZoomCameraAction extends AbstractInputAction{      //TODO: add a direction to overload this into one class over in/out
    private Camera cam;
    private Vector3f loc = new Vector3f();
    private int direction;
    private boolean keyboard;

    private Vector3f oldPos, newPos, fwdDir;
/* 
    public ZoomInAction(Camera c, GameObject obj){
        cam = c; loc = new Vector3f(obj.getWorldLocation()); keyboard = false;
    }
    public ZoomInAction(Camera c, Vector3f target){
        cam = c; loc = new Vector3f(target); keyboard = false;
    }
    public ZoomInAction(Camera c){ cam = c; loc = cam.getN(); keyboard = false; }
*/
    public ZoomCameraAction(Camera c, GameObject obj, int dir){
        cam = c; keyboard = true; obj.getWorldLocation(loc);//loc = new Vector3f(obj.getWorldLocation()); 
    }
    public ZoomCameraAction(Camera c, Vector3f target, int dir){
        cam = c; keyboard = true; direction = dir; loc.set(target);//loc = new Vector3f(target); 
    }
    public ZoomCameraAction(Camera c, int dir){ cam = c; keyboard = true; direction = dir; cam.getN(loc); }//loc = cam.getN(); }

@Override
    public void performAction(float time, Event e){
        cam.lookAt(loc);
        cam.getLocation(oldPos);// = cam.getLocation();
        cam.getN(fwdDir);// = cam.getN();
        if(keyboard)
            fwdDir.mul(spot.zoomSpeed*time*direction);
        else
            fwdDir.mul(spot.zoomSpeed*time);
        newPos = oldPos.add(fwdDir.x(),fwdDir.y(),fwdDir.z());
        cam.setLocation(newPos);
    }
}