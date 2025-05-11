package a2;

import tage.Light;
import net.java.games.input.Event;
import tage.input.action.AbstractInputAction;

public class ToggleFlashLightAction extends AbstractInputAction{
    private Light lamp;
    
    public ToggleFlashLightAction(Light l){ lamp = l; }

@Override
    public void performAction(float time, Event e){
        lamp.toggleOnOff();
    }
}
