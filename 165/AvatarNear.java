/*import tage.ai.behaviortrees.BTCondition;
public class AvatarNear extends BTCondition
{ 
	NPC npc;
	NPCcontroller npcc;
	GameAIServerUDP server;
	public AvatarNear(GameAIServerUDP s, NPCcontroller c, NPC n,
	boolean toNegate)
	{ 
		super(toNegate);
		server = s; npcc = c; npc = n;
	}
	
	protected boolean check()
	{ 
		server.sendCheckForAvatarNear();
		return npcc.getNearFlag();
	} 
}*/
