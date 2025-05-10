package a2;
import tage.Camera;
import tage.GameObject;

import tage.input.action.AbstractInputAction;
import net.java.games.input.Event;

public class MountAction extends AbstractInputAction {
    private MyGame game;
    private GameObject obj;
    private Camera cam;

    public MountAction(MyGame g, Camera c){ game = g; cam = c; obj = g.getAvatar(); }
@Override
    public void performAction(float time, Event e){ 
//        if((float)cam.getLocation().distance(obj.getWorldLocation()) < spot.mountingDistance){
 /*            game.mounted = !game.mounted;
            if(game.mounted)
                game.jumpToDol();
            else
                game.dismountDol();
        }
*/    }
}
