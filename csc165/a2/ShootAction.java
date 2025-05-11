package a2;

import net.java.games.input.Event;
import tage.GameObject;
import tage.input.action.AbstractInputAction;

import org.joml.*;

import java.util.ArrayList;

public class ShootAction extends AbstractInputAction{
    private ArrayList<GameObject> ammo = new ArrayList<GameObject>();
    private GameObject host, obj;
    private ProtocolClient protClient;

    private Vector3f v = new Vector3f();
    private Matrix4f m = new Matrix4f();
    private float[] vals = new float[16];
    private float[] stop = {0f,0f,0f};

    private double prevShot, currTime;
    private int shootSpeed = 500, head = 0, tail;
    private int bulletSpeed = 3500;

    public ShootAction(ArrayList<GameObject> bullets, GameObject character, ProtocolClient p){
        protClient = p;
        prevShot = System.currentTimeMillis();
        host = character;

        for(GameObject object: bullets)
            ammo.add(object);

        tail = ammo.size();
    }
/** set the how many milliseconds must occur between shots */
    public void setShootSpeed(int speedInMillis){ shootSpeed = speedInMillis; }
/** set the speed of the bullets themselves */
    public void setBulletSpeed(float speed){ bulletSpeed = (int)(speed*1000); }

@Override
    public void performAction(float time, Event e){
        currTime = System.currentTimeMillis();
        if(currTime - prevShot < shootSpeed) return; //only shoot once per <shootSpeed> milliseconds

        prevShot = currTime;

        obj = ammo.get(head);
//        ammo.get(head).getRenderStates().enableRendering();

        host.getLocalLocation(v);
        obj.setLocalLocation(v);

        host.getWorldRotation(m);
        host.getWorldForwardVector(v);
        v.negate();
        m.m10(v.x()); m.m11(v.y());m.m12(v.z());
        obj.setLocalRotation(m);
//System.out.println("current direction:\n" + m);

        obj.getLocalForwardVector(v);
        v.mul(-bulletSpeed);
        obj.getPhysicsObject().setLinearVelocity(stop);
        obj.getPhysicsObject().applyForce(v.x(), 0f, v.z(), 0f, 0f, 0f);

        obj.getLocalForwardVector(v);
        host.getLocalTranslation(m);

        v.mul(-3f);
        m.translate(v.x(), 0, v.z());
        obj.getPhysicsObject().setTransform(obj.toDoubleArray(m.get(vals)));
//        obj.getRenderStates().enableRendering();
//move object to current location, point it along character's forward face, enable rendering, move physics into position, shove, increment head

        head++;
        if(head == tail)
            head = 0;
        //load next object

        if(protClient != null){
            obj.getLocalForwardVector(v);
//            protClient.sendShootMessage(bulletSpeed, v); 
                    //send the bullet's speed and forward direction to server to be sent to clients. starting location and ObjShape/Texture will be sent earlier
        }
    }
    
}