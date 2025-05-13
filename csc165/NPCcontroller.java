import java.util.*;
import tage.ai.behaviortrees.*;
import org.joml.*;

import a2.spot;

import java.util.Random;

public class NPCcontroller
{ 
	private Random rn = new Random();
	private BehaviorTree bt = new BehaviorTree(BTCompositeType.SELECTOR);
	private boolean nearFlag = false;
	private long thinkStartTime, tickStartTime;
	private long lastThinkUpdateTime, lastTickUpdateTime;
	private GameServerUDP server;

	private Vector<NPC> bots = new Vector<NPC>();
	private Vector<Player> players = new Vector<Player>();
	private Player temp = new Player();
	private Vector3f v = new Vector3f();
	
	public void updateNPCs(float time)
	{ 
		for(NPC npc: bots){
			npc.updateLocation(time);
		}
	}
/** update the position of the supplied player */
	public void updatePlayerLocation(UUID player, Vector3f location){
		temp = findPlayer(player);
		if(temp == null) return;
		temp.setPosition(location);
	}
/** add a new player to NPC's perception. Does nothing if UUID is already in the list */
	public void addPlayer(UUID player, Vector3f location){
		temp = findPlayer(player);
		if(temp == null)
			players.add(new Player(player, location));
	}
/** removes target player from NPC's perception */
	public void removePlayer(UUID player){
		temp = findPlayer(player);
		if(temp == null) return;
		players.remove(temp);
	}
	private Player findPlayer(UUID player){ 
		Iterator<Player> p = players.iterator();
		while(p.hasNext()){
			temp = p.next();
			if(temp.getID().compareTo(player) == 0)
				return temp;
		}
		return null; 
	}

/** the supplied NPC returns the vector position of its closest known player */
	public Vector3f closestPlayer(NPC npc){
		float dist = 1000f, next;

		for(Player p : players){
			next = npc.getPosition(v).distance(p.getPosition(v));
			if(dist > next){
				dist = next;
				temp = p;
			}
		}

		return temp.getPosition(v); //The position of the closest player
	}
	
	public void start(GameServerUDP s)
	{ 
		thinkStartTime = System.nanoTime();
		tickStartTime = System.nanoTime();
		lastThinkUpdateTime = thinkStartTime;
		lastTickUpdateTime = tickStartTime;
		server = s;
		setupNPCs();
		setupBehaviorTree();
		npcLoop();
	}

	public void setupNPCs()
	{ 
		bots.add(new NPC());
		bots.add(new NPC());
		bots.add(new NPC());
		bots.add(new NPC());

		for(NPC npc: bots){
			npc.randomizeLocation(rn.nextInt(40),rn.nextInt(40));
		}
	}
	
	public void npcLoop()
	{ 
		while (true)
		{ 
			long currentTime = System.nanoTime();
			float elapsedThinkMilliSecs =
			(currentTime-lastThinkUpdateTime)/(1000000.0f);
			float elapsedTickMilliSecs = (currentTime-lastTickUpdateTime)/(1000000.0f);

			if (elapsedTickMilliSecs >= spot.tickSpeed)		//TICK (move forward)
			{ 
				lastTickUpdateTime = currentTime;
				updateNPCs((float)lastTickUpdateTime);
				server.sendNPCinfo();
			}
			
			if (elapsedThinkMilliSecs >= spot.thinkSpeed)	//THINK (face nearest player)
			{ 
				lastThinkUpdateTime = currentTime;
				bt.update(elapsedThinkMilliSecs);
			}
			Thread.yield();
		} 
	}
	
	public void setupBehaviorTree()
	{ 
		bt.insertAtRoot(new BTSequence(10));
		//bt.insert(10, new AvatarNear(server,this,npc,false));

		for(NPC npc: bots){ bt.insert(10, new FollowPlayer(server, this, npc)); }
		//TODO Add a player to follow and figure out a way to do that
		
		//bt.insertAtRoot(new BTSequence(20));
		//bt.insert(10, new GetSmall(npc));
		//bt.insert(20, new AvatarNear(server,this,npc,false));
		//bt.insert(20, new GetBig(npc));
		//TODO: removed this behavior for now to add new behavior. Reinstate to test the code (otherwise this doesn't work).
	} 
	
	public void setNearFlag (boolean flag)
	{
		nearFlag = flag;
	}
	
	public boolean getNearFlag ()
	{
		return nearFlag;
	}
	
	public NPC getNPC (int id) { return bots.get(id); }

	private class Player{
		private UUID id;
		private Vector3f position;

		public Player(UUID id, Vector3f p){
			this.id = id;
			position = new Vector3f(p);
		}
		public Player(){} //blank build for the temporary container

		public UUID getID(){ return id; }
		public Vector3f getPosition(Vector3f dest){ dest.set(position); return dest; }
		public void setPosition(Vector3f pos){ position.set(pos); }
	}
}
