package a2;
import tage.Camera;
import tage.GameObject;

import tage.input.action.AbstractInputAction;
import net.java.games.input.Event;
import org.joml.*;

public class DLookAction extends AbstractInputAction{   //look+turn up
    private MyGame game;
    private GameObject obj;
    private Camera cam;

    public DLookAction(MyGame o, Camera c){ obj = o.getAvatar(); cam = c; game = o; }

    @Override
    public void performAction(float time, Event e){
        float keyValue = e.getValue();
        if(keyValue > -spot.deadzoneBounds && keyValue < spot.deadzoneBounds) return; //deadzone

        if(spot.followCamera)
            cam.pitch(-spot.turnSpeed*keyValue*time);
//        if(game.mounted)
//            obj.pitch(-spot.turnSpeed*keyValue*time);
    }
}
