package a2; 
import tage.Camera;
import tage.GameObject;

import tage.input.action.AbstractInputAction;
import net.java.games.input.Event;
import org.joml.*;

public class RTurnAction extends AbstractInputAction{       //look+turn right
    private MyGame game;
    private GameObject obj;    //this needs to change to a GameObject to turn the avatar
    private Camera cam;       //DON'T FORGET TO ALTER LTurnAction TOO

/** Constructor for camera and avatar movign in sync */
    public RTurnAction(MyGame g, Camera c){ obj = g.getAvatar(); cam = c; game = g; }
/** Constructor for avatar moving independently */
    public RTurnAction(MyGame g){ game = g; cam = null; obj = g.getAvatar(); }
/** Constructor for Camera moving independently */
    public RTurnAction(Camera c){ cam = c; game = null; obj = null; }

@Override
    public void performAction(float time, Event e){
        float keyValue = e.getValue();
        if(keyValue > -spot.deadzoneBounds && keyValue < spot.deadzoneBounds) return; //deadzone


        if(cam != null)
            cam.yaw(time * -spot.turnSpeed*keyValue);
        if(obj != null)
          obj.yaw(time * -spot.turnSpeed*keyValue);

/*         rightVec = c.getU(); upVec = c.getV(); fwdVec = c.getN();

        rightVec.rotateAxis(-0.01f, upVec.x(), upVec.y(), upVec.z());
        fwdVec.rotateAxis(-0.01f, upVec.x(), upVec.y(), upVec.z());

        c.setU(rightVec); c.setN(fwdVec);
*/    }   
};