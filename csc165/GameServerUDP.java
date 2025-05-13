import java.io.IOException;
import java.net.InetAddress;
import java.util.UUID;

import tage.networking.server.GameConnectionServer;
import tage.networking.server.IClientInfo;
import tage.networking.Message;
import org.joml.*;

import a2.spot;

public class GameServerUDP extends GameConnectionServer<UUID> 
{
	private Message message = Message.getMessage();
	private UUID clientID, ghostID;
	public UUID ID;
	private Message.MessageType t;
	private NPCcontroller npcCtrl;

	private Vector3f v = new Vector3f(); //helper objects for Message
	private Matrix4f m = new Matrix4f();

	public GameServerUDP(int localPort, NPCcontroller npc) throws IOException 
	{	super(localPort, ProtocolType.UDP);
		npcCtrl = npc; 
		this.ID = UUID.randomUUID();
		npcCtrl.setupNPCs();
		sendCreateNPCmsg(this.ID, npcCtrl.getNPC(0));
		sendCreateNPCmsg(this.ID, npcCtrl.getNPC(1));
		sendCreateNPCmsg(this.ID, npcCtrl.getNPC(2));
		sendCreateNPCmsg(this.ID, npcCtrl.getNPC(3));
		sendCreateAll();
	}
		
	// --- additional protocol for NPCs ----
	public void handleNearTiming(UUID clientID) { npcCtrl.setNearFlag(true);}

	// ------------ SENDING NPC MESSAGES -----------------
	// Informs clients of the whereabouts of the NPCs.
	public void sendCreateNPCmsg(UUID clientID, NPC npc) //TODO: call this in JOIN case. send the position of every NPC to the new user
	{ 
		System.out.println("The server is telling clients about an NPC..."); //May need to delete this
		
		try 
		{	message.addItem(Message.MessageType.CREATE_NPC);	
			npc.getPosition(v);
			message.addItem(v);
			forwardPacketToAll(message, clientID);	//TODO: change this to sending to a specific receiver
		} catch (IOException e) { e.printStackTrace(); }
	}
	
	public void sendNPCinfo()
	{ 
		for(int i = 0; i < 4; i++){
			message.addItem(npcCtrl.getNPC(i).getPosition(v));
			message.addItem(npcCtrl.getNPC(i).getRotation(m));
			message.addItem(ID);
			sendToAll();
		}
		//message.addItem(npcCtrl.getNPC().getOrientation());
		//sendToAll();
	}
/* 
	public void sendCheckForAvatarNear() //throws IOException
	{ 
		npcCtrl.getNPC(0).getPosition(v);
		message.addItem(v);
		sendToAll(); 
	}	
	public void sendNPCstart(UUID clientID) 
	{
//		message.addItem();
//		sendPacketToAll(message);
	}
*/
	private void sendToAll(){
		try{	
			sendPacketToAll(message);
		}
		catch(IOException e){
			System.out.println("couldn't send msg"); e.printStackTrace();
		}
	}
	@Override
	public void processPacket(Object o, InetAddress senderIP, int senderPort)
	{
		message.copy((Message)o);
		t = message.type;
					//RemoteID is the sender (protocolClient), ID is receiver (here) 
		switch(t){
			case JOIN:
				try{	
					IClientInfo ci;					
					ci = getServerSocket().createClientInfo(senderIP, senderPort);
					clientID = message.getSenderID();//UUID.fromString(messageTokens[1]);
					addClient(ci, clientID);
					System.out.println("Join request received from - " + clientID.toString());
					sendJoinedMessage(clientID, true);
//TODO: call npcCtrl.addPlayer(clientID, clientPosition); This means adding position to the join message in ProtocolClient.java					
//TODO: send createNPC requests with all of the current NPC positions
				} 
				catch (IOException e) { e.printStackTrace(); }
				break;
			case BYE:
				clientID = message.getSenderID();
				System.out.println("Exit request received from - " + clientID.toString());
				sendByeMessages(clientID);
				removeClient(clientID);
				break;
			case CREATE:
				clientID = message.getSenderID();
				sendCreateMessages(clientID);//v);
				sendWantsDetailsMessages(clientID);
				break;
			case DSFR:
				ghostID = message.getReceiverID();
				sendDetailsForMessage(ghostID);//v);
				break;
			case TURN:
				ghostID = message.getSenderID();
				sendTurnMessages(ghostID);//m);
				break;
			case MOVE:
				ghostID = message.getSenderID();
//System.out.println("message in case MOVE in GameServer\n" + message.toString());
//TODO: send along to npcCtrl.updatePlayerLocation(ghostID, message.getVector(v));
				sendMoveMessages(ghostID);//vf);
				break;
			case WSDS:
				System.out.println("WSDS was sent to 165/GameServerUDP.java for some reason");
				break;
				
			case CHANGE_NPC:
//System.out.println("CHANGE_NPC command being forwarded" + message.toString());
				ghostID = message.getSenderID();
				sendChangeMessage(ghostID);
				break;
			case CREATE_NPC:
				System.out.println("CREATE_NPC");
				ID = message.getSenderID();
				sendCreateMessages(ID);
				sendWantsDetailsMessages(ID);
				//addClient(ci, clientID); We'll need to add this to whichever is first
				break;
		
			case MNPC://move npc
//TODO: put npc position and rotation into a message and sendToAll();
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
//				sendNPCstart(clientID);
				break;
				
			case DEFAULT:

				System.out.println("received blank message");
				break;
			default:

				System.out.println("an unknown MessageType was sent to GameServerUDP.java");
		}
	}
	
