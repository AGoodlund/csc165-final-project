import java.util.*;
import tage.ai.behaviortrees.*;
import tage.GhostNPC;
public class NPCcontroller
{ 
	private NPC npcA, npcB, npcC, npcD;
	Random rn = new Random();
	BehaviorTree bt = new BehaviorTree(BTCompositeType.SELECTOR);
	boolean nearFlag = false;
	long thinkStartTime, tickStartTime;
	long lastThinkUpdateTime, lastTickUpdateTime;
	GameServerUDP server;
	//double criteria = 2.0;
	
	public void updateNPCs()
	{ npcA.updateLocation();
	  npcB.updateLocation();
	  npcC.updateLocation();
	  npcD.updateLocation();}
	
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
		npcA = new NPC();
		//npcA.randomizeLocation(rn.nextInt(40),rn.nextInt(40));
		npcB = new NPC();
		//npcB.randomizeLocation(rn.nextInt(50), rn.nextInt(50));
		npcC = new NPC();
		//npcC.randomizeLocation(rn.nextInt(60),rn.nextInt(60));
		npcD = new NPC();
		//npcD.randomizeLocation(rn.nextInt(70), rn.nextInt(70));
		
		npcA.randomizeLocation(1,1);
		
		npcB.randomizeLocation(2,2);
		
		npcC.randomizeLocation(3,3);
		
		npcD.randomizeLocation(4,4);
	}
	
	public void npcLoop()
	{ 
		while (true)
		{ 
			long currentTime = System.nanoTime();
			float elapsedThinkMilliSecs =
			(currentTime-lastThinkUpdateTime)/(1000000.0f);
			float elapsedTickMilliSecs = (currentTime-lastTickUpdateTime)/(1000000.0f);
			if (elapsedTickMilliSecs >= 25.0f)
			{ 
				lastTickUpdateTime = currentTime;
				npcA.updateLocation(); 
				npcB.updateLocation();
				npcC.updateLocation();
				npcD.updateLocation();
				server.sendNPCinfo();
			}
			
			if (elapsedThinkMilliSecs >= 250.0f)
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
		bt.insert(10, new FollowPlayer(server, this, npcA));
		bt.insert(10, new FollowPlayer(server, this, npcB));
		bt.insert(10, new FollowPlayer(server, this, npcC));
		bt.insert(10, new FollowPlayer(server, this, npcD));
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
	
	public NPC getNPC (int id)
	{
		NPC npc;
		
		if (id == 0)
		{
			npc = npcA;
		}
		else if (id == 1)
		{
			npc = npcB;
		}
		else if (id == 2)
		{
			npc = npcC;
		}
		else
		{
			npc = npcD;
		}
		return npc;
	}
}
