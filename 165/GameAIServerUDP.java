import java.io.IOException;
import java.net.InetAddress;
import java.util.UUID;

import tage.networking.server.GameConnectionServer;
import tage.networking.server.IClientInfo;
import tage.networking.Message;
import org.joml.*;

public class GameAIServerUDP extends GameConnectionServer<UUID> 
{ 
	private NPCcontroller npcCtrl;
	private Message message = Message.getMessage();
	private UUID clientID, ghostID, remoteID;
	private Message.MessageType t;
	
	public GameAIServerUDP(int localPort, NPCcontroller npc) throws IOException 
	{ 
		super(localPort, ProtocolType.UDP);
		npcCtrl = npc; 
	}
	
	// --- additional protocol for NPCs ----
	
	
	public void handleNearTiming(UUID clientID)
	{ npcCtrl.setNearFlag(true);}

	// ------------ SENDING NPC MESSAGES -----------------
	// Informs clients of the whereabouts of the NPCs.
	public void sendCreateNPCmsg(UUID clientID, String[] position)
	{ 
		System.out.println("The server is telling clients about an NPC...");
		
		try 
		{	message.addItem(Message.MessageType.CREATE_NPC);	
			forwardPacketToAll(message, clientID);
		} catch (IOException e) { e.printStackTrace(); }
	}
	
	public void sendCheckForAvatarNear() throws IOException
	{ try
		{ 
			//TODO: Change for our protocol
			
			String message = new String("isnr");
			/*message += "," + (npcCtrl.getNPC()).getX();
			message += "," + (npcCtrl.getNPC()).getY();
			message += "," + (npcCtrl.getNPC()).getZ();
			message += "," + (npcCtrl.getCriteria());*/
			sendPacketToAll(message);
		}
		
	  catch (IOException e)
	  { System.out.println("couldnt send msg"); e.printStackTrace(); }
	}	
	
	public void sendNPCinfo()
	{
		System.out.println("If this is still here, it's because I forgot to finish it. - SL"); //TODO: Create SendNPCInfo protocol
	}
//public void sendNPCstart(UUID clientID)
//{ }

@Override
	public void processPacket(Object o, InetAddress senderIP, int Senderport)
	{ 
		message.copy((Message)o);
		t = message.type;
		switch(t)
		{
			case CREATE_NPC:
			System.out.println("CREATE_NPC");
			break;
		
			case MNPC:
			System.out.println("MNPC");
			break;
		
			case IS_NEAR:
			System.out.println("NEAR");
				//UUID clientID = UUID.fromString(messageTokens[1])
				//handleNearTiming(clientID);
			break;
			
			case NPC_REQUEST:
				System.out.println("AI server got a needNPC message");
				//UUID clientID = UUID.fromString(messageTokens[1]); //TODO Change this
				//sendNPCstart(clientID);
			break;
		}
	} 
	public void trace(){
	System.out.println("AI GAME_SERVER_UDP" + message.toString());
	}
}