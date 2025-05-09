package a2;

import net.java.games.input.Event;
import tage.shapes.AnimatedShape;
import tage.input.action.AbstractInputAction;

/** action to be put on release of a button that starts an animation */
public class StopAnimatingAction extends AbstractInputAction{

    AnimatedShape shape;

    public StopAnimatingAction(AnimatedShape s){
        shape = s;
    }

@Override
    public void performAction(float time, Event e){
        shape.stopAnimation();
    }
    
}
