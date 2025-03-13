package a2;

import tage.input.action.AbstractInputAction;
import net.java.games.input.Event;

public class CountAction extends AbstractInputAction{
    private MyGame game;
    public CountAction(MyGame g){ game = g; }
    @Override
    public void performAction(float time, Event e){ game.counter++; }
}
