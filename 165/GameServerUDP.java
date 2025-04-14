import java.io.IOException;
import java.net.InetAddress;
import java.util.UUID;

import tage.networking.server.GameConnectionServer;
import tage.networking.server.IClientInfo;
import tage.networking.Message;
import org.joml.*;

public class GameServerUDP extends GameConnectionServer<UUID> 
{
	private Message message = Message.getMessage();
	private UUID clientID, ghostID, remoteID;
	private Message.MessageType t;

//	private Vector3f v = new Vector3f(); //helper objects for Message
//	private Matrix4f m = new Matrix4f();

	public GameServerUDP(int localPort) throws IOException 
	{	super(localPort, ProtocolType.UDP);
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
					clientID = message.getRemoteID();//UUID.fromString(messageTokens[1]);
					addClient(ci, clientID);
					System.out.println("Join request received from - " + clientID.toString());
					sendJoinedMessage(clientID, true);
				} 
				catch (IOException e) { e.printStackTrace(); }
				break;
			case BYE:
				clientID = message.getRemoteID();
				System.out.println("Exit request received from - " + clientID.toString());
				sendByeMessages(clientID);
				removeClient(clientID);
				break;
			case CREATE:
				clientID = message.getRemoteID();
//				String[] pos = {messageTokens[2], messageTokens[3], messageTokens[4]};
				sendCreateMessages(clientID);//v);
				sendWantsDetailsMessages(clientID);
				break;
			case DSFR:
				remoteID = message.getRemoteID();
//				String[] pos = {messageTokens[3], messageTokens[4], messageTokens[5]};
				sendDetailsForMessage(remoteID);//v);
				break;
			case TURN:
				ghostID = message.getRemoteID();
				sendTurnMessages(ghostID);//m);
				break;
			case MOVE:
				ghostID = message.getRemoteID();
//System.out.println("message in case MOVE in GameServer\n" + message.toString());
				sendMoveMessages(ghostID);//vf);
				break;
			case WSDS:
				System.out.println("WSDS was sent to 165/GameServerUDP.java for some reason");
				break;
			case DEFAULT:
				System.out.println("received blank message");
				break;
			default:
				System.out.println("an unknown MessageType was sent to GameServerUDP.java");
		}
	}
//		if(messageTokens.length > 0)
//		{	// JOIN -- Case where client just joined the server
			// Received Message Format: (join,localId)
//			if(messageTokens[0].compareTo("join") == 0)
		
/*			if(message.type == Message.MessageType.JOIN)
			{	try 
				{	IClientInfo ci;					
					ci = getServerSocket().createClientInfo(senderIP, senderPort);
					UUID clientID = message.getID();//UUID.fromString(messageTokens[1]);
					addClient(ci, clientID);
					System.out.println("Join request received from - " + clientID.toString());
					sendJoinedMessage(clientID, true);
				} 
				catch (IOException e) 
				{	e.printStackTrace();
			}	}
			
 			// BYE -- Case where clients leaves the server
			// Received Message Format: (bye,localId)
			if(messageTokens[0].compareTo("bye") == 0)
			{	UUID clientID = UUID.fromString(messageTokens[1]);
				System.out.println("Exit request received from - " + clientID.toString());
				sendByeMessages(clientID);
				removeClient(clientID);
			}
			
			// CREATE -- Case where server receives a create message (to specify avatar location)
			// Received Message Format: (create,localId,x,y,z) 
			if(messageTokens[0].compareTo("create") == 0)
			{	UUID clientID = UUID.fromString(messageTokens[1]);
				String[] pos = {messageTokens[2], messageTokens[3], messageTokens[4]};
				sendCreateMessages(clientID, pos);
				sendWantsDetailsMessages(clientID);
			}
			
			// DETAILS-FOR --- Case where server receives a details for message
			// Received Message Format: (dsfr,remoteId,localId,x,y,z)
			if(messageTokens[0].compareTo("dsfr") == 0)
			{	UUID clientID = UUID.fromString(messageTokens[1]);
				UUID remoteID = UUID.fromString(messageTokens[2]);
				String[] pos = {messageTokens[3], messageTokens[4], messageTokens[5]};
				sendDetailsForMessage(clientID, remoteID, pos);
			}
			// TURN --- Case where server receives a turn message
			// Received Message Format: (turn,localId,x,y,z)
			if(messageTokens[0].compareTo("turn") == 0)
			{	//UUID clientID = UUID.fromString(messageTokens[1]);
				
					//System.out.println(message);
					
					//Format: (turn,remoteID,worldRotation)	note: no up vector because it's 0,1,0 
				UUID ghostID = UUID.fromString(messageTokens[1]);
//				System.out.println("Attempting to turn");


				//String [] Small = {messageTokens[2], messageTokens[3]};
				String [] orientationStrOut = {messageTokens[2], messageTokens[3],messageTokens[4],messageTokens[5],
					messageTokens[6],messageTokens[7],messageTokens[8],messageTokens[9], messageTokens[10],messageTokens[11],
					messageTokens[12],messageTokens[13], messageTokens[14], messageTokens[15], messageTokens[16],messageTokens[17]};
				
				//String orientationStr = orientation.toString();
				//System.out.println(orientationStr);
				sendTurnMessages(ghostID, orientationStrOut);
				
				//sendMoveMessages(clientID, orientation);
				//ghostManager.turnGhostAvatar(ghostID, orientation);
				
				//String [] ori = {messageTokens[2], messageTokens[3], messageTokens[4], messageTokens[5], messageTokens[6], messageTokens[7], messageTokens[8], messageTokens[9], messageTokens[10], messageTokens[11], messageTokens[12], messageTokens[13], messageTokens[14], messageTokens[15], messageTokens[16], messageTokens[17], messageTokens[18]};
				//sendTurnMessages(clientID, ori);
			}
			
			// MOVE --- Case where server receives a move message
			// Received Message Format: (move,localId,x,y,z)
			if(messageTokens[0].compareTo("move") == 0)
			{	UUID clientID = UUID.fromString(messageTokens[1]);
				String[] pos = {messageTokens[2], messageTokens[3], messageTokens[4]};
				sendMoveMessages(clientID, pos);
	}	}	}*/
	
