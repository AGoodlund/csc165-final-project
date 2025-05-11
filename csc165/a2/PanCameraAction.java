package a2;

import tage.Camera;
import org.joml.*;
import net.java.games.input.Event;
import tage.GameObject;
import tage.input.action.AbstractInputAction;

public class PanCameraAction extends AbstractInputAction{
    private Camera c;
    private GameObject o;
    private boolean isFlat = false;
    private Vector3f v;

    public PanCameraAction(Camera cam, MyGame game){
        o = game.getAvatar();
        c = cam;
        v = new Vector3f();
    }

    public void performAction(float time, Event e){
        o.getWorldLocation(v);

        isFlat = !isFlat;
        if(isFlat)
            v.set(v.x(), 1, spot.cameraOffset);
        else
            v.set(v.x(), spot.cameraOffset, v.z());
        c.setLocation(v);
        c.lookAt(o);
    }
}
