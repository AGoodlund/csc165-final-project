package a2;
import tage.Camera;
import tage.GameObject;

import tage.input.action.AbstractInputAction;
import net.java.games.input.Event;
import org.joml.*;

public class ULookAction extends AbstractInputAction{   //look+turn up
    private MyGame game;
    private GameObject obj;
    private Camera cam;

    public ULookAction(MyGame g, Camera c){ obj = g.getAvatar(); cam = c; game = g; }

    @Override
    public void performAction(float time, Event e){
        float keyValue = e.getValue();
        if(keyValue > -spot.deadzoneBounds && keyValue < spot.deadzoneBounds) return; //deadzone
        
        if(spot.followCamera)
            cam.pitch(spot.turnSpeed*keyValue*time);
        if(game.mounted)
            obj.pitch(spot.turnSpeed*keyValue*time);
    }
}
