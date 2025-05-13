import java.util.*;
import tage.ai.behaviortrees.*;
import org.joml.*;
import java.util.Random;

import a2.spot;

public class NPCcontroller
{ 
	private Random rn = new Random();
	private BehaviorTree bt = new BehaviorTree(BTCompositeType.SELECTOR);
	private boolean nearFlag = false;
	private long thinkStartTime, tickStartTime;
	private long lastThinkUpdateTime, lastTickUpdateTime;
	private GameServerUDP server;
	private int spawnRange = 20;

	private Vector<NPC> bots = new Vector<NPC>();
	private Vector<Player> players = new Vector<Player>();

	private Player temp = new Player();
	private NPC tempBot = new NPC();
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
		Vector3f v2 = new Vector3f();

		if(players.isEmpty()) return new Vector3f(-1000,-1000,-1000);
		for(Player p : players){
			npc.getPosition(v);
			p.getPosition(v2); 
			next = v.distance(v2);

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
			npc.randomizeLocation(rn.nextInt(spawnRange),rn.nextInt(spawnRange));
		}
	}
	
	public void npcLoop()
	{ 
		long currentTime;
		float elapsedThinkMilliSecs, elapsedTickMilliSecs;
		while (true)
		{ 
			currentTime = System.nanoTime();
			elapsedThinkMilliSecs = (currentTime-lastThinkUpdateTime)/(1000000.0f);
			elapsedTickMilliSecs = (currentTime-lastTickUpdateTime)/(1000000.0f);

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

	public NPC getNPC(UUID id){ 
		Iterator<NPC> it = bots.iterator();
		while(it.hasNext())
		{	tempBot = it.next();
			if(tempBot.getID().compareTo(id) == 0)
				return tempBot;	
		}		
		return null;
	}

	private class Player{
		private UUID id;
		private Vector3f position;

		public Player(UUID id, Vector3f p){
			this.id = id;
			position = new Vector3f(p);
		}
		public Player(){ position = new Vector3f(); } //blank build for the temporary container

		public UUID getID(){ return id; }
		public Vector3f getPosition(Vector3f dest){ dest.set(position); return dest; }
		public void setPosition(Vector3f pos){ position.set(pos); }
	}
}
