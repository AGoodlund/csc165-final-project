package a2;
import tage.GameObject;
import tage.Camera;
import tage.Light;

import tage.input.action.AbstractInputAction;
import net.java.games.input.Event;
import org.joml.*;

public class LorRStrafeAction extends AbstractInputAction {
    private MyGame game;
    private GameObject obj;
    private Camera cam;
    private ProtocolClient protClient;
    private Light light;

    private int direction = 1;
    private boolean keyboard = false;
    private float keyValue;

    private Vector3f strafeDir = new Vector3f(), v = new Vector3f();
    private Matrix4f loc = new Matrix4f();
    private float[] vals = new float[16];
    
/** Constructor for camera and avatar with controller */
    public LorRStrafeAction(MyGame g, Camera c, ProtocolClient p){ cam = c; obj = g.getAvatar(); protClient = p; }
/** Constructor for camera and avatar with keyboard */
    public LorRStrafeAction(MyGame g, Camera c, int dir, ProtocolClient p){ game = g; obj = g.getAvatar(); cam = c; direction = dir; keyboard = true; protClient = p; }// objS = anim; }

    public void addLight(Light l){ light = l; }
@Override
    public void performAction(float time, Event e){

        keyValue = e.getValue();
        if(keyValue > -0.2f && keyValue < 0.2f) return; //deadzone

        if(!game.isAnimating && !game.hasLooped){
            game.startAnimation();
        }
        game.isAnimating = true;
        game.hasLooped = true;

        keyValue *= direction;
//TODO: perhaps limit camera to being at 0 and only moves far enough so that the ground is always in view. Hallway method
        if(obj != null){
            obj.getLocalLocation(v);
            strafeDir.set(keyValue,0f,0f);
            strafeDir.mul(time*spot.runSpeed);
            v.add(strafeDir);
            obj.setLocalLocation(v);

            if(light != null){
                v.add(0,2f,0);
                light.setLocation(v);
            }
            obj.getWorldTranslation(loc);
            obj.getPhysicsObject().setTransform(obj.toDoubleArray(loc.get(vals))); 
        }

        if(cam != null){
            cam.getLocation(v);
            v.add(strafeDir);
            cam.setLocation(v);
//            cam.setLocation(v);
//            cam.heightAdjust(spot.cameraOffset);
        }

        if(protClient != null){
			obj.getWorldLocation(v); protClient.sendMoveMessage(v);
        }//obj.getWorldLocation());

    }
}
