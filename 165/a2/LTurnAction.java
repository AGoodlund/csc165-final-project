package a2; 
import tage.GameObject;
import tage.Camera;

import tage.input.action.AbstractInputAction;
import net.java.games.input.Event;
import org.joml.*;

public class LTurnAction extends AbstractInputAction{       //look+turn left
    private MyGame game;
    private GameObject obj;
    private Camera cam;

/** Constructor for camera and avatar movign in sync */
    public LTurnAction(MyGame g, Camera c){ obj = g.getAvatar(); cam = c; game = g; }
/** Constructor for avatar moving independently */
    public LTurnAction(MyGame g){ game = g; cam = null; obj = g.getAvatar(); }
/** Constructor for Camera moving independently */
    public LTurnAction(Camera c){ cam = c; game = null; obj = null; }

@Override
    public void performAction(float time, Event e){
        float keyValue = e.getValue();
        if(keyValue > -0.2f && keyValue < 0.2f) return; //deadzone

        if(cam != null)
            cam.yaw(spot.turnSpeed*keyValue*time);
        
        if(game != null)
            obj.yaw(spot.turnSpeed*keyValue*time);
//        c = (game.getEngine().getRenderSystem()).getViewport("MAIN").getCamera();
/*        rightVec = c.getU(); upVec = c.getV(); fwdVec = c.getN();

        rightVec.rotateAxis(0.01f, upVec.x(), upVec.y(), upVec.z());    //to turn left use -0.01f for these two
        fwdVec.rotateAxis(0.01f, upVec.x(), upVec.y(), upVec.z());

        c.setU(rightVec); c.setN(fwdVec);
*/     }
  
};