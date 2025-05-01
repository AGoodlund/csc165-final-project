import java.util.*;
import tage.ai.behaviortrees.*;
import tage.GhostNPC;
public class NPCcontroller
{ 
	private GhostNPC npc;
	Random rn = new Random();
	BehaviorTree bt = new BehaviorTree(BTCompositeType.SELECTOR);
	boolean nearFlag = false;
	long thinkStartTime, tickStartTime;
	long lastThinkUpdateTime, lastTickUpdateTime;
	GameServerUDP server;
	double criteria = 2.0;
	
	public void updateNPCs()
	{ npc.updateLocation();}
	
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
		npc = new GhostNPC();
		npc.randomizeLocation(rn.nextInt(40),rn.nextInt(40));
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
				npc.updateLocation();
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
		bt.insertAtRoot(new BTSequence(20));
		//bt.insert(10, new OneSecPassed(this,npc,false));
		//bt.insert(10, new GetSmall(npc));
		//bt.insert(20, new AvatarNear(server,this,npc,false));
		//bt.insert(20, new GetBig(npc));
		
		//TODO: removed this behavior for now to add new behavior. Reinstate to test the code (otherwise this doesn't work).
		//TODO: Make new class files to dictate npc behavior
		//SEE ghostNPC for npc behavior as well
	} 
	
	public void setNearFlag (boolean flag)
	{
		nearFlag = flag;
	}
	
	public boolean getNearFlag ()
	{
		return nearFlag;
	}
	
	public GhostNPC getNPC ()
	{
		return npc;
	}
}
