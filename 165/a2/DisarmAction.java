package a2;

import a2.spot;
import java.util.ArrayList;
import tage.input.action.AbstractInputAction;
import net.java.games.input.Event;
import tage.GameObject;
import tage.NodeController;

import org.joml.*;
import a2.MyGame;
import tage.nodeControllers.*;

public class DisarmAction extends AbstractInputAction{
    private GameObject avatar, closest;
    private ArrayList<GameObject> disarmables = new ArrayList<GameObject>(); 
    private float distance;
    private NodeController node, node2;//roller, rotation;

    public DisarmAction(GameObject obj, ArrayList<GameObject> armed, NodeController action, NodeController action2){//node, NodeController rot){
        avatar = obj;
        node = action;
        node2 = action2;
//        roller = node;
//        rotation = rot;
        for(GameObject object: armed)
            disarmables.add(object);
        closest = disarmables.get(0);
        distance = avatar.getWorldLocation().distance(closest.getWorldLocation());
    }
    
@Override
    public void performAction(float time, Event e){
//Determine if closest object is close enough to be disarmed
        for(GameObject current : disarmables){
            if(avatar.getWorldLocation().distance(current.getWorldLocation()) < distance){
                distance = avatar.getWorldLocation().distance(current.getWorldLocation());
                closest = current;
            }
        }
//System.out.println("closest object is " + distance + " away");
        if(avatar.getWorldLocation().distance(closest.getWorldLocation()) < spot.close && !closest.destroyed && !closest.disarmed){
            closest.disarmed = true;
            MyGame.counter += 1;
//            action.addTarget(closest);
//            rotation.removeTarget(closest);
            node.addTarget(closest);
            disarmables.remove(closest);
        }

        if(closest.hasChildren() && closest.disarmed){//remove child from parent and add to avatar
            GameObject child = closest.getFirstChild();
            child.setParent(avatar);
            child.setLocalLocation(new Vector3f(0f,2f,0f));
            child.setLocalScale(new Matrix4f().scaling(.25f));
            node2.addTarget(child);
            child.propagateRotation(true);
//System.out.println("Item stolen");
        }
        if(!disarmables.isEmpty())
            closest = disarmables.get(0);
        distance = 1000f;
    }
}