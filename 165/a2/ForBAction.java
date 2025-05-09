package a2;
import tage.GameObject;
import tage.Camera;
import tage.Light;

import tage.input.action.AbstractInputAction;
import net.java.games.input.Event;
import org.joml.*;

public class ForBAction extends AbstractInputAction {    //move camera+avatar forward
    private GameObject obj;
    private MyGame game;
    private Camera cam;
    private Light light;
    private int direction = 1;
    private boolean keyboard = false;
    private float keyValue;
    private ProtocolClient protClient;

    private Vector3f fwdDir = new Vector3f(), v = new Vector3f();
    private Matrix4f loc = new Matrix4f();

    private float[] vals = new float[16];
    
/** Constructor for camera and avatar with controller */
    public ForBAction(MyGame g, Camera c, ProtocolClient p){ cam = c; obj = g.getAvatar(); protClient = p; }
/** Constructor for camera and avatar with keyboard */
    public ForBAction(MyGame g, Camera c, int dir, ProtocolClient p){ game = g; obj = g.getAvatar(); cam = c; direction = dir; keyboard = true; protClient = p; }// objS = anim; }
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

//System.out.println("isAnimating in moveAction = " + game.isAnimating);

//        if(keyboard)  if controller has wacky movement this being commented out is why
            keyValue *= direction;

        if(obj != null){
            obj.getLocalLocation(v);
            fwdDir.set(0f,0f,keyValue);
            fwdDir.mul(time*spot.runSpeed);//*keyValue);
            v.add(fwdDir);
            obj.setLocalLocation(v);

            if(light != null){
                v.add(0,2f,0);
                light.setLocation(v);
            }

//physics object moving alongside object
            obj.getWorldTranslation(loc);
            obj.getPhysicsObject().setTransform(obj.toDoubleArray(loc.get(vals))); 
        }

        if(cam != null){
            cam.getLocation(v);
            v.add(fwdDir);
            cam.setLocation(v);
//            cam.setLocation(v);
//            cam.heightAdjust(spot.cameraOffset);
        }
        if(protClient != null)
		{   obj.getWorldLocation(v);
			protClient.sendMoveMessage(v);
//System.out.println("ForBAction moved to " + obj.getWorldLocation());
		}
    }    
}