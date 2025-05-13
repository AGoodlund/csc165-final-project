import tage.ai.behaviortrees.*;
import org.joml.*;
import tage.GhostNPC;
import a2.spot;

public class FollowPlayer extends BTAction{ //TODO: Follow the player if in range

	Vector3f playerPosition = new Vector3f(0,0,0);
	
	Vector3f npcPosition;
	NPC npc;
	NPCcontroller npcCtrl;
	GameServerUDP server;
	float distance; //This just returns a float for now
	
	
	public FollowPlayer(GameServerUDP s, NPCcontroller c, NPC n)
	{ 
		super();
		server = s; npcCtrl = c; npc = n; n.getPosition(npcPosition);
	}
	
	//TODO: Keep adding to this
	
	
	protected BTStatus update(float elapsedTime) //THINK
	{
		playerPosition.set(npcCtrl.closestPlayer(npc));	//returns the position of the closest player
		calculateDistance(); //find closest player in range

		//if distance flag is ticked then look at the player and move toward them
		return BTStatus.BH_SUCCESS;
	}
	
	
	//TODO: This may be best served in its own node.
	private void calculateDistance ()
	{
		//	playerPosition = server.sendWantsDetailsMessages(server.ID);
		distance = npcPosition.distance(playerPosition);
		
		if (distance < spot.aggroRange)	//if closest player is in aggro range move toward it
		{
			npc.setHunt(true);
			npc.lookAt(playerPosition);
			//System.out.println("Player is near, woop woop");
		}
		else npc.setHunt(false);
	}
}
