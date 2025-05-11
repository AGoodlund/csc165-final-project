package a2;
import tage.GameObject;
import tage.Camera;
import tage.Light;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import tage.input.action.AbstractInputAction;
import net.java.games.input.Event;

public class LorRTurnAction extends AbstractInputAction {    //move camera+avatar forward
    private GameObject obj = null;
    private Camera cam = null;
    private Light light;
    private int direction = 1;
    private boolean keyboard = false;
    private float keyValue;
    private ProtocolClient protClient = null;

    private Matrix4f m = new Matrix4f();
    private Vector3f v = new Vector3f();
//    private float[] vals = new float[16];
    
/** Constructor for camera and avatar movign in sync without keyboard */
    public LorRTurnAction(MyGame g, ProtocolClient p){ obj = g.getAvatar(); protClient = p; }

    public LorRTurnAction(MyGame g, int dir, ProtocolClient p){ obj = g.getAvatar(); direction = dir; keyboard = true; protClient = p; }

    public void addLight(Light l){ light = l; }
@Override
    public void performAction(float time, Event e){

        keyValue = e.getValue();
        if(keyValue > -spot.deadzoneBounds && keyValue < spot.deadzoneBounds) return; //deadzone
//        if(keyboard)
            keyValue *= direction;
        if(cam != null)
            cam.yaw(time * spot.turnSpeed * keyValue);
        if(obj != null)
            obj.yaw(time * spot.turnSpeed * keyValue);
            
        obj.getWorldForwardVector(v);
        v.add(0,.5f,0f);
        light.setDirection(v);

        if(protClient != null){
            obj.getLocalRotation(m);
            protClient.sendTurnMessage(m);
        }
    }    
}