//TODO: this is where messages are created
	// Informs the client who just requested to join the server if their 
	// request was able to be granted. 
	// Message Format: (join,success) or (join,failure)
	
	public void sendJoinedMessage(UUID clientID, boolean success)
	{	try 
		{	System.out.println("trying to confirm join");
			message.setSuccess(success);
			sendPacket(message, clientID);
			message.clear();
/* 			String message = new String("join,");
			if(success)
				message += "success";
			else
				message += "failure";
			sendPacket(message, clientID);
*/		} 
		catch (IOException e) 
		{	e.printStackTrace();
	}	}
	
	// Informs a client that the avatar with the identifier remoteId has left the server. 
	// This message is meant to be sent to all client currently connected to the server 
	// when a client leaves the server.
	// Message Format: (bye,remoteId)
	
	public void sendByeMessages(UUID clientID)
	{	try 
		{	
			//String message = new String("bye," + clientID.toString());
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
	// Message Format: (create,remoteId,x,y,z) where x, y, and z represent the position

	public void sendCreateMessages(UUID clientID)//Vector3f position)//String[] position)
	{	try 
		{	
			/*String message = new String("create," + clientID.toString());
			message += "," + position[0];
			message += "," + position[1];
			message += "," + position[2];	*/
			forwardPacketToAll(message, clientID);
			message.clear();
		} 
		catch (IOException e) 
		{	e.printStackTrace();
	}	}
	
	// Informs a client of the details for a remote client�s avatar. This message is in response 
	// to the server receiving a DETAILS_FOR message from a remote client. That remote client�s 
	// message�s localId becomes the remoteId for this message, and the remote client�s message�s 
	// remoteId is used to send this message to the proper client. 
	// Message Format: (dsfr,remoteId,x,y,z) where x, y, and z represent the position.

	public void sendDetailsForMessage(UUID remoteId)//Vector3f position)//String[] position)
	{	try 
		{	message.addItem(Message.MessageType.DSFR);
			/*String message = new String("dsfr," + remoteId.toString());
			message += "," + position[0];
			message += "," + position[1];
			message += "," + position[2];	*/
			sendPacket(message, remoteID);
		} 
		catch (IOException e) 
		{	e.printStackTrace();
	}	}
	
	// Informs a local client that a remote client wants the local client�s avatar�s information. 
	// This message is meant to be sent to all clients connected to the server when a new client 
	// joins the server. 
	// Message Format: (wsds,remoteId)
	
	public void sendWantsDetailsMessages(UUID clientID)
	{	try 
		{	
			//String message = new String("wsds," + clientID.toString());	
			forwardPacketToAll(message, clientID);
		} 
		catch (IOException e) 
		{	e.printStackTrace();
	}	}
	
	// Informs a client that a remote client�s avatar has changed position. x, y, and z represent 
	// the new position of the remote avatar. This message is meant to be forwarded to all clients
	// connected to the server when it receives a MOVE message from the remote client.   
	// Message Format: (move,remoteId,x,y,z) where x, y, and z represent the position.

	public void sendMoveMessages(UUID clientID)//float[] position)//String[] position)
	{	try 
		{	
			/*String message = new String("move," + clientID.toString());
			message += "," + position[0];
			message += "," + position[1];
			message += "," + position[2];*/
			forwardPacketToAll(message, clientID);
		} 
		catch (IOException e) 
		{	e.printStackTrace();
	}	}
	
	public void sendTurnMessages(UUID clientID)//Matrix4f orientation)//String[] orientation)
	{	try 
		{	
			/*String message = new String("turn," + clientID.toString());
			message += "," + orientation[0];
			message += "," + orientation[1];
			message += "," + orientation[2];
			message += "," + orientation[3];
			message += "," + orientation[4];
			message += "," + orientation[5];
			message += "," + orientation[6];
			message += "," + orientation[7];
			message += "," + orientation[8];
			message += "," + orientation[9];
			message += "," + orientation[10];
			message += "," + orientation[11];
			message += "," + orientation[12];
			message += "," + orientation[13];
			message += "," + orientation[14];
			message += "," + orientation[15];*/
			forwardPacketToAll(message, clientID);
		} 
		catch (IOException e) 
		{	e.printStackTrace();
	}	}
}
