package a2;

import java.util.ArrayList;

import net.java.games.input.Event;
import tage.GameObject;
import tage.input.action.AbstractInputAction;

public class HideObjectAction extends AbstractInputAction {
   private ArrayList<GameObject> obj = new ArrayList<GameObject>();

   public HideObjectAction(ArrayList<GameObject> o) { 
      for(GameObject object:o)
         obj.add(object);
   }

@Override
   public void performAction(float time, Event e) { 
      for (GameObject obj:obj){
         if(obj.getRenderStates().renderingEnabled())
            obj.getRenderStates().disableRendering();
         else
            obj.getRenderStates().enableRendering(); 
      }
   }
}
