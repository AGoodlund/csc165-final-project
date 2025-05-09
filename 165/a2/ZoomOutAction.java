import tage.input.action.AbstractInputAction;
import net.java.games.input.Event;
import a2.spot;
import tage.Camera;
import tage.GameObject;
import org.joml.*;

public class ZoomOutAction extends AbstractInputAction{
    private Camera cam;
    private Vector3f loc;

    public ZoomOutAction(Camera c, GameObject obj){
        cam = c; loc = new Vector3f(obj.getWorldLocation());
    }
    public ZoomOutAction(Camera c, Vector3f target){
        cam = c; loc = new Vector3f(target);
    }
    public ZoomOutAction(Camera c){ cam = c; loc = cam.getN(); }

@Override
    public void performAction(float time, Event e){
        Vector3f oldPos = new Vector3f();
        Vector3f newPos = new Vector3f();
        Vector3f fwdDir = new Vector3f();
        cam.lookAt(loc);
        oldPos = cam.getLocation();
        fwdDir = cam.getN();
        fwdDir.mul(-spot.zoomSpeed*time);
        newPos = oldPos.add(fwdDir.x(),fwdDir.y(),fwdDir.z());
        cam.setLocation(newPos);
    }
}