	// Informs the client who just requested to join the server if their 
	// request was able to be granted. 
	
	public void sendJoinedMessage(UUID clientID, boolean success)
	{	try 
		{	System.out.println("trying to confirm join");
			message.setSuccess(success);

			sendPacket(message, clientID);
//			message.clear();
		} 
		catch (IOException e) 
		{	e.printStackTrace();
	}	}
	
	// Informs a client that the avatar with the identifier remoteId has left the server. 
	// This message is meant to be sent to all client currently connected to the server 
	// when a client leaves the server.
	
	public void sendByeMessages(UUID clientID)
	{	try 
		{	
			forwardPacketToAll(message, clientID);
			message.clear();
		} 
		catch (IOException e) 
		{	e.printStackTrace();
	}	}
	
	// Informs a client that a new avatar has joined the server with the unique identifier 
	// remoteId. This message is intended to be send to all clients currently connected to 
	// the server when a new client has joined the server and sent a create message to the 
	// server. This message also triggers WANTS_DETAILS messages to be sent to all client 
	// connected to the server. 

	public void sendCreateMessages(UUID clientID)//Vector3f position)//String[] position)
	{	try 
		{	
//			message.addItem(Message.MessageType.CREATE);
			forwardPacketToAll(message, clientID);
//			message.clear();
		} 
		catch (IOException e) 
		{	e.printStackTrace();
	}	}
	
	// Informs a client of the details for a remote client�s avatar. This message is in response 
	// to the server receiving a DETAILS_FOR message from a remote client. That remote client�s 
	// message�s localId becomes the remoteId for this message, and the remote client�s message�s 
	// remoteId is used to send this message to the proper client. 

	public void sendDetailsForMessage(UUID remoteID)//Vector3f position)//String[] position)
	{	try 
		{	
			sendPacket(message, remoteID);
		} 
		catch (IOException e) 
		{	e.printStackTrace();
	}	}
	
	// Informs a local client that a remote client wants the local client�s avatar�s information. 
	// This message is meant to be sent to all clients connected to the server when a new client 
	// joins the server. 
	
	public void sendWantsDetailsMessages(UUID clientID)
	{	try 
		{	message.addItem(Message.MessageType.WSDS);	
			forwardPacketToAll(message, clientID);
		} 
		catch (IOException e) 
		{	e.printStackTrace();
		}	
	}
	
	// Informs a client that a remote client�s avatar has changed position. x, y, and z represent 
	// the new position of the remote avatar. This message is meant to be forwarded to all clients
	// connected to the server when it receives a MOVE message from the remote client.   

	public void sendMoveMessages(UUID clientID)
	{	try 
		{	
			forwardPacketToAll(message, clientID);
		} 
		catch (IOException e) 
		{	e.printStackTrace();
	}	}
	
	public void sendTurnMessages(UUID clientID)
	{	try 
		{	
			forwardPacketToAll(message, clientID);
		} 
		catch (IOException e) 
		{	e.printStackTrace();
	}	}
	
	public void sendCreateAll ()
	{
		message.addItem(Message.MessageType.CREATE_NPC);
		message.addItem(ID);
		//Work out how to send more specifics
		
		try{
		sendPacketToAll(message);
		}
		catch (IOException p)
		{	p.printStackTrace();
	}	}

	public void sendChangeMessage(UUID clientID){
		try{
			forwardPacketToAll(message, clientID);
		}
		catch (IOException e){
			e.printStackTrace();
		}
	}

public void trace(){
	System.out.println("GAME_SERVER_UDP" + message.toString());
}
}
