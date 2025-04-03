package a2;

import tage.input.action.AbstractInputAction;
import net.java.games.input.Event;

public class PauseAction extends AbstractInputAction{
    private MyGame game;
    public PauseAction(MyGame g){ game = g; }
    @Override
    public void performAction(float time, Event e){ }//game.paused = !game.paused; } 
}
