import tage.ai.behaviortrees.*;
import org.joml.*;
import tage.GhostNPC;
public class FollowPlayer extends BTAction
{ //TODO: Follow the player if in range

	Vector3f playerPosition = new Vector3f(0,0,0); //TODO: Implement code to fetch the player position.
	
	Vector3f npcPosition;
	NPC npc;
	NPCcontroller npcCtrl;
	GameServerUDP server;
	float distance; //This just returns a float for now
	
	
	public FollowPlayer(GameServerUDP s, NPCcontroller c, NPC n)
	{ 
		super();
		server = s; npcCtrl = c; npc = n; npcPosition = n.getPosition();
	}
	
	//TODO: Keep adding to this
	
	
	protected BTStatus update(float elapsedTime)
	{
		calculateDistance();
		return BTStatus.BH_SUCCESS;
	}
	
	
	//TODO: This may be best served in its own node.
	private void calculateDistance ()
	{
		distance = npcPosition.distance(playerPosition);
		
		if (distance < 5.0f)
		{
			System.out.println("Player is near, woop woop");
		}
	}
}
