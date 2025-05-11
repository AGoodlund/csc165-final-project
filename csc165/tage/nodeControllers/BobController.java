package tage.nodeControllers;
import tage.*;
import java.lang.Math;
import org.joml.*;

/**
* bounces a GameObject up and down
* @author Aaron Goodlund
*/
public class BobController extends NodeController{
    private float speed = 0.01f;
    private float amplitude = 1f;
    private float angle = 0f;

    public BobController(){ super(); }
    public BobController(float speed, float height){
        super();
        this.speed = speed;
        amplitude = height;
    }

    public void setSpeed(float s){ speed = s; }
    public void setHeight(float h){ amplitude = h; }

    public void apply(GameObject g){
        g.heightAdjust((float)Math.cos(Math.toRadians((double)angle))*amplitude);
        angle += speed;
        angle = angle % speed;
    }
}