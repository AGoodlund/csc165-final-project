import tage.input.action.AbstractInputAction;
import net.java.games.input.Event;
import a2.spot;
import tage.Camera;
import tage.GameObject;
import org.joml.*;

public class ZoomCameraAction extends AbstractInputAction{      //TODO: add a direction to overload this into one class over in/out
    private Camera cam;
    private Vector3f loc;
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
        cam = c; loc = new Vector3f(obj.getWorldLocation()); keyboard = true;
    }
    public ZoomCameraAction(Camera c, Vector3f target, int dir){
        cam = c; loc = new Vector3f(target); keyboard = true; direction = dir;
    }
    public ZoomCameraAction(Camera c, int dir){ cam = c; loc = cam.getN(); keyboard = true; direction = dir; }

@Override
    public void performAction(float time, Event e){
        cam.lookAt(loc);
        oldPos = cam.getLocation();
        fwdDir = cam.getN();
        if(keyboard)
            fwdDir.mul(spot.zoomSpeed*time*direction);
        else
            fwdDir.mul(spot.zoomSpeed*time);
        newPos = oldPos.add(fwdDir.x(),fwdDir.y(),fwdDir.z());
        cam.setLocation(newPos);
    }
}